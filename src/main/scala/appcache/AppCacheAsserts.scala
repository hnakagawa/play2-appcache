package appcache

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
