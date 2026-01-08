# Import & Export

LedgerKit provides JSON snapshots and CSV transaction round-trips. Both are tolerant to partial failures and return warnings instead of throwing.

## JSON snapshots
```kotlin
import java.time.YearMonth
import kotlinx.coroutines.runBlocking
import ledgerkit.LedgerKit
import ledgerkit.storage.InMemoryLedgerStore
import ledgerkit.util.getOrThrow

fun main() = runBlocking {
    val store = InMemoryLedgerStore()
    // ... populate store ...
    val export = LedgerKit.exportJson(store, month = YearMonth.of(2024, 1)).getOrThrow()

    val imported = LedgerKit.importJson(export.bytes).getOrThrow()
    println("Imported: tx=${imported.transactions.size}, categories=${imported.categories.size}")
}
```

## CSV transactions
CSV columns: `id,date,type,amount,currency,description,categoryId,tags,recurrence`

```kotlin
import kotlinx.coroutines.runBlocking
import ledgerkit.LedgerKit
import ledgerkit.storage.InMemoryLedgerStore
import ledgerkit.util.getOrThrow

fun main() = runBlocking {
    val store = InMemoryLedgerStore()
    // ... populate store ...
    val csvExport = LedgerKit.exportCsvTransactions(store.queryTransactions().getOrThrow())
    val csvImport = LedgerKit.importCsv(csvExport.bytes).getOrThrow()
    println("Warnings: ${csvImport.warnings}")
}
```

CSV rules:
- Quotes and commas are handled; quotes are escaped by doubling.
- Tags are semicolon-separated and validated.
- Recurrence formats: `NONE`, `WEEKLY:1..7`, `MONTHLY:1..28`, `YEARLY:MM-DD`.
- Invalid rows are skipped with warnings; malformed recurrence defaults to `None` with a warning.
