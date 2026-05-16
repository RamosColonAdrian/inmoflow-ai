CREATE TABLE leads (
                       id UUID PRIMARY KEY,
                       agency_id UUID NOT NULL,
                       property_id UUID,
                       name VARCHAR(150),
                       email VARCHAR(180),
                       phone VARCHAR(30),
                       source VARCHAR(50) NOT NULL,
                       status VARCHAR(50) NOT NULL,
                       message TEXT,
                       budget NUMERIC(12, 2),
                       desired_zone VARCHAR(120),
                       score INTEGER,
                       created_at TIMESTAMP NOT NULL,
                       updated_at TIMESTAMP NOT NULL,

                       CONSTRAINT fk_leads_agency
                           FOREIGN KEY (agency_id)
                               REFERENCES agencies(id),

                       CONSTRAINT fk_leads_property
                           FOREIGN KEY (property_id)
                               REFERENCES properties(id)
);