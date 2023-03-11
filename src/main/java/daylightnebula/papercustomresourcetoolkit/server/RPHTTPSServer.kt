package daylightnebula.papercustomresourcetoolkit.server

import daylightnebula.papercustomresourcetoolkit.ConfigManager
import daylightnebula.papercustomresourcetoolkit.packer.ResourcePackFinalizedEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

object RPHTTPSServer: Listener {

    private fun startServer() {
        println("Attempting to start server...")
        val createKeyStore = ConfigManager.getValueFromJson("createKeyStore", false)
        val keyStoreMaster = ConfigManager.getValueFromJson("keyStoreMaster", "changeme1")
        val keyStoreAlias = ConfigManager.getValueFromJson("keyStoreAlias", "changeme2")
        val keyStorePass = ConfigManager.getValueFromJson("keyStorePass", "changeme3")
    }

    @EventHandler
    fun onResourcePackFinalizedEvent(event: ResourcePackFinalizedEvent) {
        startServer()
    }
}