Play Appcache
=============

HTML5 Application cache manifest controller for Play framework


Feature
------
+  Can specify an asset resource directory
+  Update manifest automatically if asset files are updated(Dev mode)
+  Output manifest is changed according to the mode(Dev,Prod)


Install to local repository
------

```
$ git clone https://github.com/hnakagawa/play2-appcache.git
$ cd play2-appcache/project-code
$ play publish-local
```

Configuration
------
## project/Build.scala

```scala
  val appDependencies = Seq(
    // Add your project dependencies here,
    "play2-appcache" %% "play2-appcache" % "0.0.1-SNAPSHOT",
    jdbc,
    anorm
  )
```

## conf/routes

```text
GET     /assets/*file               controllers.Assets.at(path="/public", file)

GET     /*name.appcache             controllers.AppCacheAssets.at(name)
```

## conf/appcache.conf
'@[file path]' means asset resource.

```text
example = {
  cache = ["/", "@/app/assets/javascripts", "@/app/assets/stylesheets"]
  network = ["/api/*", "@/public/networks/network.coffee"]
  fallback = {
    "/" = "@/public/error.html"
  }
}
```

## Confirm behavior
see http://localhost:9000/example.appcache

The following is output in Dev mode.
```text
CACHE MANIFEST
#2013-03-24 23:27:51.000
CACHE:
/
/assets/javascripts/example.js
/assets/stylesheets/example.css
NETWORK:
/api/*
/assets/networks/network.js
FALLBACK:
/ /assets/error.html
```

The following is output in Prod mode.
```text
CACHE MANIFEST
#2013-03-24 23:27:51.000
CACHE:
/
/assets/javascripts/example.min.js
/assets/stylesheets/example.min.css
NETWORK:
/api/*
/assets/networks/network.min.js
FALLBACK:
/ /assets/error.html
```
