package ledgerkit.storage

import java.time.LocalDate
import ledgerkit.model.Transaction
import ledgerkit.model.TransactionType
import ledgerkit.model.normalizeTags
import ledgerkit.util.CategoryId

/**
 * In-memory transaction filter.
 *
 * @param from inclusive start date
 * @param to inclusive end date
 * @param types optional type filter
 * @param categoryIds optional category filter
 * @param tagsAny matches if any tag intersects (normalized)
 * @param textContains case-insensitive substring on description
 * @param limit optional max results after sorting
 */
data class QuerySpec(
    val from: LocalDate? = null,
    val to: LocalDate? = null,
    val types: Set<TransactionType> = emptySet(),
    val categoryIds: Set<CategoryId> = emptySet(),
    val tagsAny: Set<String> = emptySet(),
    val textContains: String? = null,
    val limit: Int? = null
) {
    private val normalizedTagsAny: Set<String> = normalizeTags(tagsAny)

    init {
        if (from != null && to != null) {
            require(!from.isAfter(to)) { "from must be <= to" }
        }
        limit?.let {
            require(it in 1..10_000) { "limit must be between 1 and 10_000" }
        }
    }

    /**
     * Checks if a transaction satisfies the filter.
     */
    fun matches(tx: Transaction): Boolean {
        if (from != null && tx.date.isBefore(from)) return false
        if (to != null && tx.date.isAfter(to)) return false
        if (types.isNotEmpty() && tx.type !in types) return false
        if (categoryIds.isNotEmpty()) {
            val cid = tx.categoryId ?: return false
            if (cid !in categoryIds) return false
        }
        if (normalizedTagsAny.isNotEmpty()) {
            val txTagsNormalized = tx.tags.map { it.trim().lowercase() }.filter { it.isNotEmpty() }.toSet()
            if (txTagsNormalized.isEmpty()) return false
            if (txTagsNormalized.none { it in normalizedTagsAny }) return false
        }
        textContains?.trim()?.takeIf { it.isNotEmpty() }?.let { needle ->
            if (!tx.description.contains(needle, ignoreCase = true)) return false
        }
        return true
    }
}
