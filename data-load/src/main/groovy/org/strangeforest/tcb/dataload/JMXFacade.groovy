package org.strangeforest.tcb.dataload

import javax.management.remote.*

abstract class JMXFacade {

	static clearCaches() {
		println new GroovyMBean(serverConnection, 'org.springframework.boot:type=Endpoint,name=Cache').clearCache('')
	}

	static restartConnector() {
		println new GroovyMBean(serverConnection, 'org.springframework.boot:type=Endpoint,name=Restart').restartConnector()
	}

	private static getServerConnection() {
		JMXConnectorFactory.connect(new JMXServiceURL('service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi')).MBeanServerConnection
	}
}

