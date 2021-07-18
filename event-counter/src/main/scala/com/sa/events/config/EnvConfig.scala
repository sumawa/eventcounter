package com.sa.events.config

case class EnvConfig(activeEnv: String, homeDir: String, triggerInterval: Int) {

  def getExternalConfigPath: String =
    {println("GETTING EXTERNAL"); s"${this.homeDir}/${this.activeEnv}.conf".toLowerCase}

}

object EnvConfig {
  val namespace: String = "env"
}
