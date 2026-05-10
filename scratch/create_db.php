<?php
$conn = mysqli_connect('127.0.0.1', 'root', '', '', 3307);
if ($conn) {
    if (mysqli_query($conn, "CREATE DATABASE IF NOT EXISTS saimumba_music")) {
        echo "DATABASE CREATED SUCCESSFULLY\n";
    } else {
        echo "ERROR CREATING DATABASE: " . mysqli_error($conn) . "\n";
    }
    mysqli_close($conn);
} else {
    echo "CONNECTION FAILED: " . mysqli_connect_error() . "\n";
}
?>
