@file:UseSerializers(ledgerkit.util.YearMonthSerializer::class)

package ledgerkit.model

import java.time.YearMonth
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import ledgerkit.util.BudgetId
import ledgerkit.util.CategoryId

/**
 * Spending plan for specific [categoryIds] within a [month].
 */
@Serializable
data class Budget(
    val id: BudgetId,
    val name: String,
    val month: YearMonth,
    val limit: Money,
    val categoryIds: Set<CategoryId>
) {
    init {
        require(name.isNotBlank()) { "Budget name cannot be blank" }
        require(name.length <= 40) { "Budget name must be at most 40 characters" }
        require(limit.amount >= java.math.BigDecimal.ZERO) { "Budget limit must be non-negative" }
        require(categoryIds.isNotEmpty()) { "Budget must target at least one category" }
    }
}
