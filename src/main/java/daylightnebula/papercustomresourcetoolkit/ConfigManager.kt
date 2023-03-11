package daylightnebula.papercustomresourcetoolkit

import org.json.JSONObject
import java.io.File

object ConfigManager {

    private lateinit var configFile: File
    lateinit var configJson: JSONObject

    fun init() {
        // load config folder
        configFile = File(PaperCustomResourceToolkit.plugin.dataFolder, "config.json")
        configJson = if (configFile.exists()) JSONObject(configFile.readText()) else JSONObject()
        println("Loaded config from ${configFile.absoluteFile}")
    }

    fun save() {
        // save config, creating the file and parent folders
        if (!configFile.parentFile.exists()) configFile.parentFile.mkdirs()
        configFile.writeText(configJson.toString(1))
        println("Saved config to ${configFile.absoluteFile}")
    }

    inline fun <reified T: Any> getValueFromJson(key: String, default: T): T {
        // get json value
        val any = configJson.opt(key)

        // if of correct type, return that
        if (any is T) return any
        // otherwise, remove the old value
        else configJson.remove(key)

        // save default value
        configJson.put(key, default)

        // return the default
        return default
    }
}