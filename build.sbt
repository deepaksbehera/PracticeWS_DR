name := """PracticeWebSocket"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)
lazy val myProject = (project in file("."))
 .enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
	"org.apache.poi" % "poi-ooxml" % "3.9",
	"org.json" % "json" % "20140107",
  "org.webjars" % "jquery" % "2.1.4", 
  "com.adrianhurt" %% "play-bootstrap3" % "0.4.4-P24",
  "commons-io" % "commons-io" % "2.3",
  "joda-time" % "joda-time" % "2.4",
  "commons-collections" % "commons-collections" % "3.2.1"
)

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
libraryDependencies += "org.postgresql" % "postgresql" % "9.3-1100-jdbc41"