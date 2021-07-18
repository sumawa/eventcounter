addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.9")

addSbtPlugin("org.foundweekends.giter8" % "sbt-giter8-scaffold" % "0.11.0")

addSbtPlugin("org.scalatra.sbt" % "sbt-scalatra" % "1.0.2")

addSbtPlugin("com.geirsson" % "sbt-scalafmt" % "1.5.1")

addSbtPlugin("io.github.davidmweber" % "flyway-sbt" % "5.2.0")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.10.0-RC1")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.0")

addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3")

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.5.0")

resolvers += Resolver.sonatypeRepo("releases")

resolvers += "Flyway".at("https://davidmweber.github.io/flyway-sbt.repo")

resolvers += "jgit-repo".at("https://download.eclipse.org/jgit/maven")
