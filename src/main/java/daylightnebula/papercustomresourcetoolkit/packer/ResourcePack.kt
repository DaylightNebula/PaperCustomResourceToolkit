package daylightnebula.papercustomresourcetoolkit.packer

import daylightnebula.papercustomresourcetoolkit.ConfigManager
import daylightnebula.papercustomresourcetoolkit.PaperCustomResourceToolkit
import daylightnebula.papercustomresourcetoolkit.ZipManager
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.math.BigInteger
import java.security.MessageDigest
import java.util.*
import kotlin.collections.HashMap

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
    internal var shouldGenerate = true
    private var passToMinecraft = true

    private val resources = hashMapOf<ResourceType<*>, HashMap<String, Resource>>().apply {
        this[ResourceType.IMAGE] = hashMapOf()
        this[ResourceType.STATIC_MODEL] = hashMapOf()
        this[ResourceType.ANIMATED_MODEL] = hashMapOf()
        this[ResourceType.TEXT_IMAGE] = hashMapOf()
        this[ResourceType.FONT] = hashMapOf()
    }
    const val namespace = "custom_resource_toolkit"
    private val packFolder = File("ResourcePack")
    private val assetsFolder = File(packFolder, "assets")
    private val namespaceFolder = File(assetsFolder, namespace)
    val texturesFolder = File(namespaceFolder, "textures/$namespace")
    val minecraftFolder = File(assetsFolder, "minecraft")
    val fontFolder = File(minecraftFolder, "font")
    private val modelsFolder = File(minecraftFolder, "models")
    val itemFolder = File(modelsFolder, "item")
    private val blockFolder = File(modelsFolder, "block")
    private val metaFile = File(packFolder, "pack.mcmeta")
    private val configFolder = File(PaperCustomResourceToolkit.plugin.dataFolder, "pack_config")

    fun getResource(type: ResourceType<*>, key: String): Resource? {
        return resources[type]?.let { it[key] }
    }

    fun getAllResourcesOfType(type: ResourceType<*>): Collection<Resource>? {
        return resources[type]?.values
    }

    internal fun addResource(type: ResourceType<*>, key: String, value: Resource) {
        resources[type]?.let { it[key] = value }
    }

    internal fun init() {
        // initialize sub system
        ItemAllocator.init()
        FontManager.init()

        // get should generate config value
        shouldGenerate = ConfigManager.getValueFromJson("shouldGenerateResourcePack", true)
        passToMinecraft = ConfigManager.getValueFromJson("attemptMinecraftExport", true)

        // check if we should generate
        if (shouldGenerate) {
            // create folders
            makeSureExistsAndClear(texturesFolder)
            makeSureExistsAndClear(itemFolder)
            makeSureExistsAndClear(blockFolder)
            makeSureExistsAndClear(fontFolder)

            // create meta file
            metaFile.writeText(packJson.toString(1))
        } else {
            // load from save files
            loadPackConfig()
        }
    }

    private fun savePackConfig() {
        // make config directory
        configFolder.mkdirs()

        // loop through maps to save
        resources.forEach { (type, map) ->
            val arr = JSONArray()
            map.values.map { type.toJson(it) }.forEach { arr.put(it) }
            File(configFolder, "${type.name}.json").writeText(arr.toString(4))
        }
    }

    private fun loadPackConfig() {
        // make sure we have a config folder
        if (!configFolder.exists()) return

        // get all configs and loop through them
        configFolder.listFiles()?.forEach { file ->
            println("Attempt to load config ${file.name}")
            // get config file with type name
            resources.keys.firstOrNull { it.name == file.nameWithoutExtension }?.let { key ->
                println("Found key")
                // get json array
                val map = resources[key]!!
                val arr = JSONArray(file.readText())

                // loop through all json objects and load them
                arr.forEach {
                    val json = it as? JSONObject ?: return@forEach
                    val resource = key.fromJson(json)
                    println("Loaded resource ${resource.name}")
                    map[resource.name] = resource
                }
            }
        }
    }

    private fun makeSureExistsAndClear(file: File) {
        if (file.exists()) {
            file.listFiles()?.forEach { it.deleteRecursively() }
        } else
            file.mkdirs()
    }

    internal fun addAssetsFolder(folder: File) {
        if (!shouldGenerate) return

        // load every file in the folder that is passed in
        folder.listFiles()?.forEach { file ->
            if (file.isDirectory)
                addAssetsFolder(file)
            else {
                when (file.extension) {
                    "png" -> addPNGTexture(file)
                    "bbmodel" -> addBBModel(file)
                }
                println("Created resource ${file.nameWithoutExtension}")
            }
        }
    }

    fun addCustomResource(path: String, content: String) {
        val file = File(assetsFolder, path)
        file.parentFile.mkdirs()
        file.writeText(content)
    }

    private fun addPNGTexture(file: File) {
        // get new texture id
        val texID = TextureAllocator.saveTexture(file)

        val hasOptions = file.nameWithoutExtension.contains("-")
        val parentType = if (hasOptions) file.nameWithoutExtension.split("-", limit = 2).last() else "generated"
        val textureName = if (hasOptions) file.nameWithoutExtension.split("-", limit = 2).first() else file.nameWithoutExtension

        // generate model for the texture
        val model = JSONObject()
            .put("parent", "minecraft:item/$parentType")
            .put(
                "textures",
                JSONObject()
                    .put("layer0", "$namespace:$namespace/$texID")
            )

        // add custom model
        val modelData = ItemAllocator.addCustomModel(model)

        // save resource
        addResource(ResourceType.IMAGE, file.nameWithoutExtension, ImageResource(file.nameWithoutExtension, modelData.first, modelData.second))
    }

    private fun addBBModel(file: File) {
        val json = JSONObject(file.readText())

        if (json.has("animations")) {
            val target = File(PaperCustomResourceToolkit.plugin.dataFolder, "../ModelEngine/blueprints/${file.name}")
            target.parentFile.mkdir()
            file.copyTo(target, overwrite = true)
            addResource(ResourceType.ANIMATED_MODEL, file.nameWithoutExtension, AnimatedModelResource(file.nameWithoutExtension, ))
        } else {
            val (uuid, model) = BBModelConverter.convertStatic(json)
            val modelData = ItemAllocator.addCustomModel(model)
            addResource(ResourceType.STATIC_MODEL, file.nameWithoutExtension, StaticModelResource(file.nameWithoutExtension, uuid, modelData.first, modelData.second))
        }
    }

    internal fun finalizePack() {
        // if a resource pack was generated
        if (shouldGenerate) {
            // finalize sub systems
            ItemAllocator.cleanup()

            // copy model instance assets
            val meAssets = File(PaperCustomResourceToolkit.plugin.dataFolder, "../ModelEngine/resource pack/assets")
            meAssets.copyRecursively(assetsFolder, overwrite = true)

            // finalize atlas
            AtlasManager.init()
            FontManager.finalize()

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

            savePackConfig()
        } else
            Bukkit.getScheduler().runTask(
                PaperCustomResourceToolkit.plugin,
                Runnable {
                    Bukkit.getPluginManager().callEvent(ResourcePackFinalizedEvent("", ""))
                }
            )
        println("Pack finalized")
    }
}
abstract class Resource(val name: String)
class ImageResource(name: String, val material: Material, val customModelID: Int): Resource(name)
class StaticModelResource(name: String, val id: UUID, val material: Material, val customModelID: Int): Resource(name)
class AnimatedModelResource(name: String): Resource(name)
class TextImageResource(name: String, val offset: Int): Resource(name)
class FontResource(name: String, val firstChar: Int): Resource(name)
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