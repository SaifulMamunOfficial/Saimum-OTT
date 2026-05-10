<?php
$mysqli = mysqli_connect('127.0.0.1', 'root', '', 'saimumba_music', 3307);
$res = mysqli_query($mysqli, 'SHOW TABLES');
while($row = mysqli_fetch_row($res)) {
    mysqli_query($mysqli, "DROP TABLE IF EXISTS `$row[0]`");
}
echo "All tables dropped.\n";
?>
