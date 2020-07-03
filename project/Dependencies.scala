import sbt._

object Dependencies {

  object versions {
    val cats          = "2.1.1"
    val catsEffect    = "2.1.3"
    val circe         = "0.13.0"
    val ciris         = "1.1.1"
    val http4s        = "0.21.3"
    val pureConfig    = "0.12.1"
    val logback       = "1.2.3"
    val kindProjector = "0.11.0"
  }

  def circe(artifact: String)  = "io.circe"   %% artifact % versions.circe
  def http4s(artifact: String) = "org.http4s" %% artifact % versions.http4s

  lazy val cats       = "org.typelevel" %% "cats-core"   % versions.cats
  lazy val catsEffect = "org.typelevel" %% "cats-effect" % versions.catsEffect

  lazy val http4sDsl    = http4s("http4s-dsl")
  lazy val http4sServer = http4s("http4s-blaze-server")
  lazy val http4sCirce  = http4s("http4s-circe")

  lazy val circeCore    = circe("circe-core")
  lazy val circeGeneric = circe("circe-generic")

  lazy val pureConfig = "com.github.pureconfig" %% "pureconfig" % versions.pureConfig

  lazy val kindProjector = "org.typelevel"  %% "kind-projector" % versions.kindProjector
  lazy val logback       = "ch.qos.logback" % "logback-classic" % versions.logback
}
