import sbt._

object Dependencies {

  object Versions {
    val scalaTestV            = "3.0.8"
    //val hadoopV               = "2.7.0-mapr-1808"
    val log4jV                = "2.12.0"
    val orgSlf4jV             = "1.7.25"
    val circeV                = "0.12.3"
    val circeConfigV          = "0.7.0"
    val enumeratumCirceV      = "1.5.21"
    val doobieV               = "0.8.6"
    val flywayV               = "5.2.4"
    val enumeratumQuillV      = "1.5.14"
    val kindProjectorV        = "0.10.3"
    val log4catV              = "1.0.1"
    val scalaCheckV           = "1.14.0"
    val catsScalaCheckV       = "0.2.0"
//    val http4sV               = "0.20.7"
    val http4sV               = "0.21.22"
    val pureConfigV           = "0.13.0"
    val circeShapesV          = "0.7.0"
    val catsV                 = "2.0.0"
    val kittensV              = "2.0.0"
    val mariadbJavaClientV    = "2.5.4"
    val h2V                   = "1.4.199"
    val sparkV                = "3.0.0"
    val redisV                = "0.12.0"
  }

  object ExclusionRules {
    val orgSlf4j              = ExclusionRule(organization = "org.slf4j")
    val ldapApi               = ExclusionRule(organization = "org.apache.directory.api")
    val comGoogleCodeFindbugs = ExclusionRule(organization = "com.google.code.findbugs")
    val hikari                = ExclusionRule(organization = "com.zaxxer")
  }

  import Versions._

  val circeDepedencies = Seq(
    "io.circe"     %% "circe-core"           % circeV,
    "io.circe"     %% "circe-generic"        % circeV,
    "io.circe"     %% "circe-literal"        % circeV,
    "io.circe"     %% "circe-parser"         % circeV,
    "io.circe"     %% "circe-generic-extras" % "0.12.2",
    "io.circe"     %% "circe-config"         % circeConfigV,
    "io.circe" %% "circe-refined" % circeV,
  "com.beachape" %% "enumeratum-circe"     % enumeratumCirceV
  )

  val testDependencies = Seq(
    "org.scalactic"     %% "scalactic"       % scalaTestV, // apache
    "org.scalatest"     %% "scalatest"       % scalaTestV % Test, // apache
    "org.scalacheck"    %% "scalacheck"      % scalaCheckV % Test,
    "io.chrisdavenport" %% "cats-scalacheck" % catsScalaCheckV % Test,
//    "org.scalamock"     %% "scalamock"       % scalaMockV % Test,
    "org.typelevel" %% "cats-laws" % "2.0.0" % Test,
    "com.github.alexarchambault" %% "scalacheck-shapeless_1.14" % "1.2.3" % Test
    ,  "org.typelevel" %% "scalacheck-effect" % "0.2.0"
    , "com.codecommit" %% "cats-effect-testing-specs2" % "0.2.0" % Test
    , "io.cucumber" %% "cucumber-scala" % "6.9.0"
    , "io.cucumber" % "cucumber-junit" % "6.9.0"
    , "junit" % "junit" % "4.12"
  )

  val http4sClientDependencies = Seq(
    "org.http4s" %% "http4s-dsl"          % http4sV,
    "org.http4s"      %% "http4s-blaze-server" % http4sV,
      "org.http4s" %% "http4s-blaze-client" % http4sV
    , "org.http4s"  %% "http4s-circe"     % http4sV // supply some utility methods to convert the Encoder/Decoder of circe to the EntityEncoder/EntityDecoder of http4s
  )

  val catsDependencies = Seq(
    "org.typelevel" %% "cats-core"   % catsV,
    "org.typelevel" %% "cats-free"   % catsV,
    "org.typelevel" %% "cats-effect" % catsV,
    "org.typelevel" %% "alleycats-core" % catsV,
    "io.chrisdavenport" %% "cats-effect-time" % "0.1.0"

  )

  val kindDependency =
    ("org.typelevel" %% "kind-projector" % kindProjectorV).cross(CrossVersion.binary)

  val log4catsDependencies = Seq(
    "io.chrisdavenport" %% "log4cats-slf4j" % log4catV // Direct Slf4j Support - Recommended
  )

  val log4jDependencies = Seq(
    "org.apache.logging.log4j" % "log4j-api"        % log4jV,
    "org.apache.logging.log4j" % "log4j-core"       % log4jV,
    "org.apache.logging.log4j" % "log4j-slf4j-impl" % log4jV
  )

  val pureConfigDependencies = Seq(
    "com.github.pureconfig" %% "pureconfig"             % pureConfigV,
    "com.github.pureconfig" %% "pureconfig-cats-effect" % pureConfigV,
    "com.github.pureconfig" %% "pureconfig-enumeratum"  % pureConfigV
  )

  val fs2Dependencies = Seq(
    "co.fs2" %% "fs2-core" % "2.5.0"
    , "co.fs2" %% "fs2-io" % "2.5.0"
  )

  val kafkaDependencies = Seq(
    "org.apache.kafka" %% "kafka" % "2.7.0"
  )

  val redisDependencies = Seq(
    "dev.profunktor" %% "redis4cats-effects" % redisV
    , "dev.profunktor" %% "redis4cats-streams" % redisV
  )

  val jodaTimeDependencies = Seq(
    "joda-time" % "joda-time" % "2.10.2"
  )

  val kittenDependencies = Seq(
    "org.typelevel" %% "kittens" % kittensV
  )

  val doobieDependencies = Seq(
    "org.tpolecat" %% "doobie-core"      % doobieV,
    "org.tpolecat" %% "doobie-hikari"    % doobieV,
    "org.tpolecat" %% "doobie-quill"     % doobieV,
    "org.tpolecat" %% "doobie-scalatest" % doobieV % Test,
    "org.tpolecat" %% "doobie-h2"        % doobieV % Test
    , "org.tpolecat" %% "doobie-postgres"        % doobieV % Test
    , "org.tpolecat" %% "doobie-h2"        % doobieV
    , "com.beachape" %% "enumeratum-doobie" % enumeratumQuillV
//    , "com.opentable.components" % "otj-pg-embedded" % doobieV % Test
    , "com.opentable.components" % "otj-pg-embedded" % "0.13.3" % Test
  )

  val dbConnectorDependencies = Seq(
    "org.mariadb.jdbc" % "mariadb-java-client" % mariadbJavaClientV // LGPL-2.1
    , "com.h2database"   % "h2"                  % h2V % Test // EPL 1.0, MPL 2.0
    , "org.postgresql" % "postgresql" % "42.2.18"
  )

  val flywayDependencies = Seq(
    "org.flywaydb" % "flyway-core" % flywayV
  )

  val euTimepitDependencies = Seq(
    "eu.timepit" %% "refined" % "0.9.15"
    , "eu.timepit" %% "refined-cats" % "0.9.15"
    , "eu.timepit" %% "refined-pureconfig" % "0.9.15"
    , "eu.timepit" %% "refined-scalacheck" % "0.9.15"
  )

  val sparkDependencies = Seq(
    ("org.apache.spark" %% "spark-core" % sparkV % Provided)
      .withSources() // apache
    , ("org.apache.spark" %% "spark-mllib" % sparkV % Provided)
      .withSources() // apache
    , ("org.apache.spark" %% "spark-hive" % sparkV % Provided)
      .withSources() // apache
    , ("org.apache.spark" %% "spark-streaming" % sparkV % Provided)
      .withSources() // apache
  )

}
