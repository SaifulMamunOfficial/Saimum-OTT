<?php $page_title="Settings Advertisement";
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
    
    $qry="SELECT * FROM tbl_settings where id='1'";
    $result=mysqli_query($mysqli,$qry);
    $settings_data=mysqli_fetch_assoc($result);
    
    
    if(isset($_POST['submit'])){
        
        $ad_network  =  cleanInput($_POST['ad_network']);
        
        if($ad_network == 'admob'){
            
            $data = array(
                'ad_status'  =>  ($_POST['ad_status']) ? 'true' : 'false',
                'ad_network'  =>  cleanInput($_POST['ad_network']),
                
                'admob_publisher_id'  =>  $_POST['admob_publisher_id'],
                'admob_banner_unit_id'  =>  $_POST['admob_banner_unit_id'],
                'admob_interstitial_unit_id'  =>  $_POST['admob_interstitial_unit_id'],
                'admob_native_unit_id'  =>  $_POST['admob_native_unit_id'],
                'admob_app_open_ad_unit_id'  =>  $_POST['admob_app_open_ad_unit_id'],
                'admob_reward_ad_unit_id'  =>  $_POST['admob_reward_ad_unit_id'],
                
                'interstital_ad_click'  =>  $_POST['interstital_ad_click'],
                'native_position'  =>  $_POST['native_position'],
                'reward_credit'  =>  $_POST['reward_credit'],
                
                'banner_home'  =>  ($_POST['banner_home']) ? 'true' : 'false',
                'banner_post_details'  =>  ($_POST['banner_post_details']) ? 'true' : 'false',
                'banner_category_details'  =>  ($_POST['banner_category_details']) ? 'true' : 'false',
                'banner_search'  =>  ($_POST['banner_search']) ? 'true' : 'false',
                
                'interstitial_post_list'  =>  ($_POST['interstitial_post_list']) ? 'true' : 'false',
                
                'native_ad_post_list'  =>  ($_POST['native_ad_post_list']) ? 'true' : 'false',
                'native_ad_category_list'  =>  ($_POST['native_ad_category_list']) ? 'true' : 'false',
                
                'app_open_ad_on_start'  =>  ($_POST['app_open_ad_on_start']) ? 'true' : 'false',
                'reward_ad_on'  =>  ($_POST['reward_ad_on']) ? 'true' : 'false'
            );
            
        } else if($ad_network == 'startapp'){
            
            $data = array(
                'ad_status'  =>  ($_POST['ad_status']) ? 'true' : 'false',
                'ad_network'  =>  cleanInput($_POST['ad_network']),
                
                'startapp_app_id'  =>  $_POST['startapp_app_id'],
                
                'interstital_ad_click'  =>  $_POST['interstital_ad_click'],
                'native_position'  =>  $_POST['native_position'],
                'reward_credit'  =>  $_POST['reward_credit'],
                
                'banner_home'  =>  ($_POST['banner_home']) ? 'true' : 'false',
                'banner_post_details'  =>  ($_POST['banner_post_details']) ? 'true' : 'false',
                'banner_category_details'  =>  ($_POST['banner_category_details']) ? 'true' : 'false',
                'banner_search'  =>  ($_POST['banner_search']) ? 'true' : 'false',
                
                'interstitial_post_list'  =>  ($_POST['interstitial_post_list']) ? 'true' : 'false',
                
                'native_ad_post_list'  =>  ($_POST['native_ad_post_list']) ? 'true' : 'false',
                'native_ad_category_list'  =>  ($_POST['native_ad_category_list']) ? 'true' : 'false',
                
                'app_open_ad_on_start'  =>  ($_POST['app_open_ad_on_start']) ? 'true' : 'false',
                'reward_ad_on'  =>  ($_POST['reward_ad_on']) ? 'true' : 'false'
            );
            
        } else if($ad_network == 'unity'){
            
            $data = array(
                'ad_status'  =>  ($_POST['ad_status']) ? 'true' : 'false',
                'ad_network'  =>  cleanInput($_POST['ad_network']),
                
                'unity_game_id'  =>  $_POST['unity_game_id'],
                'unity_banner_placement_id'  =>  $_POST['unity_banner_placement_id'],
                'unity_interstitial_placement_id'  =>  $_POST['unity_interstitial_placement_id'],
                'unity_reward_ad_unit_id'  =>  $_POST['unity_reward_ad_unit_id'],
                
                'interstital_ad_click'  =>  $_POST['interstital_ad_click'],
                'native_position'  =>  $_POST['native_position'],
                'reward_credit'  =>  $_POST['reward_credit'],
                
                'banner_home'  =>  ($_POST['banner_home']) ? 'true' : 'false',
                'banner_post_details'  =>  ($_POST['banner_post_details']) ? 'true' : 'false',
                'banner_category_details'  =>  ($_POST['banner_category_details']) ? 'true' : 'false',
                'banner_search'  =>  ($_POST['banner_search']) ? 'true' : 'false',
                
                'interstitial_post_list'  =>  ($_POST['interstitial_post_list']) ? 'true' : 'false',
                
                'native_ad_post_list'  =>  ($_POST['native_ad_post_list']) ? 'true' : 'false',
                'native_ad_category_list'  =>  ($_POST['native_ad_category_list']) ? 'true' : 'false',
                
                'app_open_ad_on_start'  =>  ($_POST['app_open_ad_on_start']) ? 'true' : 'false',
                'reward_ad_on'  =>  ($_POST['reward_ad_on']) ? 'true' : 'false'
            );
            
        } else if($ad_network == 'applovin'){
            
            $data = array(
                'ad_status'  =>  ($_POST['ad_status']) ? 'true' : 'false',
                'ad_network'  =>  cleanInput($_POST['ad_network']),
                
                'applovin_banner_ad_unit_id'  =>  $_POST['applovin_banner_ad_unit_id'],
                'applovin_interstitial_ad_unit_id'  =>  $_POST['applovin_interstitial_ad_unit_id'],
                'applovin_native_ad_manual_unit_id'  =>  $_POST['applovin_native_ad_manual_unit_id'],
                'applovin_app_open_ad_unit_id'  =>  $_POST['applovin_app_open_ad_unit_id'],
                'applovin_reward_ad_unit_id'  =>  $_POST['applovin_reward_ad_unit_id'],
                
                'interstital_ad_click'  =>  $_POST['interstital_ad_click'],
                'native_position'  =>  $_POST['native_position'],
                'reward_credit'  =>  $_POST['reward_credit'],
                
                'banner_home'  =>  ($_POST['banner_home']) ? 'true' : 'false',
                'banner_post_details'  =>  ($_POST['banner_post_details']) ? 'true' : 'false',
                'banner_category_details'  =>  ($_POST['banner_category_details']) ? 'true' : 'false',
                'banner_search'  =>  ($_POST['banner_search']) ? 'true' : 'false',
                
                'interstitial_post_list'  =>  ($_POST['interstitial_post_list']) ? 'true' : 'false',
                
                'native_ad_post_list'  =>  ($_POST['native_ad_post_list']) ? 'true' : 'false',
                'native_ad_category_list'  =>  ($_POST['native_ad_category_list']) ? 'true' : 'false',
                
                'app_open_ad_on_start'  =>  ($_POST['app_open_ad_on_start']) ? 'true' : 'false',
                'reward_ad_on'  =>  ($_POST['reward_ad_on']) ? 'true' : 'false'
            );
            
        } else if($ad_network == 'ironsource'){
            
            $data = array(
                'ad_status'  =>  ($_POST['ad_status']) ? 'true' : 'false',
                'ad_network'  =>  cleanInput($_POST['ad_network']),
                
                'ironsource_app_key'  =>  $_POST['ironsource_app_key'],
                
                'interstital_ad_click'  =>  $_POST['interstital_ad_click'],
                'native_position'  =>  $_POST['native_position'],
                'reward_credit'  =>  $_POST['reward_credit'],
                
                'banner_home'  =>  ($_POST['banner_home']) ? 'true' : 'false',
                'banner_post_details'  =>  ($_POST['banner_post_details']) ? 'true' : 'false',
                'banner_category_details'  =>  ($_POST['banner_category_details']) ? 'true' : 'false',
                'banner_search'  =>  ($_POST['banner_search']) ? 'true' : 'false',
                
                'interstitial_post_list'  =>  ($_POST['interstitial_post_list']) ? 'true' : 'false',
                
                'native_ad_post_list'  =>  ($_POST['native_ad_post_list']) ? 'true' : 'false',
                'native_ad_category_list'  =>  ($_POST['native_ad_category_list']) ? 'true' : 'false',
                
                'app_open_ad_on_start'  =>  ($_POST['app_open_ad_on_start']) ? 'true' : 'false',
                'reward_ad_on'  =>  ($_POST['reward_ad_on']) ? 'true' : 'false'
            );
            
        } else if($ad_network == 'meta'){
            
            $data = array(
                'ad_status'  =>  ($_POST['ad_status']) ? 'true' : 'false',
                'ad_network'  =>  cleanInput($_POST['ad_network']),
                
                'mata_banner_ad_unit_id'  =>  $_POST['mata_banner_ad_unit_id'],
                'mata_interstitial_ad_unit_id'  =>  $_POST['mata_interstitial_ad_unit_id'],
                'mata_native_ad_manual_unit_id'  =>  $_POST['mata_native_ad_manual_unit_id'],
                
                'interstital_ad_click'  =>  $_POST['interstital_ad_click'],
                'native_position'  =>  $_POST['native_position'],
                'reward_credit'  =>  $_POST['reward_credit'],
                
                'banner_home'  =>  ($_POST['banner_home']) ? 'true' : 'false',
                'banner_post_details'  =>  ($_POST['banner_post_details']) ? 'true' : 'false',
                'banner_category_details'  =>  ($_POST['banner_category_details']) ? 'true' : 'false',
                'banner_search'  =>  ($_POST['banner_search']) ? 'true' : 'false',
                
                'interstitial_post_list'  =>  ($_POST['interstitial_post_list']) ? 'true' : 'false',
                
                'native_ad_post_list'  =>  ($_POST['native_ad_post_list']) ? 'true' : 'false',
                'native_ad_category_list'  =>  ($_POST['native_ad_category_list']) ? 'true' : 'false',
                
                'app_open_ad_on_start'  =>  ($_POST['app_open_ad_on_start']) ? 'true' : 'false',
                'reward_ad_on'  =>  ($_POST['reward_ad_on']) ? 'true' : 'false'
            );
            
        } else if($ad_network == 'yandex'){
            
            $data = array(
                'ad_status'  =>  ($_POST['ad_status']) ? 'true' : 'false',
                'ad_network'  =>  cleanInput($_POST['ad_network']),
                
                'yandex_banner_ad_unit_id'  =>  $_POST['yandex_banner_ad_unit_id'],
                'yandex_interstitial_ad_unit_id'  =>  $_POST['yandex_interstitial_ad_unit_id'],
                'yandex_native_ad_manual_unit_id'  =>  $_POST['yandex_native_ad_manual_unit_id'],
                'yandex_app_open_ad_unit_id'  =>  $_POST['yandex_app_open_ad_unit_id'],
                
                'interstital_ad_click'  =>  $_POST['interstital_ad_click'],
                'native_position'  =>  $_POST['native_position'],
                'reward_credit'  =>  $_POST['reward_credit'],
                
                'banner_home'  =>  ($_POST['banner_home']) ? 'true' : 'false',
                'banner_post_details'  =>  ($_POST['banner_post_details']) ? 'true' : 'false',
                'banner_category_details'  =>  ($_POST['banner_category_details']) ? 'true' : 'false',
                'banner_search'  =>  ($_POST['banner_search']) ? 'true' : 'false',
                
                'interstitial_post_list'  =>  ($_POST['interstitial_post_list']) ? 'true' : 'false',
                
                'native_ad_post_list'  =>  ($_POST['native_ad_post_list']) ? 'true' : 'false',
                'native_ad_category_list'  =>  ($_POST['native_ad_category_list']) ? 'true' : 'false',
                
                'app_open_ad_on_start'  =>  ($_POST['app_open_ad_on_start']) ? 'true' : 'false',
                'reward_ad_on'  =>  ($_POST['reward_ad_on']) ? 'true' : 'false'
            );
            
        } else if($ad_network == 'wortise'){
            
            $data = array(
                'ad_status'  =>  ($_POST['ad_status']) ? 'true' : 'false',
                'ad_network'  =>  cleanInput($_POST['ad_network']),
                
                'wortise_app_id'  =>  $_POST['wortise_app_id'],
                'wortise_banner_unit_id'  =>  $_POST['wortise_banner_unit_id'],
                'wortise_interstitial_unit_id'  =>  $_POST['wortise_interstitial_unit_id'],
                'wortise_native_unit_id'  =>  $_POST['wortise_native_unit_id'],
                'wortise_app_open_unit_id'  =>  $_POST['wortise_app_open_unit_id'],
                'wortise_reward_ad_unit_id'  =>  $_POST['wortise_reward_ad_unit_id'],
                
                'interstital_ad_click'  =>  $_POST['interstital_ad_click'],
                'native_position'  =>  $_POST['native_position'],
                'reward_credit'  =>  $_POST['reward_credit'],
                
                'banner_home'  =>  ($_POST['banner_home']) ? 'true' : 'false',
                'banner_post_details'  =>  ($_POST['banner_post_details']) ? 'true' : 'false',
                'banner_category_details'  =>  ($_POST['banner_category_details']) ? 'true' : 'false',
                'banner_search'  =>  ($_POST['banner_search']) ? 'true' : 'false',
                
                'interstitial_post_list'  =>  ($_POST['interstitial_post_list']) ? 'true' : 'false',
                
                'native_ad_post_list'  =>  ($_POST['native_ad_post_list']) ? 'true' : 'false',
                'native_ad_category_list'  =>  ($_POST['native_ad_category_list']) ? 'true' : 'false',
                
                'app_open_ad_on_start'  =>  ($_POST['app_open_ad_on_start']) ? 'true' : 'false',
                'reward_ad_on'  =>  ($_POST['reward_ad_on']) ? 'true' : 'false'
            );
            
        } else {
            
            $data = array(
                'ad_status'  =>  ($_POST['ad_status']) ? 'true' : 'false',
                'ad_network'  =>  cleanInput($_POST['ad_network']),
                
                'interstital_ad_click'  =>  $_POST['interstital_ad_click'],
                'native_position'  =>  $_POST['native_position'],
                'reward_credit'  =>  $_POST['reward_credit'],
                
                'banner_home'  =>  ($_POST['banner_home']) ? 'true' : 'false',
                'banner_post_details'  =>  ($_POST['banner_post_details']) ? 'true' : 'false',
                'banner_category_details'  =>  ($_POST['banner_category_details']) ? 'true' : 'false',
                'banner_search'  =>  ($_POST['banner_search']) ? 'true' : 'false',
                
                'interstitial_post_list'  =>  ($_POST['interstitial_post_list']) ? 'true' : 'false',
                
                'native_ad_post_list'  =>  ($_POST['native_ad_post_list']) ? 'true' : 'false',
                'native_ad_category_list'  =>  ($_POST['native_ad_category_list']) ? 'true' : 'false',
                
                'app_open_ad_on_start'  =>  ($_POST['app_open_ad_on_start']) ? 'true' : 'false',
                'reward_ad_on'  =>  ($_POST['reward_ad_on']) ? 'true' : 'false'
            );
        }
        
        $settings_edit=Update('tbl_settings', $data, "WHERE id = '1'");
        $_SESSION['msg']="11";
        $_SESSION['class'] = "success";
        header( "Location:settings_ads.php");
        exit;
    }
?>
<style>
    .col-form-label {
        color: var(--ns-gray-300);
    }
</style>

<!-- Start: main -->
<main id="nsofts_main">
    <div class="nsofts-container">
        <nav aria-label="breadcrumb">
            <ol class="breadcrumb align-items-center">
                <li class="breadcrumb-item d-inline-flex"><a href="dashboard.php"><i class="ri-home-4-fill"></i></a></li>
                <li class="breadcrumb-item d-inline-flex active" aria-current="page"><?php echo (isset($page_title)) ? $page_title : "" ?></li>
            </ol>
        </nav>
        
        <form action="" name="advertisement" method="POST" enctype="multipart/form-data">
        <div class="row clearfix">
            <div class="col-lg-8 col-md-8 col-sm-12 col-xs-12">
                <div class="card">
                    
                    <div class="card-top d-md-inline-flex align-items-center justify-content-between py-3 px-4">
                        <h5  class="d-inline-flex align-items-center fw-semibold m-0">MANAGE ADS</h5>
                        <div class="d-flex mt-2 mt-md-0">
                            <button type="submit" name="submit" class="btn btn-primary d-inline-flex align-items-center justify-content-center" style="min-width: 120px;">
                                <i class="ri-refresh-line"></i>
                                <span class="ps-1 text-nowrap d-none d-sm-block">UPDATE</span>
                            </button>
                        </div>
                    </div>
                    <div class="card-body p-0">
                        
                        <div class="p-4">
                            <div class="mb-3 row">
                                <label for="" class="col-sm-2 col-form-label">Ad Status</label>
                                <div class="col-sm-10">
                                    <div class="nsofts-switch d-flex align-items-center">
                                        <input type="checkbox" id="ad_status" name="ad_status" value="true" class="nsofts-switch__label" <?php if($settings_data['ad_status']=='true'){ echo 'checked'; }?>/>
                                        <label for="ad_status" class="nsofts-switch__label"></label>
                                    </div>
                                </div>
                            </div>
                            
                            <div class="mb-3">
                                <label for="" class="col-form-label">Primary Ad Network</label>
                                <select name="ad_network" id="ad_network" class="nsofts-select " required>
                                    <option value="admob" <?php if ($settings_data['ad_network'] == 'admob') { ?>selected<?php } ?>>AdMob</option>
                                    <option value="wortise" <?php if ($settings_data['ad_network'] == 'wortise') { ?>selected<?php } ?>>Wortise</option>
                                    <option value="startapp" <?php if ($settings_data['ad_network'] == 'startapp') { ?>selected<?php } ?>>StartApp</option>
                                    <option value="unity" <?php if ($settings_data['ad_network'] == 'unity') { ?>selected<?php } ?>>Unity Ads</option>
                                    <option value="applovin" <?php if ($settings_data['ad_network'] == 'applovin') { ?>selected<?php } ?>>AppLovin MAX</option>
                                    <option value="ironsource" <?php if ($settings_data['ad_network'] == 'ironsource') { ?>selected<?php } ?>>ironSource</option>
                                    <option value="meta" <?php if ($settings_data['ad_network'] == 'meta') { ?>selected<?php } ?>>Meta Audience Network</option>
                                    <option value="yandex" <?php if ($settings_data['ad_network'] == 'yandex') { ?>selected<?php } ?>>Yandex</option>
                                </select>
                            </div>
                            
                            <div class="admob_ads" style="display: none">
                                <div class="mb-2">
                                    <label for="" class="col-form-label">AdMob Publisher ID</label>
                                    <input type="text" name="admob_publisher_id" id="admob_publisher_id" value="<?php echo $settings_data['admob_publisher_id']; ?>" class="form-control">
                                </div>
                                <div class="mb-2">
                                    <label for="" class="col-form-label">AdMob Banner Ad Unit ID</label>
                                    <input type="text" name="admob_banner_unit_id" id="admob_banner_unit_id" value="<?php echo $settings_data['admob_banner_unit_id']; ?>" class="form-control">
                                </div>
                                <div class="mb-2">
                                    <label for="" class="col-form-label">AdMob Interstitial Ad Unit ID</label>
                                    <input type="text" name="admob_interstitial_unit_id" id="admob_interstitial_unit_id" value="<?php echo $settings_data['admob_interstitial_unit_id']; ?>" class="form-control">
                                </div>
                                <div class="mb-2">
                                    <label for="" class="col-form-label">AdMob Native Ad Unit ID</label>
                                    <input type="text" name="admob_native_unit_id" id="admob_native_unit_id" value="<?php echo $settings_data['admob_native_unit_id']; ?>" class="form-control">
                                </div>
                                <div class="mb-2">
                                    <label for="" class="col-form-label">AdMob App Open Ad Unit ID</label>
                                    <input type="text" name="admob_app_open_ad_unit_id" id="admob_app_open_ad_unit_id" value="<?php echo $settings_data['admob_app_open_ad_unit_id']; ?>" class="form-control">
                                </div>
                                <div class="mb-2">
                                    <label for="" class="col-form-label">AdMob Reward Ad Unit ID</label>
                                    <input type="text" name="admob_reward_ad_unit_id" id="admob_reward_ad_unit_id" value="<?php echo $settings_data['admob_reward_ad_unit_id']; ?>" class="form-control">
                                </div>
                            </div>
                            
                            <div class="startapp_ads" style="display: none">
                                <div class="mb-2">
                                    <label for="" class="col-form-label">StartApp App ID</label>
                                    <input type="text" name="startapp_app_id" id="startapp_app_id" value="<?php echo $settings_data['startapp_app_id']; ?>" class="form-control">
                                </div>
                            </div>
                            
                            <div class="unity_ads" style="display: none">
                                <div class="mb-2">
                                    <label for="" class="col-form-label">Unity Game ID</label>
                                    <input type="text" name="unity_game_id" id="unity_game_id" value="<?php echo $settings_data['unity_game_id']; ?>" class="form-control">
                                </div>
                                <div class="mb-2">
                                    <label for="" class="col-form-label">Unity Banner Ad Placement ID</label>
                                    <input type="text" name="unity_banner_placement_id" id="unity_banner_placement_id" value="<?php echo $settings_data['unity_banner_placement_id']; ?>" class="form-control">
                                </div>
                                <div class="mb-2">
                                    <label for="" class="col-form-label">Unity Interstitial Ad Placement ID</label>
                                    <input type="text" name="unity_interstitial_placement_id" id="unity_interstitial_placement_id" value="<?php echo $settings_data['unity_interstitial_placement_id']; ?>" class="form-control">
                                </div>
                                <div class="mb-2">
                                    <label for="" class="col-form-label">Unity Reward Ad Unit ID</label>
                                    <input type="text" name="unity_reward_ad_unit_id" id="unity_reward_ad_unit_id" value="<?php echo $settings_data['unity_reward_ad_unit_id']; ?>" class="form-control">
                                </div>
                            </div>

                            <div class="applovin_ads" style="display: none">
                                <div class="mb-2">
                                    <label for="" class="col-form-label">AppLovin Banner Ad ID</label>
                                    <input type="text" name="applovin_banner_ad_unit_id" id="applovin_banner_ad_unit_id" value="<?php echo $settings_data['applovin_banner_ad_unit_id']; ?>" class="form-control">
                                </div>
                                <div class="mb-2">
                                    <label for="" class="col-form-label">AppLovin Interstitial Ad ID</label>
                                    <input type="text" name="applovin_interstitial_ad_unit_id" id="applovin_interstitial_ad_unit_id" value="<?php echo $settings_data['applovin_interstitial_ad_unit_id']; ?>" class="form-control">
                                </div>
                                <div class="mb-2">
                                    <label for="" class="col-form-label">AppLovin Native Ad (Manual) ID</label>
                                    <input type="text" name="applovin_native_ad_manual_unit_id" id="applovin_native_ad_manual_unit_id" value="<?php echo $settings_data['applovin_native_ad_manual_unit_id']; ?>" class="form-control">
                                </div>
                                <div class="mb-2">
                                    <label for="" class="col-form-label">AppLovin App Open Ad ID</label>
                                    <input type="text" name="applovin_app_open_ad_unit_id" id="applovin_app_open_ad_unit_id" value="<?php echo $settings_data['applovin_app_open_ad_unit_id']; ?>" class="form-control">
                                </div>
                                <div class="mb-2">
                                    <label for="" class="col-form-label">AppLovin Reward Ad Unit ID</label>
                                    <input type="text" name="applovin_reward_ad_unit_id" id="applovin_reward_ad_unit_id" value="<?php echo $settings_data['applovin_reward_ad_unit_id']; ?>" class="form-control">
                                </div>
                            </div>
                            
                            <div class="ironsource_ads" style="display: none">
                                <div class="mb-2">
                                    <label for="" class="col-form-label">ironSource App Key</label>
                                    <input type="text" name="ironsource_app_key" id="ironsource_app_key" value="<?php echo $settings_data['ironsource_app_key']; ?>" class="form-control">
                                </div>
                            </div>

                            <div class="meta_ads" style="display: none">
                                <div class="mb-2">
                                    <label for="" class="col-form-label">Audience Network Banner Placement Name</label>
                                    <input type="text" name="mata_banner_ad_unit_id" id="mata_banner_ad_unit_id" value="<?php echo $settings_data['mata_banner_ad_unit_id']; ?>" class="form-control">
                                </div>
                                <div class="mb-2">
                                    <label for="" class="col-form-label">Audience Network Interstitial Placement Name</label>
                                    <input type="text" name="mata_interstitial_ad_unit_id" id="mata_interstitial_ad_unit_id" value="<?php echo $settings_data['mata_interstitial_ad_unit_id']; ?>" class="form-control">
                                </div>
                                <div class="mb-2">
                                    <label for="" class="col-form-label">Audience Network Native Placement Name</label>
                                    <input type="text" name="mata_native_ad_manual_unit_id" id="mata_native_ad_manual_unit_id" value="<?php echo $settings_data['mata_native_ad_manual_unit_id']; ?>" class="form-control">
                                </div>
                            </div>
                            
                            <div class="yandex_ads" style="display: none">
                                <div class="mb-2">
                                    <label for="" class="col-form-label">Yandex Banner Ad Unit ID</label>
                                    <input type="text" name="yandex_banner_ad_unit_id" id="yandex_banner_ad_unit_id" value="<?php echo $settings_data['yandex_banner_ad_unit_id']; ?>" class="form-control">
                                </div>
                                <div class="mb-2">
                                    <label for="" class="col-form-label">Yandex Interstitial Ad Unit ID</label>
                                    <input type="text" name="yandex_interstitial_ad_unit_id" id="yandex_interstitial_ad_unit_id" value="<?php echo $settings_data['yandex_interstitial_ad_unit_id']; ?>" class="form-control">
                                </div>
                                <div class="mb-2">
                                    <label for="" class="col-form-label">Yandex Native Ad Unit ID</label>
                                    <input type="text" name="yandex_native_ad_manual_unit_id" id="yandex_native_ad_manual_unit_id" value="<?php echo $settings_data['yandex_native_ad_manual_unit_id']; ?>" class="form-control">
                                </div>
                                <div class="mb-2">
                                    <label for="" class="col-form-label">Yandex App Open Ad Unit ID</label>
                                    <input type="text" name="yandex_app_open_ad_unit_id" id="yandex_app_open_ad_unit_id" value="<?php echo $settings_data['yandex_app_open_ad_unit_id']; ?>" class="form-control">
                                </div>
                            </div>
                            
                            <div class="wortise_ads" style="display: none">
                                <div class="mb-2">
                                    <label for="" class="col-form-label">Wortise App ID</label>
                                    <input type="text" name="wortise_app_id" id="wortise_app_id" value="<?php echo $settings_data['wortise_app_id']; ?>" class="form-control">
                                </div>
                                <div class="mb-2">
                                    <label for="" class="col-form-label">Wortise Banner Ad Unit ID</label>
                                    <input type="text" name="wortise_banner_unit_id" id="wortise_banner_unit_id" value="<?php echo $settings_data['wortise_banner_unit_id']; ?>" class="form-control">
                                </div>
                                <div class="mb-2">
                                    <label for="" class="col-form-label">Wortise Interstitial Ad Unit ID</label>
                                    <input type="text" name="wortise_interstitial_unit_id" id="wortise_interstitial_unit_id" value="<?php echo $settings_data['wortise_interstitial_unit_id']; ?>" class="form-control">
                                </div>
                                <div class="mb-2">
                                    <label for="" class="col-form-label">Wortise Native Ad Unit ID</label>
                                    <input type="text" name="wortise_native_unit_id" id="wortise_native_unit_id" value="<?php echo $settings_data['wortise_native_unit_id']; ?>" class="form-control">
                                </div>
                                <div class="mb-2">
                                    <label for="" class="col-form-label">Wortise App Open Ad Unit ID</label>
                                    <input type="text" name="wortise_app_open_unit_id" id="wortise_app_open_unit_id" value="<?php echo $settings_data['wortise_app_open_unit_id']; ?>" class="form-control">
                                </div>
                                <div class="mb-2">
                                    <label for="" class="col-form-label">Wortise Reward Ad Unit ID</label>
                                    <input type="text" name="wortise_reward_ad_unit_id" id="wortise_reward_ad_unit_id" value="<?php echo $settings_data['wortise_reward_ad_unit_id']; ?>" class="form-control">
                                </div>
                            </div>
                            
                        </div>
                    </div>
                </div>
                
                <div class="card mt-4">
                    <div class="card-body p-0">
                        <div class="card-top p-3">
                            <h5 class="fw-semibold ps-2 lh-1 m-0">GLOBAL CONFIGURATION</h5>
                        </div>
                        
                        <div class="p-4 pt-1">
                            <div class="mb-1">
                                <label for="" class="col-form-label">Interstitial Ad Interval</label>
                                <input type="text" name="interstital_ad_click" id="interstital_ad_click" value="<?php echo $settings_data['interstital_ad_click']; ?>" class="form-control ads_click">
                            </div>
                            <div class="mb-2">
                                <label for="" class="col-form-label">Native Ad Index</label>
                                <input type="text" name="native_position" id="native_position" value="<?php echo $settings_data['native_position']; ?>" class="form-control ads_click">
                            </div>
                            <div class="mb-2">
                                <label for="" class="col-form-label">One Reward Ad Credit</label>
                                <input type="text" name="reward_credit" id="reward_credit" value="<?php echo $settings_data['reward_credit']; ?>" class="form-control ads_click">
                            </div>
                        </div>
                    </div>
                </div>

            </div>
            <div class="col-lg-4 col-md-4 col-sm-12 col-xs-12">
                <div class="card">
                    <div class="card-body p-0">
                        
                        <div class="card-top p-3">
                            <h5 class="fw-semibold ps-2 lh-1 m-0">ADS PLACEMENT</h5>
                        </div>
                        
                        <div class="p-4">
                            <label class="col-form-label"> Enable or Disable Certain Ads Format Separately</label>
                            
                            <div class="row">
                                <div class="col-sm-3 mt-2">
                                    <div class="nsofts-switch d-flex align-items-center">
                                        <input type="checkbox" id="banner_home" name="banner_home" value="true" class="cbx hidden" <?php if($settings_data['banner_home']=='true'){ echo 'checked'; }?>/>
                                        <label for="banner_home" class="nsofts-switch__label"></label>
                                    </div>
                                </div>
                                <label for="" class="col-sm col-form-label">Banner Ad on Home Page</label>
                            </div>
                            
                            <div class="row">
                                <div class="col-sm-3 mt-2">
                                    <div class="nsofts-switch d-flex align-items-center">
                                        <input type="checkbox" id="banner_post_details" name="banner_post_details" value="true" class="cbx hidden" <?php if($settings_data['banner_post_details']=='true'){ echo 'checked'; }?>/>
                                        <label for="banner_post_details" class="nsofts-switch__label"></label>
                                    </div>
                                </div>
                                <label for="" class="col-sm col-form-label">Banner Ad on Post Details</label>
                            </div>
                            
                            <div class="row">
                                <div class="col-sm-3 mt-2">
                                    <div class="nsofts-switch d-flex align-items-center">
                                        <input type="checkbox" id="banner_category_details" name="banner_category_details" value="true" class="cbx hidden" <?php if($settings_data['banner_category_details']=='true'){ echo 'checked'; }?>/>
                                        <label for="banner_category_details" class="nsofts-switch__label"></label>
                                    </div>
                                </div>
                                <label for="" class="col-sm col-form-label">Banner Ad on Category Details</label>
                            </div>
                            
                            <div class="row">
                                <div class="col-sm-3 mt-2">
                                    <div class="nsofts-switch d-flex align-items-center">
                                        <input type="checkbox" id="banner_search" name="banner_search" value="true" class="cbx hidden" <?php if($settings_data['banner_search']=='true'){ echo 'checked'; }?>/>
                                        <label for="banner_search" class="nsofts-switch__label"></label>
                                    </div>
                                </div>
                                <label for="" class="col-sm col-form-label">Banner Ad on Search Page</label>
                            </div>
                            
                            <div class="mb-4"></div>
    
                            <div class="row">
                                <div class="col-sm-3 mt-2">
                                    <div class="nsofts-switch d-flex align-items-center">
                                        <input type="checkbox" id="interstitial_post_list" name="interstitial_post_list" value="true" class="cbx hidden" <?php if($settings_data['interstitial_post_list']=='true'){ echo 'checked'; }?>/>
                                        <label for="interstitial_post_list" class="nsofts-switch__label"></label>
                                    </div>
                                </div>
                                <label for="" class="col-sm col-form-label">Interstitial Ad on Post List</label>
                            </div>
                            
                            <div class="mb-4"></div>
                            
                            <div class="row">
                                <div class="col-sm-3 mt-2">
                                    <div class="nsofts-switch d-flex align-items-center">
                                        <input type="checkbox" id="native_ad_post_list" name="native_ad_post_list" value="true" class="cbx hidden" <?php if($settings_data['native_ad_post_list']=='true'){ echo 'checked'; }?>/>
                                        <label for="native_ad_post_list" class="nsofts-switch__label"></label>
                                    </div>
                                </div>
                                <label for="" class="col-sm col-form-label">Native Ad on Post List</label>
                            </div>
                            <div class="row">
                                <div class="col-sm-3 mt-2">
                                    <div class="nsofts-switch d-flex align-items-center">
                                        <input type="checkbox" id="native_ad_category_list" name="native_ad_category_list" value="true" class="cbx hidden" <?php if($settings_data['native_ad_category_list']=='true'){ echo 'checked'; }?>/>
                                        <label for="native_ad_category_list" class="nsofts-switch__label"></label>
                                    </div>
                                </div>
                                <label for="" class="col-sm col-form-label">Native Ad on Category List</label>
                            </div>
                            
                            <div class="mb-4"></div>
                            
                            <div class="row">
                                <div class="col-sm-3 mt-2">
                                    <div class="nsofts-switch d-flex align-items-center">
                                        <input type="checkbox" id="app_open_ad_on_start" name="app_open_ad_on_start" value="true" class="cbx hidden" <?php if($settings_data['app_open_ad_on_start']=='true'){ echo 'checked'; }?>/>
                                        <label for="app_open_ad_on_start" class="nsofts-switch__label"></label>
                                    </div>
                                </div>
                                <label for="" class="col-sm col-form-label">App Open Ad</label>
                            </div>
                            
                            <div class="mb-4"></div>
                            
                            <div class="row">
                                <div class="col-sm-3 mt-2">
                                    <div class="nsofts-switch d-flex align-items-center">
                                        <input type="checkbox" id="reward_ad_on" name="reward_ad_on" value="true" class="cbx hidden" <?php if($settings_data['reward_ad_on']=='true'){ echo 'checked'; }?>/>
                                        <label for="reward_ad_on" class="nsofts-switch__label"></label>
                                    </div>
                                </div>
                                <label for="" class="col-sm col-form-label">Reward Ad</label>
                            </div>

                        </div>
                    </div>
                </div>
            </div>
        </div>
        </form>
    </div>
</main>
<!-- End: main -->
    
<?php include("includes/footer.php");?>
<script type="text/javascript">
$(document).ready(function(e) {
    var adType = $("select[name='ad_network']").val();
    if (adType === 'admob') {
        $(".admob_ads").show();
    } else {
        $(".admob_ads").hide();
    }
    
    if (adType === 'startapp') {
        $(".startapp_ads").show();
    } else {
        $(".startapp_ads").hide();
    }
    
    if (adType === 'unity') {
        $(".unity_ads").show();
    } else {
        $(".unity_ads").hide();
    }
    
    if (adType === 'applovin') {
        $(".applovin_ads").show();
    } else {
        $(".applovin_ads").hide();
    }
    
    if (adType === 'ironsource') {
        $(".ironsource_ads").show();
    } else {
        $(".ironsource_ads").hide();
    }
    
    if (adType === 'meta') {
        $(".meta_ads").show();
    } else {
        $(".meta_ads").hide();
    }
    
    if (adType === 'yandex') {
        $(".yandex_ads").show();
    } else {
        $(".yandex_ads").hide();
    }
    
    if (adType === 'wortise') {
        $(".wortise_ads").show();
    } else {
        $(".wortise_ads").hide();
    }
    
});

$("select[name='ad_network']").change(function(e) {
    if ($(this).val() === 'admob') {
        $(".admob_ads").show();
    } else {
        $(".admob_ads").hide();
    }
    
    if ($(this).val() === 'startapp') {
        $(".startapp_ads").show();
    } else {
        $(".startapp_ads").hide();
    }
    
    if ($(this).val() === 'unity') {
        $(".unity_ads").show();
    } else {
        $(".unity_ads").hide();
    }
    
    if ($(this).val() === 'applovin') {
        $(".applovin_ads").show();
    } else {
        $(".applovin_ads").hide();
    }
    
    if ($(this).val() === 'ironsource') {
        $(".ironsource_ads").show();
    } else {
        $(".ironsource_ads").hide();
    }
    
    if ($(this).val() === 'meta') {
        $(".meta_ads").show();
    } else {
        $(".meta_ads").hide();
    }
    
    if ($(this).val() === 'yandex') {
        $(".yandex_ads").show();
    } else {
        $(".yandex_ads").hide();
    }
    
    if ($(this).val() === 'wortise') {
        $(".wortise_ads").show();
    } else {
        $(".wortise_ads").hide();
    }
});

$("input[name='native_position']").blur(function(e) {
    if ($(this).val() == '' || parseInt($(this).val()) <= 0) {
      $(this).val('1');
    }
});

$("#interstital_ad_click").blur(function(e) {
if ($(this).val() == '')
  $(this).val("1");
});

$("input[name='reward_credit']").blur(function(e) {
    if ($(this).val() == '' || parseInt($(this).val()) <= 0) {
      $(this).val('1');
    }
});

</script>
