scalaVersion := "2.10.7"

scalacOptions ++= Seq("-unchecked", "-deprecation")

resolvers ++= Seq(Resolver.jcenterRepo, Resolver.bintrayIvyRepo("malliina", "sbt-plugins"))

Seq(
  "org.scala-android" % "sbt-android" % "1.7.10",
  "com.timushev.sbt" % "sbt-updates" % "0.1.6",
//  "com.typesafe.sbt" % "sbt-pgp" % "1.1.1",
//  "com.jsuereth" % "sbt-pgp" % "1.1.0",
//  "org.xerial.sbt" % "sbt-sonatype" % "2.3",
  "com.malliina" %% "sbt-utils" % "0.6.3",
  "net.virtual-void" % "sbt-dependency-graph" % "0.7.4"
) map addSbtPlugin
