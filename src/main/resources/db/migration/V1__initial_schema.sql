create table admin_users (
    id uuid primary key,
    email varchar(255) not null unique,
    password_hash varchar(255) not null,
    full_name varchar(255) not null,
    role varchar(50) not null,
    is_active boolean not null default true,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table refresh_tokens (
    id uuid primary key,
    user_id uuid not null references admin_users(id),
    token_hash varchar(255) not null,
    expires_at timestamptz not null,
    revoked_at timestamptz,
    created_at timestamptz not null default now(),
    ip_address varchar(255),
    user_agent text
);

create table business_profile (
    id uuid primary key,
    brand_name varchar(255) not null,
    tagline varchar(255),
    description text,
    phone_whatsapp varchar(50) not null,
    address_line varchar(255),
    city varchar(100) not null,
    country varchar(100) not null,
    currency_code varchar(10) not null,
    timezone varchar(100) not null,
    instagram_url varchar(500),
    facebook_url varchar(500),
    booking_enabled boolean not null default true,
    supports_home_service boolean not null default true,
    supports_studio_service boolean not null default true,
    updated_at timestamptz not null default now()
);

create table business_hours (
    id uuid primary key,
    day_of_week smallint not null,
    open_time time,
    close_time time,
    is_closed boolean not null default false
);

create table schedule_blocks (
    id uuid primary key,
    block_date date not null,
    start_time time,
    end_time time,
    reason varchar(255),
    is_full_day boolean not null default false,
    created_at timestamptz not null default now()
);

create table services (
    id uuid primary key,
    category varchar(50) not null,
    name varchar(255) not null,
    slug varchar(255) not null unique,
    description text,
    base_price numeric(10, 2) not null,
    duration_minutes integer not null,
    supports_touch_up boolean not null default false,
    touch_up_discount numeric(10, 2) not null default 0,
    is_active boolean not null default true,
    sort_order integer not null default 0,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table gallery_items (
    id uuid primary key,
    file_key varchar(500) not null,
    public_url varchar(1000) not null,
    alt_text varchar(255),
    caption varchar(500),
    sort_order integer not null default 0,
    is_active boolean not null default true,
    created_at timestamptz not null default now()
);

create table testimonials (
    id uuid primary key,
    client_name varchar(255) not null,
    text text not null,
    rating smallint,
    is_featured boolean not null default false,
    sort_order integer not null default 0,
    created_at timestamptz not null default now()
);

create table landing_content (
    id uuid primary key,
    content_key varchar(255) not null unique,
    title varchar(255),
    subtitle varchar(500),
    body text,
    json_value jsonb,
    updated_at timestamptz not null default now()
);

create table clients (
    id uuid primary key,
    full_name varchar(255) not null,
    phone varchar(50),
    whatsapp varchar(50),
    notes text,
    last_visit_at timestamptz,
    total_appointments integer not null default 0,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table appointments (
    id uuid primary key,
    client_id uuid not null references clients(id),
    status varchar(50) not null,
    appointment_mode varchar(50) not null,
    scheduled_start timestamptz not null,
    scheduled_end timestamptz not null,
    address_snapshot varchar(255),
    notes text,
    subtotal_amount numeric(10, 2) not null default 0,
    travel_fee numeric(10, 2) not null default 0,
    total_amount numeric(10, 2) not null default 0,
    completed_at timestamptz,
    cancelled_at timestamptz,
    cancel_reason varchar(255),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table appointment_items (
    id uuid primary key,
    appointment_id uuid not null references appointments(id) on delete cascade,
    service_id uuid not null references services(id),
    service_name_snapshot varchar(255) not null,
    unit_price_snapshot numeric(10, 2) not null,
    duration_snapshot_minutes integer not null,
    is_touch_up boolean not null default false,
    discount_amount numeric(10, 2) not null default 0,
    final_price numeric(10, 2) not null
);

create table expense_categories (
    id uuid primary key,
    name varchar(255) not null unique,
    is_active boolean not null default true,
    created_at timestamptz not null default now()
);

create table expenses (
    id uuid primary key,
    expense_category_id uuid references expense_categories(id),
    expense_date date not null,
    description varchar(255) not null,
    amount numeric(10, 2) not null,
    notes text,
    created_at timestamptz not null default now()
);

create table whatsapp_click_events (
    id uuid primary key,
    page_path varchar(500) not null,
    cta_label varchar(255) not null,
    device_type varchar(50),
    created_at timestamptz not null default now()
);

create extension if not exists btree_gist;

alter table appointments
    add constraint appointments_no_overlap
    exclude using gist (
        tstzrange(scheduled_start, scheduled_end, '[)') with &&
    )
    where (status in ('CONFIRMED', 'COMPLETED'));

insert into business_profile (
    id,
    brand_name,
    description,
    phone_whatsapp,
    address_line,
    city,
    country,
    currency_code,
    timezone,
    supports_home_service,
    supports_studio_service
) values (
    '11111111-1111-1111-1111-111111111111',
    'Jeni''s Lashes & Brows',
    'Especialista en cejas y pestanas en Camaguey.',
    '55902529',
    'Cornelio Porro #172 altos entre 5 y 6 Garrido',
    'Camaguey',
    'Cuba',
    'CUP',
    'America/Havana',
    true,
    true
);

insert into services (id, category, name, slug, description, base_price, duration_minutes, supports_touch_up, touch_up_discount, sort_order)
values
    ('20000000-0000-0000-0000-000000000001', 'BROWS', 'Diseno de cejas con cera', 'diseno-cejas-cera', 'Perfilado limpio y rapido para mantener la forma natural.', 300, 15, false, 0, 1),
    ('20000000-0000-0000-0000-000000000002', 'BROWS', 'Aplicacion de tinte henna', 'tinte-henna', 'Realce suave del color para definir mejor la mirada.', 300, 15, false, 0, 2),
    ('20000000-0000-0000-0000-000000000003', 'BROWS', 'Depilacion de bozo', 'depilacion-bozo', 'Servicio rapido para acabado pulcro en el rostro.', 150, 10, false, 0, 3),
    ('20000000-0000-0000-0000-000000000004', 'BROWS', 'Laminado de cejas', 'laminado-cejas', 'Diseno estructurado con fijacion para un look prolijo y duradero.', 1400, 60, false, 0, 4),
    ('20000000-0000-0000-0000-000000000005', 'LASHES', 'Aplicacion de Volumen 2D', 'volumen-2d', 'Volumen ligero con definicion pareja en toda la linea.', 3000, 150, true, 500, 5),
    ('20000000-0000-0000-0000-000000000006', 'LASHES', 'Aplicacion de Volumen 3D', 'volumen-3d', 'Cobertura mas densa para una mirada marcada pero elegante.', 3300, 150, true, 500, 6),
    ('20000000-0000-0000-0000-000000000007', 'LASHES', 'Aplicacion de Volumen 4D', 'volumen-4d', 'Resultado mas intenso para clientas que buscan impacto visual.', 3500, 150, true, 500, 7),
    ('20000000-0000-0000-0000-000000000008', 'LASHES', 'Aplicacion de Clasicas', 'clasicas', 'Extensiones una a una para un acabado natural y definido.', 3000, 150, true, 500, 8),
    ('20000000-0000-0000-0000-000000000009', 'LASHES', 'Lifting de pestanas', 'lifting-pestanas', 'Curvatura y levantamiento para destacar tus pestanas naturales.', 1700, 60, false, 0, 9);
