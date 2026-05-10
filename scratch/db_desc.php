<?php
$mysqli = mysqli_connect('127.0.0.1', 'root', '', 'saimumba_music', 3307);
if ($mysqli) {
    $table = $argv[1] ?? 'tbl_artist';
    $res = mysqli_query($mysqli, "DESCRIBE $table");
    while($row = mysqli_fetch_assoc($res)) {
        print_r($row);
    }
}
?>
