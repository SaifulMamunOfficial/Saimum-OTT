<?php
$conn = mysqli_connect('localhost', 'root', '', 'saimumba_music', 3307);
mysqli_query($conn, "ALTER TABLE tbl_settings ADD COLUMN IF NOT EXISTS home_limit INT DEFAULT 10");
mysqli_query($conn, "ALTER TABLE tbl_settings ADD COLUMN IF NOT EXISTS music_limit INT DEFAULT 20");
echo "Columns added successfully or already exist.\n";

// Update default values if they are 0
mysqli_query($conn, "UPDATE tbl_settings SET home_limit = 10 WHERE home_limit = 0 OR home_limit IS NULL");
mysqli_query($conn, "UPDATE tbl_settings SET music_limit = 20 WHERE music_limit = 0 OR music_limit IS NULL");
echo "Default values set.\n";
