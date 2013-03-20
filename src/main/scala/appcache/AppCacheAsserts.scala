package appcache

import scala.collection.JavaConverters._

import java.io.File
import java.util.concurrent.ConcurrentHashMap;
import java.util.{ ArrayList, Date }

import com.typesafe.config.ConfigFactory

import play.api._
import play.api.mvc._
import play.api.Play.current

object AppCacheAssets extends AppCacheAssetsBuilder

class AppCacheAssetsBuilder extends Controller {
  def at(name:String): Action[AnyContent] = Action { request =>
    try {
      Ok(Manifest(name).content).as("text/cache-manifest")
    } catch {
      case exp:NoSuchElementException => NotFound
    }
  }
}

object Manifest {
  private val manifestCache = (new ConcurrentHashMap[String, Manifest]()).asScala

  def apply(name:String):Manifest = {
    val appcacheConfig = new Configuration(ConfigFactory.parseFileAnySyntax(new File("conf/appcache.conf")))
    val config = appcacheConfig.getConfig(name).get

    val cacheResources = config.getStringList("cache").getOrElse(new ArrayList()).asScala.toSeq
    val networkResources = config.getStringList("network").getOrElse(new ArrayList()).asScala.toSeq
    Manifest(name, cacheResources, networkResources)
  }

  def apply(name:String, cacheResources:Seq[String], networkResources:Seq[String]):Manifest = {
    if (Play.isProd) manifestCache.get(name).getOrElse(new Manifest(name, cacheResources, networkResources))
    else new Manifest(name, cacheResources, networkResources)
  }
}

class Manifest(val name:String, cacheResources:Seq[String], networkResources:Seq[String]) {
  val cacheAssets = toAssets(cacheResources)
  val networkAssets = toAssets(networkResources)

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
     "%s").format(lastModified, cacheSection, networkSection)
  }

  private def toAssets(resources:Seq[String]):Seq[File] = {
    resources.flatMap { r =>
      find(new File(Play.current.path, r))
    }
  }

  private def find(file: File): Seq[File] = {
    if (!file.exists || !file.isDirectory)
      return Seq(file)

    file.listFiles.flatMap { find(_) }
  }

  private def cacheSection = toSection(cacheAssets)

  private def networkSection = toSection(networkAssets)

  private def toSection(files:Seq[File]) = {
    files.foldRight("") { (a, b) =>
      toRelativeUrl(a.getCanonicalPath) + "\n" + b
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
