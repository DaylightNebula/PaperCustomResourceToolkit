package daylightnebula.papercustomresourcetoolkit.packer

import daylightnebula.papercustomresourcetoolkit.ConfigManager
import org.bukkit.Material
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object ItemAllocator {

    private val defaultItemList = JSONArray().put("BLACK_DYE").put("RED_DYE").put("BLUE_DYE")
    private lateinit var itemList: JSONArray
    private var currentItem = 0
    private var currentItemCMID = 0
    private var currentModelID = 0

    private lateinit var currentItemJson: JSONObject

    fun init() {
        println("Loading item allocation information...")
        itemList = ConfigManager.getValueFromJson<JSONArray>("itemList", defaultItemList)
        currentItemJson = getCleanModelJson(Material.valueOf(itemList.getString(0)))
    }

    // save model
    fun addCustomModel(model: JSONObject): Pair<Material, Int> {
        val modelID = currentModelID++
        val modelData = getCleanCustomModelID()
        val file = File(ResourcePack.itemFolder, "$modelID.json")
        file.writeText(model.toString(1))
        currentItemJson.getJSONArray("overrides").put(
            JSONObject()
                .put(
                    "predicate",
                    JSONObject()
                        .put("custom_model_data", modelData.second)
                )
                .put("model", "item/${modelID}")
        )
        return modelData
    }

    private fun getCleanCustomModelID(): Pair<Material, Int> {
        // advance custom model id counter
        currentItemCMID++
        if (currentItemCMID >= 32766) {
            currentItemCMID = 1
            saveCurrentItemJson()
            currentItem++
        }

        // return pair
        return Pair(Material.valueOf(defaultItemList.getString(currentItem)), currentItemCMID)
    }

    // generates a clean model file for the given texture
    private fun getCleanModelJson(material: Material): JSONObject {
        return JSONObject()
            .put("parent", "minecraft:item/generated")
            .put(
                "textures",
                JSONObject()
                    .put("layer0", "minecraft:item/${material.name.lowercase()}")
            )
            .put("overrides", JSONArray())
    }

    // saves current item json to appropriate file
    private fun saveCurrentItemJson() {
        val material = Material.valueOf(itemList.getString(currentItem))
        val file = File(ResourcePack.itemFolder, "${material.name.lowercase()}.json")
        file.writeText(currentItemJson.toString(1))
    }

    fun cleanup() {
        saveCurrentItemJson()
    }
}