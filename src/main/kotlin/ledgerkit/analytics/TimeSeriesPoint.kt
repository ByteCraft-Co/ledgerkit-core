@file:UseSerializers(ledgerkit.util.YearMonthSerializer::class)

package ledgerkit.analytics

import java.time.YearMonth
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import ledgerkit.model.Money

/**
 * Aggregated total for a month.
 */
@Serializable
data class TimeSeriesPoint(
    val period: YearMonth,
    val total: Money
)
