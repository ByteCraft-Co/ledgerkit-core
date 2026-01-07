package ledgerkit.import

import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import ledgerkit.export.LedgerSnapshot
import ledgerkit.util.Result
import ledgerkit.util.err
import ledgerkit.util.ok

/**
 * JSON import utilities.
 */
object JsonImport {
    private val json = Json { ignoreUnknownKeys = true }

    /** Parses a snapshot JSON payload. */
    fun parseSnapshot(bytes: ByteArray): Result<ImportResult> {
        val payload = bytes.toString(Charsets.UTF_8)
        val snapshot = try {
            json.decodeFromString<LedgerSnapshot>(payload)
        } catch (e: SerializationException) {
            return err("Invalid JSON: ${e.message}", e)
        }

        val warnings = mutableListOf<String>()
        val dedupedCategories = dedupe(snapshot.categories.map { it.id.value } to snapshot.categories, "category", warnings)
        val dedupedBudgets = dedupe(snapshot.budgets.map { it.id.value } to snapshot.budgets, "budget", warnings)
        val dedupedTransactions = dedupe(snapshot.transactions.map { it.id.value } to snapshot.transactions, "transaction", warnings)

        val categoryIds = dedupedCategories.second.map { it.id }.toSet()
        dedupedTransactions.second.forEach { tx ->
            tx.categoryId?.let {
                if (categoryIds.isNotEmpty() && it !in categoryIds) {
                    warnings += "Transaction ${tx.id.value} references missing category ${it.value}"
                }
            }
        }

        return ok(
            ImportResult(
                transactions = dedupedTransactions.second,
                categories = dedupedCategories.second,
                budgets = dedupedBudgets.second,
                warnings = warnings
            )
        )
    }

    private fun <T> dedupe(keysAndItems: Pair<List<String>, List<T>>, label: String, warnings: MutableList<String>): Pair<Set<String>, List<T>> {
        val seen = mutableSetOf<String>()
        val result = mutableListOf<T>()
        for ((key, item) in keysAndItems.first.zip(keysAndItems.second)) {
            if (key in seen) {
                warnings += "Duplicate $label id '$key' ignored"
                continue
            }
            seen += key
            result += item
        }
        return seen to result
    }
}
