package daylightnebula.papercustomresourcetoolkit.packer

import org.bukkit.Material
import org.bukkit.util.Vector
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal
import java.util.*
import kotlin.IllegalArgumentException
import kotlin.math.absoluteValue

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

//    // post render animation stuff
//    data class RenderedAnimation(val name: String, val frames: RenderedAnimationFrames)
//    data class RenderedAnimationStack(val stack: List<Pair<Material, Int>>)
//    data class RenderedAnimationFrames(val frames: List<RenderedAnimationStack>)
//    private fun renderAnimation(
//        boneStacks: List<BoneAnimatedComponent>,
//        animation: PreRenderAnimation,
//        create: (element: ElementAnimatedComponent, position: Vector3f, rotation: Vector3f) -> Pair<Material, Int>
//    ): RenderedAnimationFrames {
//        // loop through every frame, creating a new stack for each
//        val numFrames = (animation.length * 20.0).toInt()
//        val frames = RenderedAnimationFrames(
//            (0 .. numFrames).map { tick ->
//                RenderedAnimationStack(
//                    boneStacks.map {
//                        renderBoneStack(it, animation, tick / 20.0, create)
//                    }.flatten()
//                )
//            }
//        )
//        return frames
//    }
//    private fun renderBoneStack(
//        boneStack: BoneAnimatedComponent,
//        animation: PreRenderAnimation,
//        time: Double,
//        create: (element: ElementAnimatedComponent, position: Vector3f, rotation: Vector3f) -> Pair<Material, Int>
//    ): List<Pair<Material, Int>> {
//        return renderBoneStackRecursive(boneStack, animation, Matrix4f().identity(), time, create)
//    }
//    private fun renderBoneStackRecursive(
//        boneStack: BoneAnimatedComponent,
//        animation: PreRenderAnimation,
//        matrix: Matrix4f,
//        time: Double,
//        create: (element: ElementAnimatedComponent, position: Vector3f, rotation: Vector3f) -> Pair<Material, Int>
//    ): List<Pair<Material, Int>> {
//        val output = mutableListOf<Pair<Material, Int>>()
//        matrix.set(boneStack.children.firstOrNull { it is ElementAnimatedComponent }?.let { e ->
//            val element = e as ElementAnimatedComponent
//
//            // apply translation
//            val translation = (animation.animators[boneStack.uuid] ?: return listOf()).getPositionAtTime(time)
//            matrix.translate(translation.x.toFloat(), translation.y.toFloat(), translation.z.toFloat())
//
//            // rotate around origin
////            val rotation = (animation.animators[boneStack.uuid] ?: return listOf()).getRotationAtTime(time)
////            matrix.rotateAround(Quaternionf().rotateXYZ(
////                (-rotation.x + masterPart.rotation.x).toFloat() * 0.0174532f,
////                (-rotation.y + masterPart.rotation.y).toFloat() * 0.0174532f,
////                (-rotation.z + masterPart.rotation.z).toFloat() * 0.0174532f
////            ), masterPart.origin.x.toFloat() * 0.125f, masterPart.origin.y.toFloat() * 0.125f, masterPart.origin.z.toFloat() * 0.125f)
//
//            // add origin to matrix so rotation is correct
//            val scl = 0.125f
//            matrix.translate(boneStack.origin.x.toFloat() * scl, boneStack.origin.y.toFloat() * scl, boneStack.origin.z.toFloat() * scl)
//
//            // get translation (multiplication cause block bench weird)
//            val totalTranslation = matrix.getTranslation(Vector3f()).mul(-1f, 1f, -1f)
//
//            // apply rotation
////            val rotation = (animation.animators[boneStack.uuid] ?: return listOf()).getRotationAtTime(time)
////            matrix.rotateX((-rotation.x /*+ boneStack.rotation.x*/).toFloat() * 0.0174532f)
////            matrix.rotateY((-rotation.y /*+ boneStack.rotation.y*/).toFloat() * 0.0174532f)
////            matrix.rotateZ((rotation.z /*+ boneStack.rotation.z*/).toFloat() * 0.0174532f)
//            val totalRotation = matrix.getEulerAnglesXYZ(Vector3f())
//
//            // remove origin to make matrix correct again
//            matrix.translate(-boneStack.origin.x.toFloat() * scl, -boneStack.origin.y.toFloat() * scl, -boneStack.origin.z.toFloat() * scl)
//
//            output.add(create(element, totalTranslation, totalRotation))
//            matrix
//        })
//
//        // loop through children, if bone run this function on it, otherwise, render its json with given translation
//        boneStack.children.forEach {
//            if (it is BoneAnimatedComponent)
//                output.addAll(renderBoneStackRecursive(it, animation, matrix.clone() as Matrix4f, time, create))
//            else if (it is ElementAnimatedComponent) {}
//            else
//                throw IllegalArgumentException("???????????")
//        }
//        return output
//    }

    // pre render animation stuffs
//    data class PreRenderAnimation(val uuid: UUID, val name: String, val loop: Boolean, val length: Double, val animators: HashMap<UUID, PreRenderAnimator>)
//    data class PreRenderAnimator(val uuid: UUID, val positionKeyFrames: List<PreRenderKeyFrame>, val rotationKeyFrames: List<PreRenderKeyFrame>, val scaleKeyFrames: List<PreRenderKeyFrame>) {
//        fun getPositionAtTime(time: Double): Vector {
//            // try to get index of the last frame with a timestamp before the given timestamp, return if nothing found
//            val firstFrameIdx = positionKeyFrames.indexOfLast { time < it.time }
//            if (firstFrameIdx == -1) return Vector(0.0, 0.0, 0.0)
//
//            // get first frame, return its position if it's the last frame
//            val firstFrame = positionKeyFrames[firstFrameIdx]
//            if (firstFrameIdx >= positionKeyFrames.size - 1) return firstFrame.vec
//
//            // get second frame
//            val secondFrame = positionKeyFrames[firstFrameIdx + 1]
//
//            // interpolate and return result
//            val percent = (time - firstFrame.time) / (secondFrame.time - firstFrame.time)
//            val direction = secondFrame.vec.clone().subtract(firstFrame.vec).multiply(percent)
//            return firstFrame.vec.clone().add(direction)
//        }
//
//        fun getRotationAtTime(time: Double): Vector {
//            // try to get index of the last frame with a timestamp before the given timestamp, return if nothing found
//            val firstFrameIdx = rotationKeyFrames.indexOfLast { time < it.time }
//            if (firstFrameIdx == -1) return Vector(0.0, 0.0, 0.0)
//
//            // get first frame, return its position if it's the last frame
//            val firstFrame = rotationKeyFrames[firstFrameIdx]
//            if (firstFrameIdx >= rotationKeyFrames.size - 1) return firstFrame.vec
//
//            // get second frame
//            val secondFrame = rotationKeyFrames[firstFrameIdx + 1]
//
//            // interpolate and return result
//            val percent = (time - firstFrame.time) / (secondFrame.time - firstFrame.time)
//            val direction = secondFrame.vec.clone().subtract(firstFrame.vec).multiply(percent)
//            return firstFrame.vec.clone().add(direction)
//        }
//    }
//    data class PreRenderKeyFrame(val time: Double, val vec: Vector, val isLinear: Boolean)
//    private fun convertToPreRenderAnimation(json: JSONObject): PreRenderAnimation {
//        // generate animators hash map
//        val animators = hashMapOf<UUID, PreRenderAnimator>()
//        val animatorsJson = json.getJSONObject("animators")
//        animatorsJson.keys().forEach { key ->
//            val uuid = UUID.fromString(key)
//            animators[uuid] = convertToPreRenderAnimator(uuid, animatorsJson.getJSONObject(key))
//        }
//
//        // save pre render animation
//        return PreRenderAnimation(
//            UUID.fromString(json.getString("uuid")),
//            json.getString("name"),
//            json.getString("loop") == "loop",
//            json.getDouble("length"),
//            animators
//        )
//    }
//    private fun convertToPreRenderAnimator(uuid: UUID, json: JSONObject): PreRenderAnimator {
//        // create channel key frame lists
//        val positionKeyFrames = mutableListOf<PreRenderKeyFrame>()
//        val rotationKeyFrames = mutableListOf<PreRenderKeyFrame>()
//        val scaleKeyFrames = mutableListOf<PreRenderKeyFrame>()
//
//        // load keyframes
//        json.getJSONArray("keyframes").forEach {
//            val keyFrame = it as JSONObject
//
//            // get key frame list and add key frame
//            when(val channel = keyFrame.getString("channel")) {
//                "position" -> positionKeyFrames
//                "rotation" -> rotationKeyFrames
//                "scale" -> scaleKeyFrames
//                else -> throw IllegalArgumentException("Unknown key frame channel $channel")
//            }.add(convertToPreRenderKeyFrame(keyFrame))
//        }
//
//        // create and return animator
//        return PreRenderAnimator(uuid, positionKeyFrames, rotationKeyFrames, scaleKeyFrames)
//    }
//    private fun convertToPreRenderKeyFrame(json: JSONObject): PreRenderKeyFrame {
//        // convert data point
//        val dataPointJson = json.getJSONArray("data_points").first() as JSONObject
//        val dataPoint = Vector(
//            handlePossibleStringInDoubleConversion(dataPointJson.get("x")),
//            handlePossibleStringInDoubleConversion(dataPointJson.get("y")),
//            handlePossibleStringInDoubleConversion(dataPointJson.get("z")),
//        )
//
//        // create and return key frame
//        return PreRenderKeyFrame(
//            json.getDouble("time"),
//            dataPoint,
//            json.getString("interpolation") == "linear"
//        )
//    }

    // bone stuff
    abstract class AnimatedComponent {
        abstract fun render(resolution: JSONObject, textures: JSONObject, allocate: (json: JSONObject) -> Pair<Material, Int>): List<Pair<Material, Int>>
    }
//    class BoneAnimatedComponent(val uuid: UUID, val origin: Vector, val children: List<AnimatedComponent>): AnimatedComponent() {
//        override fun render(resolution: JSONObject, textures: JSONObject, allocate: (json: JSONObject) -> Pair<Material, Int>): List<Pair<Material, Int>> {
//            return children.map { it.render(resolution, textures, allocate) }.flatten()
//        }
//    }
//    class ElementAnimatedComponent(val uuid: UUID, val elements: List<ModelPart>): AnimatedComponent() {
//        override fun render(
//            resolution: JSONObject,
//            textures: JSONObject,
//            allocate: (json: JSONObject) -> Pair<Material, Int>
//        ): List<Pair<Material, Int>> {
//            return listOf(allocate(render(resolution, textures, Vector3f(0f, 0f, 0f), Vector3f(0f, 0f, 0f))))
//        }
//
//        fun render(
//            resolution: JSONObject,
//            textures: JSONObject,
//            position: Vector3f,
//            rotation: Vector3f // todo implement rotation
//        ): JSONObject {
//            val elementsJson = JSONArray()
//            elements.forEach { elementsJson.put(it.toJson(resolution)) }
//            return JSONObject()
//                .put("textures", textures)
//                .put(
//                    "display",
//                    JSONObject()
//                        .put(
//                            "head",
//                            JSONObject()
//                                .put("scale", JSONArray().put(1.6).put(1.6).put(1.6))
//                                .put("translation", JSONArray().put(position.x).put(position.y).put(position.z))
//                                .put("rotation", JSONArray().put(rotation.x * 57.29577f).put(rotation.y * 57.29577f).put(rotation.z * 57.29577f))
//                        )
//                )
//                .put("elements", elementsJson)
//        }
//    }
//    data class ModelPart(val from: Vector, val to: Vector, val origin: Vector, val rotation: Vector, val faces: JSONObject) {
//        constructor(json: JSONObject): this(
//            jsonArrayToVector(json.getJSONArray("from")),
//            jsonArrayToVector(json.getJSONArray("to")),
//            if (json.has("origin")) jsonArrayToVector(json.getJSONArray("origin")) else Vector(0.0, 0.0, 0.0),
//            if (json.has("rotation")) jsonArrayToVector(json.getJSONArray("rotation")) else Vector(0.0, 0.0, 0.0),
//            json.getJSONObject("faces")
//        )
//
//        fun toJson(resolution: JSONObject): JSONObject {
//            val faceOut = JSONObject()
//
//            faces.keySet().forEach { key ->
//                val json = faces.getJSONObject(key)
//                val uv = json.getJSONArray("uv").mapIndexed { idx, it ->
//                    val dimension = if (idx % 2 == 0) resolution.getInt("width") else resolution.getInt("height")
//                    (handlePossibleStringInDoubleConversion(it) / dimension) * 16.0
//                }
//                val tint = json.getInt("tint")
//                val texture = json.getInt("texture")
//                faceOut.put(
//                    key,
//                    JSONObject()
//                        .put("uv", uv)
//                        .put("tint", tint)
//                        .put("texture", texture)
//                )
//            }
//
//            val output = JSONObject()
//                .put("from", JSONArray().put(from.x).put(from.y).put(from.z))
//                .put("to", JSONArray().put(to.x).put(to.y).put(to.z))
//                .put("faces", faceOut)
//
//            arrayOf(rotation.x, rotation.y, rotation.z).forEachIndexed { index, angle ->
//                if (angle.absoluteValue < 1) return@forEachIndexed
//
//                output.put(
//                    "rotation",
//                    JSONObject()
//                        .put("origin", JSONArray().put(origin.x).put(origin.y).put(origin.z))
//                        .put("angle", angle)
//                        .put("axis", "xyz"[index])
//                )
//            }
//
//            return output
//        }
//    }
//    private fun createBoneStack(root: JSONObject, elements: HashMap<UUID, ModelPart>): BoneAnimatedComponent {
//        val origin = root.getJSONArray("origin")
//        val components = mutableListOf<AnimatedComponent>()
//        val modelParts = mutableListOf<ModelPart>()
//        val childrenJson = root.getJSONArray("children")
//
//        childrenJson.forEach {
//            if (it is String)
//                modelParts.add(elements[UUID.fromString(it)]!!)
//            else if (it is JSONObject)
//                components.add(createBoneStack(it, elements))
//            else
//                throw IllegalArgumentException("Cannot convert $it to AnimatedComponent")
//        }
//
//        if (modelParts.isNotEmpty())
//            components.add(ElementAnimatedComponent(UUID.randomUUID(), modelParts))
//
//        return BoneAnimatedComponent(
//            UUID.fromString(root.getString("uuid")),
//            Vector(origin.getDouble(0), origin.getDouble(1), origin.getDouble(2)),
//            components
//        )
//    }
//    private fun convertAnimatedModelToParts(json: JSONObject): HashMap<UUID, ModelPart> {
//        val elements = json.getJSONArray("elements")
//        val output = hashMapOf<UUID, ModelPart>()
//        elements.forEach {
//            val elementJson = it as JSONObject
//            output[UUID.fromString(elementJson.getString("uuid"))] = ModelPart(elementJson)
//        }
//        return output
//    }

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
//    private fun jsonArrayToVector(arr: JSONArray): Vector {
//        return Vector(
//            handlePossibleStringInDoubleConversion(arr.get(0)),
//            handlePossibleStringInDoubleConversion(arr.get(1)),
//            handlePossibleStringInDoubleConversion(arr.get(2)),
//        )
//    }
//    private fun jomlVectorToVector(vec: Vector3f): Vector = Vector(vec.x.toDouble(), vec.y.toDouble(), vec.z.toDouble())
//    private fun handlePossibleStringInDoubleConversion(any: Any): Double {
//        return if (any is String) any.toDoubleOrNull() ?: throw IllegalArgumentException("Could not convert $any to double")
//        else if (any is Double) any
//        else if (any is Integer) any.toDouble()
//        else if (any is BigDecimal) any.toDouble()
//        else throw IllegalArgumentException("Could not convert $any to double")
//    }
}