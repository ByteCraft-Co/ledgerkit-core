package ledgerkit.rules

import ledgerkit.model.Transaction
import ledgerkit.util.RuleId

/**
 * Transformation applied to a transaction.
 */
interface Rule {
    val id: RuleId
    val name: String
    fun apply(tx: Transaction): Transaction
}
