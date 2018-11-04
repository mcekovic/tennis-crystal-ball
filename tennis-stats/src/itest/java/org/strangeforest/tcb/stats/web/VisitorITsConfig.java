package org.strangeforest.tcb.stats.web;

import java.time.*;

import org.springframework.beans.factory.config.*;
import org.springframework.boot.test.context.*;
import org.springframework.context.annotation.*;
import org.springframework.transaction.annotation.*;
import org.strangeforest.tcb.stats.*;

import static java.util.Collections.singletonMap;

@TestConfiguration
@PropertySource("/visitors-test.properties")
@EnableTransactionManagement
@Import(DataSourceITConfig.class)
public class VisitorITsConfig {

	@Bean
	public VisitorManager visitorManager() {
		return new VisitorManager();
	}

	@Bean
	public VisitorRepository visitorRepository() {
		return new VisitorRepository();
	}

	@Bean
	public GeoIPService geoIPService() {
		return new GeoIPService();
	}

	@Bean
	public static CustomEditorConfigurer customEditorConfigurer() {
		CustomEditorConfigurer editorConfigurer = new CustomEditorConfigurer();
		editorConfigurer.setCustomEditors(singletonMap(Duration.class, DurationPropertyEditor.class));
		return editorConfigurer;
	}
}
