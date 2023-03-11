package daylightnebula.papercustomresourcetoolkit.server

import daylightnebula.papercustomresourcetoolkit.packer.ResourcePackFinalizedEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

object RPHTTPSServer: Listener {

    private fun startServer() {
        println("TODO start server")
    }

    @EventHandler
    fun onResourcePackFinalizedEvent(event: ResourcePackFinalizedEvent) {
        startServer()
    }
}