-- V2: Add missing columns to claim_attachments for AttachmentService compatibility
-- Adds: attachment_type, description columns

ALTER TABLE claim_attachments ADD COLUMN IF NOT EXISTS attachment_type VARCHAR(30);
ALTER TABLE claim_attachments ADD COLUMN IF NOT EXISTS description TEXT;
