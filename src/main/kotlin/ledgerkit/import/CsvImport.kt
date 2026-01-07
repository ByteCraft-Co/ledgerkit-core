package ledgerkit.import

import java.nio.charset.StandardCharsets
import java.time.LocalDate
import ledgerkit.model.CurrencyCode
import ledgerkit.model.Money
import ledgerkit.model.Recurrence
import ledgerkit.model.Transaction
import ledgerkit.model.TransactionType
import ledgerkit.model.normalizeTags
import ledgerkit.util.CategoryId
import ledgerkit.util.TransactionId
import ledgerkit.util.Result
import ledgerkit.util.err
import ledgerkit.util.ok

/**
 * CSV importer for transactions.
 *
 * Expected header (all lower-case): `id,date,type,amount,currency,description,categoryId,tags,recurrence`.
 * Rows with missing or invalid required fields are skipped with warnings; import continues for others.
 */
object CsvImport {
    private const val HEADER = "id,date,type,amount,currency,description,categoryId,tags,recurrence"

    fun parseTransactions(bytes: ByteArray): Result<ImportResult> {
        val text = decodeText(bytes)
        val lines = splitLines(text)
        if (lines.isEmpty()) return ok(ImportResult())
        if (!lines.first().equals(HEADER, ignoreCase = true)) {
            return err("CSV header mismatch. Expected: $HEADER")
        }

        val warnings = mutableListOf<String>()
        val transactions = mutableListOf<Transaction>()

        lines.drop(1).forEachIndexed { idx, rawLine ->
            val rowIndex = idx + 2 // account for header (1-based)
            if (rawLine.isBlank()) return@forEachIndexed
            val cells = parseCsvLine(rawLine)
            if (cells.size < 9) {
                warnings += "Row $rowIndex: expected 9 columns, got ${cells.size}"
                return@forEachIndexed
            }

            val idCell = cells[0].trim()
            val dateCell = cells[1].trim()
            val typeCell = cells[2].trim()
            val amountCell = cells[3].trim()
            val currencyCell = cells[4].trim()
            val descriptionCell = cells[5]
            val categoryCell = cells[6].trim()
            val tagsCell = cells[7]
            val recurrenceCell = cells[8].trim()

            try {
                val id = TransactionId(idCell)
                val date = LocalDate.parse(dateCell)
                val type = TransactionType.valueOf(typeCell.uppercase())
                val currency = CurrencyCode(currencyCell)
                val amount = Money.of(amountCell, currency)
                val categoryId: CategoryId? =
                    if (categoryCell.isBlank()) null
                    else try { CategoryId(categoryCell) } catch (e: IllegalArgumentException) {
                        warnings.add("Row $rowIndex: invalid categoryId '$categoryCell' (ignored)")
                        null
                    }

                val tags = try {
                    normalizeTags(tagsCell.split(';').filter { it.isNotBlank() })
                } catch (e: IllegalArgumentException) {
                    warnings += "Row $rowIndex: invalid tags '${tagsCell}', dropped"
                    emptySet()
                }

                val recurrence = parseRecurrence(recurrenceCell, warnings, rowIndex)

                transactions += Transaction(
                    id = id,
                    date = date,
                    type = type,
                    amount = amount,
                    description = descriptionCell,
                    categoryId = categoryId,
                    tags = tags,
                    recurrence = recurrence
                )
            } catch (e: Exception) {
                warnings += "Row $rowIndex skipped: ${e.message ?: "invalid data"}"
            }
        }

        return ok(ImportResult(transactions = transactions, warnings = warnings))
    }

    private fun decodeText(bytes: ByteArray): String {
        var text = String(bytes, StandardCharsets.UTF_8)
        if (text.startsWith("\uFEFF")) {
            text = text.removePrefix("\uFEFF")
        }
        return text
    }

    private fun splitLines(text: String): List<String> =
        text.split(Regex("\\r?\\n")).filter { it.isNotEmpty() }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val ch = line[i]
            when {
                ch == '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                ch == ',' && !inQuotes -> {
                    result += current.toString()
                    current.clear()
                }
                else -> current.append(ch)
            }
            i++
        }
        result += current.toString()
        return result
    }

    private fun parseRecurrence(cell: String, warnings: MutableList<String>, rowIndex: Int): Recurrence {
        if (cell.isBlank() || cell.equals("NONE", ignoreCase = true)) return Recurrence.None
        val value = cell.trim()
        return when {
            value.startsWith("WEEKLY:", ignoreCase = true) ->
                value.substringAfter(':').toIntOrNull()?.let { runCatching { Recurrence.Weekly(it) }.getOrNull() }
            value.startsWith("MONTHLY:", ignoreCase = true) ->
                value.substringAfter(':').toIntOrNull()?.let { runCatching { Recurrence.Monthly(it) }.getOrNull() }
            value.startsWith("YEARLY:", ignoreCase = true) -> {
                val parts = value.substringAfter(':').split('-')
                val month = parts.getOrNull(0)?.toIntOrNull()
                val day = parts.getOrNull(1)?.toIntOrNull()
                if (month != null && day != null) runCatching { Recurrence.Yearly(month, day) }.getOrNull() else null
            }
            else -> null
        } ?: warningAndReturnNone(value, warnings, rowIndex)
    }

    private fun warningAndReturnNone(cell: String, warnings: MutableList<String>, rowIndex: Int): Recurrence {
        warnings += "Row $rowIndex: invalid recurrence '$cell', defaulted to NONE"
        return Recurrence.None
    }
}
