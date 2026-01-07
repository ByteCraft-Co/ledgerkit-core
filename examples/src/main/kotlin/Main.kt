import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.runBlocking
import ledgerkit.analytics.Analytics
import ledgerkit.model.Budget
import ledgerkit.model.Category
import ledgerkit.model.CurrencyCode
import ledgerkit.model.Money
import ledgerkit.model.Transaction
import ledgerkit.model.TransactionType
import ledgerkit.rules.DefaultRules
import ledgerkit.storage.InMemoryLedgerStore
import ledgerkit.util.BudgetId
import ledgerkit.util.TransactionId
import ledgerkit.util.getOrThrow

fun main() = runBlocking {
    val store = InMemoryLedgerStore()
    val categories = Category.predefined()
    categories.forEach { store.upsertCategory(it) }

    val jan = YearMonth.of(2024, 1)
    val feb = YearMonth.of(2024, 2)

    val txs = listOf(
        Transaction(TransactionId("salary-jan"), LocalDate.of(2024, 1, 1), TransactionType.INCOME, Money.of("4000.00", CurrencyCode.USD), "Salary", Category.Salary.id, emptySet()),
        Transaction(TransactionId("rent-jan"), LocalDate.of(2024, 1, 3), TransactionType.EXPENSE, Money.of("1200.00", CurrencyCode.USD), "Rent", Category.Bills.id, emptySet()),
        Transaction(TransactionId("uber-jan"), LocalDate.of(2024, 1, 5), TransactionType.EXPENSE, Money.of("25.00", CurrencyCode.USD), "Uber trip", null, emptySet()),
        Transaction(TransactionId("coffee-jan"), LocalDate.of(2024, 1, 8), TransactionType.EXPENSE, Money.of("5.00", CurrencyCode.USD), "Coffee", Category.Food.id, setOf("coffee")),
        Transaction(TransactionId("salary-feb"), LocalDate.of(2024, 2, 1), TransactionType.INCOME, Money.of("4000.00", CurrencyCode.USD), "Salary", Category.Salary.id, emptySet()),
        Transaction(TransactionId("rent-feb"), LocalDate.of(2024, 2, 3), TransactionType.EXPENSE, Money.of("1200.00", CurrencyCode.USD), "Rent", Category.Bills.id, emptySet()),
        Transaction(TransactionId("train-feb"), LocalDate.of(2024, 2, 10), TransactionType.EXPENSE, Money.of("15.00", CurrencyCode.USD), "Train", Category.Transport.id, setOf("commute")),
    )

    val engine = DefaultRules.engine()
    txs.map(engine::process).forEach { store.upsertTransaction(it) }

    val breakdownJan = Analytics.categoryBreakdown(store.queryTransactions().getOrThrow(), jan, CurrencyCode.USD)
    println("Category breakdown for Jan:")
    breakdownJan.forEach { println(" - ${it.categoryId.value}: ${it.total.amount}") }

    val totals = Analytics.monthlyTotals(store.queryTransactions().getOrThrow(), jan..feb, CurrencyCode.USD)
    println("\nMonthly totals:")
    totals.forEach { println(" - ${it.period}: ${it.total.amount}") }

    val budget = Budget(BudgetId("food-jan"), "Food Jan", jan, Money.of("300.00", CurrencyCode.USD), setOf(Category.Food.id))
    store.upsertBudget(budget)
    val progress = Analytics.budgetProgress(listOf(budget), store.queryTransactions().getOrThrow()).single()
    println("\nBudget progress ${budget.name}: spent=${progress.spent.amount}, remaining=${progress.remaining.amount}, percent=${progress.percentUsed}")

    val sampleTx = Transaction(TransactionId("auto-cat"), LocalDate.of(2024, 1, 12), TransactionType.EXPENSE, Money.of("18.00", CurrencyCode.USD), "Uber trip", null, emptySet())
    val categorized = engine.process(sampleTx)
    println("\nRule engine categorized '${sampleTx.description}' -> ${categorized.categoryId}")

    val jsonExport = ledgerkit.LedgerKit.exportJson(store).getOrThrow()
    val jsonImport = ledgerkit.LedgerKit.importJson(jsonExport.bytes).getOrThrow()
    println("\nJSON export/import: tx=${jsonImport.transactions.size}, categories=${jsonImport.categories.size}")

    val csvExport = ledgerkit.LedgerKit.exportCsvTransactions(store.queryTransactions().getOrThrow())
    val csvImport = ledgerkit.LedgerKit.importCsv(csvExport.bytes).getOrThrow()
    println("CSV import warnings: ${csvImport.warnings.size}")
    println("CSV round-trip tx count: ${csvImport.transactions.size}")
}
