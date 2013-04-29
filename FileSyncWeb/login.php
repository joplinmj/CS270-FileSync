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
                    /*if($_SERVER["HTTPS"] != "on")
                    {
                        header("Location: https://" . $_SERVER["HTTP_HOST"] . $_SERVER["REQUEST_URI"]);
                    }*/
                    
                    session_start();
                    if(isset($_SESSION['username'])){
                        header("Location: home.php");
                    }
                    mysql_connect("localhost", "root", "12345") or die(mysql_error()); 
                    mysql_select_db("fileshare") or die(mysql_error()); 
                    if(isset($_POST['email']) && !empty($_POST['email']) AND isset($_POST['password']) && !empty($_POST['password'])){  
                        $email = mysql_escape_string($_POST['email']);  
                        $password = mysql_escape_string(md5($_POST['password']));  
                                      
                        $search = mysql_query("SELECT email, password, active FROM users WHERE email='".$email."' AND password='".$password."' AND active='1'") or die(mysql_error());   
                        $match  = mysql_num_rows($search);  
                        
                        if($match > 0){  
                            $msg = 'Login Complete! Thanks';  
                            $_SESSION['username'] = $email;
                            header("Location: home.php");  
                            exit();
                        }else{  
                            $msg = 'Login Failed! Please make sure that you enter the correct details and that you have activated your account.';  
                        }  
                    }  
                ?>
                    
                
                <h3>Login Form</h3>  
                <p>Please enter your login information.</p>  
                
                      
                <?php   
                    if(isset($msg)){ 
                        echo '<div class="statusmsg">'.$msg.'</div>';   
                    }   
                ?>  
                     
                <form class="user" action="" method="post">  
                <label for="email">Email:</label>  
                <input type="text" name="email" value="" />  
                <label for="password">Password:</label>  
                <input type="password" name="password" value="" />  
                  
                <input type="submit" class="submit_button" value="Login" />  
                </form>     
                <a href="signup.php">Signup</a>
            </div>  
        </div>
    </div>
</body>
</html>