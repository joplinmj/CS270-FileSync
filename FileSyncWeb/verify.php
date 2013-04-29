<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>CS283 Cloud-base File Synchronization</title>

    <meta http-equiv='content-type' content='text/html; charset=utf-8'/>
    <meta http-equiv='charset' content='utf-8'/>
    <meta name='keywords' content='fileshare,files,transfer,cs283,networks,project'/>
    <meta name='description' content='Cloud-based file synchronization'/>
          <link rel="stylesheet" type="text/css" href="cloud.css"/>
    <script src="http://code.jquery.com/jquery-1.9.1.js"></script>
    <script src="http://code.jquery.com/ui/1.10.2/jquery-ui.js"></script>
    
    <script>
    </script>
</head>

<body>
    <div id='container'>
        <div id='header'>
            <h1><img src="cloud.png" alt=""></img>-based File Synchronization</h1>
        </div>
        
        <div id='content'>
            <div id="wrap">  
                <?php
                    mysql_connect("localhost", "root", "12345") or die(mysql_error()); 
                    mysql_select_db("fileshare") or die(mysql_error()); 
                    
                    if(isset($_GET['email']) && !empty($_GET['email']) AND isset($_GET['hash']) && !empty($_GET['hash'])){  
                        $email = mysql_escape_string($_GET['email']);  
                        $hash = mysql_escape_string($_GET['hash']);
                        $search = mysql_query("SELECT email, hash, active FROM users WHERE email='".$email."' AND hash='".$hash."' AND active='0'") or die(mysql_error());   
                        $match  = mysql_num_rows($search);
                        if($match > 0){
                            mysql_query("UPDATE users SET active='1' WHERE email='".$email."' AND hash='".$hash."' AND active='0'") or die(mysql_error());  
                            echo '<div class="statusmsg">Your account has been activated.</div>';  
                        } else{
                            echo '<div class="statusmsg">Invalid URL or account already activated.</div>';  
                        }
                    }else{  
                    }  
                ?>
                <a href="login.php">Login</a>
            </div>   
        </div>
    </div>
</body>
</html>