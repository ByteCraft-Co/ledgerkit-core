# Transaction

Transactions record dated events with positive amounts interpreted by `TransactionType` (EXPENSE, INCOME, TRANSFER). Amount polarity is derived from type, not sign.

## Structure
```kotlin
import java.time.LocalDate
import ledgerkit.model.*
import ledgerkit.util.TransactionId

val tx = Transaction(
    id = TransactionId("tx-123"),
    date = LocalDate.now(),
    type = TransactionType.EXPENSE,
    amount = Money.of("19.99", CurrencyCode.USD),
    description = "Lunch",
    categoryId = Category.Food.id,
    tags = normalizeTags(listOf("food", "lunch")),
    recurrence = Recurrence.None
)
```

Validation rules:
- Description must be non-blank (trimmed) and ≤120 chars
- Amount must be positive; type conveys meaning (expense vs income)
- Tags are normalized lowercase and validated

## Signed amount helper
```kotlin
val signed = tx.signedAmount() // negative for EXPENSE, positive otherwise
```

## Recurrence
`Recurrence` supports `None`, `Weekly`, `Monthly`, `Yearly` with strict bounds to avoid invalid dates. Use `nextDate(from)` to compute the next occurrence.
