MERGE INTO products (code, name, base_price) KEY(code) VALUES ('CE', 'Cheese', 5.95);
MERGE INTO products (code, name, base_price) KEY(code) VALUES ('HM', 'Ham', 7.95);
MERGE INTO products (code, name, base_price) KEY(code) VALUES ('SS', 'Soy Sauce', 11.95);


-- Insert Packaging Options for Cheese (CE)
MERGE INTO packaging_options (id, product_code, bundle_size, bundle_price) KEY(id) VALUES (1, 'CE', 3, 14.95);
MERGE INTO packaging_options (id, product_code, bundle_size, bundle_price) KEY(id) VALUES (2, 'CE', 5, 20.95);

-- Insert Packaging Options for Ham (HM)
MERGE INTO packaging_options (id, product_code, bundle_size, bundle_price) KEY(id) VALUES (3, 'HM', 2, 13.95);
MERGE INTO packaging_options (id, product_code, bundle_size, bundle_price) KEY(id) VALUES (4, 'HM', 5, 29.95);
MERGE INTO packaging_options (id, product_code, bundle_size, bundle_price) KEY(id) VALUES (5, 'HM', 8, 40.95);

-- Reset the identity sequence to start after the max ID
ALTER TABLE packaging_options ALTER COLUMN id RESTART WITH (SELECT COALESCE(MAX(id), 0) + 1 FROM packaging_options);
