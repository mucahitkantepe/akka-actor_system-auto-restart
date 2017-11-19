package com.mucahit.environment

import com.mucahit.component.{DefaultAkkaSystemComponent, DefaultMockServiceComponent}

class DevEnvironment
  extends BaseEnvironment
    with DefaultAkkaSystemComponent
    with DefaultMockServiceComponent
