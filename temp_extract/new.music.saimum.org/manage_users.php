<?php $page_title="Manage Users";
    include("includes/header.php");
    require("includes/lb_helper.php");
    require("language/language.php");
    
    if(!isset($_SESSION['admin_type'])){
        if($_SESSION['admin_type'] == 0){
            session_destroy();
            header( "Location:index.php");
            exit;
        }
    }
    
    $tableName = "tbl_users";   
    
    if(!isset($_GET['keyword'])){
        $sql_query="SELECT * FROM tbl_users ORDER BY tbl_users.`id` DESC"; 
    } else {
        $keyword=addslashes(trim($_GET['keyword']));
        $sql_query="SELECT * FROM tbl_users WHERE (`user_name` LIKE '%$keyword%' OR `user_email` LIKE '%$keyword%' OR `user_phone` LIKE '%$keyword%') ORDER BY tbl_users.`id` DESC"; 
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
                    <form method="get" id="searchForm" action="" class="me-2">
                        <div class="input-group">
                            <input type="text" id="search_input" class="form-control" placeholder="Search name, email or phone" name="keyword" value="<?php if(isset($_GET['keyword'])){ echo $_GET['keyword'];} ?>" required="required">
                            <button class="btn btn-outline-default d-inline-flex align-items-center" type="search">
                                <i class="ri-search-2-line"></i>
                            </button>
                        </div>
                    </form>
                    <a href="create_user.php?add=yes" class="btn btn-primary d-inline-flex align-items-center justify-content-center">
                        <i class="ri-add-line"></i>
                        <span class="ps-1 text-nowrap d-none d-sm-block">Create User</span>
                    </a>
                </div>
            </div>
            <div class="card-body p-4">
                <?php if(mysqli_num_rows($result) > 0){ ?>
                    <div class="row g-4">
                        <table class="table">
                            <thead>
                                <tr>
                                    <th class="display-desktop" style="width: 70px !important;">Image</th>
                                    <th>Name</th>
                                    <th class="display-desktop">Email</th>
                                    <th class="display-desktop">Registered</th>
                                    <th class="text-center text-truncate">Status</th>
                                    <th style="width: 150px;" class="text-center">Actions</th>
                                </tr>
                            </thead>
                            <tbody id="load-more-container">
                                <?php $i=0; while($row=mysqli_fetch_array($result)) { ?>
                                    <tr class="card-item">
                                        <td class="display-desktop">
                                            <div class="text-center">
                                                <?php if($row['user_type']=='Google'){?>
                                                    <img src="assets/images/google-logo.png" class="social_img" alt="">
                                                <?php }?>
                                                <img src="images/<?php echo $row['profile_img']?>" class="image-card__cover" onerror="this.src='assets/images/user_photo.png';"  loading="lazy" alt="">
                                            </div>
                                        </td>
                                        <td class="text-truncate"><?php echo $row['user_name'];?></td>
                                        <td class="display-desktop text-truncate"><?php echo $row['user_email'];?></td>
                                        <td class="display-desktop"><?php echo date('d-m-Y',$row['registered_on']);?></td>
                                        <td class="text-center" >
                                            <div class="nsofts-switch enable_disable" data-bs-toggle="tooltip" data-bs-placement="top" title="Enable / Disable">
                                                <input type="checkbox" id="enable_disable_check_<?= $i ?>" data-id="<?= $row['id'] ?>" data-table="<?=$tableName ?>" data-column="status" class="cbx hidden btn_enable_disable" <?php if ($row['status'] == 1) { echo 'checked'; } ?>>
                                                <label for="enable_disable_check_<?= $i ?>" class="nsofts-switch__label"></label>
                                            </div>
                                        </td>
                                        <td class="text-center">
                                            <a href="create_user.php?user_id=<?php echo $row['id']; ?>" class="btn btn-primary btn-icon" style="padding: 10px 10px !important;  margin-right: 10px !important;" data-bs-toggle="tooltip" data-bs-placement="top" title="Edit">
                                                <i class="ri-pencil-line"></i>
                                            </a>
                                            <a href="javascript:void(0)" class="btn btn-danger btn-icon btn_delete" data-id="<?php echo $row['id'];?>" data-table="<?=$tableName ?>" style="padding: 10px 10px !important;" data-bs-toggle="tooltip" data-bs-placement="top" title="Delete">
                                                <i class="ri-delete-bin-line"></i>
                                            </a>
                                        </td>
                                    </tr>
                                 <?php $i++; } ?>
                            </tbody>
                        </table>
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