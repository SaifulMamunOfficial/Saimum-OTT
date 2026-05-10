<?php 
include("includes/db_helper.php");
include("includes/lb_helper.php"); 
include("language/api_language.php"); 
include("smtp_email.php");

error_reporting(0);

$file_path = getBaseUrl();

$mysqli->set_charset('utf8mb4');

date_default_timezone_set("Asia/Colombo");

define("DEFAULT_PASSWORD",'123');
define("PACKAGE_NAME",$settings_details['envato_package_name']);

// For Api header
$API_NAME = 'NEMOSOFTS_APP';

// Purchase code verification
if($settings_details['envato_buyer_name']=='' OR $settings_details['envato_purchase_code']=='' OR $settings_details['envato_api_key']=='') {
    $set[$API_NAME][]=array('MSG'=> 'Purchase code verification failed!','success'=>'0');
	header( 'Content-Type: application/json; charset=utf-8' );
	echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
	die();
}

// Generate random password
function generateRandomPassword($length = 10) {
	$characters = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ';
	$charactersLength = strlen($characters);
	$randomString = '';
	for ($i = 0; $i < $length; $i++) {
		$randomString .= $characters[rand(0, $charactersLength - 1)];
	}
	return $randomString;
}
function update_activity_log($user_id){
	global $mysqli;
    
    $sql="SELECT * FROM tbl_active_log WHERE `user_id`='$user_id'";
    $result = mysqli_query($mysqli,$sql) or die(mysqli_error($mysqli));
    
    if(mysqli_num_rows($result) == 0){
        
        $data_log = array('user_id'  =>  $user_id, 'date_time'  =>  strtotime(date('d-m-Y h:i:s A')));
        $qry = Insert('tbl_active_log',$data_log);
    } else {
        
        $data_log = array('date_time'  =>  strtotime(date('d-m-Y h:i:s A')));
        $update=Update('tbl_active_log', $data_log, "WHERE user_id = '$user_id'");  
    }
    
    mysqli_free_result($result);
}
function send_register_email($to, $recipient_name, $subject, $message){	
	global $file_path;
    global $app_lang;

	$message_body='<div style="background-color: #eee;" align="center"><br />
	<table style="font-family: OpenSans,sans-serif; color: #666666;" border="0" width="600" cellspacing="0" cellpadding="0" align="center" bgcolor="#FFFFFF">
	<tbody>
	<tr>
	<td colspan="2" bgcolor="#FFFFFF" align="center" ><img src="'.$file_path.'images/'.APP_LOGO.'" alt="logo" style="width:100px;height:auto"/></td>
	</tr>
	<br>
	<br>
	<tr>
	<td colspan="2" bgcolor="#FFFFFF" align="center" style="padding-top:25px;">
	<img src="'.$file_path.'assets/images/thankyoudribble.gif" alt="header" auto-height="100" width="50%"/>
	</td>
	</tr>
	<tr>
	<td width="600" valign="top" bgcolor="#FFFFFF">
	<table style="font-family:OpenSans,sans-serif; color: #666666; font-size: 10px; padding: 15px;" border="0" width="100%" cellspacing="0" cellpadding="0" align="left">
	<tbody>
	<tr>
	<td valign="top">
	<table border="0" align="left" cellpadding="0" cellspacing="0" style="font-family:OpenSans,sans-serif; color: #666666; font-size: 10px; width:100%;">
	<tbody>
	<tr>
	<td>
	<p style="color: #717171; font-size: 24px; margin-top:0px; margin:0 auto; text-align:center;"><strong>'.$app_lang['welcome_lbl'].', '.$recipient_name.'</strong></p>
	<br>
	<p style="color:#15791c; font-size:18px; line-height:32px;font-weight:500;margin-bottom:30px; margin:0 auto; text-align:center;">'.$message.'<br /></p>
	<br/>
	<p style="color:#999; font-size:17px; line-height:32px;font-weight:500;">'.$app_lang['thank_you_lbl'].' '.APP_NAME.'</p>
	</td>
	</tr>
	</tbody>
	</table>
	</td>
	</tr>
	</tbody>
	</table>
	</td>
	</tr>
	<tr>
	<td style="color: #262626; padding: 20px 0; font-size: 18px; border-top:5px solid #52bfd3;" colspan="2" align="center" bgcolor="#ffffff">'.$app_lang['email_copyright'].' '.APP_NAME.'.</td>
	</tr>
	</tbody>
	</table>
	</div>';

	send_email($to,$recipient_name,$subject,$message_body);
}
function is_favourite($id,$user_id=''){	
 	global $mysqli;
 	$sql="SELECT * FROM tbl_favourite WHERE `post_id`='$id' AND `user_id`='$user_id'";
 	$result = mysqli_query($mysqli,$sql) or die(mysqli_error($mysqli));
 	if(mysqli_num_rows($result) > 0){
 		return true;
 	} else {
 		return false;
 	}
}
// For subscription
function is_subscription($user_id=''){
    global $mysqli;
    $my_date = date('Y-m-d');
 	$sql="SELECT * FROM tbl_transaction WHERE `end_date_time` > '$my_date' AND `user_id`='$user_id'";
 	$result=mysqli_query($mysqli, $sql);
 	if(mysqli_num_rows($result) > 0){
 		return true;
 	} else {
 		return false;
 	}
}

$get_helper = get_api_data($_POST['data']);
if($get_helper['helper_name']=="get_home"){
    
    $limit = HOME_LIMIT;
    $limit_recently = 15;
    
    $user_id=isset($get_helper['user_id']) ? cleanInput($get_helper['user_id']) : 0;
    $recently_ids=isset($get_helper['songs_ids']) ? trim($get_helper['songs_ids']) : 0;
    
    $jsonObj= array();
	$data_arr= array();
    
    $sql="SELECT * FROM tbl_banner WHERE tbl_banner.status='1' ORDER BY tbl_banner.bid DESC";
    $result = mysqli_query($mysqli, $sql) or die(mysqli_error($mysqli));
    if(mysqli_num_rows($result) > 0){
        while($data = mysqli_fetch_assoc($result)){
            $data_arr['bid'] = $data['bid'];
            $data_arr['banner_title'] = $data['banner_title'];
            $data_arr['banner_info'] = $data['banner_info'];
            $data_arr['banner_image'] = $file_path.'images/'.$data['banner_image'];
            array_push($jsonObj,$data_arr);
        }
    }
    $row['slider'] = $jsonObj;
    
    mysqli_free_result($result);
    $jsonObj = array();
	$data_arr = array();
	
	$sql = "SELECT * FROM tbl_album WHERE status='1' ORDER BY tbl_album.`aid` DESC LIMIT $limit";
	$result = mysqli_query($mysqli, $sql) or die(mysqli_error($mysqli));
	if(mysqli_num_rows($result) > 0){
	    while ($data = mysqli_fetch_assoc($result)){
            $data_arr['aid'] = $data['aid'];
            $data_arr['album_name'] = $data['album_name'];
            $data_arr['album_image'] = $file_path.'images/'.$data['album_image'];
            
            array_push($jsonObj, $data_arr);
    	}
	}
	$row['home_album'] = $jsonObj;
	
	mysqli_free_result($result);
	$jsonObj = array();
	$data_arr = array();
	
	$sql = "SELECT * FROM tbl_artist ORDER BY RAND() DESC LIMIT $limit";
	$result = mysqli_query($mysqli, $sql) or die(mysqli_error($mysqli));
	if(mysqli_num_rows($result) > 0){
	    while ($data = mysqli_fetch_assoc($result)){
            $data_arr['id'] = $data['id'];
            $data_arr['artist_name'] = $data['artist_name'];
            $data_arr['artist_image'] = $file_path.'images/'.$data['artist_image'];
            
            array_push($jsonObj, $data_arr);
    	}
	}
	$row['home_artist'] = $jsonObj;
	
	mysqli_free_result($result);
	$jsonObj = array();
	$data_arr = array();
	
	
	$sql="SELECT * FROM tbl_audio
        LEFT JOIN tbl_album ON tbl_audio.`album_id`= tbl_album.`aid` 
        LEFT JOIN tbl_category ON tbl_audio.`cat_id`= tbl_category.`cid` 
        WHERE tbl_audio.`id` IN ($recently_ids) AND tbl_category.`status`=1 AND tbl_audio.`audio_status`=1 ORDER BY tbl_audio.`id` DESC LIMIT $limit_recently";
    $result = mysqli_query($mysqli, $sql) or die(mysqli_error($mysqli));
    if(mysqli_num_rows($result) > 0){
        while ($data = mysqli_fetch_assoc($result)){
            
            $data_arr['id'] = $data['id'];
            $data_arr['audio_title'] = $data['audio_title'];
            
            $audio_file=$data['audio_url'];
            if($data['audio_type']=='local'){
            	$audio_file=$file_path.'uploads/'.basename($data['audio_url']);
            }
            $data_arr['audio_url'] = $audio_file;
            $data_arr['audio_url_high'] = $data['audio_url_high'];
            $data_arr['audio_url_low'] = $data['audio_url_low'];
            
            $audio_thumbnail=$data['audio_thumbnail'];
            if($data['thumbnail_type']=='album_img'){
                $audio_thumbnail=$data['album_image'];
            }
            $data_arr['image'] = $file_path.'images/'.$audio_thumbnail;
            
            $data_arr['audio_artist'] = $data['audio_artist'];
            $data_arr['audio_description'] = $data['audio_description'];
            
            $data_arr['rate_avg'] = $data['rate_avg'];
            $data_arr['total_views'] = $data['total_views'];
            $data_arr['total_download'] = $data['total_download'];
            
            $data_arr['is_favourite'] = is_favourite($data['id'],$user_id);
            
            array_push($jsonObj, $data_arr);
    	}
    }
	$row['recently_songs'] = $jsonObj;
	
	mysqli_free_result($result);
	$jsonObj = array();
	$data_arr = array();
	
	$sql_views="SELECT *,DATE_FORMAT(`date`, '%m/%d/%Y') FROM `tbl_audio_views` WHERE `date` BETWEEN NOW() - INTERVAL 30 DAY AND NOW() GROUP BY `audio_id` ORDER BY views DESC LIMIT 25";
	$res_views = mysqli_query($mysqli, $sql_views) or die(mysqli_error($mysqli));
	if(mysqli_num_rows($res_views) > 0){
	    while ($row_views=mysqli_fetch_assoc($res_views)){
            
            $id=$row_views['audio_id'];
            
            $sql="SELECT * FROM tbl_audio
            LEFT JOIN tbl_album ON tbl_audio.`album_id`= tbl_album.`aid` 
            LEFT JOIN tbl_category ON tbl_audio.`cat_id`= tbl_category.`cid` 
            WHERE tbl_audio.`audio_status`=1 AND tbl_category.`status`=1 AND tbl_audio.`id`='$id'  ORDER BY tbl_audio.`id` DESC";
            $result = mysqli_query($mysqli, $sql);
            while ($data = mysqli_fetch_assoc($result)){
                $data_arr['id'] = $data['id'];
                $data_arr['audio_title'] = $data['audio_title'];
                
                $audio_file=$data['audio_url'];
                if($data['audio_type']=='local'){
                	$audio_file=$file_path.'uploads/'.basename($data['audio_url']);
                }
                $data_arr['audio_url'] = $audio_file;
                $data_arr['audio_url_high'] = $data['audio_url_high'];
                $data_arr['audio_url_low'] = $data['audio_url_low'];
                
                $audio_thumbnail=$data['audio_thumbnail'];
                if($data['thumbnail_type']=='album_img'){
                    $audio_thumbnail=$data['album_image'];
                }
                $data_arr['image'] = $file_path.'images/'.$audio_thumbnail;
                
                $data_arr['audio_artist'] = $data['audio_artist'];
                $data_arr['audio_description'] = $data['audio_description'];
                
                $data_arr['rate_avg'] = $data['rate_avg'];
                $data_arr['total_views'] = $data['total_views'];
                $data_arr['total_download'] = $data['total_download'];
                
                $data_arr['is_favourite'] = is_favourite($data['id'],$user_id);
                
                array_push($jsonObj, $data_arr);
        	}
    	}
	}
	$row['trending_songs'] = $jsonObj;
	
	mysqli_free_result($res_views);
	$jsonObj = array();
	$data_arr = array();
	
	$sql = "SELECT * FROM tbl_home_sections WHERE status='1' ORDER BY tbl_home_sections.`id` DESC";
	$result = mysqli_query($mysqli, $sql) or die(mysqli_error($mysqli));
	if(mysqli_num_rows($result) > 0){
	    while ($data = mysqli_fetch_assoc($result)){
    	    $id_list=explode(",", $data['post_ids']);
            
            if($data['section_type']=="category"){
              
                foreach($id_list as $ids){
                    
                    $query01="SELECT * FROM tbl_category WHERE tbl_category.`cid`='$ids' AND tbl_category.status='1' ORDER BY tbl_category.cid DESC LIMIT $limit";
                    $sql01 = mysqli_query($mysqli, $query01) or die(mysqli_error($mysqli));
                	if(mysqli_num_rows($sql01) > 0){
                	    while($data01 = mysqli_fetch_assoc($sql01)) {
                	        $home_content1[]= array(
                	            "post_id"=>$data01['cid'],
                	            "post_title"=>$data01['category_name'],
                	            "post_image"=> $file_path.'images/'.$data01['category_image']
                	        );
                		}
                	}
                }
                $row['home_sections'][]=array("home_id"=>$data['id'],"home_title"=>$data['section_name'],"home_type"=>$data['section_type'],"home_content"=>$home_content1);
                unset($home_content1);
            }

          	else if($data['section_type']=="artist"){
          	    
                foreach($id_list as $aids){
                    
                    $query02="SELECT * FROM tbl_artist WHERE tbl_artist.`id`='$aids' ORDER BY tbl_artist.id DESC LIMIT $limit";
                    $sql02 = mysqli_query($mysqli, $query02) or die(mysqli_error($mysqli));
    				if(mysqli_num_rows($sql02) > 0){
    				    while($data02 = mysqli_fetch_assoc($sql02)) {
    				        $home_content2[]= array(
    				            "post_id"=>$data02['id'],
    				            "post_title"=>$data02['artist_name'],
    				            "post_image"=> $file_path.'images/'.$data02['artist_image']
    				        );
    					}
    				}
                }
                $row['home_sections'][]=array("home_id"=>$data['id'],"home_title"=>$data['section_name'],"home_type"=>$data['section_type'],"home_content"=>$home_content2);
                unset($home_content2);
          	}
          	
          	else if($data['section_type']=="album"){
          	    
                foreach($id_list as $alids){
                    
                    $query03="SELECT * FROM tbl_album WHERE tbl_album.`aid`='$alids' ORDER BY tbl_album.aid DESC LIMIT $limit";
                    $sql03 = mysqli_query($mysqli, $query03) or die(mysqli_error($mysqli));
    				if(mysqli_num_rows($sql03) > 0){
    				    while($data03 = mysqli_fetch_assoc($sql03)) {
    				        $home_content3[]= array(
    				            "post_id"=>$data03['aid'],
    				            "post_title"=>$data03['album_name'],
    				            "post_image"=> $file_path.'images/'.$data03['album_image']
    				        );
    					}
    				}
                }
                $row['home_sections'][]=array("home_id"=>$data['id'],"home_title"=>$data['section_name'],"home_type"=>$data['section_type'],"home_content"=>$home_content3);
                unset($home_content3);
          	}
          	
          	else if($data['section_type']=="playlist"){
          	    
                foreach($id_list as $pids){
                    
                    $query04="SELECT * FROM tbl_playlist WHERE tbl_playlist.`pid`='$pids' ORDER BY tbl_playlist.pid DESC LIMIT $limit";
                    $sql04 = mysqli_query($mysqli, $query04) or die(mysqli_error($mysqli));
    				if(mysqli_num_rows($sql04) > 0){
    				    while($data04 = mysqli_fetch_assoc($sql04)) {
    				        $home_content4[]= array(
    				            "post_id"=>$data04['pid'],
    				            "post_title"=>$data04['playlist_name'],
    				            "post_image"=> $file_path.'images/'.$data04['playlist_image']
    				        );
    					}
    				}
                }
                $row['home_sections'][]=array("home_id"=>$data['id'],"home_title"=>$data['section_name'],"home_type"=>$data['section_type'],"home_content"=>$home_content4);
                unset($home_content4);
          	}
          	
          	else if($data['section_type']=="song"){
          	    
                foreach($id_list as $sids){
                    
                    $query05="SELECT * FROM tbl_audio
                        LEFT JOIN tbl_album ON tbl_audio.`album_id`= tbl_album.`aid` 
                        LEFT JOIN tbl_category ON tbl_audio.`cat_id`= tbl_category.`cid` 
                        WHERE tbl_audio.`id` ='$sids' AND tbl_category.`status`=1 AND tbl_audio.`audio_status`=1 ORDER BY tbl_audio.`id` DESC LIMIT $limit";
                    $sql05 = mysqli_query($mysqli, $query05) or die(mysqli_error($mysqli));
    
    				if(mysqli_num_rows($sql05) > 0){
    				    while($data05 = mysqli_fetch_assoc($sql05)) {
    				        
    				        $data_arr['id'] = $data05['id'];
                    		$data_arr['audio_title'] = $data05['audio_title'];
                    		
                    		$audio_file=$data05['audio_url'];
                    		if($data05['audio_type']=='local'){
                    			$audio_file=$file_path.'uploads/'.basename($data05['audio_url']);
                    		}
                    		$data_arr['audio_url'] = $audio_file;
                    		$data_arr['audio_url_high'] = $data05['audio_url_high'];
                    		$data_arr['audio_url_low'] = $data05['audio_url_low'];
                    		
                    		$audio_thumbnail=$data05['audio_thumbnail'];
                            if($data05['thumbnail_type']=='album_img'){
                                $audio_thumbnail=$data05['album_image'];
                            }
                            $data_arr['image'] = $file_path.'images/'.$audio_thumbnail;
                    		
                    		$data_arr['audio_artist'] = $data05['audio_artist'];
                    		$data_arr['audio_description'] = $data05['audio_description'];
                    		
                            $data_arr['rate_avg'] = $data05['rate_avg'];
                    		$data_arr['total_views'] = $data05['total_views'];
                    		$data_arr['total_download'] = $data05['total_download'];
                    		
                    		$data_arr['is_favourite'] = is_favourite($data05['id'],$user_id);
                    		
                    		array_push($jsonObj, $data_arr);
    					}
    				}
                }
                $row['home_sections'][]=array("home_id"=>$data['id'],"home_title"=>$data['section_name'],"home_type"=>$data['section_type'],"home_content"=>$jsonObj);
                unset($jsonObj);
          	}
    	}
	}
    $set[$API_NAME] = $row;
	header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
	die();
}
else if($get_helper['helper_name']=="home_collections"){
    
    $user_id=isset($get_helper['user_id']) ? cleanInput($get_helper['user_id']) : 0;
    $sections_id=trim($get_helper['id']);
    
	$jsonObj = array();
	$data_arr = array();
	
	$sql = "SELECT * FROM tbl_home_sections WHERE tbl_home_sections.`id`='$sections_id' AND status='1' ORDER BY tbl_home_sections.`id` DESC";
	$result = mysqli_query($mysqli, $sql) or die(mysqli_error($mysqli));
	if(mysqli_num_rows($result) > 0){
	    while ($data = mysqli_fetch_assoc($result)){
    	    
    	    $id_list=explode(",", $data['post_ids']);
            
            if($data['section_type']=="category"){
                foreach($id_list as $ids){
                    
                    $query01="SELECT * FROM tbl_category WHERE tbl_category.`cid`='$ids' AND tbl_category.status='1' ORDER BY tbl_category.cid DESC";
                    $sql01 = mysqli_query($mysqli, $query01) or die(mysqli_error($mysqli));
                	if(mysqli_num_rows($sql01) > 0){
                	    while($data01 = mysqli_fetch_assoc($sql01)) {
                	        
                	        $data_arr['cid'] = $data01['cid'];
                    		$data_arr['category_name'] = $data01['category_name'];
                    		$data_arr['category_image'] = $file_path.'images/'.$data01['category_image'];
                    		
                	        array_push($jsonObj, $data_arr);
                		}
                	}
                }
            }
          	
          	else if($data['section_type']=="artist"){
    
                foreach($id_list as $aids){
                    
                    $query02="SELECT * FROM tbl_artist WHERE tbl_artist.`id`='$aids' ORDER BY tbl_artist.id DESC";
                    $sql02 = mysqli_query($mysqli, $query02) or die(mysqli_error($mysqli));
    				if(mysqli_num_rows($sql02) > 0){
    				    while($data02 = mysqli_fetch_assoc($sql02)) {
    				        
    				        $data_arr['id'] = $data02['id'];
                    		$data_arr['artist_name'] = $data02['artist_name'];
                    		$data_arr['artist_image'] = $file_path.'images/'.$data02['artist_image'];
                    		
    				        array_push($jsonObj, $data_arr);
    					}
    				}
                }
          	}
          	
          	else if($data['section_type']=="album"){
    
                foreach($id_list as $alids){
                    
                    $query03="SELECT * FROM tbl_album WHERE tbl_album.`aid`='$alids' ORDER BY tbl_album.aid DESC";
                    $sql03 = mysqli_query($mysqli, $query03) or die(mysqli_error($mysqli));
    				if(mysqli_num_rows($sql03) > 0){
    				    while($data03 = mysqli_fetch_assoc($sql03)) {
    				        
    				        $data_arr['aid'] = $data03['aid'];
                    		$data_arr['album_name'] = $data03['album_name'];
                    		$data_arr['album_image'] = $file_path.'images/'.$data03['album_image'];
                    		
    				        array_push($jsonObj, $data_arr);
    					}
    				}
                    
                }
          	}
          	
          	else if($data['section_type']=="playlist"){
    
                foreach($id_list as $pids){
                    
                    $query04="SELECT * FROM tbl_playlist WHERE tbl_playlist.`pid`='$pids' ORDER BY tbl_playlist.pid DESC";
                    $sql04 = mysqli_query($mysqli, $query04) or die(mysqli_error($mysqli));
    				if(mysqli_num_rows($sql04) > 0){
    				    while($data04 = mysqli_fetch_assoc($sql04)) {
    				        
    				        $data_arr['pid'] = $data04['pid'];
                    		$data_arr['playlist_name'] = $data04['playlist_name'];
                    		$data_arr['playlist_image'] = $file_path.'images/'.$data04['playlist_image'];
                    		
    				        array_push($jsonObj, $data_arr);
    					}
    				}
                    
                }
          	}
          	
          	else if($data['section_type']=="song"){
    
                foreach($id_list as $sids){
                    
                    $query05="SELECT * FROM tbl_audio
                        LEFT JOIN tbl_album ON tbl_audio.`album_id`= tbl_album.`aid` 
                        LEFT JOIN tbl_category ON tbl_audio.`cat_id`= tbl_category.`cid` 
                        WHERE tbl_audio.`id` ='$sids' AND tbl_category.`status`=1 AND tbl_audio.`audio_status`=1 ORDER BY tbl_audio.`id` DESC";
                    $sql05 = mysqli_query($mysqli, $query05) or die(mysqli_error($mysqli));
    				if(mysqli_num_rows($sql05) > 0){
    				    while($data05 = mysqli_fetch_assoc($sql05)) {
    				        
    				        $data_arr['id'] = $data05['id'];
                    		$data_arr['audio_title'] = $data05['audio_title'];
                    		$audio_file=$data05['audio_url'];
                    		if($data05['audio_type']=='local'){
                    			$audio_file=$file_path.'uploads/'.basename($data05['audio_url']);
                    		}
                    		$data_arr['audio_url'] = $audio_file;
                    		$data_arr['audio_url_high'] = $data05['audio_url_high'];
                    		$data_arr['audio_url_low'] = $data05['audio_url_low'];
                    		$audio_thumbnail=$data05['audio_thumbnail'];
                            if($data05['thumbnail_type']=='album_img'){
                                $audio_thumbnail=$data05['album_image'];
                            }
                            $data_arr['image'] = $file_path.'images/'.$audio_thumbnail;
                    		$data_arr['audio_artist'] = $data05['audio_artist'];
                    		$data_arr['audio_description'] = $data05['audio_description'];
                            $data_arr['rate_avg'] = $data05['rate_avg'];
                    		$data_arr['total_views'] = $data05['total_views'];
                    		$data_arr['total_download'] = $data05['total_download'];
                    		$data_arr['is_favourite'] = is_favourite($data05['id'],$user_id);
                    
                    		array_push($jsonObj, $data_arr);
    					}
    				}
                }
          	}
    	}
	}
    $set[$API_NAME] = $jsonObj;
	header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
	die();
}

else if($get_helper['helper_name']=="get_search"){
    
    $user_id=isset($get_helper['user_id']) ? cleanInput($get_helper['user_id']) : 0;
    $search_text=addslashes(trim($get_helper['search_text']));
    
	$jsonObj = array();
	$data_arr = array();

	$sql="SELECT * FROM tbl_audio
        LEFT JOIN tbl_album ON tbl_audio.`album_id`= tbl_album.`aid` 
        LEFT JOIN tbl_category ON tbl_audio.`cat_id`= tbl_category.`cid` 
        WHERE tbl_audio.`audio_title` like '%$search_text%' AND tbl_category.`status`=1 AND tbl_audio.`audio_status`=1 ORDER BY tbl_audio.`audio_title` DESC";
    $result = mysqli_query($mysqli, $sql) or die(mysqli_error($mysqli));
    if(mysqli_num_rows($result) > 0){
        while ($data = mysqli_fetch_assoc($result)){
            $data_arr['id'] = $data['id'];
            $data_arr['audio_title'] = $data['audio_title'];
            $audio_file=$data['audio_url'];
            if($data['audio_type']=='local'){
            	$audio_file=$file_path.'uploads/'.basename($data['audio_url']);
            }
            $data_arr['audio_url'] = $audio_file;
            $data_arr['audio_url_high'] = $data['audio_url_high'];
            $data_arr['audio_url_low'] = $data['audio_url_low'];
            $audio_thumbnail=$data['audio_thumbnail'];
            if($data['thumbnail_type']=='album_img'){
                $audio_thumbnail=$data['album_image'];
            }
            $data_arr['image'] = $file_path.'images/'.$audio_thumbnail;
            $data_arr['audio_artist'] = $data['audio_artist'];
            $data_arr['audio_description'] = $data['audio_description'];
            $data_arr['rate_avg'] = $data['rate_avg'];
            $data_arr['total_views'] = $data['total_views'];
            $data_arr['total_download'] = $data['total_download'];
            $data_arr['is_favourite'] = is_favourite($data['id'],$user_id);
            
            array_push($jsonObj, $data_arr);
    	}
    }
	$row['songs_list'] = $jsonObj;
    
    mysqli_free_result($result);
    $jsonObj = array();
	$data_arr = array();

    $sql = "SELECT * FROM tbl_artist WHERE tbl_artist.`artist_name` like '%$search_text%'
    ORDER BY tbl_artist.`id` DESC";
	$result = mysqli_query($mysqli, $sql) or die(mysqli_error($mysqli));
	if(mysqli_num_rows($result) > 0){
	    while ($data = mysqli_fetch_assoc($result)){
            $data_arr['post_id'] = $data['id'];
            $data_arr['post_title'] = $data['artist_name'];
            $data_arr['post_image'] = $file_path.'images/'.$data['artist_image'];
            
            array_push($jsonObj, $data_arr);
    	}
	}
	$row['artist_list'] = $jsonObj;
	
	mysqli_free_result($result);
	$jsonObj = array();
	$data_arr = array();
	
	$sql = "SELECT * FROM tbl_album WHERE tbl_album.`album_name` like '%$search_text%'
    ORDER BY tbl_album.`album_name` DESC";
	$result = mysqli_query($mysqli, $sql) or die(mysqli_error($mysqli));
	if(mysqli_num_rows($result) > 0){
	    while ($data = mysqli_fetch_assoc($result)){
            $data_arr['post_id'] = $data['aid'];
            $data_arr['post_title'] = $data['album_name'];
            $data_arr['post_image'] = $file_path.'images/'.$data['album_image'];
            array_push($jsonObj, $data_arr);
    	}
	}
	$row['album_list'] = $jsonObj;
	
	mysqli_free_result($result);
	$jsonObj = array();
	$data_arr = array();
	
	$sql = "SELECT * FROM tbl_category WHERE tbl_category.`category_name` like '%$search_text%'
    ORDER BY tbl_category.`category_name` DESC";
	$result = mysqli_query($mysqli, $sql) or die(mysqli_error($mysqli));
	if(mysqli_num_rows($result) > 0){
	    while ($data = mysqli_fetch_assoc($result)){
            $data_arr['post_id'] = $data['cid'];
            $data_arr['post_title'] = $data['category_name'];
            $data_arr['post_image'] = $file_path.'images/'.$data['category_image'];
            
            array_push($jsonObj, $data_arr);
    	}
	}
	$row['category_list'] = $jsonObj;

	mysqli_free_result($result);
	$jsonObj = array();
	$data_arr = array();
	
	$sql = "SELECT * FROM tbl_playlist WHERE tbl_playlist.`playlist_name` like '%$search_text%'
    ORDER BY tbl_playlist.`playlist_name` DESC";
	$result = mysqli_query($mysqli, $sql) or die(mysqli_error($mysqli));
	if(mysqli_num_rows($result) > 0){
	    while ($data = mysqli_fetch_assoc($result)){
            $data_arr['post_id'] = $data['pid'];
            $data_arr['post_title'] = $data['playlist_name'];
            $data_arr['post_image'] = $file_path.'images/'.$data['playlist_image'];
            
            array_push($jsonObj, $data_arr);
    	}
	}
	$row['playlist_list'] = $jsonObj;
	
    $set[$API_NAME] = $row;
	header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
	die();
}
else if($get_helper['helper_name']=="get_search_audio"){
    
    $user_id=isset($get_helper['user_id']) ? cleanInput($get_helper['user_id']) : 0;
    $search_text=addslashes(trim($get_helper['search_text']));
    
    $jsonObj= array();
    
    $page_limit=15;
    $limit=($get_helper['page']-1) * $page_limit;
    
    $sql="SELECT * FROM tbl_audio
        LEFT JOIN tbl_album ON tbl_audio.`album_id`= tbl_album.`aid` 
        LEFT JOIN tbl_category ON tbl_audio.`cat_id`= tbl_category.`cid` 
        WHERE tbl_audio.`audio_title` like '%$search_text%' AND tbl_category.`status`=1 AND tbl_audio.`audio_status`=1 ORDER BY tbl_audio.`audio_title` DESC LIMIT $limit, $page_limit";
    $result = mysqli_query($mysqli, $sql) or die(mysqli_error($mysqli));
    if(mysqli_num_rows($result) > 0){
        while($data = mysqli_fetch_assoc($result)){
            
            $row['id'] = $data['id'];
            $row['audio_title'] = $data['audio_title'];
            $audio_file=$data['audio_url'];
            if($data['audio_type']=='local'){
            	$audio_file=$file_path.'uploads/'.basename($data['audio_url']);
            }
            $row['audio_url'] = $audio_file;
            $row['audio_url_high'] = $data['audio_url_high'];
            $row['audio_url_low'] = $data['audio_url_low'];
            $audio_thumbnail=$data['audio_thumbnail'];
            if($data['thumbnail_type']=='album_img'){
                $audio_thumbnail=$data['album_image'];
            }
            $row['image'] = $file_path.'images/'.$audio_thumbnail;
            $row['audio_artist'] = $data['audio_artist'];
            $row['audio_description'] = $data['audio_description'];
            $row['rate_avg'] = $data['rate_avg'];
            $row['total_views'] = $data['total_views'];
            $row['total_download'] = $data['total_download'];
            $row['is_favourite'] = is_favourite($data['id'],$user_id);
            array_push($jsonObj,$row);
        }
    }
    $set[$API_NAME] = $jsonObj;
    header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
}

else if($get_helper['helper_name']=="cat_list"){
    
    $search_text=addslashes(trim($get_helper['search_text']));
    $search_type=trim($get_helper['search_type']);
    
    $jsonObj= array();
    
    $page_limit=15;
    $limit=($get_helper['page']-1) * $page_limit;
    
    if($search_type == 'search'){
        $sql="SELECT * FROM tbl_category WHERE tbl_category.status='1' AND tbl_category.`category_name` like '%$search_text%'
        ORDER BY tbl_category.category_name DESC LIMIT $limit, $page_limit";
    } else {
        $sql="SELECT * FROM tbl_category WHERE tbl_category.status='1' ORDER BY tbl_category.cid DESC LIMIT $limit, $page_limit";
    }
    $result = mysqli_query($mysqli, $sql) or die(mysqli_error($mysqli));
    if(mysqli_num_rows($result) > 0){
        while($data = mysqli_fetch_assoc($result)){
            $row['cid'] = $data['cid'];
            $row['category_name'] = $data['category_name'];
            $row['category_image'] = $file_path.'images/'.$data['category_image'];
            
            array_push($jsonObj,$row);
        }
    }
    $set[$API_NAME] = $jsonObj;
    header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
}
else if($get_helper['helper_name']=="artist_list"){
    
    $search_text=addslashes(trim($get_helper['search_text']));
    $search_type=trim($get_helper['search_type']);

    $jsonObj= array();
    
    $page_limit=15;
    $limit=($get_helper['page']-1) * $page_limit;
    
    if($search_type == 'search'){
        $sql = "SELECT * FROM tbl_artist WHERE tbl_artist.`artist_name` like '%$search_text%'
        ORDER BY tbl_artist.`artist_name` DESC LIMIT $limit, $page_limit";
    } else {
        $sql="SELECT * FROM tbl_artist WHERE tbl_artist.`id` ORDER BY tbl_artist.id DESC LIMIT $limit, $page_limit";
    }
    $result = mysqli_query($mysqli, $sql) or die(mysqli_error($mysqli));
    if(mysqli_num_rows($result) > 0){
        while($data = mysqli_fetch_assoc($result)){
            $row['id'] = $data['id'];
            $row['artist_name'] = $data['artist_name'];
            $row['artist_image'] = $file_path.'images/'.$data['artist_image'];
            array_push($jsonObj,$row);
        }
    }
    $set[$API_NAME] = $jsonObj;
    header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
}
else if($get_helper['helper_name']=="album_list"){
    
    $search_text=addslashes(trim($get_helper['search_text']));
    $search_type=trim($get_helper['search_type']);
    
    $jsonObj= array();
    
    $page_limit=15;
    $limit=($get_helper['page']-1) * $page_limit;
    
    if($search_type == 'search'){
        $sql = "SELECT * FROM tbl_album WHERE  status='1' AND tbl_album.`album_name` like '%$search_text%'
        ORDER BY tbl_album.`album_name` DESC LIMIT $limit, $page_limit";
    } else {
        $sql = "SELECT * FROM tbl_album WHERE tbl_album.`aid` AND status='1' ORDER BY tbl_album.`aid` DESC LIMIT $limit, $page_limit";
    }
    $result = mysqli_query($mysqli, $sql) or die(mysqli_error($mysqli));
    if(mysqli_num_rows($result) > 0){
        while($data = mysqli_fetch_assoc($result)){
            $row['aid'] = $data['aid'];
            $row['album_name'] = $data['album_name'];
            $row['album_image'] = $file_path.'images/'.$data['album_image'];
            
            array_push($jsonObj,$row);
        }
    }
    $set[$API_NAME] = $jsonObj;
    header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
}
else if($get_helper['helper_name']=="playlist"){
    
    $search_text=addslashes(trim($get_helper['search_text']));
    $search_type=trim($get_helper['search_type']);
    
    $jsonObj= array();
    
    $page_limit=15;
    $limit=($get_helper['page']-1) * $page_limit;
    
    if($search_type == 'search'){
        $sql = "SELECT * FROM tbl_playlist WHERE  status='1' AND tbl_playlist.`playlist_name` like '%$search_text%'
        ORDER BY tbl_playlist.`playlist_name` DESC LIMIT $limit, $page_limit";
    } else {
        $sql = "SELECT * FROM tbl_playlist WHERE tbl_playlist.`pid` AND status='1' ORDER BY tbl_playlist.`pid` DESC LIMIT $limit, $page_limit";
    }
    $result = mysqli_query($mysqli, $sql) or die(mysqli_error($mysqli));
    if(mysqli_num_rows($result) > 0){
        while($data = mysqli_fetch_assoc($result)){
            $row['pid'] = $data['pid'];
            $row['playlist_name'] = $data['playlist_name'];
            $row['playlist_image'] = $file_path.'images/'.$data['playlist_image'];
            
            array_push($jsonObj,$row);
        }
    }
    $set[$API_NAME] = $jsonObj;
    header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
}
else if($get_helper['helper_name']=="album_cat_id"){
    
    $jsonObj= array();
    
    $page_limit=15;
    $limit=($get_helper['page']-1) * $page_limit;
    
    $sql = "SELECT * FROM tbl_album WHERE tbl_album.`catid`='" . $get_helper['cat_id'] . "' AND status='1' ORDER BY tbl_album.`aid` DESC LIMIT $limit, $page_limit";
    $result = mysqli_query($mysqli, $sql) or die(mysqli_error($mysqli));
    if(mysqli_num_rows($result) > 0){
        while($data = mysqli_fetch_assoc($result)){
            $row['aid'] = $data['aid'];
            $row['album_name'] = $data['album_name'];
            $row['album_image'] = $file_path.'images/'.$data['album_image'];
            array_push($jsonObj,$row);
        }
    }
    $set[$API_NAME] = $jsonObj;
    header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
}
else if($get_helper['helper_name']=="album_artist_id"){
    
    $jsonObj= array();
    
    $page_limit=15;
    $limit=($get_helper['page']-1) * $page_limit;
    
    $sql = "SELECT * FROM tbl_album WHERE status='1' AND FIND_IN_SET(" . $get_helper['artist_id'] . ",tbl_album.artist_ids) ORDER BY tbl_album.`aid` DESC LIMIT $limit, $page_limit";
    $result = mysqli_query($mysqli, $sql) or die(mysqli_error($mysqli));
    if(mysqli_num_rows($result) > 0){
        while($data = mysqli_fetch_assoc($result)){
            $row['aid'] = $data['aid'];
            $row['album_name'] = $data['album_name'];
            $row['album_image'] = $file_path.'images/'.$data['album_image'];
            array_push($jsonObj,$row);
        }
    }
    $set[$API_NAME] = $jsonObj;
    header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
}
else if($get_helper['helper_name']=="trending_songs"){
    
    $user_id=isset($get_helper['user_id']) ? cleanInput($get_helper['user_id']) : 0;

    $jsonObj = array();
	$data_arr = array();
	
	$sql_views="SELECT *,DATE_FORMAT(`date`, '%m/%d/%Y') FROM `tbl_audio_views` WHERE `date` BETWEEN NOW() - INTERVAL 30 DAY AND NOW() GROUP BY `audio_id` ORDER BY views DESC LIMIT 25";
	$res_views = mysqli_query($mysqli, $sql_views) or die(mysqli_error($mysqli));
	if(mysqli_num_rows($res_views) > 0){
	    while ($row_views=mysqli_fetch_assoc($res_views)){
            
            $id=$row_views['audio_id'];
            
            $sql="SELECT * FROM tbl_audio
            LEFT JOIN tbl_album ON tbl_audio.`album_id`= tbl_album.`aid` 
            LEFT JOIN tbl_category ON tbl_audio.`cat_id`= tbl_category.`cid` 
            WHERE tbl_audio.`audio_status`=1 AND tbl_category.`status`=1 AND tbl_audio.`id`='$id'  ORDER BY tbl_audio.`id` DESC";
            $result = mysqli_query($mysqli, $sql) or die(mysqli_error($mysqli));
            if(mysqli_num_rows($result) > 0){
                while ($data = mysqli_fetch_assoc($result)){
                    
                    $data_arr['id'] = $data['id'];
                    $data_arr['audio_title'] = $data['audio_title'];
                    $audio_file=$data['audio_url'];
                    if($data['audio_type']=='local'){
                    	$audio_file=$file_path.'uploads/'.basename($data['audio_url']);
                    }
                    $data_arr['audio_url'] = $audio_file;
                    $data_arr['audio_url_high'] = $data['audio_url_high'];
                    $data_arr['audio_url_low'] = $data['audio_url_low'];
                    $audio_thumbnail=$data['audio_thumbnail'];
                    if($data['thumbnail_type']=='album_img'){
                        $audio_thumbnail=$data['album_image'];
                    }
                    $data_arr['image'] = $file_path.'images/'.$audio_thumbnail;
                    $data_arr['audio_artist'] = $data['audio_artist'];
                    $data_arr['audio_description'] = $data['audio_description'];
                    $data_arr['rate_avg'] = $data['rate_avg'];
                    $data_arr['total_views'] = $data['total_views'];
                    $data_arr['total_download'] = $data['total_download'];
                    $data_arr['is_favourite'] = is_favourite($data['id'],$user_id);
                    
                    array_push($jsonObj, $data_arr);
            	}
            }
    	}
	}
    $set[$API_NAME] = $jsonObj;
    header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
}
else if($get_helper['helper_name']=="all_songs"){
    
    $user_id=isset($get_helper['user_id']) ? cleanInput($get_helper['user_id']) : 0;
    
    $jsonObj= array();
    
    $page_limit=15;
    $limit=($get_helper['page']-1) * $page_limit;
    
    $sql="SELECT * FROM tbl_audio
        LEFT JOIN tbl_album ON tbl_audio.`album_id`= tbl_album.`aid` 
        LEFT JOIN tbl_category ON tbl_audio.`cat_id`= tbl_category.`cid` 
        WHERE tbl_audio.`id` AND tbl_category.`status`=1 AND tbl_audio.`audio_status`=1 ORDER BY tbl_audio.`id` DESC LIMIT $limit, $page_limit";
    $result = mysqli_query($mysqli, $sql) or die(mysqli_error($mysqli));
    if(mysqli_num_rows($result) > 0){
        while($data = mysqli_fetch_assoc($result)){
            
            $row['id'] = $data['id'];
            $row['audio_title'] = $data['audio_title'];
            $audio_file=$data['audio_url'];
            if($data['audio_type']=='local'){
            	$audio_file=$file_path.'uploads/'.basename($data['audio_url']);
            }
            $row['audio_url'] = $audio_file;
            $row['audio_url_high'] = $data['audio_url_high'];
            $row['audio_url_low'] = $data['audio_url_low'];
            $audio_thumbnail=$data['audio_thumbnail'];
            if($data['thumbnail_type']=='album_img'){
                $audio_thumbnail=$data['album_image'];
            }
            $row['image'] = $file_path.'images/'.$audio_thumbnail;
            $row['audio_artist'] = $data['audio_artist'];
            $row['audio_description'] = $data['audio_description'];
            $row['rate_avg'] = $data['rate_avg'];
            $row['total_views'] = $data['total_views'];
            $row['total_download'] = $data['total_download'];
            $row['is_favourite'] = is_favourite($data['id'],$user_id);
            
            array_push($jsonObj,$row);
        } 
    }
    $set[$API_NAME] = $jsonObj;
    header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
}
else if($get_helper['helper_name']=="single_song"){
    
    $user_id=isset($get_helper['user_id']) ? cleanInput($get_helper['user_id']) : 0;
    
    $jsonObj= array();
    
    $sql="SELECT * FROM tbl_audio
        LEFT JOIN tbl_album ON tbl_audio.`album_id`= tbl_album.`aid` 
        LEFT JOIN tbl_category ON tbl_audio.`cat_id`= tbl_category.`cid` 
        WHERE tbl_audio.`id`='" . $get_helper['song_id'] . "' AND tbl_category.`status`=1 AND tbl_audio.`audio_status`=1 ORDER BY tbl_audio.`id` DESC";
    $result = mysqli_query($mysqli, $sql) or die(mysqli_error($mysqli));
    if(mysqli_num_rows($result) > 0){
        while($data = mysqli_fetch_assoc($result)){
            
            $row['id'] = $data['id'];
            $row['audio_title'] = $data['audio_title'];
            $audio_file=$data['audio_url'];
            if($data['audio_type']=='local'){
            	$audio_file=$file_path.'uploads/'.basename($data['audio_url']);
            }
            $row['audio_url'] = $audio_file;
            $row['audio_url_high'] = $data['audio_url_high'];
            $row['audio_url_low'] = $data['audio_url_low'];
            $audio_thumbnail=$data['audio_thumbnail'];
            if($data['thumbnail_type']=='album_img'){
                $audio_thumbnail=$data['album_image'];
            }
            $row['image'] = $file_path.'images/'.$audio_thumbnail;
            $row['audio_artist'] = $data['audio_artist'];
            $row['audio_description'] = $data['audio_description'];
            $row['rate_avg'] = $data['rate_avg'];
            $row['total_views'] = $data['total_views'];
            $row['total_download'] = $data['total_download'];
            $row['is_favourite'] = is_favourite($data['id'],$user_id);
            
            array_push($jsonObj,$row);
        }
    }
    
    $view_qry = mysqli_query($mysqli, "UPDATE tbl_audio SET total_views = total_views + 1 WHERE id = '" . $get_helper['song_id'] . "'");
    
    $song_id = $get_helper['song_id'];
	$date = date('Y-m-d');
	
	$start = (date('D') != 'Mon') ? date('Y-m-d', strtotime('last Monday')) : date('Y-m-d');
	$finish = (date('D') != 'Sat') ? date('Y-m-d', strtotime('next Saturday')) : date('Y-m-d');
	
	$query = "SELECT * FROM tbl_audio_views WHERE audio_id='$song_id' and date BETWEEN '$start' AND '$finish'";
	$sql = mysqli_query($mysqli, $query);
	
	if (mysqli_num_rows($sql) > 0) {
		
		$query1 = "UPDATE tbl_audio_views SET views=views+1 WHERE audio_id='$song_id' and date BETWEEN '$start' AND '$finish'";
		$sql1 = mysqli_query($mysqli, $query1);
		
	} else {
	    
	    $query2 = "SELECT * FROM tbl_audio_views WHERE audio_id='$song_id'";
	    $sql2 = mysqli_query($mysqli, $query2) or die(mysqli_error($mysqli));
	    if (mysqli_num_rows($sql2) > 0) {
	        $deleteSql = "DELETE FROM tbl_audio_views WHERE `audio_id` IN ($song_id)";
            mysqli_query($mysqli, $deleteSql);
	    }
	    
		$data = array(
			'audio_id'  =>  $song_id,
			'views'  =>  1,
			'date'  =>  $date
		);
		$qry = Insert('tbl_audio_views', $data);
	}
	
	$set[$API_NAME] = $jsonObj;
    header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
}
else if($get_helper['helper_name']=="song_download"){
    
    $view_qry = mysqli_query($mysqli, "UPDATE tbl_audio SET total_download = total_download + 1 WHERE id = '" . $get_helper['song_id'] . "'");
    $set[$API_NAME][]=array('msg'=>"Download Songs",'success'=>'1');
    header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
    
}
else if($get_helper['helper_name']=="get_recent_songs"){
    
    $user_id=isset($get_helper['user_id']) ? cleanInput($get_helper['user_id']) : 0;
    $ids=isset($get_helper['songs_ids']) ? trim($get_helper['songs_ids']) : 0;

    $jsonObj= array();
    
    $page_limit=15;
    $limit=($get_helper['page']-1) * $page_limit;
    
    $sql="SELECT * FROM tbl_audio
        LEFT JOIN tbl_album ON tbl_audio.`album_id`= tbl_album.`aid` 
        LEFT JOIN tbl_category ON tbl_audio.`cat_id`= tbl_category.`cid` 
        WHERE tbl_audio.`id` IN ($ids) AND tbl_category.`status`=1 AND tbl_audio.`audio_status`=1 ORDER BY tbl_audio.`id` DESC LIMIT $limit, $page_limit";
    $result = mysqli_query($mysqli, $sql) or die(mysqli_error($mysqli));
    if(mysqli_num_rows($result) > 0){
        while($data = mysqli_fetch_assoc($result)){
            
            $row['id'] = $data['id'];
            $row['audio_title'] = $data['audio_title'];
            $audio_file=$data['audio_url'];
            if($data['audio_type']=='local'){
            	$audio_file=$file_path.'uploads/'.basename($data['audio_url']);
            }
            $row['audio_url'] = $audio_file;
            $row['audio_url_high'] = $data['audio_url_high'];
            $row['audio_url_low'] = $data['audio_url_low'];
            $audio_thumbnail=$data['audio_thumbnail'];
            if($data['thumbnail_type']=='album_img'){
                $audio_thumbnail=$data['album_image'];
            }
            $row['image'] = $file_path.'images/'.$audio_thumbnail;
            $row['audio_artist'] = $data['audio_artist'];
            $row['audio_description'] = $data['audio_description'];
            $row['rate_avg'] = $data['rate_avg'];
            $row['total_views'] = $data['total_views'];
            $row['total_download'] = $data['total_download'];
            $row['is_favourite'] = is_favourite($data['id'],$user_id);
            array_push($jsonObj,$row);
        }
    }
    $set[$API_NAME] = $jsonObj;
    header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
}
else if($get_helper['helper_name']=="cat_songs"){
    
    $user_id=isset($get_helper['user_id']) ? cleanInput($get_helper['user_id']) : 0;
    $cat_id = $get_helper['cat_id'];
    
    $jsonObj= array();
    
    $page_limit=15;
    $limit=($get_helper['page']-1) * $page_limit;

    $sql="SELECT * FROM tbl_audio
        LEFT JOIN tbl_album ON tbl_audio.`album_id`= tbl_album.`aid` 
        LEFT JOIN tbl_category ON tbl_audio.`cat_id`= tbl_category.`cid` 
        WHERE tbl_audio.`cat_id`='$cat_id' AND tbl_category.`status`=1 AND tbl_audio.`audio_status`=1 ORDER BY tbl_audio.`id` DESC LIMIT $limit, $page_limit";
    $result = mysqli_query($mysqli, $sql) or die(mysqli_error($mysqli));
    if(mysqli_num_rows($result) > 0){
        while($data = mysqli_fetch_assoc($result)){
            
            $row['id'] = $data['id'];
            $row['audio_title'] = $data['audio_title'];
            $audio_file=$data['audio_url'];
            if($data['audio_type']=='local'){
                $audio_file=$file_path.'uploads/'.basename($data['audio_url']);
            }
            $row['audio_url'] = $audio_file;
            $row['audio_url_high'] = $data['audio_url_high'];
            $row['audio_url_low'] = $data['audio_url_low'];
            $audio_thumbnail=$data['audio_thumbnail'];
            if($data['thumbnail_type']=='album_img'){
                $audio_thumbnail=$data['album_image'];
            }
            $row['image'] = $file_path.'images/'.$audio_thumbnail;
            $row['audio_artist'] = $data['audio_artist'];
            $row['audio_description'] = $data['audio_description'];
            $row['rate_avg'] = $data['rate_avg'];
            $row['total_views'] = $data['total_views'];
            $row['total_download'] = $data['total_download'];
            $row['is_favourite'] = is_favourite($data['id'],$user_id);
            
            array_push($jsonObj,$row);
        }
    }
    $set[$API_NAME] = $jsonObj;
    header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
}
else if($get_helper['helper_name']=="album_songs"){
    
    $user_id=isset($get_helper['user_id']) ? cleanInput($get_helper['user_id']) : 0;
    $album_id = $get_helper['album_id'];
     
    $jsonObj= array();
    
    $page_limit=15;
    $limit=($get_helper['page']-1) * $page_limit;

    $sql="SELECT * FROM tbl_audio
        LEFT JOIN tbl_album ON tbl_audio.`album_id`= tbl_album.`aid` 
        LEFT JOIN tbl_category ON tbl_audio.`cat_id`= tbl_category.`cid` 
        WHERE tbl_audio.`album_id`='$album_id' AND tbl_category.`status`=1 AND tbl_audio.`audio_status`=1 ORDER BY tbl_audio.`id` DESC LIMIT $limit, $page_limit";
    $result = mysqli_query($mysqli, $sql) or die(mysqli_error($mysqli));
    if(mysqli_num_rows($result) > 0){
        while($data = mysqli_fetch_assoc($result)){
            
            $row['id'] = $data['id'];
            $row['audio_title'] = $data['audio_title'];
            $audio_file=$data['audio_url'];
            if($data['audio_type']=='local'){
            	$audio_file=$file_path.'uploads/'.basename($data['audio_url']);
            }
            $row['audio_url'] = $audio_file;
            $row['audio_url_high'] = $data['audio_url_high'];
            $row['audio_url_low'] = $data['audio_url_low'];
            $audio_thumbnail=$data['audio_thumbnail'];
            if($data['thumbnail_type']=='album_img'){
                $audio_thumbnail=$data['album_image'];
            }
            $row['image'] = $file_path.'images/'.$audio_thumbnail;
            $row['audio_artist'] = $data['audio_artist'];
            $row['audio_description'] = $data['audio_description'];
            $row['rate_avg'] = $data['rate_avg'];
            $row['total_views'] = $data['total_views'];
            $row['total_download'] = $data['total_download'];
            $row['is_favourite'] = is_favourite($data['id'],$user_id);
            
            array_push($jsonObj,$row);
        }
    }
    $set[$API_NAME] = $jsonObj;
    header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
}
else if($get_helper['helper_name']=="artist_name_songs"){
    
    $user_id=isset($get_helper['user_id']) ? cleanInput($get_helper['user_id']) : 0;
    $artist_name = $get_helper['artist_name'];
    
    $jsonObj= array();
    
    $page_limit=15;
    $limit=($get_helper['page']-1) * $page_limit;
    
    $sql="SELECT * FROM tbl_audio
        LEFT JOIN tbl_album ON tbl_audio.`album_id`= tbl_album.`aid` 
        LEFT JOIN tbl_category ON tbl_audio.`cat_id`= tbl_category.`cid` 
        WHERE FIND_IN_SET('" . $artist_name . "',tbl_audio.`audio_artist`) AND tbl_category.`status`=1 AND tbl_audio.`audio_status`=1 ORDER BY tbl_audio.`total_views` DESC LIMIT $limit, $page_limit";
    $result = mysqli_query($mysqli, $sql) or die(mysqli_error($mysqli));
    if(mysqli_num_rows($result) > 0){
        while($data = mysqli_fetch_assoc($result)){
            
            $row['id'] = $data['id'];
            $row['audio_title'] = $data['audio_title'];
            $audio_file=$data['audio_url'];
            if($data['audio_type']=='local'){
            	$audio_file=$file_path.'uploads/'.basename($data['audio_url']);
            }
            $row['audio_url'] = $audio_file;
            $row['audio_url_high'] = $data['audio_url_high'];
            $row['audio_url_low'] = $data['audio_url_low'];
            $audio_thumbnail=$data['audio_thumbnail'];
            if($data['thumbnail_type']=='album_img'){
                $audio_thumbnail=$data['album_image'];
            }
            $row['image'] = $file_path.'images/'.$audio_thumbnail;
            $row['audio_artist'] = $data['audio_artist'];
            $row['audio_description'] = $data['audio_description'];
            $row['rate_avg'] = $data['rate_avg'];
            $row['total_views'] = $data['total_views'];
            $row['total_download'] = $data['total_download'];
            $row['is_favourite'] = is_favourite($data['id'],$user_id);
            
            array_push($jsonObj,$row);
        }
    }
    $set[$API_NAME] = $jsonObj;
    header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
}
else if($get_helper['helper_name']=="playlist_songs") {
    
	$user_id=isset($get_helper['user_id']) ? cleanInput($get_helper['user_id']) : 0;
	$playlist_id = $get_helper['playlist_id'];
	
    $sql_playlist="SELECT * FROM tbl_playlist WHERE status='1' AND `pid`='$playlist_id' ORDER BY tbl_playlist.`pid` DESC";
	$res_playlist = mysqli_query($mysqli,$sql_playlist);
	$row_playlist=mysqli_fetch_assoc($res_playlist);

	$songs_ids = trim($row_playlist['playlist_audio']);
	
	$jsonObj= array();
    
    $page_limit=15;
    $limit=($get_helper['page']-1) * $page_limit;
    
    $sql="SELECT * FROM tbl_audio
        LEFT JOIN tbl_album ON tbl_audio.`album_id`= tbl_album.`aid` 
        LEFT JOIN tbl_category ON tbl_audio.`cat_id`= tbl_category.`cid` 
        WHERE tbl_audio.`id` IN ($songs_ids) AND tbl_category.`status`=1 AND tbl_audio.`audio_status`=1 ORDER BY tbl_audio.`total_views` DESC LIMIT $limit, $page_limit";
    $result = mysqli_query($mysqli, $sql) or die(mysqli_error($mysqli));
    if(mysqli_num_rows($result) > 0){
        while($data = mysqli_fetch_assoc($result)){
            
            $row['id'] = $data['id'];
            $row['audio_title'] = $data['audio_title'];
            $audio_file=$data['audio_url'];
            if($data['audio_type']=='local'){
            	$audio_file=$file_path.'uploads/'.basename($data['audio_url']);
            }
            $row['audio_url'] = $audio_file;
            $row['audio_url_high'] = $data['audio_url_high'];
            $row['audio_url_low'] = $data['audio_url_low'];
            $audio_thumbnail=$data['audio_thumbnail'];
            if($data['thumbnail_type']=='album_img'){
                $audio_thumbnail=$data['album_image'];
            }
            $row['image'] = $file_path.'images/'.$audio_thumbnail;
            $row['audio_artist'] = $data['audio_artist'];
            $row['audio_description'] = $data['audio_description'];
            $row['rate_avg'] = $data['rate_avg'];
            $row['total_views'] = $data['total_views'];
            $row['total_download'] = $data['total_download'];
            $row['is_favourite'] = is_favourite($data['id'],$user_id);
            
            array_push($jsonObj,$row);
        }
    }
    $set[$API_NAME] = $jsonObj;
    header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
}
else if($get_helper['helper_name']=="banner_songs"){
    
    $user_id=isset($get_helper['user_id']) ? cleanInput($get_helper['user_id']) : 0;
    $banner_id = $get_helper['banner_id'];
    
    $sql_banner="SELECT * FROM tbl_banner WHERE status='1' AND `bid`='$banner_id' ORDER BY tbl_banner.`bid` DESC";
	$res_banner = mysqli_query($mysqli,$sql_banner);
	$row_banner=mysqli_fetch_assoc($res_banner);

	$songs_ids = trim($row_banner['banner_post_id']);

    $jsonObj= array();
    
    $page_limit=15;
    $limit=($get_helper['page']-1) * $page_limit;
    
    $sql="SELECT * FROM tbl_audio
        LEFT JOIN tbl_album ON tbl_audio.`album_id`= tbl_album.`aid` 
        LEFT JOIN tbl_category ON tbl_audio.`cat_id`= tbl_category.`cid` 
        WHERE tbl_audio.`id` IN ($songs_ids) AND tbl_category.`status`=1 AND tbl_audio.`audio_status`=1 ORDER BY tbl_audio.`total_views` DESC LIMIT $limit, $page_limit";
    $result = mysqli_query($mysqli, $sql) or die(mysqli_error($mysqli));
    if(mysqli_num_rows($result) > 0){
        while($data = mysqli_fetch_assoc($result)){
            
            $row['id'] = $data['id'];
            $row['audio_title'] = $data['audio_title'];
            $audio_file=$data['audio_url'];
            if($data['audio_type']=='local'){
            	$audio_file=$file_path.'uploads/'.basename($data['audio_url']);
            }
            $row['audio_url'] = $audio_file;
            $row['audio_url_high'] = $data['audio_url_high'];
            $row['audio_url_low'] = $data['audio_url_low'];
            $audio_thumbnail=$data['audio_thumbnail'];
            if($data['thumbnail_type']=='album_img'){
                $audio_thumbnail=$data['album_image'];
            }
            $row['image'] = $file_path.'images/'.$audio_thumbnail;
            $row['audio_artist'] = $data['audio_artist'];
            $row['audio_description'] = $data['audio_description'];
            $row['rate_avg'] = $data['rate_avg'];
            $row['total_views'] = $data['total_views'];
            $row['total_download'] = $data['total_download'];
            $row['is_favourite'] = is_favourite($data['id'],$user_id);
            
            array_push($jsonObj,$row);
        }
    }
    $set[$API_NAME] = $jsonObj;
    header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
}
else if($get_helper['helper_name']=="favourite_post"){
    
    $response=array();
    $jsonObj= array();	
	
	$user_id = cleanInput($get_helper['user_id']);
	$post_id = cleanInput($get_helper['post_id']);
	$type = cleanInput($get_helper['type']);

    $sql="SELECT * FROM tbl_favourite WHERE `post_id`='$post_id' AND `user_id`='$user_id' AND `type`='$type'";
    $res = mysqli_query($mysqli, $sql) or die(mysqli_error($mysqli));
	if(mysqli_num_rows($res) == 0){
		
		$data = array(
            'post_id'  =>  $post_id,
            'user_id'  =>  $user_id,
            'type'  =>  $type,
            'created_at'  =>  strtotime(date('d-m-Y h:i:s A')), 
        );
        $qry = Insert('tbl_favourite',$data);
		$response=array('MSG' => $app_lang['favourite_success'],'success'=>'1');
		
	} else {
        
        $deleteSql="DELETE FROM tbl_favourite WHERE `post_id`='$post_id' AND `user_id`='$user_id' AND `type`='$type'";
        if(mysqli_query($mysqli, $deleteSql)){
            $response=array('MSG' => $app_lang['favourite_remove_success'],'success'=>'0');
        } else{
            $response=array('MSG' => $app_lang['favourite_remove_error'],'success'=>'1');
        }
	}
    $set[$API_NAME][]=$response;
	header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
	die();
}
else if($get_helper['helper_name']=="get_favourite"){
    $jsonObj= array();	
    
    $user_id=isset($get_helper['user_id']) ? cleanInput($get_helper['user_id']) : 0;
    $type=trim($get_helper['type']);
    
    $page_limit=10;
    $limit=($get_helper['page']-1) * $page_limit;

    $query="SELECT tbl_audio.*, tbl_category.*, tbl_album.* FROM tbl_audio
        LEFT JOIN tbl_album ON tbl_audio.`album_id`= tbl_album.`aid` 
		LEFT JOIN tbl_favourite ON tbl_audio.`id`= tbl_favourite.`post_id`
		LEFT JOIN tbl_category ON tbl_audio.`cat_id`=tbl_category.`cid` 
		WHERE tbl_audio.`audio_status`='1' AND tbl_category.`status`='1' AND tbl_favourite.`user_id`='$user_id' AND tbl_favourite.`type`='$type' ORDER BY tbl_favourite.`id` DESC LIMIT $limit, $page_limit";
	$sql = mysqli_query($mysqli, $query) or die(mysqli_error($mysqli));
	if(mysqli_num_rows($sql) > 0){
	    while($data = mysqli_fetch_assoc($sql)){
            
            $row['id'] = $data['id'];
            $row['audio_title'] = $data['audio_title'];
            $audio_file=$data['audio_url'];
            if($data['audio_type']=='local'){
            	$audio_file=$file_path.'uploads/'.basename($data['audio_url']);
            }
            $row['audio_url'] = $audio_file;
            $row['audio_url_high'] = $data['audio_url_high'];
            $row['audio_url_low'] = $data['audio_url_low'];
            $audio_thumbnail=$data['audio_thumbnail'];
            if($data['thumbnail_type']=='album_img'){
                $audio_thumbnail=$data['album_image'];
            }
            $row['image'] = $file_path.'images/'.$audio_thumbnail;
            $row['audio_artist'] = $data['audio_artist'];
            $row['audio_description'] = $data['audio_description'];
            $row['rate_avg'] = $data['rate_avg'];
            $row['total_views'] = $data['total_views'];
            $row['total_download'] = $data['total_download'];
            $row['is_favourite'] = is_favourite($data['id'],$user_id);
            
            array_push($jsonObj,$row);
    	}
	}
	$set[$API_NAME] = $jsonObj;
	header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
	die();
}
else if($get_helper['helper_name']=="news_list"){
 	
 	$jsonObj= array();
 	$page_limit=10;
	$limit=($get_helper['page']-1) * $page_limit;

	$query="SELECT * FROM tbl_news WHERE status='1' ORDER BY tbl_news.id DESC LIMIT $limit, $page_limit";
	$sql = mysqli_query($mysqli, $query) or die(mysqli_error($mysqli));
	if(mysqli_num_rows($sql) > 0){
	    while($data = mysqli_fetch_assoc($sql)){
        	
        	$row['id'] = $data['id'];
        	$row['news_title'] = $data['news_title'];
        	$row['news_url'] = $data['news_url'];
        	
        	array_push($jsonObj,$row);
        }
	}
	$set[$API_NAME] = $jsonObj;
	header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
	die();
}
else if($get_helper['helper_name']=="subscription_list"){
    
 	$jsonObj= array();
 	
	$query="SELECT * FROM tbl_subscription WHERE tbl_subscription.id ORDER BY tbl_subscription.id DESC";
	$sql = mysqli_query($mysqli, $query) or die(mysqli_error($mysqli));
	if(mysqli_num_rows($sql) > 0){
	    while($data = mysqli_fetch_assoc($sql)){
            
            $row['id'] = $data['id'];
            $row['plan_name'] = $data['name'];
            $row['plan_duration'] = $data['duration'];
            $row['plan_price'] = $data['price'];
            $row['currency_code'] = $data['currency_code'];
            $row['subscription_id'] = $data['subscription_id'];
            $row['base_key'] = $data['base_key'];
            
            array_push($jsonObj,$row);
    	}
	}
	$set[$API_NAME] = $jsonObj;
	header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
	die();
}
else if($get_helper['helper_name']=="transaction"){	
    
    if($get_helper['planId']!="" && $get_helper['planName']!="" && $get_helper['planPrice']!="" && $get_helper['planDuration']!="" && $get_helper['planCurrencyCode']!="" && $get_helper['user_id']!=""){
        
        $planId =  $get_helper['planId'];
        $planName =  $get_helper['planName'];
        $planPrice =  $get_helper['planPrice'];
        $planDuration =  $get_helper['planDuration'];
        $planCurrencyCode =  $get_helper['planCurrencyCode'];
        $user_id =  $get_helper['user_id'];
        
        $Price = $planPrice;
        $StartDays = $live_date;
        $EndDays = calculate_end_days($live_date, $planDuration);
         
        $sql="SELECT * FROM tbl_transaction WHERE `user_id`='$user_id'";
        $res = mysqli_query($mysqli, $sql) or die(mysqli_error($mysqli));
	    if(mysqli_num_rows($res) == 0){
	        $data = array(
				'user_id'  => $user_id,	
				'plan_name'  => $planName,	
				'plan_price'  => $Price,	
				'date_time'  => $StartDays,	
				'end_date_time'  => $EndDays
			);		
			$qry = Insert('tbl_transaction',$data);	
			
			$set[$API_NAME][] = array('MSG' => 'Add Success','success'=>'1');
	    } else {
	        $data_update = array(
				'user_id'  => $user_id,	
				'plan_name'  => $planName,	
				'plan_price'  => $Price,	
				'date_time'  => $StartDays,	
				'end_date_time'  => $EndDays
			);	
            
            $Update=Update('tbl_transaction', $data_update, "WHERE `user_id`='$user_id'");
            
            $set[$API_NAME][]=array('MSG'=> $app_lang['transaction_success'],'success'=>'1');
	    }
    } else {
        $set[$API_NAME][]=array('MSG'=> $app_lang['transaction_fail'],'success'=>'0');
    }
    header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
}
else if($get_helper['helper_name']=="post_report"){
    
    $jsonObj= array();
    $post_id=cleanInput($get_helper['post_id']);
	$user_id=cleanInput($get_helper['user_id']);
	$report_title=cleanInput($get_helper['report_title']);
	$report_msg=cleanInput($get_helper['report_msg']);
    
	$data = array(
        'post_id'  =>  $post_id,
        'user_id'  =>  $user_id,
        'report_title'  =>  $report_title,
        'report_msg'  =>  $report_msg,
        'report_on'  =>  strtotime(date('d-m-Y h:i:s A')), 
    );
    $qry = Insert('tbl_reports',$data);
    
    $data_not = array(
        'user_id' => $user_id,
        'notification_title' => 'Report successful',
        'notification_msg' => $report_msg,
        'notification_on' =>  strtotime(date('d-m-Y h:i:s A')) 
    );
    
    $qry2 = Insert('tbl_notification',$data_not);
    
	$set[$API_NAME][]=array('MSG'=> $app_lang['report_success'],'success'=> '1');
  	header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
	die();
}
else if($get_helper['helper_name']=="post_suggest"){	
    
	$user_id=cleanInput($get_helper['user_id']);
	$suggest_title=cleanInput($get_helper['suggest_title']);
	$suggest_message=cleanInput($get_helper['suggest_message']);
    	
	if($_FILES['image_data']['name']!=""){
		$image_data=rand(0,99999)."_".$_FILES['image_data']['name'];
		
        //Main Image
        $tpath1='images/'.$image_data;        
        $pic1=compress_image($_FILES["image_data"]["tmp_name"], $tpath1, 80);
	} else {
	    $image_data = '';
	}
	
	$data = array(
        'user_id'  =>  $user_id,
        'suggest_title'  =>  $suggest_title,
        'suggest_image'  =>  $image_data,
        'suggest_message'  =>  $suggest_message,
        'suggest_on'  =>  strtotime(date('d-m-Y h:i:s A')), 
    );
    $qry = Insert('tbl_suggest',$data);
    
    $data_not = array(
        'user_id' => $user_id,
        'notification_title' => 'Suggest successfully',
        'notification_msg' => $suggest_message,
        'notification_on' =>  strtotime(date('d-m-Y h:i:s A')) 
    );
    
    $qry2 = Insert('tbl_notification',$data_not);

    $set[$API_NAME][]=array('MSG'=> $app_lang['suggest_success'],'success'=> '1');
  	header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
	die();
}
else if($get_helper['helper_name']=="get_rating"){
    
    $jsonObj= array();	
    
    $post_id = cleanInput($get_helper['post_id']);
	$device_id = cleanInput($get_helper['device_id']);
	
	$result = mysqli_query($mysqli,"SELECT * FROM tbl_rating WHERE `post_id` = '$post_id' AND `device_id` = '$device_id'"); 
    if(mysqli_num_rows($result) > 0){
		$data = mysqli_fetch_assoc($result);
		$jsonObj = array( 'total_rate' => $data['rate'] , 'message' => $data['message']);	
	} else {
		$jsonObj = array( 'total_rate' => 0, 'message' => '');
	}
	
	$set[$API_NAME][] = $jsonObj;
	header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
	die();
}
else if($get_helper['helper_name']=="post_rating"){
    
    $jsonObj= array();	
    
    $post_id = cleanInput($get_helper['post_id']);
    $device_id = cleanInput($get_helper['device_id']);
    $therate = cleanInput($get_helper['rate']);
    $message = cleanInput($get_helper['message']);
    
    $result = mysqli_query($mysqli,"SELECT * FROM tbl_rating WHERE `post_id` = '$post_id' AND `device_id` = '$device_id'");
    
    if(mysqli_num_rows($result) == 0){
        
        $data = array(   
            'post_id' => $post_id,
            'device_id' => $device_id,
            'rate' => $therate,
            'message' => addslashes($message)
        );  
        $qry = Insert('tbl_rating',$data); 
        
        $query = mysqli_query($mysqli,"SELECT * FROM tbl_rating WHERE `post_id` = '$post_id'");
        
        while($data = mysqli_fetch_assoc($query)){
            $rate_db[] = $data;
            $sum_rates[] = $data['rate'];
        }
        
        if(@count($rate_db)){
            $rate_times = count($rate_db);
            $sum_rates = array_sum($sum_rates);
            $rate_value = $sum_rates/$rate_times;
            $rate_bg = (($rate_value)/5)*100;
        } else {
            $rate_times = 0;
            $rate_value = 0;
            $rate_bg = 0;
        }
        
        $rate_avg=round($rate_value); 
        
        $sql="UPDATE tbl_audio SET `total_rate` = `total_rate` + 1, `rate_avg` = '$rate_avg' where id='$post_id'";
        mysqli_query($mysqli,$sql);
        
        $total_rat_sql="SELECT * FROM tbl_audio WHERE id='".$post_id."'";
        $total_rat_res=mysqli_query($mysqli,$total_rat_sql);
        $total_rat_row=mysqli_fetch_assoc($total_rat_res);
        
        $jsonObj = array('total_rate' => $total_rat_row['total_rate'],'rate_avg' => $total_rat_row['rate_avg'],'MSG' => $app_lang['rate_success'],'success'=> '1');
    } else {
        $jsonObj = array('MSG' => $app_lang['rate_already'],'success'=> '0');
    }
    $set[$API_NAME][] = $jsonObj;
    header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
}
else if($get_helper['helper_name']=="get_rating_list"){
    
    $post_id = cleanInput($get_helper['post_id']);
    
    $jsonObj= array();
    
    $page_limit=15;
    $limit=($get_helper['page']-1) * $page_limit;
    
    $sql="SELECT tbl_rating.*, tbl_users.* FROM tbl_rating
        LEFT JOIN tbl_users ON tbl_rating.`device_id`= tbl_users.`id` 
        WHERE tbl_rating.`post_id` = '$post_id' AND tbl_users.`status`=1 ORDER BY tbl_rating.id DESC LIMIT $limit, $page_limit";
    $result = mysqli_query($mysqli, $sql) or die(mysqli_error($mysqli));
    if(mysqli_num_rows($result) > 0){
        while($data = mysqli_fetch_assoc($result)){
            
            $row['id'] = $data['id'];
            $row['rate'] = $data['rate'];
            $row['message'] = $data['message'];
            $row['user_name'] = $data['user_name'];
            $row['user_profile'] = $file_path.'images/'.$data['profile_img'];
            
            array_push($jsonObj, $row);
        }
    }
    $set[$API_NAME] = $jsonObj;
    header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
}
// User
else if($get_helper['helper_name']=="user_register"){
    
    $user_type=trim($get_helper['type']);

	$email=addslashes(trim($get_helper['user_email']));
	$auth_id=addslashes(trim($get_helper['auth_id']));

	$to = $get_helper['user_email'];
	$recipient_name=$get_helper['user_name'];

	$subject = str_replace('###', APP_NAME, $app_lang['register_mail_lbl']);

	$response=array();

	$user_id='';
	
    switch ($user_type) {
        case 'Google':
        {
            $sql="SELECT * FROM tbl_users WHERE (`user_email` = '$email' OR `auth_id`='$auth_id') AND `user_type`='Google'";
            $res = mysqli_query($mysqli, $sql) or die(mysqli_error($mysqli));
            if(mysqli_num_rows($res) == 0){
                
                $data = [
                    'user_type'=>'Google',
                    'user_name' => addslashes(trim($get_helper['user_name'])),
                    'user_email' => addslashes(trim($get_helper['user_email'])),
                    'user_phone' => '',
                    'user_password' => password_hash(trim(DEFAULT_PASSWORD), PASSWORD_DEFAULT),
                    'user_gender'  => '',
                    'registered_on'  =>  strtotime(date('d-m-Y h:i:s A')),
                    'auth_id' => $auth_id,
                    'profile_img' => '',
                    'status'  =>  '1'
                ];
                
                $qry = Insert('tbl_users',$data);
                
                $user_id=mysqli_insert_id($mysqli);
                
                send_register_email($to, $recipient_name, $subject, $app_lang['google_register_msg']);
                
                // login success
                $response = array('user_id' =>  strval($user_id), 'user_name'=> $get_helper['user_name'], 'user_email'=> $get_helper['user_email'], 'user_phone'=> '', 'user_gender'=> '', 'profile_img'=> '', 'auth_id' => $auth_id, 'MSG' => $app_lang['login_success'], 'success'=>'1');
            }
            else {
                
                $row = mysqli_fetch_assoc($res);
                
                $data = array('auth_id'  =>  $auth_id); 
                $update=Update('tbl_users', $data, "WHERE id = '".$row['id']."'");
                
                $user_id=$row['id'];
                
                if($row['status']==0){
                    $response=array('msg' =>$app_lang['account_deactive'],'success'=>'0');
                } else {
                    $response = array('user_id' =>  $row['id'],'user_name'=> $row['user_name'],'user_email'=> $row['user_email'],'user_phone'=> $row['user_phone'],'user_gender'=> $row['user_gender'],'profile_img'=> $row['profile_img'],'auth_id' => $auth_id,'MSG' => $app_lang['login_success'],'success'=> '1');
                }
            }
            
            update_activity_log($user_id);
        }
        break;
        case 'Normal':
        {
            $sql = "SELECT * FROM tbl_users WHERE user_email = '$email'"; 
            $result = mysqli_query($mysqli, $sql) or die(mysqli_error($mysqli));
            $row = mysqli_fetch_assoc($result);
            
            if (!filter_var($get_helper['user_email'], FILTER_VALIDATE_EMAIL)) {
                $response=array('MSG' => $app_lang['invalid_email_format'],'success'=>'0');
            }
            else if($row['user_email']!="") {
                $response=array('MSG' => $app_lang['email_exist'],'success'=>'0');
            }
            else {
                
                if($_FILES['image_data']['name']!="") {
                    
                    $imgName=rand(0,99999)."_".$_FILES['image_data']['name'];
                    
                    //Main Image
                    $tpath1='images/'.$imgName;        
                    $pic1=compress_image($_FILES["image_data"]["tmp_name"], $tpath1, 80);
                    
                } else {
                    $imgName = '';
                }
                
                $data = [
                    'user_name' => addslashes(trim($get_helper['user_name'])),
                    'user_email' => addslashes(trim($get_helper['user_email'])),
                    'user_phone' => addslashes(trim($get_helper['user_phone'])),
                    'user_password' => password_hash(trim($get_helper['user_password']), PASSWORD_DEFAULT),
                    'user_gender'  => addslashes(trim($get_helper['user_gender'])),
                    'registered_on'  =>  strtotime(date('d-m-Y h:i:s A')),
                    'profile_img' => $imgName,
                    'status'  =>  '1'
                ];
                
                $qry = Insert('tbl_users',$data);
                
                $user_id=mysqli_insert_id($mysqli);
                
                send_register_email($to, $recipient_name, $subject, $app_lang['normal_register_msg']);
                
                $response=array('MSG' => $app_lang['register_success'],'success'=>'1');
                
                update_activity_log($user_id);
            }
        }
        break;
        default:
        {
            $response=array('success'=>'0', 'MSG' =>$app_lang['register_fail']);
        }
        break;
    }
	
	$set[$API_NAME][]=$response;
	header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
	die();
}
else if($get_helper['helper_name']=="user_login"){
    
    $response=array();
    
    $email= trim($get_helper['user_email']);
    $password = trim($get_helper['user_password']);
    $auth_id = trim($get_helper['auth_id']);
    $user_type = trim($get_helper['type']);
    
    if (!filter_var($email, FILTER_VALIDATE_EMAIL) AND $email!='') {
        $response=array('MSG' => $app_lang['invalid_email_format'],'success'=>'0');
        
        $set[$API_NAME][]=$response;
        header( 'Content-Type: application/json; charset=utf-8' );
        echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
        die();
    }
    
    switch ($user_type) {
        case 'Google':
        {
            $sql = "SELECT * FROM tbl_users WHERE (`user_email` = '$email' OR `auth_id`='$auth_id') AND (`user_type`='Google' OR `user_type`='google')";
            $res = mysqli_query($mysqli, $sql) or die(mysqli_error($mysqli));
            
            if(mysqli_num_rows($res) > 0) {
                $row = mysqli_fetch_assoc($res);
                
                if($row['status']==0) {
                    $response=array('MSG' => $app_lang['account_deactive'],'success'=>'0');
                } else {
                    $user_id=$row['id'];
                    
                    update_activity_log($user_id);
                    
                    $data = array('auth_id'  =>  $auth_id);  
                    
                    Update('tbl_users', $data, "WHERE `id` = ".$row['id']);
                    
                    $response = array('user_id' =>  $row['id'],'user_name'=> $row['user_name'],'user_phone'=> $row['user_phone'],'user_gender'=> $row['user_gender'],'profile_img'=> $file_path.'images/'.$row['profile_img'],'MSG' => $app_lang['login_success'],'success'=>'1');
                }
            }
            else {
                $response=array('MSG' => $app_lang['email_not_found'],'success'=>'0');
            }
        }
        break;
        case 'Normal':
        {
            $qry = "SELECT * FROM tbl_users WHERE user_email = '$email' AND (`user_type`='Normal' OR `user_type`='normal') AND `id` <> 0"; 
            $result = mysqli_query($mysqli, $qry) or die(mysqli_error($mysqli));
            $num_rows = mysqli_num_rows($result);
            
            if($num_rows > 0) {
                $row = mysqli_fetch_assoc($result);
                
                if($row['status']==1) {
                    
                    if (password_verify($password, $row['user_password'])) {
                        
                        $user_id=$row['id'];
                        
                        update_activity_log($user_id);
                        
                        $response = array('user_id' =>  $row['id'],'user_name'=> $row['user_name'],'user_phone'=> $row['user_phone'],'user_gender'=> $row['user_gender'],'profile_img'=> $file_path.'images/'.$row['profile_img'],'MSG' => $app_lang['login_success'],'success'=>'1');
                        
                    }
                    else{
                        $response=array('MSG' =>$app_lang['invalid_password'],'success'=>'0');
                    }
                }
                else {
                    $response=array('MSG' =>$app_lang['account_deactive'],'success'=>'0');
                }
            }
            else {
                $response=array('MSG' =>$app_lang['email_not_found'],'success'=>'0');	
            }
        }
        break;
        default:
        {
            $response=array('success'=>'0', 'MSG' =>$app_lang['register_fail']);
        }
        break;
    }
    
    $set[$API_NAME][]=$response;
    header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
}
else if($get_helper['helper_name'] == "user_profile") {
	$jsonObj= array();	
	
	$user_id=cleanInput($get_helper['user_id']);

	$qry = "SELECT * FROM tbl_users WHERE id = '$user_id'"; 
	$result = mysqli_query($mysqli, $qry) or die(mysqli_error($mysqli));
	$row = mysqli_fetch_assoc($result);	
	
	$data['success']="1";
	$data['user_id'] = $row['id'];
	$data['user_name'] = $row['user_name'];
	$data['user_email'] = ($row['user_email']!='') ? $row['user_email'] : '';
	$data['user_phone'] = ($row['user_phone']!='') ? $row['user_phone'] : '';
	$data['user_gender'] = $row['user_gender'];
	$data['profile_img'] = $file_path.'images/'.$row['profile_img'];

	array_push($jsonObj,$data);

    $set[$API_NAME] = $jsonObj;
	header( 'Content-Type: application/json; charset=utf-8' );
	echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE));
	die();
}
else if($get_helper['helper_name']=="edit_profile"){
    
    $jsonObj= array();	
	
	$qry = "SELECT * FROM tbl_users WHERE id = '".$get_helper['user_id']."'"; 
	$result = mysqli_query($mysqli, $qry) or die(mysqli_error($mysqli));
	$row = mysqli_fetch_assoc($result);
  
  	if (!filter_var($get_helper['user_email'], FILTER_VALIDATE_EMAIL)) {
  	    $set[$API_NAME][]=array('MSG' => $app_lang['invalid_user_type'],'success'=>'0');
	}
	else if($row['user_email']==$get_helper['user_email'] AND $row['id']!=$get_helper['user_id']) {
        $set[$API_NAME][]=array('MSG' => $app_lang['email_not_found'],'success'=>'0');
	} 
	else {
        $data = array(
            'user_name'  =>  cleanInput($get_helper['user_name']),
            'user_email'  =>  trim($get_helper['user_email']),
            'user_phone'  =>  cleanInput($get_helper['user_phone']),
		);
		
		if($get_helper['user_password']!=""){
			$data = array_merge($data, array("user_password" => password_hash(trim($get_helper['user_password']), PASSWORD_DEFAULT)));
		}
		
		$user_edit=Update('tbl_users', $data, "WHERE id = '".$get_helper['user_id']."'");
		
		$set[$API_NAME][] = array('MSG' => $app_lang['update_success'], 'success' => '1');
	}
    header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
}
else if($get_helper['helper_name']=="user_images_update"){	
    
	if($_FILES['image_data']['name']!="") {
	    
		$image_data=rand(0,99999)."_".$_FILES['image_data']['name'];
		
        //Main Image
        $tpath1='images/'.$image_data;        
        $pic1=compress_image_app($_FILES["image_data"]["tmp_name"], $tpath1, 80);
        
        $data = array('profile_img'  =>  $image_data);
        $user_update =Update('tbl_users', $data, "WHERE id = '".$get_helper['user_id']."'");
        
        $set[$API_NAME][]=array('MSG'=> $app_lang['update_success'],'success' => '1');
	} else {
        $set[$API_NAME][]=array('MSG' => "Update error",'success' => '0');
	}
  	header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
	die();
}
else if($get_helper['helper_name']=="account_delete"){
    
    $ids = cleanInput($get_helper['user_id']);
    
    $sql="SELECT * FROM tbl_users WHERE `id`='$ids'";
    $res = mysqli_query($mysqli, $sql) or die(mysqli_error($mysqli));
    if(mysqli_num_rows($res) > 0) {
        
        $sql_img="SELECT * FROM tbl_users WHERE `id` IN ($ids)";
        $res_img=mysqli_query($mysqli, $sql_img);
        while ($row=mysqli_fetch_assoc($res_img)) {
            if($row['profile_img']!="") {
                unlink('images/'.$row['profile_img']);
            }
        }
        
        $deleteSql = "DELETE FROM tbl_active_log WHERE `user_id` IN ($ids)";
        mysqli_query($mysqli, $deleteSql);
        
        $deleteSql="DELETE FROM tbl_notification WHERE `user_id` IN ($ids)";
        mysqli_query($mysqli, $deleteSql);
        
        $deleteSql = "DELETE FROM tbl_users WHERE `id` IN ($ids)";
        mysqli_query($mysqli, $deleteSql);
        
        $set[$API_NAME][]=array('MSG'=> "Remove success",'success'=> '1');
    } else {
        $set[$API_NAME][]=array('MSG'=> 'Remove error','success'=> '0');
    }
    header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
}
else if($get_helper['helper_name']=="forgot_pass"){
    
    $email=addslashes(trim($get_helper['user_email']));

	$qry = "SELECT * FROM tbl_users WHERE user_email = '$email' AND `user_type`='Normal' AND `id` <> 0"; 
	$result = mysqli_query($mysqli, $qry) or die(mysqli_error($mysqli));
	$row = mysqli_fetch_assoc($result);
	
	if($row['user_email']!="") {
		$password=generateRandomPassword(7);
		
		$new_password= password_hash(trim($password), PASSWORD_DEFAULT);
		
		$to = $row['user_email'];
		$recipient_name=$row['user_name'];
		
		// subject
		$subject = str_replace('###', APP_NAME, $app_lang['forgot_password_sub_lbl']);
		$message='<div style="background-color: #f9f9f9;" align="center"><br />
				  <table style="font-family: OpenSans,sans-serif; color: #666666;" border="0" width="600" cellspacing="0" cellpadding="0" align="center" bgcolor="#FFFFFF">
				    <tbody>
				      <tr>
				        <td colspan="2" bgcolor="#FFFFFF" align="center"><img src="'.$file_path.'images/'.APP_LOGO.'" alt="header" style="width:100px;height:auto"/></td>
				      </tr>
				      <tr>
				        <td width="600" valign="top" bgcolor="#FFFFFF"><br>
				          <table style="font-family:OpenSans,sans-serif; color: #666666; font-size: 10px; padding: 15px;" border="0" width="100%" cellspacing="0" cellpadding="0" align="left">
				            <tbody>
				              <tr>
				                <td valign="top"><table border="0" align="left" cellpadding="0" cellspacing="0" style="font-family:OpenSans,sans-serif; color: #666666; font-size: 10px; width:100%;">
				                    <tbody>
				                      <tr>
				                        <td>
				                          <p style="color: #262626; font-size: 24px; margin-top:0px;"><strong>'.$app_lang['dear_lbl'].' '.$row['user_name'].'</strong></p>
				                          <p style="color:#262626; font-size:20px; line-height:32px;font-weight:500;margin-top:5px;"><br>'.$app_lang['your_password_lbl'].': <span style="font-weight:400;">'.$password.'</span></p>
				                          <p style="color:#262626; font-size:17px; line-height:32px;font-weight:500;margin-bottom:30px;">'.$app_lang['thank_you_lbl'].' '.APP_NAME.'</p>
				                          
				                        </td>
				                      </tr>
				                    </tbody>
				                  </table></td>
				              </tr>
				               
				            </tbody>
				          </table></td>
				      </tr>
				      <tr>
				        <td style="color: #262626; padding: 20px 0; font-size: 18px; border-top:5px solid #52bfd3;" colspan="2" align="center" bgcolor="#ffffff">'.$app_lang['email_copyright'].' '.APP_NAME.'.</td>
				      </tr>
				    </tbody>
				  </table>
				</div>';
				
		send_email($to,$recipient_name,$subject,$message);
		
		$sql="UPDATE tbl_users SET `user_password`='$new_password' WHERE `id`='".$row['id']."'";
      	mysqli_query($mysqli,$sql);
		 	  
		$set[$API_NAME][]=array('MSG' => $app_lang['password_sent_mail'],'success'=>'1');
	}
	else {  	 
		$set[$API_NAME][]=array('MSG' => $app_lang['email_not_found'],'success'=>'0');
	}
	header( 'Content-Type: application/json; charset=utf-8' );
	echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE));
	die();
}
else if($get_helper['helper_name']=="get_notification") {
    
    $user_id = cleanInput($get_helper['user_id']);
	    
    $jsonObj= array();
    
	$page_limit=50;
	$limit=($get_helper['page']-1) * $page_limit;

    $query="SELECT * FROM tbl_notification WHERE `user_id`='$user_id' ORDER BY tbl_notification.`id` DESC LIMIT $limit, $page_limit"; 
	$sql = mysqli_query($mysqli,$query)or die(mysqli_error($mysqli));
	if(mysqli_num_rows($sql) > 0){
	    while($data = mysqli_fetch_assoc($sql)){
            $row['id'] = $data['id'];
            $row['notification_title'] = $data['notification_title'];
            $row['notification_msg'] = $data['notification_msg']; 
            $row['notification_on'] = calculate_time_span($data['notification_on'],true);		 
            
            array_push($jsonObj,$row);
    	}
	}
	$set[$API_NAME] = $jsonObj;
	header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
	die();
}
else if($get_helper['helper_name']=="remove_notification"){
    
    $post_id=cleanInput($get_helper['post_id']);
	$user_id=cleanInput($get_helper['user_id']);

	$jsonObj= array();
	
	$sql="SELECT * FROM tbl_notification WHERE `id`='$post_id' AND `user_id`='$user_id'";
	$res = mysqli_query($mysqli, $sql) or die(mysqli_error($mysqli));
	if(mysqli_num_rows($res) > 0) {
		$deleteSql="DELETE FROM tbl_notification WHERE `id`='$post_id' AND `user_id`='$user_id'";
		mysqli_query($mysqli, $deleteSql);
		
        $set[$API_NAME][]=array('MSG'=> $app_lang['remove_success'],'success'=> '1');
	} else {
	    $set[$API_NAME][]=array('MSG'=> $app_lang['like_remove_error'],'success'=> '0');
	}
	header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
	die();
}
// App details
else if($get_helper['helper_name']=="app_details"){
    
    $user_id=isset($get_helper['user_id']) ? cleanInput($get_helper['user_id']) : 0;

    $jsonObj= array();	
	$query="SELECT * FROM tbl_settings WHERE id='1'";
	$sql = mysqli_query($mysqli, $query) or die(mysqli_error($mysqli));
	while($data = mysqli_fetch_assoc($sql)){
	    
        // App Details ---------------------------------------------------------
        $row['app_email'] = $data['app_email'];
        $row['app_author'] = $data['app_author'];
        $row['app_contact'] = $data['app_contact'];
        $row['app_website'] = $data['app_website'];
        $row['app_description'] = $data['app_description'];
        $row['app_developed_by'] = $data['app_developed_by'];
        
        // Envato --------------------------------------------------------------
        $row['envato_api_key'] = $data['envato_api_key'];
        
        // API Latest Limit ----------------------------------------------------
        $row['api_latest_limit'] = $data['api_latest_limit'];
        
        // is ------------------------------------------------------------------
        $row['isRTL'] = $data['isRTL'];
        $row['isMaintenance'] = $data['isMaintenance'];
        $row['isScreenshot'] = $data['isScreenshot'];
        $row['isGoogleLogin'] = $data['isGoogleLogin'];
        $row['isLogin'] = $data['isLogin'];
        $row['isAPK'] = $data['isAPK'];
        $row['isVPN'] = $data['isVPN'];
        
        $row['isDummy_1'] = $data['isDummy_1'];
        $row['isDummy_2'] = $data['isDummy_2'];
        $row['dummy_test_1'] = $data['dummy_test_1'];
        $row['dummy_test_2'] = $data['dummy_test_2'];
       
        // AppUpdate -----------------------------------------------------------
        $row['app_update_status'] = $data['app_update_status'];
        $row['app_new_version'] = $data['app_new_version'];
        $row['app_update_desc'] = $data['app_update_desc'];
        $row['app_redirect_url'] = $data['app_redirect_url'];
        
        // More Apps -----------------------------------------------------------
        $row['more_apps_url'] = $data['more_apps_url'];
        
        // Ads Network ---------------------------------------------------------
        $row['ad_status'] = $data['ad_status'];
        // PRIMARY ADS
        $row['ad_network'] = $data['ad_network'];
        // admob
        $row['admob_publisher_id'] = $data['admob_publisher_id'];
        $row['admob_banner_unit_id'] = $data['admob_banner_unit_id'];
        $row['admob_interstitial_unit_id'] = $data['admob_interstitial_unit_id'];
        $row['admob_native_unit_id'] = $data['admob_native_unit_id'];
        $row['admob_app_open_ad_unit_id'] = $data['admob_app_open_ad_unit_id'];
        $row['admob_reward_ad_unit_id'] = $data['admob_reward_ad_unit_id'];
        // startapp
        $row['startapp_app_id'] = $data['startapp_app_id'];
        // unity
        $row['unity_game_id'] = $data['unity_game_id'];
        $row['unity_banner_placement_id'] = $data['unity_banner_placement_id'];
        $row['unity_interstitial_placement_id'] = $data['unity_interstitial_placement_id'];
        $row['unity_reward_ad_unit_id'] = $data['unity_reward_ad_unit_id'];
        // applovin max
        $row['applovin_banner_ad_unit_id'] = $data['applovin_banner_ad_unit_id'];
        $row['applovin_interstitial_ad_unit_id'] = $data['applovin_interstitial_ad_unit_id'];
        $row['applovin_native_ad_manual_unit_id'] = $data['applovin_native_ad_manual_unit_id'];
        $row['applovin_app_open_ad_unit_id'] = $data['applovin_app_open_ad_unit_id'];
        $row['applovin_reward_ad_unit_id'] = $data['applovin_reward_ad_unit_id'];
        // ironsource
        $row['ironsource_app_key'] = $data['ironsource_app_key'];
        // Meta Audience Network
        $row['mata_banner_ad_unit_id'] = $data['mata_banner_ad_unit_id'];
        $row['mata_interstitial_ad_unit_id'] = $data['mata_interstitial_ad_unit_id'];
        $row['mata_native_ad_manual_unit_id'] = $data['mata_native_ad_manual_unit_id'];
        // yandex
        $row['yandex_banner_ad_unit_id'] = $data['yandex_banner_ad_unit_id'];
        $row['yandex_interstitial_ad_unit_id'] = $data['yandex_interstitial_ad_unit_id'];
        $row['yandex_native_ad_manual_unit_id'] = $data['yandex_native_ad_manual_unit_id'];
        $row['yandex_app_open_ad_unit_id'] = $data['yandex_app_open_ad_unit_id'];
        // wortise
        $row['wortise_app_id'] = $data['wortise_app_id'];
        $row['wortise_banner_unit_id'] = $data['wortise_banner_unit_id'];
        $row['wortise_interstitial_unit_id'] = $data['wortise_interstitial_unit_id'];
        $row['wortise_native_unit_id'] = $data['wortise_native_unit_id'];
        $row['wortise_app_open_unit_id'] = $data['wortise_app_open_unit_id'];
        $row['wortise_reward_ad_unit_id'] = $data['wortise_reward_ad_unit_id'];
        
        // ADS PLACEMENT
        $row['banner_home'] = $data['banner_home'];
        $row['banner_post_details'] = $data['banner_post_details'];
        $row['banner_category_details'] = $data['banner_category_details'];
        $row['banner_search'] = $data['banner_search'];
        $row['interstitial_post_list'] = $data['interstitial_post_list'];
        $row['native_ad_post_list'] = $data['native_ad_post_list'];
        $row['native_ad_category_list'] = $data['native_ad_category_list'];
        $row['app_open_ad_on_start'] = $data['app_open_ad_on_start'];
        $row['reward_ad_on'] = $data['reward_ad_on'];
        
        // GLOBAL CONFIGURATION
        $row['interstital_ad_click'] = $data['interstital_ad_click'];
        $row['native_position'] = $data['native_position'];
        $row['reward_credit'] = $data['reward_credit'];
        
        // Purchases
        $row['isPurchases'] = is_subscription($user_id);
        
        array_push($jsonObj,$row);
    }
	$set[$API_NAME] = $jsonObj;
	header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
	die();
}
else {
	$get_helper = get_api_data($_POST['data']);
}