@file:UseSerializers(ledgerkit.util.LocalDateSerializer::class)

package ledgerkit.model

import java.math.BigDecimal
import java.time.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import ledgerkit.util.CategoryId
import ledgerkit.util.TransactionId

/**
 * A recorded financial event with positive [amount] interpreted by [type].
 */
@Serializable
data class Transaction(
    val id: TransactionId,
    val date: LocalDate,
    val type: TransactionType,
    val amount: Money,
    val description: String,
    val categoryId: CategoryId?,
    val tags: Tags = emptySet(),
    val recurrence: Recurrence = Recurrence.None
) {
    init {
        val trimmed = description.trim()
        require(trimmed.isNotEmpty()) { "Description cannot be blank" }
        require(trimmed.length <= 120) { "Description must be at most 120 characters" }
        require(trimmed == description) { "Description cannot have leading/trailing whitespace" }
        require(amount.amount > BigDecimal.ZERO) { "Transaction amount must be positive" }
    }

    fun signedAmount(): Money = when (type) {
        TransactionType.EXPENSE -> -amount
        TransactionType.INCOME, TransactionType.TRANSFER -> amount
    }

    /**
     * Case-insensitive substring match against [description].
     */
    fun matchesText(query: String): Boolean {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return false
        return description.contains(trimmed, ignoreCase = true)
    }
}
