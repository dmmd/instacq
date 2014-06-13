import AssemblyKeys._

assemblySettings

jarName in assembly := "instag.jar"

name := "Instacq"

version := "0.2"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
  "org.apache.httpcomponents" % "httpclient" % "4.3.3",
  "com.typesafe" % "config" % "1.2.1",
  "postgresql" % "postgresql" % "9.1-901.jdbc4",
  "com.fasterxml" % "jackson-xml-databind" % "0.6.2",
  "com.typesafe.slick" %% "slick" % "2.0.2",
  "org.slf4j" % "slf4j-nop" % "1.6.4"
)
