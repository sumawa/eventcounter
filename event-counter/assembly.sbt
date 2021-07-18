assemblyMergeStrategy in assembly := {
  case PathList("org", "apache", "commons", xs @ _*) => MergeStrategy.last
  case PathList("org", "apache", "hadoop", xs @ _*)  => MergeStrategy.last
  case PathList("META-INF", xs @ _*)                 => MergeStrategy.discard
  case PathList("org", "slf4j")                      => MergeStrategy.first
  case "mime.types"                                  => MergeStrategy.last
  case _                                             => MergeStrategy.first
}
