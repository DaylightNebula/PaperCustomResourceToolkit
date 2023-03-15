package daylightnebula.papercustomresourcetoolkit

import daylightnebula.papercustomresourcetoolkit.items.CustomItem
import daylightnebula.papercustomresourcetoolkit.items.CustomItemCommand
import daylightnebula.papercustomresourcetoolkit.packer.CreateAnimatedModelCommand
import daylightnebula.papercustomresourcetoolkit.packer.FontManager
import daylightnebula.papercustomresourcetoolkit.packer.ResourcePack
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import java.io.File

var tick = 0L
class PaperCustomResourceToolkit : JavaPlugin() {

    companion object {
        internal lateinit var plugin: PaperCustomResourceToolkit
        lateinit var customItemReferenceIDKey: NamespacedKey
    }

    override fun onLoad() {
        // save plugin
        plugin = this

        // set up the name spaced keys that we need for storing information in minecraft objects
        customItemReferenceIDKey = NamespacedKey(this, "customItemReferenceID")

        // load configs
        ConfigManager.init()

        // initialize packer
        ResourcePack.init()
        addAssetsFolder(File(dataFolder, "assets"))
    }

    override fun onEnable() {
        // run finalize packer
        Bukkit.getScheduler().runTaskLaterAsynchronously(this, Runnable { CustomItem.loadRemainingJSON(); ResourcePack.finalizePack() }, 0L)

        // update loop
        updateTask = Bukkit.getScheduler().runTaskTimer(this, Runnable { update() }, 2L, 1L)

        getCommand("createanimatedmodel")?.setExecutor(CreateAnimatedModelCommand())
        getCommand("getcustomitem")?.setExecutor(CustomItemCommand())
    }

    override fun onDisable() {
        // stop update loop
        updateTask.cancel()

        // save config with any changes
        ConfigManager.save()
    }

    private lateinit var updateTask: BukkitTask
    private fun update() {
        tick++
    }

    fun addAssetsFolder(folder: File) {
        ResourcePack.addAssetsFolder(folder)
        CustomItem.loadJSONFromFolder(folder)
    }
}