package ledgerkit.util

/**
 * Minimal result type to avoid exceptions for domain operations.
 */
sealed class Result<out T> {
    data class Ok<T>(val value: T) : Result<T>()
    data class Err(val message: String, val cause: Throwable? = null) : Result<Nothing>()
}

/** Builds a successful [Result]. */
fun <T> ok(value: T): Result<T> = Result.Ok(value)

/** Builds a failed [Result]. */
fun err(message: String, cause: Throwable? = null): Result<Nothing> = Result.Err(message, cause)

/** Maps a successful value, propagating errors unchanged. */
inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> = when (this) {
    is Result.Ok -> ok(transform(value))
    is Result.Err -> this
}

/** Returns the value or throws [IllegalStateException] with the original message. */
fun <T> Result<T>.getOrThrow(): T = when (this) {
    is Result.Ok -> value
    is Result.Err -> throw IllegalStateException(message, cause)
}

/** Invokes [block] when ok, then returns this. */
inline fun <T> Result<T>.onOk(block: (T) -> Unit): Result<T> = also {
    if (this is Result.Ok) block(value)
}

/** Invokes [block] when error, then returns this. */
inline fun <T> Result<T>.onErr(block: (String, Throwable?) -> Unit): Result<T> = also {
    if (this is Result.Err) block(message, cause)
}
