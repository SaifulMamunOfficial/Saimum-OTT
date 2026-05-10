<?php $page_title="Settings App";
    include("includes/header.php");
    require("includes/lb_helper.php");
    require("language/language.php");
    
    if(!isset($_SESSION['admin_type'])){
        if($_SESSION['admin_type'] == 0){
            session_destroy();
            header( "Location:index.php");
            exit;
        }
    }
    
    $privacy_policy_file_path = getBaseUrl().'privacy_policy.php';
    $terms_file_path = getBaseUrl().'terms.php';
    
    $qry="SELECT * FROM tbl_settings where id='1'";
    $result=mysqli_query($mysqli,$qry);
    $settings_data=mysqli_fetch_assoc($result);
    
    if(isset($_POST['submit_general'])){
        
        $data = array(
            'app_email'  =>  $_POST['app_email'],
            'app_author'  =>  $_POST['app_author'],
            'app_contact'  =>  $_POST['app_contact'],
            'app_website'  =>  $_POST['app_website'],
            'app_developed_by'  =>  $_POST['app_developed_by'],
            'app_description'  =>  addslashes($_POST['app_description'])
        );
        
        $settings_edit=Update('tbl_settings', $data, "WHERE id = '1'");
        
        $_SESSION['msg']="11";
        $_SESSION['class'] = "success";
        header( "Location:settings_app.php");
        exit;
        
    } else if(isset($_POST['app_submit'])){
        
        $data = array(
            'isRTL'  =>  ($_POST['isRTL']) ? 'true' : 'false',
            'isMaintenance'  =>  ($_POST['isMaintenance']) ? 'true' : 'false',
            'isScreenshot'  =>  ($_POST['isScreenshot']) ? 'true' : 'false',
            
            'isLogin'  =>  ($_POST['isLogin']) ? 'true' : 'false',
            'isGoogleLogin'  =>  ($_POST['isGoogleLogin']) ? 'true' : 'false',
            
            'isAPK'  =>  ($_POST['isAPK']) ? 'true' : 'false',
            'isVPN'  =>  ($_POST['isVPN']) ? 'true' : 'false',
            
            'isDummy_1'  =>  ($_POST['isDummy_1']) ? 'true' : 'false'
        );
        
        $settings_edit = Update('tbl_settings', $data, "WHERE id = '1'");
        
        $_SESSION['msg'] = "11";
        $_SESSION['class'] = "success";
        header("Location:settings_app.php");
        exit;
        
    } else if (isset($_POST['api_submit'])) {
      
        $data = array(
            'home_limit'  =>  $_POST['home_limit'],
            'api_latest_limit'  =>  $_POST['api_latest_limit'],
            'api_cat_order_by'  =>  $_POST['api_cat_order_by'],
            'api_cat_post_order_by'  =>  $_POST['api_cat_post_order_by']
        );
        
        $settings_edit = Update('tbl_settings', $data, "WHERE id = '1'");
        
        $_SESSION['msg'] = "11";
        $_SESSION['class'] = "success";
        header("Location:settings_app.php");
        exit;
        
    } else if(isset($_POST['policy_submit'])){
        
        $data = array('app_privacy_policy'  =>  addslashes($_POST['app_privacy_policy']));
        $settings_edit=Update('tbl_settings', $data, "WHERE id = '1'");
        
        $_SESSION['msg']="11";
        $_SESSION['class'] = "success";
        header( "Location:settings_app.php");
        exit;
        
    } else if(isset($_POST['terms_submit'])){
        
        $data = array('app_terms'  =>  addslashes($_POST['app_terms']));
        $settings_edit=Update('tbl_settings', $data, "WHERE id = '1'");
        
        $_SESSION['msg']="11";
        $_SESSION['class'] = "success";
        header( "Location:settings_app.php");
        exit;
        
    } else if(isset($_POST['notification_submit'])) {
        
        $data = array(
          'onesignal_app_id' => trim($_POST['onesignal_app_id']),
          'onesignal_rest_key' => trim($_POST['onesignal_rest_key']),
        );
        
        $settings_edit = Update('tbl_settings', $data, "WHERE id = '1'");
        
        $_SESSION['msg'] = "11";
        $_SESSION['class'] = "success";
        header("Location:settings_app.php");
        exit;
        
    } else if(isset($_POST['app_update_submit'])){
        
        $data = array(
            'app_update_status'  =>  ($_POST['app_update_status']) ? 'true' : 'false',
            'app_new_version'  =>  trim($_POST['app_new_version']),
            'app_update_desc'  =>  trim($_POST['app_update_desc']),
            'app_redirect_url'  =>  trim($_POST['app_redirect_url'])
        );
        
        $settings_edit = Update('tbl_settings', $data, "WHERE id = '1'");
        
        $_SESSION['msg']="11";
        $_SESSION['class']='success'; 
        header("Location:settings_app.php");
        exit;
    } else if(isset($_POST['submit_more'])){
        
        $data = array('more_apps_url'  =>  trim($_POST['more_apps_url']));
        
        $settings_edit=Update('tbl_settings', $data, "WHERE id = '1'");
        
        $_SESSION['msg']="11";
        $_SESSION['class'] = "success";
        header( "Location:settings_app.php");
        exit;
        
    } 
?>

<!-- Start: main -->
<main id="nsofts_main">
    <div class="nsofts-container">
        <nav aria-label="breadcrumb">
            <ol class="breadcrumb align-items-center">
                <li class="breadcrumb-item d-inline-flex"><a href="dashboard.php"><i class="ri-home-4-fill"></i></a></li>
                <li class="breadcrumb-item d-inline-flex active" aria-current="page"><?php echo (isset($page_title)) ? $page_title : "" ?></li>
            </ol>
        </nav>
            
        <div class="card">
            <div class="card-body p-0">                    
                <div class="nsofts-setting">
                    <div class="nsofts-setting__sidebar">
                        <a class="d-inline-flex align-items-center text-decoration-none fw-semibold mb-4">
                            <span class="ps-2 lh-1"><?php echo (isset($page_title)) ? $page_title : "" ?></span>
                        </a>
                        <div class="nav flex-column nav-pills" id="nsofts_setting" role="tablist" aria-orientation="vertical">
                            <button class="nav-link active" id="nsofts_setting_1" data-bs-toggle="pill" data-bs-target="#nsofts_setting_content_1" type="button" role="tab" aria-controls="nsofts_setting_1" aria-selected="true">
                                <i class="ri-list-settings-line"></i>
                                <span>General</span>
                            </button>
                            
                            <button class="nav-link" id="nsofts_setting_2" data-bs-toggle="pill" data-bs-target="#nsofts_setting_content_2" type="button" role="tab" aria-controls="nsofts_setting_2" aria-selected="false">
                                <i class="ri-settings-5-line"></i>
                                <span>App Settings</span>
                            </button>
                            
                            <button class="nav-link" id="nsofts_setting_3" data-bs-toggle="pill" data-bs-target="#nsofts_setting_content_3" type="button" role="tab" aria-controls="nsofts_setting_3" aria-selected="false">
                                <i class="ri-cloud-line"></i>
                                <span>API</span>
                            </button>
                            
                            <button class="nav-link" id="nsofts_setting_4" data-bs-toggle="pill" data-bs-target="#nsofts_setting_content_4" type="button" role="tab" aria-controls="nsofts_setting_4" aria-selected="false">
                                <i class="ri-survey-line"></i>
                                <span>Privacy Policy</span>
                            </button>
                            
                            <button class="nav-link" id="nsofts_setting_5" data-bs-toggle="pill" data-bs-target="#nsofts_setting_content_5" type="button" role="tab" aria-controls="nsofts_setting_5" aria-selected="false">
                                <i class="ri-survey-line"></i>
                                <span>Terms & Conditions</span>
                            </button>
                            
                            <button class="nav-link" id="nsofts_setting_6" data-bs-toggle="pill" data-bs-target="#nsofts_setting_content_6" type="button" role="tab" aria-controls="nsofts_setting_6" aria-selected="false">
                                <i class="ri-notification-3-line"></i>
                                <span>Notification</span>
                            </button>
                            
                            <button class="nav-link" id="nsofts_setting_7" data-bs-toggle="pill" data-bs-target="#nsofts_setting_content_7" type="button" role="tab" aria-controls="nsofts_setting_7" aria-selected="false">
                                <i class="ri-refresh-line"></i>
                                <span>App Update</span>
                            </button>
                            
                            <button class="nav-link" id="nsofts_setting_8" data-bs-toggle="pill" data-bs-target="#nsofts_setting_content_8" type="button" role="tab" aria-controls="nsofts_setting_8" aria-selected="false">
                                <i class="ri-link"></i>
                                <span>More Apps</span>
                            </button>
                        </div>
                    </div>
                    <div class="nsofts-setting__content">
                        <div class="tab-content">
                            
                            <!--General Settings-->
                            <div class="tab-pane fade show active" id="nsofts_setting_content_1" role="tabpanel" aria-labelledby="nsofts_setting_1" tabindex="0">
                                <form action="" name="settings_general" method="POST" enctype="multipart/form-data">
                                    <h4 class="mb-4">General Settings</h4>
                                    <div class="mb-3 row">
                                        <label for="" class="col-sm-2 col-form-label">Email</label>
                                        <div class="col-sm-10">
                                            <input type="text" class="form-control" name="app_email" id="app_email" value="<?php echo $settings_data['app_email']?>" >
                                        </div>
                                    </div>
                                    <div class="mb-3 row">
                                        <label for="" class="col-sm-2 col-form-label">Author</label>
                                        <div class="col-sm-10">
                                            <input type="text" class="form-control" name="app_author" id="app_author" value="<?php echo $settings_data['app_author']?>" >
                                        </div>
                                    </div>
                                    <div class="mb-3 row">
                                        <label for="" class="col-sm-2 col-form-label">Contact</label>
                                        <div class="col-sm-10">
                                            <input type="text" class="form-control" name="app_contact" id="app_contact" value="<?php echo $settings_data['app_contact']?>" >
                                        </div>
                                    </div>
                                    <div class="mb-3 row">
                                        <label for="" class="col-sm-2 col-form-label">Website</label>
                                        <div class="col-sm-10">
                                            <input type="text" class="form-control" name="app_website" id="app_website" value="<?php echo $settings_data['app_website']?>" >
                                        </div>
                                    </div>
                                    <div class="mb-3 row">
                                        <label for="" class="col-sm-2 col-form-label">Developed By</label>
                                        <div class="col-sm-10">
                                            <input type="text" class="form-control" name="app_developed_by" id="app_developed_by" value="<?php echo $settings_data['app_developed_by']?>" >
                                        </div>
                                    </div>
                                    <div class="mb-3 row">
                                        <label for="" class="col-sm-2 col-form-label">Description</label>
                                        <div class="col-sm-10">
                                            <textarea name="app_description" id="app_description" class="form-control" ><?php echo stripslashes($settings_data['app_description']); ?></textarea>
                                        </div>
                                    </div>
                                    <button type="submit" name="submit_general" class="btn btn-primary" style="min-width: 120px;">Save</button>
                                </form>
                            </div>
                            
                            <!--App Settings-->
                            <div class="tab-pane fade" id="nsofts_setting_content_2" role="tabpanel" aria-labelledby="nsofts_setting_2" tabindex="0">
                                <form action="" name="settings_app" method="POST" enctype="multipart/form-data">
                                    <h4 class="mb-4">App Settings</h4>
                                    <div class="mb-3 row">
                                        <label for="" class="col-sm-2 col-form-label">Song Dowload</label>
                                        <div class="col-sm-10">
                                            <div class="nsofts-switch d-flex align-items-center">
                                                <input type="checkbox" id="isDummy_1" name="isDummy_1" value="true" class="cbx hidden" <?php if($settings_data['isDummy_1']=='true'){ echo 'checked'; }?>/>
                                                <label for="isDummy_1" class="nsofts-switch__label"></label>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="mb-3 row">
                                        <label for="" class="col-sm-2 col-form-label">RTL</label>
                                        <div class="col-sm-10">
                                            <div class="nsofts-switch d-flex align-items-center">
                                                <input type="checkbox" id="isRTL" name="isRTL" value="true" class="nsofts-switch__label" <?php if($settings_data['isRTL']=='true'){ echo 'checked'; }?>/>
                                                <label for="isRTL" class="nsofts-switch__label"></label>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="mb-3 row">
                                        <label for="" class="col-sm-2 col-form-label">App Maintenance</label>
                                        <div class="col-sm-10">
                                            <div class="nsofts-switch d-flex align-items-center">
                                                <input type="checkbox" id="isMaintenance" name="isMaintenance" value="true" class="cbx hidden" <?php if($settings_data['isMaintenance']=='true'){ echo 'checked'; }?>/>
                                                <label for="isMaintenance" class="nsofts-switch__label"></label>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="mb-3 row">
                                        <label for="" class="col-sm-2 col-form-label">Google Login</label>
                                        <div class="col-sm-10">
                                            <div class="nsofts-switch d-flex align-items-center">
                                                <input type="checkbox" id="isGoogleLogin" name="isGoogleLogin" value="true" class="cbx hidden" <?php if($settings_data['isGoogleLogin']=='true'){ echo 'checked'; }?>/>
                                                <label for="isGoogleLogin" class="nsofts-switch__label"></label>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="mb-3 row">
                                        <label for="" class="col-sm-2 col-form-label">First open login</label>
                                        <div class="col-sm-10">
                                            <div class="nsofts-switch d-flex align-items-center">
                                                <input type="checkbox" id="isLogin" name="isLogin" value="true" class="cbx hidden" <?php if($settings_data['isLogin']=='true'){ echo 'checked'; }?>/>
                                                <label for="isLogin" class="nsofts-switch__label"></label>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="mb-3 row">
                                        <label for="" class="col-sm-2 col-form-label">Sccrenshot block</label>
                                        <div class="col-sm-10">
                                            <div class="nsofts-switch d-flex align-items-center">
                                                <input type="checkbox" id="isScreenshot" name="isScreenshot" value="true" class="cbx hidden" <?php if($settings_data['isScreenshot']=='true'){ echo 'checked'; }?>/>
                                                <label for="isScreenshot" class="nsofts-switch__label"></label>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="mb-3 row">
                                        <label for="" class="col-sm-2 col-form-label">Developer block</label>
                                        <div class="col-sm-10">
                                            <div class="nsofts-switch d-flex align-items-center">
                                                <input type="checkbox" id="isAPK" name="isAPK" value="true" class="cbx hidden" <?php if($settings_data['isAPK']=='true'){ echo 'checked'; }?>/>
                                                <label for="isAPK" class="nsofts-switch__label"></label>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="mb-3 row">
                                        <label for="" class="col-sm-2 col-form-label">VPN block</label>
                                        <div class="col-sm-10">
                                            <div class="nsofts-switch d-flex align-items-center">
                                                <input type="checkbox" id="isVPN" name="isVPN" value="true" class="cbx hidden" <?php if($settings_data['isVPN']=='true'){ echo 'checked'; }?>/>
                                                <label for="isVPN" class="nsofts-switch__label"></label>
                                            </div>
                                        </div>
                                    </div>
                                    <button type="submit" name="app_submit" class="btn btn-primary" style="min-width: 120px;">Save</button>
                                </form>
                            </div>
                            
                            <!--API-->
                            <div class="tab-pane fade" id="nsofts_setting_content_3" role="tabpanel" aria-labelledby="nsofts_setting_3" tabindex="0">
                                <form action="" name="settings_api" method="POST" enctype="multipart/form-data">
                                    <h4 class="mb-4">API Settings</h4>
                                    <div class="row mb-4">
                                        <div class="col-md-6">
                                            <div class="mb-3 row">
                                                <label for="" class="col-sm-5 col-form-label">Home limit</label>
                                                <div class="col-sm-7">
                                                    <input type="number" name="home_limit" id="home_limit" required="" class="form-control" value="<?php echo $settings_data['home_limit'];?>">
                                                </div>
                                            </div>
                                            <div class="mb-3 row">
                                                <label for="" class="col-sm-5 col-form-label">Latest limit</label>
                                                <div class="col-sm-7">
                                                   <input type="number" name="api_latest_limit" id="api_latest_limit" required="" class="form-control" value="<?php echo $settings_data['api_latest_limit'];?>">
                                                </div>
                                            </div>
                                        </div>
                                        <div class="col-md-6">
                                            <div class="mb-3 row">
                                                <label for="" class="col-sm-5 col-form-label">Category order</label>
                                                <div class="col-sm-7">
                                                    <select name="api_cat_order_by" id="api_cat_order_by" class="form-control" required>
                                                    <option value="cid" <?php if ($settings_data['api_cat_order_by'] == 'cid') { ?>selected<?php } ?>>ID</option>
                                                    <option value="category_name" <?php if ($settings_data['api_cat_order_by'] == 'category_name') { ?>selected<?php } ?>>Name</option>
                                                </select>
                                                </div>
                                            </div>
                                            <div class="mb-3 row">
                                                <label for="" class="col-sm-5 col-form-label">Category post order</label>
                                                <div class="col-sm-7">
                                                    <select name="api_cat_post_order_by" id="api_cat_post_order_by" class="form-control" required>
                                                        <option value="ASC" <?php if ($settings_data['api_cat_post_order_by'] == 'ASC') { ?>selected<?php } ?>>ASC</option>
                                                        <option value="DESC" <?php if ($settings_data['api_cat_post_order_by'] == 'DESC') { ?>selected<?php } ?>>DESC</option>						 
                                                    </select>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <button type="submit" name="api_submit" class="btn btn-primary" style="min-width: 120px;">Save</button>
                                </form>
                            </div>
                            
                            <!--Privacy Policy-->
                            <div class="tab-pane fade" id="nsofts_setting_content_4" role="tabpanel" aria-labelledby="nsofts_setting_4" tabindex="0">
                                <form action="" name="settings_policy" method="POST" enctype="multipart/form-data">
                                    <h4 class="mb-4">Privacy Policy</h4>
                                    <div class="pb-clipboard mb-2">
                                        <span class="pb-clipboard__url"><span id="clipboard_policy"><?=$privacy_policy_file_path ?></span></span>
                                        <a class="pb-clipboard__link btn_policy" href="javascript:void(0);" data-clipboard-action="copy" data-clipboard-target="#clipboard_base_url" data-bs-toggle="tooltip" data-bs-placement="top" title="Copy">
                                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                                <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" />
                                            </svg>
                                        </a>
                                    </div>
                                    <div>
                                        <textarea name="app_privacy_policy" id="app_privacy_policy" rows="5" class="nsofts-editor mb-4">
                                            <?php echo stripslashes($settings_data['app_privacy_policy']); ?>
                                            
                                        </textarea>
                                    </div>
                                    <button type="submit" name="policy_submit" class="btn btn-primary" style="min-width: 120px;">Save</button>
                                </form>
                            </div>
                            
                            <!--Terms & Conditions-->
                            <div class="tab-pane fade" id="nsofts_setting_content_5" role="tabpanel" aria-labelledby="nsofts_setting_5" tabindex="0">
                                <form action="" name="settings_terms" method="POST" enctype="multipart/form-data">
                                    <h4 class="mb-4">Terms & Conditions</h4>
                                    <div class="pb-clipboard mb-2">
                                        <span class="pb-clipboard__url"><span id="clipboard_terms"><?=$terms_file_path ?></span></span>
                                        <a class="pb-clipboard__link btn_terms" href="javascript:void(0);" data-clipboard-action="copy" data-clipboard-target="#clipboard_base_url" data-bs-toggle="tooltip" data-bs-placement="top" title="Copy">
                                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                                <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" />
                                            </svg>
                                        </a>
                                    </div>
                                    <div>
                                        <textarea name="app_terms" id="app_terms" rows="5" class="nsofts-editor mb-4">
                                           <?php echo stripslashes($settings_data['app_terms']); ?>
                                        </textarea>
                                    </div>
                                    <button type="submit" name="terms_submit" class="btn btn-primary" style="min-width: 120px;">Save</button>
                                </form>
                            </div>
                            
                            <!--Notification-->
                            <div class="tab-pane fade" id="nsofts_setting_content_6" role="tabpanel" aria-labelledby="nsofts_setting_6" tabindex="0">
                                <form action="" name="settings_notification" method="POST" enctype="multipart/form-data">
                                    <h4 class="mb-4">Notification</h4>
                                    <div class="mb-3 row">
                                        <label for="" class="col-sm-2 col-form-label">OneSignal App ID</label>
                                        <div class="col-sm-10">
                                            <input type="text" name="onesignal_app_id" id="onesignal_app_id" value="<?php echo $settings_data['onesignal_app_id']; ?>"  class="form-control">
                                        </div>
                                    </div>
                                    <div class="mb-3 row">
                                        <label for="" class="col-sm-2 col-form-label">OneSignal Rest Key</label>
                                        <div class="col-sm-10">
                                            <input type="text" name="onesignal_rest_key" id="onesignal_rest_key" value="<?php echo $settings_data['onesignal_rest_key']; ?>"   class="form-control">
                                        </div>
                                    </div>
                                    <button type="submit" name="notification_submit" class="btn btn-primary" style="min-width: 120px;">Save</button>
                                </form>
                            </div>
                            
                            <!--App Update-->
                            <div class="tab-pane fade" id="nsofts_setting_content_7" role="tabpanel" aria-labelledby="nsofts_setting_7" tabindex="0">
                                <form action="" name="settings_app_update" method="POST" enctype="multipart/form-data">
                                    <h4 class="mb-4">App Update</h4>
                                    <div class="mb-3 row">
                                        <label for="" class="col-sm-2 col-form-label">ON/OFF</label>
                                        <div class="col-sm-10">
                                            <div class="nsofts-switch d-flex align-items-center">
                                                <input type="checkbox" id="app_update_status" name="app_update_status" value="true" class="nsofts-switch__label" <?php if($settings_data['app_update_status']=='true'){ echo 'checked'; }?>/>
                                                <label for="app_update_status" class="nsofts-switch__label"></label>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="mb-3 row">
                                        <label for="" class="col-sm-2 col-form-label">New App Version Code</label>
                                        <div class="col-sm-10">
                                            <input type="number" min="1" name="app_new_version" id="app_new_version" required="" class="form-control" value="<?php echo $settings_data['app_new_version'];?>">
                                        </div>
                                    </div>
                                    <div class="mb-3 row">
                                        <label for="" class="col-sm-2 col-form-label">Description</label>
                                        <div class="col-sm-10">
                                            <textarea name="app_update_desc"  class="form-control"><?php echo stripslashes($settings_data['app_update_desc']); ?></textarea>
                                        </div>
                                    </div>
                                    <div class="mb-3 row">
                                        <label for="" class="col-sm-2 col-form-label">App Link</label>
                                        <div class="col-sm-10">
                                            <input type="text" name="app_redirect_url" id="app_redirect_url" required="" class="form-control" value="<?php echo $settings_data['app_redirect_url'];?>">
                                        </div>
                                    </div>
                                    <button type="submit" name="app_update_submit" class="btn btn-primary" style="min-width: 120px;">Save</button>
                                </form>
                            </div>
                            
                            <!--More Apps-->
                            <div class="tab-pane fade" id="nsofts_setting_content_8" role="tabpanel" aria-labelledby="nsofts_setting_8" tabindex="0">
                                <form action="" name="settings_app_more" method="POST" enctype="multipart/form-data">
                                    <h4 class="mb-4">More Apps</h4>
                                    <div class="mb-3 row">
                                        <label for="" class="col-sm-2 col-form-label">Link</label>
                                        <div class="col-sm-10">
                                            <input type="text" name="more_apps_url" id="more_apps_url" required="" class="form-control" value="<?php echo $settings_data['more_apps_url'];?>">
                                        </div>
                                    </div>
                                    <button type="submit" name="submit_more" class="btn btn-primary" style="min-width: 120px;">Save</button>
                                </form>
                            </div>
                            
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</main>
<!-- End: main -->
    
<?php include("includes/footer.php");?>

<script type="text/javascript">
    $(document).ready(function(event) {
        
        $(document).on("click", ".btn_policy", function(e) {
            var el = document.getElementById('clipboard_policy');
            var successful = copyToClipboard(el);
            if (successful) {
                $.notify('Copied!', { position:"top right",className: 'success'} );
            } else {
                $.notify('Whoops, not copied!', { position:"top right",className: 'error'} );
            }
        });
        
        $(document).on("click", ".btn_terms", function(e) {
            var el = document.getElementById('clipboard_terms');
            var successful = copyToClipboard(el);
            if (successful) {
                $.notify('Copied!', { position:"top right",className: 'success'} );
            } else {
                $.notify('Whoops, not copied!', { position:"top right",className: 'error'} );
            }
        });
    });
</script>