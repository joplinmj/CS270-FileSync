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
            <div id='wrap'>
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
                        $password = mysql_escape_string($_POST['password']);
                        
                        if(!eregi("^[_a-z0-9-]+(\.[_a-z0-9-]+)*@[a-z0-9-]+(\.[a-z0-9-]+)*(\.[a-z]{2,3})$", $email)){  
                            $msg = 'The email is invalid. Please retry.';  
                        }else{  
                            $msg = 'Your account has been successfully made, <br/> please verify by clicking the link in your e-mail.';
                            $hash = hash("md5",rand(0,1000));
                            mysql_query("INSERT INTO users (password, email, hash) VALUES(
                            '". mysql_escape_string(hash('md5',$password)) ."',  
                            '". mysql_escape_string($email) ."',  
                            '". mysql_escape_string($hash) ."') ") or die(mysql_error()); 
                            
                            $to      = $email;
                            $subject = 'Signup | Verification';   
                            $message = ' 
                             
                            Thanks for signing up! 
                            Your account has been created, you can login with the following credentials after you have activated your account by pressing the url below. 
                             
                            ------------------------ 
                            Username: '.$email.' 
                            Password: '.$password.' 
                            ------------------------ 
                             
                            Please click this link to activate your account: 
                             
                            http://ec2-54-235-31-37.compute-1.amazonaws.com/verify.php?email='.$email.'&hash='.$hash.' 
                             
                            ';  
                                                  
                            $headers = 'From:matthew.j.joplin@vanderbilt.edu' . "\r\n"; 
                            mail($to, $subject, $message, $headers); 
                        }  
                    }  
                ?>
                    
                
                <h3>Signup Form</h3>  
                <p>Please provide the information to create your account.</p>  
                
                      
                <?php   
                    if(isset($msg)){  // Check if $msg is not empty  
                        echo '<div class="statusmsg">'.$msg.'</div>'; // Display our message and wrap it with a div with the class "statusmsg".  
                    }   
                ?>  
                     
                <form class="user" action="" method="post">
                    <label for="email">Email:</label>  
                    <input type="text" name="email" value="" />
                    <label for="password">Password:</label>
                    <input type="password" name="password" class="password_test" value="" /><br />
                    <input type="submit" value="Sign up" />  
                </form> 
                <a href="login.php">Already signed up? Login.</a>
            </div>  
        </div>
    </div>
</body>
</html>