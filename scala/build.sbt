name := "scala"

version := "1"

organization := "edu.ar.utn.tadp"

scalaVersion := "3.3.1"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.15" % "test",
  "org.scalactic" %% "scalactic" % "3.2.15",
  "org.typelevel" %% "cats-core" % "2.12.0"
)
