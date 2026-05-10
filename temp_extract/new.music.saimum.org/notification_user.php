<?php 
    $page_title='Notification send to a User';
    include("includes/header.php");
    require("includes/lb_helper.php");
    require("language/language.php");
    require_once("thumbnail_images.class.php");
    
    $users_qry="SELECT * FROM tbl_users ORDER BY user_name";
    $users_result=mysqli_query($mysqli,$users_qry);

    if(isset($_POST['submit'])) {
        
        $data = array(
            'user_id' => $_POST['user_id'],
            'notification_title' => $_POST['notification_title'],
            'notification_msg' => $_POST['notification_msg'],
            'notification_on' =>  strtotime(date('d-m-Y h:i:s A')) 
        );
        
        $qry = Insert('tbl_notification',$data);	
        
        $_SESSION['class'] = "success";
        $_SESSION['msg']="16";
        header("Location:notification_user.php");
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
                        <form action="" name="addeditone" method="POST" enctype="multipart/form-data">
                            
                             <div class="mb-3 row">
                                <label class="col-sm-2 col-form-label">Select User</label>
                                <div class="col-sm-10">
                                    <select name="user_id" id="user_id" class="nsofts-select" required>
                                        <option value="">--Select User--</option>
                                        <?php while($users_row=mysqli_fetch_array($users_result)){ ?>      
                                            <option value="<?php echo $users_row['id'];?>"><?php echo $users_row['user_name'];?></option> 
                                        <?php } ?> 
                                    </select>
                                </div>
                            </div>
                        
                            <div class="mb-3 row">
                                <label class="col-sm-2 col-form-label">Notification Title</label>
                                <div class="col-sm-10">
                                    <input type="text" name="notification_title" id="notification_title" value=""  class="form-control" required>
                                </div>
                            </div>
                            
                            <div class="mb-3 row">
                                <label class="col-sm-2 col-form-label">Notification Message</label>
                                <div class="col-sm-10">
                                    <textarea name="notification_msg" id="notification_msg" rows="5" class="form-control" required></textarea>
                                </div>
                            </div>

                            <div class="mb-3 row">
                                <label class="col-sm-2 col-form-label">&nbsp;</label>
                                <div class="col-sm-10">
                                    <button type="submit" name="submit" class="btn btn-primary" style="min-width: 120px;">Send</button>
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