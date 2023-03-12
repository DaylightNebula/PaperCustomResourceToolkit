package daylightnebula.papercustomresourcetoolkit.packer

import org.json.JSONArray
import org.json.JSONObject
import java.lang.IllegalArgumentException
import java.math.BigDecimal

object BBModelConverter {
    // config functions
    private val faceOptions = arrayOf("north", "south", "east", "west", "up", "down")
    private val displayJson = JSONObject().put("head", JSONObject().put("scale", JSONArray().put(1.6).put(1.6).put(1.6)))

    // public functions
    fun convertStatic(json: JSONObject): JSONObject {
        val resolution = json.getJSONObject("resolution")
        return JSONObject()
            .put("elements", json.getJSONArray("elements").map { convertElement(it as JSONObject, resolution) })
            .put("textures", loadTextures(json.getJSONArray("textures")))
            .put("display", displayJson)
    }

    fun convertAnimatedModel(json: JSONObject): Array<JSONObject> {
        val resolution = json.getJSONObject("resolution")
        val elements = json.getJSONArray("elements")
        val textures = loadTextures(json.getJSONArray("textures"))
        return elements.map {
            JSONObject()
                .put("elements", listOf(convertElement(it as JSONObject, resolution)))
                .put("textures", textures)
                .put("display", displayJson)
        }.toTypedArray()
    }

    // helper functions
    private fun convertElement(srcElement: JSONObject, resolution: JSONObject): JSONObject {
        val out = JSONObject()

        // copy over those that are the same
        out.put("name", srcElement.get("name"))
        out.put("from", srcElement.get("from"))
        out.put("to", srcElement.get("to"))

        // get faces array and convert texture element of each face to MC format
        val faces = srcElement.get("faces") as JSONObject
        faceOptions.forEach { faceName ->
            // get face
            val face = faces.get(faceName) as JSONObject

            // convert block bench UVs to MC UVs
            val uv = face.getJSONArray("uv")
            uv.put(0, (uv.getDouble(0) / resolution.getInt("width")) * 16.0)
            uv.put(1, (uv.getDouble(1) / resolution.getInt("height")) * 16.0)
            uv.put(2, (uv.getDouble(2) / resolution.getInt("width")) * 16.0)
            uv.put(3, (uv.getDouble(3) / resolution.getInt("height")) * 16.0)
            face.put("uv", uv)

            // update texture entry
            face.put("texture", "#${face.get("texture")}")
        }
        out.put("faces", faces)

        // build and save placeholder rotation object
        val rotation = JSONObject()
        rotation.put("origin", srcElement.get("origin"))
        rotation.put("angle", 0)
        rotation.put("axis", "y")
        out.put("rotation", rotation)
        return out
    }
    private fun loadTextures(textureArray: JSONArray): JSONObject {
        val textures = JSONObject()
        loadTexturesFromJsonArray(textureArray).forEachIndexed { index, i ->
            textures.put(index.toString(), "${ResourcePack.namespace}:${ResourcePack.namespace}/$i")
        }
        return textures
    }
    private fun loadTexturesFromJsonArray(json: JSONArray): Array<Int> {
        // map the json array to an array of integers, those integers being texture ids after they are processed by the texture allocator
        return json.map {
            if (it is JSONObject) {
                val src = it.getString("source")
                TextureAllocator.saveTexture(src)
            } else -1
        }.toTypedArray()
    }
}