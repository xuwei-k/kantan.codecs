ThisBuild / kantanProject := "codecs"
ThisBuild / startYear     := Some(2016)

lazy val jsModules: Seq[ProjectReference] = Seq(
  catsJS,
  catsLawsJS,
  coreJS,
  enumeratumJS,
  enumeratumLawsJS,
  lawsJS,
  refinedJS,
  refinedLawsJS,
  scalazJS,
  scalazLawsJS,
  shapelessJS,
  shapelessLawsJS
)

lazy val jvmModules: Seq[ProjectReference] = Seq(
  catsJVM,
  catsLawsJVM,
  coreJVM,
  enumeratumJVM,
  enumeratumLawsJVM,
  java8,
  java8Laws,
  lawsJVM,
  libra,
  libraLaws,
  refinedJVM,
  refinedLawsJVM,
  scalazJVM,
  scalazLawsJVM,
  shapelessJVM,
  shapelessLawsJVM
)

// - root projects -----------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
lazy val root = Project(id = "kantan-codecs", base = file("."))
  .settings(moduleName := "root")
  .enablePlugins(UnpublishedPlugin)
  .aggregate((jsModules ++ jvmModules :+ (docs: ProjectReference)): _*)
  .dependsOn(catsJVM, coreJVM, enumeratumJVM, libra, refinedJVM, scalazJVM, shapelessJVM)

lazy val docs = project
  .enablePlugins(DocumentationPlugin)
  .settings(name := "docs")
  .settings(
    ScalaUnidoc / unidoc / unidocProjectFilter :=
      inAnyProject -- inProjects(jsModules: _*)
  )
  .dependsOn(catsJVM, coreJVM, enumeratumJVM, libra, refinedJVM, scalazJVM, shapelessJVM, java8)

lazy val scala3root = project
  .aggregate(
    catsJVM,
    catsLawsJVM,
    coreJVM,
    lawsJVM,
    refinedJVM,
    refinedLawsJVM,
    scalazJVM,
    scalazLawsJVM
  )

val scala3settings = Def.settings(
  scalaVersion                        := "3.3.5",
  ThisBuild / wartremoverCrossVersion := CrossVersion.binary,
  Seq(Compile, Test).map { c =>
    c / unmanagedSourceDirectories ++= {
      scalaBinaryVersion.value match {
        case "2.13" | "3" =>
          val base = crossProjectBaseDirectory.?.value.fold(baseDirectory.value)(_ / "shared")
          Seq(
            base / "src" / Defaults.nameForSrc(c.name) / "scala-2.13+"
          )
        case _ =>
          Nil
      }
    }
  },
  conflictWarning := {
    if(scalaBinaryVersion.value == "3") {
      ConflictWarning("warn", Level.Warn, false)
    } else {
      conflictWarning.value
    }
  },
  libraryDependencies := {
    scalaBinaryVersion.value match {
      case "3" =>
        libraryDependencies.value.filterNot(_.name == "scala-reflect")
      case _ =>
        libraryDependencies.value
    }
  },
  scalacOptions                     := Nil,
  Compile / compile / scalacOptions := Nil
)

// - core projects -----------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
lazy val core = kantanCrossProject("core")
  .settings(moduleName := "kantan.codecs")
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %%% "scala-collection-compat" % Versions.collectionCompat % Test,
      "org.scalatest"          %%% "scalatest"               % Versions.scalatest        % Test
    ),
    scala3settings
  )
  .enablePlugins(PublishedPlugin)
  .laws("laws")

lazy val coreJVM = core.jvm
  .settings(libraryDependencies += "commons-io" % "commons-io" % Versions.commonsIo % Test)
lazy val coreJS = core.js

lazy val laws = kantanCrossProject("laws")
  .settings(moduleName := "kantan.codecs-laws")
  .enablePlugins(BoilerplatePlugin, PublishedPlugin)
  .dependsOn(core)
  .settings(
    scala3settings,
    libraryDependencies ++= Seq(
      "org.scalacheck" %%% "scalacheck"           % Versions.scalacheck,
      "org.scalatest"  %%% "scalatest"            % Versions.scalatest,
      "org.typelevel"  %%% "discipline-scalatest" % Versions.disciplineScalatest
    )
  )

lazy val lawsJVM = laws.jvm
lazy val lawsJS  = laws.js

// - cats projects -----------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
lazy val cats = kantanCrossProject("cats")
  .in(file("cats/core"))
  .settings(moduleName := "kantan.codecs-cats")
  .enablePlugins(PublishedPlugin)
  .dependsOn(core)
  .settings(
    scala3settings,
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % Versions.cats,
      "org.scalatest" %%% "scalatest" % Versions.scalatest % Test
    )
  )
  .laws("cats-laws")

lazy val catsJVM = cats.jvm
lazy val catsJS  = cats.js

lazy val catsLaws = kantanCrossProject("cats-laws")
  .in(file("cats/laws"))
  .settings(moduleName := "kantan.codecs-cats-laws")
  .enablePlugins(PublishedPlugin)
  .dependsOn(core, laws, cats)
  .settings(
    scala3settings,
    libraryDependencies += "org.typelevel" %%% "cats-laws" % Versions.cats
  )

lazy val catsLawsJVM = catsLaws.jvm
lazy val catsLawsJS  = catsLaws.js

// - java8 projects ----------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
lazy val java8 = Project(id = "java8", base = file("java8/core"))
  .settings(
    moduleName := "kantan.codecs-java8",
    name       := "java8"
  )
  .enablePlugins(PublishedPlugin)
  .dependsOn(coreJVM)
  .settings(
    scala3settings,
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % Versions.scalatest % "test"
    )
  )
  .laws("java8-laws")

lazy val java8Laws = Project(id = "java8-laws", base = file("java8/laws"))
  .settings(
    moduleName := "kantan.codecs-java8-laws",
    name       := "java8-laws"
  )
  .enablePlugins(PublishedPlugin)
  .dependsOn(coreJVM, lawsJVM, java8)
  .settings(
    scala3settings,
    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %% "scala-collection-compat" % Versions.collectionCompat
    )
  )

// - scalaz projects ---------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
lazy val scalaz = kantanCrossProject("scalaz")
  .in(file("scalaz/core"))
  .settings(moduleName := "kantan.codecs-scalaz")
  .enablePlugins(PublishedPlugin)
  .dependsOn(core)
  .settings(
    scala3settings,
    libraryDependencies ++= Seq(
      "org.scalaz"    %%% "scalaz-core" % Versions.scalaz,
      "org.scalatest" %%% "scalatest"   % Versions.scalatest % Test
    )
  )
  .laws("scalaz-laws")

lazy val scalazJVM = scalaz.jvm
lazy val scalazJS  = scalaz.js

lazy val scalazLaws = kantanCrossProject("scalaz-laws")
  .in(file("scalaz/laws"))
  .settings(moduleName := "kantan.codecs-scalaz-laws")
  .enablePlugins(PublishedPlugin)
  .dependsOn(core, laws, scalaz)
  .settings(
    scala3settings,
    libraryDependencies ++= Seq(
      "org.scalaz"    %%% "scalaz-core"               % Versions.scalaz,
      "org.scalaz"    %%% "scalaz-scalacheck-binding" % (Versions.scalaz),
      "org.scalatest" %%% "scalatest"                 % Versions.scalatest % "optional"
    )
  )

lazy val scalazLawsJVM = scalazLaws.jvm
lazy val scalazLawsJS  = scalazLaws.js

// - refined project ---------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
lazy val refined = kantanCrossProject("refined")
  .in(file("refined/core"))
  .settings(moduleName := "kantan.codecs-refined")
  .enablePlugins(PublishedPlugin)
  .dependsOn(core)
  .settings(
    scala3settings,
    libraryDependencies ++= Seq(
      "eu.timepit"    %%% "refined"   % Versions.refined,
      "org.scalatest" %%% "scalatest" % Versions.scalatest % Test
    )
  )
  .laws("refined-laws")

lazy val refinedJVM = refined.jvm
lazy val refinedJS  = refined.js

lazy val refinedLaws = kantanCrossProject("refined-laws")
  .in(file("refined/laws"))
  .settings(scala3settings)
  .settings(moduleName := "kantan.codecs-refined-laws")
  .settings(libraryDependencies += "eu.timepit" %%% "refined-scalacheck" % Versions.refined)
  .enablePlugins(PublishedPlugin)
  .dependsOn(core, laws, refined)

lazy val refinedLawsJVM = refinedLaws.jvm
lazy val refinedLawsJS  = refinedLaws.js

// - enumeratum project ------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
lazy val enumeratum = kantanCrossProject("enumeratum")
  .in(file("enumeratum/core"))
  .settings(moduleName := "kantan.codecs-enumeratum")
  .enablePlugins(PublishedPlugin)
  .dependsOn(core)
  .settings(
    scala3settings,
    scalacOptions += "-Yretain-trees",
    libraryDependencies ++= Seq(
      "com.beachape"  %%% "enumeratum" % Versions.enumeratum,
      "org.scalatest" %%% "scalatest"  % Versions.scalatest % Test
    )
  )
  .laws("enumeratum-laws")

lazy val enumeratumJVM = enumeratum.jvm
lazy val enumeratumJS  = enumeratum.js

lazy val enumeratumLaws = kantanCrossProject("enumeratum-laws")
  .in(file("enumeratum/laws"))
  .settings(moduleName := "kantan.codecs-enumeratum-laws")
  .settings(scala3settings)
  .settings(scalacOptions += "-Yretain-trees")
  .settings(libraryDependencies += "com.beachape" %%% "enumeratum-scalacheck" % Versions.enumeratumScalacheck)
  .enablePlugins(PublishedPlugin)
  .dependsOn(core, laws, enumeratum)

lazy val enumeratumLawsJVM = enumeratumLaws.jvm
lazy val enumeratumLawsJS  = enumeratumLaws.js

// - libra projects ------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
lazy val libra = Project(id = "libra", base = file("libra/core"))
  .settings(
    moduleName := "kantan.codecs-libra",
    name       := "libra"
  )
  .enablePlugins(PublishedPlugin)
  .dependsOn(coreJVM)
  .settings(
    scala3settings,
    libraryDependencies ++= Seq(
      "com.github.to-ithaca" %% "libra"     % Versions.libra,
      "org.scalatest"        %% "scalatest" % Versions.scalatest % Test
    )
  )
  .laws("libra-laws")

lazy val libraLaws = Project(id = "libra-laws", base = file("libra/laws"))
  .settings(
    scala3settings,
    moduleName := "kantan.codecs-libra-laws",
    name       := "libra-laws"
  )
  .enablePlugins(PublishedPlugin)
  .dependsOn(coreJVM, lawsJVM, libra)

// - shapeless projects ------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
lazy val shapeless = kantanCrossProject("shapeless")
  .in(file("shapeless/core"))
  .settings(moduleName := "kantan.codecs-shapeless")
  .enablePlugins(PublishedPlugin)
  .dependsOn(core)
  .settings(
    scala3settings,
    libraryDependencies ++= Seq(
      "com.chuusai"   %%% "shapeless" % Versions.shapeless cross CrossVersion.for3Use2_13,
      "org.scalatest" %%% "scalatest" % Versions.scalatest % Test
    )
  )
  .laws("shapeless-laws")

lazy val shapelessJVM = shapeless.jvm
lazy val shapelessJS  = shapeless.js

lazy val shapelessLaws = kantanCrossProject("shapeless-laws")
  .in(file("shapeless/laws"))
  .settings(moduleName := "kantan.codecs-shapeless-laws")
  .settings(
    scala3settings,
    libraryDependencies +=
      "com.github.alexarchambault" %%% "scalacheck-shapeless_1.18" %
        Versions.scalacheckShapeless cross CrossVersion.for3Use2_13
  )
  .enablePlugins(PublishedPlugin)
  .dependsOn(core, laws, shapeless)

lazy val shapelessLawsJVM = shapelessLaws.jvm
lazy val shapelessLawsJS  = shapelessLaws.js
