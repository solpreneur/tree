CREATE TABLE edge (
  from_id INTEGER NOT NULL,
  to_id   INTEGER NOT NULL,

  CONSTRAINT edge_pkey         PRIMARY KEY (from_id, to_id),
  CONSTRAINT edge_to_id_key    UNIQUE (to_id),
  CONSTRAINT edge_no_self_loop CHECK (from_id <> to_id)
);