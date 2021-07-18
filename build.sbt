import Dependencies._
import sbt.addCompilerPlugin

javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

lazy val commonSettings = Seq(
  organization := "com.sa",
  version := "0.1.0",
  scalaVersion := "2.12.10",
  scalacOptions := Seq("-deprecation", "-feature", "-encoding", "utf8"),
  test in assembly := {},
  fork in Test := true,
  exportJars in Test := false,
  parallelExecution in Test := false,
  coverageEnabled.in(Test, test) := true,
  coverageEnabled in (Compile, compile) := false,
  addCompilerPlugin(kindDependency),
  (scalacOptions in Compile) ++= scalacOptionsForVersion(scalaVersion.value),
  (scalacOptions in Test) --= Seq(
    "-Ywarn-dead-code",
    "-Ywarn-value-discard"
  ),
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint"),
  //,credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
)

lazy val root = (project in file("."))
  .settings(name := "eventcounter")
  .settings(unmanagedSourceDirectories in Compile := Nil)
  .aggregate(eventCounter)

lazy val eventCounter = (project in file("event-counter"))
  .settings(commonSettings: _*)
  .settings(name := "event-counter")
  .settings(exportJars := true)
  .settings(libraryDependencies ++= circeDepedencies)
  .settings(libraryDependencies ++= testDependencies)
  .settings(libraryDependencies ++= catsDependencies)
  .settings(libraryDependencies ++= log4catsDependencies)
  .settings(libraryDependencies ++= log4jDependencies)
  .settings(libraryDependencies ++= http4sClientDependencies)
  .settings(libraryDependencies ++= pureConfigDependencies)
  .settings(libraryDependencies ++= fs2Dependencies)
  .settings(libraryDependencies ++= jodaTimeDependencies)
  .settings(libraryDependencies ++= kittenDependencies)
  .settings(libraryDependencies ++= doobieDependencies)
  .settings(libraryDependencies ++= dbConnectorDependencies)
  .settings(libraryDependencies ++= flywayDependencies)
  .settings(libraryDependencies ++= euTimepitDependencies)
  .settings(libraryDependencies ++= sparkDependencies)
  .enablePlugins(ScalafmtPlugin)

cleanFiles += baseDirectory { base =>
  base / "temp"
}.value

def scalacOptionsForVersion(version: String): Seq[String] = {
  // format: off
  val defaultOpts = Seq(
    "-deprecation", // Emit warning and location for usages of deprecated APIs.
    "-encoding", "utf-8", // Specify character encoding used by source files.
    "-explaintypes", // Explain type errors in more detail.
    "-feature", // Emit warning and location for usages of features that should be imported explicitly.
    "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
    "-language:experimental.macros", // Allow macro definition (besides implementation and application)
    "-language:higherKinds", // Allow higher-kinded types
    "-language:implicitConversions", // Allow definition of implicit functions called views
    "-unchecked", // Enable additional warnings where generated code depends on assumptions.
    "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
    "-Xlint:adapted-args", // Warn if an argument list is modified to match the receiver.
    "-Xlint:constant", // Evaluation of a constant arithmetic expression results in an error.
    "-Xlint:delayedinit-select", // Selecting member of DelayedInit.
    "-Xlint:doc-detached", // A Scaladoc comment appears to be detached from its element.
    "-Xlint:inaccessible", // Warn about inaccessible types in method signatures.
    "-Xlint:infer-any", // Warn when a type argument is inferred to be `Any`.
    "-Xlint:missing-interpolator", // A string literal appears to be missing an interpolator id.
    "-Xlint:nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
    "-Xlint:nullary-unit", // Warn when nullary methods return Unit.
    "-Xlint:option-implicit", // Option.apply used implicit view.
    "-Xlint:package-object-classes", // Class or object defined in package object.
    "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
    "-Xlint:private-shadow", // A private field (or class parameter) shadows a superclass field.
    "-Xlint:stars-align", // Pattern sequence wildcard must align with sequence component.
    "-Xlint:type-parameter-shadow", // A local type parameter shadows a type already in scope.
    "-Ywarn-dead-code", // Warn when dead code is identified.
    "-Ywarn-extra-implicit", // Warn when more than one implicit parameter section is defined.
    "-Ywarn-numeric-widen", // Warn when numerics are widened.
    "-Ywarn-unused:implicits", // Warn if an implicit parameter is unused.
    "-Ywarn-unused:locals", // Warn if a local definition is unused.
    "-Ywarn-unused:params", // Warn if a value parameter is unused.
    "-Ywarn-unused:patvars", // Warn if a variable bound in a pattern is unused.
    "-Ywarn-unused:privates", // Warn if a private member is unused.
    "-Ywarn-value-discard"  // Warn when non-Unit expression results are unused.
  )
  val versionOpts: Seq[String] = CrossVersion.partialVersion(version) match {
    case Some((2, major)) if major < 13 => Seq(
      "-Xlint:by-name-right-associative", // By-name parameter of right associative operator.
      "-Xlint:unsound-match", // Pattern match may not be typesafe.
      // "-Xfatal-warnings", // Fail the compilation if there are any warnings.
      "-Xfuture", // Turn on future language features.
      "-Yno-adapted-args", // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
      "-Ypartial-unification", // Enable partial unification in type constructor inference
      "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
      "-Ywarn-infer-any", // Warn when a type argument is inferred to be `Any`.
      "-Ywarn-nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
      "-Ywarn-nullary-unit" // Warn when nullary methods return Unit.
    )
    case _ => Seq()
  }

  defaultOpts ++ versionOpts
}

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}
