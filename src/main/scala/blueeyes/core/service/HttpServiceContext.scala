package blueeyes.core.service

import net.lag.configgy.ConfigMap

case class ServiceContext(config: ConfigMap, serviceName: String, serviceVersion: ServiceVersion, hostName: String, port: Int, sslPort: Int) {
  override def toString = serviceName + ".v" + serviceVersion.majorVersion
}
