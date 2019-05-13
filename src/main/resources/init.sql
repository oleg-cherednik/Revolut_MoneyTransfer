create table accounts
(
    account_id  char(36) not null primary key,
    holder_name varchar(50),
    cents       int8     not null default 0,
    version     int8     not null default 0
);

create table transactions
(
    transaction_id  int8        not null primary key,
    src_account_id  char(36)    not null check (src_account_id != dest_account_id),
    dest_account_id char(36)    not null check (dest_account_id != src_account_id),
    cents           int4        not null check (cents > 0),
    status          varchar(15) not null,
    error_reason    varchar(255),
    version         int8        not null default 0,
    foreign key (src_account_id) references accounts (account_id),
    foreign key (dest_account_id) references accounts (account_id)
);

create sequence transaction_id;
