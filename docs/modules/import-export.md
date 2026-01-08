# Import/Export

LedgerKit supports JSON snapshots and CSV transaction round-trips. Both return `Result` with warnings instead of throwing on partial failures.

## JSON snapshots
```kotlin
import java.time.YearMonth
import kotlinx.coroutines.runBlocking
import ledgerkit.LedgerKit
import ledgerkit.storage.InMemoryLedgerStore
import ledgerkit.util.getOrThrow

fun main() = runBlocking {
    val store = InMemoryLedgerStore()
    // populate store...
    val export = LedgerKit.exportJson(store, month = YearMonth.of(2024, 1)).getOrThrow()
    val imported = LedgerKit.importJson(export.bytes).getOrThrow()
    println("Imported tx=${imported.transactions.size}, categories=${imported.categories.size}")
}
```

## CSV transactions
Header: `id,date,type,amount,currency,description,categoryId,tags,recurrence`

Recurrence formats: `NONE`, `WEEKLY:1..7`, `MONTHLY:1..28`, `YEARLY:MM-DD`

```kotlin
import kotlinx.coroutines.runBlocking
import ledgerkit.LedgerKit
import ledgerkit.storage.InMemoryLedgerStore
import ledgerkit.util.getOrThrow

fun main() = runBlocking {
    val store = InMemoryLedgerStore()
    val csvExport = LedgerKit.exportCsvTransactions(store.queryTransactions().getOrThrow())
    val csvImport = LedgerKit.importCsv(csvExport.bytes).getOrThrow()
    println("Warnings: ${csvImport.warnings}")
}
```

Notes:
- Quotes and commas are handled; quotes are escaped by doubling.
- Tags are semicolon-separated and normalized.
- Malformed rows are skipped with warnings; malformed recurrence defaults to `None` with a warning.
