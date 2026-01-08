# Category

Categories classify transactions and can be nested via an optional `parentId`. LedgerKit ships predefined categories for quick starts and validation baked into the model.

## Creating categories
```kotlin
import ledgerkit.model.Category
import ledgerkit.util.CategoryId

val food = Category(CategoryId("food"), "Food")
val transport = Category(CategoryId("transport"), "Transport")
val bills = Category(CategoryId("bills"), "Bills")
```

Validation:
- Name must be non-blank and ≤40 characters
- Optional `colorHex` must match `#RRGGBB`
- Optional `parentId` must be a valid `CategoryId`

Predefined set:
```kotlin
val defaults = Category.predefined()
// Food, Transport, Bills, Shopping, Health, Salary
```

## Usage with transactions
```kotlin
import java.time.LocalDate
import ledgerkit.model.*
import ledgerkit.util.TransactionId

val tx = Transaction(
    id = TransactionId("groceries-1"),
    date = LocalDate.now(),
    type = TransactionType.EXPENSE,
    amount = Money.of("30.00", CurrencyCode.USD),
    description = "Groceries",
    categoryId = Category.Food.id,
    tags = normalizeTags(listOf("grocery"))
)
```
