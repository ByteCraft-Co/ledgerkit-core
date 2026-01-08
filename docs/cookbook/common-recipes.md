# Common Recipes

Practical snippets for frequent tasks using LedgerKit.

## Categorize and store a transaction
```kotlin
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import ledgerkit.model.*
import ledgerkit.rules.DefaultRules
import ledgerkit.storage.InMemoryLedgerStore
import ledgerkit.util.TransactionId
import ledgerkit.util.getOrThrow

fun main() = runBlocking {
    val store = InMemoryLedgerStore()
    Category.predefined().forEach { store.upsertCategory(it) }

    val tx = Transaction(
        id = TransactionId("uber-1"),
        date = LocalDate.now(),
        type = TransactionType.EXPENSE,
        amount = Money.of("18.00", CurrencyCode.USD),
        description = "Uber trip",
        categoryId = null,
        tags = normalizeTags(listOf("transport"))
    )

    val processed = DefaultRules.engine().process(tx)
    store.upsertTransaction(processed)

    val all = store.queryTransactions().getOrThrow()
    println("Stored: ${all.size}")
}
```

## Filter transactions with QuerySpec
```kotlin
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import ledgerkit.model.TransactionType
import ledgerkit.storage.InMemoryLedgerStore
import ledgerkit.storage.QuerySpec
import ledgerkit.util.getOrThrow

fun main() = runBlocking {
    val store = InMemoryLedgerStore()
    val spec = QuerySpec(
        from = LocalDate.of(2024, 1, 1),
        to = LocalDate.of(2024, 1, 31),
        types = setOf(TransactionType.EXPENSE),
        tagsAny = setOf("coffee"),
        textContains = "coffee"
    )
    val results = store.queryTransactions(spec).getOrThrow()
    println("Matched: ${results.size}")
}
```

## Export/import snapshot
```kotlin
import kotlinx.coroutines.runBlocking
import ledgerkit.LedgerKit
import ledgerkit.storage.InMemoryLedgerStore
import ledgerkit.util.getOrThrow

fun main() = runBlocking {
    val store = InMemoryLedgerStore()
    val export = LedgerKit.exportJson(store).getOrThrow()
    val imported = LedgerKit.importJson(export.bytes).getOrThrow()
    println("Imported tx count: ${imported.transactions.size}")
}
```
