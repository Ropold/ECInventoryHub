-- Dummy-Daten passend zu sql_schema.sql
-- Alles in einer Transaktion: schlägt ein INSERT fehl, wird nichts eingefügt
BEGIN;

-- Optional: vorhandene Dummy-Daten vorher löschen (Reihenfolge wegen Fremdschlüsseln beachten).
-- users bleiben erhalten, users.employee_id wird per ON DELETE SET NULL geleert.
-- DELETE FROM assignment_files;
-- DELETE FROM device_files;
-- DELETE FROM assignments;
-- DELETE FROM devices;
-- DELETE FROM locations;
-- DELETE FROM employees;

-- 1. EMPLOYEES
INSERT INTO employees (id, personnel_number, name, email, phone, address, department, active, notes, image_url) VALUES
    ('11111111-1111-1111-1111-111111111111', 'P-1001', 'Anna Schmidt', 'anna.schmidt@ec.de', '+49 151 1000001', 'Hauptstraße 1, 40667 Meerbusch', 'DEVELOPMENT', true,  'Team Backend', NULL),
    ('22222222-2222-2222-2222-222222222222', 'P-1002', 'Bernd Müller', 'bernd.mueller@ec.de', '+49 151 1000002', 'Ringweg 22, 45127 Essen',        'ACCOUNTING',  true,  NULL,           NULL),
    ('33333333-3333-3333-3333-333333333333', 'P-1003', 'Carla Weber',  'carla.weber@ec.de',  '+49 151 1000003', NULL,                             'HR',          false, 'Elternzeit',   NULL),
    ('44444444-4444-4444-4444-444444444444', 'P-1004', 'Dennis Koch',  'dennis.koch@ec.de',  '+49 151 1000004', 'Am Markt 5, 40667 Meerbusch',    'MARKETING',   true,  NULL,           NULL),
    ('55555555-5555-5555-5555-555555555555', 'P-1005', 'Eva Fischer',  'eva.fischer@ec.de',  '+49 151 1000005', NULL,                             'MANAGEMENT',  true,  'Geschäftsführung', NULL);

-- 2. LOCATIONS
INSERT INTO locations (id, name, address, phone, email, notes, latitude, longitude, sort_order, image_url) VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Meerbusch',   'Meerbuscher Str. 70-72, 40667 Meerbusch, Deutschland', '+49 2159 100000', 'meerbusch@ec.de', 'Zentrale',         51.26776404327255, 6.6280980981685165, 1, NULL),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Essen',       'Langenberger Str. 590, 45127 Essen, Deutschland',     '+49 201 200000',  'essen@ec.de',     NULL,               51.41352072337824, 7.0773141310505485, 2, NULL),
    ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'Home Office', NULL,                                                   NULL,              NULL,              'Remote-Kategorie', NULL,              NULL,                3, NULL);

-- 3. DEVICES (referenzieren locations)
-- status passt zu den Zuweisungen: ASSIGNED genau dann, wenn es eine offene Zuweisung gibt
INSERT INTO devices (id, type, manufacturer, model_name, serial_number, inventory_number, hostname, purchase_date, status, defective, location_id, notes) VALUES
    ('d1111111-1111-1111-1111-111111111111', 'LAPTOP',  'Lenovo',  'ThinkPad T560',    'SN-LENO-001', 'INV-0001', 'EC-NB-0001', '2016-03-15', 'ASSIGNED',  false, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Entwickler-Laptop'),
    ('d2222222-2222-2222-2222-222222222222', 'PHONE',   'Apple',   'iPhone 15',        'SN-APPL-002', 'INV-0002', NULL,         '2024-06-01', 'AVAILABLE', false, 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Displaykratzer, aber funktionsfähig'),
    ('d3333333-3333-3333-3333-333333333333', 'MONITOR', 'Samsung', 'S27 4K',           'SN-SAMS-003', 'INV-0003', NULL,         '2023-11-20', 'IN_REPAIR', true,  'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Pixelfehler gemeldet'),
    ('d4444444-4444-4444-4444-444444444444', 'LAPTOP',  'Lenovo',  'ThinkPad T540p',   'SN-LENO-004', 'INV-0004', 'EC-NB-0002', '2014-01-10', 'AVAILABLE', false, 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Zurück im Lager'),
    ('d5555555-5555-5555-5555-555555555555', 'LAPTOP',  'Microsoft', 'Surface Laptop 4', 'SN-MSFT-005', 'INV-0005', 'EC-NB-0003', '2022-09-05', 'RETIRED',   true,  'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Akku defekt, ausgemustert'),
    ('d6666666-6666-6666-6666-666666666666', 'TABLET',  'Apple',   'iPad Air 11',      'SN-APPL-006', 'INV-0006', NULL,         '2025-06-20', 'ASSIGNED',  false, 'cccccccc-cccc-cccc-cccc-cccccccccccc', NULL),
    ('d7777777-7777-7777-7777-777777777777', 'PHONE',   'Apple',   'iPhone 16',        'SN-APPL-007', 'INV-0007', NULL,         '2024-09-20', 'ASSIGNED',  false, 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', NULL);

-- 4. ASSIGNMENTS (referenzieren devices + employees)
-- Pro Gerät höchstens eine offene Zuweisung (returned_date NULL), returned_date nie vor assigned_date
INSERT INTO assignments (id, device_id, employee_id, handed_out_by, assigned_date, returned_date, condition_out, condition_in, notes, copy_handed_to_employee, copy_filed_in_personnel_file) VALUES
    -- ThinkPad T560: Anna, aktuell
    ('a1111111-1111-1111-1111-111111111111', 'd1111111-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111', '22222222-2222-2222-2222-222222222222', '2024-04-01', NULL,         'Gebrauchsspuren',           NULL,                  'Ersatz für das Surface Laptop 4', true,  true),
    -- iPhone 15: erst Bernd, dann Eva, jetzt im Lager
    ('a2222222-2222-2222-2222-222222222222', 'd2222222-2222-2222-2222-222222222222', '22222222-2222-2222-2222-222222222222', NULL,                                   '2024-06-05', '2024-09-10', 'Ohne Kratzer',        'Displaykratzer',      'Getauscht gegen iPhone 16',  true,  false),
    ('a4444444-4444-4444-4444-444444444444', 'd2222222-2222-2222-2222-222222222222', '55555555-5555-5555-5555-555555555555', '22222222-2222-2222-2222-222222222222', '2024-09-15', '2025-06-30', 'Displaykratzer',      'Displaykratzer',      'Umstieg auf iPad',            true,  true),
    -- Samsung Monitor: Carla, defekt zurück
    ('a3333333-3333-3333-3333-333333333333', 'd3333333-3333-3333-3333-333333333333', '33333333-3333-3333-3333-333333333333', '11111111-1111-1111-1111-111111111111', '2023-12-01', '2024-05-15', 'Voll funktionsfähig', 'Defekt',              'Zur Reparatur zurück',        false, false),
    -- Surface Laptop: Anna bis zur Ausmusterung
    ('a5555555-5555-5555-5555-555555555555', 'd5555555-5555-5555-5555-555555555555', '11111111-1111-1111-1111-111111111111', '22222222-2222-2222-2222-222222222222', '2022-09-10', '2024-03-28', 'Neu',                 'Akku defekt',         NULL,                          true,  true),
    -- ThinkPad T540p: Dennis als Leihgerät, zurückgegeben
    ('a6666666-6666-6666-6666-666666666666', 'd4444444-4444-4444-4444-444444444444', '44444444-4444-4444-4444-444444444444', '11111111-1111-1111-1111-111111111111', '2025-01-15', '2025-04-30', 'Gebrauchsspuren',     'Gebrauchsspuren',           'Leihgerät für Messeprojekt',  true,  true),
    -- iPad Air: Eva, aktuell, Kopie noch nicht in der Personalakte
    ('a7777777-7777-7777-7777-777777777777', 'd6666666-6666-6666-6666-666666666666', '55555555-5555-5555-5555-555555555555', '11111111-1111-1111-1111-111111111111', '2025-07-01', NULL,         'Neu',                 NULL,                  NULL,                          true,  false),
    -- iPhone 16: Bernd, aktuell, Protokoll fehlt noch komplett
    ('a8888888-8888-8888-8888-888888888888', 'd7777777-7777-7777-7777-777777777777', '22222222-2222-2222-2222-222222222222', '11111111-1111-1111-1111-111111111111', '2024-09-23', NULL,         'Neu',                 NULL,                  'Ersatz für iPhone 15',        false, false);

-- 5. DEVICE_FILES (referenzieren devices)
-- file_type ist der MIME-Type wie beim Upload (DeviceCard zeigt Dateien mit "image/..." als Bild)
INSERT INTO device_files (id, device_id, file_url, file_type, uploaded_at) VALUES
    ('f1111111-1111-1111-1111-111111111111', 'd1111111-1111-1111-1111-111111111111', 'https://res.cloudinary.com/demo/image/upload/sample.jpg',        'image/jpeg',      now()),
    ('f2222222-2222-2222-2222-222222222222', 'd1111111-1111-1111-1111-111111111111', 'https://res.cloudinary.com/demo/devices/t560-invoice.pdf',   'application/pdf', now()),
    ('f3333333-3333-3333-3333-333333333333', 'd3333333-3333-3333-3333-333333333333', 'https://res.cloudinary.com/demo/devices/monitor-warranty.pdf',   'application/pdf', now());

-- 6. ASSIGNMENT_FILES (referenzieren assignments)
INSERT INTO assignment_files (id, assignment_id, file_url, file_type, uploaded_at) VALUES
    ('e1111111-1111-1111-1111-111111111111', 'a1111111-1111-1111-1111-111111111111', 'https://res.cloudinary.com/demo/assignments/protokoll-001.pdf',  'application/pdf', now()),
    ('e2222222-2222-2222-2222-222222222222', 'a2222222-2222-2222-2222-222222222222', 'https://res.cloudinary.com/demo/image/upload/sample.jpg',        'image/jpeg',      now()),
    ('e3333333-3333-3333-3333-333333333333', 'a3333333-3333-3333-3333-333333333333', 'https://res.cloudinary.com/demo/assignments/ruecknahme-003.pdf', 'application/pdf', now());

COMMIT;
