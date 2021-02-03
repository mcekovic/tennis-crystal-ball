package org.strangeforest.tcb.stats.visitors;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;
import javax.annotation.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.util.*;
import org.strangeforest.tcb.util.*;

import com.github.benmanes.caffeine.cache.*;
import com.maxmind.geoip2.record.Country;
import com.neovisionaries.i18n.*;
import io.micrometer.core.instrument.util.*;

import static java.util.stream.Collectors.*;

@Service @VisitorSupport
public class VisitorManager {

	@Autowired private VisitorRepository repository;
	@Autowired private GeoIPService geoIPService;

	@Value("${tennis-stats.visitors.expiry-period:PT1H}")
	private Duration expiryPeriod = Duration.ofHours(1);

	@Value("${tennis-stats.visitors.expiry-check-period:PT5M}")
	private Duration expiryCheckPeriod = Duration.ofMinutes(5);

	@Value("${tennis-stats.visitors.save-every-hit-count:10}")
	private int saveEveryHitCount = 10;

	@Value("${tennis-stats.visitors.cache-size:1000}")
	private int cacheSize = 1000;

	@Value("#{${tennis-stats.visitors.max-hits:{WEB_BROWSER: 5000, MOBILE_BROWSER: 5000, TEXT_BROWSER: 2000, TOOL: 2000, APP: 2000, UNKNOWN: 1000}}}")
	private Map<String, Integer> maxHits = Map.of("WEB_BROWSER", 5000, "MOBILE_BROWSER", 5000, "TEXT_BROWSER", 2000, "TOOL", 2000, "APP", 2000, "UNKNOWN", 1000);

	@Value("${tennis-stats.visitors.max-hit-rate:10.0}")
	private Double maxHitRate = 10.0;

	@Value("${tennis-stats.visitors.max-hit-rate-delay:PT5M}")
	private Duration maxHitRateDelay = Duration.ofMinutes(5L);

	private LockManager<String> lockManager;
	private LoadingCache<String, Optional<Visitor>> visitors;
	private ScheduledExecutorService visitorExpirer;
	private ScheduledFuture<?> visitorExpirerFuture;

	private static final String MAX_HITS_MESSAGE = "Maximum hits";
	private static final String MAX_HIT_RATE_MESSAGE = "Maximum hit rate";

	@PostConstruct
	public void init() {
		lockManager = new LockManager<>();
		visitors = Caffeine.newBuilder()
			.maximumSize(cacheSize)
			.removalListener(this::visitorRemoved)
			.build(repository::find);
		visitors.putAll(repository.findAll().stream().collect(toMap(Visitor::getIpAddress, Optional::of)));
		visitorExpirer = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Visitor Expirer"));
		var period = expiryCheckPeriod.getSeconds();
		visitorExpirerFuture = visitorExpirer.scheduleAtFixedRate(this::expire, period, period, TimeUnit.SECONDS);
	}

	@PreDestroy
	public void destroy() throws InterruptedException {
		if (visitors == null)
			return;
		try {
			try {
				if (visitorExpirer != null) {
					if (visitorExpirerFuture != null) {
						visitorExpirerFuture.cancel(false);
						visitorExpirerFuture = null;
					}
					visitorExpirer.shutdown();
					visitorExpirer.awaitTermination(15L, TimeUnit.SECONDS);
					visitorExpirer = null;
				}
			}
			finally {
				repository.saveAll(cachedVisitorStream().filter(Visitor::isDirty).collect(toList()));
				expire();
			}
		}
		finally {
			visitors.cleanUp();
			visitors = null;
		}
	}

	public Visit visit(String ipAddress, String agentType) {
		try {
			return doVisit(ipAddress, agentType);
		}
		catch (ExecutionException ex) {
			throw new TennisStatsException("Error tracking visit.", Optional.ofNullable(ex.getCause()).orElse(ex));
		}
		catch (Exception ex) {
			throw new TennisStatsException("Error tracking visit.", ex);
		}
	}

	private Visit doVisit(String ipAddress, String agentType) throws Exception {
		return lockManager.withLock(ipAddress, () -> {
			var optionalVisitor = visitors.get(ipAddress);
			if (optionalVisitor.isEmpty()) {
				var optionalCountry = geoIPService.getCountry(ipAddress);
				String countryId = null;
				String countryName = null;
				if (optionalCountry.isPresent()) {
					var country = optionalCountry.get();
					var code = CountryCode.getByCode(country.getIsoCode());
					if (code != null)
						countryId = code.getAlpha3();
					countryName = country.getName();
				}
				var visitor = repository.create(ipAddress, countryId, countryName, agentType);
				visitors.put(ipAddress, Optional.of(visitor));
				return new Visit(visitor);
			}
			else {
				var visitor = optionalVisitor.get();
				visitor.visit();
				if (visitor.isMaxHitsBreached(maxHits.get(agentType))) {
					visitor.unvisit();
					return new Visit(visitor, MAX_HITS_MESSAGE);
				}
				else if (visitor.isHitRateBreached(maxHitRate, maxHitRateDelay)) {
					visitor.unvisit();
					return new Visit(visitor, MAX_HIT_RATE_MESSAGE);
				}
				if (saveEveryHitCount > 0 && (visitor.getHits() % saveEveryHitCount == 0)) {
					repository.save(visitor);
					visitor.clearDirty();
				}
				return new Visit(visitor);
			}
		});
	}

	private void expire() {
		visitorStream().filter(visitor -> visitor.isExpired(expiryPeriod)).forEach(visitor -> {
			repository.expire(visitor);
			visitors.invalidate(visitor.getIpAddress());
		});
	}

	private Stream<Visitor> visitorStream() {
		return repository.findAll().stream();
	}

	private Stream<Visitor> cachedVisitorStream() {
		return visitors.asMap().values().stream().map(Optional::get);
	}

	void clearCache() {
		if (visitors != null)
			visitors.invalidateAll();
	}

	private void visitorRemoved(String ipAddress, Optional<Visitor> optionalVisitor, RemovalCause cause) {
		if (cause.wasEvicted()) {
			if (optionalVisitor != null && optionalVisitor.isPresent())
				repository.save(optionalVisitor.get());
		}
	}
}
