import sbt.Keys._
import sbt._

object BuildBuild extends Build {
  override lazy val settings = super.settings ++ Seq(
    scalaVersion := "2.10.4",
    scalacOptions ++= Seq("-unchecked", "-deprecation")
  ) ++ sbtPlugins

  def sbtPlugins = Seq(
    "com.hanhuy.sbt" % "android-sdk-plugin" % "1.3.6",
    "com.hanhuy.sbt" % "sbt-idea" % "1.6.0",
    "com.timushev.sbt" % "sbt-updates" % "0.1.6",
    "com.typesafe.sbt" % "sbt-pgp" % "0.8.1",
    "org.xerial.sbt" % "sbt-sonatype" % "0.1.4",
    "com.github.malliina" %% "sbt-utils" % "0.0.5",
    "net.virtual-void" % "sbt-dependency-graph" % "0.7.4"
  ) map addSbtPlugin
}