package org.strangeforest.tcb.stats.spring;

import java.text.*;
import java.util.*;

import org.strangeforest.tcb.stats.model.core.*;
import org.strangeforest.tcb.stats.model.price.*;
import org.thymeleaf.context.*;
import org.thymeleaf.dialect.*;
import org.thymeleaf.expression.*;

public class UTSThymeleafDialect extends AbstractDialect implements IExpressionObjectDialect {

	private IExpressionObjectFactory expressionObjectFactory;

	public UTSThymeleafDialect() {
		super("UTS");
	}

	@Override public IExpressionObjectFactory getExpressionObjectFactory() {
		if (expressionObjectFactory == null)
			expressionObjectFactory = new UTSExpressionObjectFactory();
		return expressionObjectFactory;
	}

	private static class UTSExpressionObjectFactory implements IExpressionObjectFactory {

		private static final UTSUtil utsUtil = new UTSUtil();
		private static final String UTS = "uts";

		@Override public Set<String> getAllExpressionObjectNames() {
			return Collections.singleton(UTS);
		}

		@Override public Object buildObject(IExpressionContext context, String expressionObjectName) {
			return utsUtil;
		}

		@Override public boolean isCacheable(String expressionObjectName) {
			return true;
		}
	}

	private static final class UTSUtil {

		public TournamentLevel level(String level) {
			return TournamentLevel.decode(level);
		}

		public Surface surface(String surface) {
			return Surface.decode(surface);
		}

		public Round round(String round) {
			return Round.decode(round);
		}

		public EventResult result(String result) {
			return EventResult.decode(result);
		}

		public String formatDecimal(double number, String format) {
			return new DecimalFormat(format).format(number);
		}

		public String toFormattedPrice(double probability, String format) {
			return PriceUtil.toFormattedPrice(probability, format);
		}

		public boolean isCloseTo(double d1, double d2, double offset) {
			return Math.abs(d1 - d2) <= offset;
		}
	}
}
