package org.strangeforest.tcb.stats.web;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;
import javax.annotation.*;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import com.google.common.cache.*;

import static java.util.stream.Collectors.*;

@Service
public class VisitorManager {

	private VisitorRepository repository;
	@Value("tennis-stats.visitors.expiryTimeout") private Duration expiryTimeout = Duration.ofHours(1);
	@Value("tennis-stats.visitors.expiryCheckPeriod") private Duration expiryCheckPeriod = Duration.ofMinutes(5);
	@Value("tennis-stats.visitors.saveAfterVisitCount") private int saveAfterVisitCount = 10;
	@Value("tennis-stats.visitors.cacheSize") private int cacheSize = 1000;

	private LoadingCache<String, Optional<Visitor>> visitors;
	private ScheduledExecutorService visitorExpirer;
	private ScheduledFuture<?> visitorExpirerFuture;

	private static Logger LOGGER = LoggerFactory.getLogger(VisitorManager.class);

	@Autowired
	public VisitorManager(VisitorRepository repository) {
		this.repository = repository;
	}

	void setSaveAfterVisitCount(int saveAfterVisitCount) {
		this.saveAfterVisitCount = saveAfterVisitCount;
	}

	@PostConstruct
	public void init() {
		visitors = CacheBuilder.newBuilder().maximumSize(cacheSize).build(
			new CacheLoader<String, Optional<Visitor>>() {
				public Optional<Visitor> load(String ipAddress) {
					return repository.find(ipAddress);
				}
			}
		);
		visitors.putAll(repository.findAll().stream().collect(toMap(Visitor::getIpAddress, Optional::of)));
		visitorExpirer = Executors.newSingleThreadScheduledExecutor();
		long period = expiryCheckPeriod.getSeconds();
		visitorExpirerFuture = visitorExpirer.scheduleAtFixedRate(this::expire, period, period, TimeUnit.SECONDS);
	}

	@PreDestroy
	public void destroy() throws InterruptedException {
		try {
			try {
				if (visitorExpirer != null) {
					if (visitorExpirerFuture != null)
						visitorExpirerFuture.cancel(false);
					visitorExpirer.shutdown();
					visitorExpirer.awaitTermination(15L, TimeUnit.SECONDS);
				}
			}
			finally {
				expire();
				repository.saveAll(visitorStream().collect(toList()));
			}
		}
		finally {
			visitors.cleanUp();
		}
	}

	public void visit(String ipAddress) {
		try {
			Optional<Visitor> optionalVisitor = visitors.get(ipAddress);
			if (!optionalVisitor.isPresent()) {
				optionalVisitor = Optional.of(repository.create(ipAddress));
				visitors.put(ipAddress, optionalVisitor);
			}
			else {
				Visitor visitor = optionalVisitor.get();
				if (visitor.visit() % saveAfterVisitCount == 0)
					repository.save(visitor);
			}
		}
		catch (ExecutionException ex) {
			LOGGER.error("Error tracking visit.", ex);
		}
	}

	private void expire() {
		visitorStream().filter(visitor -> visitor.isExpired(expiryTimeout)).forEach(visitor -> {
			repository.expire(visitor);
			visitors.invalidate(visitor.getIpAddress());
		});
	}

	private Stream<Visitor> visitorStream() {
		return visitors.asMap().values().stream().map(Optional::get);
	}
}
