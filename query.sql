INSERT INTO roles (name) VALUES ('ROLE_VENDEDOR');
INSERT INTO roles (name) VALUES ('ROLE_BODEGA');
INSERT INTO roles (name) VALUES ('ROLE_CLIENTE');
INSERT INTO roles (name) VALUES ('ROLE_ADMIN');
CREATE INDEX idx_user_username_lower ON local.user (LOWER(username));

CREATE UNIQUE INDEX uk_client_email_active
ON local.clients (email)
WHERE deleted_at IS NULL;

