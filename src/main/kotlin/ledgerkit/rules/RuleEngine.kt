package ledgerkit.rules

import ledgerkit.model.Transaction

/**
 * Runs rules sequentially.
 */
class RuleEngine(private val rules: List<Rule>) {
    fun process(transaction: Transaction): Transaction {
        var current = transaction
        for (rule in rules) {
            current = rule.apply(current)
        }
        return current
    }

    fun applyAll(txs: List<Transaction>): List<Transaction> = txs.map { process(it) }
}
