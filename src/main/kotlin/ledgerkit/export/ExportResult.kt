package ledgerkit.export

/**
 * Result of an export operation.
 */
data class ExportResult(
    val bytes: ByteArray,
    val mimeType: String,
    val fileName: String
)
