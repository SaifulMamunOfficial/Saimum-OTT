<?php 
    $page_title='Create Multiple Audio';
    include("includes/header.php");
    require("includes/lb_helper.php");
    require("language/language.php");
    require_once("thumbnail_images.class.php");
    
    $page_save = 'Create All';
    
    $album_qry="SELECT * FROM tbl_album ORDER BY album_name";
    $album_result=mysqli_query($mysqli,$album_qry);

    $art_qry="SELECT * FROM tbl_artist ORDER BY artist_name";
    $art_result=mysqli_query($mysqli,$art_qry);
    
    $cat_qry="SELECT * FROM tbl_category ORDER BY category_name";
    $cat_result=mysqli_query($mysqli,$cat_qry);
    
    if(isset($_POST['submit']) and isset($_GET['add'])){

        $thumbnail_type = trim($_POST['img_type']);
        if($thumbnail_type=='local_img'){
            
            if($_FILES['audio_thumbnail']['name']!=""){
            
                $ext = pathinfo($_FILES['audio_thumbnail']['name'], PATHINFO_EXTENSION);
                $audio_thumbnail=rand(0,99999)."_audio.".$ext;
                $tpath1='images/'.$audio_thumbnail;
                
                if($ext!='png')  {
                    $pic1=compress_image($_FILES["audio_thumbnail"]["tmp_name"], $tpath1, 80);
                } else {
                    $tmp = $_FILES['audio_thumbnail']['tmp_name'];
                    move_uploaded_file($tmp, $tpath1);
                }
            } else {
                $audio_thumbnail='';
            }
        } else {
            $audio_thumbnail='';
        }
        
        
        for ($i = 1; $i <= 8; $i++) {
            if (!empty($_POST["audio_title_$i"]) && !empty($_POST["audio_url_$i"])) {
                $data = array(
                    'cat_id' => trim($_POST['cat_id']),
                    'album_id' => trim($_POST['album_id']),
                    'audio_title' => cleanInput($_POST["audio_title_$i"]),
                    'audio_type' => 'server_url',
                    'audio_url' => htmlentities(trim($_POST["audio_url_$i"])),
                    'audio_url_high' => '',
                    'audio_url_low' => '',
                    'thumbnail_type' => $thumbnail_type,
                    'audio_thumbnail' => $audio_thumbnail,
                    'audio_artist' => implode(',', $_POST['audio_artist']),
                    'audio_description' => ''
                );
                $qry = Insert('tbl_audio', $data);
            }
        }
        
        $_SESSION['msg']="10";
        $_SESSION['class']='success';
        header( "Location:manage_audio.php");
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
        
        
        <form action="" name="addeditaudio" method="POST" enctype="multipart/form-data">
            <div class="row g-4">
                <div class="col-lg-6">
                    <div class="card h-100">
                        <div class="card-body p-4">
                            <h5 class="mb-4"><?=$page_title ?></h5>

                            <div class="mb-3">
                                <select name="cat_id" id="cat_id" class="nsofts-select " required>
                                    <option value="">--Select Category --</option>
                                    <?php while($cat_row=mysqli_fetch_array($cat_result)){ ?>                       
                                        <option value="<?php echo $cat_row['cid'];?>"><?php echo $cat_row['category_name'];?></option>
                                    <?php } ?>
                                </select>
                            </div>
                            
                            <div class="mb-3">
                                <select name="album_id" id="album_id" class="nsofts-select" required>
                                    <option value="">--Select Album --</option>
                                    <?php while($album_row=mysqli_fetch_array($album_result)){ ?>                       
                                         <option value="<?php echo $album_row['aid'];?>"><?php echo $album_row['album_name'];?></option>
                                    <?php } ?>
                                </select>
                            </div>
                            
                            <div class="mb-3">
                                <label class="form-label">Artist</label>
                                <select name="audio_artist[]" id="audio_artist" class="nsofts-select" required multiple>
                                    <option value="">--Select Artist--</option>
                                    <?php while($art_row=mysqli_fetch_array($art_result)){ ?> 
                                        <option value="<?php echo $art_row['artist_name'];?>"><?php echo $art_row['artist_name'];?></option>                           
                                    <?php } ?>
                                </select>
                            </div>
                            
                            <div class="mb-3">
                                <select name="img_type" id="img_type" class="nsofts-select" required>
                                    <option value="album_img">Get album Image</option>
                                    <option value="local_img">Select Image</option>
                                </select>
                            </div>
                            
                            <div class="mb-3" id="img_local_display"  style="display:none;">
                                <label class="form-label mb-2">Select Image</label>
                                <input type="file" class="form-control-file" name="audio_thumbnail" accept=".png, .jpg, .JPG .PNG" onchange="fileValidation()" id="fileupload">
                            </div>
                            
                            <div class="mb-3" id="img_preview_display" style="display:none;">
                               <div class="fileupload_img" id="imagePreview">
                                     <img class="col-sm-3 img-thumbnail" type="image" src="assets/images/300x300.jpg" alt="image" />
                                </div>
                            </div>
                            
                            <div class="mb-3 mt-5">
                                <input type="text" name="audio_title_1" id="audio_title_1" class="form-control" placeholder="Enter Audio Title 1" required>
                            </div>
                            <div class="mb-3">
                                <input type="text" name="audio_url_1" id="audio_url_1" class="form-control" placeholder="Normal Quality Audio URL" required>
                            </div>
                            
                            
                            <div class="mb-3 mt-5">
                                <input type="text" name="audio_title_2" id="audio_title_2" class="form-control" placeholder="Enter Audio Title 2">
                            </div>
                            <div class="mb-3">
                                <input type="text" name="audio_url_2" id="audio_url_2" class="form-control" placeholder="Normal Quality Audio URL">
                            </div>
                            
                            <div class="mb-3 mt-5">
                                <input type="text" name="audio_title_3" id="audio_title_3" class="form-control" placeholder="Enter Audio Title 3">
                            </div>
                            <div class="mb-3">
                                <input type="text" name="audio_url_3" id="audio_url_3" class="form-control" placeholder="Normal Quality Audio URL">
                            </div>

                            <button type="submit" name="submit" class="btn btn-primary" style="min-width: 120px;"><?=$page_save?></button>
                        </div>
                    </div>
                </div>
                
                <div class="col-lg-6">
                    <div class="card h-100">
                        <div class="card-body p-4">

                            <div class="mb-3 mt-5">
                                <input type="text" name="audio_title_4" id="audio_title_4" class="form-control" placeholder="Enter Audio Title 4">
                            </div>
                            <div class="mb-3">
                                <input type="text" name="audio_url_4" id="audio_url_4" class="form-control" placeholder="Normal Quality Audio URL">
                            </div>
                            
                            <div class="mb-3 mt-5">
                                <input type="text" name="audio_title_5" id="audio_title_5" class="form-control" placeholder="Enter Audio Title 5">
                            </div>
                            <div class="mb-3">
                                <input type="text" name="audio_url_5" id="audio_url_5" class="form-control" placeholder="Normal Quality Audio URL">
                            </div>
                            
                            <div class="mb-3 mt-5">
                                <input type="text" name="audio_title_6" id="audio_title_6" class="form-control" placeholder="Enter Audio Title 6">
                            </div>
                            <div class="mb-3">
                                <input type="text" name="audio_url_6" id="audio_url_6" class="form-control" placeholder="Normal Quality Audio URL">
                            </div>
                            
                            <div class="mb-3 mt-5">
                                <input type="text" name="audio_title_7" id="audio_title_7" class="form-control" placeholder="Enter Audio Title 7">
                            </div>
                            <div class="mb-3">
                                <input type="text" name="audio_url_7" id="audio_url_7" class="form-control" placeholder="Normal Quality Audio URL">
                            </div>
                            
                            <div class="mb-3 mt-5">
                                <input type="text" name="audio_title_8" id="audio_title_8" class="form-control" placeholder="Enter Audio Title 8">
                            </div>
                            <div class="mb-3">
                                <input type="text" name="audio_url_8" id="audio_url_8" class="form-control" placeholder="Normal Quality Audio URL">
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
        $("#img_type").change(function(){
          var type=$("#img_type").val();
          if(type=="album_img"){
              $("#img_local_display").hide();
              $("#img_preview_display").hide();
          }
          else{
              $("#img_local_display").show();
              $("#img_preview_display").show();
          }
        });
    });
    
</script>