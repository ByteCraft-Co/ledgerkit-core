@file:UseSerializers(ledgerkit.util.BigDecimalSerializer::class)

package ledgerkit.analytics

import java.math.BigDecimal
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import ledgerkit.model.Money
import ledgerkit.util.BudgetId

/**
 * Progress for a single budget period.
 */
@Serializable
data class BudgetProgress(
    val budgetId: BudgetId,
    val spent: Money,
    val remaining: Money,
    val percentUsed: BigDecimal
)
