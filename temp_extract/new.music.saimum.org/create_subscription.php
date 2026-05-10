<?php 
    $page_title=(isset($_GET['sub_id'])) ? 'Edit Subscription' : 'Add Subscription';
    include("includes/header.php");
    require("includes/lb_helper.php");
    require("language/language.php");
    require_once("thumbnail_images.class.php");
    
    $page_save=(isset($_GET['sub_id'])) ? 'Save' : 'Create';
    
    if(isset($_POST['submit']) and isset($_GET['add'])){

        $data = array( 
            'name'  =>  $_POST['name'],
            'duration'  =>  $_POST['duration'],
            'price'  =>  $_POST['price'],
            'currency_code'  =>  $_POST['currency_code'],
            'subscription_id'  =>  $_POST['subscription_id'],
            'base_key'  =>  $_POST['base_key'],
        );
        $qry = Insert('tbl_subscription',$data);  
        
        $_SESSION['msg']="10";
        $_SESSION['class']='success'; 
        header( "Location:manage_subscription.php");
        exit; 
    }
    
    if(isset($_GET['sub_id'])){
        $qry="SELECT * FROM tbl_subscription where id='".$_GET['sub_id']."'";
        $result=mysqli_query($mysqli,$qry);
        $row=mysqli_fetch_assoc($result);
    }
    
    if(isset($_POST['submit']) and isset($_POST['sub_id'])){
        
        $data = array( 
            'name'  =>  $_POST['name'],
            'duration'  =>  $_POST['duration'],
            'price'  =>  $_POST['price'],
            'currency_code'  =>  $_POST['currency_code'],
            'subscription_id'  =>  $_POST['subscription_id'],
            'base_key'  =>  $_POST['base_key'],
        );
        $category_edit=Update('tbl_subscription', $data, "WHERE id = '".$_POST['sub_id']."'");
        
        $_SESSION['msg']="11";
        $_SESSION['class']='success'; 
        header( "Location:create_subscription.php?sub_id=".$_POST['sub_id']);
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
                        <form action="" name="addeditcategory" method="POST" enctype="multipart/form-data">
                            <input  type="hidden" name="sub_id" value="<?=(isset($_GET['sub_id'])) ? $_GET['sub_id'] : ''?>" />
                            
                            <div class="mb-3 row">
                                <label class="col-sm-2 col-form-label">Plan name</label>
                                <div class="col-sm-10">
                                    <input type="text" name="name" class="form-control" value="<?php if(isset($_GET['sub_id'])){echo $row['name'];}?>" required>
                                </div>
                            </div>
                            
                            <div class="mb-3 row">
                                <label class="col-sm-2 col-form-label">Plan Duration</label>
                                <div class="col-sm-10">
                                    <input type="number" name="duration" class="form-control" value="<?php if(isset($_GET['sub_id'])){echo $row['duration'];}?>" required>
                                </div>
                            </div>
                            
                            <div class="mb-3 row">
                                <label class="col-sm-2 col-form-label">Plan price</label>
                                <div class="col-sm-10">
                                    <input type="number" name="price" class="form-control" value="<?php if(isset($_GET['sub_id'])){echo $row['price'];}?>" required>
                                </div>
                            </div>
                            
                            <div class="mb-3 row">
                                <label class="col-sm-2 col-form-label">Currency code</label>
                                <div class="col-sm-10">
                                    <input type="text" name="currency_code" class="form-control" value="<?php if(isset($_GET['sub_id'])){echo $row['currency_code'];}?>" required>
                                </div>
                            </div>
                            
                            <div class="mb-3 row">
                                <label class="col-sm-2 col-form-label">Subscription id</label>
                                <div class="col-sm-10">
                                    <input type="text" name="subscription_id" class="form-control" value="<?php if(isset($_GET['sub_id'])){echo $row['subscription_id'];}?>" required>
                                    <small id="sh-text1" class="form-text text-muted col-md-6" style="padding: 0px;"> <a class="text-secondary text-decoration-none">To get key go to Developer Console > Select your app > Products > Subscriptions.</a></small>
                                </div>
                            </div>
                            
                            <div class="mb-3 row">
                                <label class="col-sm-2 col-form-label">Base key</label>
                                <div class="col-sm-10">
                                    <input type="text" name="base_key" class="form-control" value="<?php if(isset($_GET['sub_id'])){echo $row['base_key'];}?>" required>
                                    <small id="sh-text1" class="form-text text-muted col-md-6" style="padding: 0px;"> <a class="text-secondary text-decoration-none">To get key go to Developer Console > Select your app > Products > Subscriptions.</a></small>
                                </div>
                            </div>
                            
                            <div class="row">
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