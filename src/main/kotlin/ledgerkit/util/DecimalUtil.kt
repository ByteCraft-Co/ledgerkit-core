package ledgerkit.util

import java.math.BigDecimal
import java.math.RoundingMode
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Helpers for safe decimal parsing and normalization.
 */
object DecimalUtil {
    private val decimalRegex = Regex("^-?\\d+(\\.\\d+)?$")

    /**
     * Parses a string into [BigDecimal], normalizing scale to 2, or returns null if invalid.
     */
    fun parseOrNull(raw: String?): BigDecimal? {
        if (raw == null) return null
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return null
        if (!decimalRegex.matches(trimmed)) return null
        return normalizeScale(BigDecimal(trimmed))
    }

    /**
     * Parses a string into [BigDecimal], normalizing scale to 2, or throws [IllegalArgumentException].
     */
    fun parseOrThrow(raw: String?): BigDecimal =
        parseOrNull(raw) ?: throw IllegalArgumentException("Invalid decimal value: '${raw ?: "null"}'")

    /**
     * Normalizes a decimal to a fixed [scale] using HALF_UP.
     */
    fun normalizeScale(value: BigDecimal, scale: Int = 2): BigDecimal =
        value.setScale(scale, RoundingMode.HALF_UP)
}

/**
 * Serializer for [BigDecimal] using plain string representation.
 */
object BigDecimalSerializer : KSerializer<BigDecimal> {
    override val descriptor = PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BigDecimal) {
        encoder.encodeString(value.toPlainString())
    }

    override fun deserialize(decoder: Decoder): BigDecimal {
        val text = decoder.decodeString()
        return DecimalUtil.parseOrThrow(text)
    }
}
