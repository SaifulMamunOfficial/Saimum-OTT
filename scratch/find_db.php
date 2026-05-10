<?php
$attempts = [
    ['root', ''],
    ['root', 'root'],
    ['saimumba_music', 'saimumba_music']
];

foreach ($attempts as $auth) {
    $user = $auth[0];
    $pass = $auth[1];
    $conn = @mysqli_connect('localhost', $user, $pass);
    if ($conn) {
        echo "SUCCESS: Connected with User: $user, Pass: $pass\n";
        echo "Databases found:\n";
        $res = mysqli_query($conn, "SHOW DATABASES");
        while ($row = mysqli_fetch_array($res)) {
            echo " - " . $row[0] . "\n";
        }
        mysqli_close($conn);
        exit;
    }
}
echo "FAILED: Could not connect with any common credentials.\n";
?>
