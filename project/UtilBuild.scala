import sbt._
import sbt.Keys._
import android.Keys._
import xerial.sbt.Sonatype

object UtilBuild extends Build {
  lazy val utilProject = Project("util-android", file(".")).settings(utilSettings: _*)
  lazy val utilSettings = android.Plugin.androidBuild ++ publishSettings ++ Seq(
    version := "0.1.2",
    platformTarget in Android := "android-18",
    libraryProject := true,
    libraryDependencies ++= Seq(
      "com.android.support" % "support-v4" % "18.0.0",
      "com.typesafe.play" %% "play-json" % "2.2.0",
      "com.loopj.android" % "android-async-http" % "1.4.4",
      "org.java-websocket" % "Java-WebSocket" % "1.3.0",
      "org.scalatest" %% "scalatest" % "1.9.2" % "test"
    ),
    // android-sdk-plugin sets these to false, but we want to create jars for maven
    (publishArtifact in packageBin in Compile) := true,
    (publishArtifact in packageSrc in Compile) := true
  )

  def publishSettings = Sonatype.sonatypeSettings ++ Seq(
    organization := "com.github.malliina",
    // The Credentials object must be a DirectCredentials. We obtain one using loadCredentials(File).
    credentials += loadDirectCredentials(Path.userHome / ".ivy2" / "sonatype.txt"),
    publishArtifact in Test := false,
    pomExtra := myGitPom(name.value)
  )

  def loadDirectCredentials(file: File) =
    Credentials.loadCredentials(file).fold(
      errorMsg => throw new Exception(errorMsg),
      cred => cred)

  def myGitPom(projectName: String) =
    com.mle.sbthelpers.SbtHelpers.gitPom(projectName, "malliina", "Michael Skogberg", "http://mskogberg.info")
}