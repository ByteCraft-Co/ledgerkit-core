# Recurrence

Recurrence captures simple repeating patterns to support scheduled transactions.

## Variants
- `Recurrence.None` — no repetition
- `Recurrence.Weekly(dayOfWeek: Int)` — 1..7 (ISO Monday..Sunday)
- `Recurrence.Monthly(day: Int)` — 1..28 to avoid month-end edge cases
- `Recurrence.Yearly(month: Int, day: Int)` — month 1..12, day 1..28

Validation enforces these ranges strictly.

## Next date helper
```kotlin
import java.time.LocalDate
import ledgerkit.model.Recurrence

val rec = Recurrence.Monthly(15)
val next = rec.nextDate(LocalDate.of(2024, 1, 20)) // -> 2024-02-15
```
