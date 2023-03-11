package daylightnebula.papercustomresourcetoolkit

import daylightnebula.papercustomresourcetoolkit.packer.ResourcePack
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class PaperCustomResourceToolkit : JavaPlugin() {

    companion object {
        internal lateinit var plugin: PaperCustomResourceToolkit
    }

    override fun onLoad() {
        // save plugin
        plugin = this

        // load configs
        ConfigManager.init()

        // initialize packer
        ResourcePack.init()
        ResourcePack.addAssetsFolder(File(dataFolder, "assets"))
    }

    override fun onEnable() {
        // run finalize packer
        Bukkit.getScheduler().runTaskAsynchronously(this, Runnable { ResourcePack.finalizePack() })
    }

    override fun onDisable() {
        // save config with any changes
        ConfigManager.save()
    }
}