# Tags & IDs

LedgerKit validates both tags and identifiers to keep data consistent.

## Tags
- Lowercase, trimmed
- Allowed characters: `[a-z0-9_-]`
- Max length: 24
- Normalized via `normalizeTags(...)`

Example:
```kotlin
import ledgerkit.model.normalizeTags

val tags = normalizeTags(listOf("Coffee", " morning "))
println(tags) // ["coffee", "morning"]
```

## IDs
Value classes defined in `ledgerkit.util`:
- `TransactionId`
- `CategoryId`
- `BudgetId`
- `RuleId`

Rules:
- Non-blank, trimmed
- Max length: 64
- Allowed characters: `[A-Za-z0-9_-]`

Example:
```kotlin
import ledgerkit.util.TransactionId

val id = TransactionId("tx-123")
```
