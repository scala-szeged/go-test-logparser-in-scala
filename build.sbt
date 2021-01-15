name := "LogParser"

version := "1.0"

scalaVersion := "2.13.4"

// https://mvnrepository.com/artifact/org.specs2/specs2-core
libraryDependencies += "org.specs2" %% "specs2-core" % "4.10.5" % Test

// https://mvnrepository.com/artifact/org.scala-lang.modules/scala-parser-combinators
libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.2.0-M1"


scalacOptions in Test ++= Seq("-Yrangepos")
