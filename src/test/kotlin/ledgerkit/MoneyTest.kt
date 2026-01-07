package ledgerkit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import ledgerkit.model.CurrencyCode
import ledgerkit.model.Money
import java.math.BigDecimal

class MoneyTest {
    @Test
    fun roundsInputStrings() {
        val money = Money.of("10.005", CurrencyCode.USD)
        assertEquals(BigDecimal("10.01"), money.amount)
    }

    @Test
    fun normalizesScaleToTwo() {
        val money = Money.of("1", CurrencyCode.EUR)
        assertEquals(BigDecimal("1.00"), money.amount)
    }

    @Test
    fun plusRequiresSameCurrency() {
        val usd = Money.of("5.00", CurrencyCode.USD)
        val eur = Money.of("5.00", CurrencyCode.EUR)
        assertFailsWith<IllegalArgumentException> { usd + eur }
    }

    @Test
    fun operatorsWork() {
        val a = Money.of("3.00", CurrencyCode.GBP)
        val b = Money.of("1.50", CurrencyCode.GBP)
        assertEquals(BigDecimal("4.50"), (a + b).amount)
        assertEquals(BigDecimal("1.50"), (a - b).amount)
        assertEquals(BigDecimal("-3.00"), (-a).amount)
    }

    @Test
    fun multiplicationAndDivisionKeepScale() {
        val amount = Money.of("2.50", CurrencyCode.GBP)
        assertEquals(BigDecimal("5.00"), (amount * 2).amount)
        assertEquals(BigDecimal("7.50"), (amount * BigDecimal("3")).amount)
        assertEquals(BigDecimal("1.25"), (amount / 2).amount)
        assertFailsWith<IllegalArgumentException> { amount / 0 }
    }
}
