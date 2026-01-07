package ledgerkit.model

import java.time.LocalDate
import kotlinx.serialization.Serializable

@Serializable
sealed class Recurrence {
    /**
     * Returns the next occurrence date on or after [from], or null if none.
     */
    abstract fun nextDate(from: LocalDate): LocalDate?

    @Serializable
    data object None : Recurrence() {
        override fun nextDate(from: LocalDate): LocalDate? = null
    }

    @Serializable
    data class Weekly(val dayOfWeek: Int) : Recurrence() {
        init {
            require(dayOfWeek in 1..7) { "Weekly dayOfWeek must be 1-7" }
        }

        override fun nextDate(from: LocalDate): LocalDate {
            val currentDow = from.dayOfWeek.value
            val daysToAdd = if (currentDow <= dayOfWeek) dayOfWeek - currentDow else 7 - (currentDow - dayOfWeek)
            return from.plusDays(daysToAdd.toLong())
        }
    }

    @Serializable
    data class Monthly(val day: Int) : Recurrence() {
        init {
            require(day in 1..28) { "Monthly day must be between 1 and 28" }
        }

        override fun nextDate(from: LocalDate): LocalDate {
            val targetMonth = if (from.dayOfMonth <= day) from.withDayOfMonth(day) else from.plusMonths(1).withDayOfMonth(day)
            return targetMonth
        }
    }

    @Serializable
    data class Yearly(val month: Int, val day: Int) : Recurrence() {
        init {
            require(month in 1..12) { "Yearly month must be between 1 and 12" }
            require(day in 1..28) { "Yearly day must be between 1 and 28" }
        }

        override fun nextDate(from: LocalDate): LocalDate {
            val targetThisYear = LocalDate.of(from.year, month, day)
            return if (!from.isAfter(targetThisYear)) targetThisYear else targetThisYear.plusYears(1)
        }
    }
}
