import com.malliina.sbtutils.SbtProjects
import com.malliina.sbtutils.SbtUtils.{developerHomePageUrl, developerName, gitUserName}

lazy val utilProject = SbtProjects.mavenPublishProject("util-android")
  .enablePlugins(AndroidLib)

net.virtualvoid.sbt.graph.Plugin.graphSettings

organization := "com.malliina"
scalaVersion := "2.11.12"
platformTarget in Android := "android-27"
libraryProject := true
libraryDependencies ++= Seq(
  "com.android.support" % "support-v4" % "23.0.0",
  "com.loopj.android" % "android-async-http" % "1.4.9",
  "com.malliina" %% "util-base" % "1.6.3"
)
// android-sdk-plugin sets these to false, but we want to create jars for maven
publishArtifact in packageBin in Compile := true
publishArtifact in packageSrc in Compile := true
resolvers ++= Seq(
  "Typesafe releases" at "http://repo.typesafe.com/typesafe/releases/",
  "Sonatype releases" at "https://oss.sonatype.org/content/repositories/releases/",
  Resolver.jcenterRepo
)
javacOptions ++= Seq("-source", "1.6", "-target", "1.6")
scalacOptions += "-target:jvm-1.6"

developerName := "Michael Skogberg"
gitUserName := "malliina"
developerHomePageUrl := "http://mskogberg.info"
