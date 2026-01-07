package ledgerkit

import kotlin.test.Test
import kotlin.test.assertEquals
import ledgerkit.model.Transaction
import ledgerkit.model.TransactionType
import ledgerkit.model.Money
import ledgerkit.model.CurrencyCode
import ledgerkit.rules.DefaultRules
import ledgerkit.util.TransactionId
import java.time.LocalDate

class RuleEngineTest {
    @Test
    fun autoCategorizesTransport() {
        val tx = Transaction(
            id = TransactionId("t1"),
            date = LocalDate.of(2024, 1, 1),
            type = TransactionType.EXPENSE,
            amount = Money.of("15.00", CurrencyCode.USD),
            description = "Uber trip",
            categoryId = null,
            tags = emptySet()
        )
        val processed = DefaultRules.engine().process(tx)
        assertEquals(ledgerkit.model.Category.Transport.id, processed.categoryId)
    }

    @Test
    fun defaultRulesAreCaseInsensitive() {
        val tx = Transaction(
            id = TransactionId("t2"),
            date = LocalDate.of(2024, 1, 1),
            type = TransactionType.EXPENSE,
            amount = Money.of("12.00", CurrencyCode.USD),
            description = "NETFLIX subscription",
            categoryId = null,
            tags = emptySet()
        )
        val processed = DefaultRules.engine().process(tx)
        assertEquals(ledgerkit.model.Category.Shopping.id, processed.categoryId)
    }
}
