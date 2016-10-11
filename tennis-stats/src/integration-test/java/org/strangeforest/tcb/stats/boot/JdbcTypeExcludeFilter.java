package org.strangeforest.tcb.stats.boot;

import java.io.*;
import java.util.*;

import org.springframework.boot.test.autoconfigure.filter.*;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.*;
import org.springframework.core.type.classreading.*;

class JdbcTypeExcludeFilter extends AnnotationCustomizableTypeExcludeFilter {

	private final JdbcTest annotation;

	JdbcTypeExcludeFilter(Class<?> testClass) {
		annotation = AnnotatedElementUtils.getMergedAnnotation(testClass, JdbcTest.class);
	}

	@Override protected boolean hasAnnotation() {
		return annotation != null;
	}

	@Override protected ComponentScan.Filter[] getFilters(FilterType type) {
		switch (type) {
			case INCLUDE:
				return annotation.includeFilters();
			case EXCLUDE:
				return annotation.excludeFilters();
		}
		throw new IllegalStateException("Unsupported filter type: " + type);
	}

	@Override protected boolean isUseDefaultFilters() {
		return annotation.useDefaultFilters();
	}

	@Override protected boolean defaultInclude(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
		return false;
	}

	@Override protected Set<Class<?>> getDefaultIncludes() {
		return Collections.emptySet();
	}
}
