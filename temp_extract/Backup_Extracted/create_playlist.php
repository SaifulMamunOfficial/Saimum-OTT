<?php 
    $page_title=(isset($_GET['playlist_id'])) ? 'Edit Playlist ' : 'Create Playlist';
    include("includes/header.php");
    require("includes/lb_helper.php");
    require("language/language.php");
    require_once("thumbnail_images.class.php");
    
    $page_save=(isset($_GET['playlist_id'])) ? 'Save' : 'Create';
    
    $audio_qry="SELECT * FROM tbl_audio ORDER BY tbl_audio.id DESC"; 
    $audio_result=mysqli_query($mysqli,$audio_qry); 
    
    if(isset($_POST['submit']) and isset($_GET['add'])){
        
        if($_FILES['playlist_image']['name']!=""){
            
            $ext = pathinfo($_FILES['playlist_image']['name'], PATHINFO_EXTENSION);
            $playlist_image=rand(0,99999)."_playlist.".$ext;
            $tpath1='images/'.$playlist_image;
            
            if($ext!='png')  {
                $pic1=compress_image($_FILES["playlist_image"]["tmp_name"], $tpath1, 80);
            } else {
                $tmp = $_FILES['playlist_image']['tmp_name'];
                move_uploaded_file($tmp, $tpath1);
            }
            
        } else {
            $playlist_image='';
        }
        
        $data = array( 
            'playlist_name'  =>  cleanInput($_POST['playlist_name']),
            'playlist_audio'  =>  implode(',',$_POST['playlist_audio']),
            'playlist_image'  =>  $playlist_image
        );  
        
        $qry = Insert('tbl_playlist',$data);
        
        $_SESSION['msg']="10";
        $_SESSION['class']='success';
        header( "Location:create_playlist.php?add=yes");
        exit;
    }
    
    if(isset($_GET['playlist_id'])){
        $qry="SELECT * FROM tbl_playlist where pid='".$_GET['playlist_id']."'";
        $result=mysqli_query($mysqli,$qry);
        $row=mysqli_fetch_assoc($result);
    }

    if(isset($_POST['submit']) and isset($_POST['playlist_id'])){
        
        if($_FILES['playlist_image']['name']!=""){
            
            if($row['playlist_image']!=""){
                unlink('images/'.$row['playlist_image']);
            }
            
            $ext = pathinfo($_FILES['playlist_image']['name'], PATHINFO_EXTENSION);
            $playlist_image=rand(0,99999)."_playlist.".$ext;
            $tpath1='images/'.$playlist_image;
            
            if($ext!='png')  {
                $pic1=compress_image($_FILES["playlist_image"]["tmp_name"], $tpath1, 80);
            } else {
                $tmp = $_FILES['playlist_image']['tmp_name'];
                move_uploaded_file($tmp, $tpath1);
            }
            
        } else {
            $playlist_image = $row['playlist_image'];
        }
        
        $data = array( 
            'playlist_name'  =>  cleanInput($_POST['playlist_name']),
            'playlist_audio'  =>  implode(',',$_POST['playlist_audio']),
            'playlist_image'  =>  $playlist_image
        );
        
        
        $playlist_edit=Update('tbl_playlist', $data, "WHERE pid = '".$_POST['playlist_id']."'");
        
        $_SESSION['msg']="11";
        $_SESSION['class']='success';
        header( "Location:create_playlist.php?playlist_id=".$_POST['playlist_id']);
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
                        <form action="" name="addeditplaylist" method="POST" enctype="multipart/form-data">
                             <input  type="hidden" name="playlist_id" value="<?=(isset($_GET['playlist_id'])) ? $_GET['playlist_id'] : ''?>" />
                            
                            <div class="mb-3 row">
                                <label class="col-sm-2 col-form-label">Playlist name</label>
                                <div class="col-sm-10">
                                     <input type="text" name="playlist_name" class="form-control" value="<?php if(isset($_GET['playlist_id'])){echo $row['playlist_name'];}?>" required>
                                </div>
                            </div>

                            <div class="mb-3 row">
                                <label class="col-sm-2 col-form-label">Add Audio</label>
                                <div class="col-sm-10">
                                    <select name="playlist_audio[]" id="playlist_audio" class="nsofts-select" required multiple>
                                        <option value="">--Select Songs--</option>
                                        <?php while($audio_row=mysqli_fetch_array($audio_result)){ ?> 
    										<option value="<?php echo $audio_row['id'];?>" <?php if(in_array($audio_row['id'], explode(",",$row['playlist_audio']))){?>selected<?php }?>><?php echo $audio_row['audio_title'];?></option>                           
    									<?php } ?>
                                    </select>
                                </div>
                            </div>
                            
                            <div class="mb-3 row">
                                <div class="mb-3 row">
                                    <label class="col-sm-2 col-form-label">Select Image</label>
                                    <div class="col-sm-10">
                                        <input type="file" class="form-control-file" name="playlist_image"   accept=".png, .jpg, .JPG .PNG" onchange="fileValidation()" id="fileupload" <?php if(!isset($_GET['cat_id'])){?>required<?php } ?>>
                                    </div>
                                </div>
                            </div>
                            
                            <div class="mb-3 row">
                                <div class="mb-3 row">
                                    <label class="col-sm-2 col-form-label">&nbsp;</label>
                                    <div class="col-sm-10">
                                        <div class="fileupload_img" id="imagePreview">
                                            <?php if(isset($_GET['playlist_id'])) {?>
                                              <img class="col-sm-2 img-thumbnail" type="image" src="images/<?php echo $row['playlist_image'];?>" alt="image" />
                                            <?php }else{?>
                                              <img class="col-sm-2 img-thumbnail" type="image" src="assets/images/300x300.jpg" alt="image" />
                                            <?php } ?>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            
                            <div class="mb-3 row">
                                <div class="mb-3 row">
                                    <label class="col-sm-2 col-form-label">&nbsp;</label>
                                    <div class="col-sm-10">
                                        <button type="submit" name="submit" class="btn btn-primary" style="min-width: 120px;"><?=$page_save?></button>
                                    </div>
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