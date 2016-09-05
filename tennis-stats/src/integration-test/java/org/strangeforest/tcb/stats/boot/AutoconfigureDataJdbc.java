package org.strangeforest.tcb.stats.boot;

import java.lang.annotation.*;

import org.springframework.boot.autoconfigure.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ImportAutoConfiguration
public @interface AutoconfigureDataJdbc {}


