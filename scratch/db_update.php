<?php
error_reporting(E_ALL);
ini_set('display_errors', 1);

// Standard XAMPP Defaults with Port 3307
DEFINE ('DB_USER', 'root');
DEFINE ('DB_PASSWORD', '');
DEFINE ('DB_HOST', '127.0.0.1:3307');
DEFINE ('DB_NAME', 'saimumba_music');

$mysqli = mysqli_connect('127.0.0.1', DB_USER, DB_PASSWORD, DB_NAME, 3307);

if (!$mysqli) {
    die("Connection failed: " . mysqli_connect_error());
}

echo "Connected successfully to " . DB_NAME . " on port 3307\n";
echo "Checking tbl_users for missing columns...\n";

// Add role column if not exists
$check_role = mysqli_query($mysqli, "SHOW COLUMNS FROM `tbl_users` LIKE 'role'");
if (mysqli_num_rows($check_role) == 0) {
    echo "Adding 'role' column...\n";
    mysqli_query($mysqli, "ALTER TABLE `tbl_users` ADD `role` VARCHAR(20) DEFAULT 'user' AFTER `status`") or die(mysqli_error($mysqli));
} else {
    echo "'role' column already exists.\n";
}

// Add student_id column if not exists
$check_student = mysqli_query($mysqli, "SHOW COLUMNS FROM `tbl_users` LIKE 'student_id'");
if (mysqli_num_rows($check_student) == 0) {
    echo "Adding 'student_id' column...\n";
    mysqli_query($mysqli, "ALTER TABLE `tbl_users` ADD `student_id` VARCHAR(50) DEFAULT NULL AFTER `role`") or die(mysqli_error($mysqli));
} else {
    echo "'student_id' column already exists.\n";
}

echo "Database update complete!\n";
?>
