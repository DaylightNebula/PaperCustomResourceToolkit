package daylightnebula.papercustomresourcetoolkit.packer

import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStreamWriter
import java.util.*

object FontManager {

    private var offset = 0
    private const val offsetMax = 65535
    private const val fontImageWidth = 16
    private const val fontImageHeight = 16
    private val fontProviders = JSONArray()

    fun init() {
        // add base font and negative spaces
        addFont("default")
        fontProviders.put(negativeSpacesJson)
    }

    fun addTexturedCharacter(name: String, texID: String, ascent: Int, height: Int) {
        val texture = TextureAllocator.getTextureID(texID)
        if (texture == null) {
            System.err.println("No texture with name \"$texID\" created!")
            return
        }

        // save font before offset changes
        ResourcePack.addResource(ResourceType.TEXT_IMAGE, name, TextImageResource(name, offset))
        println("Adding texture with $offset")

        // save everything to a json object then add that object to providers
        fontProviders.put(
            JSONObject()
                .put("file", "${ResourcePack.namespace}:${ResourcePack.namespace}/$texture.png")
                .put("ascent", ascent)
                .put("height", height)
                .put("type", "bitmap")
                .put("chars", JSONArray().put(offset.toChar()))
        )

        // update offset
        offset++
    }

    fun addFont(name: String, ascent: Int = 7, size: Int = 16, oversample: Int = 0) {
        // save font resource (doing this now before offset changes)
        ResourcePack.addResource(ResourceType.FONT, name, FontResource(name, offset))

        // generate characters array
        val chars = JSONArray()
        for (y in 0 until fontImageHeight) {
            chars.put(String((0 until fontImageWidth).map { (it + offset).toChar() }.toCharArray()).replace("¦", "╕╣")) // ¦ = ╕╣
            offset += fontImageWidth
        }

        // generate json and add it to the font providers
        fontProviders.put(
            JSONObject()
                .put("file", "minecraft:font/ascii.png")
                .put("ascent", ascent)
                .put("size", size)
                .put("oversample", oversample)
                .put("type", "bitmap")
                .put("chars", chars)
        )
    }

    fun finalize() {
        File(ResourcePack.fontFolder, "negative_spaces.ttf").writeBytes(Base64.getDecoder().decode(negativeSpacesFile))

        val defaultJsonFile = File(ResourcePack.fontFolder, "default.json")//.writeText(JSONObject().put("providers", fontProviders).toString(4))
        val writer = OutputStreamWriter(defaultJsonFile.outputStream(), "UTF-8")
        writer.write(JSONObject().put("providers", fontProviders).toString(4))
        writer.flush()
    }

    private val negativeSpacesJson = JSONObject()
        .put("file", "minecraft:negative_spaces.ttf")
        .put("size", 10)
        .put("shift", JSONArray().put(0).put(0))
        .put("oversample", 1)
        .put("type", "ttf")
    private const val negativeSpacesFile = "AAEAAAAHAEAAAgAwY21hcPCK8KEAAAFkAAAATGdseWZXAoc1AAAB+AAAAnZoZWFk" +
            "DnYbrAAAAHwAAAA2aGhlYQBXABQAAAC0AAAAJGhtdHgAAQAAAAAA2AAAAIxsb2Nh" +
            "CsILZAAAAbAAAABIcG9zdAGcAMwAAARwAAAAIAABAAAAAQAACz9zQl8PPPUAAgBk" +
            "AAAAANeqQQAAAAAA17WdSgAAAAAAAQABAAAACAACAAAAAAAAAAEAAABV//EAAAAA" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAjAAEAAAAKAAAAFAAAAB4AAAAoAAAAMgAA" +
            "ADwAAABGAAAAUAAAAKAAAAFAAAACgAAABQAAAAoAAAAUAAAAKAAAAFAAAAB//wAA" +
            "//YAAP/sAAD/4gAA/9gAAP/OAAD/xAAA/7oAAP+wAAD/YAAA/sAAAP2AAAD7AAAA" +
            "9gAAAOwAAADYAAAAsAAAAIABAAAAAAACAAAAAwAAABQAAwABAAAAFAAEADgAAAAK" +
            "AAgAAgAC+AD4D/gg+C///wAA+AD4Afgg+CH//wgiCBEH8QfgAAEAAAAAAAAAAAAA" +
            "AAAACQASABsAJAAtADYAPwBIAFEAWgBjAGwAdQB+AIcAkACZAKIAqwC0AL0AxgDP" +
            "ANgA4QDqAPMA/AEFAQ4BFwEgASkBMgE7AAEAAAAAAAEAAQABAAAxNwEBAAEAAAAA" +
            "AAEAAQABAAAxNwEBAAEAAAAAAAEAAQABAAAxNwEBAAEAAAAAAAEAAQABAAAxNwEB" +
            "AAEAAAAAAAEAAQABAAAxNwEBAAEAAAAAAAEAAQABAAAxNwEBAAEAAAAAAAEAAQAB" +
            "AAAxNwEBAAEAAAAAAAEAAQABAAAxNwEBAAEAAAAAAAEAAQABAAAxNwEBAAEAAAAA" +
            "AAEAAQABAAAxNwEBAAEAAAAAAAEAAQABAAAxNwEBAAEAAAAAAAEAAQABAAAxNwEB" +
            "AAEAAAAAAAEAAQABAAAxNwEBAAEAAAAAAAEAAQABAAAxNwEBAAEAAAAAAAEAAQAB" +
            "AAAxNwEBAAEAAAAAAAEAAQABAAAxNwEBAAEAAAAAAAEAAQABAAAxNwEBAAEAAAAA" +
            "AAEAAQABAAAxNwEBAAEAAAAAAAEAAQABAAAxNwEBAAEAAAAAAAEAAQABAAAxNwEB" +
            "AAEAAAAAAAEAAQABAAAxNwEBAAEAAAAAAAEAAQABAAAxNwEBAAEAAAAAAAEAAQAB" +
            "AAAxNwEBAAEAAAAAAAEAAQABAAAxNwEBAAEAAAAAAAEAAQABAAAxNwEBAAEAAAAA" +
            "AAEAAQABAAAxNwEBAAEAAAAAAAEAAQABAAAxNwEBAAEAAAAAAAEAAQABAAAxNwEB" +
            "AAEAAAAAAAEAAQABAAAxNwEBAAEAAAAAAAEAAQABAAAxNwEBAAEAAAAAAAEAAQAB" +
            "AAAxNwEBAAEAAAAAAAEAAQABAAAxNwEBAAEAAAAAAAEAAQABAAAxNwEBAAEAAAAA" +
            "AAEAAQABAAAxNwEBAAEAAAAAAAEAAQABAAAxNwEBAAAAAwAAAAAAAAGZAMwAAAAA" +
            "AAAAAAAAAAAAAAAAAAAAAA=="
}