package org.strangeforest.tcb.util;

import java.util.function.*;

public abstract class ExceptionUtil {

	private static final Thrower<RuntimeException> THROWER = new Thrower<>();

	public static RuntimeException propagate(Throwable exception) {
		return THROWER.throwIt(exception);
	}

	private static class Thrower<T extends Throwable> {
		private T throwIt(Throwable exception) throws T {
			throw (T)exception;
		}
	}

	public static <T, R, E extends Exception> Function<T, R> uncheckedWrap(ThrowingFunction<T, R, E> function) {
		return t -> {
			try {
				return function.apply(t);
			}
			catch (Exception ex) {
				throw propagate(ex);
			}
		};
	}

	@FunctionalInterface
	public interface ThrowingFunction<T, R, E extends Exception> {
		R apply(T t) throws E;
	}
}
