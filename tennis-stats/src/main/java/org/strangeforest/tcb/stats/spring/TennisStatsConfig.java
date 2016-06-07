package org.strangeforest.tcb.stats.spring;

import java.time.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.beans.factory.config.*;
import org.springframework.boot.autoconfigure.web.*;
import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.*;
import org.strangeforest.tcb.stats.controler.*;

import static java.util.Collections.*;

@Configuration
public class TennisStatsConfig extends WebMvcConfigurerAdapter {

	@Bean
	public static CustomEditorConfigurer customEditorConfigurer() {
		CustomEditorConfigurer editorConfigurer = new CustomEditorConfigurer();
		editorConfigurer.setCustomEditors(singletonMap(Duration.class, DurationPropertyEditor.class));
		return editorConfigurer;
	}

	@Bean
	public static ErrorAttributes errorAttributes() {
		return new TennisStatsErrorAttributes();
	}

	@Autowired DownForMaintenanceInterceptor downForMaintenanceInterceptor;

	@Override public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(downForMaintenanceInterceptor);
	}
}
