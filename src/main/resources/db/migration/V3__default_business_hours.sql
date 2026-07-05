create unique index if not exists business_hours_day_of_week_key on business_hours(day_of_week);

insert into business_hours (id, day_of_week, open_time, close_time, is_closed)
values
    ('30000000-0000-0000-0000-000000000001', 1, '09:00', '17:00', false),
    ('30000000-0000-0000-0000-000000000002', 2, '09:00', '17:00', false),
    ('30000000-0000-0000-0000-000000000003', 3, '09:00', '17:00', false),
    ('30000000-0000-0000-0000-000000000004', 4, '09:00', '17:00', false),
    ('30000000-0000-0000-0000-000000000005', 5, '09:00', '17:00', false),
    ('30000000-0000-0000-0000-000000000006', 6, '09:00', '14:00', false),
    ('30000000-0000-0000-0000-000000000007', 7, null, null, true)
on conflict (day_of_week) do update
set open_time = excluded.open_time,
    close_time = excluded.close_time,
    is_closed = excluded.is_closed;
