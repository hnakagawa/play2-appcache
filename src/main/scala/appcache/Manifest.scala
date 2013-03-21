package appcache

import scala.collection.JavaConverters._

import java.io.File
import java.util.concurrent.ConcurrentHashMap;
import java.util.{ ArrayList, Date }

import com.typesafe.config.ConfigFactory

import play.api._
import play.api.mvc._
import play.api.Play.current

object Manifest {
  private val manifestCache = (new ConcurrentHashMap[String, Manifest]()).asScala

  def apply(name:String):Manifest = {
    val appcacheConfig = new Configuration(ConfigFactory.parseFileAnySyntax(new File("conf/appcache.conf")))
    val config = appcacheConfig.getConfig(name).get
    Manifest(name, loadCacheResource(config), loadNetworkResource(config), loadFallbackResource(config))
  }

  def apply(name:String, cacheResources:Seq[String], networkResources:Seq[String],
	    fallbackResources:Map[String,String]):Manifest = {
    if (Play.isProd) manifestCache.get(name).getOrElse(new Manifest(name, cacheResources, networkResources, fallbackResources))
    else new Manifest(name, cacheResources, networkResources, fallbackResources)
  }

  private def loadCacheResource(config:Configuration) =
    config.getStringList("cache").getOrElse(new ArrayList()).asScala.toSeq

  private def loadNetworkResource(config:Configuration) =
    config.getStringList("network").getOrElse(new ArrayList()).asScala.toSeq

  private def loadFallbackResource(config:Configuration) = {
    val fallbackConfig = config.getConfig("fallback").getOrElse(Configuration.empty)
    fallbackConfig.subKeys.map { key =>
      (key, fallbackConfig.getString("\"" + key + "\"").get)
    }.toMap
  }
}

class Manifest(val name:String, cacheResources:Seq[String], networkResources:Seq[String],
	       val fallbackResources:Map[String, String]) {
  val cacheAssets = toAssets(cacheResources)

  val networkAssets = toAssets(networkResources)

  val fallbackAssets = toAssets(fallbackResources)

  lazy val lastModified = {
    val last = cacheAssets.foldLeft(0L) { (a, b) =>
      a.max(b.lastModified)
    }

    "%tF %<tT.%<tL".format(new Date(last))
  }

  lazy val content = {
    ("CACHE MANIFEST\n" +
     "#%s\n" +
     "CACHE:\n" +
     "%s" +
     "NETWORK:\n" +
     "%s" +
     "FALLBACK:\n" +
     "%s").format(lastModified, cacheSection, networkSection, fallbackSection)
  }

  private def toAssets(resources:Seq[String]):Seq[File] = {
    resources.flatMap { r =>
      find(new File(Play.current.path, r))
    }
  }

  private def toAssets(resources:Map[String,String]):Map[File,File] = {
    resources.flatMap { r =>
      val t = new File(Play.current.path, r._2)
      find(new File(Play.current.path, r._1)).map{ (_, t) }.toMap
    }
  }

  private def find(file: File): Seq[File] = {
    if (!file.exists || !file.isDirectory)
      return Seq(file)

    file.listFiles.flatMap { find(_) }
  }

  private def cacheSection = toSection(cacheAssets)

  private def networkSection = toSection(networkAssets)

  private def fallbackSection = toSection(fallbackAssets)

  private def toSection(assets:Seq[File]) = {
    assets.foldRight("") { (a, b) =>
      toRelativeUrl(a.getCanonicalPath) + "\n" + b
    }
  }

  private def toSection(assets:Map[File,File]) = {
    assets.foldRight("") { (a, b) =>
      toRelativeUrl(a._1.getCanonicalPath) + " " + toRelativeUrl(a._2.getCanonicalPath) + "\n" + b
    }
  }

  private val COFFEE_EXT_REG = """\.coffee$""".r

  private val LESS_EXT_REG = """\.less$""".r

  private val ASSET_EXT = if (Play.isProd) (".min.js", ".min.css") else (".js", ".css")

  private val ASSET_PATH_REG = """^/app/assets/|/public/""".r

  private val ASSET_BASE = "/assets/"
  
  private def toRelativeUrl(path:String) =
    replaceExtension(replaceAsset(path.substring(Play.current.path.getCanonicalPath.length)))

  private def replaceAsset(url:String) = {
    if (url == "/*") "*"
    else ASSET_PATH_REG.replaceAllIn(url, ASSET_BASE)
  }

  private def replaceExtension(url:String) =
    LESS_EXT_REG.replaceAllIn(COFFEE_EXT_REG.replaceAllIn(url, ASSET_EXT._1), ASSET_EXT._2)
}
