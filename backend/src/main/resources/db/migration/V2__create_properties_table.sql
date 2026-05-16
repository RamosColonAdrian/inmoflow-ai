CREATE TABLE properties (
                            id UUID PRIMARY KEY,
                            agency_id UUID NOT NULL,
                            reference VARCHAR(80) NOT NULL,
                            title VARCHAR(180) NOT NULL,
                            description TEXT,
                            price NUMERIC(12, 2),
                            city VARCHAR(120),
                            zone VARCHAR(120),
                            address VARCHAR(255),
                            rooms INTEGER,
                            bathrooms INTEGER,
                            square_meters INTEGER,
                            property_type VARCHAR(50) NOT NULL,
                            operation_type VARCHAR(50) NOT NULL,
                            available BOOLEAN NOT NULL,
                            created_at TIMESTAMP NOT NULL,
                            updated_at TIMESTAMP NOT NULL,

                            CONSTRAINT fk_properties_agency
                                FOREIGN KEY (agency_id)
                                    REFERENCES agencies(id),

                            CONSTRAINT uk_properties_agency_reference
                                UNIQUE (agency_id, reference)
);