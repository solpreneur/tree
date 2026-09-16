CREATE TABLE author (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    biography TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE book (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    release_date DATE,
    isbn VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE book_author (
    author_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    note VARCHAR(255),
    main BOOLEAN NOT NULL DEFAULT TRUE,

    PRIMARY KEY (author_id, book_id),

    CONSTRAINT fk_book_author_author
     FOREIGN KEY (author_id)
         REFERENCES author(id)
         ON DELETE CASCADE,

    CONSTRAINT fk_book_author_book
     FOREIGN KEY (book_id)
         REFERENCES book(id)
         ON DELETE CASCADE
);