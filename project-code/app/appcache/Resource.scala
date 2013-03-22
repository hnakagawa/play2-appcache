package appcache

import scala.util.matching.Regex

import java.io.{ File, FileNotFoundException }
import java.net.URL

import play.api._
import play.api.Play.current

object Resource {
  private val HTTP_SCHEME_REG = """(^https?://.+)""".r

  private val IGNORE_REG = """.*~$""".r

  private val ASSET_REG = """^@(.*)""".r

  def apply(res:String, replace:Regex, base:String):Seq[Resource] = res match {
    case HTTP_SCHEME_REG(url) => Seq(new URLResource(new URL(url)))
    case ASSET_REG(path) => findAssetResources(path, replace, base)
    case _ => Seq(new RelativeResource(res))
  }

  private def findAssetResources(path:String, replace:Regex, base:String):Seq[AssetResource] =
    findAsset(new File(Play.current.path, path)).map { file => new AssetResource(file, replace, base) }

  private def findAsset(file: File):Seq[File] = file match {
    case file if !IGNORE_REG.findAllIn(file.getName).isEmpty => Seq()
    case file if !file.isDirectory && file.exists => Seq(file)
    case dir if dir.exists => dir.listFiles.flatMap { findAsset(_) }
    case _ => throw new FileNotFoundException("Not found: " + file.getCanonicalPath)
  }
}

trait Resource {
  def lastModified:Long

  def mkResouceString:String

  final override def toString = mkResouceString
}

class AssetResource(val file:File, val replace:Regex, val base:String) extends Resource {
  private val JAVASCRIPT_EXT_REG = """\.coffee$""".r

  private val CSS_EXT_REG = """\.(less|scss)$""".r

  private val ASSET_EXT = if (Play.isProd) (".min.js", ".min.css") else (".js", ".css")

  override lazy val mkResouceString =
    replaceExtension(replacePath(file.getCanonicalPath.substring(Play.current.path.getCanonicalPath.length)))

  lazy val lastModified = file.lastModified

  private def replacePath(path:String):String = replace.replaceAllIn(path, base)

  private def replaceExtension(path:String):String =
    CSS_EXT_REG.replaceAllIn(JAVASCRIPT_EXT_REG.replaceAllIn(path, ASSET_EXT._1), ASSET_EXT._2)
}

class RelativeResource(val path:String) extends Resource {
  override lazy val mkResouceString = path

  lazy val lastModified = 0L
}

class URLResource(val url:URL) extends Resource {
  override lazy val mkResouceString = url.toString

  lazy val lastModified = 0L
}
