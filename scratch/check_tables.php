<?php
$conn = mysqli_connect('127.0.0.1', 'root', '', 'saimumba_music', 3307);
if ($conn) {
    $res = mysqli_query($conn, "SHOW TABLES");
    echo "Tables in saimumba_music:\n";
    while ($row = mysqli_fetch_array($res)) {
        echo " - " . $row[0] . "\n";
    }
    mysqli_close($conn);
} else {
    echo "Connection failed: " . mysqli_connect_error() . "\n";
}
?>
