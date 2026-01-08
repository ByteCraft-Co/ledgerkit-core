# Storage

LedgerKit defines storage interfaces for categories, budgets, and transactions. The project ships only interfaces and an in-memory reference store; no database implementations are included.

## LedgerStore API (suspend)
- Categories: upsert, delete, get, list
- Budgets: upsert, delete, get, list (optional month filter)
- Transactions: upsert with sync status, delete, get, query, get/set sync status

All methods return `ledgerkit.util.Result` and are `suspend`, so call them within coroutines.

## InMemoryLedgerStore
Useful for tests and local runs:
```kotlin
import kotlinx.coroutines.runBlocking
import ledgerkit.storage.InMemoryLedgerStore
import ledgerkit.util.getOrThrow

runBlocking {
    val store = InMemoryLedgerStore()
    val categories = store.listCategories().getOrThrow()
    println(categories)
}
```

## QuerySpec filters
Inclusive `from`/`to` dates, type set, category IDs, tagsAny (normalized), textContains (case-insensitive), limit (1..10_000). Limit applies after sorting (by date then id).

```kotlin
import java.time.LocalDate
import ledgerkit.model.TransactionType
import ledgerkit.storage.QuerySpec

val spec = QuerySpec(
    from = LocalDate.of(2024, 1, 1),
    to = LocalDate.of(2024, 1, 31),
    types = setOf(TransactionType.EXPENSE),
    tagsAny = setOf("coffee"),
    textContains = "uber",
    limit = 10
)
```

Notes:
- No database wiring is provided; implement `LedgerStore` for your persistence layer.
- Sync status enum covers LOCAL_ONLY, SYNCED, DIRTY, CONFLICT.
