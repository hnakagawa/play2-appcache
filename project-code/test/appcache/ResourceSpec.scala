package appcache

import java.io.{ File, FileNotFoundException }

import play.api.test._
import play.api.test.Helpers._

import org.specs2.mutable._

class ResourceSpec extends Specification {
  "Resouce.apply" should {
    "return AssetResource for asset directory path string" in {
      running(FakeApplication()) {
        val res = Resource("@/test/assets", """/test/assets/""".r, "/assets/")
        res must have size 3
        res(0).isInstanceOf[AssetResource] must beTrue
      }
    }

    "return AssetResource for asset file path string" in {
      running(FakeApplication()) {
        val res = Resource("@/test/assets/test.coffee", """/test/assets/""".r, "/assets/")
        res must have size 1
        res(0).isInstanceOf[AssetResource] must beTrue
      }
    }

    "throw NoSouchElementException for not found asset path string" in {
      running(FakeApplication()) {
        {
          Resource("@/test/assets/aaa", """/test/assets/""".r, "/assets/")
        } must throwA[FileNotFoundException]
      }
    }

    "return RelativeResource for HTTP path string" in {
      running(FakeApplication()) {
        val res = Resource("/", """/test/assets/""".r, "/assets/")
        res must have size 1
        res(0).isInstanceOf[RelativeResource] must beTrue
      }
    }

    "return URLResource for HTTP URL string" in {
      running(FakeApplication()) {
        val res = Resource("http://example.com", """/test/assets/""".r, "/assets/")
        res must have size 1
        res(0).isInstanceOf[URLResource] must beTrue
      }
    }

    "return URLResource for HTTPS URL string" in {
      running(FakeApplication()) {
        val res = Resource("https://example.com", """/test/assets/""".r, "/assets/")
        res must have size 1
        res(0).isInstanceOf[URLResource] must beTrue
      }
    }
  }

  "AssetResouce#lastModified" should {
    "return not 0" in {
      running(FakeApplication()) {
        val res = Resource("@/test/assets/test.coffee", """/test/assets/""".r, "/assets/")
            res(0).lastModified must_!= 0
      }
    }
  }

  "AssetResouce#toString" should {
    "return /assets/test.js" in {
      running(FakeApplication()) {
        val res = Resource("@/test/assets/test.coffee", """/test/assets/""".r, "/assets/")
            res(0).toString must_== "/assets/test.js"
      }
    }

    "return /assets/test.css" in {
      running(FakeApplication()) {
        val res = Resource("@/test/assets/test.less", """/test/assets/""".r, "/assets/")
            res(0).toString must_== "/assets/test.css"
      }
    }

    "return /assets/test_sass.css" in {
      running(FakeApplication()) {
        val res = Resource("@/test/assets/test_sass.scss", """/test/assets/""".r, "/assets/")
            res(0).toString must_== "/assets/test_sass.css"
      }
    }

    "return /assets/test.min.js" in {
      running(new FakeApplication() {
        override val mode = play.api.Mode.Prod
      }) {
        val res = Resource("@/test/assets/test.coffee", """/test/assets/""".r, "/assets/")
            res(0).toString must_== "/assets/test.min.js"
      }
    }

    "return /assets/test.min.css" in {
      running(new FakeApplication() {
        override val mode = play.api.Mode.Prod
      }) {
        val res = Resource("@/test/assets/test.less", """/test/assets/""".r, "/assets/")
            res(0).toString must_== "/assets/test.min.css"
      }
    }

    "return /assets/test_sass.min.css" in {
      running(new FakeApplication() {
        override val mode = play.api.Mode.Prod
      }) {
        val res = Resource("@/test/assets/test_sass.scss", """/test/assets/""".r, "/assets/")
            res(0).toString must_== "/assets/test_sass.min.css"
      }
    }
  }

  "RelativeResouce#lastModified" should {
    "return 0" in {
      running(FakeApplication()) {
        val res = Resource("/", """/test/assets/""".r, "/assets/")
            res(0).lastModified must_== 0
      }
    }
  }

  "RelativeResouce#toString" should {
    "return *" in {
      running(FakeApplication()) {
        val res = Resource("*", """/test/assets/""".r, "/assets/")
            res(0).toString must_== "*"
      }
    }
  }

  "URLResouce#lastModified" should {
    "return 0" in {
      running(FakeApplication()) {
        val res = Resource("http://example.com", """/test/assets/""".r, "/assets/")
            res(0).lastModified must_== 0
      }
    }
  }

  "URLResouce#toString" should {
    "return http://example.com" in {
      running(FakeApplication()) {
        val res = Resource("http://example.com", """/test/assets/""".r, "/assets/")
        res(0).toString must_== "http://example.com"
      }
    }
  }
}
