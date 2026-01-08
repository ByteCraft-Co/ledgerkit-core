# Analytics

Analytics are pure, in-memory helpers for aggregating transactions.

## Category breakdown
Groups expense transactions by category for a given month, sorted by total desc:
```kotlin
import java.time.YearMonth
import ledgerkit.analytics.Analytics
import ledgerkit.model.CurrencyCode
import ledgerkit.model.Transaction

val breakdown = Analytics.categoryBreakdown(
    transactions = transactions,
    month = YearMonth.of(2024, 1),
    currency = CurrencyCode.USD
)
```

## Monthly totals
Net totals (income - expense) per month across a range, including zero months:
```kotlin
val totals = Analytics.monthlyTotals(
    transactions = transactions,
    range = YearMonth.of(2024, 1)..YearMonth.of(2024, 3),
    currency = CurrencyCode.USD
)
```

## Budget progress
Spend vs limit for budgets in their month:
```kotlin
val progress = Analytics.budgetProgress(
    budgets = budgets,
    transactions = transactions
)
```

Notes:
- Transactions with different currencies are ignored.
- Expense totals are positive in breakdown; budget percentUsed treats zero limits as 0 if unspent, otherwise 1.
