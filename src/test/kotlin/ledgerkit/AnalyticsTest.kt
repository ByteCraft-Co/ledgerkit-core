package ledgerkit

import kotlin.test.Test
import kotlin.test.assertEquals
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import ledgerkit.analytics.Analytics
import ledgerkit.model.Budget
import ledgerkit.model.Category
import ledgerkit.model.CurrencyCode
import ledgerkit.model.Money
import ledgerkit.model.Transaction
import ledgerkit.model.TransactionType
import ledgerkit.util.CategoryId
import ledgerkit.util.BudgetId
import ledgerkit.util.TransactionId

class AnalyticsTest {
    private val categories = Category.predefined().associateBy { it.id }

    @Test
    fun categoryBreakdownSumsExpenses() {
        val month = YearMonth.of(2024, 1)
        val txs = listOf(
            expense("tx1", 10.00, categories.getValue(Category.Food.id), LocalDate.of(2024, 1, 5)),
            expense("tx2", 5.50, categories.getValue(Category.Transport.id), LocalDate.of(2024, 1, 6)),
            expense("tx3", 4.50, categories.getValue(Category.Food.id), LocalDate.of(2024, 1, 20)),
            income("tx4", 100.00, LocalDate.of(2024, 1, 1))
        )

        val slices = Analytics.categoryBreakdown(txs, month, CurrencyCode.USD)
        assertEquals(2, slices.size)
        val foodTotal = slices.first { it.categoryId == Category.Food.id }.total.amount
        assertEquals(BigDecimal("14.50"), foodTotal)
    }

    @Test
    fun categoryBreakdownSortedByTotalDesc() {
        val month = YearMonth.of(2024, 1)
        val txs = listOf(
            expense("tx1", 5.00, categories.getValue(Category.Food.id), LocalDate.of(2024, 1, 5)),
            expense("tx2", 10.00, categories.getValue(Category.Transport.id), LocalDate.of(2024, 1, 6))
        )
        val slices = Analytics.categoryBreakdown(txs, month, CurrencyCode.USD)
        assertEquals(listOf(Category.Transport.id, Category.Food.id), slices.map { it.categoryId })
    }

    @Test
    fun monthlyTotalsComputeNetAcrossRange() {
        val txs = listOf(
            expense("tx1", 50.00, categories.getValue(Category.Bills.id), LocalDate.of(2024, 1, 10)),
            income("tx2", 200.00, LocalDate.of(2024, 1, 1)),
            expense("tx3", 25.00, categories.getValue(Category.Transport.id), LocalDate.of(2024, 2, 2)),
            income("tx4", 200.00, LocalDate.of(2024, 2, 1))
        )
        val points = Analytics.monthlyTotals(txs, YearMonth.of(2024, 1)..YearMonth.of(2024, 2), CurrencyCode.USD)
        assertEquals(BigDecimal("150.00"), points[0].total.amount)
        assertEquals(BigDecimal("175.00"), points[1].total.amount)
    }

    @Test
    fun monthlyTotalsIncludeZeroMonths() {
        val txs = emptyList<Transaction>()
        val points = Analytics.monthlyTotals(txs, YearMonth.of(2024, 1)..YearMonth.of(2024, 3), CurrencyCode.USD)
        assertEquals(3, points.size)
        assertEquals(BigDecimal("0.00"), points[0].total.amount)
    }

    @Test
    fun budgetProgressCalculatesSpentAndRemaining() {
        val budget = Budget(
            id = BudgetId("b1"),
            name = "Food Jan",
            month = YearMonth.of(2024, 1),
            limit = Money.of("100.00", CurrencyCode.USD),
            categoryIds = setOf(Category.Food.id)
        )
        val txs = listOf(
            expense("tx1", 30.00, categories.getValue(Category.Food.id), LocalDate.of(2024, 1, 5)),
            expense("tx2", 10.00, categories.getValue(Category.Food.id), LocalDate.of(2024, 1, 10)),
            expense("tx3", 5.00, categories.getValue(Category.Transport.id), LocalDate.of(2024, 1, 11))
        )
        val progress = Analytics.budgetProgress(listOf(budget), txs).single()
        assertEquals(BigDecimal("40.00"), progress.spent.amount)
        assertEquals(BigDecimal("60.00"), progress.remaining.amount)
        assertEquals(BigDecimal("0.4000"), progress.percentUsed)
    }

    @Test
    fun budgetProgressHandlesZeroLimit() {
        val budget = Budget(
            id = BudgetId("b-zero"),
            name = "Zero",
            month = YearMonth.of(2024, 1),
            limit = Money.of("0.00", CurrencyCode.USD),
            categoryIds = setOf(Category.Food.id)
        )
        val txs = listOf(expense("tx1", 1.00, categories.getValue(Category.Food.id), LocalDate.of(2024, 1, 2)))
        val progress = Analytics.budgetProgress(listOf(budget), txs).single()
        assertEquals(BigDecimal.ONE, progress.percentUsed)
    }

    private fun expense(id: String, amount: Double, category: Category, date: LocalDate): Transaction =
        Transaction(
            id = TransactionId(id),
            date = date,
            type = TransactionType.EXPENSE,
            amount = Money.of(amount.toString(), CurrencyCode.USD),
            description = "Expense $id",
            categoryId = category.id,
            tags = emptySet()
        )

    private fun income(id: String, amount: Double, date: LocalDate): Transaction =
        Transaction(
            id = TransactionId(id),
            date = date,
            type = TransactionType.INCOME,
            amount = Money.of(amount.toString(), CurrencyCode.USD),
            description = "Income $id",
            categoryId = Category.Salary.id,
            tags = emptySet()
        )
}
