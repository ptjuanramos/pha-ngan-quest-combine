EXEC sp_rename 'photos.data_url', 'blob_url', 'COLUMN';

EXEC('ALTER TABLE photos ADD validation_status NVARCHAR(20) NOT NULL DEFAULT ''PENDING''');