package com.dzegel.DynamockServer.server

import com.dzegel.DynamockServer.controller.{ExpectationController, MockController}
import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.routing.HttpRouter

class DynamockServer extends HttpServer {
  override val defaultFinatraHttpPort: String = ":8080"
  override val disableAdminHttpServer = true

  override protected def configureHttp(router: HttpRouter): Unit = {
    router
      .add[ExpectationController]
      .add[MockController]
  }
}
