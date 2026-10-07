create sequence ticket_number_seq start with 1001;

create table tickets (
    id bigint generated always as identity primary key,
    ticket_number integer not null unique,
    ticket_id varchar(16) not null unique,
    title varchar(200) not null,
    description varchar(5000) not null,
    priority varchar(16) not null,
    assignee varchar(200) not null,
    category varchar(100) not null,
    resolution_notes varchar(5000),
    status varchar(16) not null,
    created_at timestamptz not null default now(),
    constraint tickets_priority_check check (priority in ('LOW', 'MEDIUM', 'HIGH')),
    constraint tickets_status_check check (status in ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'CANCELLED'))
);

create index tickets_created_at_ticket_number_idx
    on tickets (created_at desc, ticket_number desc);

create function assign_ticket_id()
returns trigger
language plpgsql
as $$
begin
    if new.ticket_number is null then
        new.ticket_number := nextval('ticket_number_seq')::integer;
    end if;
    new.ticket_id := 'TKT-' || new.ticket_number::text;
    return new;
end;
$$;

create trigger tickets_assign_ticket_id
before insert on tickets
for each row
execute function assign_ticket_id();

create table comments (
    id bigint generated always as identity primary key,
    ticket_id bigint not null references tickets (id),
    text varchar(5000) not null,
    created_at timestamptz not null default now()
);
