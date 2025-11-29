-- Add customization fields to the clients table
ALTER TABLE clients ADD COLUMN widget_color VARCHAR(255) DEFAULT '#007aff';
ALTER TABLE clients ADD COLUMN chatbot_name VARCHAR(255) DEFAULT 'AI Assistant';
ALTER TABLE clients ADD COLUMN welcome_message TEXT;

-- Set a default welcome message for existing clients
UPDATE clients SET welcome_message = 'Hi! How can I help you today?' WHERE welcome_message IS NULL;
