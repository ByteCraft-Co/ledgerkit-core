# LedgerKit

LedgerKit Core is a Kotlin/JVM 17 finance domain framework with strongly typed models, analytics utilities, a lightweight rules engine, storage abstractions, and import/export helpers. It has no Android dependencies and ships no database implementations (storage is interface-based).

## Why LedgerKit
- Strong domain typing and validation for money, currency codes, IDs, and tags
- Separation of models, rules, analytics, and storage interfaces
- Designed for testing: in-memory store and warning-based import/export
- Serialization-first: kotlinx.serialization with BigDecimal and java.time support

## Key Features
- Models: `Money`, `Transaction`, `Category`, `Budget`, `Recurrence`, validated tags/IDs
- Analytics: category breakdowns, monthly totals, budget progress
- Rules: composable engine with default auto-categorization patterns
- Storage: interfaces plus an in-memory reference store
- Import/Export: JSON snapshots and CSV round-trips with warnings
- Serialization: kotlinx.serialization (BigDecimal, java.time)

## Quickstart
```kotlin
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.runBlocking
import ledgerkit.analytics.Analytics
import ledgerkit.model.*
import ledgerkit.rules.DefaultRules
import ledgerkit.storage.InMemoryLedgerStore
import ledgerkit.util.TransactionId
import ledgerkit.util.getOrThrow

fun main() = runBlocking {
    val store = InMemoryLedgerStore()
    Category.predefined().forEach { store.upsertCategory(it) }

    val tx = Transaction(
        id = TransactionId("tx-1"),
        date = LocalDate.now(),
        type = TransactionType.EXPENSE,
        amount = Money.of("12.50", CurrencyCode.USD),
        description = "Coffee",
        categoryId = null,
        tags = normalizeTags(listOf("coffee"))
    )

    val processed = DefaultRules.engine().process(tx)
    store.upsertTransaction(processed)

    val breakdown = Analytics.categoryBreakdown(
        transactions = store.queryTransactions().getOrThrow(),
        month = YearMonth.now(),
        currency = CurrencyCode.USD
    )
    println(breakdown)
}
```

## Next Steps
- Start with [Getting Started](./getting-started.md)
- Explore concepts: [Money](./concepts/money.md), [Transaction](./concepts/transaction.md)
- Follow guides: [Import & Export](./guides/import-export.md)
