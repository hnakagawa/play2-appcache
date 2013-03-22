Play Appcache
=============

Application cache manifest controller for Play framework


機能
------
+  Assetリソースのディレクトリ指定
+  Assetファイル更新時にManifestファイルも自動更新(Devモード動作時)
+  動作モード(Dev,Prod)に応じた出力形式


ローカルリポジトリにインストールします
------

```
$ git clone https://github.com/hnakagawa/play2-appcache.git
$ cd play2-appcache/project-code
$ play publish-local
```

設定
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
'@[ファイルパス]'はassetリソースを指定した事を意味し、ファイルパスがディレクトリを指す場合は自動展開されます。
また、.coffeeや.less等の拡張子はそれぞれの出力ファイル形式(.js,.css)に置き換えられます。

```text
example = {
  cache = ["/", "@/app/assets/javascripts", "@/app/assets/stylesheets"]
  network = ["/api/*", "@/public/networks/network.coffee"]
  fallback = {
    "/" = "@/public/error.html"
  }
}
```

## 動作確認
see http://localhost:9000/example.appcache

Devモードでは以下が出力されます。
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

Prodモードでは以下が出力されます。
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
