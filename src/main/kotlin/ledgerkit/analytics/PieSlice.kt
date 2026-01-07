package ledgerkit.analytics

import kotlinx.serialization.Serializable
import ledgerkit.model.Money
import ledgerkit.util.CategoryId

/**
 * Pie chart slice for category totals.
 */
@Serializable
data class PieSlice(
    val categoryId: CategoryId,
    val total: Money
)
