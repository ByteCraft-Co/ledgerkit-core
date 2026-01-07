package ledgerkit.analytics

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.YearMonth
import ledgerkit.model.Budget
import ledgerkit.model.Money
import ledgerkit.model.Transaction
import ledgerkit.model.TransactionType
import ledgerkit.model.CurrencyCode

/**
 * Stateless aggregation helpers for in-memory data.
 */
object Analytics {
    /**
     * Returns expense totals by category for a month. Slices are sorted by total descending, then category id.
     */
    fun categoryBreakdown(
        transactions: List<Transaction>,
        month: YearMonth,
        currency: CurrencyCode
    ): List<PieSlice> {
        val filtered = transactions.filter {
            it.currencyMatches(currency) &&
                YearMonth.from(it.date) == month &&
                it.type == TransactionType.EXPENSE &&
                it.categoryId != null
        }
        val grouped = filtered.groupBy { it.categoryId!! }
        return grouped.map { (categoryId, txs) ->
            val total = txs.fold(Money.zero(currency)) { acc, tx -> acc + tx.signedAmount().abs() }
            PieSlice(categoryId, total.abs())
        }.sortedWith(compareByDescending<PieSlice> { it.total.amount }.thenBy { it.categoryId.value })
    }

    /**
     * Net totals per month across [range], including months with zero totals.
     */
    fun monthlyTotals(
        transactions: List<Transaction>,
        range: ClosedRange<YearMonth>,
        currency: CurrencyCode
    ): List<TimeSeriesPoint> {
        val months = generateMonths(range)
        return months.map { period ->
            val total = transactions.filter { it.currencyMatches(currency) && YearMonth.from(it.date) == period }
                .fold(Money.zero(currency)) { acc, tx -> acc + tx.signedAmount() }
            TimeSeriesPoint(period, total)
        }
    }

    /**
     * Calculates spend vs limit per budget. Percent used for zero limits is 0 when spent is 0, otherwise 1.
     */
    fun budgetProgress(
        budgets: List<Budget>,
        transactions: List<Transaction>
    ): List<BudgetProgress> {
        return budgets.sortedWith(compareBy<Budget> { it.name }.thenBy { it.id.value }).map { budget ->
            val currency = budget.limit.currency
            val spent = transactions.filter {
                it.currencyMatches(currency) &&
                    it.type == TransactionType.EXPENSE &&
                    YearMonth.from(it.date) == budget.month &&
                    it.categoryId in budget.categoryIds
            }.fold(Money.zero(currency)) { acc, tx -> acc + tx.signedAmount().abs() }
            val remaining = budget.limit - spent
            val percentUsed = when {
                budget.limit.amount.compareTo(BigDecimal.ZERO) == 0 && spent.amount.compareTo(BigDecimal.ZERO) == 0 -> BigDecimal.ZERO
                budget.limit.amount.compareTo(BigDecimal.ZERO) == 0 -> BigDecimal.ONE
                else -> spent.amount.divide(budget.limit.amount, 4, RoundingMode.HALF_UP)
            }
            BudgetProgress(budget.id, spent, remaining, percentUsed)
        }
    }

    private fun generateMonths(range: ClosedRange<YearMonth>): List<YearMonth> {
        val months = mutableListOf<YearMonth>()
        var current = range.start
        while (current <= range.endInclusive) {
            months += current
            current = current.plusMonths(1)
        }
        return months
    }

    private fun Transaction.currencyMatches(currency: CurrencyCode): Boolean = this.amount.currency == currency
}
