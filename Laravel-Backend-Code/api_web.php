<?php 
include("includes/db_helper.php");
include("includes/lb_helper.php"); 
include("language/api_language.php"); 
include("smtp_email.php");

error_reporting(0);

$file_path = getBaseUrl();

$mysqli->set_charset('utf8mb4');

date_default_timezone_set("Asia/Colombo");

// For Api header
$API_NAME = 'NEMOSOFTS_WEB';

// Purchase code verification
if($settings_details['envato_buyer_name']=='' OR $settings_details['envato_purchase_code']=='' OR $settings_details['envato_api_key']=='') {
    $set[$API_NAME][]=array('MSG'=> 'Purchase code verification failed!','success'=>'0');
	header( 'Content-Type: application/json; charset=utf-8' );
	echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
	die();
}

function update_activity_log($user_id){
	global $mysqli;
    
    $sql="SELECT * FROM tbl_active_log WHERE `user_id`='$user_id'";
    $result=mysqli_query($mysqli, $sql);
    
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

if($_POST['helper_name']=="get_home"){
    
    $limit = 10;
    $limit_audio = 15;
    
    $jsonObj= array();
	$data_arr= array();
    
    $sql="SELECT * FROM tbl_banner WHERE tbl_banner.status='1' ORDER BY tbl_banner.bid DESC";
    $result = mysqli_query($mysqli, $sql);
    
    while($data = mysqli_fetch_assoc($result)){
        $data_arr['bid'] = $data['bid'];
	    $data_arr['banner_title'] = $data['banner_title'];
	    $data_arr['banner_info'] = $data['banner_info'];
	    $data_arr['banner_image'] = $file_path.'images/'.$data['banner_image'];
        array_push($jsonObj,$data_arr);
    }
    $row['slider'] = $jsonObj;
    
    mysqli_free_result($result);
    $jsonObj = array();
	$data_arr = array();
	
	$sql = "SELECT * FROM tbl_album WHERE status='1' ORDER BY tbl_album.`aid` DESC LIMIT $limit";
	$result = mysqli_query($mysqli, $sql);
	while ($data = mysqli_fetch_assoc($result)){
		$data_arr['aid'] = $data['aid'];
		$data_arr['album_name'] = $data['album_name'];
		$data_arr['album_image'] = $file_path.'images/'.$data['album_image'];
		
		array_push($jsonObj, $data_arr);
	}
	$row['home_album'] = $jsonObj;
	
	mysqli_free_result($result);
	$jsonObj = array();
	$data_arr = array();
	
	$sql = "SELECT * FROM tbl_artist ORDER BY RAND() DESC LIMIT $limit";
	$result = mysqli_query($mysqli, $sql);
	while ($data = mysqli_fetch_assoc($result)){
		$data_arr['id'] = $data['id'];
		$data_arr['artist_name'] = $data['artist_name'];
		$data_arr['artist_image'] = $file_path.'images/'.$data['artist_image'];
		
		array_push($jsonObj, $data_arr);
	}
	$row['home_artist'] = $jsonObj;
	
	mysqli_free_result($result);
	$jsonObj = array();
	$data_arr = array();
	
	$sql="SELECT * FROM tbl_audio
        LEFT JOIN tbl_album ON tbl_audio.`album_id`= tbl_album.`aid` 
        LEFT JOIN tbl_category ON tbl_audio.`cat_id`= tbl_category.`cid` 
        WHERE tbl_audio.`id` AND tbl_category.`status`=1 AND tbl_audio.`audio_status`=1 ORDER BY tbl_audio.`id` DESC LIMIT $limit_audio";
    $result = mysqli_query($mysqli, $sql);
    while ($data = mysqli_fetch_assoc($result)){
        $data_arr['id'] = $data['id'];
		$data_arr['audio_title'] = $data['audio_title'];
		
		$audio_file=$data['audio_url'];
		if($data['audio_type']=='local'){
			$audio_file=$file_path.'uploads/'.basename($data['audio_url']);
		}
		$data_arr['audio_url'] = $audio_file;
		
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
		
		$data_arr['aid'] = $data['aid'];
		$data_arr['category_name'] = $data['category_name'];
		
		array_push($jsonObj, $data_arr);
	}
	$row['recently_songs'] = $jsonObj;
	
	mysqli_free_result($result);
	$jsonObj = array();
	$data_arr = array();
	
	$sql_views="SELECT *,DATE_FORMAT(`date`, '%m/%d/%Y') FROM `tbl_audio_views` WHERE `date` BETWEEN NOW() - INTERVAL 30 DAY AND NOW() GROUP BY `audio_id` ORDER BY views DESC LIMIT 25";
	$res_views=mysqli_query($mysqli, $sql_views);
	
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
    		
    		$data_arr['aid'] = $data['aid'];
    		$data_arr['category_name'] = $data['category_name'];
    		
    		array_push($jsonObj, $data_arr);
    	}
	}
	$row['trending_songs'] = $jsonObj;
	
	mysqli_free_result($result);
	$jsonObj = array();
	$data_arr = array();
	
	$sql = "SELECT * FROM tbl_home_sections WHERE status='1' ORDER BY tbl_home_sections.`id` DESC";
	$result = mysqli_query($mysqli, $sql);
	while ($data = mysqli_fetch_assoc($result)){
	    $id_list=explode(",", $data['post_ids']);
      	
      	if($data['section_type']=="category"){
      	    
            foreach($id_list as $ids){
                
                $query01="SELECT * FROM tbl_category WHERE tbl_category.`cid`='$ids' AND tbl_category.status='1' ORDER BY tbl_category.cid DESC";
				$sql01 = mysqli_query($mysqli,$query01);
				
				if(mysqli_num_rows($sql01) > 0){
				    while($data01 = mysqli_fetch_assoc($sql01)) {
				        $home_content1[]= array(
				            "cid"=>$data01['cid'],
				            "category_name"=>$data01['category_name'],
				            "category_image"=> $file_path.'images/'.$data01['category_image']
				        );
					}
				}
            }
            
            $row['home_sections'][]=array("home_id"=>$data['id'],"home_title"=>$data['section_name'],"home_type"=>$data['section_type'],"home_content"=>$home_content1);
            unset($home_content1);
      	}
      	
      	else if($data['section_type']=="artist"){
      	    
            foreach($id_list as $aids){
                
                $query02="SELECT * FROM tbl_artist WHERE tbl_artist.`id`='$aids' ORDER BY tbl_artist.id DESC";
				$sql02 = mysqli_query($mysqli,$query02);
				
				if(mysqli_num_rows($sql02) > 0){
				    while($data02 = mysqli_fetch_assoc($sql02)) {
				        $home_content2[]= array(
				            "id"=>$data02['id'],
				            "artist_name"=>$data02['artist_name'],
				            "artist_image"=> $file_path.'images/'.$data02['artist_image']
				        );
					}
				}
            }
            
            $row['home_sections'][]=array("home_id"=>$data['id'],"home_title"=>$data['section_name'],"home_type"=>$data['section_type'],"home_content"=>$home_content2);
            unset($home_content2);
      	}
      	
      	else if($data['section_type']=="album"){
      	    
            foreach($id_list as $alids){
                
                $query03="SELECT * FROM tbl_album WHERE tbl_album.`aid`='$alids' ORDER BY tbl_album.aid DESC";
				$sql03 = mysqli_query($mysqli,$query03);
				
				if(mysqli_num_rows($sql03) > 0){
				    while($data03 = mysqli_fetch_assoc($sql03)) {
				        $home_content3[]= array(
				            "aid"=>$data03['aid'],
				            "album_name"=>$data03['album_name'],
				            "album_image"=> $file_path.'images/'.$data03['album_image']
				        );
					}
				}
            }
            
            $row['home_sections'][]=array("home_id"=>$data['id'],"home_title"=>$data['section_name'],"home_type"=>$data['section_type'],"home_content"=>$home_content3);
            unset($home_content3);
      	}
      	
      	else if($data['section_type']=="playlist"){
      	    
            foreach($id_list as $pids){
                
                $query04="SELECT * FROM tbl_playlist WHERE tbl_playlist.`pid`='$pids' ORDER BY tbl_playlist.pid DESC";
				$sql04 = mysqli_query($mysqli,$query04);
				
				if(mysqli_num_rows($sql04) > 0){
				    while($data04 = mysqli_fetch_assoc($sql04)) {
				        $home_content4[]= array(
				            "pid"=>$data04['pid'],
				            "playlist_name"=>$data04['playlist_name'],
				            "playlist_image"=> $file_path.'images/'.$data04['playlist_image']
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
                    WHERE tbl_audio.`id` ='$sids' AND tbl_category.`status`=1 AND tbl_audio.`audio_status`=1 ORDER BY tbl_audio.`id` DESC";
				$sql05 = mysqli_query($mysqli,$query05);
				if(mysqli_num_rows($sql05) > 0){
				    while($data05 = mysqli_fetch_assoc($sql05)) {
				        
				        $data_arr['id'] = $data05['id'];
                		$data_arr['audio_title'] = $data05['audio_title'];
                		
                		$audio_file=$data05['audio_url'];
                		if($data05['audio_type']=='local'){
                			$audio_file=$file_path.'uploads/'.basename($data05['audio_url']);
                		}
                		$data_arr['audio_url'] = $audio_file;
                		
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
                		
                		$data_arr['aid'] = $data05['aid'];
                		$data_arr['category_name'] = $data05['category_name'];
                		
                		array_push($jsonObj, $data_arr);
					}
				}
            }
            $row['home_sections'][]=array("home_id"=>$data['id'],"home_title"=>$data['section_name'],"home_type"=>$data['section_type'],"home_content"=>$jsonObj);
            unset($jsonObj);
      	}
	}
    $set[$API_NAME] = $row;
	header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
	die();
}
else if($_POST['helper_name']=="get_release"){
    
    $limit = 15;

    $jsonObj = array();
	$data_arr = array();
	
	$sql="SELECT * FROM tbl_audio
        LEFT JOIN tbl_album ON tbl_audio.`album_id`= tbl_album.`aid` 
        LEFT JOIN tbl_category ON tbl_audio.`cat_id`= tbl_category.`cid` 
        WHERE tbl_audio.`id` AND tbl_category.`status`=1 AND tbl_audio.`audio_status`=1 ORDER BY tbl_audio.`id` DESC LIMIT $limit";
    $result = mysqli_query($mysqli, $sql);
    while ($data = mysqli_fetch_assoc($result)){
        $data_arr['id'] = $data['id'];
		$data_arr['audio_title'] = $data['audio_title'];
		
		$audio_file=$data['audio_url'];
		if($data['audio_type']=='local'){
			$audio_file=$file_path.'uploads/'.basename($data['audio_url']);
		}
		$data_arr['audio_url'] = $audio_file;
		
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
		
		$data_arr['aid'] = $data['aid'];
		$data_arr['category_name'] = $data['category_name'];
		
		array_push($jsonObj, $data_arr);
	}
	$row['release_songs'] = $jsonObj;
	
	
	mysqli_free_result($result);
    $jsonObj = array();
	$data_arr = array();
	
	$sql = "SELECT * FROM tbl_album WHERE status='1' ORDER BY tbl_album.`aid` DESC LIMIT $limit";
	$result = mysqli_query($mysqli, $sql);
	while ($data = mysqli_fetch_assoc($result)){
		$data_arr['aid'] = $data['aid'];
		$data_arr['album_name'] = $data['album_name'];
		$data_arr['album_image'] = $file_path.'images/'.$data['album_image'];
		
		array_push($jsonObj, $data_arr);
	}
	$row['release_album'] = $jsonObj;
	
	mysqli_free_result($result);
	$jsonObj = array();
	$data_arr = array();
	
	$sql = "SELECT * FROM tbl_artist ORDER BY RAND() DESC LIMIT $limit";
	$result = mysqli_query($mysqli, $sql);
	while ($data = mysqli_fetch_assoc($result)){
		$data_arr['id'] = $data['id'];
		$data_arr['artist_name'] = $data['artist_name'];
		$data_arr['artist_image'] = $file_path.'images/'.$data['artist_image'];
		
		array_push($jsonObj, $data_arr);
	}
	$row['release_artist'] = $jsonObj;
	
	mysqli_free_result($result);
	$jsonObj = array();
	$data_arr = array();
	
	$sql="SELECT * FROM tbl_category WHERE tbl_category.status='1' ORDER BY tbl_category.cid DESC LIMIT $limit";
	$result = mysqli_query($mysqli, $sql);
	while ($data = mysqli_fetch_assoc($result)){
		$data_arr['cid'] = $data['cid'];
        $data_arr['category_name'] = $data['category_name'];
        $data_arr['category_image'] = $file_path.'images/'.$data['category_image'];
		
		array_push($jsonObj, $data_arr);
	}
	$row['release_category'] = $jsonObj;
	
	mysqli_free_result($result);
	$jsonObj = array();
	$data_arr = array();
	
	$sql = "SELECT * FROM tbl_playlist WHERE tbl_playlist.`pid` AND status='1' ORDER BY tbl_playlist.`pid` DESC LIMIT $limit";
	$result = mysqli_query($mysqli, $sql);
	while ($data = mysqli_fetch_assoc($result)){
		$data_arr['pid'] = $data['pid'];
		$data_arr['playlist_name'] = $data['playlist_name'];
		$data_arr['playlist_image'] = $file_path.'images/'.$data['playlist_image'];
		
		array_push($jsonObj, $data_arr);
	}
	$row['release_playlist'] = $jsonObj;

    $set[$API_NAME] = $row;
	header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
	die();
}
else if($_POST['helper_name']=="get_search"){
    
    $search_text=addslashes(trim($_POST['search_text']));
    
    $limit = 30;

    $jsonObj = array();
	$data_arr = array();
	
	$sql="SELECT * FROM tbl_audio
        LEFT JOIN tbl_album ON tbl_audio.`album_id`= tbl_album.`aid` 
        LEFT JOIN tbl_category ON tbl_audio.`cat_id`= tbl_category.`cid` 
        WHERE tbl_audio.`audio_title` like '%$search_text%' AND tbl_audio.`id` AND tbl_category.`status`=1 AND tbl_audio.`audio_status`=1 ORDER BY tbl_audio.`id` DESC LIMIT $limit";
    $result = mysqli_query($mysqli, $sql);
    while ($data = mysqli_fetch_assoc($result)){
        $data_arr['id'] = $data['id'];
		$data_arr['audio_title'] = $data['audio_title'];
		
		$audio_file=$data['audio_url'];
		if($data['audio_type']=='local'){
			$audio_file=$file_path.'uploads/'.basename($data['audio_url']);
		}
		$data_arr['audio_url'] = $audio_file;
		
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
		
		$data_arr['aid'] = $data['aid'];
		$data_arr['category_name'] = $data['category_name'];
		
		array_push($jsonObj, $data_arr);
	}
	$row['release_songs'] = $jsonObj;
	
	
	mysqli_free_result($result);
    $jsonObj = array();
	$data_arr = array();
	
	$sql = "SELECT * FROM tbl_album WHERE tbl_album.`album_name` like '%$search_text%' AND status='1' ORDER BY tbl_album.`aid` DESC LIMIT $limit";
	$result = mysqli_query($mysqli, $sql);
	while ($data = mysqli_fetch_assoc($result)){
		$data_arr['aid'] = $data['aid'];
		$data_arr['album_name'] = $data['album_name'];
		$data_arr['album_image'] = $file_path.'images/'.$data['album_image'];
		
		array_push($jsonObj, $data_arr);
	}
	$row['release_album'] = $jsonObj;
	
	mysqli_free_result($result);
	$jsonObj = array();
	$data_arr = array();
	
	$sql = "SELECT * FROM tbl_artist WHERE tbl_artist.`artist_name` like '%$search_text%' ORDER BY RAND() DESC LIMIT $limit";
	$result = mysqli_query($mysqli, $sql);
	while ($data = mysqli_fetch_assoc($result)){
		$data_arr['id'] = $data['id'];
		$data_arr['artist_name'] = $data['artist_name'];
		$data_arr['artist_image'] = $file_path.'images/'.$data['artist_image'];
		
		array_push($jsonObj, $data_arr);
	}
	$row['release_artist'] = $jsonObj;
	
	mysqli_free_result($result);
	$jsonObj = array();
	$data_arr = array();
	
	$sql="SELECT * FROM tbl_category WHERE tbl_category.`category_name` like '%$search_text%' AND tbl_category.status='1' ORDER BY tbl_category.cid DESC LIMIT $limit";
	$result = mysqli_query($mysqli, $sql);
	while ($data = mysqli_fetch_assoc($result)){
		$data_arr['cid'] = $data['cid'];
        $data_arr['category_name'] = $data['category_name'];
        $data_arr['category_image'] = $file_path.'images/'.$data['category_image'];
		
		array_push($jsonObj, $data_arr);
	}
	$row['release_category'] = $jsonObj;
	
	mysqli_free_result($result);
	$jsonObj = array();
	$data_arr = array();
	
	$sql = "SELECT * FROM tbl_playlist WHERE tbl_playlist.`playlist_name` like '%$search_text%' AND tbl_playlist.`pid` AND status='1' ORDER BY tbl_playlist.`pid` DESC LIMIT $limit";
	$result = mysqli_query($mysqli, $sql);
	while ($data = mysqli_fetch_assoc($result)){
		$data_arr['pid'] = $data['pid'];
		$data_arr['playlist_name'] = $data['playlist_name'];
		$data_arr['playlist_image'] = $file_path.'images/'.$data['playlist_image'];
		
		array_push($jsonObj, $data_arr);
	}
	$row['release_playlist'] = $jsonObj;

    $set[$API_NAME] = $row;
	header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
	die();
}
else if($_POST['helper_name']=="trending_songs"){
    
    $jsonObj= array();
    
    $sql_views="SELECT *,DATE_FORMAT(`date`, '%m/%d/%Y') FROM `tbl_audio_views` WHERE `date` BETWEEN NOW() - INTERVAL 30 DAY AND NOW() GROUP BY `audio_id` ORDER BY views DESC LIMIT 25";
	$res_views=mysqli_query($mysqli, $sql_views);
	
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
    		
    		$data_arr['aid'] = $data['aid'];
    		$data_arr['category_name'] = $data['category_name'];
    		
    		array_push($jsonObj, $data_arr);
    	}
	}
    $set[$API_NAME] = $jsonObj;
    header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
}
else if($_POST['helper_name']=="single_song"){
    
    $jsonObj= array();

    $sql="SELECT * FROM tbl_audio
        LEFT JOIN tbl_album ON tbl_audio.`album_id`= tbl_album.`aid` 
        LEFT JOIN tbl_category ON tbl_audio.`cat_id`= tbl_category.`cid` 
        WHERE tbl_audio.`id`='" . $_POST['song_id'] . "' AND tbl_category.`status`=1 AND tbl_audio.`audio_status`=1 ORDER BY tbl_audio.`id` DESC";
    $result = mysqli_query($mysqli, $sql);
    while($data = mysqli_fetch_assoc($result)){
        $data_arr['id'] = $data['id'];
		$data_arr['audio_title'] = $data['audio_title'];
		
		$audio_file=$data['audio_url'];
		if($data['audio_type']=='local'){
			$audio_file=$file_path.'uploads/'.basename($data['audio_url']);
		}
		$data_arr['audio_url'] = $audio_file;
		
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
		
		$data_arr['aid'] = $data['aid'];
		$data_arr['album_name'] = $data['album_name'];
		$data_arr['category_name'] = $data['category_name'];
		
        array_push($jsonObj,$data_arr);
    }
    $set[$API_NAME] = $jsonObj;
    header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
}
else if($_POST['helper_name']=="release_artists"){
    
    $jsonObj= array();
    
    $sql="SELECT * FROM tbl_artist WHERE tbl_artist.`id` ORDER BY tbl_artist.id DESC LIMIT 10";
    $result = mysqli_query($mysqli, $sql);
    while($data = mysqli_fetch_assoc($result)){
        
        $data_arr['id'] = $data['id'];
		$data_arr['artist_name'] = $data['artist_name'];
		$data_arr['artist_image'] = $file_path.'images/'.$data['artist_image'];
		
        array_push($jsonObj,$data_arr);
    }
    $set[$API_NAME] = $jsonObj;
    header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
}
else if($_POST['helper_name']=="latest_songs"){
    
    $jsonObj= array();
    
    $sql="SELECT * FROM tbl_audio
        LEFT JOIN tbl_album ON tbl_audio.`album_id`= tbl_album.`aid` 
        LEFT JOIN tbl_category ON tbl_audio.`cat_id`= tbl_category.`cid` 
        WHERE tbl_audio.`id` AND tbl_category.`status`=1 AND tbl_audio.`audio_status`=1 ORDER BY tbl_audio.`id` DESC LIMIT 50";
    $result = mysqli_query($mysqli, $sql);
    while($data = mysqli_fetch_assoc($result)){
        $data_arr['id'] = $data['id'];
		$data_arr['audio_title'] = $data['audio_title'];
		
		$audio_file=$data['audio_url'];
		if($data['audio_type']=='local'){
			$audio_file=$file_path.'uploads/'.basename($data['audio_url']);
		}
		$data_arr['audio_url'] = $audio_file;
		
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
		
		$data_arr['aid'] = $data['aid'];
		$data_arr['category_name'] = $data['category_name'];
		
        array_push($jsonObj,$data_arr);
    }
    $set[$API_NAME] = $jsonObj;
    header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
}

else if($_POST['helper_name']=="cat_list"){
  
    $jsonObj= array();
    
    $sql="SELECT * FROM tbl_category WHERE tbl_category.status='1' ORDER BY tbl_category.cid DESC";
    $result = mysqli_query($mysqli, $sql);
    while($data = mysqli_fetch_assoc($result)){
        
        $row['cid'] = $data['cid'];
        $row['category_name'] = $data['category_name'];
        $row['category_image'] = $file_path.'images/'.$data['category_image'];
        
        array_push($jsonObj,$row);
    }
    $set[$API_NAME] = $jsonObj;
    header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
}
else if($_POST['helper_name']=="album_list"){
    
    $jsonObj= array();

    $sql = "SELECT * FROM tbl_album WHERE tbl_album.`aid` AND status='1' ORDER BY tbl_album.`aid` DESC";
    $result = mysqli_query($mysqli, $sql);
    while($data = mysqli_fetch_assoc($result)){
        
        $row['aid'] = $data['aid'];
		$row['album_name'] = $data['album_name'];
		$row['album_image'] = $file_path.'images/'.$data['album_image'];
        
        array_push($jsonObj,$row);
    }
    $set[$API_NAME] = $jsonObj;
    header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
}
else if($_POST['helper_name']=="artist_list"){
    
    $jsonObj= array();

    $sql="SELECT * FROM tbl_artist WHERE tbl_artist.`id` ORDER BY tbl_artist.id DESC";
    $result = mysqli_query($mysqli, $sql);
    while($data = mysqli_fetch_assoc($result)){
        
        $row['id'] = $data['id'];
		$row['artist_name'] = $data['artist_name'];
		$row['artist_image'] = $file_path.'images/'.$data['artist_image'];
        
        array_push($jsonObj,$row);
    }
    $set[$API_NAME] = $jsonObj;
    header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
}
else if($_POST['helper_name']=="playlists_list"){

    $jsonObj= array();

    $sql = "SELECT * FROM tbl_playlist WHERE tbl_playlist.`pid` AND status='1' ORDER BY tbl_playlist.`pid` DESC";
    $result = mysqli_query($mysqli, $sql);
    while($data = mysqli_fetch_assoc($result)){
        
        $row['pid'] = $data['pid'];
		$row['playlist_name'] = $data['playlist_name'];
		$row['playlist_image'] = $file_path.'images/'.$data['playlist_image'];
        
        array_push($jsonObj,$row);
    }
    $set[$API_NAME] = $jsonObj;
    header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
}

else if($_POST['helper_name']=="cat_album_list"){
    
    $cat_id = isset($_POST['cat_id']) ? $_POST['cat_id'] : 0;

    $jsonObj= array();

    $sql = "SELECT * FROM tbl_album WHERE tbl_album.`catid`='$cat_id' AND status='1' ORDER BY tbl_album.`aid` DESC";
    $result = mysqli_query($mysqli, $sql);
    while($data = mysqli_fetch_assoc($result)){
        
        $row['aid'] = $data['aid'];
		$row['album_name'] = $data['album_name'];
		$row['album_image'] = $file_path.'images/'.$data['album_image'];
        
        array_push($jsonObj,$row);
    }
    $set[$API_NAME] = $jsonObj;
    header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
}

else if($_POST['helper_name']=="playlist_songs") {
    
	$playlist_id=isset($_POST['playlist_id']) ? $_POST['playlist_id'] : 0;
	
    $sql_playlist="SELECT * FROM tbl_playlist WHERE status='1' AND `pid`='$playlist_id' ORDER BY tbl_playlist.`pid` DESC";
	$res_playlist = mysqli_query($mysqli,$sql_playlist);
	$row_playlist=mysqli_fetch_assoc($res_playlist);

	$songs_ids = trim($row_playlist['playlist_audio']);
	
	$jsonObj= array();

    $sql="SELECT * FROM tbl_audio
        LEFT JOIN tbl_album ON tbl_audio.`album_id`= tbl_album.`aid` 
        LEFT JOIN tbl_category ON tbl_audio.`cat_id`= tbl_category.`cid` 
        WHERE tbl_audio.`id` IN ($songs_ids) AND tbl_category.`status`=1 AND tbl_audio.`audio_status`=1 ORDER BY tbl_audio.`total_views` DESC";
    $result = mysqli_query($mysqli, $sql);
    while($data = mysqli_fetch_assoc($result)){
        
        $row['id'] = $data['id'];
		$row['audio_title'] = $data['audio_title'];
		
		$audio_file=$data['audio_url'];
		if($data['audio_type']=='local'){
			$audio_file=$file_path.'uploads/'.basename($data['audio_url']);
		}
		$row['audio_url'] = $audio_file;
		
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
		
		$row['aid'] = $data['aid'];
		$row['category_name'] = $data['category_name'];
		
        array_push($jsonObj,$row);
    }
    $set[$API_NAME] = $jsonObj;
    header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
}
else if($_POST['helper_name']=="category_songs"){
    
    $cat_id=isset($_POST['cat_id']) ? $_POST['cat_id'] : 0;
    
    $jsonObj= array();

    $sql="SELECT * FROM tbl_audio
        LEFT JOIN tbl_album ON tbl_audio.`album_id`= tbl_album.`aid` 
        LEFT JOIN tbl_category ON tbl_audio.`cat_id`= tbl_category.`cid` 
        WHERE tbl_audio.`cat_id`='$cat_id' AND tbl_category.`status`=1 AND tbl_audio.`audio_status`=1 ORDER BY tbl_audio.`id` DESC";
    $result = mysqli_query($mysqli, $sql);
    
    while($data = mysqli_fetch_assoc($result)){
        $row['id'] = $data['id'];
		$row['audio_title'] = $data['audio_title'];
		
		$audio_file=$data['audio_url'];
		if($data['audio_type']=='local'){
			$audio_file=$file_path.'uploads/'.basename($data['audio_url']);
		}
		$row['audio_url'] = $audio_file;
		
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
		
		$row['aid'] = $data['aid'];
		$row['category_name'] = $data['category_name'];
		
        array_push($jsonObj,$row);
    }
    $set[$API_NAME] = $jsonObj;
    header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
}
else if($_POST['helper_name']=="banner_songs"){
    
    $banner_id=isset($_POST['banner_id']) ? $_POST['banner_id'] : 0;
    
    $sql_banner="SELECT * FROM tbl_banner WHERE status='1' AND `bid`='$banner_id' ORDER BY tbl_banner.`bid` DESC";
	$res_banner = mysqli_query($mysqli,$sql_banner);
	$row_banner=mysqli_fetch_assoc($res_banner);

	$songs_ids = trim($row_banner['banner_post_id']);

    $jsonObj= array();
    
    $sql="SELECT * FROM tbl_audio
        LEFT JOIN tbl_album ON tbl_audio.`album_id`= tbl_album.`aid` 
        LEFT JOIN tbl_category ON tbl_audio.`cat_id`= tbl_category.`cid` 
        WHERE tbl_audio.`id` IN ($songs_ids) AND tbl_category.`status`=1 AND tbl_audio.`audio_status`=1 ORDER BY tbl_audio.`total_views` DESC";
    $result = mysqli_query($mysqli, $sql);
    while($data = mysqli_fetch_assoc($result)){
        $row['id'] = $data['id'];
		$row['audio_title'] = $data['audio_title'];
		
		$audio_file=$data['audio_url'];
		if($data['audio_type']=='local'){
			$audio_file=$file_path.'uploads/'.basename($data['audio_url']);
		}
		$row['audio_url'] = $audio_file;
		
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
		
		$row['aid'] = $data['aid'];
		$row['category_name'] = $data['category_name'];
		
        array_push($jsonObj,$row);
    }
    $set[$API_NAME] = $jsonObj;
    header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
}
else if($_POST['helper_name']=="album_songs"){
    
    $album_id=isset($_POST['album_id']) ? $_POST['album_id'] : 0;
    
    $jsonObj= array();
    
    $sql="SELECT * FROM tbl_audio
        LEFT JOIN tbl_album ON tbl_audio.`album_id`= tbl_album.`aid` 
        LEFT JOIN tbl_category ON tbl_audio.`cat_id`= tbl_category.`cid` 
        WHERE tbl_audio.`album_id`='$album_id' AND tbl_category.`status`=1 AND tbl_audio.`audio_status`=1 ORDER BY tbl_audio.`id` DESC";
    $result = mysqli_query($mysqli, $sql);
    while($data = mysqli_fetch_assoc($result)){
        
        $row['id'] = $data['id'];
		$row['audio_title'] = $data['audio_title'];
		
		$audio_file=$data['audio_url'];
		if($data['audio_type']=='local'){
			$audio_file=$file_path.'uploads/'.basename($data['audio_url']);
		}
		$row['audio_url'] = $audio_file;
		
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
		
		$row['aid'] = $data['aid'];
		$row['category_name'] = $data['category_name'];
		
        array_push($jsonObj,$row);
    }
    $set[$API_NAME] = $jsonObj;
    header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
}
else if($_POST['helper_name']=="artists_songs"){
    
    $artists_id=isset($_POST['artists_id']) ? $_POST['artists_id'] : 0;

    $sql_artists = "SELECT * FROM tbl_artist WHERE `id`='$artists_id' ORDER BY tbl_artist.`id` DESC";
	$res_artists = mysqli_query($mysqli,$sql_artists);
	$row_artists = mysqli_fetch_assoc($res_artists);

	$artist_name = trim($row_artists['artist_name']);
	
	$jsonObj= array();

    $sql="SELECT * FROM tbl_audio
        LEFT JOIN tbl_album ON tbl_audio.`album_id`= tbl_album.`aid` 
        LEFT JOIN tbl_category ON tbl_audio.`cat_id`= tbl_category.`cid` 
        WHERE FIND_IN_SET('$artist_name',tbl_audio.`audio_artist`) AND tbl_category.`status`=1 AND tbl_audio.`audio_status`=1 ORDER BY tbl_audio.`total_views` DESC";
    $result = mysqli_query($mysqli, $sql);
    while($data = mysqli_fetch_assoc($result)){
        
        $row['id'] = $data['id'];
		$row['audio_title'] = $data['audio_title'];
		
		$audio_file=$data['audio_url'];
		if($data['audio_type']=='local'){
			$audio_file=$file_path.'uploads/'.basename($data['audio_url']);
		}
		$row['audio_url'] = $audio_file;
		
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
		
		$row['aid'] = $data['aid'];
		$row['category_name'] = $data['category_name'];
		
        array_push($jsonObj,$row);
    }
    $set[$API_NAME] = $jsonObj;
    header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
}

else if($_POST['helper_name']=="total_views"){
    
    $view_qry = mysqli_query($mysqli, "UPDATE tbl_audio SET total_views = total_views + 1 WHERE id = '" . $_POST['song_id'] . "'");
    
    $song_id = $_POST['song_id'];
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
	    $sql2 = mysqli_query($mysqli, $query2);
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
	
	$set[$API_NAME][]=array('MSG'=> 'success','success'=> '1');
    header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
}

else if($_POST['helper_name']=="playlist_details"){
    
    $playlist_id=isset($_POST['playlist_id']) ? $_POST['playlist_id'] : 0;
    
    $jsonObj= array();
    
    $sql_playlist="SELECT * FROM tbl_playlist WHERE status='1' AND `pid`='$playlist_id' ORDER BY tbl_playlist.`pid` DESC";
    $result = mysqli_query($mysqli, $sql_playlist);
    while($data = mysqli_fetch_assoc($result)){
        
        $data_arr['pid'] = $data['pid'];
		$data_arr['playlist_name'] = $data['playlist_name'];
		$data_arr['playlist_image'] = $file_path.'images/'.$data['playlist_image'];
		
        array_push($jsonObj,$data_arr);
    }
    $set[$API_NAME] = $jsonObj;
    header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
}
else if($_POST['helper_name']=="category_details"){
    
    $cat_id=isset($_POST['cat_id']) ? $_POST['cat_id'] : 0;
    
    $jsonObj= array();
    
    $sql="SELECT * FROM tbl_category WHERE tbl_category.`cid`='$cat_id' AND tbl_category.status='1' ORDER BY tbl_category.cid DESC";
    $result = mysqli_query($mysqli, $sql);
    while($data = mysqli_fetch_assoc($result)){
        
        $data_arr['cid'] = $data['cid'];
        $data_arr['category_name'] = $data['category_name'];
        $data_arr['category_image'] = $file_path.'images/'.$data['category_image'];
		
        array_push($jsonObj,$data_arr);
    }
    $set[$API_NAME] = $jsonObj;
    header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
}
else if($_POST['helper_name']=="banner_details"){
    
    $banner_id=isset($_POST['banner_id']) ? $_POST['banner_id'] : 0;
    
    $jsonObj= array();
    
    $sql_banner="SELECT * FROM tbl_banner WHERE status='1' AND `bid`='$banner_id' ORDER BY tbl_banner.`bid` DESC";
    $result = mysqli_query($mysqli, $sql_banner);
    while($data = mysqli_fetch_assoc($result)){
        $data_arr['bid'] = $data['bid'];
        $data_arr['banner_title'] = $data['banner_title'];
        $data_arr['banner_info'] = $data['banner_info'];
        $data_arr['banner_image'] = $file_path.'images/'.$data['banner_image'];
        array_push($jsonObj,$data_arr);
    }
    $set[$API_NAME] = $jsonObj;
    header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
}
else if($_POST['helper_name']=="album_details"){
    
    $album_id=isset($_POST['album_id']) ? $_POST['album_id'] : 0;
    
    $jsonObj= array();
    
    $sql = "SELECT * FROM tbl_album WHERE tbl_album.`aid`='$album_id' AND status='1' ORDER BY tbl_album.`aid` DESC";
    $result = mysqli_query($mysqli, $sql);
    while($data = mysqli_fetch_assoc($result)){
        
        $data_arr['aid'] = $data['aid'];
		$data_arr['album_name'] = $data['album_name'];
		$data_arr['album_image'] = $file_path.'images/'.$data['album_image'];
		
        array_push($jsonObj,$data_arr);
    }
    $set[$API_NAME] = $jsonObj;
    header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
}
else if($_POST['helper_name']=="artists_details"){
    
    $artists_id=isset($_POST['artists_id']) ? $_POST['artists_id'] : 0;
    
    $jsonObj= array();
    
    $sql = "SELECT * FROM tbl_artist WHERE tbl_artist.`id`='$artists_id' ORDER BY tbl_artist.`id` DESC";
    $result = mysqli_query($mysqli, $sql);
    while($data = mysqli_fetch_assoc($result)){
        
        $data_arr['id'] = $data['id'];
		$data_arr['artist_name'] = $data['artist_name'];
		$data_arr['artist_image'] = $file_path.'images/'.$data['artist_image'];
		
        array_push($jsonObj,$data_arr);
    }
    $set[$API_NAME] = $jsonObj;
    header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
}

else if($_POST['helper_name']=="user_register"){

    $email=isset($_POST['user_email']) ? addslashes(trim($_POST['user_email'])) : '';
  
    $to=isset($_POST['user_email']) ? $_POST['user_email'] : '';
    $recipient_name=isset($_POST['user_name']) ? $_POST['user_name'] : '';

	$subject = str_replace('###', APP_NAME, $app_lang['register_mail_lbl']);

	$response=array();

	$user_id='';
	
	$sql = "SELECT * FROM tbl_users WHERE user_email = '$email'"; 
    $result = mysqli_query($mysqli, $sql);
    $row = mysqli_fetch_assoc($result);
    
    if (!filter_var($_POST['user_email'], FILTER_VALIDATE_EMAIL)) {
        $response=array('MSG' => $app_lang['invalid_email_format'],'success'=>'0');
    }
    else if($row['user_email']!="") {
        $response=array('MSG' => $app_lang['email_exist'],'success'=>'0');
    }
    else {

        $data = [
            'user_name' => addslashes(trim($_POST['user_name'])),
            'user_email' => addslashes(trim($_POST['user_email'])),
            'user_phone' => addslashes(trim($_POST['user_phone'])),
            'user_password' => password_hash(trim($_POST['user_password']), PASSWORD_DEFAULT),
            'user_gender'  => addslashes(trim($_POST['user_gender'])),
            'registered_on'  =>  strtotime(date('d-m-Y h:i:s A')),
            'profile_img' => '',
            'status'  =>  '1'
        ];
        
        $qry = Insert('tbl_users',$data);
        
        $user_id=mysqli_insert_id($mysqli);
        
        send_register_email($to, $recipient_name, $subject, $app_lang['normal_register_msg']);
        
        $response=array('MSG' => $app_lang['register_success'],'success'=>'1');
        
        update_activity_log($user_id);
    }
    
	$set[$API_NAME][]=$response;
	header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
	die();
}
else if($_POST['helper_name']=="user_login"){
    
    $response=array();

    $email=isset($_POST['user_email']) ? trim($_POST['user_email']) : '';
    $password=isset($_POST['user_password']) ? trim($_POST['user_password']) : '';
    
    if (!filter_var($email, FILTER_VALIDATE_EMAIL) AND $email!='') {
        $response=array('MSG' => $app_lang['invalid_email_format'],'success'=>'0');
        $set[$API_NAME][]=$response;
        header( 'Content-Type: application/json; charset=utf-8' );
        echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
        die();
    }
    
    $qry = "SELECT * FROM tbl_users WHERE user_email = '$email' AND (`user_type`='Normal' OR `user_type`='normal') AND `id` <> 0"; 
    $result = mysqli_query($mysqli,$qry);
    $num_rows = mysqli_num_rows($result);
    if($num_rows > 0) {
        $row = mysqli_fetch_assoc($result);
        if($row['status']==1) {
            
            if (password_verify($password, $row['user_password'])) {
                
                $user_id=$row['id'];
                
                update_activity_log($user_id);
                
                $response = array('user_id' =>  $row['id'],'user_name'=> $row['user_name'],'MSG' => $app_lang['login_success'],'success'=>'1'); 
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
    $set[$API_NAME][]=$response;
    header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
}
else if($_POST['helper_name'] == "user_profile") {
	$jsonObj= array();	
	
	$user_id=cleanInput($_POST['user_id']);

	$qry = "SELECT * FROM tbl_users WHERE id = '$user_id'"; 
	$result = mysqli_query($mysqli,$qry);
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
else if($_POST['helper_name']=="profile_update"){
    
	$qry = "SELECT * FROM tbl_users WHERE id = '".$_POST['user_id']."'"; 
	$result = mysqli_query($mysqli,$qry);
	$row = mysqli_fetch_assoc($result);
  
  	if (!filter_var($_POST['user_email'], FILTER_VALIDATE_EMAIL)) {
  	    $set[$API_NAME][]=array('MSG' => $app_lang['invalid_user_type'],'success'=>'0');
	}
	else if($row['user_email']==$_POST['user_email'] AND $row['id']!=$_POST['user_id']) {
        $set[$API_NAME][]=array('MSG' => $app_lang['email_not_found'],'success'=>'0');
	} 
	else {
        $data = array(
            'user_name'  =>  cleanInput($_POST['user_name']),
            'user_email'  =>  trim($_POST['user_email']),
            'user_phone'  =>  cleanInput($_POST['user_phone']),
            'user_gender'  =>  cleanInput($_POST['user_gender'])
		);
		
		$user_edit=Update('tbl_users', $data, "WHERE id = '".$_POST['user_id']."'");
		
		$set[$API_NAME][] = array('MSG' => $app_lang['update_success'], 'success' => '1');
	}
    header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
}
else if($_POST['helper_name']=="profile_password"){
    
    $qry = "SELECT * FROM tbl_users WHERE id = '".$_POST['user_id']."'"; 
	$result = mysqli_query($mysqli,$qry);
	$row = mysqli_fetch_assoc($result);
  
  	if($row['id']!=$_POST['user_id']) {
        $set[$API_NAME][]=array('MSG' => $app_lang['email_not_found'],'success'=>'0');
	} 
	else {
	    
	    if($_POST['user_password']!=""){
	        $data = array(
                'user_password'  =>  password_hash(trim($_POST['user_password']), PASSWORD_DEFAULT)
    		);
    		
    		$user_edit=Update('tbl_users', $data, "WHERE id = '".$_POST['user_id']."'");
    		
    		$set[$API_NAME][] = array('MSG' => $app_lang['update_success'], 'success' => '1');
	    } else {
	        $set[$API_NAME][] = array('MSG' => 'no data', 'success' => '0');
	    }
	}
    header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
}

else if($_POST['helper_name']=="get_notification") {
    
    $user_id=isset($_POST['user_id']) ? cleanInput($_POST['user_id']) : 0;

    $jsonObj= array();

    $query="SELECT * FROM tbl_notification WHERE `user_id`='$user_id' ORDER BY tbl_notification.`id` DESC"; 
	$sql = mysqli_query($mysqli,$query)or die(mysqli_error($mysqli));
	while($data = mysqli_fetch_assoc($sql)){
		$row['id'] = $data['id'];
      	$row['notification_title'] = $data['notification_title'];
      	$row['notification_msg'] = $data['notification_msg']; 
		$row['notification_on'] = calculate_time_span($data['notification_on'],true);		 
		array_push($jsonObj,$row);
	}
	$set[$API_NAME] = $jsonObj;
	header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
	die();
}
else if($_POST['helper_name']=="clear_notification"){
    
    $ids = cleanInput($_POST['user_id']);
    
    $jsonObj= array();	
    
    $sql="SELECT * FROM tbl_users WHERE `id`='$ids'";
    $res=mysqli_query($mysqli, $sql);
    if(mysqli_num_rows($res) > 0) {
        
        $deleteSql="DELETE FROM tbl_notification WHERE `user_id` IN ($ids)";
        mysqli_query($mysqli, $deleteSql);
        
        $set[$API_NAME][]=array('MSG'=> "clear success",'success'=> '1');
    } else {
        $set[$API_NAME][]=array('MSG'=> 'clear error','success'=> '0');
    }
	header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
    die();
}

else if($_POST['helper_name']=="post_suggest"){	
    
	$user_id=cleanInput($_POST['user_id']);
	$suggest_title=cleanInput($_POST['suggest_title']);
	$suggest_message=cleanInput($_POST['suggest_message']);
    	
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
else if($_POST['helper_name']=="post_report"){
    
    $jsonObj= array();
    $post_id = cleanInput($_POST['post_id']);
	$user_id = cleanInput($_POST['user_id']);
	$report_title = cleanInput($_POST['report_title']);
	$report_msg = cleanInput($_POST['report_msg']);
    
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

else if($_POST['helper_name']=="web_details"){

    $jsonObj= array();	
	$query="SELECT * FROM tbl_web_settings WHERE id='1'";
	$sql = mysqli_query($mysqli,$query);
	
	while($data = mysqli_fetch_assoc($sql)){
	    
        // App Details
        $row['admin_panel'] = $data['admin_panel'];
        $row['site_name'] = $data['site_name'];
        $row['site_description'] = $data['site_description'];
        $row['site_keywords'] = $data['site_keywords'];
        $row['copyright_text'] = $data['copyright_text'];
        $row['web_logo_1'] = $file_path.'images/'.$data['web_logo_1'];
        $row['web_logo_2'] = $file_path.'images/'.$data['web_logo_2'];
        $row['web_favicon'] = $file_path.'images/'.$data['web_favicon'];
        $row['header_code'] = $data['header_code'];
        $row['footer_code'] = $data['footer_code'];
        $row['contact_page_title'] = $data['contact_page_title'];
        $row['address'] = $data['address'];
        $row['contact_number'] = $data['contact_number'];
        $row['contact_email'] = $data['contact_email'];
        $row['android_app_url'] = $data['android_app_url'];
        $row['ios_app_url'] = $data['ios_app_url'];
        $row['facebook_url'] = $data['facebook_url'];
        $row['twitter_url'] = $data['twitter_url'];
        $row['youtube_url'] = $data['youtube_url'];
        $row['instagram_url'] = $data['instagram_url'];
        $row['about_page_title'] = $data['about_page_title'];
        $row['about_content'] = $data['about_content'];
        $row['about_status'] = $data['about_status'];
        $row['privacy_page_title'] = $data['privacy_page_title'];
        $row['privacy_content'] = $data['privacy_content'];
        $row['privacy_page_status'] = $data['privacy_page_status'];
        $row['terms_of_use_page_title'] = $data['terms_of_use_page_title'];
        $row['terms_of_use_content'] = $data['terms_of_use_content'];
        $row['terms_of_use_page_status'] = $data['terms_of_use_page_status'];
        $row['isSongDowload'] = $data['isSongDowload'];

        array_push($jsonObj,$row);
    }
	$set[$API_NAME] = $jsonObj;
	header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
	die();
}

else {
    $set[]=array('MSG'=> 'Directory access is forbidden.','success'=> '0');
  	header( 'Content-Type: application/json; charset=utf-8' );
    echo $val= str_replace('\\/', '/', json_encode($set,JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT));
	die();
}