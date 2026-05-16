CREATE TABLE agencies (
                          id UUID PRIMARY KEY,
                          name VARCHAR(150) NOT NULL,
                          email VARCHAR(180),
                          phone VARCHAR(30),
                          website VARCHAR(255),
                          created_at TIMESTAMP NOT NULL,
                          updated_at TIMESTAMP NOT NULL
);