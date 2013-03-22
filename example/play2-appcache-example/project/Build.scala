import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "appcache_example"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    "play2-appcache" %% "play2-appcache" % "0.0.1-SNAPSHOT",
    jdbc,
    anorm
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here
  )
}
