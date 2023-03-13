package daylightnebula.papercustomresourcetoolkit.packer

import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object AtlasManager {

    val atlasesFolder = File(ResourcePack.minecraftFolder, "atlases")
//    val atlasJson = JSONObject()
//        .put("sources", JSONArray().put(
//            JSONObject()
//                .put("type", "directory")
//                .put("source", ResourcePack.namespace)
//                .put("prefix", "${ResourcePack.namespace}/")
//        ))

    fun init() {
        // get and load block atlas
        atlasesFolder.mkdirs()
        val blocksAtlas = File(atlasesFolder, "blocks.json")
        val blocksJson = if (blocksAtlas.exists()) JSONObject(blocksAtlas.readText()) else JSONObject().put("sources", JSONArray())

        // add our own source to atlas
        blocksJson.getJSONArray("sources").put(
            JSONObject()
                .put("type", "directory")
                .put("source", ResourcePack.namespace)
                .put("prefix", "${ResourcePack.namespace}/")
        )

        // save atlas
        blocksAtlas.writeText(blocksJson.toString(1))
    }
}