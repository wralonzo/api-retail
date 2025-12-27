INSERT INTO roles (name) VALUES ('ROLE_VENDEDOR');
INSERT INTO roles (name) VALUES ('ROLE_BODEGA');
INSERT INTO roles (name) VALUES ('ROLE_CLIENTE');

CREATE UNIQUE INDEX uk_client_email_active
ON local.clients (email)
WHERE deleted_at IS NULL;