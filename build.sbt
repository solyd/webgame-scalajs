name := "scalajstest"

version := "0.1"

scalaVersion := "2.13.2"

enablePlugins(ScalaJSPlugin)

// This is an application with a main method
scalaJSUseMainModuleInitializer := true

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "1.0.0",
  "io.github.cquiroz" %%% "scala-java-time" % "2.0.0",
  "com.lihaoyi" %%% "utest" % "0.7.4" % "test"
)

jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv()
testFrameworks += new TestFramework("utest.runner.Framework")