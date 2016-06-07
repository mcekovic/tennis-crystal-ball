package org.strangeforest.tcb.stats.controler;

import javax.servlet.http.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.web.servlet.handler.*;

@Component
public class DownForMaintenanceInterceptor extends HandlerInterceptorAdapter {

	@Value("${tennis-stats.down-for-maintenance:false}")
	private boolean downForMaintenance;

	private static final String MAINTENANCE_PATH = "/maintenance";

	@Override public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		if (downForMaintenance && !request.getServletPath().equals(MAINTENANCE_PATH)) {
			response.sendRedirect(request.getContextPath() + MAINTENANCE_PATH);
			return false;
		}
		else
			return true;
	}
}
