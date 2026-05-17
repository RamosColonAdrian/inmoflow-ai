CREATE TABLE appointments (
                              id UUID PRIMARY KEY,
                              agency_id UUID NOT NULL,
                              lead_id UUID NOT NULL,
                              property_id UUID,
                              conversation_id UUID,
                              requested_date_text VARCHAR(255),
                              status VARCHAR(50) NOT NULL,
                              notes TEXT,
                              created_at TIMESTAMP NOT NULL,
                              updated_at TIMESTAMP NOT NULL,

                              CONSTRAINT fk_appointments_agency
                                  FOREIGN KEY (agency_id)
                                      REFERENCES agencies(id),

                              CONSTRAINT fk_appointments_lead
                                  FOREIGN KEY (lead_id)
                                      REFERENCES leads(id),

                              CONSTRAINT fk_appointments_property
                                  FOREIGN KEY (property_id)
                                      REFERENCES properties(id),

                              CONSTRAINT fk_appointments_conversation
                                  FOREIGN KEY (conversation_id)
                                      REFERENCES conversations(id)
);
