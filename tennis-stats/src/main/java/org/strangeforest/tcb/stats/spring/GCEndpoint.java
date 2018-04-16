package org.strangeforest.tcb.stats.spring;

import org.springframework.boot.actuate.endpoint.annotation.*;
import org.springframework.stereotype.*;

@Component @Endpoint(id = "gc")
public class GCEndpoint {

	@ReadOperation
	public String gc() {
		System.gc();
		return "Garbage collection initiated";
	}
}