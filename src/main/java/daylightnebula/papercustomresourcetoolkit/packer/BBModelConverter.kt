package daylightnebula.papercustomresourcetoolkit.packer

import org.bukkit.Material
import org.bukkit.util.Vector
import org.json.JSONArray
import org.json.JSONObject
import java.lang.IllegalArgumentException
import java.util.*

object BBModelConverter {
    // config functions
    private val faceOptions = arrayOf("north", "south", "east", "west", "up", "down")
    private val displayJson = JSONObject().put("head", JSONObject().put("scale", JSONArray().put(1.6).put(1.6).put(1.6)))

    // public functions
    fun convertStatic(json: JSONObject): Pair<UUID, JSONObject> {
        val resolution = json.getJSONObject("resolution")
        val elements = json.getJSONArray("elements").map { convertElement(it as JSONObject, resolution) }
        return Pair(
            UUID.randomUUID(),
            JSONObject()
                .put("elements", elements)
                .put("textures", loadTextures(json.getJSONArray("textures")))
                .put("display", displayJson)
        )
    }

    fun convertAnimatedModelFromBBModel(json: JSONObject, allocate: (json: JSONObject) -> Pair<Material, Int>): AnimatedModelResource {
        // get converted elements and bone stack
        val elements = convertAnimatedModelToParts(json)
        val boneStacks = json.getJSONArray("outliner").map { createBoneStack(it as JSONObject, elements) }
        val animations = json.getJSONArray("animations").map { convertToPreRenderAnimation(it as JSONObject) }

        // render and return animations
        return AnimatedModelResource(
            RenderedAnimationStack(elements.map { pair -> allocate(pair.value) }),
            animations.map { RenderedAnimation(it.name, renderAnimation(boneStacks, it)) }
        )
    }

    // post render animation stuff
    data class RenderedAnimation(val name: String, val stacks: List<RenderedAnimationStack>)
    data class RenderedAnimationStack(val stack: List<Pair<Material, Int>>)
    private fun renderAnimation(boneStacks: List<BoneAnimatedComponent>, animation: PreRenderAnimation): List<RenderedAnimationStack> {
        return listOf() //RenderedAnimationStack(listOf()) // TODO
    }

    // pre render animation stuffs
    data class PreRenderAnimation(val uuid: UUID, val name: String, val loop: Boolean, val length: Double, val animators: HashMap<UUID, PreRenderAnimator>)
    data class PreRenderAnimator(val uuid: UUID, val positionKeyFrames: List<PreRenderKeyFrame>, val rotationKeyFrames: List<PreRenderKeyFrame>, val scaleKeyFrames: List<PreRenderKeyFrame>)
    data class PreRenderKeyFrame(val time: Double, val vec: Vector, val isLinear: Boolean)
    private fun convertToPreRenderAnimation(json: JSONObject): PreRenderAnimation {
        // generate animators hash map
        val animators = hashMapOf<UUID, PreRenderAnimator>()
        val animatorsJson = json.getJSONObject("animators")
        animatorsJson.keys().forEach { key ->
            animators[UUID.fromString(key)] = convertToPreRenderAnimator(animatorsJson.getJSONObject(key))
        }

        // save pre render animation
        return PreRenderAnimation(
            UUID.fromString(json.getString("uuid")),
            json.getString("name"),
            json.getString("loop") == "loop",
            json.getDouble("length"),
            animators
        )
    }
    private fun convertToPreRenderAnimator(json: JSONObject): PreRenderAnimator {
        // create channel key frame lists
        val positionKeyFrames = mutableListOf<PreRenderKeyFrame>()
        val rotationKeyFrames = mutableListOf<PreRenderKeyFrame>()
        val scaleKeyFrames = mutableListOf<PreRenderKeyFrame>()

        // load keyframes
        json.getJSONArray("keyframes").forEach {
            val keyFrame = it as JSONObject

            // get key frame list and add key frame
            when(val channel = keyFrame.getString("channel")) {
                "position" -> positionKeyFrames
                "rotation" -> rotationKeyFrames
                "scale" -> scaleKeyFrames
                else -> throw IllegalArgumentException("Unknown key frame channel $channel")
            }.add(convertToPreRenderKeyFrame(keyFrame))
        }

        // create and return animator
        return PreRenderAnimator(UUID.fromString(json.getString("uuid")), positionKeyFrames, rotationKeyFrames, scaleKeyFrames)
    }
    private fun convertToPreRenderKeyFrame(json: JSONObject): PreRenderKeyFrame {
        // convert data point
        val dataPointJson = json.getJSONArray("data_points").first() as JSONObject
        val dataPoint = Vector(
            handlePossibleStringInDoubleConversion(dataPointJson.get("x")),
            handlePossibleStringInDoubleConversion(dataPointJson.get("y")),
            handlePossibleStringInDoubleConversion(dataPointJson.get("z")),
        )

        // create and return key frame
        return PreRenderKeyFrame(
            json.getDouble("time"),
            dataPoint,
            json.getString("interpolation") == "linear"
        )
    }
    private fun handlePossibleStringInDoubleConversion(any: Any): Double {
        if (any is String) return any.toDoubleOrNull() ?: throw IllegalArgumentException("Could not convert $any to double")
        else if (any is Double) return any
        else throw IllegalArgumentException("Could not convert $any to double")
    }

    // bone stuff
    abstract class AnimatedComponent(val uuid: UUID)
    class BoneAnimatedComponent(uuid: UUID, val origin: Vector, val children: List<AnimatedComponent>): AnimatedComponent(uuid)
    class ElementAnimatedComponent(uuid: UUID, val element: JSONObject): AnimatedComponent(uuid)
    private fun createBoneStack(root: JSONObject, elements: HashMap<UUID, JSONObject>): BoneAnimatedComponent {
        val origin = root.getJSONArray("origin")
        return BoneAnimatedComponent(
            UUID.fromString(root.getString("uuid")),
            Vector(origin.getDouble(0), origin.getDouble(1), origin.getDouble(2)),
            root.getJSONArray("children").map {
                if (it is String) {
                    val uuid = UUID.fromString(it)
                    ElementAnimatedComponent(uuid, elements[uuid]!!)
                } else if (it is JSONObject)
                    createBoneStack(it, elements)
                else
                    throw IllegalArgumentException("Cannot convert $it to AnimatedComponent")
            }
        )
    }
    private fun convertAnimatedModelToParts(json: JSONObject): HashMap<UUID, JSONObject> {
        val resolution = json.getJSONObject("resolution")
        val elements = json.getJSONArray("elements")
        val textures = loadTextures(json.getJSONArray("textures"))
        val output = hashMapOf<UUID, JSONObject>()
        elements.forEach {
            val elementJson = it as JSONObject
            val element = convertElement(elementJson, resolution)
            output[UUID.fromString(elementJson.getString("uuid"))] =
                JSONObject()
                    .put("elements", listOf(element))
                    .put("textures", textures)
                    .put("display", displayJson)
        }
        return output
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