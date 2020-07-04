import Dependencies._

ThisBuild / scalaVersion := "2.13.1"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "lamedh"

lazy val root = (project in file("."))
  .settings(
    name := "forex",
    libraryDependencies ++= Seq(
      compilerPlugin(kindProjector cross CrossVersion.full),
      cats,
      catsEffect,
      circeCore,
      circeGeneric,
      http4sDsl,
      http4sCirce,
      http4sServer,
      log4cats,
      logback % Runtime,
      enumeratum,
      pureConfig
    )
  )
