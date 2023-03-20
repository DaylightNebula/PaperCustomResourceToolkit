package daylightnebula.papercustomresourcetoolkit.uis

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentBuilder
import net.kyori.adventure.text.TextComponent

object UIManager {
    fun render(stack: UIStack): TextComponent {
        val builder = Component.text()
        for (comp in stack.list) {
            comp.render(builder)
        }
        return builder.build()
    }
}
data class UIStack(val alignment: UIAlignment, val bounds: UIBounds, val list: MutableList<UIComponent>)
enum class UIAlignment { LEFT, CENTER, RIGHT }
data class UIBounds(val maxLeft: Int, val maxRight: Int)