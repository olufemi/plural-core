-- Wallet ledger idempotency audit.
-- Run before adding a unique transaction-id key to KUL_PAYMENT_TRANS.

SELECT transaction_id, COUNT(*) duplicate_count
FROM KUL_PAYMENT_TRANS
WHERE transaction_id IS NOT NULL AND transaction_id <> ''
GROUP BY transaction_id
HAVING COUNT(*) > 1;

-- Apply after duplicate cleanup:
ALTER TABLE KUL_PAYMENT_TRANS
    ADD UNIQUE KEY ux_kul_payment_trans_transaction_id (transaction_id);
