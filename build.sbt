name := "ls-hack"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.typesafe.slick"         %% "slick"                              % "3.1.1",
  "com.chuusai"                %%  "shapeless"                         % "2.3.2",
  "org.typelevel" %% "cats" % "0.9.0"
)