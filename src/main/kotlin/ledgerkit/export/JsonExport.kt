package ledgerkit.export

import java.time.Instant
import java.time.YearMonth
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ledgerkit.model.Budget
import ledgerkit.model.Category
import ledgerkit.model.CurrencyCode
import ledgerkit.model.Transaction
import ledgerkit.storage.LedgerStore
import ledgerkit.util.Result
import ledgerkit.util.ok
import ledgerkit.util.CategoryId

/**
 * JSON export utilities.
 */
/**
 * Serializable ledger snapshot for backup/restore.
 */
@Serializable
data class LedgerSnapshot(
    val exportedAt: String,
    val currency: CurrencyCode? = null,
    val categories: List<Category>,
    val budgets: List<Budget>,
    val transactions: List<Transaction>
)

object JsonExport {
    private val json = Json { prettyPrint = true; encodeDefaults = true }

    /** Exports a provided snapshot to JSON bytes. */
    fun exportSnapshot(snapshot: LedgerSnapshot, pretty: Boolean = true): ExportResult {
        val configured = if (pretty) json else Json { prettyPrint = false; encodeDefaults = true }
        val payload = configured.encodeToString(snapshot)
        return ExportResult(payload.toByteArray(Charsets.UTF_8), "application/json", "ledger-snapshot.json")
    }

    /** Exports a store to a snapshot, optionally filtered to a month. */
    suspend fun exportFromStore(store: LedgerStore, month: YearMonth? = null): Result<ExportResult> {
        val categories = store.listCategories()
        val budgets = store.listBudgets(month)
        val transactions = when (month) {
            null -> store.queryTransactions()
            else -> store.queryTransactions(
                ledgerkit.storage.QuerySpec(
                    from = month.atDay(1),
                    to = month.atDay(month.lengthOfMonth())
                )
            )
        }
        val catValue = when (categories) { is Result.Ok -> categories.value; is Result.Err -> return categories }
        val budgetValue = when (budgets) { is Result.Ok -> budgets.value; is Result.Err -> return budgets }
        val txValue = when (transactions) { is Result.Ok -> transactions.value; is Result.Err -> return transactions }

        val categorySet: Set<Category> = if (month == null) {
            catValue.toSet()
        } else {
            val referencedIds = txValue.mapNotNull { it.categoryId }.toMutableSet()
            referencedIds += budgetValue.flatMap { it.categoryIds }
            val idToCategory = catValue.associateBy { it.id }
            val expanded = mutableSetOf<CategoryId>()
            fun includeWithParents(id: CategoryId) {
                if (!expanded.add(id)) return
                idToCategory[id]?.parentId?.let { includeWithParents(it) }
            }
            referencedIds.forEach { includeWithParents(it) }
            catValue.filter { it.id in expanded }.toSet()
        }

        val snapshot = LedgerSnapshot(
            exportedAt = Instant.now().toString(),
            currency = txValue.firstOrNull()?.amount?.currency,
            categories = categorySet.sortedWith(compareBy<Category> { it.name }.thenBy { it.id.value }),
            budgets = budgetValue.sortedWith(compareBy<Budget> { it.month }.thenBy { it.name }.thenBy { it.id.value }),
            transactions = txValue.sortedWith(compareBy<Transaction> { it.date }.thenBy { it.id.value })
        )
        return ok(exportSnapshot(snapshot))
    }
}
