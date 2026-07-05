alter table appointments
    drop constraint appointments_no_overlap;

alter table appointments
    add constraint appointments_no_overlap
    exclude using gist (
        tstzrange(scheduled_start, scheduled_end, '[)') with &&
    )
    where (status = 'CONFIRMED');
