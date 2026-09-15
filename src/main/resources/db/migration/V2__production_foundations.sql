create extension if not exists "btree_gist";

alter table tenants
    add column if not exists version bigint not null default 0;
alter table whatsapp_channels
    add column if not exists version bigint not null default 0;
alter table customers
    add column if not exists version bigint not null default 0;
alter table professionals
    add column if not exists version bigint not null default 0,
    add column if not exists time_zone varchar(50) not null default 'UTC';
alter table appointments
    add column if not exists version bigint not null default 0,
    add column if not exists idempotency_key varchar(100);

alter table tenants
    alter column created_at type timestamptz using created_at at time zone 'UTC',
    alter column updated_at type timestamptz using updated_at at time zone 'UTC';
alter table whatsapp_channels
    alter column created_at type timestamptz using created_at at time zone 'UTC',
    alter column updated_at type timestamptz using updated_at at time zone 'UTC';
alter table customers
    alter column created_at type timestamptz using created_at at time zone 'UTC',
    alter column updated_at type timestamptz using updated_at at time zone 'UTC';
alter table professionals
    alter column created_at type timestamptz using created_at at time zone 'UTC',
    alter column updated_at type timestamptz using updated_at at time zone 'UTC';
alter table appointments
    alter column starts_at type timestamptz using starts_at at time zone 'UTC',
    alter column ends_at type timestamptz using ends_at at time zone 'UTC',
    alter column created_at type timestamptz using created_at at time zone 'UTC',
    alter column updated_at type timestamptz using updated_at at time zone 'UTC';

alter table appointments
    add constraint ck_appointments_time_order check (ends_at > starts_at),
    add constraint ck_appointments_status check (status in ('PENDING', 'CONFIRMED', 'CANCELLED'));

alter table appointments
    add constraint ex_appointments_professional_overlap
    exclude using gist (
        professional_id with =,
        tstzrange(starts_at, ends_at, '[)') with &&
    ) where (status in ('PENDING', 'CONFIRMED'));

create unique index uk_appointments_tenant_idempotency
    on appointments (tenant_id, idempotency_key)
    where idempotency_key is not null;

create index idx_appointments_tenant_starts_at on appointments (tenant_id, starts_at);
create index idx_appointments_professional_starts_at on appointments (professional_id, starts_at);

create table professional_availability (
    id uuid primary key,
    professional_id uuid not null references professionals(id) on delete cascade,
    day_of_week varchar(9) not null,
    start_time time not null,
    end_time time not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    version bigint not null default 0,
    constraint ck_availability_day check (day_of_week in ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY')),
    constraint ck_availability_time_order check (end_time > start_time),
    constraint uk_professional_availability unique (professional_id, day_of_week, start_time, end_time)
);

insert into tenants (id, name, active)
values ('00000000-0000-0000-0000-000000000001', 'Development Tenant', true)
on conflict (id) do nothing;

insert into whatsapp_channels (id, tenant_id, phone_number, active)
values ('00000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000001', '15555550100', true)
on conflict (id) do nothing;
