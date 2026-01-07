# LedgerKit Core

LedgerKit Core is a Kotlin JVM finance domain framework that provides strongly typed money primitives, analytics helpers, a lightweight rules engine, storage abstractions, and import/export utilities. It targets JVM 17 with no Android or database dependencies.

## Features
- Core models: `Money`, `Transaction`, `Category`, `Budget`, `Recurrence`, and validated IDs
- Analytics: category breakdowns, monthly totals, and budget progress
- Rules: composable rules and default auto-categorization patterns
- Storage: interfaces plus an in-memory reference store for testing/examples
- Import/Export: JSON snapshots and CSV transaction round-trips with warnings
- Serialization: kotlinx.serialization for JSON, BigDecimal money, and java.time

## Installation
The project is currently consumed as a local module. In your multi-project build:
```kotlin
// settings.gradle.kts
include(":ledgerkit-core")

// build.gradle.kts
dependencies {
    implementation(project(":ledgerkit-core"))
}
```

### Publishing
Planned future support for Maven/JitPack distribution. For now, include the module locally.

## Quickstart
```kotlin
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.YearMonth
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
        description = "Coffee to go",
        categoryId = null,
        tags = normalizeTags(listOf("coffee"))
    )

    val categorized = DefaultRules.engine().process(tx)
    store.upsertTransaction(categorized)

    val breakdown = ledgerkit.analytics.Analytics.categoryBreakdown(
        transactions = store.queryTransactions().getOrThrow(),
        month = YearMonth.now(),
        currency = CurrencyCode.USD
    )
    println(breakdown)

    val jsonExport = ledgerkit.LedgerKit.exportJson(store).getOrThrow()
    val importResult = ledgerkit.LedgerKit.importJson(jsonExport.bytes).getOrThrow()
    println("Imported transactions: ${importResult.transactions.size}")
}
```

## Examples
A runnable console app lives in the `:examples` module:
```
./gradlew :examples:run
```
It demonstrates categories, transactions across months, analytics, rules, and JSON/CSV export/import.

## Stability and Versioning
- Current version: 0.1.0 (pre-release). APIs may change.
- Versioning policy: Semantic Versioning once the API is stable.

## Development Notes
- Kotlin JVM target 17
- kotlinx.serialization for JSON
- No Android or database dependencies included
