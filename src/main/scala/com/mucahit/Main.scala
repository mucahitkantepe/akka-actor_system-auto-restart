package com.mucahit

import com.mucahit.environment.{DevEnvironment, EnvironmentRestarter}

object Main extends App {

  EnvironmentRestarter.startAndSupervise(new DevEnvironment)

}
