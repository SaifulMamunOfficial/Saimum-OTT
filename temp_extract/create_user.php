<?php 
    $page_title=(isset($_GET['user_id'])) ? 'Edit User' : 'Create User';
    include("includes/header.php");
    require("includes/lb_helper.php");
    require("language/language.php");
	require_once("thumbnail_images.class.php");
	
	$page_save=(isset($_GET['user_id'])) ? 'Save' : 'Create';
	
	if(isset($_POST['submit']) and isset($_GET['add'])){
	    
	    if($_FILES['profile_img']['name']!=""){ 
	        
	        $ext = pathinfo($_FILES['profile_img']['name'], PATHINFO_EXTENSION);
            $profile_img=rand(0,99999)."_user.".$ext;
            $tpath1='images/'.$profile_img;
            
            if($ext!='png')  {
                $pic1=compress_image($_FILES["profile_img"]["tmp_name"], $tpath1, 80);
            } else {
                $tmp = $_FILES['profile_img']['tmp_name'];
                move_uploaded_file($tmp, $tpath1);
            }
            
	     } else {
	         $profile_img = "";
	     }
	     
        $data = array(
          'user_type'=>'Normal',											 
          'user_name'  => addslashes(trim($_POST['user_name'])),				    
          'user_email'  =>  addslashes(trim($_POST['user_email'])),
          'user_password'  =>  md5(trim($_POST['user_password'])),
          'user_phone'  =>  addslashes(trim($_POST['user_phone'])),
          'user_gender'  =>  $_POST['user_gender'],
          'registered_on'  =>  strtotime(date('d-m-Y h:i:s A')),
          'profile_img'  => $profile_img,
          'status'  =>  '1'
        );
        
        $qry = Insert('tbl_users',$data);
        
        $_SESSION['msg']="10";
        $_SESSION['class']='success'; 
        header("location:manage_users.php");	 
        exit;
    }
	
	if(isset($_GET['user_id'])){
        $user_qry="SELECT * FROM tbl_users where id='".$_GET['user_id']."'";
        $user_result=mysqli_query($mysqli,$user_qry);
        $user_row=mysqli_fetch_assoc($user_result);
    }
    
    if(isset($_POST['submit']) and isset($_POST['user_id'])){
        if($_POST['user_password']!="" AND $_FILES['category_image']['name']!=""){
            
            $img_res=mysqli_query($mysqli,'SELECT * FROM tbl_users WHERE id='.$_GET['user_id'].'');
            $img_res_row=mysqli_fetch_assoc($img_res);
            
            if($img_res_row['profile_img']!=""){
                unlink('images/'.$img_res_row['profile_img']);
            }
            
            $ext = pathinfo($_FILES['profile_img']['name'], PATHINFO_EXTENSION);
            $profile_img=rand(0,99999)."_user.".$ext;
            $tpath1='images/'.$profile_img;
            
            if($ext!='png')  {
                $pic1=compress_image($_FILES["profile_img"]["tmp_name"], $tpath1, 80);
            } else {
                $tmp = $_FILES['profile_img']['tmp_name'];
                move_uploaded_file($tmp, $tpath1);
            }
            
            $data = array(
                  'user_name'  => addslashes(trim($_POST['user_name'])),				    
                  'user_email'  =>  addslashes(trim($_POST['user_email'])),
                  'user_password'  =>  md5(trim($_POST['user_password'])),
                  'user_phone'  =>  addslashes(trim($_POST['user_phone'])),
                  'user_gender'  =>  $_POST['user_gender'],
                  'profile_img'  => $profile_img,
            );
        } else if($_FILES['category_image']['name']!=""){
            
            $img_res=mysqli_query($mysqli,'SELECT * FROM tbl_users WHERE id='.$_GET['user_id'].'');
            $img_res_row=mysqli_fetch_assoc($img_res);
            
            if($img_res_row['profile_img']!=""){
                unlink('images/'.$img_res_row['profile_img']);
            }
            
            $ext = pathinfo($_FILES['profile_img']['name'], PATHINFO_EXTENSION);
            $profile_img=rand(0,99999)."_user.".$ext;
            $tpath1='images/'.$profile_img;
            
            if($ext!='png')  {
                $pic1=compress_image($_FILES["profile_img"]["tmp_name"], $tpath1, 80);
            } else {
                $tmp = $_FILES['profile_img']['tmp_name'];
                move_uploaded_file($tmp, $tpath1);
            }
            
            $data = array(
                  'user_name'  => addslashes(trim($_POST['user_name'])),				    
                  'user_email'  =>  addslashes(trim($_POST['user_email'])),
                  'user_phone'  =>  addslashes(trim($_POST['user_phone'])),
                  'user_gender'  =>  $_POST['user_gender'],
                  'profile_img'  => $profile_img,
            );
        } else if($_POST['user_password']!=""){
             $data = array(
                  'user_name'  => addslashes(trim($_POST['user_name'])),				    
                  'user_email'  =>  addslashes(trim($_POST['user_email'])),
                  'user_password'  =>  md5(trim($_POST['user_password'])),
                  'user_phone'  =>  addslashes(trim($_POST['user_phone'])),
                  'user_gender'  =>  $_POST['user_gender']
            );
        } else {
            $data = array(
                'user_name'  => addslashes(trim($_POST['user_name'])),				    
                'user_email'  =>  addslashes(trim($_POST['user_email'])),
                'user_phone'  =>  addslashes(trim($_POST['user_phone'])),
                'user_gender'  =>  $_POST['user_gender'],
            );
        }
        
        $user_edit=Update('tbl_users', $data, "WHERE id = '".$_POST['user_id']."'");
        
        $_SESSION['msg']="11";
        $_SESSION['class']='success'; 
        header("Location:create_user.php?user_id=".$_POST['user_id']);
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
            
        <div class="row g-4">
            <div class="col-12">
                <div class="card h-100">
                    <div class="card-body p-4">
                        <h5 class="mb-3"><?=$page_title ?></h5>
                        <form action="" name="addedituser" method="POST" enctype="multipart/form-data">
                            <input type="hidden" name="user_id" value="<?=(isset($_GET['user_id'])) ? $_GET['user_id'] : ''?>" />
                            
                            <div class="mb-3 row">
                                <label class="col-sm-2 col-form-label">Full name</label>
                                <div class="col-sm-10">
                                    <input type="text" name="user_name" value="<?php if(isset($_GET['user_id'])){echo $user_row['user_name'];}?>"  class="form-control" required>
                                </div>
                            </div>
                            
                            <div class="mb-3 row">
                                <label class="col-sm-2 col-form-label">Email ID</label>
                                <div class="col-sm-10">
                                    <input type="text" name="user_email" value="<?php if(isset($_GET['user_id'])){echo $user_row['user_email'];}?>" class="form-control" required>
                                </div>
                            </div>
                            
                            <div class="mb-3 row">
                                <label class="col-sm-2 col-form-label">Mobile</label>
                                <div class="col-sm-10">
                                    <input type="text" name="user_phone" value="<?php if(isset($_GET['user_id'])){echo $user_row['user_phone'];}?>" class="form-control" required>
                                </div>
                            </div>
                            
                            <div class="mb-3 row">
                                <label class="col-sm-2 col-form-label">Password</label>
                                <div class="col-sm-10">
                                    <input type="text" name="user_password" class="form-control" >
                                </div>
                            </div>
                            
                            <div class="mb-3 row">
                                <label class="col-sm-2 col-form-label">Gender</label>
                                <div class="col-sm-10">
                                    <select name="user_gender" id="user_gender" class="form-control" required>
                                        <?php if(isset($_GET['user_id'])){ ?>
                                            <option value="Male" <?php if($user_row['user_gender']=="Male"){?>selected<?php }?>>Male</option>
                                            <option value="Female" <?php if ($user_row['user_gender'] == 'Female') { ?>selected<?php } ?>>Female</option>         							 
                                        <?php }else{ ?>
                                            <option value="Male">Male</option>
                                            <option value="Female">Female</option>  						 
                                        <?php } ?>
                                    </select>
                                </div>
                            </div>
                            
                            <div class="mb-3 row">
                                <label class="col-sm-2 col-form-label">Select Profile Image</label>
                                <div class="col-sm-10">
                                    <input type="file" class="form-control-file" name="profile_img"  accept=".png, .jpg, .JPG .PNG" onchange="fileValidation()" id="fileupload">
                                </div>
                            </div>
                            
                            <div class="mb-3 row">
                                <label class="col-sm-2 col-form-label">Select Profile Image</label>
                                <div class="col-sm-10">
                                    <div class="fileupload_img" id="imagePreview">
                                        <?php if($row['profile_img']!="" AND file_exists("images/".$row['profile_img'])){ ?>
                                            <img class="col-sm-2 img-thumbnail" type="image" src="images/<?php echo $row['profile_img'];?>" alt="image" />
                                        <?php }else{ ?>
                                            <img class="col-sm-2 img-thumbnail" type="image" src="assets/images/300x300.jpg" alt="image" />
                                        <?php } ?>
                                    </div>
                                </div>
                            </div>
                            
                            <div class="mb-3 row">
                                <label class="col-sm-2 col-form-label">&nbsp;</label>
                                <div class="col-sm-10">
                                    <button type="submit" name="submit" class="btn btn-primary" style="min-width: 120px;"><?=$page_save?></button>
                                </div>
                            </div>
                            
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</main>
<!-- End: main -->
    
<?php include("includes/footer.php");?> 