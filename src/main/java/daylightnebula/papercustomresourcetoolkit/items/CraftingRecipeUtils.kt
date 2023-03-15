package daylightnebula.papercustomresourcetoolkit.items

import daylightnebula.papercustomresourcetoolkit.PaperCustomResourceToolkit
import daylightnebula.papercustomresourcetoolkit.isCustomItem
import net.axay.kspigot.items.meta
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.inventory.CraftingInventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe

object CraftingRecipeUtils {

    val recipes = mutableListOf<CustomRecipe>()
    private var counter = 0

    fun shapedRecipe(
        result: ItemStack,
        topRow: String,
        midRow: String,
        botRow: String,
        vararg elements: RecipeElement
    ) {
        // if any elements are custom, add to custom list
        if (elements.any { it.item.isCustomItem() }) {
            recipes.add(CustomShapedRecipe(result, topRow, midRow, botRow, elements))
        }
        // otherwise, simply register the recipe to bukkit
        else {
            val recipe = ShapedRecipe(NamespacedKey(PaperCustomResourceToolkit.plugin, "${counter++}"), result)
            recipe.shape(topRow, midRow, botRow)
            elements.forEach { recipe.setIngredient(it.char, it.item) }
            Bukkit.getScheduler().runTask(PaperCustomResourceToolkit.plugin, Runnable { Bukkit.addRecipe(recipe) })
        }
    }

    fun shapelessRecipe(
        result: ItemStack,
        vararg items: ItemStack
    ) {
        // if any elements are custom, add to custom list
        if (items.any { it.isCustomItem() }) {
            recipes.add(CustomShapelessRecipe(result, items))
        }
        // otherwise, simply register the recipe to bukkit
        else {
            val recipe = ShapelessRecipe(NamespacedKey(PaperCustomResourceToolkit.plugin, "$result"), result)
            items.forEach { recipe.addIngredient(it) }
            Bukkit.addRecipe(recipe)
        }
    }
}
data class RecipeElement(val char: Char, val item: ItemStack)
abstract class CustomRecipe(val result: ItemStack) {
    abstract fun isRecipeValid(inv: CraftingInventory): Boolean
}
class CustomShapedRecipe(result: ItemStack, val topRow: String, val midRow: String, val botRow: String, val elements: Array<out RecipeElement>): CustomRecipe(result) {
    override fun isRecipeValid(inv: CraftingInventory): Boolean {
        return checkRow(inv, topRow, 0, elements)
                && checkRow(inv, midRow, 3, elements)
                && checkRow(inv, botRow, 6, elements)
    }

    private fun checkRow(inv: CraftingInventory, str: String, matOffset: Int, elements: Array<out RecipeElement>): Boolean {
        str.forEachIndexed { idx, char ->
            if (char == ' ') return@forEachIndexed
            if (!elements.any {
                    val item = inv.matrix[idx + matOffset]
                    if (item != null) {
                        it.char == char &&
                        item.type == it.item.type &&
                        item.itemMeta.customModelData == it.item.itemMeta.customModelData
                    } else false
            }) return false
        }
        return true
    }
}
class CustomShapelessRecipe(result: ItemStack, private val items: Array<out ItemStack>): CustomRecipe(result) {
    override fun isRecipeValid(inv: CraftingInventory): Boolean {
        val list = items.toMutableList()

        // loop through whole matrix
        inv.matrix.forEach {
            // if no item here, skip to next iteration
            val item = it ?: return@forEach

            // try to get a list item that is equivalent to the given inventory, return false as the recipe fails
            val listItem = list.firstOrNull { it.type == item.type && it.itemMeta.customModelData == item.itemMeta.customModelData } ?: return false

            // if list item amount is greater than 1, simply remove one, otherwise just remove the item from the list
            if (listItem.amount > 1) listItem.amount--
            else list.remove(listItem)
        }

        // if the list is now empty, everything has passed
        return list.isEmpty()
    }
}