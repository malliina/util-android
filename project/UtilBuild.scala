import com.mle.sbtutils.{SbtUtils, SbtProjects}
import sbt._
import sbt.Keys._
import android.Keys._

object UtilBuild extends Build {

  lazy val utilProject = SbtProjects.mavenPublishProject("util-android").settings(utilSettings: _*)
  lazy val utilSettings = android.Plugin.androidBuild ++ publishSettings ++
    net.virtualvoid.sbt.graph.Plugin.graphSettings ++ Seq(
    version := "0.9.3",
    scalaVersion := "2.11.2",
    crossScalaVersions := Seq("2.11.2", "2.10.4"),
    platformTarget in Android := "android-19",
    libraryProject := true,
    libraryDependencies ++= Seq(
      "com.android.support" % "support-v4" % "19.1.0",
      "com.typesafe.play" %% "play-json" % "2.3.5",
      "com.loopj.android" % "android-async-http" % "1.4.4",
      "org.java-websocket" % "Java-WebSocket" % "1.3.0",
      "com.github.malliina" %% "util-base" % "0.3.0"
    ),
    // android-sdk-plugin sets these to false, but we want to create jars for maven
    (publishArtifact in packageBin in Compile) := true,
    (publishArtifact in packageSrc in Compile) := true,
    resolvers += "typesafe releases" at "http://repo.typesafe.com/typesafe/releases/"
  )

  import SbtUtils._

  def publishSettings = SbtUtils.publishSettings ++ Seq(
    developerName := "Michael Skogberg",
    gitUserName := "malliina",
    developerHomePageUrl := "http://mskogberg.info"
  )

  //  def amazonProguardOptions = Seq(
  //    "-dontwarn com.amazon.**",
  //    "-keep class com.amazon.** {*;}",
  //    "-keepattributes *Annotation*",
  //    "-dontoptimize "
  //  )
  //
  //  def allProguardOptions = amazonProguardOptions ++ Seq(
  //    "-dontwarn org.w3c.**"
  //  )
  //
  //  def utilAndroidProguardSettings = Seq(
  //    proguardOptions in Android ++= allProguardOptions,
  //    apkbuildExcludes in Android ++= Seq("LICENSE.txt", "NOTICE.txt", "LICENSE", "NOTICE").map(file => s"META-INF/$file")
  //  )
}