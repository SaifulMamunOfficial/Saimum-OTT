<?php $page_title="Settings Web";
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
    
    $qry="SELECT * FROM tbl_web_settings where id='1'";
    $result=mysqli_query($mysqli,$qry);
    $settings_data=mysqli_fetch_assoc($result);

    if(isset($_POST['submit_general'])){
        
        if($_FILES['web_favicon']['name']!=""){
            
            $img_res=mysqli_query($mysqli,"SELECT * FROM tbl_web_settings WHERE id='1'");
            $img_row=mysqli_fetch_assoc($img_res);
            
            if($img_row['web_favicon']!=""){
                unlink('images/'.$img_row['web_favicon']);
            }
            
            $ext = pathinfo($_FILES['web_favicon']['name'], PATHINFO_EXTENSION);
            $favicon_image=rand(0,99999)."_web_favicon.".$ext;
            $tpath1='images/'.$favicon_image;
            
            if($ext!='png')  {
                $pic1=compress_image($_FILES["web_favicon"]["tmp_name"], $tpath1, 80);
            } else {
                $tmp = $_FILES['web_favicon']['tmp_name'];
                move_uploaded_file($tmp, $tpath1);
            }
            
        } else {
            $favicon_image=$settings_data['web_favicon'];
        }
        
        $data = array(
            'site_name'  =>  cleanInput($_POST['site_name']),
            'site_description'  =>  cleanInput($_POST['site_description']),
            'site_keywords'  =>  cleanInput($_POST['site_keywords']),
            'web_favicon'  =>  $favicon_image,
            'copyright_text'  =>  cleanInput($_POST['copyright_text']),
            'header_code'  =>  htmlentities(trim($_POST['header_code'])),
            'footer_code'  => htmlentities(trim($_POST['footer_code']))
        );
        
        $settings_edit = Update('tbl_web_settings', $data, "WHERE id = '1'");
        
        $_SESSION['msg'] = "11";
        header("Location:settings_web.php");
        exit;
        
    } else if(isset($_POST['submit_contact_us'])){

        $data = array(
            
            'contact_page_title'  =>  cleanInput($_POST['contact_page_title']),
            'address'  =>  cleanInput($_POST['address']),
            'contact_number'  =>  cleanInput($_POST['contact_number']),
            'contact_email'  =>  cleanInput($_POST['contact_email']),
            
            'android_app_url'  =>  $_POST['android_app_url'],
            'ios_app_url'  =>  $_POST['ios_app_url'],
            
            'facebook_url'  =>  $_POST['facebook_url'],
            'twitter_url'  =>  $_POST['twitter_url'],
            'youtube_url'  =>  $_POST['youtube_url'],
            'instagram_url'  => $_POST['instagram_url']
            
        );
        
        $settings_edit = Update('tbl_web_settings', $data, "WHERE id = '1'");
        
        $_SESSION['msg'] = "11";
        header("Location:settings_web.php");
        exit;
        
    } else if(isset($_POST['submit_about_us'])){

        $data = array(
            'about_page_title'  =>  cleanInput($_POST['about_page_title']),
            'about_content'  =>  addslashes($_POST['about_content']),
            'about_status' => ($_POST['about_status']) ? 'true' : 'false'
        );
        
        $settings_edit = Update('tbl_web_settings', $data, "WHERE id = '1'");
        
        $_SESSION['msg'] = "11";
        header("Location:settings_web.php");
        exit;
        
    } else if(isset($_POST['submit_privacy'])){
        
        $data = array(
            'privacy_page_title'  =>  cleanInput($_POST['privacy_page_title']),
            'privacy_content'  =>  addslashes($_POST['privacy_content']),
            'privacy_page_status' => ($_POST['privacy_page_status']) ? 'true' : 'false'
        );
        
        $settings_edit = Update('tbl_web_settings', $data, "WHERE id = '1'");
        
        $_SESSION['msg'] = "11";
        header("Location:settings_web.php");
        exit;
        
    } else if(isset($_POST['submit_terms'])){
        
        $data = array(
            'terms_of_use_page_title'  =>  cleanInput($_POST['terms_of_use_page_title']),
            'terms_of_use_content'  =>  addslashes($_POST['terms_of_use_content']),
            'terms_of_use_page_status' => ($_POST['terms_of_use_page_status']) ? 'true' : 'false'
        );
        
        $settings_edit = Update('tbl_web_settings', $data, "WHERE id = '1'");
        
        $_SESSION['msg'] = "11";
        header("Location:settings_web.php");
        exit;
    } else if(isset($_POST['submit_set'])){
        
        $data = array('isSongDowload'  =>  ($_POST['isSongDowload']) ? 'true' : 'false');
        $settings_edit=Update('tbl_web_settings', $data, "WHERE id = '1'");
        
        $_SESSION['msg']="11";
        header("Location:settings_web.php");
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
                                <span>Web Settings</span>
                            </button>
                            
                            <button class="nav-link" id="nsofts_setting_3" data-bs-toggle="pill" data-bs-target="#nsofts_setting_content_3" type="button" role="tab" aria-controls="nsofts_setting_3" aria-selected="false">
                                <i class="ri-contacts-line"></i>
                                <span>Contact us</span>
                            </button>
                            
                            <button class="nav-link" id="nsofts_setting_4" data-bs-toggle="pill" data-bs-target="#nsofts_setting_content_4" type="button" role="tab" aria-controls="nsofts_setting_4" aria-selected="false">
                                <i class="ri-pages-line"></i>
                                <span>About Us</span>
                            </button>
                            
                            <button class="nav-link" id="nsofts_setting_5" data-bs-toggle="pill" data-bs-target="#nsofts_setting_content_5" type="button" role="tab" aria-controls="nsofts_setting_5" aria-selected="false">
                                <i class="ri-survey-line"></i>
                                <span>Privacy Policy</span>
                            </button>
                            
                            <button class="nav-link" id="nsofts_setting_6" data-bs-toggle="pill" data-bs-target="#nsofts_setting_content_6" type="button" role="tab" aria-controls="nsofts_setting_6" aria-selected="false">
                                <i class="ri-survey-line"></i>
                                <span>Terms & Conditions</span>
                            </button>
                            
                        </div>
                    </div>
                    <div class="nsofts-setting__content">
                        <div class="tab-content">
                            
                            <!--General Settings-->
                            <div class="tab-pane fade show active" id="nsofts_setting_content_1" role="tabpanel" aria-labelledby="nsofts_setting_1" tabindex="0">
                                <form action="" name="settings_general" method="POST" enctype="multipart/form-data">
                                    <h4 class="mb-4">General Settings</h4>
                                    
                                
                                    
                                    <div class="mb-3 mt-3 row">
                                        <label for="" class="col-sm-2 col-form-label">Site Name</label>
                                        <div class="col-sm-10">
                                           <input type="text" name="site_name" id="site_name" value="<?= $settings_data['site_name']?>"  class="form-control">
                                        </div>
                                    </div>
                                    
                                    <div class="mb-3 row">
                                        <label for="" class="col-sm-2 col-form-label">Site Description</label>
                                        <div class="col-sm-10">
                                            <textarea rows="4" name="site_description" class="form-control" required=""><?php echo stripslashes($settings_data['site_description']); ?></textarea>
                                        </div>
                                    </div>
                                    
                                    <div class="mb-3 row">
                                        <label for="" class="col-sm-2 col-form-label">Site Keywords</label>
                                        <div class="col-sm-10">
                                            <input type="text" name="site_keywords" id="site_keywords" value="<?php echo $settings_data['site_keywords']; ?>"  class="form-control" required="required">
                                        </div>
                                    </div>
                                    
                                    <div class="mb-3 row">
                                        <label for="" class="col-sm-2 col-form-label">Site Favicon</label>
                                        <div class="col-sm-10">
                                            <div class="row">
                                                <div class="col-md-5">
                                                    <input type="file" class="form-control-file" name="web_favicon" value="fileupload" accept=".png, .jpg, .JPG .PNG" onchange="fileValidation()" id="fileupload">
                                                    <p class="control-label-help hint_lbl">(Recommended resolution: 16x16 or 32x32)</p>
                                                </div>
                                                <div class="col-md-3">
                                                    <?php if($settings_data['web_favicon']!='' AND file_exists('images/'.$settings_data['web_favicon'])) { ?>
                                                        <div class="fileupload_img" id="imagePreview">
                                                            <img  type="image" src="images/<?=$settings_data['web_favicon']?>" style="width: 50px;height: 50px"   alt="image" />
                                                        </div>
                                                    <?php }else{ ?>
                                                        <div class="fileupload_img" id="imagePreview">
                                                            <img type="image" src="assets/images/300x300.jpg" style="width: 50px; height: 50px"  alt="image" />
                                                        </div>
                                                    <?php } ?>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    
                                    <div class="mb-3 row">
                                        <label for="" class="col-sm-2 col-form-label">Copyright Text</label>
                                        <div class="col-sm-10">
                                            <input type="text" name="copyright_text" id="copyright_text" value="<?php echo $settings_data['copyright_text']; ?>"  class="form-control" required="required">
                                        </div>
                                    </div>
                                    
                                    <div class="mb-3 row">
                                        <label for="" class="col-sm-2 col-form-label">Header Code</label>
                                        <div class="col-sm-10">
                                            <textarea rows="6" name="header_code" class="form-control"  placeholder="Custom CSS or JS Script" ><?php echo html_entity_decode($settings_data['header_code']); ?></textarea>
                                        </div>
                                    </div>
                                    
                                    <div class="mb-3 row">
                                        <label for="" class="col-sm-2 col-form-label">Footer Code</label>
                                        <div class="col-sm-10">
                                            <textarea rows="6" name="footer_code" class="form-control" placeholder="Custom CSS or JS Script"><?php echo html_entity_decode($settings_data['footer_code']); ?></textarea>
                                        </div>
                                    </div>
                                    
                                    <button type="submit" name="submit_general" class="btn btn-primary" style="min-width: 120px;">Save</button>
                                </form>
                            </div>
                            
                            <!--Web Settings-->
                            <div class="tab-pane fade" id="nsofts_setting_content_2" role="tabpanel" aria-labelledby="nsofts_setting_2" tabindex="0">
                                <form action="" name="settings_general" method="POST" enctype="multipart/form-data">
                                    <h4 class="mb-4">Web Settings</h4>
                                    <div class="mb-3 row">
                                        <label for="" class="col-sm-2 col-form-label">Song Dowload</label>
                                        <div class="col-sm-10">
                                            <div class="nsofts-switch d-flex align-items-center">
                                                <input type="checkbox" id="isSongDowload" name="isSongDowload" value="true" class="nsofts-switch__label" <?php if($settings_data['isSongDowload']=='true'){ echo 'checked'; }?>/>
                                                <label for="isSongDowload" class="nsofts-switch__label"></label>
                                            </div>
                                        </div>
                                    </div>
                                    <button type="submit" name="submit_set" class="btn btn-primary" style="min-width: 120px;">Save</button>
                                </form>
                            </div>
                            
                            <!--Contact us-->
                            <div class="tab-pane fade " id="nsofts_setting_content_3" role="tabpanel" aria-labelledby="nsofts_setting_3" tabindex="0">
                                <form action="" name="settings_contact_us" method="POST" enctype="multipart/form-data">
                                    <h4 class="mb-4">Contact us</h4>
                                    
                                    <div class="mb-3 row">
                                        <label for="" class="col-sm-2 col-form-label">Address</label>
                                        <div class="col-sm-10">
                                           <input type="text" name="address" id="address" value="<?= $settings_data['address']?>"  class="form-control">
                                        </div>
                                    </div>
                                    
                                    <div class="mb-3 row">
                                        <label for="" class="col-sm-2 col-form-label">Contact number</label>
                                        <div class="col-sm-10">
                                           <input type="text" name="contact_number" id="contact_number" value="<?= $settings_data['contact_number']?>"  class="form-control">
                                        </div>
                                    </div>
                                    
                                    <div class="mb-3 row">
                                        <label for="" class="col-sm-2 col-form-label">Contact email</label>
                                        <div class="col-sm-10">
                                           <input type="text" name="contact_email" id="contact_email" value="<?= $settings_data['contact_email']?>"  class="form-control">
                                        </div>
                                    </div>

                                    <div class="mb-3 row">
                                        <label for="" class="col-sm-2 col-form-label">iOS App Link</label>
                                        <div class="col-sm-10">
                                           <input type="text" name="ios_app_url" id="ios_app_url" value="<?= $settings_data['ios_app_url']?>"  class="form-control">
                                        </div>
                                    </div>
                                    
                                    <div class="mb-3 row">
                                        <label for="" class="col-sm-2 col-form-label">Facebook</label>
                                        <div class="col-sm-10">
                                           <input type="text" name="facebook_url" id="facebook_url" value="<?= $settings_data['facebook_url']?>"  class="form-control">
                                        </div>
                                    </div>
                                    
                                    <div class="mb-3 row">
                                        <label for="" class="col-sm-2 col-form-label">Twitter</label>
                                        <div class="col-sm-10">
                                           <input type="text" name="twitter_url" id="twitter_url" value="<?= $settings_data['twitter_url']?>"  class="form-control">
                                        </div>
                                    </div>
                                    
                                    <div class="mb-3 row">
                                        <label for="" class="col-sm-2 col-form-label">YouTube</label>
                                        <div class="col-sm-10">
                                           <input type="text" name="youtube_url" id="youtube_url" value="<?= $settings_data['youtube_url']?>"  class="form-control">
                                        </div>
                                    </div>
                                    
                                    <div class="mb-3 row">
                                        <label for="" class="col-sm-2 col-form-label">Instagram</label>
                                        <div class="col-sm-10">
                                           <input type="text" name="instagram_url" id="instagram_url" value="<?= $settings_data['instagram_url']?>"  class="form-control">
                                        </div>
                                    </div>
                                    <button type="submit" name="submit_contact_us" class="btn btn-primary" style="min-width: 120px;">Save</button>
                                </form>
                            </div>
                            
                            <!--About Us-->
                            <div class="tab-pane fade" id="nsofts_setting_content_4" role="tabpanel" aria-labelledby="nsofts_setting_4" tabindex="0">
                                <form action="" name="settings_about_us" method="POST" enctype="multipart/form-data">
                                    <h4 class="mb-4">About Us</h4>
                                    <div class="mb-3 row">
                                        <label for="" class="col-sm-2 col-form-label">Page status</label>
                                        <div class="col-sm-10">
                                            <div class="nsofts-switch d-flex align-items-center">
                                                <input type="checkbox" id="about_status" name="about_status" value="true" class="nsofts-switch__label" <?php if($settings_data['about_status']=='true'){ echo 'checked'; }?>/>
                                                <label for="about_status" class="nsofts-switch__label"></label>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="mb-3 row">
                                        <label for="" class="col-sm-2 col-form-label">Page title</label>
                                        <div class="col-sm-10">
                                           <input type="text" name="about_page_title" id="about_page_title" value="<?= $settings_data['about_page_title']?>"  class="form-control">
                                        </div>
                                    </div>
                                    <div>
                                        <textarea name="about_content" id="about_content" rows="5" class="nsofts-editor mb-4">
                                           <?php echo stripslashes($settings_data['about_content']); ?>
                                        </textarea>
                                    </div>
                                    <button type="submit" name="submit_about_us" class="btn btn-primary" style="min-width: 120px;">Save</button>
                                </form>
                            </div>
                            
                            <!--Privacy Policy-->
                            <div class="tab-pane fade" id="nsofts_setting_content_5" role="tabpanel" aria-labelledby="nsofts_setting_5" tabindex="0">
                                <form action="" name="submit_privacy" method="POST" enctype="multipart/form-data">
                                    <h4 class="mb-4">Privacy Policy</h4>
                                    <div class="mb-3 row">
                                        <label for="" class="col-sm-2 col-form-label">Page status</label>
                                        <div class="col-sm-10">
                                            <div class="nsofts-switch d-flex align-items-center">
                                                <input type="checkbox" id="privacy_page_status" name="privacy_page_status" value="true" class="nsofts-switch__label" <?php if($settings_data['privacy_page_status']=='true'){ echo 'checked'; }?>/>
                                                <label for="privacy_page_status" class="nsofts-switch__label"></label>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="mb-3 row">
                                        <label for="" class="col-sm-2 col-form-label">Page title</label>
                                        <div class="col-sm-10">
                                           <input type="text" name="privacy_page_title" id="privacy_page_title" value="<?= $settings_data['privacy_page_title']?>"  class="form-control">
                                        </div>
                                    </div>
                                    <div>
                                        <textarea name="privacy_content" id="privacy_content" rows="5" class="nsofts-editor mb-4">
                                           <?php echo stripslashes($settings_data['privacy_content']); ?>
                                        </textarea>
                                    </div>
                                    <button type="submit" name="submit_privacy" class="btn btn-primary" style="min-width: 120px;">Save</button>
                                </form>
                            </div>
                            
                            <!--Terms & Conditions-->
                            <div class="tab-pane fade" id="nsofts_setting_content_6" role="tabpanel" aria-labelledby="nsofts_setting_6" tabindex="0">
                               <form action="" name="settings_privacy"  method="POST" enctype="multipart/form-data">
                                    <h4 class="mb-4">Terms & Conditions</h4>
                                    <div class="mb-3 row">
                                        <label for="" class="col-sm-2 col-form-label">Page status</label>
                                        <div class="col-sm-10">
                                            <div class="nsofts-switch d-flex align-items-center">
                                                <input type="checkbox" id="terms_of_use_page_status" name="terms_of_use_page_status" value="true" class="nsofts-switch__label" <?php if($settings_data['terms_of_use_page_status']=='true'){ echo 'checked'; }?>/>
                                                <label for="terms_of_use_page_status" class="nsofts-switch__label"></label>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="mb-3 row">
                                        <label for="" class="col-sm-2 col-form-label">Page title</label>
                                        <div class="col-sm-10">
                                           <input type="text" name="terms_of_use_page_title" id="terms_of_use_page_title" value="<?= $settings_data['terms_of_use_page_title']?>"  class="form-control">
                                        </div>
                                    </div>
                                    <div>
                                        <textarea name="terms_of_use_content" id="terms_of_use_content" rows="5" class="nsofts-editor mb-4">
                                           <?php echo stripslashes($settings_data['terms_of_use_content']); ?>
                                        </textarea>
                                    </div>
                                    <button type="submit" name="submit_terms" class="btn btn-primary" style="min-width: 120px;">Save</button>
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