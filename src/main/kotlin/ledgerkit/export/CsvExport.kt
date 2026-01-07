package ledgerkit.export

import java.lang.StringBuilder
import ledgerkit.model.Recurrence
import ledgerkit.model.Transaction

/**
 * CSV export for transactions.
 */
object CsvExport {
    private val header = listOf("id", "date", "type", "amount", "currency", "description", "categoryId", "tags", "recurrence")

    /** Exports transactions to CSV with a header row. */
    fun exportTransactions(transactions: List<Transaction>): ExportResult {
        val builder = StringBuilder()
        builder.append(header.joinToString(",")).append('\n')
        for (tx in transactions) {
            val row = listOf(
                tx.id.value,
                tx.date.toString(),
                tx.type.name,
                tx.amount.amount.toPlainString(),
                tx.amount.currency.value,
                tx.description,
                tx.categoryId?.value.orEmpty(),
                tx.tags.joinToString(";"),
                recurrenceToString(tx.recurrence)
            ).map(::escape)
            builder.append(row.joinToString(",")).append('\n')
        }
        val bytes = builder.toString().toByteArray(Charsets.UTF_8)
        return ExportResult(bytes, "text/csv", "transactions.csv")
    }

    private fun escape(value: String): String {
        if (value.contains('"') || value.contains(',') || value.contains('\n')) {
            return "\"${value.replace("\"", "\"\"")}\""
        }
        return value
    }

    private fun recurrenceToString(recurrence: Recurrence): String = when (recurrence) {
        Recurrence.None -> "NONE"
        is Recurrence.Weekly -> "WEEKLY:${recurrence.dayOfWeek}"
        is Recurrence.Monthly -> "MONTHLY:${recurrence.day}"
        is Recurrence.Yearly -> "YEARLY:%02d-%02d".format(recurrence.month, recurrence.day)
    }
}
