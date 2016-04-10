package org.strangeforest.tcb.stats.web;

import java.lang.annotation.*;

import org.springframework.context.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Profile("!dev")
public @interface VisitorSupport {}
