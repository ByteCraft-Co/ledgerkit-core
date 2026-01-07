package ledgerkit.rules

import ledgerkit.model.Transaction
import ledgerkit.util.RuleId

/**
 * Placeholder validation hook for extensibility.
 */
class ValidationRule : Rule {
    override val id: RuleId = RuleId("validation")
    override val name: String = "ValidationRule"

    override fun apply(tx: Transaction): Transaction {
        require(tx.description.isNotBlank()) { "Transaction description cannot be blank" }
        require(tx.amount.amount > java.math.BigDecimal.ZERO) { "Transaction amount must be positive" }
        return tx
    }
}
