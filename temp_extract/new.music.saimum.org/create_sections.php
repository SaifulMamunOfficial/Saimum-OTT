<?php 
    $page_title=(isset($_GET['section_id'])) ? 'Edit Section' : 'Create Section';
    include("includes/header.php");
    require("includes/lb_helper.php");
    require("language/language.php");
    require_once("thumbnail_images.class.php");
    
    $page_save=(isset($_GET['section_id'])) ? 'Save' : 'Create';
    
    $audio_qry="SELECT * FROM tbl_audio ORDER BY tbl_audio.id DESC"; 
    $audio_result=mysqli_query($mysqli,$audio_qry); 
    
    $category_qry="SELECT * FROM tbl_category ORDER BY tbl_category.cid DESC"; 
    $category_result=mysqli_query($mysqli,$category_qry); 
    
    $artist_qry="SELECT * FROM tbl_artist ORDER BY tbl_artist.id DESC"; 
    $artist_result=mysqli_query($mysqli,$artist_qry); 
    
    $album_qry="SELECT * FROM tbl_album ORDER BY tbl_album.aid DESC"; 
    $album_result=mysqli_query($mysqli,$album_qry); 
    
    $playlist_qry="SELECT * FROM tbl_playlist ORDER BY tbl_playlist.pid DESC"; 
    $playlist_result=mysqli_query($mysqli,$playlist_qry); 

    if(isset($_POST['submit']) and isset($_GET['add'])){
        
        $section_type = trim($_POST['section_type']);
        
        if($section_type=='category'){
            $post_ids = implode(',',$_POST['cat_post_id']);
        } else if($section_type=='artist'){
            $post_ids = implode(',',$_POST['artists_post_id']);
        } else if($section_type=='album'){
            $post_ids = implode(',',$_POST['album_post_id']);
        } else if($section_type=='song'){
            $post_ids = implode(',',$_POST['audio_post_id']);
        } else if($section_type=='playlist'){
            $post_ids = implode(',',$_POST['playlist_post_id']);
        } else{
            $post_ids='';
        }
        
        $data = array( 
            'section_name'  =>  $_POST['section_name'],
            'section_type'  =>  $section_type,
            'post_ids'  =>  $post_ids
        );
        
        $qry = Insert('tbl_home_sections',$data);
        
        $_SESSION['msg']="10";
        $_SESSION['class']='success'; 
        header( "Location:manage_sections.php");
        exit;			 
    }
    
    if(isset($_GET['section_id'])){
        $qry="SELECT * FROM tbl_home_sections where id='".$_GET['section_id']."'";
        $result=mysqli_query($mysqli,$qry);
        $row=mysqli_fetch_assoc($result);
    }
    
    if(isset($_POST['submit']) and isset($_POST['section_id'])){
        
        $section_type = trim($_POST['section_type']);
        
        if($section_type=='category'){
            $post_ids = implode(',',$_POST['cat_post_id']);
        } else if($section_type=='artist'){
            $post_ids = implode(',',$_POST['artists_post_id']);
        } else if($section_type=='album'){
            $post_ids = implode(',',$_POST['album_post_id']);
        } else if($section_type=='song'){
            $post_ids = implode(',',$_POST['audio_post_id']);
        } else if($section_type=='playlist'){
            $post_ids = implode(',',$_POST['playlist_post_id']);
        } else{
            $post_ids='';
        }
        
        $data = array( 
            'section_name'  =>  $_POST['section_name'],
            'section_type'  =>  $section_type,
            'post_ids'  =>  $post_ids
        );
        
        $banner_edit=Update('tbl_home_sections', $data, "WHERE id = '".$_POST['section_id']."'");
        
        $_SESSION['msg']="11";
        $_SESSION['class']='success'; 
        header( "Location:create_sections.php?section_id=".$_POST['section_id']);
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
                            <input  type="hidden" name="section_id" value="<?=(isset($_GET['section_id'])) ? $_GET['section_id'] : ''?>" />
                            
                            <div class="mb-3 row">
                                <label class="col-sm-2 col-form-label">Section Title</label>
                                <div class="col-sm-10">
                                    <input type="text" name="section_name" class="form-control" value="<?php if(isset($_GET['section_id'])){echo $row['section_name'];}?>" required>
                                </div>
                            </div>
                            
                            <div class="mb-3 row">
                                <label class="col-sm-2 col-form-label">Section Type</label>
                                <div class="col-sm-10">
                                    <select name="section_type" id="section_type" class="nsofts-select" required>
                                        <option value="">--Select Type --</option>
                                         <?php if(isset($_GET['section_id'])){ ?>
                                            <option value="category" <?php if($row['section_type']=='category'){?>selected<?php }?>>Categories</option>
                                            <option value="artist" <?php if($row['section_type']=='artist'){?>selected<?php }?>>Artists</option>
                                            <option value="album" <?php if($row['section_type']=='album'){?>selected<?php }?>>Album</option>
                                            <option value="song" <?php if($row['section_type']=='song'){?>selected<?php }?>>Audio</option>
                                            <option value="playlist" <?php if($row['section_type']=='playlist'){?>selected<?php }?>>Playlist</option>
                                         <?php } else { ?>
                                            <option value="category">Categories</option>
                                            <option value="artist">Artists</option>
                                            <option value="album">Album</option>
                                            <option value="song">Audio</option>
                                            <option value="playlist">Playlist</option>
                                         <?php } ?>
                                    </select>
                                </div>
                            </div>
                            
                            <div id="section_cat" class="mb-3 row" <?php if(isset($_GET['section_id'])){?> <?php if($row['section_type']!='category'){?>style="display:none;"<?php }?> <?php } else { ?>style="display:none;"<?php }?>>
                                <label class="col-sm-2 col-form-label">Add Categories</label>
                                <div class="col-sm-10">
                                    <select name="cat_post_id[]" id="cat_post_id" class="nsofts-select" multiple="multiple">
                                        <option value="">--Select Categories--</option>
                                        <?php while($category_row=mysqli_fetch_array($category_result)){ ?> 
                                            <option value="<?php echo $category_row['cid'];?>" <?php if(in_array($category_row['cid'], explode(",",$row['post_ids']))){?>selected<?php }?>><?php echo $category_row['category_name'];?></option>                           
                                        <?php } ?>
                                    </select>
                                </div>
                            </div>
                            
                            <div id="section_artists" class="mb-3 row" <?php if(isset($_GET['section_id'])){?> <?php if($row['section_type']!='artist'){?>style="display:none;"<?php }?> <?php } else { ?>style="display:none;"<?php }?>>
                                <label class="col-sm-2 col-form-label">Add Artists</label>
                                <div class="col-sm-10">
                                    <select name="artists_post_id[]" id="artists_post_id" class="nsofts-select" multiple="multiple">
                                        <option value="">--Select Artists--</option>
                                        <?php while($artist_row=mysqli_fetch_array($artist_result)){ ?> 
                                            <option value="<?php echo $artist_row['id'];?>" <?php if(in_array($artist_row['id'], explode(",",$row['post_ids']))){?>selected<?php }?>><?php echo $artist_row['artist_name'];?></option>                           
                                        <?php } ?>
                                    </select>
                                </div>
                            </div>
                            
                            <div id="section_album"  class="mb-3 row" <?php if(isset($_GET['section_id'])){?> <?php if($row['section_type']!='album'){?>style="display:none;"<?php }?> <?php } else { ?>style="display:none;"<?php }?>>
                                <label class="col-sm-2 col-form-label">Add Album</label>
                                <div class="col-sm-10">
                                    <select name="album_post_id[]" id="album_post_id" class="nsofts-select" multiple="multiple">
                                        <option value="">--Select Album--</option>
                                        <?php while($album_row=mysqli_fetch_array($album_result)){ ?> 
                                            <option value="<?php echo $album_row['aid'];?>" <?php if(in_array($album_row['aid'], explode(",",$row['post_ids']))){?>selected<?php }?>><?php echo $album_row['album_name'];?></option>                           
                                        <?php } ?>
                                    </select>
                                </div>
                            </div>
                            
                            <div id="section_audio" class="mb-3 row" <?php if(isset($_GET['section_id'])){?> <?php if($row['section_type']!='song'){?>style="display:none;"<?php }?> <?php } else { ?>style="display:none;"<?php }?>>
                                <label class="col-sm-2 col-form-label">Add Audio</label>
                                <div class="col-sm-10">
                                    <select name="audio_post_id[]" id="audio_post_id" class="nsofts-select" multiple="multiple">
                                        <option value="">--Select Audio--</option>
                                        <?php while($audio_row=mysqli_fetch_array($audio_result)){ ?> 
                                            <option value="<?php echo $audio_row['id'];?>" <?php if(in_array($audio_row['id'], explode(",",$row['post_ids']))){?>selected<?php }?>><?php echo $audio_row['audio_title'];?></option>                           
                                        <?php } ?>
                                    </select>
                                </div>
                            </div>
                            
                            <div id="section_playlist" class="mb-3 row" <?php if(isset($_GET['section_id'])){?> <?php if($row['section_type']!='playlist'){?>style="display:none;"<?php }?> <?php } else { ?>style="display:none;"<?php }?>>
                                <label class="col-sm-2 col-form-label">Add Playlist</label>
                                <div class="col-sm-10">
                                    <select name="playlist_post_id[]" id="playlist_post_id" class="nsofts-select" multiple="multiple">
                                        <option value="">--Select Playlist--</option>
                                        <?php while($playlist_row=mysqli_fetch_array($playlist_result)){ ?> 
                                            <option value="<?php echo $playlist_row['pid'];?>" <?php if(in_array($playlist_row['pid'], explode(",",$row['post_ids']))){?>selected<?php }?>><?php echo $playlist_row['playlist_name'];?></option>                           
                                        <?php } ?>
                                    </select>
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

<script type="text/javascript">
    $(document).ready(function(e) {
        $("#section_type").change(function(){
          var type=$("#section_type").val();
          if(type=="category"){
            $("#section_cat").show();
            $("#section_artists").hide();
            $("#section_album").hide();
            $("#section_audio").hide();
            $("#section_playlist").hide();
            
          } else if(type=="artist"){
            $("#section_cat").hide();
            $("#section_artists").show();
            $("#section_album").hide();
            $("#section_audio").hide();
            $("#section_playlist").hide();
            
          } else if(type=="album"){
            $("#section_cat").hide();
            $("#section_artists").hide();
            $("#section_album").show();
            $("#section_audio").hide();
            $("#section_playlist").hide();
            
          } else if(type=="song"){
            $("#section_cat").hide();
            $("#section_artists").hide();
            $("#section_album").hide();
            $("#section_audio").show();
            $("#section_playlist").hide();
            
          } else if(type=="playlist"){
            $("#section_cat").hide();
            $("#section_artists").hide();
            $("#section_album").hide();
            $("#section_audio").hide();
            $("#section_playlist").show();
            
          } else{
            $("#section_cat").hide();
            $("#section_artists").hide();
            $("#section_album").hide();
            $("#section_audio").hide();
            $("#section_playlist").hide();
          }
        });
    });
</script>