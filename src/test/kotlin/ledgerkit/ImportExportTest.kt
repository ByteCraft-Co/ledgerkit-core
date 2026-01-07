package ledgerkit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import java.time.LocalDate
import java.time.YearMonth
import ledgerkit.export.JsonExport
import ledgerkit.import.JsonImport
import ledgerkit.export.CsvExport
import ledgerkit.import.CsvImport
import ledgerkit.model.Budget
import ledgerkit.model.Category
import ledgerkit.model.CurrencyCode
import ledgerkit.model.Money
import ledgerkit.model.Recurrence
import ledgerkit.model.Transaction
import ledgerkit.model.TransactionType
import ledgerkit.storage.InMemoryLedgerStore
import ledgerkit.util.BudgetId
import ledgerkit.util.CategoryId
import ledgerkit.util.TransactionId
import ledgerkit.util.getOrThrow
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine

class ImportExportTest {

    private fun <T> runBlockingTest(block: suspend () -> T): T {
        var outcome: kotlin.Result<T>? = null
        val continuation = object : kotlin.coroutines.Continuation<T> {
            override val context = EmptyCoroutineContext
            override fun resumeWith(result: kotlin.Result<T>) { outcome = result }
        }
        block.startCoroutine(continuation)
        return outcome!!.getOrThrow()
    }

    @Test
    fun jsonSnapshotRoundTrip() = runBlockingTest {
        val store = InMemoryLedgerStore()
        store.upsertCategory(Category(CategoryId("food"), "Food"))
        store.upsertBudget(
            Budget(
                id = BudgetId("b1"),
                name = "Jan Food",
                month = YearMonth.of(2024, 1),
                limit = Money.of("100.00", CurrencyCode.USD),
                categoryIds = setOf(CategoryId("food"))
            )
        )
        val tx = Transaction(
            id = TransactionId("t1"),
            date = LocalDate.of(2024, 1, 5),
            type = TransactionType.EXPENSE,
            amount = Money.of("12.00", CurrencyCode.USD),
            description = "Groceries",
            categoryId = CategoryId("food"),
            tags = setOf("groceries")
        )
        store.upsertTransaction(tx)

        val export = JsonExport.exportFromStore(store).getOrThrow()
        val imported = JsonImport.parseSnapshot(export.bytes).getOrThrow()
        assertEquals(1, imported.transactions.size)
        assertEquals(1, imported.categories.size)
        assertEquals("Groceries", imported.transactions.first().description)
    }

    @Test
    fun csvExportImportTransactions() {
        val txs = listOf(
            Transaction(
                id = TransactionId("t1"),
                date = LocalDate.of(2024, 1, 1),
                type = TransactionType.INCOME,
                amount = Money.of("1000.00", CurrencyCode.USD),
                description = "Salary",
                categoryId = Category.Salary.id,
                tags = setOf("work"),
                recurrence = Recurrence.Monthly(1)
            ),
            Transaction(
                id = TransactionId("t2"),
                date = LocalDate.of(2024, 1, 2),
                type = TransactionType.EXPENSE,
                amount = Money.of("15.00", CurrencyCode.USD),
                description = "Coffee, morning",
                categoryId = Category.Food.id,
                tags = setOf("coffee")
            )
        )
        val export = CsvExport.exportTransactions(txs)
        val imported = CsvImport.parseTransactions(export.bytes).getOrThrow()
        assertEquals(2, imported.transactions.size)
        assertEquals("Salary", imported.transactions.first { it.id.value == "t1" }.description)
        assertEquals(Category.Food.id, imported.transactions.first { it.id.value == "t2" }.categoryId)
    }

    @Test
    fun csvRoundTripsQuotedDescription() {
        val txs = listOf(
            Transaction(
                id = TransactionId("t3"),
                date = LocalDate.of(2024, 1, 3),
                type = TransactionType.EXPENSE,
                amount = Money.of("5.00", CurrencyCode.USD),
                description = "Sandwich, \"deluxe\"",
                categoryId = Category.Food.id,
                tags = setOf("lunch")
            )
        )
        val export = CsvExport.exportTransactions(txs)
        val imported = CsvImport.parseTransactions(export.bytes).getOrThrow()
        assertEquals("Sandwich, \"deluxe\"", imported.transactions.single().description)
    }

    @Test
    fun csvMalformedRecurrenceWarnsButImportsRow() {
        val csv = """
            id,date,type,amount,currency,description,categoryId,tags,recurrence
            t1,2024-01-01,EXPENSE,5.00,USD,Test,food,tag,BAD
        """.trimIndent().toByteArray()
        val result = CsvImport.parseTransactions(csv).getOrThrow()
        assertEquals(1, result.transactions.size)
        assertTrue(result.warnings.isNotEmpty())
    }

    @Test
    fun csvImportWithBrokenRowAddsWarning() {
        val csv = """
            id,date,type,amount,currency,description,categoryId,tags,recurrence
            t1,2024-01-01,INCOME,1000.00,USD,Salary,salary,work,MONTHLY:1
            t2,2024-01-02,EXPENSE,not-a-number,USD,Coffee,food,coffee,NONE
            t3,2024-01-03,EXPENSE,5.00,USD,Snack,food,snack,NONE
        """.trimIndent().toByteArray()

        val result = CsvImport.parseTransactions(csv).getOrThrow()
        assertEquals(2, result.transactions.size)
        assertTrue(result.warnings.isNotEmpty())
    }
}
