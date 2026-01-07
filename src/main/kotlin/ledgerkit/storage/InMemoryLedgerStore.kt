package ledgerkit.storage

import java.time.YearMonth
import java.util.concurrent.ConcurrentHashMap
import ledgerkit.model.Budget
import ledgerkit.model.Category
import ledgerkit.model.Transaction
import ledgerkit.util.BudgetId
import ledgerkit.util.CategoryId
import ledgerkit.util.Result
import ledgerkit.util.TransactionId
import ledgerkit.util.err
import ledgerkit.util.ok

/**
 * Simple in-memory implementation for tests and examples.
 */
class InMemoryLedgerStore : LedgerStore {
    private val categories = ConcurrentHashMap<CategoryId, Category>()
    private val budgets = ConcurrentHashMap<BudgetId, Budget>()
    private val transactions = ConcurrentHashMap<TransactionId, Transaction>()
    private val statuses = ConcurrentHashMap<TransactionId, SyncStatus>()
    private val lock = Any()

    override suspend fun upsertCategory(category: Category): Result<Unit> =
        synchronized(lock) {
            categories[category.id] = category
            ok(Unit)
        }

    override suspend fun deleteCategory(id: CategoryId): Result<Unit> =
        synchronized(lock) {
            categories.remove(id)
            ok(Unit)
        }

    override suspend fun getCategory(id: CategoryId): Result<Category?> =
        synchronized(lock) { ok(categories[id]) }

    override suspend fun listCategories(): Result<List<Category>> =
        synchronized(lock) {
            ok(categories.values.sortedWith(compareBy<Category> { it.name }.thenBy { it.id.value }))
        }

    override suspend fun upsertBudget(budget: Budget): Result<Unit> =
        synchronized(lock) {
            budgets[budget.id] = budget
            ok(Unit)
        }

    override suspend fun deleteBudget(id: BudgetId): Result<Unit> =
        synchronized(lock) {
            budgets.remove(id)
            ok(Unit)
        }

    override suspend fun getBudget(id: BudgetId): Result<Budget?> =
        synchronized(lock) { ok(budgets[id]) }

    override suspend fun listBudgets(month: YearMonth?): Result<List<Budget>> =
        synchronized(lock) {
            val filtered = budgets.values.filter { month == null || it.month == month }
            ok(filtered.sortedWith(compareBy<Budget> { it.month }.thenBy { it.name }.thenBy { it.id.value }))
        }

    override suspend fun upsertTransaction(tx: Transaction, status: SyncStatus): Result<Unit> =
        synchronized(lock) {
            transactions[tx.id] = tx
            statuses[tx.id] = status
            ok(Unit)
        }

    override suspend fun deleteTransaction(id: TransactionId): Result<Unit> =
        synchronized(lock) {
            transactions.remove(id)
            statuses.remove(id)
            ok(Unit)
        }

    override suspend fun getTransaction(id: TransactionId): Result<Transaction?> =
        synchronized(lock) { ok(transactions[id]) }

    override suspend fun queryTransactions(spec: QuerySpec): Result<List<Transaction>> =
        synchronized(lock) {
            var result = transactions.values.filter { spec.matches(it) }
            result = result.sortedWith(compareBy<Transaction> { it.date }.thenBy { it.id.value })
            spec.limit?.let { limit -> result = result.take(limit) }
            ok(result)
        }

    override suspend fun getTransactionSyncStatus(id: TransactionId): Result<SyncStatus?> =
        synchronized(lock) { ok(statuses[id]) }

    override suspend fun setTransactionSyncStatus(id: TransactionId, status: SyncStatus): Result<Unit> =
        synchronized(lock) {
            if (!transactions.containsKey(id)) return@synchronized err("Transaction $id not found")
            statuses[id] = status
            ok(Unit)
        }
}
