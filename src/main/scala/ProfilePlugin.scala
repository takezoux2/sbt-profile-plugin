package profile

import sbt._
import Keys._
import java.io.File

object Plugin extends sbt.Plugin{
  
  case class ProfileSetting(
    val profileName : String,
    val resourceDirs : Seq[File] = Nil,
    val sourceDirs : Seq[File] = Nil,
    val publishTo : Option[Resolver] = None,
    val overrideSettings : Seq[sbt.Project.Setting[_]] = Nil
  )
  
  object ProfileKeys{
    val profile = SettingKey[String]("profile","profile-name")
    val profileList = SettingKey[Seq[String]]("profiles","available profile list")
    val showProfiles = TaskKey[Unit]("show-profiles","show profile list")
    val additionalResourceDir = TaskKey[Seq[File]]("profile-resource-directories","Additional resource directories for profile")
    val additionalSrcDir = TaskKey[Seq[File]]("profile-source-directories","Additional source directories for profile")
    val overridePublishTo = TaskKey[Option[Resolver]]("override-publish-to","Publish target for profile")
    val overrideSettings = TaskKey[Seq[sbt.Project.Setting[_]]]("override-settings","override settings for profile")
    
    val profileSettingList = SettingKey[Seq[ProfileSetting]]("profile-settings","Profile settings foreach profile")
  }
  import ProfileKeys._
  
  override lazy val settings = Seq(
    commands ++= Seq(swichProfileCommand)
  )
  
  var firstCall_? = true
  
  def runProject(profileName : String) = (state : State) => {
    if(firstCall_?){
      firstCall_? = false
      updateProperties(state,profileName)
      
    }else state
  }
  
  lazy val profileSettings = Seq(
    profile := "development",
    profileList := Seq("development","test","production"),
    showProfilesTask,
    additionalResourceDirTask,
    additionalSrcDirTask,
    overrideSettingsTask,
    overridePublishToTask,
    profileSettingList := Seq(),
    onLoad in Global <<= (profile).apply( p => {
      runProject(p)
    })
  )
  
  
  val showProfilesTask = showProfiles <<= (profileList) map( profiles => {
    profiles.foreach( println(_))
  })
  val additionalResourceDirTask = ProfileKeys.additionalResourceDir <<= ( profileSettingList, profile , baseDirectory) map( 
    (pSettings,p,baseDir) => {
      pSettings.find(_.profileName == p).map(_.resourceDirs) getOrElse Seq(baseDir / "src" / "main" / "profiles" /p)
    })
  
  val additionalSrcDirTask = additionalSrcDir <<= (profileSettingList,profile) map( (pSettings,p) => { 
    //nothing
    pSettings.find(_.profileName == p).map(_.sourceDirs).getOrElse(Seq())
  })
  
  val overrideSettingsTask = overrideSettings <<= (profileSettingList,profile) map( (pSettings,p) => { 
    //nothing
    pSettings.find(_.profileName == p).map(_.overrideSettings).getOrElse(Nil)
  })
  
  val overridePublishToTask = overridePublishTo <<= (profileSettingList,profile) map(
    (pSettings,p) => {
      pSettings.find(_.profileName == p).map(_.publishTo) getOrElse None
    }
  )
  
  
  
  
  lazy val swichProfileCommand = Command.single("set-profile") { updateProperties }
  
  def updateProperties(state : State ,profileName : String) = {
    
    val extracted = Project.extract(state)
    
    val profiles = extracted.get(profileList)
    
    if(!profiles.contains(profileName)){
      LogManager.defaultScreen.error("Unknown profile '%s'!".format(profileName))
      state.fail
    }else{    
      LogManager.defaultScreen.debug("Set profile to '%s'.".format(profileName))
      
      val baseDir =extracted.get(baseDirectory)
      val publishTarget =extracted.getOpt(publishTo).getOrElse(None)
      val (s1,resourceDirs) = extracted.runTask(ProfileKeys.additionalResourceDir,state)
      val unRes = extracted.getOpt(unmanagedResourceDirectories).getOrElse {
          Seq( baseDir / "src" / "main" / "resources")
      }
      val (s2,sourceDirs) = extracted.runTask(ProfileKeys.additionalSrcDir,s1)
      val unSrc = extracted.getOpt(unmanagedSourceDirectories).getOrElse {
          Seq( baseDir / "src" / "main" / "scala")
      }
      val (s3,oSettings) = extracted.runTask(ProfileKeys.overrideSettings,s2)
      
      val (s4,oPublishTo) = extracted.runTask(overridePublishTo,s3)
      
      println(overrideSettings)
      extracted.append( Seq(
        profile := profileName,
        unmanagedResourceDirectories in Compile := (unRes ++ resourceDirs),
        unmanagedSourceDirectories in Compile := (unSrc ++ sourceDirs),
        publishTo := oPublishTo orElse publishTarget
      ) ++ oSettings, s4)
    }
  }
  
}