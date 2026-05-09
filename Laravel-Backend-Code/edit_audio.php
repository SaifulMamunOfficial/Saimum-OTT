<?php 
    $page_title='Edit Audio';
    include("includes/header.php");
    require("includes/lb_helper.php");
    require("language/language.php");
    require_once("thumbnail_images.class.php");
    
    $page_save = 'Save';
    
    $album_qry="SELECT * FROM tbl_album ORDER BY album_name";
    $album_result=mysqli_query($mysqli,$album_qry);

    $art_qry="SELECT * FROM tbl_artist ORDER BY artist_name";
    $art_result=mysqli_query($mysqli,$art_qry);
    
    $cat_qry="SELECT * FROM tbl_category ORDER BY category_name";
    $cat_result=mysqli_query($mysqli,$cat_qry);
    
    $qry="SELECT * FROM tbl_audio where id='".$_GET['audio_id']."'";
    $result=mysqli_query($mysqli,$qry);
    $row=mysqli_fetch_assoc($result);
    
    if(isset($_POST['submit'])){
        
        $thumbnail_type = trim($_POST['img_type']);
        if($thumbnail_type=='local_img'){
            
            if($_FILES['audio_thumbnail']['name']!=""){
                
                if($row['audio_thumbnail']!="" AND $row['thumbnail_type']=="local_img"){
                    unlink('images/'.$row['audio_thumbnail']);
                }
                
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
                $audio_thumbnail = $row['audio_thumbnail'];;
            }
            
        } else {
            
            $audio_thumbnail = '';
            if($row['audio_thumbnail']!="" AND $row['thumbnail_type']=="local_img"){
                unlink('images/'.$row['audio_thumbnail']);
            }
        }
        
        $audio_type = trim($_POST['audio_type']);
        if($audio_type=='server_url'){
            
            $audio_url = htmlentities(trim($_POST['audio_url']));
            $audio_url_high = htmlentities(trim($_POST['audio_url_high']));
            $audio_url_low = htmlentities(trim($_POST['audio_url_low']));
            
            if($row['audio_type']=='local'){
              unlink('uploads/'.basename($row['audio_url']));
            }
            
        } else {
            
            if($_FILES['audio_local']['name']!=""){
                
                unlink('uploads/'.basename($row['audio_url']));
                
                $path = "uploads/";
                $audio_local=rand(0,99999)."_".str_replace(" ", "-", $_FILES['audio_local']['name']);
                $tmp = $_FILES['audio_local']['tmp_name'];
                
                if (move_uploaded_file($tmp, $path.$audio_local)) {
                    $audio_url = $audio_local;
                    $audio_url_high = '';
                    $audio_url_low = '';
                } else {
                    echo "Error in uploading mp3 file !!";
                    exit;
                }
            } else {
                
                $audio_url = basename($row['audio_url']);
                $audio_url_high = '';
                $audio_url_low = '';
            }
        }
        
        $data = array( 
            'cat_id'  =>  trim($_POST['cat_id']),
            'album_id'  =>  trim($_POST['album_id']),
            'audio_title'  =>  cleanInput($_POST['audio_title']),
            'audio_type'  =>  $audio_type,
            'audio_url'  =>  $audio_url,
            'audio_url_high'  =>  $audio_url_high,
            'audio_url_low'  =>  $audio_url_low,
            'thumbnail_type'  =>  $thumbnail_type,
            'audio_thumbnail'  =>  $audio_thumbnail,
            'audio_artist'  =>  implode(',', $_POST['audio_artist']),
            'audio_description'  =>  addslashes(trim($_POST['audio_description']))
        );  
        
        $qry=Update('tbl_audio', $data, "WHERE id = '".$_POST['audio_id']."'");
        
        $_SESSION['msg']="10";
        $_SESSION['class']='success';
        header( "Location:edit_audio.php?audio_id=".$_POST['audio_id']);
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
            <input  type="hidden" name="audio_id" value="<?php echo $_GET['audio_id'];?>" />
            <div class="row g-4">
                <div class="col-lg-6">
                    <div class="card h-100">
                        <div class="card-body p-4">
                            <h5 class="mb-4"><?=$page_title ?></h5>
                            
                            <div class="mb-3">
                                <input type="text" name="audio_title" id="audio_title" class="form-control" placeholder="Enter Audio Title"  value="<?php echo $row['audio_title']?>" required>
                            </div>
                            
                            <div class="mb-3">
                                <select name="cat_id" id="cat_id" class="nsofts-select " required>
                                    <option value="">--Select Category --</option>
                                    <?php while($cat_row=mysqli_fetch_array($cat_result)){ ?>                       
                                         <option value="<?php echo $cat_row['cid'];?>" <?php if($cat_row['cid']==$row['cat_id']){?>selected<?php }?>><?php echo $cat_row['category_name'];?></option>
                                    <?php } ?>
                                </select>
                            </div>
                            
                            <div class="mb-3">
                                <select name="album_id" id="album_id" class="nsofts-select" required>
                                    <option value="">--Select Album --</option>
                                    <?php while($album_row=mysqli_fetch_array($album_result)){ ?>                       
                                         <option value="<?php echo $album_row['aid'];?>" <?php if($album_row['aid']==$row['album_id']){?>selected<?php }?>><?php echo $album_row['album_name'];?></option>
                                    <?php } ?>
                                </select>
                            </div>
                            
                            <div class="mb-3">
                                <label class="form-label">Artist</label>
                                <select name="audio_artist[]" id="audio_artist" class="nsofts-select" required multiple>
                                    <option value="">--Select Artist--</option>
                                    <?php while($art_row=mysqli_fetch_array($art_result)){ ?> 
                                        <option value="<?php echo $art_row['artist_name'];?>" <?php if(in_array($art_row['artist_name'], explode(",",$row['audio_artist']))){?>selected<?php }?>><?php echo $art_row['artist_name'];?></option>                           
                                    <?php } ?>
                                </select>
                            </div>
                            
                            <div class="mb-3">
                                <label class="form-label">Lyrics</label>
                                <textarea name="audio_description" id="audio_description" rows="5" class="nsofts-editor"><?php echo $row['audio_description']?></textarea>
                            </div>
                            
                            <button type="submit" name="submit" class="btn btn-primary" style="min-width: 120px;"><?=$page_save?></button>
                        </div>
                    </div>
                </div>
                
                <div class="col-lg-6">
                    <div class="card h-100">
                        <div class="card-body p-4">
                            <h5 class="mb-4">Audio & Image</h5>
                            
                            <div class="mb-3">
                                <select name="audio_type" id="audio_type" class="nsofts-select" required>
                                    <option value="server_url" <?php if($row['audio_type']=='server_url'){?>selected<?php }?>>From Server(URL)</option>
                                    <option value="local" <?php if($row['audio_type']=='local'){?>selected<?php }?>>Browse From Device</option>
                                </select>
                            </div>
                            
                            <div id="audio_url_display" class="mb-3" <?php if($row['audio_type']=='local'){?>style="display:none;"<?php }?>>
                                <input type="text" name="audio_url" id="audio_url" class="form-control" placeholder="Normal Quality Audio URL" value="<?php echo $row['audio_url']?>">
                            </div>
                            
                            <div id="audio_url_display_high" class="mb-3" <?php if($row['audio_type']=='local'){?>style="display:none;"<?php }?>>
                                <input type="text" name="audio_url_high" id="audio_url_high" class="form-control" placeholder="High Quality Audio URL" value="<?php echo $row['audio_url_high']?>">
                            </div>
                            
                            <div id="audio_url_display_low" class="mb-3" <?php if($row['audio_type']=='local'){?>style="display:none;"<?php }?>>
                                <input type="text" name="audio_url_low" id="audio_url_low" class="form-control" placeholder="Low Quality Audio URL" value="<?php echo $row['audio_url_low']?>">
                            </div>
                            
                            <div id="audio_local_display"  <?php if($row['audio_type']!='local'){?>style="display:none;"<?php }?> class="mb-3">
                                <?php
                                  $audio_file=$row['audio_url'];
                                  if($row['audio_type']=='local'){
                                    $audio_file=$file_path.'uploads/'.basename($row['audio_url']);
                                  } 
                                  ?>
                                <label class="form-label mb-2">Audio Upload</label>
                                <input type="file" class="form-control-file" name="audio_local" id="audio_local" value=""  accept=".mp3">
                                <p><strong>Current URL:</strong> <?=$audio_file?></p>
                                <div id="audio_player" class="nemosofts-player" <?php if($row['audio_type']!='local'){?>style="display:none;"<?php }?>>
                                    <audio id="audio" controls src="<?=$audio_file?>"></audio>  
                                </div>
                            </div>
                            
                            <div class="mb-3">
                                <select name="img_type" id="img_type" class="nsofts-select" required>
                                    <option value="album_img" <?php if($row['thumbnail_type']=='album_img'){?>selected<?php }?>>Get album Image</option>
                                    <option value="local_img" <?php if($row['thumbnail_type']=='local_img'){?>selected<?php }?>>Select Image</option>
                                </select>
                            </div>
                            
                            
                            <div class="mb-3" id="img_local_display"  <?php if($row['thumbnail_type']!='local_img'){?>style="display:none;"<?php }?>>
                                <label class="form-label mb-2">Select Image</label>
                                <input type="file" class="form-control-file" name="audio_thumbnail" accept=".png, .jpg, .JPG .PNG" onchange="fileValidation()" id="fileupload">
                            </div>
                            
                            <div class="mb-3" id="img_preview_display" <?php if($row['thumbnail_type']!='local_img'){?>style="display:none;"<?php }?>>
                               <div class="fileupload_img" id="imagePreview">
                                     <img class="col-sm-3 img-thumbnail" type="image" src="assets/images/300x300.jpg" alt="image" />
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
        $("#audio_type").change(function(){
          var type=$("#audio_type").val();
          if(type=="server_url"){
            $("#audio_url_display").show();
            $("#audio_url_display_high").show();
            $("#audio_url_display_low").show();
            $("#audio_local_display").hide();
            $("#audio_local").val('');
            $("#audio").attr('src','');
          }
          else {
            $("#audio_url_display").hide(); 
            $("#audio_url_display_high").hide();
            $("#audio_url_display_low").hide();
            $("#audio_local_display").show();
          }
        });
    });
    $(document).ready(function(e) {
        $("#img_type").change(function(){
          var type=$("#img_type").val();
          if(type=="album_img"){
            $("#img_local_display").hide();
            $("#img_preview_display").hide();
          }
          else {
            $("#img_local_display").show();
            $("#img_preview_display").show();
          }
        });
    });
    
    var objectUrl;
    $("#audio_local").change(function(e){
        var file = e.currentTarget.files[0];
        
        $("#filesize").text(file.size);
        
        objectUrl = URL.createObjectURL(file);
        $("#audio").prop("src", objectUrl);
        $("#audio_player").show();
    });
</script>