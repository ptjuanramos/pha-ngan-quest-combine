-- Make username NOT NULL (safe to re-run)
ALTER TABLE players
    ALTER COLUMN username NVARCHAR(255) NOT NULL;

-- Add unique constraint on username only if it doesn't already exist
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'uq_players_username' AND object_id = OBJECT_ID('players')
)
    ALTER TABLE players ADD CONSTRAINT uq_players_username UNIQUE (username);

-- Drop the auto-generated unique constraint on device_token (name varies per environment)
DECLARE @constraint NVARCHAR(256) = (
    SELECT kc.name
    FROM sys.key_constraints kc
    JOIN sys.index_columns ic
        ON kc.unique_index_id = ic.index_id AND ic.object_id = kc.parent_object_id
    JOIN sys.columns c
        ON ic.object_id = c.object_id AND ic.column_id = c.column_id
    WHERE kc.parent_object_id = OBJECT_ID('players') AND c.name = 'device_token'
);
IF @constraint IS NOT NULL
    EXEC('ALTER TABLE players DROP CONSTRAINT [' + @constraint + ']');

-- Drop device_token column if it still exists
IF EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('players') AND name = 'device_token'
)
    ALTER TABLE players DROP COLUMN device_token;