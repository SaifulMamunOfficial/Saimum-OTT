<?php
session_start();
require("../includes/curl_helper.php");
$errors = false;
$database_dump_file = 'database.sql';

$product_info = getLatestVersion();
if($product_info ==''){
    $errors = true;
}

$step = isset($_GET['step']) ? $_GET['step'] : '';

$installFile="../includes/.lic";
if(is_writeable($installFile)){
  $errors = true; 
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">

    <!-- Website Title -->
    <title><?php echo $product_info['product_name']; ?> - Installer</title>
    
    <!-- Favicon -->
    <link href="../assets/images/open-box.svg" rel="icon">

    <!-- IOS Touch Icons -->
    <link rel="apple-touch-icon" href="../assets/images/open-box.svg">
    <link rel="apple-touch-icon" sizes="152x152" href="../assets/images/open-box.svg">
    <link rel="apple-touch-icon" sizes="180x180" href="../assets/images/open-box.svg">
    <link rel="apple-touch-icon" sizes="167x167" href="../assets/images/open-box.svg">

    <!-- Google fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@100;200;300;400;500;600;700;800;900&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Ubuntu:wght@300;400;500;700&display=swap" rel="stylesheet">

    <!-- Vendor styles -->
   <link rel="stylesheet" href="../assets/vendors/bootstrap/bootstrap.min.css" type="text/css">
   <link rel="stylesheet" href="../assets/vendors/perfect-scrollbar/perfect-scrollbar.min.css" type="text/css">
   <link rel="stylesheet" href="../assets/vendors/remixicon/remixicon.min.css" type="text/css">

   <!-- Main style -->
   <link rel="stylesheet" href="../assets/css/styles.css?v=1.0.0" type="text/css">
   
</head>
<body>
    
    <!-- Start: main -->
    <main class="d-flex justify-content-center align-items-center py-5 min-vh-100">
        <div class="nsofts-container install-container">
            <div class="card">
                <div class="card-body p-0">                    
                    <div class="nsofts-install">
                        <?php switch ($step) { default: ?>
                            <?php  
                                if(phpversion() < "7.4"){
                                    $errors = true;
                                }
                                if(!extension_loaded('mysqli')){
                                    $errors = true; 
                                }
                                if(!extension_loaded('curl')){
                                    $errors = true; 
                                }
                                if(!extension_loaded('pdo')){
                                    $errors = true; 
                                }
                                if(!extension_loaded('json')){
                                    $errors = true; 
                                }
                                if(is_writeable($installFile)){
                                    $errors = true; 
                                }
                            ?>
                            <div class="nsofts-install__sidebar">
                                <div class="nav flex-column nav-pills" aria-orientation="vertical">
                                    <div class="nav-link">
                                        <?php if($errors==true){?>
                                            <i class="ri-checkbox-circle-fill error"></i>
                                        <?php } else { ?>
                                            <i class="ri-checkbox-blank-circle-line"></i>
                                        <?php } ?>
                                        <span>Server Requirements</span>
                                    </div>
                                    <div class="nav-link">
                                        <i class="ri-checkbox-blank-circle-line"></i>
                                        <span>Verify Purchase Code</span>
                                    </div>
                                    <div class="nav-link">
                                        <i class="ri-checkbox-blank-circle-line"></i>
                                        <span>Database</span>
                                    </div>
                                    <div class="nav-link">
                                        <i class="ri-checkbox-blank-circle-line"></i>
                                        <span>Let's go</span>
                                    </div>
                                </div>
                            </div>
                            <div class="nsofts-install__content">
                                <div class="tab-content">
                                    
                                    <!-- Server Requirements -->
                                    <div class="tab-pane fade show active">
                                        <div class="row">
                                            <h5 class="mb-4"><?php echo $product_info['product_name']; ?></h5>
                                            <p class="text-secondary">This php extensions Are must needed! If you server don't have this Ask you
                                                server provider to enable it. This are commonly used php extension in all Hosting's.</p>
                                            <div class="notify-list">
                                                <?php  
                                                    // Add or remove your script's requirements below
                                                    if(is_writeable($installFile)){
                                                        echo "<div class='notify notify--error'>
                                                        <svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' width='24' height='24'>
                                                        <path fill='none' d='M0 0h24v24H0z'/>
                                                        <path fill='currentColor' d='M12 10.586l4.95-4.95 1.414 1.414-4.95 4.95 4.95 4.95-1.414 1.414-4.95-4.95-4.95 4.95-1.414-1.414 4.95-4.95-4.95-4.95L7.05 5.636z'/>
                                                        </svg>
                                                        <span class='notify__text'>The installation process is already complete !</span>
                                                        </div>";
                                                    } else {
                                                        
                                                        if(phpversion() < "7.4"){
                                                            echo "<div class='notify notify--error'>
                                                            <svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' width='24' height='24'>
                                                            <path fill='none' d='M0 0h24v24H0z'/>
                                                            <path fill='currentColor' d='M12 10.586l4.95-4.95 1.414 1.414-4.95 4.95 4.95 4.95-1.414 1.414-4.95-4.95-4.95 4.95-1.414-1.414 4.95-4.95-4.95-4.95L7.05 5.636z'/>
                                                            </svg>
                                                            <span class='notify__text'>Current PHP version is ".phpversion()."! minimum PHP 7.4 or higher required.</span>
                                                            </div>";
                                                        } else {
                                                            echo "<div class='notify notify--success'>
                                                            <svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' width='24' height='24'>
                                                            <path fill='none' d='M0 0h24v24H0z'/>
                                                            <path fill='currentColor' d='M10 15.172l9.192-9.193 1.415 1.414L10 18l-6.364-6.364 1.414-1.414z'/>
                                                            </svg> 
                                                            <span class='notify__text'>You are running PHP version ".phpversion()."</span>
                                                            </div>";
                                                        }
                                                        if(!extension_loaded('mysqli')){
                                                            echo "<div class='notify notify--error'>
                                                            <svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' width='24' height='24'>
                                                            <path fill='none' d='M0 0h24v24H0z'/>
                                                            <path fill='currentColor' d='M12 10.586l4.95-4.95 1.414 1.414-4.95 4.95 4.95 4.95-1.414 1.414-4.95-4.95-4.95 4.95-1.414-1.414 4.95-4.95-4.95-4.95L7.05 5.636z'/>
                                                            </svg>
                                                            <span class='notify__text'>MySQLi PHP extension missing!</span>
                                                            </div>";
                                                        } else {
                                                            echo "<div class='notify notify--success'>
                                                            <svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' width='24' height='24'>
                                                            <path fill='none' d='M0 0h24v24H0z'/>
                                                            <path fill='currentColor' d='M10 15.172l9.192-9.193 1.415 1.414L10 18l-6.364-6.364 1.414-1.414z'/>
                                                            </svg>
                                                            <span class='notify__text'>MySQLi PHP extension available</span>
                                                            </div>";
                                                        } 
                                                        if(!extension_loaded('curl')){
                                                            echo "<div class='notify notify--error'>
                                                            <svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' width='24' height='24'>
                                                            <path fill='none' d='M0 0h24v24H0z'/>
                                                            <path fill='currentColor' d='M12 10.586l4.95-4.95 1.414 1.414-4.95 4.95 4.95 4.95-1.414 1.414-4.95-4.95-4.95 4.95-1.414-1.414 4.95-4.95-4.95-4.95L7.05 5.636z'/>
                                                            </svg>
                                                            <span class='notify__text'>Curl PHP extension missing!</span>
                                                            </div>";
                                                        } else {
                                                            echo "<div class='notify notify--success'>
                                                            <svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' width='24' height='24'>
                                                            <path fill='none' d='M0 0h24v24H0z'/>
                                                            <path fill='currentColor' d='M10 15.172l9.192-9.193 1.415 1.414L10 18l-6.364-6.364 1.414-1.414z'/>
                                                            </svg>
                                                            <span class='notify__text'>Curl PHP extension available</span>
                                                            </div>";
                                                        }
                                                        if(!extension_loaded('pdo')){
                                                            echo "<div class='notify notify--error'>
                                                            <svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' width='24' height='24'>
                                                            <path fill='none' d='M0 0h24v24H0z'/>
                                                            <path fill='currentColor' d='M12 10.586l4.95-4.95 1.414 1.414-4.95 4.95 4.95 4.95-1.414 1.414-4.95-4.95-4.95 4.95-1.414-1.414 4.95-4.95-4.95-4.95L7.05 5.636z'/>
                                                            </svg>
                                                            <span class='notify__text'>PDO PHP extension missing!</span>
                                                            </div>";
                                                        } else {
                                                            echo "<div class='notify notify--success'>
                                                            <svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' width='24' height='24'>
                                                            <path fill='none' d='M0 0h24v24H0z'/>
                                                            <path fill='currentColor' d='M10 15.172l9.192-9.193 1.415 1.414L10 18l-6.364-6.364 1.414-1.414z'/>
                                                            </svg>
                                                            <span class='notify__text'>PDO PHP extension available</span>
                                                            </div>";
                                                        }
                                                        if(!extension_loaded('json')){
                                                            echo "<div class='notify notify--error'>
                                                            <svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' width='24' height='24'>
                                                            <path fill='none' d='M0 0h24v24H0z'/>
                                                            <path fill='currentColor' d='M12 10.586l4.95-4.95 1.414 1.414-4.95 4.95 4.95 4.95-1.414 1.414-4.95-4.95-4.95 4.95-1.414-1.414 4.95-4.95-4.95-4.95L7.05 5.636z'/>
                                                            </svg>
                                                            <span class='notify__text'>JSON PHP extension missing!</span>
                                                            </div>";
                                                        } else {
                                                            echo "<div class='notify notify--success'>
                                                            <svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' width='24' height='24'>
                                                            <path fill='none' d='M0 0h24v24H0z'/>
                                                            <path fill='currentColor' d='M10 15.172l9.192-9.193 1.415 1.414L10 18l-6.364-6.364 1.414-1.414z'/>
                                                            </svg>
                                                            <span class='notify__text'>JSON PHP extension available</span>
                                                            </div>";
                                                        }
                                                        
                                                        if($product_info ==''){
                                                            echo "<div class='notify notify--error'>
                                                            <svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' width='24' height='24'>
                                                            <path fill='none' d='M0 0h24v24H0z'/>
                                                            <path fill='currentColor' d='M12 10.586l4.95-4.95 1.414 1.414-4.95 4.95 4.95 4.95-1.414 1.414-4.95-4.95-4.95 4.95-1.414-1.414 4.95-4.95-4.95-4.95L7.05 5.636z'/>
                                                            </svg>
                                                            <span class='notify__text'>PHP extension missing!</span>
                                                            </div>";
                                                        }
                                                        
                                                    }
                                                ?>
                                            </div>
                                            <div class="mt-2" style="text-align: right;">
                                                <a href="<?php echo PRODUCT_URL;?>" target="_blank"
                                                    class="btn btn-danger" style="min-width: 115px;">BUY</a>
                                                    <?php if(is_writeable($installFile)){ ?>
                                                        <button type="button" class="btn btn-primary btn--slide" style="min-width: 115px;" disabled>Next</button>
                                                    <?php } else { ?>
                                                        <a href="index.php?step=0" class="btn btn-primary btn--slide" style="min-width: 115px;">Next</a>
                                                    <?php } ?>
                                                
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        <?php break; case "0": ?>
                            <div class="nsofts-install__sidebar">
                                <div class="nav flex-column nav-pills" aria-orientation="vertical">
                                    <div class="nav-link">
                                        <i class="ri-checkbox-circle-fill active"></i>
                                        <span>Server Requirements</span>
                                    </div>
                                    <div class="nav-link">
                                        <i class="ri-checkbox-blank-circle-line"></i>
                                        <span>Verify Purchase Code</span>
                                    </div>
                                    <div class="nav-link">
                                        <i class="ri-checkbox-blank-circle-line"></i>
                                        <span>Database</span>
                                    </div>
                                    <div class="nav-link">
                                        <i class="ri-checkbox-blank-circle-line"></i>
                                        <span>Let's go</span>
                                    </div>
                                </div>
                            </div>
                            <div class="nsofts-install__content">
                                <div class="tab-content">
                                    
                                <?php
                                  $license_code = null;
                                  $client_name = null;
                                  if(!empty($_POST['license']) && !empty($_POST['client'])){
                                    $license_code = strip_tags(trim($_POST["license"]));
                                    $client_name = strip_tags(trim($_POST["client"]));
                                    
                                    $activate_response = activateLicense($license_code,$client_name);
                                    
                                    $_SESSION['envato_buyer_name']=$client_name;
                                    $_SESSION['envato_purchase_code']=$license_code;
                                    
                                    if(empty($activate_response)){
                                      $msg = 'Server is unavailable.';
                                    } else {
                                      $msg = $activate_response['message'];
                                    }
                                    ?>
                                    
                                    <?php if($activate_response['status'] != true){ ?>
                                    
                                        <!-- Verify Envato Purchase Code Error -->
                                        <div class="tab-pane fade show active">
                                            <div class="row">
                                                <h5 class="mb-4">Verify Envato Purchase Code</h5>
                                                <div class="mb-1">
                                                    <ol>
                                                        <li class="text-secondary">Log into your Envato Market account.</li>
                                                        <li class="text-secondary">Hover the mouse over your username at the top of the screen.</li>
                                                        <li class="text-secondary">Click ‘Downloads’ from the drop-down menu.</li>
                                                        <li class="text-secondary">Click ‘License certificate & purchase code’.</li>
                                                    </ol>
                                                </div>
                                                <div class="notify notify--error" style="margin-bottom: 18px;">
                                                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="24" height="24">
                                                        <path fill="none" d="M0 0h24v24H0z"/>
                                                        <path fill="currentColor" d="M12 10.586l4.95-4.95 1.414 1.414-4.95 4.95 4.95 4.95-1.414 1.414-4.95-4.95-4.95 4.95-1.414-1.414 4.95-4.95-4.95-4.95L7.05 5.636z"/>
                                                    </svg>
                                                    <span class="notify__text"><?php echo ucfirst($msg); ?></span>
                                                </div>
                                                <form action="index.php?step=0" method="POST">
                                                    <div class="mb-3">
                                                        <label for="email" class="form-label fw-semibold">Envato user name</label>
                                                        <div class="nsofts-input-icon nsofts-input-icon--left">
                                                            <label for="email" class="nsofts-input-icon__left">
                                                                <i class="ri-user-line"></i>
                                                            </label>
                                                            <input class="form-control" type="text" placeholder="Enter your envato user name" name="client" autocomplete="off" required>
                                                        </div>
                                                    </div>
                                                    <div class="mb-3">
                                                        <label for="email" class="form-label fw-semibold">Purchase code</label>
                                                        <div class="nsofts-input-icon nsofts-input-icon--left mb-2">
                                                            <label for="email" class="nsofts-input-icon__left">
                                                                <i class="ri-user-line"></i>
                                                            </label>
                                                            <input class="form-control mb-8" type="text" placeholder="Enter your item purchase code" name="license" autocomplete="off" required>
                                                        </div>
                                                        <a href="https://help.market.envato.com/hc/en-us/articles/202822600-Where-Is-My-Purchase-Code"
                                                            class="text-danger" target="_blank">Where Is My Purchase Code?</a>
                                                    </div>
                                                    <div style="text-align: right;">
                                                        <button type="submit"  class="btn btn-primary btn--slide" style="min-width: 115px;">Verify</a>
                                                    </div>
                                                </form>
                                            </div>
                                        </div>
                                        
                                    <?php } else { ?>
                                    
                                        <!-- Verify Envato Purchase Code Done -->
                                        <div class="tab-pane fade show active">
                                            <form action="index.php?step=1" method="POST">
                                                <div class="row">
                                                    <h5 class="mb-4">Verify Envato Purchase Code</h5>
                                                    <div class='notify notify--success mb-5'>
                                                        <svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' width='24' height='24'>
                                                            <path fill='none' d='M0 0h24v24H0z' />
                                                            <path fill='currentColor'
                                                                d='M10 15.172l9.192-9.193 1.415 1.414L10 18l-6.364-6.364 1.414-1.414z' />
                                                        </svg>
                                                        <span class='notify__text'><?php echo ucfirst($msg); ?></span>
                                                    </div>
                                                    <input type="hidden" name="lcscs" id="lcscs" value="<?php echo ucfirst($activate_response['status']); ?>">
                                                    <div style="text-align: right;">
                                                        <button type="submit"  class="btn btn-primary btn--slide" style="min-width: 115px;">Next</a>
                                                    </div>
                                                </div>
                                            </form>
                                        </div>
                                    
                                    <?php } ?>
                                    
                                 <?php } else { ?>
                                 
                                    <!-- Verify Envato Purchase Code -->
                                    <div class="tab-pane fade show active">
                                        <div class="row">
                                            <h5 class="mb-4">Verify Envato Purchase Code</h5>
                                            <div class="mb-1">
                                                <ol>
                                                    <li class="text-secondary">Log into your Envato Market account.</li>
                                                    <li class="text-secondary">Hover the mouse over your username at the top of the screen.</li>
                                                    <li class="text-secondary">Click ‘Downloads’ from the drop-down menu.</li>
                                                    <li class="text-secondary">Click ‘License certificate & purchase code’.</li>
                                                </ol>
                                            </div>
                                            <form action="index.php?step=0" method="POST">
                                                <div class="mb-3">
                                                    <label for="email" class="form-label fw-semibold">Envato user name</label>
                                                    <div class="nsofts-input-icon nsofts-input-icon--left">
                                                        <label for="email" class="nsofts-input-icon__left">
                                                            <i class="ri-user-line"></i>
                                                        </label>
                                                        <input class="form-control" type="text" placeholder="Enter your envato user name" name="client" autocomplete="off" required>
                                                    </div>
                                                </div>
                                                <div class="mb-3">
                                                    <label for="email" class="form-label fw-semibold">Purchase code</label>
                                                    <div class="nsofts-input-icon nsofts-input-icon--left mb-2">
                                                        <label for="email" class="nsofts-input-icon__left">
                                                            <i class="ri-user-line"></i>
                                                        </label>
                                                        <input class="form-control mb-8" type="text" placeholder="Enter your item purchase code" name="license" autocomplete="off" required>
                                                    </div>
                                                    <a href="https://help.market.envato.com/hc/en-us/articles/202822600-Where-Is-My-Purchase-Code"
                                                        class="text-danger" target="_blank">Where Is My Purchase Code?</a>
                                                </div>
                                                <div style="text-align: right;">
                                                    <button type="submit" class="btn btn-primary btn--slide" style="min-width: 115px;">Verify</a>
                                                </div>
                                            </form>
                                        </div>
                                    </div>
                                 
                                 <?php } ?>
                                 
                                </div>
                            </div>
                        <?php break; case "1": ?>
                            
                            <?php if($_POST && isset($_POST["lcscs"])){ ?>
                            
                                <div class="nsofts-install__sidebar">
                                    <div class="nav flex-column nav-pills" aria-orientation="vertical">
                                        <div class="nav-link">
                                            <i class="ri-checkbox-circle-fill active"></i>
                                            <span>Server Requirements</span>
                                        </div>
                                        <div class="nav-link">
                                            <i class="ri-checkbox-circle-fill active"></i>
                                            <span>Verify Purchase Code</span>
                                        </div>
                                        <div class="nav-link">
                                            <i class="ri-checkbox-blank-circle-line"></i>
                                            <span>Database</span>
                                        </div>
                                        <div class="nav-link">
                                            <i class="ri-checkbox-blank-circle-line"></i>
                                            <span>Let's go</span>
                                        </div>
                                    </div>
                                </div>
                                <div class="nsofts-install__content">
                                    <div class="tab-content">
                                       
                                       <?php 
                                        $valid = strip_tags(trim($_POST["lcscs"]));
                                        $db_host = strip_tags(trim($_POST["host"]));
                                        $db_user = strip_tags(trim($_POST["user"]));
                                        $db_pass = strip_tags(trim($_POST["pass"]));
                                        $db_name = strip_tags(trim($_POST["name"]));
                                        // Let's import the sql file into the given database
                                        
                                        if(!empty($db_host)){

                                          $con = @mysqli_connect($db_host, $db_user, $db_pass, $db_name);
                                          if(mysqli_connect_errno()){ 
                                            $error_message = "Failed to connect to MySQL: " . mysqli_connect_error();
                                          ?>
                                                <!-- Database -->
                                                <div class="tab-pane fade show active">
                                                    <form action="index.php?step=1" method="POST">
                                                        <div class="row">
                                                            <div class="notify notify--error" style="margin-bottom: 18px;">
                                                                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="24" height="24">
                                                                    <path fill="none" d="M0 0h24v24H0z"/>
                                                                    <path fill="currentColor" d="M12 10.586l4.95-4.95 1.414 1.414-4.95 4.95 4.95 4.95-1.414 1.414-4.95-4.95-4.95 4.95-1.414-1.414 4.95-4.95-4.95-4.95L7.05 5.636z"/>
                                                                </svg>
                                                                <span class="notify__text"><?php echo $error_message; ?></span>
                                                            </div>
                                                            <input type="hidden" name="lcscs" id="lcscs" value="<?php echo $valid; ?>">
                                                            <h5 class="mb-4">Database</h5>
                                                            <div class="mb-3">
                                                                <label class="form-label fw-semibold">Database Host</label>
                                                                <input class="form-control" type="text" id="host" placeholder="Enter your database host" name="host" value="localhost" required>
                                                            </div>
                                                            <div class="mb-3">
                                                                <label class="form-label fw-semibold">Database Username</label>
                                                                <input class="form-control" type="text" id="user" placeholder="Enter your database username" name="user" required>
                                                            </div>
                                                            <div class="mb-3">
                                                                <label class="form-label fw-semibold">Database Password</label>
                                                                <input class="form-control" type="text" id="pass" placeholder="Enter your database password" name="pass">
                                                            </div>
                                                            <div class="mb-3">
                                                                <label class="form-label fw-semibold">Database Name</label>
                                                                <input class="form-control" type="text" id="name" placeholder="Enter your database name" name="name" required>
                                                            </div>
                                                            <div style="text-align: right;">
                                                                <button type="submit" id="next" class="btn btn-primary btn--slide" style="min-width: 115px;">Import</button>
                                                            </div>
                                                        </div>
                                                    </form>
                                                </div>
                                        <?php
                                            exit;
                                          }
                                          $templine = '';
                                          $lines = file($database_dump_file);
                                          foreach($lines as $line){
                                            if(substr($line, 0, 2) == '--' || $line == '')
                                              continue;
                                            $templine .= $line;
                                            $query = false;
                                            if(substr(trim($line), -1, 1) == ';'){
                                              $query = mysqli_query($con, $templine);
                                              $templine = '';
                                            }
                                          }
                                          
                                          // Update config file with the provided details
                                            $dataFile = "../includes/db_helper.php";
                                            $fhandle = fopen($dataFile,"r");
                                            $content = fread($fhandle, filesize($dataFile));
                                            $content = str_replace('db_name', $db_name, $content);
                                            $content = str_replace('db_uname', $db_user, $content);
                                            $content = str_replace('db_password', $db_pass, $content);
                                            $content = str_replace('db_hname', $db_host, $content);
                                            fclose($fhandle);
                                            
                                            // Write updated content back
                                            $fhandle = fopen($dataFile, "w");
                                            fwrite($fhandle, $content);
                                            fclose($fhandle);
                                            
                                            // Transaction management
                                            mysqli_autocommit($con, FALSE);
                                          
                                            // Update envato client details
                                            $sqlUpdate = "UPDATE tbl_settings SET 
                                                `envato_buyer_name` = '".$_SESSION['envato_buyer_name']."',
                                                `envato_purchase_code` = '".$_SESSION['envato_purchase_code']."',
                                                `envato_package_name` = '' WHERE `id` = 1";
                                            
                                            $result=mysqli_query($con, $sqlUpdate) or die(mysqli_error($con));
                                          
                                          // Commit transaction
                                          if (!mysqli_commit($con)) {
                                            echo "Commit transaction failed";
                                            exit();
                                          }
                                          
                                          // Close connection
                                          mysqli_close($con);
                                        ?>
                                        
                                        <!-- Database Done -->
                                        <div class="tab-pane fade show active">
                                            <form action="index.php?step=2" method="POST">
                                                <div class="row">
                                                    <h5 class="mb-4">Database</h5>
                                                    <div class='notify notify--success mb-5'>
                                                        <svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' width='24' height='24'>
                                                            <path fill='none' d='M0 0h24v24H0z' />
                                                            <path fill='currentColor'
                                                                d='M10 15.172l9.192-9.193 1.415 1.414L10 18l-6.364-6.364 1.414-1.414z' />
                                                        </svg>
                                                        <span class='notify__text'>Database was successfully imported.</span>
                                                    </div>
                                                    <input type="hidden" name="dbscs" id="dbscs" value="true">
                                                    <div style="text-align: right;">
                                                        <button type="submit" id="next" class="btn btn-primary btn--slide" style="min-width: 115px;">Next</button>
                                                    </div>
                                                </div>
                                            </form>
                                        </div>
                                        
                                        <?php } else { ?>
                                        
                                            <!-- Database -->
                                            <div class="tab-pane fade show active">
                                                <form action="index.php?step=1" method="POST">
                                                    <div class="row">
                                                        <input type="hidden" name="lcscs" id="lcscs" value="<?php echo $valid; ?>">
                                                        <h5 class="mb-4">Database</h5>
                                                        <div class="mb-3">
                                                            <label class="form-label fw-semibold">Database Host</label>
                                                            <input class="form-control" type="text" id="host" placeholder="Enter your database host" name="host" value="localhost" required>
                                                        </div>
                                                        <div class="mb-3">
                                                            <label class="form-label fw-semibold">Database Username</label>
                                                            <input class="form-control" type="text" id="user" placeholder="Enter your database username" name="user" required>
                                                        </div>
                                                        <div class="mb-3">
                                                            <label class="form-label fw-semibold">Database Password</label>
                                                            <input class="form-control" type="text" id="pass" placeholder="Enter your database password" name="pass">
                                                        </div>
                                                        <div class="mb-3">
                                                            <label class="form-label fw-semibold">Database Name</label>
                                                            <input class="form-control" type="text" id="name" placeholder="Enter your database name" name="name" required>
                                                        </div>
                                                        <div style="text-align: right;">
                                                            <button type="submit" id="next" class="btn btn-primary btn--slide" style="min-width: 115px;">Import</button>
                                                        </div>
                                                    </div>
                                                </form>
                                            </div>
                                        
                                        <?php } ?>
                                        
                                    </div>
                                </div>
                            
                            <?php } else { ?>
                                <h2 style="color: #f44336c7;">Sorry, something went wrong.</h2>
                            <?php } ?>
                            
                        <?php break; case "2": ?>
                            <?php if($_POST && isset($_POST["dbscs"])){
                                session_destroy();
                            ?>
                                <div class="nsofts-install__sidebar">
                                    <div class="nav flex-column nav-pills" aria-orientation="vertical">
                                        <div class="nav-link">
                                            <i class="ri-checkbox-circle-fill active"></i>
                                            <span>Server Requirements</span>
                                        </div>
                                        <div class="nav-link">
                                            <i class="ri-checkbox-circle-fill active"></i>
                                            <span>Verify Purchase Code</span>
                                        </div>
                                        <div class="nav-link">
                                            <i class="ri-checkbox-circle-fill active"></i>
                                            <span>Database</span>
                                        </div>
                                        <div class="nav-link">
                                            <i class="ri-checkbox-circle-fill active"></i>
                                            <span>Let's go</span>
                                        </div>
                                    </div>
                                </div>
                                <div class="nsofts-install__content">
                                    <div class="tab-content">
                                        <!--Done -->
                                        <div class="tab-pane fade show active">
                                            <div class="row">
                                                <h5 class="mb-4">Finish</h5>
                                                <div class='notify notify--success mb-3'>
                                                    <svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' width='24' height='24'>
                                                    <path fill='none' d='M0 0h24v24H0z'/>
                                                    <path fill='currentColor' d='M10 15.172l9.192-9.193 1.415 1.414L10 18l-6.364-6.364 1.414-1.414z'/>
                                                    </svg>
                                                    <span class='notify__text'><?php echo $product_info['product_name']; ?> is successfully installed.</span>
                                                </div>
                                                <p>You can now login using your username: <b style="color: #f44336c7;">admin</b> and default password: <b style="color: #f44336c7;">admin</b></p>
                                                <p>The first thing you should do is change your account details.</p>
                                                <div class="mt-4" style="text-align: center;">
                                                    <a href="../index.php" class="btn btn-primary btn--slide" style="min-width: 115px;">Let's go</a>
                                                </div>
                                                <p class="mt-4" style="text-align: center;">Thank you for purchasing our products</p>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            <?php } else { ?>
                                <h2 style="color: #f44336c7;">Sorry, something went wrong.</h2>
                            <?php } ?>
                            
                        <?php break; } ?>
                        
                    </div>
                </div>
            </div>
        </div>
    </main>
    <!-- End: main -->
    
    <!-- Vendor scripts -->
    <script src="../assets/js/jquery.min.js"></script>
    <script src="../../assets/vendors/bootstrap/bootstrap.min.js"></script>
    <script src="../assets/vendors/notify/notify.min.js"></script>
    <script src="../assets/vendors/perfect-scrollbar/perfect-scrollbar.min.js"></script>
    <script src="../assets/vendors/quill/quill.min.js"></script>
    <script src="../assets/vendors/select2/select2.min.js"></script>
    <script src="../assets/vendors/sweetalerts2/sweetalert2.min.js"></script>
    <script src="../assets/vendors/chartjs/chart.min.js"></script>
    
    <!-- Main script -->
    <script src="../assets/js/main.js?v=1.0.0"></script>
    
</body>
</html>