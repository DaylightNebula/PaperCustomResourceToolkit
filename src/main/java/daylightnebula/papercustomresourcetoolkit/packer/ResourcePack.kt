package daylightnebula.papercustomresourcetoolkit.packer

import daylightnebula.papercustomresourcetoolkit.ConfigManager
import daylightnebula.papercustomresourcetoolkit.PaperCustomResourceToolkit
import daylightnebula.papercustomresourcetoolkit.ZipManager
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.json.JSONObject
import java.io.File
import java.math.BigInteger
import java.security.MessageDigest

object ResourcePack {

    private val packJson = JSONObject()
        .put(
            "pack",
            JSONObject()
                .put("pack_format", 13)
                .put("description", "Auto-generated pack from PaperCustomResourceToolkit")
            )
    private lateinit var hash: String
    private lateinit var packFile: File
    private lateinit var packBytes: ByteArray
    private var shouldGenerate = true
    private var passToMinecraft = true

    private val resources = hashMapOf<String, Resource>()
    const val namespace = "custom_resource_toolkit"
    private val packFolder = File("ResourcePack")
    private val assetsFolder = File(packFolder, "assets")
    private val namespaceFolder = File(assetsFolder, namespace)
    private val texturesFolder = File(namespaceFolder, "textures/$namespace")
    val minecraftFolder = File(assetsFolder, "minecraft")
    private val modelsFolder = File(minecraftFolder, "models")
    val itemFolder = File(modelsFolder, "item")
    private val blockFolder = File(modelsFolder, "block")
    private val metaFile = File(packFolder, "pack.mcmeta")

    internal fun init() {
        // initialize sub system
        ItemAllocator.init()
        AtlasManager.init()

        // get should generate config value
        shouldGenerate = ConfigManager.getValueFromJson("shouldGenerateResourcePack", true)
        passToMinecraft = ConfigManager.getValueFromJson("attemptMinecraftExport", true)

        // check if we should generate
        if (shouldGenerate) {
            // create folders
            texturesFolder.mkdirs()
            itemFolder.mkdirs()
            blockFolder.mkdirs()

            // create meta file
            metaFile.writeText(packJson.toString(1))
        }
    }

    fun addAssetsFolder(folder: File) {
        if (!shouldGenerate) return

        // load every file in the folder that is passed in
        folder.listFiles()?.forEach { file ->
            if (file.isDirectory)
                addAssetsFolder(file)
            else when (file.extension) {
                "png" -> addPNGTexture(file)
                "bbmodel" -> addBBModel(file)
            }
        }
    }

    private var currentTextureID = 0
    private fun addPNGTexture(file: File) {
        // get new texture id
        val texID = currentTextureID++

        // copy texture into the pack
        val target = File(texturesFolder, "$texID.png")
        file.copyTo(target, overwrite = true)

        // generate model for the texture
        val model = JSONObject()
            .put("parent", "minecraft:item/generated")
            .put(
                "textures",
                JSONObject()
                    .put("layer0", "$namespace:$namespace/$texID")
            )

        // add custom model
        val modelData = ItemAllocator.addCustomModel(model)

        // save resource
        resources.put(file.nameWithoutExtension, Resource(modelData.first, modelData.second))
    }

    private fun addBBModel(file: File) {
        TODO("BBModels are not supported yet!")
    }

    internal fun finalizePack() {
        // if a resource pack was generated
        if (shouldGenerate) {
            // finalize sub systems
            ItemAllocator.cleanup()

            // zip resource pack
            packFile = File("ResourcePack.zip")
            ZipManager.zip(packFolder, packFile)
            packBytes = packFile.readBytes()

            // generate hash
            hash = BigInteger(1, MessageDigest.getInstance("SHA-1").digest(packBytes)).toString(16)

            // call event when the resource pack finishes (also starts local server if necessary)
            Bukkit.getScheduler().runTask(
                PaperCustomResourceToolkit.plugin,
                Runnable {
                    Bukkit.getPluginManager().callEvent(ResourcePackFinalizedEvent(packFile.absolutePath, hash))
                }
            )

            // attempt to copy to .minecraft
            if (passToMinecraft)
                packFolder.copyRecursively(File(System.getenv("APPDATA"), "/.minecraft/resourcepacks/passToMinecraft"), overwrite = true)
//                File("%APPDATA%/.minecraft/resourcepacks/passToMinecraft.zip").writeBytes(packBytes)
        } else
            Bukkit.getScheduler().runTask(
                PaperCustomResourceToolkit.plugin,
                Runnable {
                    Bukkit.getPluginManager().callEvent(ResourcePackFinalizedEvent("", ""))
                }
            )
    }
}
data class Resource(val itemType: Material, val data: Int)
class ResourcePackFinalizedEvent(val path: String, val hash: String): Event() {
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