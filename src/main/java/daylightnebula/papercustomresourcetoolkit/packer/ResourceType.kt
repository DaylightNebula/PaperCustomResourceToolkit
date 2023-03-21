package daylightnebula.papercustomresourcetoolkit.packer

import org.bukkit.Material
import org.json.JSONObject
import java.util.*

sealed class ResourceType<T: Resource>(val name: String, val toJson: (resource: Resource) -> JSONObject, val fromJson: (json: JSONObject) -> T) {
    object IMAGE : ResourceType<ImageResource>("IMAGE", { res ->
        if (res is ImageResource)
            JSONObject()
                .put("name", res.name)
                .put("material", res.material.name)
                .put("customModelID", res.customModelID)
        else JSONObject()
    }, { json ->
        ImageResource(json.getString("name"), Material.valueOf(json.getString("material")), json.getInt("customModelID"))
    })
    object STATIC_MODEL : ResourceType<StaticModelResource>("STATIC_MODEL", { res ->
        if (res is StaticModelResource)
            JSONObject()
                .put("name", res.name)
                .put("id", res.id)
                .put("material", res.material.name)
                .put("customModelID", res.customModelID)
        else JSONObject()
    }, { json ->
        StaticModelResource(json.getString("name"), UUID.fromString(json.getString("id")), Material.valueOf(json.getString("material")), json.getInt("customModelID"))
    })
    object ANIMATED_MODEL : ResourceType<AnimatedModelResource>("ANIMATED_MODEL", { res ->
        if (res is AnimatedModelResource)
            JSONObject()
                .put("name", res.name)
        else JSONObject()
    }, { json ->
        AnimatedModelResource(json.getString("name"))
    })
    object TEXT_IMAGE : ResourceType<TextImageResource>("TEXT_IMAGE", { res ->
        if (res is TextImageResource)
            JSONObject()
                .put("name", res.name)
                .put("char", res.char)
        else JSONObject()
    }, { json ->
        TextImageResource(json.getString("name"), json.getString("char")[0])
    })
    object FONT : ResourceType<FontResource>("FONT", { res ->
        if (res is FontResource)
            JSONObject()
                .put("name", res.name)
                .put("id", res.id)
        else JSONObject()
    }, { json ->
        FontResource(json.getString("name"), json.getString("id"))
    })
}