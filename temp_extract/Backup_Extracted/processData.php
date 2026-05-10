<?php 
    require("includes/db_helper.php");
    require("includes/lb_helper.php");
    require("language/language.php");
	require("language/api_language.php");
	include("smtp_email.php");
    
	$response=array();
	
	$_SESSION['class'] = "success";

	switch ($_POST['action']) {
	    
	    case 'toggle_status':
        	$table_nm = $_POST['table'];
        
        	$sql_schema="SHOW COLUMNS FROM $table_nm";
        	$res_schema=mysqli_query($mysqli, $sql_schema);
        	$row_schema=mysqli_fetch_array($res_schema);
        
        	$id = $_POST['id'];
        	$for_action = $_POST['for_action'];
        	$column = $_POST['column'];
        	$tbl_id = $row_schema[0];
        
        	$message='';
        
        	if ($for_action == 'enable') {
        		$data = array($column  =>  '1');
        		$edit_status = Update($table_nm, $data, "WHERE $tbl_id = '$id'");
        		$message=$client_lang['13'];
        	} else {
        		$data = array($column  =>  '0');
        		$edit_status = Update($table_nm, $data, "WHERE $tbl_id = '$id'");
        		$message=$client_lang['14'];
        	}
        
        	$response['status'] = 1;
        	$response['action'] = $for_action;
        	$response['msg'] = $message;
        	$response['class'] = "success";
        	
        	echo json_encode($response);
    	break;
    	
    	case 'multi_action':
    
        	$action=$_POST['for_action'];
        	$table=$_POST['table'];
        
        	if(is_array($_POST['id'])) {
        	    $ids=implode(",", $_POST['id']);
        	} else {
        	    $ids=$_POST['id'];
        	}
        
        	if($action=='enable'){
        
        		$sql="UPDATE $table SET `status`='1' WHERE `id` IN ($ids)";
        		mysqli_query($mysqli, $sql);
        		$_SESSION['msg']="13";	
        		$_SESSION['class']="success";			
        	}
        	else if($action=='disable'){
        		$sql="UPDATE $table SET `status`='0' WHERE `id` IN ($ids)";
        		if(mysqli_query($mysqli, $sql)){
        			$_SESSION['msg']="14";
        			$_SESSION['class']="success";
        		}
        	}
        	else if($action=='delete'){
        	    
                if($table=='tbl_users'){
                    
                    $sql="SELECT * FROM tbl_users WHERE `id` IN ($ids)";
                    $res=mysqli_query($mysqli, $sql);
                    while ($row=mysqli_fetch_assoc($res)){
                    	if($row['profile_img']!=""){
                    		unlink('images/'.$row['profile_img']);
                    	}
                    }
                    
                    $deleteSql = "DELETE FROM tbl_active_log WHERE `user_id` IN ($ids)";
                    mysqli_query($mysqli, $deleteSql);
                    
                    $deleteSql = "DELETE FROM tbl_reports WHERE `user_id` IN ($ids)";
                    mysqli_query($mysqli, $deleteSql);
                    
                    $deleteSql = "DELETE FROM tbl_favourite WHERE `user_id` IN ($ids)";
                    mysqli_query($mysqli, $deleteSql);
                    
                    $sql = "SELECT * FROM tbl_suggest WHERE `user_id` IN ($ids)";
                    $res = mysqli_query($mysqli, $sql);
                    
                    $deleteSql = "DELETE FROM $table WHERE `id` IN ($ids)";
                    mysqli_query($mysqli, $deleteSql);
				} 
				
				else if($table=='tbl_category'){
				    
                    $sqlAudio = "SELECT * FROM tbl_audio WHERE `cat_id` IN ($ids)";
                    $res = mysqli_query($mysqli, $sqlAudio);
                    while ($row = mysqli_fetch_assoc($res)) {
                        
                        if ($row['audio_thumbnail'] != "" AND $row['thumbnail_type'] == "local_img") {
                            unlink('images/' . $row['audio_thumbnail']);
                        }
                        
                        if ($row['audio_type'] == "local") {
                            $file_name = basename($row['audio_url']);
                            unlink('uploads/' . $file_name);
                        }
                        
                        Delete('tbl_favourite','post_id='.$row['id']);
                        Delete('tbl_rating','post_id='.$row['id']);
                        Delete('tbl_reports','post_id='.$row['id']);
                        Delete('tbl_audio_views','audio_id='.$row['id']);
                    }
                    
                    $deleteSql = "DELETE FROM tbl_audio WHERE `cat_id` IN ($ids)";
                    mysqli_query($mysqli, $deleteSql);
                    mysqli_free_result($res);
                    
                    $sqlCategory="SELECT * FROM $table WHERE `cid` IN ($ids)";
                    $res=mysqli_query($mysqli, $sqlCategory);
                    while ($row=mysqli_fetch_assoc($res)){
                    	if($row['category_image']!="") {
                    		unlink('images/'.$row['category_image']);
                    	}
                    }
                    $deleteSql="DELETE FROM $table WHERE `cid` IN ($ids)";
                    mysqli_query($mysqli, $deleteSql);
                }
                
                else if($table=='tbl_reports'){
                	$sqlDelete="DELETE FROM $table WHERE `id` IN ($ids)";
                	mysqli_query($mysqli, $sqlDelete);
                } 
                
                else if ($table == 'tbl_playlist') {
                
                    $sql = "SELECT * FROM $table WHERE `pid` IN ($ids)";
                    $res = mysqli_query($mysqli, $sql);
                    while ($row = mysqli_fetch_assoc($res)) {
                        if ($row['playlist_image'] != "") {
                            unlink('images/' . $row['playlist_image']);
                        }
                    }
                    $deleteSql = "DELETE FROM $table WHERE `pid` IN ($ids)";
                    mysqli_query($mysqli, $deleteSql);
                } 
                
                else if($table=='tbl_banner'){
                    
                    $sql="SELECT * FROM tbl_banner WHERE `bid` IN ($ids)";
                    $res=mysqli_query($mysqli, $sql);
                    while ($row=mysqli_fetch_assoc($res)){
                    	if($row['banner_image']!=""){
                    		unlink('images/'.$row['banner_image']);
                    	}
                    }
                    $deleteSql = "DELETE FROM $table WHERE `bid` IN ($ids)";
                    mysqli_query($mysqli, $deleteSql);
                } 
				
				else if($table=='tbl_suggest'){
                    
                    $sql="SELECT * FROM tbl_suggest WHERE `id` IN ($ids)";
                    $res=mysqli_query($mysqli, $sql);
                    while ($row=mysqli_fetch_assoc($res)){
                    	if($row['suggest_image']!=""){
                    		unlink('images/'.$row['suggest_image']);
                    	}
                    }
                    
                    $deleteSql = "DELETE FROM $table WHERE `id` IN ($ids)";
                    mysqli_query($mysqli, $deleteSql);
				} 
				
				else if ($table == 'tbl_artist') {
				    
                    $sql = "SELECT * FROM $table WHERE `id` IN ($ids)";
                    $res = mysqli_query($mysqli, $sql);
                    while ($row = mysqli_fetch_assoc($res)) {
                        if ($row['artist_image'] != "") {
                            unlink('images/' . $row['artist_image']);
                        }
                    }
                    
                    $deleteSql = "DELETE FROM $table WHERE `id` IN ($ids)";
                    mysqli_query($mysqli, $deleteSql);
                } 
                
                else if($table=='tbl_album'){
                    
                    $sqlAudio = "SELECT * FROM tbl_audio WHERE `album_id` IN ($ids)";
                    $res = mysqli_query($mysqli, $sqlAudio);
                    while ($row = mysqli_fetch_assoc($res)) {
                        
                        if ($row['audio_thumbnail'] != "" AND $row['thumbnail_type'] == "local_img") {
                            unlink('images/' . $row['audio_thumbnail']);
                        }
                        
                        if ($row['audio_type'] == "local") {
                            $file_name = basename($row['audio_url']);
                            unlink('uploads/' . $file_name);
                        }
                        
                        Delete('tbl_favourite','post_id='.$row['id']);
                        Delete('tbl_rating','post_id='.$row['id']);
                        Delete('tbl_reports','post_id='.$row['id']);
                        Delete('tbl_audio_views','audio_id='.$row['id']);
                        
                    }
                    $deleteSql = "DELETE FROM tbl_audio WHERE `album_id` IN ($ids)";
                    mysqli_query($mysqli, $deleteSql);
                    mysqli_free_result($res);
                    
                    $sqlAlbum="SELECT * FROM $table WHERE `aid` IN ($ids)";
                    $res=mysqli_query($mysqli, $sqlAlbum);
                    while ($row=mysqli_fetch_assoc($res)){
                    	if($row['album_image']!=""){
                    		unlink('images/'.$row['album_image']);
                    	}
                    }
                    
					$deleteSql = "DELETE FROM $table WHERE `aid` IN ($ids)";
					mysqli_query($mysqli, $deleteSql);
				} 
				
				else if($table == 'tbl_audio') {
				    
                    $sql = "SELECT * FROM $table WHERE `id` IN ($ids)";
                    $res = mysqli_query($mysqli, $sql);
                    while ($row = mysqli_fetch_assoc($res)) {
                        
                        if ($row['audio_thumbnail'] != "" AND $row['thumbnail_type'] == "local_img") {
                        	unlink('images/' . $row['audio_thumbnail']);
                        }
                        
                        if ($row['audio_type'] == "local") {
                        	$file_name = basename($row['audio_url']);
                        	unlink('uploads/' . $file_name);
                        }
                        
                        Delete('tbl_favourite','post_id='.$row['id']);
                        Delete('tbl_rating','post_id='.$row['id']);
                        Delete('tbl_reports','post_id='.$row['id']);
                        Delete('tbl_audio_views','audio_id='.$row['id']);
                    }
                    $deleteSql = "DELETE FROM $table WHERE `id` IN ($ids)";
                    mysqli_query($mysqli, $deleteSql);
                }
				
				else if($table=='tbl_news'){
					$deleteSql = "DELETE FROM $table WHERE `id` IN ($ids)";
					mysqli_query($mysqli, $deleteSql);
				}  
				
				else if($table=='tbl_home_sections'){
                    $deleteSql = "DELETE FROM $table WHERE `id` IN ($ids)";
                    mysqli_query($mysqli, $deleteSql);
				}
                
                else if($table=='tbl_admin'){
                    $sqlDelete="DELETE FROM $table WHERE `id` IN ($ids)";
                    mysqli_query($mysqli, $sqlDelete);
                } 
                
                $_SESSION['msg']="12";
                $_SESSION['class']="success";
            }
        	$response['status']=1;	

    	    echo json_encode($response);
    	break;
    	
    	case 'check_smtp':{
    		$to = trim($_POST['email']);
    		$recipient_name='Check User';
    
    		$subject = '[IMPORTANT] '.APP_NAME.' Check SMTP Configuration';
    
    		$message='<div style="background-color: #f9f9f9;" align="center"><br />
    		<table style="font-family: OpenSans,sans-serif; color: #666666;" border="0" width="600" cellspacing="0" cellpadding="0" align="center" bgcolor="#FFFFFF">
    		<tbody>
    		<tr>
    		<td colspan="2" bgcolor="#FFFFFF" align="center"><img src="'.$file_path.'images/'.APP_LOGO.'" alt="header" /></td>
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
    		<p style="color: #262626; font-size: 24px; margin-top:0px;">Hi, '.$_SESSION['admin_name'].'</p>
    		<p style="color: #262626; font-size: 18px; margin-top:0px;">This is the demo mail to check SMTP Configuration. </p>
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
    
    		send_email($to,$recipient_name,$subject,$message, true);
    		$_SESSION['msg'] = "22";
    		$response['status'] = 1;
			echo json_encode($response);
    		break;
    	}
	
		default:
			# code...
			break;
	}
?>