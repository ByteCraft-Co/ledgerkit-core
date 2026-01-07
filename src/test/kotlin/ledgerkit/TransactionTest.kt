package ledgerkit

import kotlin.test.Test
import kotlin.test.assertFailsWith
import java.time.LocalDate
import ledgerkit.model.CurrencyCode
import ledgerkit.model.Money
import ledgerkit.model.Transaction
import ledgerkit.model.TransactionType
import ledgerkit.util.TransactionId

class TransactionTest {
    @Test
    fun rejectsBlankDescription() {
        assertFailsWith<IllegalArgumentException> {
            Transaction(
                id = TransactionId("t-blank"),
                date = LocalDate.now(),
                type = TransactionType.EXPENSE,
                amount = Money.of("1.00", CurrencyCode.USD),
                description = "   ",
                categoryId = null
            )
        }
    }
}
