<?php
$mysqli = mysqli_connect('127.0.0.1', 'root', '', 'saimumba_music', 3307);
if ($mysqli) {
    $audio = mysqli_fetch_assoc(mysqli_query($mysqli, "SELECT COUNT(*) as count FROM tbl_audio"));
    $banners = mysqli_fetch_assoc(mysqli_query($mysqli, "SELECT COUNT(*) as count FROM tbl_banner"));
    $albums = mysqli_fetch_assoc(mysqli_query($mysqli, "SELECT COUNT(*) as count FROM tbl_album"));
    
    echo "Total Audios in DB: " . $audio['count'] . "\n";
    echo "Total Banners in DB: " . $banners['count'] . "\n";
    echo "Total Albums in DB: " . $albums['count'] . "\n";
}
?>
