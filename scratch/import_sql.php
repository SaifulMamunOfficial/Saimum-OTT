<?php
set_time_limit(0);
$conn = mysqli_connect('127.0.0.1', 'root', '', 'saimumba_music', 3307);
if (!$conn) {
    die("Connection failed: " . mysqli_connect_error());
}

$sqlFile = 'D:/Web-project/Music-App-Project/saimumba_newmusic.sql';
if (!file_exists($sqlFile)) {
    die("SQL file not found at $sqlFile");
}

echo "Starting import...\n";
$query = '';
$handle = fopen($sqlFile, "r");
$count = 0;

if ($handle) {
    while (($line = fgets($handle)) !== false) {
        // Skip comments
        if (substr($line, 0, 2) == '--' || $line == '') {
            continue;
        }

        $query .= $line;
        if (substr(trim($line), -1) == ';') {
            if (!mysqli_query($conn, $query)) {
                echo "Error executing query: " . mysqli_error($conn) . "\n";
            }
            $query = '';
            $count++;
            if ($count % 100 == 0) {
                echo "Executed $count queries...\n";
            }
        }
    }
    fclose($handle);
}

echo "Import finished! Total queries: $count\n";
mysqli_close($conn);
?>
