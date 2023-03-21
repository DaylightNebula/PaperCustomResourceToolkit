package daylightnebula.papercustomresourcetoolkit.packer

import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.*

object FontManager {

    private var fontOffset = 0
    private var texturedCharacterOffset = 0

    private const val fontImageWidth = 16
    private const val fontImageHeight = 16
    private val texturedProviders = JSONArray()

    fun addTexturedCharacter(name: String, texID: String, ascent: Int, height: Int) {
        if (!ResourcePack.shouldGenerate) return

        val characterLocation = (texturedCharacterOffset++).toChar()
        val texture = TextureAllocator.getTextureID(texID)
        if (texture == null) {
            System.err.println("No texture with name \"$texID\" created!")
            return
        }

        // save font before offset changes
        ResourcePack.addResource(ResourceType.TEXT_IMAGE, name, TextImageResource(name, characterLocation))

        // save everything to a json object then add that object to providers
        texturedProviders.put(
            JSONObject()
                .put("file", "${ResourcePack.namespace}:${ResourcePack.namespace}/$texture.png")
                .put("ascent", ascent)
                .put("height", height)
                .put("type", "bitmap")
                .put("chars", JSONArray().put(characterLocation))
        )
    }

    fun addFont(name: String, ascent: Int = 7, size: Int = 16, oversample: Int = 0, height: Int = 8) {
        val id = (fontOffset++).toString()

        // save font resource (doing this now before offset changes)
        ResourcePack.addResource(ResourceType.FONT, name, FontResource(name, id))

        // generate json and add it to the font providers
        File(ResourcePack.namespacedFontFolder, "$id.json").writeText(
            JSONObject().put("providers", JSONArray().put(
                JSONObject()
                    .put("file", "minecraft:font/ascii.png")
                    .put("ascent", ascent)
                    .put("size", size)
                    .put("oversample", oversample)
                    .put("type", "bitmap")
                    .put("height", height)
                    .put("chars", defaultCharsArray)
            )).toString(4)
        )
    }

    fun finalize() {
        addTexturedCharacter("red-sword", "sword-handheld", 0, 8)

        // add negative spaces file
        File(ResourcePack.fontFolder, "negative_spaces.ttf").writeBytes(Base64.getDecoder().decode(negativeSpacesFile))

        // add ascii texture
        val asciiOutput = File(ResourcePack.minecraftFolder, "textures/font/ascii.png")
        asciiOutput.parentFile.mkdirs()
        asciiOutput.writeBytes(Base64.getDecoder().decode(asciiFile))

        // create default json
        val defaultJson = JSONObject().put(
            "providers",
            JSONArray()
                .put(
                    JSONObject()
                        .put("file", "minecraft:font/ascii.png")
                        .put("ascent", 7)
                        .put("size", 16)
                        .put("oversample", 0)
                        .put("type", "bitmap")
                        .put("chars", defaultCharsArray)
                )
                .put(
                    JSONObject()
                        .put("file", "minecraft:negative_spaces.ttf")
                        .put("size", 10)
                        .put("shift", JSONArray().put(0).put(0))
                        .put("oversample", 1)
                        .put("type", "ttf")
                )
        )
        File(ResourcePack.fontFolder, "default.json").writeText(defaultJson.toString(4))

        // save textured font
        File(ResourcePack.namespacedFontFolder, "textured.json").writeText(
            JSONObject()
                .put(
                    "providers",
                    texturedProviders
                )
                .toString(4)
        )
    }

    private val defaultCharsArray = JSONArray()
        .put("\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007\b\t\n\u000b\u000c\r\u000e\u000f")
        .put("\u0010\u0011\u0012\u0013\u0014\u0015\u0016\u0017\u0018\u0019\u001a\u001b\u001c\u001d\u001e\u001f")
        .put(" !\"#$%&'()*+,-./")
        .put("0123456789:;<=>?")
        .put("@ABCDEFGHIJKLMNO")
        .put("PQRSTUVWXYZ[\\]^_")
        .put("`abcdefghijklmno")
        .put("pqrstuvwxyz{|}~")
        .put("\u0080\u0081\u0082\u0083\u0084\u0085\u0086\u0087\u0088\u0089\u008a\u008b\u008c\u008d\u008e\u008f")
        .put("\u0090\u0091\u0092\u0093\u0094\u0095\u0096\u0097\u0098\u0099\u009a\u009b\u009c\u009d\u009e\u009f")
        .put(" ¡¢£¤¥╕╣§¨©ª«¬­®¯")
        .put("°±²³´µ¶·¸¹º»¼½¾¿")
        .put("ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏ")
        .put("ÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞß")
        .put("àáâãäåæçèéêëìíîï")
        .put("ðñòóôõö÷øùúûüýþÿ")

    private const val asciiFile = "iVBORw0KGgoAAAANSUhEUgAAAQAAAAEAAQMAAABmvDolAAAABlBMVEX///////9V" +
            "fPVsAAAAAXRSTlMAQObYZgAACNxJREFUaN7tmE1sG8cVgEcbIl0FjLqiVIcymHhF" +
            "UIbiEykZgSnR7HJNqSRlJzTdFIgjoa4CGEFOJlkUokRRZpEApgLDMdSLG7BCWitI" +
            "AhQI0F7US8WFAkTbGq6QIJdAMHQ10INy661v3szuzvJHNKwgdYLOw3J3Od++mXnz" +
            "9u3MI590KYT0dJHrXeRb0JC5GF2b3FjaKP198UJko7i7sD91kDiYOlg+OLsf2z97" +
            "QB5mjVwj1AgZKSNnjprKZ2Rba+DRUBrKlkYuXxz7ZWSoNLSYXrq0GAENjXIicZBM" +
            "LB8k9qP9ZxJk5w+ruRrT8BXVsK3Spz+iGtR3lLc1GEV+LT9SGinVFl9JbxQflBrw" +
            "dI+SSPSc7V/YLx18F3Y4uoZXuxTyvfCHQjRfy98BeSe9dvrS0ub4yHL/xFzpfqkR" +
            "beQb6QYxVdNvekC8D39u/L4R2v5r41MjZygGMYgJQjLRTC/IUOYnb70Wea4U/dmf" +
            "ly+OzZVul6qvVfPVdJVqkE0FNAS+/rLmAw13G8pqbiu7pX2u/QsOUtzM10GgD5lL" +
            "+YFS9PTGcn++vnTrXHWyOlMdr/5g/IF0KT1d6mk/g/ad3F6DCHiI2qxBBsAHvxES" +
            "gF+ZKEKtRMiP8gyIkwyc2wA3UrRdH0kjQKBaRmHFQ8jTeXpTISly2gbSNgDnqkZv" +
            "QuQM3AQ4cEoE+hIM6IObKAcCZAW10jscBb3pJcfIVIsGxbJkCtp9kegtfVAtDSkY" +
            "5otkGltl4/DDtZ9oVAMDZKiOcsAyOgLXmyxvmdqeFZjxtkUhT1DxQ39jIB6wZgAO" +
            "eu2UGJ0FP1hQJ14Yswrzr6M90RPgH7jWySj+OQi3Ggf8IB40NAIy/C2BJgp4bECG" +
            "5hSqhwJeaD1JyiAeaDVuG4q6MAKUTKJ6qkGy5hHuZAbQzlUQkG2AaeCAhs86gNMH" +
            "dDYL0DngwWo2CpkaKo7m8aCJqNHOoHJmB7mTfZ+oyX6EEoCBlmFwCzBYOlwdhn0C" +
            "fjW0S4xGlxBeJnE2mbE8eKeTeTrdGQ7oLiAF11cZ8AteyQCNA1kEchSgP2WsKOO5" +
            "gsAVuLpsARVBw8tcAwMylgYH0IUmdAa8zLsoAknspA7vu8481xnmNAeSDvACdo8Z" +
            "SiMTaKgz8FuxDPWkF3/nqj44rsE0QqFDpMcKDCqBQ0vgfGjg2RgILDvKAiBxgLQD" +
            "QuBCEr6vLkBDIMgBD77xHCijqcMcuAw9H+QfJg7QSUlBCHEAK5wLQBJbpXHqKr7t" +
            "qhu4DIDuAjR0Ghu4ao9C5lcavv42EMN5l/FgQEUECHmqo6kJftyOXNRuQNdoJONI" +
            "WHT38sjmaYYsQLEBze1NDiDhdVOEjJFZOCbgUOB3Fp71gvFl8hv8Hz1inr9mZaiY" +
            "5wGEfsHnmdNJdkRS+LTRt5sC/PMU50BFAK6IgBMfFHxtWzSIgGoHMQGYtjup4NSz" +
            "MCgAE8IwdYwPHvxNt4sP7KOa6jwz7InwozvDUVabsr3abFvomxaGBaSgQWnyMxkt" +
            "cZ0cCkyT4i4YiRmqzIOHjtNmvZumAmZmpmZWZOaVbGBm1w4gFhDCr7fCV6WmwqvD" +
            "CIQA8PMVBQOKDzjgdQFOALlueYNkAyFyUgwgPdhJDZ+ahqdpJ49jj1gToIEOk8YI" +
            "L5z95CUcQ8UGQIPG1yyuxTY3NWpoBWLCZH1f9pv5erpeXJtZ+/XmzG70XuneTH1m" +
            "c+pe4fnCQN9wdC+6R8wclYe5D+8+VAyf4dtRd+Tt90yf6auphmRIpPjF4lxx/fX1" +
            "kY3XdyPrpfXSUKk3WSsMUA2Rm5GbxMiZWeBzd++ayo3cVm7HsyN/9DbVtareyN6A" +
            "uZxL3y4MYB8eFNeW1tLRdES5c27v3L3e4eJ6sf7//MN36A/3C1OFucl69Iup/smb" +
            "Szejt8bfp37Q95/I/cLw+C14f83zpt5QDd+/FSMHkt0OGrIhv6eayj+umFmSeRY0" +
            "XIgM6SdevTi+vrQ+dmHkT2O9iZp3GGb5g6XbxJSMpJGteQzfZ5+uUi/Qf/fHVXkr" +
            "9Vv186wBWkh+L3ErsZ6P0j4UBhYH8vU33s/vTdYD0L4+nPnxDzT/EGr5VvR0AnhN" +
            "7Vq7HQMtf8vh5vzZdCvAopZ+CU/vXqMBj656ErhUokHsBaz5yz8xnPWmWVTVMfug" +
            "46eR5iMIeWZKt/oQByCEq0C6vLgDGijwTXKT9ZV+ehRch7EmNqyx9GxYOZAUfG9C" +
            "uOqiSj8AjSGsQYDmQFLYhGwD7ONc6cEmaA6ENbHiaqICGj6mAM2BxLkGtke8g4Bk" +
            "9YHmQGRcpsj2MGlWRQINOEyaA5HJr/gGNsajvQ81xA79Ols1nfIPdk3X1eCok9Tp" +
            "DAQwj+SsHALNC84giIJrMrbtCDavNSTuIotcZ5A/NM/0ytiEA9D8VAoq5zGNhX+o" +
            "Lg30gRQYO2Q58AoAQRdA52IUxG9piGMTQfw4s3sGvGkBrATJM/ZS1wcyCEe4GXBe" +
            "n2OwCjjG13ZtgVNwf4oB1g7gJQFIwUY1iZtVoZxyATpWx9zbgL4O2UHXPqFlLRmG" +
            "1YafjGHKx7kP4fqcnuEUhtN527DsPkSe5udH21122ZU6cgKGVeES58KnlcmonUPQ" +
            "eBJSdgOpdoAuSMqu7gCsQEWZiw1UBOmq4REA/YjAK+0M1W5Ff0hpu7MYE3xC9Aiv" +
            "BZwXfEL0iPBjTr/UtIskC+jclhBMtOh8p6DRFFwS9waWENztWcAC3PHNpANM29dB" +
            "zOjARc5lzYwA0LQB+cYFhARgELeormdobjMnJD2SLAWacWkQgQUGTLv6MC8AmGVN" +
            "ugCCXXOA+VZDzdrXFJhtNbX2P0guOd7k9qdwa4Txu/zJ2xphHrfEBIkL11bUcc1D" +
            "Wri2Xz1JkF6eh3ABcUF+iglMJo8bH9oA04K07eSEIHHXkL/F+HDUMnZohEF/OCzC" +
            "wD+D3dJhwUMijMr+mHV5QUywJr0GW3UKICxCuCaLApnDgUo3gDTFm0OBk/AVPNkM" +
            "+DEfweQ4mOo44dlJJz7EbO+Q7OVTh/jQARBfKRWO/wKbJ7FgdEyP7AAAAABJRU5E" +
            "rkJggg=="

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