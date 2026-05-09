<?php 
    $page_title=(isset($_GET['news_id'])) ? 'Edit News' : 'Create News';
    include("includes/header.php");
    require("includes/lb_helper.php");
    require("language/language.php");
    require_once("thumbnail_images.class.php");
    
    $page_save=(isset($_GET['news_id'])) ? 'Save' : 'Create';
    
    if(isset($_POST['submit']) and isset($_GET['add'])){
        
        $data = array( 
            'news_title'  =>  cleanInput($_POST['news_title']),
            'news_url'  =>  htmlentities(trim($_POST['news_url']))
        );  
        
        $qry = Insert('tbl_news',$data);
        
        $_SESSION['msg']="10";
        $_SESSION['class']='success';
        header( "Location:manage_news.php");
        exit;
    }
    
    if(isset($_GET['news_id'])){
        $qry="SELECT * FROM tbl_news where id='".$_GET['news_id']."'";
        $result=mysqli_query($mysqli,$qry);
        $row=mysqli_fetch_assoc($result);
    }
    
    if(isset($_POST['submit']) and isset($_POST['news_id'])){
 
        $data = array( 
            'news_title'  =>  cleanInput($_POST['news_title']),
            'news_url'  =>  htmlentities(trim($_POST['news_url']))
        );  
        
        $category_edit=Update('tbl_news', $data, "WHERE id = '".$_POST['news_id']."'");
        
        $_SESSION['msg']="11";
        $_SESSION['class']='success';
        header( "Location:create_news.php?news_id=".$_POST['news_id']);
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
                        <form action="" name="addeditnews" method="POST" enctype="multipart/form-data">
                            <input  type="hidden" name="news_id" value="<?=(isset($_GET['news_id'])) ? $_GET['news_id'] : ''?>" />
                            
                            <div class="mb-3 row">
                                <label class="col-sm-2 col-form-label">News Title</label>
                                <div class="col-sm-10">
                                    <input type="text" name="news_title" class="form-control" value="<?php if(isset($_GET['news_id'])){echo $row['news_title'];}?>" required>
                                </div>
                            </div>
                            
                            <div class="mb-3 row">
                                <label class="col-sm-2 col-form-label">News Url</label>
                                <div class="col-sm-10">
                                    <input type="text" name="news_url" class="form-control" value="<?php if(isset($_GET['news_id'])){echo $row['news_url'];}?>" required>
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