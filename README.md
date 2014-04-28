# util-android #

Utility code for Android, in Scala. Contains:

* an HTTP client based on Scala 2.10 Futures
* in-app purchasing helpers for Google and Amazon
* a WebSocket client that speaks JSON
* miscellaneous Scala code to reduce the verbosity of Java

## Usage ##

Add `lib/in-app-purchasing-1.0.3.jar` as a dependency to your project.

If you use SBT, add the following settings to your project:

```
def utilAndroidSettings = Seq(
  libraryDependencies += "com.github.malliina" %% "util-android" % "0.1.2",
  proguardOptions in Android ++= Seq(
        "-dontwarn com.amazon.**,org.w3c.**",
        "-keep class com.amazon.** {*;}",
        "-keepattributes *Annotation*",
        "-dontoptimize"
  ),
  apkbuildExcludes in Android ++= Seq("LICENSE.txt", "NOTICE.txt", "LICENSE", "NOTICE").map(file => s"META-INF/$file")
)
```
