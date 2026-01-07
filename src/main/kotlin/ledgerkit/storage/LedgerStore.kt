package ledgerkit.storage

import java.time.YearMonth
import ledgerkit.model.Budget
import ledgerkit.model.Category
import ledgerkit.model.Transaction
import ledgerkit.util.BudgetId
import ledgerkit.util.CategoryId
import ledgerkit.util.Result
import ledgerkit.util.TransactionId

/**
 * Abstraction for persistence; implementations may be in-memory or external.
 */
interface LedgerStore {
    // Categories
    /** Inserts or updates a category. */
    suspend fun upsertCategory(category: Category): Result<Unit>
    /** Deletes a category by id. */
    suspend fun deleteCategory(id: CategoryId): Result<Unit>
    /** Fetches a category or null. */
    suspend fun getCategory(id: CategoryId): Result<Category?>
    /** Lists all categories. */
    suspend fun listCategories(): Result<List<Category>>

    // Budgets
    /** Inserts or updates a budget. */
    suspend fun upsertBudget(budget: Budget): Result<Unit>
    /** Deletes a budget by id. */
    suspend fun deleteBudget(id: BudgetId): Result<Unit>
    /** Fetches a budget or null. */
    suspend fun getBudget(id: BudgetId): Result<Budget?>
    /** Lists budgets, optionally filtered by month. */
    suspend fun listBudgets(month: YearMonth? = null): Result<List<Budget>>

    // Transactions
    /** Inserts or updates a transaction and its sync status. */
    suspend fun upsertTransaction(tx: Transaction, status: SyncStatus = SyncStatus.LOCAL_ONLY): Result<Unit>
    /** Deletes a transaction by id. */
    suspend fun deleteTransaction(id: TransactionId): Result<Unit>
    /** Fetches a transaction or null. */
    suspend fun getTransaction(id: TransactionId): Result<Transaction?>
    /** Runs a filtered transaction query. */
    suspend fun queryTransactions(spec: QuerySpec = QuerySpec()): Result<List<Transaction>>
    /** Gets sync status for a transaction. */
    suspend fun getTransactionSyncStatus(id: TransactionId): Result<SyncStatus?>
    /** Sets sync status for a transaction. */
    suspend fun setTransactionSyncStatus(id: TransactionId, status: SyncStatus): Result<Unit>
}
