package ledgerkit

import kotlin.test.Test
import kotlin.test.assertEquals
import ledgerkit.model.Category
import ledgerkit.model.Transaction
import ledgerkit.model.TransactionType
import ledgerkit.model.Money
import ledgerkit.model.CurrencyCode
import ledgerkit.storage.InMemoryLedgerStore
import ledgerkit.storage.QuerySpec
import ledgerkit.storage.SyncStatus
import ledgerkit.util.CategoryId
import ledgerkit.util.TransactionId
import ledgerkit.util.getOrThrow
import java.time.LocalDate
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine

class StoreTest {
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
    fun categoriesListDeterministicOrder() = runBlockingTest {
        val store = InMemoryLedgerStore()
        store.upsertCategory(Category(CategoryId("b"), "Beverages"))
        store.upsertCategory(Category(CategoryId("a"), "Aardvark"))

        val list = store.listCategories().getOrThrow()
        assertEquals(listOf("Aardvark", "Beverages"), list.map { it.name })
    }

    @Test
    fun queryFiltersByRangeTagsTextAndLimit() = runBlockingTest {
        val store = InMemoryLedgerStore()
        val tx1 = transaction("t1", LocalDate.of(2024, 1, 5), 10.0, setOf("coffee"))
        val tx2 = transaction("t2", LocalDate.of(2024, 1, 10), 20.0, setOf("transport"))
        val tx3 = transaction("t3", LocalDate.of(2024, 2, 1), 30.0, setOf("coffee"))
        store.upsertTransaction(tx1)
        store.upsertTransaction(tx2)
        store.upsertTransaction(tx3)

        val spec = QuerySpec(
            from = LocalDate.of(2024, 1, 1),
            to = LocalDate.of(2024, 1, 31),
            tagsAny = setOf("coffee"),
            textContains = "t",
            limit = 1
        )
        val results = store.queryTransactions(spec).getOrThrow()
        assertEquals(1, results.size)
        assertEquals("t1", results.first().id.value)
    }

    @Test
    fun limitAppliesAfterSorting() = runBlockingTest {
        val store = InMemoryLedgerStore()
        store.upsertTransaction(transaction("t3", LocalDate.of(2024, 3, 1), 3.0, emptySet()))
        store.upsertTransaction(transaction("t1", LocalDate.of(2024, 1, 1), 1.0, emptySet()))
        store.upsertTransaction(transaction("t2", LocalDate.of(2024, 2, 1), 2.0, emptySet()))
        val result = store.queryTransactions(QuerySpec(limit = 2)).getOrThrow()
        assertEquals(listOf("t1", "t2"), result.map { it.id.value })
    }

    @Test
    fun tagsAnyMatchesNormalizedTags() = runBlockingTest {
        val store = InMemoryLedgerStore()
        store.upsertTransaction(transaction("t1", LocalDate.of(2024, 1, 1), 1.0, setOf("Coffee")))
        store.upsertTransaction(transaction("t2", LocalDate.of(2024, 1, 2), 1.0, setOf("food")))

        val spec = QuerySpec(tagsAny = setOf("COFFEE"))
        val result = store.queryTransactions(spec).getOrThrow()
        assertEquals(1, result.size)
        assertEquals("t1", result.first().id.value)
    }

    @Test
    fun textContainsIsCaseInsensitive() = runBlockingTest {
        val store = InMemoryLedgerStore()
        store.upsertTransaction(transaction("t1", LocalDate.of(2024, 1, 1), 1.0, emptySet(), desc = "Train Ride"))
        val spec = QuerySpec(textContains = "train")
        val result = store.queryTransactions(spec).getOrThrow()
        assertEquals(1, result.size)
    }

    @Test
    fun syncStatusRoundTrip() = runBlockingTest {
        val store = InMemoryLedgerStore()
        val tx = transaction("t1", LocalDate.of(2024, 1, 1), 15.0, emptySet())
        store.upsertTransaction(tx)
        store.setTransactionSyncStatus(tx.id, SyncStatus.SYNCED)
        val status = store.getTransactionSyncStatus(tx.id).getOrThrow()
        assertEquals(SyncStatus.SYNCED, status)
    }

    private fun transaction(id: String, date: LocalDate, amount: Double, tags: Set<String>, desc: String = "tx $id coffee"): Transaction =
        Transaction(
            id = TransactionId(id),
            date = date,
            type = TransactionType.EXPENSE,
            amount = Money.of(amount.toString(), CurrencyCode.USD),
            description = desc,
            categoryId = Category.Transport.id,
            tags = tags
        )
}
