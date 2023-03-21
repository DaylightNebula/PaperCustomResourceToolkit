package daylightnebula.papercustomresourcetoolkit.uis

import daylightnebula.papercustomresourcetoolkit.packer.FontResource
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent

abstract class UIComponent {
    abstract fun getWidth(): Int
    abstract fun getExtraOffset(): Int
    abstract fun render(builder: TextComponent.Builder)
}
class TextUIComponent(val resource: FontResource, val text: String): UIComponent() {
    override fun getWidth(): Int {
        return 0 // TODO load from ascii.png
    }

    override fun getExtraOffset(): Int {
        return 0 // TODO load from ascii.png
    }

    override fun render(builder: TextComponent.Builder) {
//        builder.append(Component.text("").font(Key.key("test")))
//        builder.append(Component.text(String(text.map { it + resource.firstChar }.toCharArray())))
    }
}