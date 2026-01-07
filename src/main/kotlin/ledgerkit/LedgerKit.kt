package ledgerkit

import java.time.YearMonth
import ledgerkit.analytics.Analytics
import ledgerkit.analytics.PieSlice
import ledgerkit.analytics.TimeSeriesPoint
import ledgerkit.export.CsvExport
import ledgerkit.export.ExportResult
import ledgerkit.export.JsonExport
import ledgerkit.import.CsvImport
import ledgerkit.import.ImportResult
import ledgerkit.import.JsonImport
import ledgerkit.model.Transaction
import ledgerkit.model.CurrencyCode
import ledgerkit.rules.Rule
import ledgerkit.rules.RuleEngine
import ledgerkit.storage.LedgerStore
import ledgerkit.util.Result

/**
 * Small facade exposing common LedgerKit operations.
 */
object LedgerKit {
    const val VERSION: String = "0.1.0"

    /** Applies rules sequentially to a transaction. */
    fun applyRules(tx: Transaction, rules: List<Rule>): Transaction = RuleEngine(rules).process(tx)

    /** Expense breakdown for a month. */
    fun breakdownForMonth(
        transactions: List<Transaction>,
        month: YearMonth,
        currency: CurrencyCode
    ): List<PieSlice> = Analytics.categoryBreakdown(transactions, month, currency)

    /** Net totals across a month range. */
    fun totalsForRange(
        transactions: List<Transaction>,
        range: ClosedRange<YearMonth>,
        currency: CurrencyCode
    ): List<TimeSeriesPoint> = Analytics.monthlyTotals(transactions, range, currency)

    /** Exports a JSON snapshot from a store. */
    suspend fun exportJson(store: LedgerStore, month: YearMonth? = null): Result<ExportResult> =
        JsonExport.exportFromStore(store, month)

    /** Exports transactions to CSV. */
    fun exportCsvTransactions(transactions: List<Transaction>): ExportResult =
        CsvExport.exportTransactions(transactions)

    /** Imports a snapshot from JSON bytes. */
    fun importJson(bytes: ByteArray): Result<ImportResult> = JsonImport.parseSnapshot(bytes)

    /** Imports transactions from CSV bytes. */
    fun importCsv(bytes: ByteArray): Result<ImportResult> = CsvImport.parseTransactions(bytes)
}
