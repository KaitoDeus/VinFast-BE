-- V3: Drop stale warranty_claim_id column from claim_attachments
-- This column was auto-created by Hibernate ddl-auto=update from old JPA mapping.
-- The correct FK column is 'claim_id' (defined in V1 DDL).

ALTER TABLE claim_attachments DROP COLUMN IF EXISTS warranty_claim_id;
