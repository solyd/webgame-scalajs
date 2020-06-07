name := "scalajstest"

version := "0.1"

scalaVersion := "2.13.2"

enablePlugins(ScalaJSPlugin)

// This is an application with a main method
scalaJSUseMainModuleInitializer := true
//mainClass in Compile := Some("gameoflife")
//mainClass in (Compile, run) := Some("gameoflife")
//mainClass in (Compile, packageBin) := Some("gameoflife")
//mainClass in packageBin := Some("gameoflife")
//mainClass in run := Some("gameoflife")

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "1.0.0",
  "io.github.cquiroz" %%% "scala-java-time" % "2.0.0",

  // akka
  "org.akka-js" %%% "akkajsactor" % "2.2.6.5",
  "org.akka-js" %%% "akkajsactorstream" % "2.2.6.5",

  // typed akka ( + test deps )
  "org.akka-js" %%% "akkajsactortyped" % "2.2.6.5",
  "org.akka-js" %%% "akkajsactorstreamtyped" % "2.2.6.5",
  "org.akka-js" %%% "akkajstypedtestkit" % "2.2.6.5" % "test",

  // test
  "com.lihaoyi" %%% "utest" % "0.7.4" % "test",
  "org.akka-js" %%% "akkajstestkit" % "2.2.6.5" % "test",
  "org.akka-js" %%% "akkajsstreamtestkit" % "2.2.6.5" % "test",
)

jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv()
testFrameworks += new TestFramework("utest.runner.Framework")