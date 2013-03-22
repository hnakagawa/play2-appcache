package appcache

import scala.util.matching.Regex

import java.io.File

import play.api.test._
import play.api.test.Helpers._

import org.specs2.mutable._

class ManifestSpec extends Specification {
  "Manifest.apply" should {
    "return Manifest" in {
      running(FakeApplication()) {
        val manifest = Manifest("example", "/test/assets/", "/assets/", "test/conf/appcache.conf")
        manifest.isInstanceOf[Manifest] must beTrue
      }
    }

    "return re-generate Manifest" in {
      running(FakeApplication()) {
        val manifest = Manifest("example", "/test/assets/", "/assets/", "test/conf/appcache.conf")
        manifest must_!= Manifest("example", "/test/assets/", "/assets/", "test/conf/appcache.conf")
      }
    }

    "return same Manifest" in {
      running(new FakeApplication() {
        override val mode = play.api.Mode.Prod
      }) {
        val manifest = Manifest("example", "/test/assets/", "/assets/", "test/conf/appcache.conf")
        manifest must_== Manifest("example", "/test/assets/", "/assets/", "test/conf/appcache.conf")
      }
    }

    "throw NotSouchElementException" in {
      running(FakeApplication()) {
        {
          val manifest = Manifest("aaa", "/test/assets/", "/assets/", "test/conf/appcache.conf")
          manifest.isInstanceOf[Manifest] must beTrue
        } must throwA[NoSuchElementException]
      }
    }
  }

  "Manifest#lastModified" should {
    "return not 0" in {
      running(FakeApplication()) {
        val manifest = Manifest("example", "/test/assets/", "/assets/", "test/conf/appcache.conf")
        manifest.lastModified must_!= 0
      }
    }
  }
}
