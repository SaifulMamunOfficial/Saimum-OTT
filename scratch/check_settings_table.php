<?php
$conn = mysqli_connect('localhost', 'root', '', 'saimumba_music', 3307);
$result = mysqli_query($conn, "SHOW TABLES LIKE 'tbl_settings'");
if (mysqli_num_rows($result) > 0) {
    $res = mysqli_query($conn, "DESCRIBE tbl_settings");
    while($row = mysqli_fetch_assoc($res)) {
        echo $row['Field'] . " (" . $row['Type'] . ")\n";
    }
} else {
    echo "tbl_settings not found";
}
