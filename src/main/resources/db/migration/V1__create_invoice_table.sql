
create table if not exists invoice (
                                       id UUID  primary key,
                                       created_date timestamp default now(),
    closed_date timestamp
    );

create table if not exists line_item (
    id UUID primary key,
    description varchar(50),
    price decimal,
    created_date timestamp default now(),
    invoice_id UUID references invoice (id)
    );

