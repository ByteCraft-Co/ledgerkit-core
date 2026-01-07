package ledgerkit.util

import kotlinx.serialization.Serializable

private val allowedIdRegex = Regex("^[A-Za-z0-9_-]{1,64}$")

private fun validateId(value: String): String {
    val trimmed = value.trim()
    require(trimmed.isNotEmpty()) { "Id cannot be blank" }
    require(trimmed.length <= 64) { "Id must be 64 characters or fewer" }
    require(allowedIdRegex.matches(trimmed)) { "Id must match [A-Za-z0-9_-]" }
    return trimmed
}

/** Identifier for transactions. */
@JvmInline
@Serializable
value class TransactionId(val value: String) {
    init {
        val trimmed = validateId(value)
        require(trimmed == value) { "TransactionId must be trimmed" }
    }

    override fun toString(): String = value
}

/** Identifier for categories. */
@JvmInline
@Serializable
value class CategoryId(val value: String) {
    init {
        val trimmed = validateId(value)
        require(trimmed == value) { "CategoryId must be trimmed" }
    }

    override fun toString(): String = value
}

/** Identifier for budgets. */
@JvmInline
@Serializable
value class BudgetId(val value: String) {
    init {
        val trimmed = validateId(value)
        require(trimmed == value) { "BudgetId must be trimmed" }
    }

    override fun toString(): String = value
}

/** Identifier for rules. */
@JvmInline
@Serializable
value class RuleId(val value: String) {
    init {
        val trimmed = validateId(value)
        require(trimmed == value) { "RuleId must be trimmed" }
    }

    override fun toString(): String = value
}

/**
 * Helpers for constructing IDs without extra imports.
 */
object Ids {
    fun from(value: String): String = validateId(value)
    fun transaction(value: String): TransactionId = TransactionId(value)
    fun category(value: String): CategoryId = CategoryId(value)
    fun budget(value: String): BudgetId = BudgetId(value)
    fun rule(value: String): RuleId = RuleId(value)
}
