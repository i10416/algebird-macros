val algebraVersion = "2.0.0"

lazy val src = project
  .in(file("."))
  .settings(
    libraryDependencies ++= Seq(
      ("org.typelevel" %% "algebra" % algebraVersion)
        .cross(CrossVersion.for3Use2_13),
      "org.scalacheck" %% "scalacheck" % "1.15.3"
    ),
    scalaVersion := "3.2.2",
    scalacOptions ++= Seq(
      "-Ykind-projector:underscores",
      "-feature",
      "-deprecation"
    )
  )
