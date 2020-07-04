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
    val log4cats      = "1.1.1"
    val enumeratum    = "1.6.1"
    val kindProjector = "0.11.0"
  }

  val cats       = "org.typelevel" %% "cats-core"   % versions.cats
  val catsEffect = "org.typelevel" %% "cats-effect" % versions.catsEffect

  def http4s(artifact: String) = "org.http4s" %% artifact % versions.http4s
  val http4sDsl                = http4s("http4s-dsl")
  val http4sServer             = http4s("http4s-blaze-server")
  val http4sCirce              = http4s("http4s-circe")

  def circe(artifact: String) = "io.circe" %% artifact % versions.circe
  val circeCore               = circe("circe-core")
  val circeGeneric            = circe("circe-generic")

  val enumeratum = "com.beachape"          %% "enumeratum-cats" % versions.enumeratum
  val pureConfig = "com.github.pureconfig" %% "pureconfig"      % versions.pureConfig
  val logback    = "ch.qos.logback"        % "logback-classic"  % versions.logback
  val log4cats   = "io.chrisdavenport"     %% "log4cats-slf4j"  % versions.log4cats

  val kindProjector = "org.typelevel" %% "kind-projector" % versions.kindProjector
}
