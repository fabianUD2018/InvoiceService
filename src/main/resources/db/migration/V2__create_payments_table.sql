-- In scenarios where a payment can wait for confirmation from a third party, pending status may be useful
create type payment_status as ENUM ('PENDING', 'PAID', 'FAILED');

create type payment_provider as ENUM ('STRIPE', 'PAYPAL', 'PAY_U');

-- payment may have much more fields, but i am sticking to the minimum
create table if not exists payment
(
    id UUID primary key,
    invoice_id UUID references invoice (id),
    created_date timestamp,
    paid_date timestamp,
    payment_provider payment_provider,
    payment_status payment_status,
    amount numeric
);