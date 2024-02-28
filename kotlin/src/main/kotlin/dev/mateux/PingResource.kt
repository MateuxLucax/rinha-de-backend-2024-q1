package dev.mateux

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path

@Path("/ping")
class PingResource {
    @GET
    fun ping() = "pong"
}