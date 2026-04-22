ALTER TABLE players
    ADD is_admin BIT NOT NULL DEFAULT 0;

EXEC('UPDATE players SET is_admin = 1 WHERE username = N''godmod''');