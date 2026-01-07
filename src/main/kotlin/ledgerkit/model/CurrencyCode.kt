package ledgerkit.model

import kotlinx.serialization.Serializable

/**
 * ISO-4217 style currency code (3 uppercase letters).
 */
@JvmInline
@Serializable
value class CurrencyCode(val value: String) {
    init {
        require(value.length == 3) { "Currency code must be 3 characters" }
        require(value.all { it in 'A'..'Z' }) { "Currency code must be uppercase A-Z" }
    }

    override fun toString(): String = value

    companion object {
        val USD = CurrencyCode("USD")
        val EUR = CurrencyCode("EUR")
        val GBP = CurrencyCode("GBP")
        val QAR = CurrencyCode("QAR")
    }
}
