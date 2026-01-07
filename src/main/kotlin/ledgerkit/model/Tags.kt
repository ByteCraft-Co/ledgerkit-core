package ledgerkit.model

/**
 * Normalized set of lowercase tags.
 */
typealias Tags = Set<String>

private val tagRegex = Regex("^[a-z0-9_-]{1,24}$")

/**
 * Trims, lowercases, validates, and de-duplicates tags.
 *
 * @throws IllegalArgumentException if any tag is invalid.
 */
fun normalizeTags(input: Iterable<String>): Tags {
    val normalized = input.map { it.trim().lowercase() }
        .filter { it.isNotEmpty() }
    normalized.forEach {
        require(tagRegex.matches(it)) { "Invalid tag '$it'" }
    }
    return normalized.toSet()
}
