# Rules

LedgerKit ships a lightweight rules engine to transform transactions.

## Rule interface
```kotlin
import ledgerkit.model.Transaction
import ledgerkit.rules.Rule
import ledgerkit.util.RuleId

class MyRule : Rule {
    override val id = RuleId("my-rule")
    override val name = "MyRule"
    override fun apply(tx: Transaction): Transaction = tx // add logic here
}
```

Rules apply in order; each returns a transaction that feeds the next rule.

## RuleEngine
```kotlin
import ledgerkit.rules.RuleEngine

val engine = RuleEngine(listOf(MyRule()))
val processed = engine.process(transaction)
val batch = engine.applyAll(transactions)
```

## DefaultRules
Prebuilt patterns for common merchants (case-insensitive):
- Uber/Careem -> Transport
- Starbucks/Cafe -> Food
- Netflix/Spotify -> Shopping
- Rent/Electric/Water -> Bills
- Pharmacy/Clinic -> Health
- Salary/Payroll -> Salary

```kotlin
import ledgerkit.rules.DefaultRules

val engine = DefaultRules.engine()
val categorized = engine.process(transaction)
```

Notes:
- Existing `categoryId` is preserved; auto-categorization only fills nulls.
- ValidationRule runs to enforce basic constraints; add your own rules to extend behavior.
