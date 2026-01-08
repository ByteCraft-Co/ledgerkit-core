# LedgerKit Core

LedgerKit Core is a Kotlin JVM 17 finance domain framework with strongly typed models, analytics utilities, a lightweight rules engine, storage abstractions, and import/export helpers. It has no Android dependencies and ships no database implementations (storage is interface-based).

Documentation: https://bytecraft-co.github.io/ledgerkit-core/

## Features
- Models: `Money`, `Transaction`, `Category`, `Budget`, `Recurrence`, validated tags/IDs
- Analytics: category breakdowns, monthly totals, budget progress
- Rules: composable engine with default auto-categorization patterns
- Storage: interfaces plus an in-memory reference store
- Import/Export: JSON snapshots and CSV round-trips with warnings
- Serialization: kotlinx.serialization (BigDecimal, java.time)

## Installation
Use as a local Gradle module:
```kotlin
// settings.gradle.kts
include(":ledgerkit-core")

// build.gradle.kts
dependencies {
    implementation(project(":ledgerkit-core"))
}
```
Publishing to Maven/JitPack is planned; for now consume locally.

## Quickstart
```kotlin
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.runBlocking
import ledgerkit.LedgerKit
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
        id = TransactionId("tx-uber-1"),
        date = LocalDate.of(2024, 1, 5),
        type = TransactionType.EXPENSE,
        amount = Money.of("18.00", CurrencyCode.USD),
        description = "Uber trip",
        categoryId = null,
        tags = normalizeTags(listOf("transport"))
    )

    val processed = DefaultRules.engine().process(tx)
    store.upsertTransaction(processed)

    val month = YearMonth.of(2024, 1)
    val breakdown = Analytics.categoryBreakdown(
        transactions = store.queryTransactions().getOrThrow(),
        month = month,
        currency = CurrencyCode.USD
    )
    println("Breakdown: $breakdown")

    val export = LedgerKit.exportJson(store).getOrThrow()
    val imported = LedgerKit.importJson(export.bytes).getOrThrow()
    println("Imported transactions: ${imported.transactions.size}")
}
```

## Examples
Run the console example module:
```
./gradlew :examples:run
```

## Documentation
Hosted docs: https://bytecraft-co.github.io/ledgerkit-core/

Local preview of the VitePress docs:
```
cd docs
npm install
npm run docs:dev
```

## Stability and Versioning
- Current version: 0.1.0 (pre-release). APIs may change.
- Versioning policy: Semantic Versioning once the API is stable.

## Development Notes
- Kotlin JVM target 17
- kotlinx.serialization for JSON
- No Android or database dependencies included

## Documentation
- [Changelog](CHANGELOG.md)
- [License](LICENSE)
- [Security Policy](SECURITY.md)
=======
- Current version: 0.1.0 (pre-1.0); APIs may change.
- Semantic Versioning will apply after stabilization.
