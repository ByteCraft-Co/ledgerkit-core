package ledgerkit.rules

import ledgerkit.model.Category
import ledgerkit.util.RuleId

/**
 * Prebuilt rule set for quick starts.
 */
object DefaultRules {
    /** Regex patterns used by [AutoCategorizeRule]. */
    fun patterns(): Map<Regex, ledgerkit.util.CategoryId> {
        return linkedMapOf(
            Regex("uber|careem", RegexOption.IGNORE_CASE) to Category.Transport.id,
            Regex("starbucks|cafe", RegexOption.IGNORE_CASE) to Category.Food.id,
            Regex("netflix|spotify", RegexOption.IGNORE_CASE) to Category.Shopping.id,
            Regex("rent|electric|water", RegexOption.IGNORE_CASE) to Category.Bills.id,
            Regex("pharmacy|clinic", RegexOption.IGNORE_CASE) to Category.Health.id,
            Regex("salary|payroll", RegexOption.IGNORE_CASE) to Category.Salary.id
        )
    }

    /** Default rules list. */
    fun rules(): List<Rule> = listOf(
        AutoCategorizeRule(patterns()),
        ValidationRule()
    )

    /** Convenience factory for [RuleEngine] with defaults. */
    fun engine(): RuleEngine = RuleEngine(rules())
}
