CREATE TABLE conversations (
                               id UUID PRIMARY KEY,
                               lead_id UUID NOT NULL,
                               channel VARCHAR(50) NOT NULL,
                               status VARCHAR(50) NOT NULL,
                               created_at TIMESTAMP NOT NULL,
                               updated_at TIMESTAMP NOT NULL,

                               CONSTRAINT fk_conversations_lead
                                   FOREIGN KEY (lead_id)
                                       REFERENCES leads(id)
);

CREATE TABLE messages (
                          id UUID PRIMARY KEY,
                          conversation_id UUID NOT NULL,
                          sender_type VARCHAR(50) NOT NULL,
                          content TEXT NOT NULL,
                          ai_generated BOOLEAN NOT NULL,
                          sent_at TIMESTAMP NOT NULL,

                          CONSTRAINT fk_messages_conversation
                              FOREIGN KEY (conversation_id)
                                  REFERENCES conversations(id)
);