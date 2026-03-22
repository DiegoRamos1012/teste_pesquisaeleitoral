INSERT INTO candidate (id, name, created_at, last_time_changed, political_party)
VALUES ('11111111-1111-1111-1111-111111111111', 'Maria Silva', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Partido A')
ON CONFLICT (id) DO NOTHING;

INSERT INTO candidate (id, name, created_at, last_time_changed, political_party)
VALUES ('22222222-2222-2222-2222-222222222222', 'Joao Souza', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Partido B')
ON CONFLICT (id) DO NOTHING;

INSERT INTO candidate (id, name, created_at, last_time_changed, political_party)
VALUES ('33333333-3333-3333-3333-333333333333', 'Ana Costa', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Partido C')
ON CONFLICT (id) DO NOTHING;

INSERT INTO candidate (id, name, created_at, last_time_changed, political_party)
VALUES ('44444444-4444-4444-4444-444444444444', 'Carlos Pereira', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Partido D')
ON CONFLICT (id) DO NOTHING;

INSERT INTO candidate (id, name, created_at, last_time_changed, political_party)
VALUES ('55555555-5555-5555-5555-555555555555', 'Fernanda Oliveira', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Partido E')
ON CONFLICT (id) DO NOTHING;

