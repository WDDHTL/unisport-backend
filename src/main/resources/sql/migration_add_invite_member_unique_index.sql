-- Ensure invite_members has a unique constraint on (invite_id, user_id)
USE unisport;

SET @table_exists := (
    SELECT COUNT(1)
    FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'invite_members'
);

SET @sql := IF(
    @table_exists = 0,
    'SELECT ''invite_members table missing, skip unique index migration''',
    IF(
        (SELECT COUNT(1)
         FROM information_schema.STATISTICS
         WHERE TABLE_SCHEMA = DATABASE()
           AND TABLE_NAME = ''invite_members''
           AND INDEX_NAME = ''uk_invite_user'') = 0,
        'ALTER TABLE invite_members ADD CONSTRAINT uk_invite_user UNIQUE (invite_id, user_id)',
        'SELECT ''uk_invite_user already exists''')
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
