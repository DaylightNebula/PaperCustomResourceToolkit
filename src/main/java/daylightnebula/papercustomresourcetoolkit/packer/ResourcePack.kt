package daylightnebula.papercustomresourcetoolkit.packer

import daylightnebula.papercustomresourcetoolkit.PaperCustomResourceToolkit
import daylightnebula.papercustomresourcetoolkit.ZipManager
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.json.JSONObject
import java.io.File

object ResourcePack {

    private val packJson = JSONObject()
        .put(
            "pack",
            JSONObject()
                .put("pack_format", 13)
                .put("description", "Auto-generated pack from PaperCustomResourceToolkit")
            )

    private val resources = hashMapOf<String, Resource>()
    private const val namespace = "custom_resource_toolkit"
    private val packFolder = File("ResourcePack")
    private val assetsFolder = File(packFolder, "assets")
    private val namespaceFolder = File(assetsFolder, namespace)
    private val texturesFolder = File(namespaceFolder, "textures/$namespace")
    private val minecraftFolder = File(assetsFolder, "minecraft")
    private val modelsFolder = File(minecraftFolder, "models")
    private val itemFolder = File(modelsFolder, "item")
    private val blockFolder = File(modelsFolder, "block")
    private val metaFile = File(packFolder, "pack.mcmeta")

    internal fun init() {
        texturesFolder.mkdirs()
        itemFolder.mkdirs()
        blockFolder.mkdirs()
        metaFile.writeText(packJson.toString(1))
    }

    fun addAssetsFolder(folder: File) {
        folder.listFiles()?.forEach { file ->
            if (file.isDirectory)
                addAssetsFolder(file)
            else when (file.extension) {
                "png" -> addPNGTexture(file)
                "bbmodel" -> addBBModel(file)
            }
        }
    }

    private fun addPNGTexture(file: File) {

    }

    private fun addBBModel(file: File) {
        TODO("BBModels are not supported yet!")
    }

    internal fun finalizePack() {
        // zip resource pack
        ZipManager.zip(packFolder, File("ResourcePack.zip"))

        // call event when the resource pack finishes (also starts local server if necessary)
        Bukkit.getScheduler().runTask(
            PaperCustomResourceToolkit.plugin,
            Runnable {
                Bukkit.getPluginManager().callEvent(ResourcePackFinalizedEvent())
            }
        )
    }
}
data class Resource(val itemType: Material, val data: Int)
class ResourcePackFinalizedEvent: Event() {
    companion object {
        @JvmStatic
        private val handlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlerList
        }
    }
    override fun getHandlers(): HandlerList {
        return handlerList
    }
}