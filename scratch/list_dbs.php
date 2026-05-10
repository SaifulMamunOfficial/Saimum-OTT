<?php
$mysqli = mysqli_connect('127.0.0.1', 'root', '', '', 3307);
$res = mysqli_query($mysqli, 'SHOW DATABASES');
while($row = mysqli_fetch_row($res)) {
    echo $row[0] . "\n";
}
?>
