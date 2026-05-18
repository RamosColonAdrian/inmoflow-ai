ALTER TABLE properties
    ADD COLUMN source_url VARCHAR(1000),
    ADD COLUMN qualification_rules_text TEXT;
