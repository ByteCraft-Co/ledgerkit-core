# Money

LedgerKit models money with `java.math.BigDecimal` and enforces currency safety via `CurrencyCode`. Amounts are normalized to scale 2 with HALF_UP rounding.

## Creating money
```kotlin
import java.math.BigDecimal
import ledgerkit.model.CurrencyCode
import ledgerkit.model.Money

val usd = CurrencyCode.USD
val a = Money.of("12.34", usd)
val b = Money.of(BigDecimal("1.50"), usd)
```

## Arithmetic
```kotlin
val sum = a + b
val diff = a - b
val neg = -a
val scaled = a * 2
```

All operations require matching currencies and throw `IllegalArgumentException` on mismatch.

## Comparison and helpers
```kotlin
if (a > b) println("a is larger")
println("abs: ${a.abs()}")
println("zero: ${Money.zero(usd)}")
```

## Serialization
`Money` is `@Serializable` with fields:
```json
{ "amount": "12.34", "currency": "USD" }
```
