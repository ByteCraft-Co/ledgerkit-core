package ledgerkit.model

import kotlinx.serialization.Serializable
import ledgerkit.util.CategoryId

/**
 * Classification for transactions, optionally nested via [parentId].
 */
@Serializable
data class Category(
    val id: CategoryId,
    val name: String,
    val colorHex: String? = null,
    val parentId: CategoryId? = null
) {
    init {
        require(name.isNotBlank()) { "Category name cannot be blank" }
        require(name.length <= 40) { "Category name must be at most 40 characters" }
        colorHex?.let {
            require(HEX_REGEX.matches(it)) { "Color must match #RRGGBB" }
        }
    }

    companion object {
        private val HEX_REGEX = Regex("^#[0-9A-Fa-f]{6}$")

        val Food = Category(CategoryId("food"), "Food")
        val Transport = Category(CategoryId("transport"), "Transport")
        val Bills = Category(CategoryId("bills"), "Bills")
        val Shopping = Category(CategoryId("shopping"), "Shopping")
        val Health = Category(CategoryId("health"), "Health")
        val Salary = Category(CategoryId("salary"), "Salary")

        fun predefined(): List<Category> = listOf(Food, Transport, Bills, Shopping, Health, Salary)
    }
}
