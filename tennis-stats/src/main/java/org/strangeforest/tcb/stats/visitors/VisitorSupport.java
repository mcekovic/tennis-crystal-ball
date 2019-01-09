package org.strangeforest.tcb.stats.visitors;

import java.lang.annotation.*;

import org.springframework.context.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Profile("!dev")
public @interface VisitorSupport {}
