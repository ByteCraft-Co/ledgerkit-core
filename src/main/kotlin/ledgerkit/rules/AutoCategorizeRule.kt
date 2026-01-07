package ledgerkit.rules

import ledgerkit.model.Transaction
import ledgerkit.util.CategoryId
import ledgerkit.util.RuleId

/**
 * Assigns a category based on regex matches against description.
 */
class AutoCategorizeRule(private val patterns: Map<Regex, CategoryId>) : Rule {
    override val id: RuleId = RuleId("auto-categorize")
    override val name: String = "AutoCategorizeRule"

    override fun apply(tx: Transaction): Transaction {
        if (tx.categoryId != null) return tx
        val matched = patterns.entries.firstOrNull { (regex, _) -> regex.containsMatchIn(tx.description) }
        return matched?.let { tx.copy(categoryId = it.value) } ?: tx
    }
}
