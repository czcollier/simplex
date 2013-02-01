// see https://github.com/siasia/xsbt-web-plugin for more information on the
// jetty plugin

// import web settings
seq(webSettings :_*)

name := "simplex"

version := "1.0"

scalaVersion := "2.9.1"

libraryDependencies ++= Seq(
"org.mortbay.jetty" % "jetty" % "6.1.22" % "container",
"javax.servlet" % "servlet-api" % "2.5" % "provided"
)
