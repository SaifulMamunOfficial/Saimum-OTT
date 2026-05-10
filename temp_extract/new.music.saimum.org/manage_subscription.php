<?php $page_title="Manage Subscription";
    include("includes/header.php");
    require("includes/lb_helper.php");
    require("language/language.php");
    
    $tableName="tbl_subscription";   
    
    if(!isset($_GET['keyword'])){
        $sql_query="SELECT * FROM tbl_subscription ORDER BY tbl_subscription.`id` DESC"; 
    } else {
        $keyword=addslashes(trim($_GET['keyword']));
        $sql_query="SELECT * FROM tbl_subscription WHERE (`name` LIKE '%$keyword%') ORDER BY tbl_subscription.`id` DESC"; 
    }
    $result=mysqli_query($mysqli, $sql_query) or die(mysqli_error($mysqli));
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
                    <form method="get" id="searchForm" action="" class="me-2">
                        <div class="input-group">
                            <input type="text" id="search_input" class="form-control" placeholder="Search title" name="keyword" value="<?php if(isset($_GET['keyword'])){ echo $_GET['keyword'];} ?>" required="required">
                            <button class="btn btn-outline-default d-inline-flex align-items-center" type="search">
                                <i class="ri-search-2-line"></i>
                            </button>
                        </div>
                    </form>
                    <a href="create_subscription.php?add=yes" class="btn btn-primary d-inline-flex align-items-center justify-content-center">
                        <i class="ri-add-line"></i>
                        <span class="ps-1 text-nowrap d-none d-sm-block">Create Subscription</span>
                    </a>
                </div>
            </div>
            <div class="card-body p-4">
                <?php if(mysqli_num_rows($result) > 0){ ?>
                    <div class="row g-4 mb-3" id="load-more-container">
                        <?php $i=0; while($row=mysqli_fetch_array($result)) { ?>
                            <div class="col-lg-3 col-sm-6" class="card-item">
                                <div class="nsofts-card-light p-3">
                                    <h5 class="mb-1"><?php echo $row['name'];?></h5>
                                    <p class="p-0 text-secondary"><?php echo $row['price'];?> <?php echo $row['currency_code'];?>, <?php echo $row['duration'];?> Day's</p>
                                    <div class="d-flex">
                                        <a href="create_subscription.php?sub_id=<?php echo $row['id'];?>" class="btn btn-outline-primary rounded-pill me-2 btn-icon" data-bs-toggle="tooltip" data-bs-placement="top" title="Edit">
                                            <i class="ri-pencil-line"></i>
                                        </a>
                                        <a href="javascript:void(0)" class="btn btn-outline-danger rounded-pill me-2 btn-icon btn_delete" data-id="<?php echo $row['id'];?>" data-table="<?=$tableName ?>" data-bs-toggle="tooltip" data-bs-placement="top" title="Delete">
                                            <i class="ri-delete-bin-line"></i>
                                        </a>
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