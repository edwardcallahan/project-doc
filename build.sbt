// General

name := "project-doc-docker"
version := "1.0-SNAPSHOT"
scalaVersion := "2.11.8"

licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))

resolvers += "spray repo" at "http://repo.spray.io"

libraryDependencies ++= Seq(
  "com.typesafe.akka"      %% "akka-distributed-data-experimental" % "2.4.7",
  "org.apache.commons"     %  "commons-compress" 		               % "1.11",
  "commons-io"             %  "commons-io"       		               % "2.5",
  "org.webjars"            %  "foundation"       	     	           % "5.5.1",
  "org.webjars"            %  "prettify"                           % "4-Mar-2013",
  "com.googlecode.kiama"   %% "kiama"            		               % "1.8.0",
  "com.typesafe.conductr"  %% "play25-conductr-bundle-lib"	       % "1.4.7",
  "com.typesafe.play"      %% "play-doc"         		               % "1.6.0",
  "io.spray"               %% "spray-caching"    		               % "1.3.3",
  "com.typesafe.akka"      %% "akka-testkit"                       % "2.3.12",
  "org.scalatest"          %% "scalatest"        		               % "2.2.4" % "test",
  "org.scalatestplus.play" %% "scalatestplus-play"                 % "1.5.1" % "test"
)

// Play
routesGenerator := InjectedRoutesGenerator

// sbt-web
JsEngineKeys.engineType := JsEngineKeys.EngineType.Node
sassOptions in Assets ++= Seq("--compass", "-r", "compass")
StylusKeys.useNib in Assets := true
StylusKeys.compress in Assets := false

// ConductR
import ByteConversions._
import com.typesafe.sbt.packager.docker._

javaOptions in Universal := Seq(
  "-J-Xmx64m",
  "-J-Xms64m"
)

BundleKeys.bundleType := Docker

BundleKeys.nrOfCpus := 0.1
BundleKeys.memory := 128.MiB
BundleKeys.diskSpace := 50.MiB
BundleKeys.roles := Set("web")

BundleKeys.system := "doc-renderer-cluster"
BundleKeys.systemVersion := "2"

BundleKeys.endpoints := Map(
  "akka-remote" -> Endpoint("tcp"),
  "web" -> Endpoint("http", services = Set(URI("http://conductr-docker.lightbend.com")))
)

// Bundle publishing configuration

inConfig(Bundle)(Seq(
  bintrayVcsUrl := Some("https://github.com/typesafehub/project-doc-docker"),
  bintrayOrganization := Some("typesafe")
))

// Project/module declarations
lazy val root = (project in file(".")).enablePlugins(PlayScala)

// Docker specifics for the native packager

dockerCommands := Seq(
  Cmd("FROM", "typesafe-docker-internal-docker.bintray.io/typesafehub/docker-java8-portal-base")
  )
// Additional args for the docker run can be supplied here (see the sbt-bundle
// README) - otherwise the startCommand is empty

BundleKeys.startCommand := Seq.empty

// The following check creates a bundle component that waits for the
// Docker build to complete and then test the postgres-bdr port. If
// the port is open then ConductR is signalled that the bundle is ready.

BundleKeys.checks := Seq(uri("docker+$WEB_HOST"))