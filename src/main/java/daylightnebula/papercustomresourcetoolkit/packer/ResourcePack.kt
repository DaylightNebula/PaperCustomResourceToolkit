package daylightnebula.papercustomresourcetoolkit.packer

import org.bukkit.Material
import java.io.File

object ResourcePack {

    private val resources = hashMapOf<String, Resource>()
    private val namespace = "custom_resource_toolkit"
    private val packFolder = File("ResourcePack")
    private val assetsFolder = File(packFolder, "assets")
    private val namespaceFolder = File(assetsFolder, namespace)
    private val texturesFolder = File(namespaceFolder, "textures/$namespace")
    private val minecraftFolder = File(assetsFolder, "minecraft")
    private val modelsFolder = File(minecraftFolder, "models")
    private val itemFolder = File(modelsFolder, "item")
    private val blockFolder = File(modelsFolder, "block")

    internal fun init() {
        texturesFolder.mkdirs()
        itemFolder.mkdirs()
        blockFolder.mkdirs()
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

    }
}
data class Resource(val itemType: Material, val data: Int)