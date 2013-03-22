package controllers

import play.api._
import play.api.mvc._
import play.api.Play.current

import appcache.Manifest

object AppCacheAssets extends AppCacheAssetsBuilder

class AppCacheAssetsBuilder extends Controller {
  def at(name:String, replace:String = "^/(app/assets/|public/)",
         base:String = "/assets/"): Action[AnyContent] = Action { request =>
    try {
      Ok(Manifest(name, replace, base).content).as("text/cache-manifest")
    } catch {
      case exp:NoSuchElementException => NotFound
    }
  }
}
