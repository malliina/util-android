import sbt._
import sbt.Keys._
import android.Keys._

object UtilBuild extends Build {
  lazy val utilProject = Project("util-android", file(".")).settings(utilSettings: _*)
  lazy val utilSettings = android.Plugin.androidBuild ++ publishSettings ++ Seq(
    version := "0.1.1",
    platformTarget in Android := "android-18",
    libraryProject := true,
    libraryDependencies ++= Seq(
      "com.android.support" % "support-v4" % "18.0.0",
      "com.typesafe.play" %% "play-json" % "2.2.0",
      "com.loopj.android" % "android-async-http" % "1.4.4",
      "org.java-websocket" % "Java-WebSocket" % "1.3.0",
      "org.scalatest" %% "scalatest" % "1.9.2" % "test"
    ),
    (publishArtifact in packageBin in Compile) := true
  )

  def publishSettings = Seq(
    organization := "com.github.malliina",
    publishTo <<= version(v => {
      val repo =
        if (v endsWith "SNAPSHOT") {
          "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
        } else {
          "Sonatype releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
        }
      Some(repo)
    }),
    credentials += Credentials(Path.userHome / ".ivy2" / "sonatype.txt"),
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := (_ => false),
    pomExtra := extraPom
  )

  def extraPom =
    (<url>https://github.com/malliina/util-android</url>
      <licenses>
        <license>
          <name>BSD-style</name>
          <url>http://www.opensource.org/licenses/BSD-3-Clause</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:malliina/util-android.git</url>
        <connection>scm:git:git@github.com:malliina/util-android.git</connection>
      </scm>
      <developers>
        <developer>
          <id>malliina</id>
          <name>Michael Skogberg</name>
          <url>http://mskogberg.info</url>
        </developer>
      </developers>)
}