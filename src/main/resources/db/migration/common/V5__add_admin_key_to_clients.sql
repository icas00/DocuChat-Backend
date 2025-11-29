-- Add a unique admin_key for managing each client
ALTER TABLE clients ADD COLUMN admin_key VARCHAR(255);

-- Generate a unique admin_key for any existing clients
-- This is a simple example; in a real-world scenario, you'd use a more robust random string generator.
UPDATE clients SET admin_key = 'demo-admin-key-' || id WHERE admin_key IS NULL;

-- Now, enforce the not-null and unique constraints
ALTER TABLE clients ALTER COLUMN admin_key SET NOT NULL;
ALTER TABLE clients ADD CONSTRAINT uk_admin_key UNIQUE (admin_key);
