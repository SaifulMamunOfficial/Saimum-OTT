<?php 
    $page_title=(isset($_GET['album_id'])) ? 'Edit Album' : 'Create Album';
    include("includes/header.php");
    require("includes/lb_helper.php");
    require("language/language.php");
    require_once("thumbnail_images.class.php");
    
    $page_save=(isset($_GET['album_id'])) ? 'Save' : 'Create';
    
    $cat_qry="SELECT * FROM tbl_category ORDER BY category_name";
    $cat_result=mysqli_query($mysqli,$cat_qry);
    
    $art_qry="SELECT * FROM tbl_artist ORDER BY artist_name";
    $art_result=mysqli_query($mysqli,$art_qry);
    
    if(isset($_POST['submit']) and isset($_GET['add'])){
        
        try {
            if($_FILES['album_image']['name']!=""){
                
                $ext = pathinfo($_FILES['album_image']['name'], PATHINFO_EXTENSION);
                $album_image=rand(0,99999)."_album.".$ext;
                $tpath1='images/'.$album_image;
                
                if($ext!='png')  {
                    $pic1=compress_image($_FILES["album_image"]["tmp_name"], $tpath1, 80);
                } else {
                    $tmp = $_FILES['album_image']['tmp_name'];
                    move_uploaded_file($tmp, $tpath1);
                }
                
            } else {
                $album_image='';
            }
            
            $data = array( 
                'album_name'  =>  cleanInput($_POST['album_name']),
                'catid'  =>  cleanInput($_POST['catid']),
                'artist_ids'  => implode(',',$_POST['art_ids']),
                'album_image'  =>  $album_image
            );  
            
            $qry = Insert('tbl_album',$data);
        } catch (Exception $e) {
		    print_r($e);
        }
        
        $_SESSION['msg']="10";
        $_SESSION['class']='success';
        header( "Location:create_album.php?add=yes");
        exit;
    }
    
    if(isset($_GET['album_id'])){
        $qry="SELECT * FROM tbl_album where aid='".$_GET['album_id']."'";
        $result=mysqli_query($mysqli,$qry);
        $row=mysqli_fetch_assoc($result);
    }
    
    if(isset($_POST['submit']) and isset($_POST['album_id'])){
        
        if($_FILES['album_image']['name']!=""){
            
            if($row['album_image']!=""){
                unlink('images/'.$row['album_image']);
            }
            
            $ext = pathinfo($_FILES['album_image']['name'], PATHINFO_EXTENSION);
            $album_image=rand(0,99999)."_album.".$ext;
            $tpath1='images/'.$album_image;
            
            if($ext!='png')  {
                $pic1=compress_image($_FILES["album_image"]["tmp_name"], $tpath1, 80);
            } else {
                $tmp = $_FILES['album_image']['tmp_name'];
                move_uploaded_file($tmp, $tpath1);
            }
            
        } else {
            $album_image=$row['album_image'];
        }

        $data = array( 
            'album_name'  =>  cleanInput($_POST['album_name']),
            'catid'  =>  cleanInput($_POST['catid']),
            'artist_ids'  => implode(',',$_POST['art_ids']),
            'album_image'  =>  $album_image
        );
        
        $edit=Update('tbl_album', $data, "WHERE aid = '".$_POST['album_id']."'");
        echo mysqli_error($mysqli);
        
        $_SESSION['msg']="11";
        $_SESSION['class']='success';
        header( "Location:create_album.php?album_id=".$_POST['album_id']);
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
                        <form action="" name="addeditalbum" method="POST" enctype="multipart/form-data">
                            <input  type="hidden" name="album_id" value="<?=(isset($_GET['album_id'])) ? $_GET['album_id'] : ''?>" />

                            <div class="mb-3 row">
                                <label class="col-sm-2 col-form-label">Album name</label>
                                <div class="col-sm-10">
                                    <input type="text" name="album_name" id="album_name" class="form-control" value="<?php if(isset($_GET['album_id'])){echo $row['album_name'];}?>" required>
                                </div>
                            </div>
                            
                            <div class="mb-3 row">
                                <label class="col-sm-2 col-form-label">Category</label>
                                <div class="col-sm-10">
                                    <select name="catid" id="catid" class="nsofts-select" required>
                                        <option value="">--Select Category--</option>
                                        <?php while($cat_row=mysqli_fetch_array($cat_result)){ ?>      
                                            <?php if(isset($_GET['album_id'])){ ?>
                      							<option value="<?php echo $cat_row['cid'];?>" <?php if($cat_row['cid']==$row['catid']){?>selected<?php }?>><?php echo $cat_row['category_name'];?></option>	          							 
                                            <?php }else{ ?>
                      						        <option value="<?php echo $cat_row['cid'];?>"><?php echo $cat_row['category_name'];?></option>   							 
                                            <?php } ?>
                                        <?php } ?> 
                                    </select>
                                </div>
                            </div>
                            
                            <div class="mb-3 row">
                                <label class="col-sm-2 col-form-label">Artist</label>
                                <div class="col-sm-10">
                                    <select name="art_ids[]" id="art_ids" class="nsofts-select" required multiple>
                                        <?php while($art_row=mysqli_fetch_array($art_result)){ ?> 
										    <option value="<?php echo $art_row['id'];?>" <?php if(in_array($art_row['id'], explode(",",$row['artist_ids']))){?>selected<?php }?>><?php echo $art_row['artist_name'];?></option>                           
    									<?php } ?>
                                    </select>
                                </div>
                            </div>
                            
                            <div class="mb-3 row">
                                <div class="mb-3 row">
                                    <label class="col-sm-2 col-form-label">Select Image</label>
                                    <div class="col-sm-10">
                                        <input type="file" class="form-control-file" name="album_image"   accept=".png, .jpg, .JPG .PNG" onchange="fileValidation()" id="fileupload" <?php if(!isset($_GET['album_id'])){?>required<?php } ?>>
                                    </div>
                                </div>
                            </div>
                            
                            <div class="mb-3 row">
                                <div class="mb-3 row">
                                    <label class="col-sm-2 col-form-label">&nbsp;</label>
                                    <div class="col-sm-10">
                                        <div class="fileupload_img" id="imagePreview">
                                            <?php if(isset($_GET['album_id'])) {?>
                                              <img class="col-sm-2 img-thumbnail" type="image" src="images/<?php echo $row['album_image'];?>" alt="image" />
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