<?php 
    $page_title=(isset($_GET['cat_id'])) ? 'Edit Category' : 'Create Category';
    include("includes/header.php");
    require("includes/lb_helper.php");
    require("language/language.php");
    require_once("thumbnail_images.class.php");
    
    $page_save=(isset($_GET['cat_id'])) ? 'Save' : 'Create';
    
    if(isset($_POST['submit']) and isset($_GET['add'])){
        
        if($_FILES['category_image']['name']!=""){
            
            $ext = pathinfo($_FILES['category_image']['name'], PATHINFO_EXTENSION);
            $category_image=rand(0,99999)."_category.".$ext;
            $tpath1='images/'.$category_image;
            
            if($ext!='png')  {
                $pic1=compress_image($_FILES["category_image"]["tmp_name"], $tpath1, 80);
            } else {
                $tmp = $_FILES['category_image']['tmp_name'];
                move_uploaded_file($tmp, $tpath1);
            }
            
        } else {
            $category_image='';
        }
        
        $data = array( 
            'category_name'  =>  cleanInput($_POST['category_name']),
            'category_image'  =>  $category_image
        );  
        
        $qry = Insert('tbl_category',$data);
        
        $_SESSION['msg']="10";
        $_SESSION['class']='success';
        header( "Location:manage_category.php");
        exit;
    }
    
    if(isset($_GET['cat_id'])){
        $qry="SELECT * FROM tbl_category where cid='".$_GET['cat_id']."'";
        $result=mysqli_query($mysqli,$qry);
        $row=mysqli_fetch_assoc($result);
    }
    
    if(isset($_POST['submit']) and isset($_POST['cat_id'])){
        
        if($_FILES['category_image']['name']!=""){
            
            if($row['category_image']!=""){
                unlink('images/'.$row['category_image']);
            }
            
            $ext = pathinfo($_FILES['category_image']['name'], PATHINFO_EXTENSION);
            $category_image=rand(0,99999)."_category.".$ext;
            $tpath1='images/'.$category_image;   
            
            if($ext!='png')  {
                $pic1=compress_image($_FILES["category_image"]["tmp_name"], $tpath1, 80);
            } else {
                $tmp = $_FILES['category_image']['tmp_name'];
                move_uploaded_file($tmp, $tpath1);
            }
        } else {
            $category_image=$row['category_image'];
        }
        
        $data = array( 
            'category_name'  =>  cleanInput($_POST['category_name']),
            'category_image'  =>  $category_image
        );
        
        $category_edit=Update('tbl_category', $data, "WHERE cid = '".$_POST['cat_id']."'");
        
        $_SESSION['msg']="11";
        $_SESSION['class']='success';
        header( "Location:create_category.php?cat_id=".$_POST['cat_id']);
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
                            <input  type="hidden" name="cat_id" value="<?=(isset($_GET['cat_id'])) ? $_GET['cat_id'] : ''?>" />
                            
                            <div class="mb-3 row">
                                <label class="col-sm-2 col-form-label">Category name</label>
                                <div class="col-sm-10">
                                     <input type="text" name="category_name" class="form-control" value="<?php if(isset($_GET['cat_id'])){echo $row['category_name'];}?>" required>
                                </div>
                            </div>
                            
                            <div class="mb-3 row">
                                <div class="mb-3 row">
                                    <label class="col-sm-2 col-form-label">Select Image</label>
                                    <div class="col-sm-10">
                                        <input type="file" class="form-control-file" name="category_image"   accept=".png, .jpg, .JPG .PNG" onchange="fileValidation()" id="fileupload" <?php if(!isset($_GET['cat_id'])){?>required<?php } ?>>
                                    </div>
                                </div>
                            </div>
                            
                            <div class="mb-3 row">
                                <div class="mb-3 row">
                                    <label class="col-sm-2 col-form-label">&nbsp;</label>
                                    <div class="col-sm-10">
                                        <div class="fileupload_img" id="imagePreview">
                                            <?php if(isset($_GET['cat_id'])) {?>
                                              <img class="col-sm-2 img-thumbnail" type="image" src="images/<?php echo $row['category_image'];?>" alt="image" />
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