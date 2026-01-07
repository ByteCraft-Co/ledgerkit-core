package ledgerkit.storage

/**
 * Basic sync state for transactions.
 * - LOCAL_ONLY: exists locally and not yet synced.
 * - SYNCED: confirmed by remote.
 * - DIRTY: pending local changes after sync.
 * - CONFLICT: requires manual resolution.
 */
enum class SyncStatus {
    LOCAL_ONLY,
    SYNCED,
    DIRTY,
    CONFLICT
}
