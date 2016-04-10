package org.strangeforest.tcb.stats.spring;

import java.time.*;

import org.springframework.beans.factory.config.*;
import org.springframework.context.annotation.*;

import static java.util.Collections.*;

@Configuration
public class PropertyEditorsConfig {

	@Bean
	public static CustomEditorConfigurer tennisStatsEditorConfigurer() {
		CustomEditorConfigurer editorConfigurer = new CustomEditorConfigurer();
		editorConfigurer.setCustomEditors(singletonMap(Duration.class, DurationPropertyEditor.class));
		return editorConfigurer;
	}
}
