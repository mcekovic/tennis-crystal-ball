package org.strangeforest.tcb.dataload

import javax.management.remote.*

clearCaches()

static clearCaches() {
	def server = JMXConnectorFactory.connect(new JMXServiceURL('service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi')).MBeanServerConnection
	println  new GroovyMBean(server, 'org.springframework.boot:type=Endpoint,name=Cache').clearCache('')
}

