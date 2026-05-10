<?php $page_title="Manage Artist";
    include("includes/header.php");
    require("includes/lb_helper.php");
    require("language/language.php");
    
    $tableName="tbl_artist";   
    $keyword='';
    
    if(!isset($_GET['keyword'])){
        $sql_query="SELECT * FROM tbl_artist ORDER BY tbl_artist.`id` DESC"; 
    } else {
        $keyword=addslashes(trim($_GET['keyword']));
        $sql_query="SELECT * FROM tbl_artist WHERE `artist_name` LIKE '%$keyword%' ORDER BY tbl_artist.`id` DESC"; 
    }
    $result=mysqli_query($mysqli,$sql_query) or die(mysqli_error($mysqli));
?>


<style>
    .nsofts-image-card .nsofts-image-card__cover {
        border-radius: 300px !important;
        border: 1px solid var(--ns-dark) !important;
    }
    .nsofts-image-card .nsofts-image-card__content:before {
        background: none !important;
    }
</style>

<!-- Start: main -->
<main id="nsofts_main">
    <div class="nsofts-container">
        <div class="card h-100">
            <div class="card-top d-md-inline-flex align-items-center justify-content-between py-3 px-4">
                <div class="d-inline-flex align-items-center text-decoration-none fw-semibold">
                    <span class="ps-2 lh-1"><?=$page_title ?></span>
                </div>
                <div class="d-flex mt-2 mt-md-0">
                    <form method="get" id="searchForm" action="" class="me-2">
                        <div class="input-group">
                            <input type="text" id="search_input" class="form-control" placeholder="Search here..." name="keyword" value="<?php if(isset($_GET['keyword'])){ echo $_GET['keyword'];} ?>" required="required">
                            <button class="btn btn-outline-default d-inline-flex align-items-center" type="search">
                                <i class="ri-search-2-line"></i>
                            </button>
                        </div>
                    </form>
                    <a href="create_artist.php?add=yes" class="btn btn-primary d-inline-flex align-items-center justify-content-center">
                        <i class="ri-add-line"></i>
                        <span class="ps-1 text-nowrap d-none d-sm-block">Create Artist</span>
                    </a>
                </div>
            </div>
            <div class="card-body p-4">
                <?php if(mysqli_num_rows($result) > 0){ ?>
                    <div class="row g-4" id="load-more-container">
                        <?php $i=0; while($row=mysqli_fetch_array($result)) { ?>
                            <div class="col-lg-3 col-sm-6 card-item">
                                <div class="nsofts-image-card">
                                    <div class="nsofts-image-card__cover">
                                        <img src="images/<?=$row['artist_image']?>" onerror="this.src='assets/images/300x300.jpg';"  loading="lazy"  alt="">
                                    </div>
                                    <div class="nsofts-image-card__content">
                                        <div class="position-relative">
                                            <div class="d-flex align-items-center justify-content-between nsofts-image-card__content__text">
                                                <span class="d-block text-truncate fs-6 fw-semibold pe-2"><?php echo $row['artist_name'];?></span>
                                                <div class="nsofts-image-card__option d-flex">
                                                    <a href="create_artist.php?artist_id=<?php echo $row['id'];?>" class="btn border-0" data-bs-toggle="tooltip" data-bs-placement="top" title="Edit">
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
                    <button class="nsofts-load-btn mt-4 mb-2 d-flex align-items-center justify-content-center" id="load-more-btn">
                        <span>Load More</span>
                        <i class="ri-sort-desc"></i>
                    </button>
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