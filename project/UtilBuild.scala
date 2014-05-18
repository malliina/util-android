import com.mle.sbtutils.SbtUtils
import sbt._
import sbt.Keys._
import android.Keys._

object UtilBuild extends Build {

  lazy val utilProject = Project("util-android", file(".")).settings(utilSettings: _*)
  lazy val utilSettings = android.Plugin.androidBuild ++ publishSettings ++ Seq(
    scalaVersion := "2.10.4",
    // cannot switch to 2.11 because play-json only exists for 2.10
    //    crossScalaVersions := Seq("2.11.0", "2.10.4"),
    version := "0.7.10",
    platformTarget in Android := "android-19",
    libraryProject := true,
    libraryDependencies ++= Seq(
      "com.android.support" % "support-v4" % "19.0.1",
      "com.typesafe.play" %% "play-json" % "2.2.2",
      "com.loopj.android" % "android-async-http" % "1.4.4",
      "org.java-websocket" % "Java-WebSocket" % "1.3.0",
      "com.github.malliina" %% "util-base" % "0.1.1",
      "org.scalatest" %% "scalatest" % "2.1.6" % "test"
    ),
    // android-sdk-plugin sets these to false, but we want to create jars for maven
    (publishArtifact in packageBin in Compile) := true,
    (publishArtifact in packageSrc in Compile) := true
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