name := "go-test-log-parser"

version := "0.1"

enablePlugins(ScalaNativePlugin)

scalaVersion := "2.11.12"

// https://mvnrepository.com/artifact/org.specs2/specs2-core
libraryDependencies += "org.specs2" %% "specs2-core" % "4.10.5" % Test

// https://github.com/scala/scala-parser-combinators
// Scala-parser-combinators is also available for Scala.js and Scala Native:
libraryDependencies += "org.scala-lang.modules" %%% "scala-parser-combinators" % "1.2.0-M1"

scalacOptions in Test ++= Seq("-Yrangepos")
