package daylightnebula.papercustomresourcetoolkit.packer

import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object AtlasManager {

    val atlasesFolder = File(ResourcePack.minecraftFolder, "atlases")
    val atlasJson = JSONObject()
        .put("sources", JSONArray().put(
            JSONObject()
                .put("type", "directory")
                .put("source", ResourcePack.namespace)
                .put("prefix", "${ResourcePack.namespace}/")
        ))

    fun init() {
        atlasesFolder.mkdirs()
        File(atlasesFolder, "blocks.json").writeText(atlasJson.toString(1))
    }
}