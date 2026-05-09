<?php $page_title="Manage Audio";
    include("includes/header.php");
    require("includes/lb_helper.php");
    require("language/language.php");
    
    $tableName="tbl_audio";  
    $targetpage = "manage_audio.php"; 
    $keyword='';
    $album='';
    $limit = 12; 
    
    if(isset($_GET['album'])){
        
        $album=trim($_GET['album']);
        $query="SELECT COUNT(*) as num  FROM tbl_audio
        LEFT JOIN tbl_album ON tbl_audio.`album_id`= tbl_album.`aid` 
        WHERE tbl_audio.`album_id`='$album' ORDER BY tbl_audio.`id` DESC"; 
        
        $targetpage = "manage_audio.php?album=".$_GET['album'];
        
    } else if(isset($_GET['keyword'])){
        $keyword=addslashes(trim($_GET['keyword']));
        $query="SELECT COUNT(*) as num FROM tbl_audio
        LEFT JOIN tbl_album ON tbl_audio.`album_id`= tbl_album.`aid` 
        WHERE `audio_title` LIKE '%$keyword%' ORDER BY tbl_audio.`id` DESC"; 
        
        $targetpage = "manage_audio.php?keyword=".$_GET['keyword'];
    } else {
        
        $query = "SELECT COUNT(*) as num FROM $tableName";
    }
    
    
    $total_pages = mysqli_fetch_array(mysqli_query($mysqli,$query));
    $total_pages = $total_pages['num'];
    
    $stages = 3;
    $page=0;
    if(isset($_GET['page'])){
        $page = mysqli_real_escape_string($mysqli,$_GET['page']);
    }
    if($page){
        $start = ($page - 1) * $limit; 
    } else {
        $start = 0; 
    } 
    

    if(isset($_GET['album'])){
        
        $album=trim($_GET['album']);
        $sql_query="SELECT * FROM tbl_audio
        LEFT JOIN tbl_album ON tbl_audio.`album_id`= tbl_album.`aid` 
        WHERE tbl_audio.`album_id`='$album' ORDER BY tbl_audio.`id` DESC "; 
        
    } else if(isset($_GET['keyword'])){
        $keyword=addslashes(trim($_GET['keyword']));
        $sql_query="SELECT * FROM tbl_audio
        LEFT JOIN tbl_album ON tbl_audio.`album_id`= tbl_album.`aid` 
        WHERE `audio_title` LIKE '%$keyword%' ORDER BY tbl_audio.`id` DESC LIMIT $start, $limit"; 
        
    } else {
        
        $sql_query="SELECT * FROM tbl_audio
        LEFT JOIN tbl_album ON tbl_audio.`album_id`= tbl_album.`aid` 
        ORDER BY tbl_audio.`id` DESC LIMIT $start, $limit";  
    }
    $result=mysqli_query($mysqli,$sql_query) or die(mysqli_error($mysqli));
?>

<!-- Start: main -->
<main id="nsofts_main">
    <div class="nsofts-container">
        <div class="card h-100">
            <div class="card-top d-md-inline-flex align-items-center justify-content-between py-3 px-4">
                <div class="d-inline-flex align-items-center text-decoration-none fw-semibold">
                    <span class="ps-2 lh-1"><?=$page_title ?></span>
                </div>
                <div class="d-flex mt-2 mt-md-0">
                    <form id="filterForm" accept="" method="GET" class="me-2">
                         <select name="album" class="nsofts-select filter" data-type="album">
                            <option value=""> Select All Albums </option>
                            <?php
                            $sql_album = "SELECT * FROM tbl_album ORDER BY album_name";
                            
                            $res_album = mysqli_query($mysqli, $sql_album);
                            while ($row_album = mysqli_fetch_array($res_album)) {
                              ?>
                              <option value="<?php echo $row_album['aid']; ?>" <?php if (isset($_GET['album']) && $_GET['album'] == $row_album['aid']) { echo 'selected'; } ?>><?php echo $row_album['album_name']; ?></option>
                              <?php
                            }
                            ?>
                          </select>
                    </form>
                    <form method="get" id="searchForm" action="" class="me-2">
                        <div class="input-group">
                            <input type="text" id="search_input" class="form-control" placeholder="Search here..." name="keyword" value="<?php if(isset($_GET['keyword'])){ echo $_GET['keyword'];} ?>" required="required">
                            <button class="btn btn-outline-default d-inline-flex align-items-center" type="search">
                                <i class="ri-search-2-line"></i>
                            </button>
                        </div>
                    </form>
                    <a href="create_multiple_audio.php?add=yes" class="btn btn-primary d-inline-flex align-items-center justify-content-center me-2">
                        <i class="ri-add-line"></i>
                        <span class="ps-1 text-nowrap d-none d-sm-block">create multiple audio</span>
                    </a>
                    <a href="create_audio.php?add=yes" class="btn btn-primary d-inline-flex align-items-center justify-content-center">
                        <i class="ri-add-line"></i>
                        <span class="ps-1 text-nowrap d-none d-sm-block">Create Audio</span>
                    </a>
                </div>
            </div>
            
            <div class="card-body p-4">
                <?php if(mysqli_num_rows($result) > 0){ ?>
                    <div class="row g-4">
                        <?php $i=0; while($row=mysqli_fetch_array($result)) { ?>
                            <div class="col-lg-3 col-sm-6 card-item">
                                <div class="nsofts-image-card">
                                    <div class="nsofts-image-card__cover mb-0">
                                        <div class="nsofts-switch d-flex align-items-center enable_disable" data-bs-toggle="tooltip" data-bs-placement="top" title="Enable / Disable">
                                            <input type="checkbox" id="enable_disable_check_<?= $i ?>" data-id="<?= $row['id'] ?>" data-table="<?=$tableName ?>" data-column="audio_status" class="cbx hidden btn_enable_disable" <?php if ($row['audio_status'] == 1) { echo 'checked'; } ?>>
                                            <label for="enable_disable_check_<?= $i ?>" class="nsofts-switch__label"></label>
                                        </div>
                                        <?php
                                          $audio_thumbnail=$row['audio_thumbnail'];
                                          if($row['thumbnail_type']=='album_img'){
                                            $audio_thumbnail=$row['album_image'];
                                          } 
                                        ?>
                                        <img src="images/<?=$audio_thumbnail?>" onerror="this.src='assets/images/300x300.jpg';"  loading="lazy"  alt="">
                                    </div>
                                    <div class="nsofts-image-card__content nsofts-bottom">
                                        <div class="position-relative">
                                            <span class="d-block"><?php echo $row['audio_artist'];?></span>
                                            <span class="d-block fs-6 fw-semibold"><?php echo $row['album_name'];?></span>
                                            <div class="d-flex align-items-center justify-content-between nsofts-image-card__content__text">
                                                <span class="d-block text-truncate fs-6 fw-semibold pe-2"><?php echo $row['audio_title'];?></span>
                                                <div class="nsofts-image-card__option d-flex">
                                                    <a href="edit_audio.php?audio_id=<?php echo $row['id'];?>" class="btn border-0" data-bs-toggle="tooltip" data-bs-placement="top" title="Edit">
                                                        <i class="ri-pencil-fill"></i>
                                                    </a>
                                                    <a href="javascript:void(0)" class="btn border-0 text-danger btn_delete" data-id="<?php echo $row['id'];?>" data-table="<?=$tableName ?>" data-bs-toggle="tooltip" data-bs-placement="top" title="Delete"><i class="ri-delete-bin-fill"></i></a>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        <?php $i++; } ?>
                    </div>
                    <div class="mt-3">
                        <?php include("pagination.php"); ?>
                    </div>
                <?php } else { ?>
                    <ul class="p-5">
                        <h1 class="text-center">No data found</h1>
                    </ul>
                <?php } ?>
            </div>
        </div>
    </div>
</main>
<!-- End: main -->
    
<?php include("includes/footer.php");?> 
<script type="text/javascript">
    $(".filter").on("change", function(e) {
        $("#filterForm *").filter(":input").each(function() {
          if ($(this).val() == '')
            $(this).prop("disabled", true);
        });
        $("#filterForm").submit();
    });
</script>