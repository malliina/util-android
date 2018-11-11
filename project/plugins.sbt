scalaVersion := "2.10.7"

scalacOptions ++= Seq("-unchecked", "-deprecation")

resolvers ++= Seq(Resolver.jcenterRepo, Resolver.bintrayIvyRepo("malliina", "sbt-plugins"))

Seq(
  "org.scala-android" % "sbt-android" % "1.7.10",
  "com.malliina" %% "sbt-utils" % "0.6.3"
) map addSbtPlugin
