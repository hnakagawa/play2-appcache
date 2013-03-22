package appcache

import scala.collection.JavaConverters._
import scala.util.matching.Regex

import java.io.File
import java.util.concurrent.ConcurrentHashMap;
import java.util.{ ArrayList, Date }

import com.typesafe.config.ConfigFactory

import play.api._
import play.api.Play.current

object Manifest {
  private val manifestCache = (new ConcurrentHashMap[String, Manifest]()).asScala

  def apply(name:String, replace:String, base:String, configPath:String = "conf/appcache.conf"):Manifest = {
    if (Play.isProd) {
      val manifest = manifestCache.get(name).getOrElse(create(name, replace, base, configPath))
      manifestCache += (name -> manifest)
      manifest
    } else
      create(name, replace, base, configPath)
  }

  private def create(name:String, replace:String, base:String, configPath:String):Manifest = {
    val appcacheConfig = new Configuration(ConfigFactory.parseFileAnySyntax(new File(configPath)))
    val config = appcacheConfig.getConfig(name).get
    val replaceReg = new Regex(replace)

    new Manifest(name, loadCacheResource(config, replaceReg, base),
                 loadNetworkResource(config, replaceReg, base),
                 loadFallbackResource(config, replaceReg, base))
  }

  private def loadCacheResource(config:Configuration, replace:Regex, base:String):Seq[Resource] = {
    config.getStringList("cache").getOrElse(new ArrayList()).asScala.toSeq.flatMap {
      Resource(_, replace, base)
    }
  }

  private def loadNetworkResource(config:Configuration, replace:Regex, base:String):Seq[Resource] = {
    config.getStringList("network").getOrElse(new ArrayList()).asScala.toSeq.flatMap {
      Resource(_, replace, base)
    }
  }

  private def loadFallbackResource(config:Configuration, replace:Regex, base:String):Map[Resource,Resource] = {
    val fallbackConfig = config.getConfig("fallback").getOrElse(Configuration.empty)

    fallbackConfig.subKeys.map { key =>
      (key, fallbackConfig.getString("\"" + key + "\"").get)
    }.toMap.flatMap { entry =>
      val s = Resource(entry._2, replace, base)
      Resource(entry._1, replace, base).map {
        (_, s(0))
      }.toMap
    }
  }
}

class Manifest private (val name:String, val cacheResources:Seq[Resource], val networkResources:Seq[Resource],
                        val fallbackResources:Map[Resource, Resource]) {

  lazy val lastModified = {
    val last = (fallbackResources.toList.flatMap { entry =>
      List(entry._1, entry._2)
    } ++ cacheResources).foldLeft(0L) { (a, b) =>
      a.max(b.lastModified)
    }

    "%tF %<tT.%<tL\n".format(new Date(last))
  }

  lazy val content =
    "CACHE MANIFEST\n" +
     "#" + lastModified +
     cacheSection +
     networkSection +
     fallbackSection

  private def cacheSection =
    if (cacheResources.size > 0) "CACHE:\n" + toSection(cacheResources)
    else ""

  private def networkSection =
    if (networkResources.size > 0) "NETWORK:\n" + toSection(networkResources)
    else ""

  private def fallbackSection =
    if (fallbackResources.size > 0) "FALLBACK:\n" + toSection(fallbackResources)
    else ""

  private def toSection(resources:Seq[Resource]):String = {
    resources.foldRight("") {
      _ + "\n" + _
    }
  }

  private def toSection(resources:Map[Resource,Resource]):String = {
    resources.foldRight("") { (a, b) =>
      a._1 + " " + a._2 + "\n" + b
    }
  }
}
