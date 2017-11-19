package com.mucahit.environment

import com.mucahit.component.{AkkaSystemComponent, MockServiceComponent}

trait BaseEnvironment
  extends AkkaSystemComponent
    with MockServiceComponent
