name := "lightning"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.2",
  "org.webjars" %% "webjars-play" % "2.2.1-2",
  "org.scalatest" %% "scalatest" % "2.0" % "test"
)

play.Project.playScalaSettings
