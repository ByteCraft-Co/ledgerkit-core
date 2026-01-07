package ledgerkit.import

import ledgerkit.model.Budget
import ledgerkit.model.Category
import ledgerkit.model.Transaction

/**
 * Data loaded from an import, plus any warnings encountered.
 */
data class ImportResult(
    val transactions: List<Transaction> = emptyList(),
    val categories: List<Category> = emptyList(),
    val budgets: List<Budget> = emptyList(),
    val warnings: List<String> = emptyList()
)
