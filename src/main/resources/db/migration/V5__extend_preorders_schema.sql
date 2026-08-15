-- =====================================================
-- V5: Extend Pre-orders Table for Auto-Provisioning
-- =====================================================

ALTER TABLE pre_orders ADD COLUMN IF NOT EXISTS preorder_code VARCHAR(50);
ALTER TABLE pre_orders ADD COLUMN IF NOT EXISTS deposit_amount DECIMAL(12, 2) DEFAULT 2000000.00;
ALTER TABLE pre_orders ADD COLUMN IF NOT EXISTS account_created BOOLEAN DEFAULT FALSE;
ALTER TABLE pre_orders ADD COLUMN IF NOT EXISTS user_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_pre_orders_user'
    ) THEN
        ALTER TABLE pre_orders 
        ADD CONSTRAINT fk_pre_orders_user 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;
    END IF;
END $$;
