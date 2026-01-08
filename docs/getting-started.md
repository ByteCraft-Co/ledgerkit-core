# Getting Started

This page shows how to set up a small LedgerKit workspace locally. LedgerKit targets Kotlin JVM 17 and relies on kotlinx.serialization; it has no Android or database dependencies.

## Prerequisites
- JDK 17+
- Gradle (wrapper or installed)
- VitePress docs optional; not required to use the library

## Add the module
If you have the repository locally, include the `ledgerkit-core` module:
```kotlin
// settings.gradle.kts
include(":ledgerkit-core")

// app/build.gradle.kts
dependencies {
    implementation(project(":ledgerkit-core"))
}
```

## Minimal usage example
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
        id = TransactionId("coffee-1"),
        date = LocalDate.now(),
        type = TransactionType.EXPENSE,
        amount = Money.of("4.50", CurrencyCode.USD),
        description = "Morning coffee",
        categoryId = null,
        tags = normalizeTags(listOf("coffee"))
    )

    val processed = DefaultRules.engine().process(tx)
    store.upsertTransaction(processed)

    val all = store.queryTransactions().getOrThrow()
    println("Stored transactions: ${all.size}")
}
```

## Running tests
From the repository root:
```
./gradlew test
```

## Running the examples
The repo includes a console example:
```
./gradlew :examples:run
```
