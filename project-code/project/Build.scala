import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {
  val appName         = "play2-appcache"
  val appVersion      = "0.0.1-SNAPSHOT"

  val appDependencies = Seq("play" %% "play" % "2.1.0",
			    "org.specs2" %% "specs2" % "1.14" % "test")

  val main = play.Project(appName, appVersion, appDependencies).settings(
  )
}
