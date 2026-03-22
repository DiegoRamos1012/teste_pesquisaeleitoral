CREATE TABLE state
(
    id                UUID PRIMARY KEY,
    name              VARCHAR(250) NOT NULL,
    created_at        TIMESTAMP    NOT NULL,
    last_time_changed TIMESTAMP    NOT NULL,
    state_acronym     VARCHAR(2)   NOT NULL
);

CREATE TABLE candidate
(
    id                UUID PRIMARY KEY,
    name              VARCHAR(250) NOT NULL,
    created_at        TIMESTAMP    NOT NULL,
    last_time_changed TIMESTAMP    NOT NULL,
    political_party   VARCHAR(120) NOT NULL
);

CREATE TABLE poll
(
    id                UUID PRIMARY KEY,
    name              VARCHAR(250) NOT NULL,
    created_at        TIMESTAMP    NOT NULL,
    last_time_changed TIMESTAMP    NOT NULL,
    poll_date         DATE         NOT NULL
);

CREATE TABLE municipality
(
    id                UUID PRIMARY KEY,
    name              VARCHAR(250) NOT NULL,
    created_at        TIMESTAMP    NOT NULL,
    last_time_changed TIMESTAMP    NOT NULL,
    population        INTEGER      NOT NULL,
    state_id          UUID         NOT NULL,
    CONSTRAINT fk_municipality_state
        FOREIGN KEY (state_id)
            REFERENCES state (id)
);

CREATE TABLE poll_result
(
    id                UUID PRIMARY KEY,
    name              VARCHAR(250)  NOT NULL,
    created_at        TIMESTAMP     NOT NULL,
    last_time_changed TIMESTAMP     NOT NULL,
    poll_id           UUID          NOT NULL,
    municipality_id   UUID          NOT NULL,
    candidate_id      UUID          NOT NULL,
    percentage        NUMERIC(5, 2) NOT NULL,
    CONSTRAINT fk_poll_result_poll
        FOREIGN KEY (poll_id)
            REFERENCES poll (id),
    CONSTRAINT fk_poll_result_municipality
        FOREIGN KEY (municipality_id)
            REFERENCES municipality (id),
    CONSTRAINT fk_poll_result_candidate
        FOREIGN KEY (candidate_id)
            REFERENCES candidate (id)
);

CREATE INDEX idx_state_name ON state (name);
CREATE UNIQUE INDEX uq_state_state_acronym ON state (state_acronym);
CREATE INDEX idx_candidate_name ON candidate (name);
CREATE INDEX idx_poll_name ON poll (name);
CREATE INDEX idx_poll_date ON poll (poll_date);
CREATE INDEX idx_municipality_name_state_id ON municipality (name, state_id);
CREATE INDEX idx_poll_result_poll_id ON poll_result (poll_id);
CREATE INDEX idx_poll_result_candidate_id ON poll_result (candidate_id);
CREATE INDEX idx_poll_result_municipality_id ON poll_result (municipality_id);
