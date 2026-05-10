<?php $page_title="Dashboard";
    include("includes/header.php");
    require("includes/lb_helper.php");
    
    $qry_cat="SELECT COUNT(*) as num FROM tbl_category";
    $total_category= mysqli_fetch_array(mysqli_query($mysqli,$qry_cat));
    $total_category = $total_category['num'];
    
    $qry_albums="SELECT COUNT(*) as num FROM tbl_album";
    $total_albums = mysqli_fetch_array(mysqli_query($mysqli,$qry_albums));
    $total_albums = $total_albums['num'];
    
    $qry_artist="SELECT COUNT(*) as num FROM tbl_artist";
    $total_artist = mysqli_fetch_array(mysqli_query($mysqli,$qry_artist));
    $total_artist = $total_artist['num'];
    
    $qry_audio="SELECT COUNT(*) as num FROM tbl_audio";
    $total_audio = mysqli_fetch_array(mysqli_query($mysqli,$qry_audio));
    $total_audio = $total_audio['num'];
    
    $qry_playlist="SELECT COUNT(*) as num FROM tbl_playlist";
    $total_playlist = mysqli_fetch_array(mysqli_query($mysqli,$qry_playlist));
    $total_playlist = $total_playlist['num'];
    
    $qry_re="SELECT COUNT(*) as num FROM tbl_reports";
    $total_reports = mysqli_fetch_array(mysqli_query($mysqli,$qry_re));
    $total_reports = $total_reports['num'];
    
    $qry_users="SELECT COUNT(*) as num FROM tbl_users";
    $total_users = mysqli_fetch_array(mysqli_query($mysqli,$qry_users));
    $total_users = $total_users['num'];
    
    $qry_admin="SELECT COUNT(*) as num FROM tbl_admin";
    $total_admin = mysqli_fetch_array(mysqli_query($mysqli,$qry_admin));
    $total_admin = $total_admin['num'];
    
    $sql_user="SELECT * FROM tbl_users ORDER BY tbl_users.`id` DESC LIMIT 5";
    $result_user=mysqli_query($mysqli,$sql_user);
    
    $countStr = '';
    $no_data_status = false;
    $count = $monthCount = 0;
    
    for ($mon = 1; $mon <= 12; $mon++) {
    
        $monthCount++;
        
        if (isset($_GET['filterByYear'])) {
            $year = $_GET['filterByYear'];
        } else {
            $year = date('Y');
        }
        
        $month = date('M', mktime(0, 0, 0, $mon, 1, $year));
        $sql_user = "SELECT `id` FROM tbl_users WHERE `registered_on` <> 0 AND DATE_FORMAT(FROM_UNIXTIME(`registered_on`), '%c') = '$mon' AND DATE_FORMAT(FROM_UNIXTIME(`registered_on`), '%Y') = '$year'";
        $totalcount = mysqli_num_rows(mysqli_query($mysqli, $sql_user));
        
        $countStr.="$totalcount, ";
        $monthStr.="'".$month."', ";
        
        if ($totalcount == 0) {
            $count++;
        }
    }
    
    if ($monthCount > $count) {
        $no_data_status = false;
    } else {
        $no_data_status = true;
    }
    
    $countStr=rtrim($countStr, ", ");
    $monthStr=rtrim($monthStr, ", ");
?>


<!-- Start: main -->
<main id="nsofts_main">
    <div class="nsofts-container">

        <div class="row g-4">
            
            <div class="col-xl-3 col-sm-6 col-12">
                <div class="card card-badge">
                    <div class="card-body">
                        <div class="row">
                            <div class="col">
                                <span class="h6 font-semibold text-muted text-sm d-block mb-2">Categories</span>
                                <span class="h3 font-bold mb-0"><?php echo thousandsNumberFormat($total_category); ?></span>
                            </div>
                            <div class="col-auto">
                                <div class="icon-shape bg-warning text-white text-lg">
                                    <i class="ri-folder-3-line"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-xl-3 col-sm-6 col-12">
                <div class="card card-badge">
                    <div class="card-body">
                        <div class="row">
                            <div class="col">
                                <span class="h6 font-semibold text-muted text-sm d-block mb-2">Artist</span>
                                <span class="h3 font-bold mb-0"><?php echo thousandsNumberFormat($total_artist); ?></span>
                            </div>
                            <div class="col-auto">
                                <div class="icon-shape bg-danger text-white text-lg">
                                    <i class="ri-user-3-line"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-xl-3 col-sm-6 col-12">
                <div class="card card-badge">
                    <div class="card-body">
                        <div class="row">
                            <div class="col">
                                <span class="h6 font-semibold text-muted text-sm d-block mb-2">Album</span>
                                <span class="h3 font-bold mb-0"><?php echo thousandsNumberFormat($total_albums); ?></span>
                            </div>
                            <div class="col-auto">
                                <div class="icon-shape bg-success text-white text-lg">
                                    <i class="ri-album-line"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-xl-3 col-sm-6 col-12">
                <div class="card card-badge">
                    <div class="card-body">
                        <div class="row">
                            <div class="col">
                                <span class="h6 font-semibold text-muted text-sm d-block mb-2">Audio</span>
                                <span class="h3 font-bold mb-0"><?php echo thousandsNumberFormat($total_audio); ?></span>
                            </div>
                            <div class="col-auto">
                                <div class="icon-shape bg-info text-white text-lg">
                                    <i class="ri-disc-line"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-xl-3 col-sm-6 col-12">
                <div class="card card-badge">
                    <div class="card-body">
                        <div class="row">
                            <div class="col">
                                <span class="h6 font-semibold text-muted text-sm d-block mb-2">Playlist</span>
                                <span class="h3 font-bold mb-0"><?php echo thousandsNumberFormat($total_playlist); ?></span>
                            </div>
                            <div class="col-auto">
                                <div class="icon-shape bg-primary text-white text-lg">
                                    <i class="ri-play-list-fill"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-xl-3 col-sm-6 col-12">
                <div class="card card-badge">
                    <div class="card-body">
                        <div class="row">
                            <div class="col">
                                <span class="h6 font-semibold text-muted text-sm d-block mb-2">Reports</span>
                                <span class="h3 font-bold mb-0"><?php echo thousandsNumberFormat($total_reports); ?></span>
                            </div>
                            <div class="col-auto">
                                <div class="icon-shape bg-warning text-white text-lg">
                                    <i class="ri-feedback-line"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-xl-3 col-sm-6 col-12">
                <div class="card card-badge">
                    <div class="card-body">
                        <div class="row">
                            <div class="col">
                                <span class="h6 font-semibold text-muted text-sm d-block mb-2">Users</span>
                                <span class="h3 font-bold mb-0"><?php echo thousandsNumberFormat($total_users); ?></span>
                            </div>
                            <div class="col-auto">
                                <div class="icon-shape bg-danger text-white text-lg">
                                    <i class="ri-folder-user-line"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-xl-3 col-sm-6 col-12">
                <div class="card card-badge">
                    <div class="card-body">
                        <div class="row">
                            <div class="col">
                                <span class="h6 font-semibold text-muted text-sm d-block mb-2">Admin Users</span>
                                <span class="h3 font-bold mb-0"><?php echo thousandsNumberFormat($total_admin); ?></span>
                            </div>
                            <div class="col-auto">
                                <div class="icon-shape bg-success text-white text-lg">
                                    <i class="ri-admin-line"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            
        </div>
            
        <div class="row g-4 mt-2">
            
            <div class="col-lg-7 col-md-6">
                <div class="card card-dashboard h-100">
                    <div class="card-body p-4">
                        <div class="d-flex justify-content-between align-items-center">
                            <div class="me-2">
                                <h5 class="mb-4">Users Analytics</h5>
                            </div>
                            <div class="d-inline-flex">
                                <form method="get" id="graphFilter">
                                <select class="form-control" name="filterByYear" style="width: 120px;" >
                                <?php 
                                    $currentYear=date('Y');
                                    $minYear=2022;
                                    for ($i=$currentYear; $i >= $minYear ; $i--) { 
                                ?>
                                <option value="<?=$i?>" <?=(isset($_GET['filterByYear']) && $_GET['filterByYear']==$i) ? 'selected' : ''?>><?=$i?></option>
                                <?php } ?>
                                </select>
                            </form>
                            </div>
                        </div>
                        <div style="height: 300px">
                            <?php if($no_data_status){ ?>
                                <h3 class="text-muted text-center" style="padding-bottom: 2em">No data found !</h3>
                            <?php } else{ ?>
                                <canvas id="nsofts_analytics"></canvas>
                            <?php } ?>
                        </div>
                    </div>
                </div>
            </div>   
            
            <div class="col-lg-5 col-md-6">
                <div class="card card-dashboard h-100">
                    <div class="card-body p-4">
                        <div class="d-flex align-items-center justify-content-between">
                            <h5 class="mb-0">New users</h5>
                            <div class="dropdown">
                                <a href="javascript:void(0);" class="text-decoration-none text-dark" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                                    <i class="ri-more-2-fill"></i>
                                </a>
                                <ul class="dropdown-menu dropdown-menu-end dropdown-menu-sm">
                                    <li><a class="dropdown-item" href="create_user.php?add=yes">Create</a></li>
                                    <li><a class="dropdown-item" href="manage_users.php">Manage</a></li>
                                  </ul>
                            </div>
                        </div>
                        <?php if(mysqli_num_rows($result_user) > 0){ ?>
                        
                            <?php $i=0; while($row=mysqli_fetch_array($result_user)) { ?>
                            
                                <div class="d-flex align-items-center mt-4">
                                    <?php if($row['profile_img']!="" AND file_exists("images/".$row['profile_img'])){?>
                                        <img class="col-sm-1 img-thumbnail display-desktop" src="images/<?php echo $row['profile_img']?>"alt="" style="padding: 1px;">
                                    <?php }else{?>
                                        <img class="col-sm-1 img-thumbnail display-desktop" src="assets/images/user_photo.png" alt="" style="padding: 1px;">
                                    <?php }?>
                                    <div class="flex-grow-1 px-3">
                                        <span class="d-block text-muted"><?php echo $row['user_email'];?></span>
                                        <span class="d-block fw-semibold"><?php echo $row['user_name'];?></span>
                                    </div>
                                    <span><td><?php echo calculate_time_span($row['registered_on']);?></td></span>
                                </div>
                            <?php $i++; } ?> 
                            
                        <?php }else{ ?>
                            <ul class="p-2">
                                <h3 class="text-center">No data found !</h3>
                            </ul>
                        <?php } ?>
                       
                    </div>
                </div>
            </div>
            
        </div>
        
    </div>
</main>
<!-- End: main -->
    
<?php include("includes/footer.php");?> 

<?php if(!$no_data_status){ ?>
<script>
    const isDarkMode = function() {
        return localStorage.getItem('dark_mode') === 'true';
    }

    const getCSSVarValue = function(name) {
        let hex = getComputedStyle(document.documentElement).getPropertyValue('--ns-' + name);
        if (hex && hex.length > 0) {
            hex = hex.trim();
        }
        return hex;
    }

    if (Chart) {
        const defaults = Chart.defaults;
        const config = {
            color: isDarkMode() ? '#fff' : getCSSVarValue('body-color'),
            borderColor: isDarkMode() ? '#2d2f32' : getCSSVarValue('gray-10'),
            
            // Chart typo
            font: {
                family: getCSSVarValue('body-font-family'),
                size: 13
            },
        };
        
        Object.assign(defaults, config);
    }

    const canvas = document.getElementById('nsofts_analytics');
    if (canvas) {
        const config = {
            type: 'line',
            data: {
                
                labels: <?php echo "[".$monthStr."]";?>,
                datasets: [
                    {
                        label: 'Users',
                        data: <?php echo "[".$countStr."]";?>,
                        backgroundColor: getCSSVarValue('primary'),
                        borderColor: getCSSVarValue('primary'),
                        tension: 0.1
                    }
                ]
            },
            options: {
                title: {
                    display: false,
                },
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        min: 0,
                        
                        grid: {
                            borderColor: isDarkMode() ? '#2d2f32' : getCSSVarValue('gray-10'),
                        }
                    },
                    x: {
                        grid: {
                            borderColor: isDarkMode() ? '#2d2f32' : getCSSVarValue('gray-10'),
                        }
                    }
                },
                layout: {
                    margin: 0,
                    padding: 0
                },
                plugins: {
                    legend: {
                        display: false
                    },
                }
            }
        };
        analyticsChart = new Chart(canvas, config);
    }
</script>
<?php } ?>
<script type="text/javascript">
  // filter of graph
  $("select[name='filterByYear']").on("change",function(e){
    $("#graphFilter").submit();
  });
</script>