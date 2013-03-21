Play Appcache
=============

Application cache manifest controller for Play framework


Todo
-----
+   External resource support


## project/Build.scala

```scala
  val appDependencies = Seq(
    // Add your project dependencies here,
    "com.github.hnakagawa" %% "play-appcache" % "0.0.1-SNAPSHOT",
    jdbc,
    anorm
  )

  lazy val appcache_scala = uri("http://github.com/hnakagawa/play_appcache.git")

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here
    resolvers += "test" at "http://github.com/hnakagawa/play_appcache.git",
    name := "play_appcache",
    organization := "com.github.hnakagawa",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.10.0"
  ).dependsOn(appcache_scala)
```


## conf/routes

```text
GET     /assets/*file               controllers.Assets.at(path="/public", file)

GET     /*name.appcache             appcache.AppCacheAssets.at(name)
```

## conf/appcache.conf

```text
example = {
  cache = ["app/assets/javascripts", "app/assets/stylesheets", "public"]
  network = ["*"]
}
```

see http://localhost:9000/example.appcache