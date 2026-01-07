package ledgerkit.model

import java.math.BigDecimal
import java.math.RoundingMode
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import ledgerkit.util.DecimalUtil

/**
 * Represents a monetary amount in a specific [currency] with scale normalized to 2 decimal places.
 *
 * Use [of] factories to ensure correct scale and validation.
 */
@Serializable(with = MoneySerializer::class)
data class Money(
    val amount: BigDecimal,
    val currency: CurrencyCode
) : Comparable<Money> {

    init {
        require(amount.scale() == 2) { "Money amount must have scale 2" }
    }

    /** Adds two amounts of the same currency. */
    operator fun plus(other: Money): Money {
        ensureSameCurrency(other)
        return Money(DecimalUtil.normalizeScale(amount + other.amount), currency)
    }

    /** Subtracts two amounts of the same currency. */
    operator fun minus(other: Money): Money {
        ensureSameCurrency(other)
        return Money(DecimalUtil.normalizeScale(amount - other.amount), currency)
    }

    operator fun unaryMinus(): Money = Money(amount.negate(), currency)

    /** Multiplies by an integer, preserving currency. */
    operator fun times(multiplier: Int): Money =
        Money(DecimalUtil.normalizeScale(amount.multiply(BigDecimal(multiplier))), currency)

    /** Multiplies by a decimal, preserving currency. */
    operator fun times(multiplier: BigDecimal): Money =
        Money(DecimalUtil.normalizeScale(amount.multiply(multiplier)), currency)

    /** Divides by an integer using HALF_UP scaling. */
    operator fun div(divisor: Int): Money {
        require(divisor != 0) { "Division by zero" }
        return Money(DecimalUtil.normalizeScale(amount.divide(BigDecimal(divisor), RoundingMode.HALF_UP)), currency)
    }

    override fun compareTo(other: Money): Int {
        ensureSameCurrency(other)
        return amount.compareTo(other.amount)
    }

    fun isZero(): Boolean = amount.compareTo(BigDecimal.ZERO) == 0

    /** Absolute value. */
    fun abs(): Money = if (amount.signum() >= 0) this else Money(amount.abs(), currency)

    private fun ensureSameCurrency(other: Money) {
        require(currency == other.currency) { "Currency mismatch: ${currency.value} vs ${other.currency.value}" }
    }

    companion object {
        /** Creates money from a string value. */
        fun of(value: String, currency: CurrencyCode): Money = of(DecimalUtil.parseOrThrow(value), currency)

        /** Creates money from a decimal value. */
        fun of(value: BigDecimal, currency: CurrencyCode): Money = Money(DecimalUtil.normalizeScale(value), currency)

        /** Zero amount in a currency. */
        fun zero(currency: CurrencyCode): Money = Money(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), currency)
    }
}

/** Serializer for [Money] using amount and currency fields. */
object MoneySerializer : KSerializer<Money> {
    override val descriptor = buildClassSerialDescriptor("Money") {
        element<String>("amount")
        element<String>("currency")
    }

    override fun serialize(encoder: Encoder, value: Money) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.amount.toPlainString())
            encodeStringElement(descriptor, 1, value.currency.value)
        }
    }

    override fun deserialize(decoder: Decoder): Money {
        var amount: String? = null
        var currency: String? = null
        decoder.decodeStructure(descriptor) {
            while (true) {
                when (decodeElementIndex(descriptor)) {
                    0 -> amount = decodeStringElement(descriptor, 0)
                    1 -> currency = decodeStringElement(descriptor, 1)
                    else -> break
                }
            }
        }
        val amt = DecimalUtil.parseOrThrow(amount ?: throw IllegalArgumentException("Missing amount"))
        val curr = currency?.let { CurrencyCode(it) } ?: throw IllegalArgumentException("Missing currency")
        return Money.of(amt, curr)
    }
}
