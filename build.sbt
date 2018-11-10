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
  "com.github.malliina" %% "util-base" % "0.6.200"
)
// android-sdk-plugin sets these to false, but we want to create jars for maven
publishArtifact in packageBin in Compile := true
publishArtifact in packageSrc in Compile := true
resolvers ++= Seq(
  "typesafe releases" at "http://repo.typesafe.com/typesafe/releases/",
  Resolver.jcenterRepo
)
javacOptions ++= Seq("-source", "1.6", "-target", "1.6")
scalacOptions += "-target:jvm-1.6"

developerName := "Michael Skogberg"
gitUserName := "malliina"
developerHomePageUrl := "http://mskogberg.info"
