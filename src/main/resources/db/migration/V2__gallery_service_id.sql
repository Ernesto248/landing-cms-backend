alter table gallery_items add column if not exists service_id uuid references services(id) on delete set null;
