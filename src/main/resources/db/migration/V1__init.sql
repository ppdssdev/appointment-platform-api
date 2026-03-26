create extension if not exists "pgcrypto";

create table if not exists tenants (
                                       id uuid primary key,
                                       name varchar(150) not null,
    active boolean not null default true,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
    );

create table if not exists whatsapp_channels (
                                                 id uuid primary key,
                                                 tenant_id uuid not null,
                                                 phone_number varchar(30) not null,
    active boolean not null default true,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now(),
    constraint fk_whatsapp_channels_tenant
    foreign key (tenant_id) references tenants(id),
    constraint uk_whatsapp_channels_phone_number
    unique (phone_number)
    );

create table if not exists customers (
                                         id uuid primary key,
                                         tenant_id uuid not null,
                                         full_name varchar(150),
    phone_number varchar(30) not null,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now(),
    constraint fk_customers_tenant
    foreign key (tenant_id) references tenants(id),
    constraint uk_customers_tenant_phone
    unique (tenant_id, phone_number)
    );

create table if not exists professionals (
                                             id uuid primary key,
                                             tenant_id uuid not null,
                                             full_name varchar(150) not null,
    active boolean not null default true,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now(),
    constraint fk_professionals_tenant
    foreign key (tenant_id) references tenants(id)
    );

create table if not exists appointments (
                                            id uuid primary key,
                                            tenant_id uuid not null,
                                            customer_id uuid not null,
                                            professional_id uuid not null,
                                            starts_at timestamp not null,
                                            ends_at timestamp not null,
                                            status varchar(50) not null,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now(),
    constraint fk_appointments_tenant
    foreign key (tenant_id) references tenants(id),
    constraint fk_appointments_customer
    foreign key (customer_id) references customers(id),
    constraint fk_appointments_professional
    foreign key (professional_id) references professionals(id)
    );