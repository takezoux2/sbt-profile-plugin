# This plugin still be alpha version.Many bugs remain.

# profile-plugin

Switch settings according to profile.

## How to use

You make project/plugins.sbt file, and addbelow.

    addSbtPlugin("com.geishatokyo.sbt.plugin" % "profile-plugin" % "0.1")
    
    resolvers += "takezoux2@github" at "http://takezoux2.github.com/maven"


## Usage

build.sbt

    import profile.Plugin._
    
    import profile.Plugin.ProfileKeys._
    
    profile := "test"
    
    profileList := Seq("development","test","production")
    
    profileSettingsList := Seq(
      ProfileSetting("test",
        resourceDirs = Seq( file("./additional/resource/directory"),
        sourceDirs = Seq( file("./additonal/source/directory"),
        publishTo = Some("test-nexus" at "http://test.nexus.com"),
        overrideSettings = Seq(
          version ~= v => v + "-SNAPSHOT"
        )
      ),
      ProfileSetting("production",publishTo = Some("test-nexus" at "http://test.nexus.com"))
    )

build.scala


    import sbt._
    import sbt.Keys._
    import profile.Plugin._
    import profile.Plugin.ProfileKeys._
    
    class YourBuild extends Build{
    
      val root = Project(id = "root",base =  file("."),
        settings = Project.defaultSettings ++ profileSettings ++ Seq(
          profile := "test",
          profileList := Seq("development","test","production"),
          profileSettingsList := Seq(
            ProfileSetting("test",
              resourceDirs = Seq( file("./additional/resource/directory"),
              sourceDirs = Seq( file("./additonal/source/directory"),
              publishTo = Some("test-nexus" at "http://test.nexus.com"),
              overrideSettings = Seq(
                version ~= v => v + "-SNAPSHOT"
              )
            ),
            ProfileSetting("production",publishTo = Some("test-nexus" at "http://test.nexus.com"))
          )
        )
      )
    
    }
    
    
@sbt console

    > show version
    0.0.1
    
    > set-profile test
    
    ...processing messages
    
    > show version
    
    0.0.1-SNAPSHOT


## About ProfileSetting

### resourceDirs

Additional resource directories.
Default: add "./src/main/profiles/{profileName}" directory

### sourceDirs

Additional source directories
Default: add no directories

### publishTo

Publish destination.
Default: use pre-set value

### overrideSettings

Override settings.


## Keys

### profile

Profile name

### profileList

Profile names which this project supports
Default is Seq("development","test","production")

### profileSettingsList

Settings foreach profile

## Commands

### set-profile {profileName}

change profile and override setting keys.






