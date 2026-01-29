CREATE UNIQUE INDEX active_payment_per_invoice_index
    ON payment (invoice_id)
    WHERE payment_status IN ('INITIATED', 'PENDING', 'PAID');

CREATE TABLE IF NOT EXISTS product (
    sku varchar(20) PRIMARY KEY,
    description VARCHAR(255)
);

insert into product (sku, description) values ('shoes', 'Some cool shoes');
insert into product (sku, description) values ('hat', 'Some cool hat');
insert into product (sku, description) values ('dress', 'Some cool dress');

ALTER TABLE line_item DROP COLUMN description;
ALTER TABLE line_item DROP COLUMN price;
ALTER TABLE line_item ADD COLUMN product_id varchar(10) REFERENCES product(sku);
ALTER TABLE line_item ADD COLUMN quantity INTEGER;
ALTER TABLE line_item ADD COLUMN unit_price DECIMAL;
ALTER TABLE line_item ADD CONSTRAINT line_item_unique_fk unique (invoice_id, product_id);