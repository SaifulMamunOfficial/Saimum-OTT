<?php 
    $page_title = 'View Suggestion';
    include("includes/header.php");
    require("includes/lb_helper.php");
    require("language/language.php");
    require_once("thumbnail_images.class.php");
    
    if(isset($_GET['suggestion_id'])){
        $qry="SELECT * FROM tbl_suggest where id='".$_GET['suggestion_id']."'";
        $result=mysqli_query($mysqli,$qry);
        $row=mysqli_fetch_assoc($result);
        
        $users_qry="SELECT * FROM tbl_users where id='".$row['user_id']."'";
        $users_result=mysqli_query($mysqli,$users_qry);
    }
    
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
        header("Location:view_suggestion.php");
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
            <div class="row g-4">
                <div class="col-lg-6">
                    <div class="card h-100">
                        <div class="card-body p-4">
                            <h5 class="mb-4"><?=$page_title ?></h5>
                            
                            <div class="mb-3">
                                <input type="text" class="form-control" value="Title - <?php if(isset($_GET['suggestion_id'])){echo $row['suggest_title'];}?>">
                            </div>
                            
                            <div class="mb-3">
                                 <textarea class="form-control" rows="7">Message - <?php if(isset($_GET['suggestion_id'])){echo $row['suggest_message'];}?></textarea>
                            </div>
                            
                            <div>
                                <?php if(isset($_GET['suggestion_id'])) {?>
                                    <?php if($row['suggest_image']!="" AND file_exists("images/".$row['suggest_image'])){?>
                                        <a href="images/<?php echo $row['suggest_image']?>" target="_blank">
                                            <img class="col-sm-2 img-thumbnail" type="image" src="images/<?php echo $row['suggest_image'];?>" alt="image" />
                                        </a>
                                    <?php }else{?>
                                        <img class="col-sm-2 img-thumbnail" src="assets/images/300x300.jpg" alt="no image">
                                    <?php }?>
                                <?php } else {?>
                                    <img class="col-sm-2 img-thumbnail" type="image" src="assets/images/300x300.jpg" alt="image" />
                                <?php } ?>
                            </div>
                            
                        </div>
                    </div>
                </div>
                
                <div class="col-lg-6">
                    <div class="card h-100">
                        <div class="card-body p-4">
                            <h5 class="mb-4">Notify this user</h5>
                            <form action="" name="addeditone" method="POST" enctype="multipart/form-data">
                                
                                <div class="mb-3">
                                    <select name="user_id" id="user_id" class="nsofts-select" required>
                                        <?php if(isset($_GET['suggestion_id'])){ ?>
                                            <?php while($users_row=mysqli_fetch_array($users_result)){ ?>      
                                                <option value="<?php echo $users_row['id'];?>"><?php echo $users_row['user_name'];?></option> 
                                            <?php } ?>
                                            <option value="">--Select User --</option>
                                        <?php } else { ?>
                                            <option value="">--Select User --</option>						 
                                        <?php } ?>
                                    </select>
                                </div>
                                
                                <div class="mb-3">
                                    <input type="text" name="notification_title" id="notification_title" placeholder="Enter notification title" class="form-control" required>
                                </div>
                                
                                <div class="mb-3">
                                    <textarea name="notification_msg" rows="8" id="notification_msg" class="form-control" placeholder="Enter notification message"  required></textarea>
                                </div>
                                
                                <button type="submit" name="submit" class="btn btn-primary" style="min-width: 120px;">Send</button>
                                
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </form>
    </div>
</main>
<!-- End: main -->
<?php include("includes/footer.php");?> 