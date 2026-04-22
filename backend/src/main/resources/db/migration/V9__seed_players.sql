DECLARE @now DATETIME2 = SYSDATETIME();

INSERT INTO players (username, created_at, updated_at) VALUES
(N'godmod',    @now, @now),
(N'elchico',   @now, @now),
(N'coelhinha', @now, @now);