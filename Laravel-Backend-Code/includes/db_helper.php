<?php
error_reporting(0);
ob_start();
session_start();

// error_reporting(E_ALL);
// ini_set('display_errors', 1);

header("Content-Type: text/html;charset=UTF-8");

if($_SERVER['HTTP_HOST']=="localhost" or $_SERVER['HTTP_HOST']=="192.168.1.125"){

	DEFINE ('DB_USER', 'saimumba_music');
	DEFINE ('DB_PASSWORD', 'saimumba_music');
	DEFINE ('DB_HOST', 'localhost');
	DEFINE ('DB_NAME', 'saimumba_music');
    
} else {

	DEFINE ('DB_USER', 'saimumba_music');
	DEFINE ('DB_PASSWORD', 'saimumba_music');
	DEFINE ('DB_HOST', 'localhost');
	DEFINE ('DB_NAME', 'saimumba_music');
}

$port = ($_SERVER['HTTP_HOST']=="localhost" or $_SERVER['HTTP_HOST']=="192.168.1.125") ? 3306 : 3306;
$mysqli = mysqli_connect(DB_HOST,DB_USER,DB_PASSWORD,DB_NAME, $port);

if ($mysqli->connect_errno) {
	echo "Failed to connect to MySQL: (" . $mysqli->connect_errno . ") " . $mysqli->connect_error;
}

mysqli_query($mysqli,"SET NAMES 'utf8'");

$setting_qry="SELECT * FROM tbl_settings where id='1'";
$setting_result=mysqli_query($mysqli,$setting_qry);
$settings_details=mysqli_fetch_assoc($setting_result);

define("APP_API_KEY",'UzCbzsPZhsH8aeh1JlsK0gR0nYtmpgwcjtXm9g9lAUt4p');
define("ONESIGNAL_APP_ID",$settings_details['onesignal_app_id']);
define("ONESIGNAL_REST_KEY",$settings_details['onesignal_rest_key']);

define("APP_NAME",$settings_details['app_name']);
define("APP_LOGO",$settings_details['app_logo']);

define("HOME_LIMIT",$settings_details['home_limit']);
define("API_LATEST_LIMIT",$settings_details['api_latest_limit']);
define("API_CAT_ORDER_BY",$settings_details['api_cat_order_by']);
define("API_CAT_POST_ORDER_BY",$settings_details['api_cat_post_order_by']);

if(isset($_SESSION['id'])){
	
	$profile_qry="SELECT * FROM tbl_admin where id='".$_SESSION['id']."'";
	$profile_result=mysqli_query($mysqli,$profile_qry);
	$profile_details=mysqli_fetch_assoc($profile_result);

	define("PROFILE_IMG",$profile_details['image']);
}
?>