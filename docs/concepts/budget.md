# Budget

Budgets cap spending for selected categories within a specific month. They validate name, non-negative limits, and at least one category target.

## Structure
```kotlin
import java.time.YearMonth
import ledgerkit.model.Budget
import ledgerkit.model.Category
import ledgerkit.model.CurrencyCode
import ledgerkit.model.Money
import ledgerkit.util.BudgetId

val budget = Budget(
    id = BudgetId("food-jan"),
    name = "Food January",
    month = YearMonth.of(2024, 1),
    limit = Money.of("300.00", CurrencyCode.USD),
    categoryIds = setOf(Category.Food.id)
)
```

Validation:
- Name non-blank, ≤40 chars
- Limit must be non-negative
- At least one `categoryId`

## Budget progress
Use `Analytics.budgetProgress` to compute spend vs limit:
```kotlin
import ledgerkit.analytics.Analytics

val progress = Analytics.budgetProgress(listOf(budget), transactions)
val first = progress.first()
println("Spent: ${first.spent.amount}, Remaining: ${first.remaining.amount}, Percent: ${first.percentUsed}")
```
