package daylightnebula.papercustomresourcetoolkit

import daylightnebula.papercustomresourcetoolkit.packer.CreateAnimatedModelCommand
import daylightnebula.papercustomresourcetoolkit.packer.RemoveAnimatedModelsCommand
import daylightnebula.papercustomresourcetoolkit.packer.ResourcePack
import daylightnebula.papercustomresourcetoolkit.packer.activeTestStands
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import java.io.File

var tick = 0L
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

        // commands
        this.getCommand("createanimatedmodel")?.setExecutor(CreateAnimatedModelCommand())
        this.getCommand("removeanimatedmodels")?.setExecutor(RemoveAnimatedModelsCommand())

        // update loop
        updateTask = Bukkit.getScheduler().runTaskTimer(this, Runnable { update() }, 2L, 1L)
    }

    override fun onDisable() {
        // stop update loop
        updateTask.cancel()

        // save config with any changes
        ConfigManager.save()
        activeTestStands.forEach {
            it.key.cancel()
            it.value.forEach { ass -> ass.remove() }
        }
    }

    private lateinit var updateTask: BukkitTask
    private fun update() {
        tick++
    }
}