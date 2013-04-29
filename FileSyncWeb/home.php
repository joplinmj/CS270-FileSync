<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>CS283 Cloud-base File Synchronization</title>

    <meta http-equiv='content-type' content='text/html; charset=utf-8'/>
    <meta http-equiv='charset' content='utf-8'/>
    <meta name='keywords' content='fileshare,files,transfer,cs283,networks,project'/>
    <meta name='description' content='Cloud-based file synchronization'/>
    <link rel="stylesheet" href="http://code.jquery.com/ui/1.10.2/themes/smoothness/jquery-ui.css" />
        <link rel="stylesheet" type="text/css" href="cloud.css"/>
    <script src="http://code.jquery.com/jquery-1.9.1.js"></script>
    <script src="http://code.jquery.com/ui/1.10.2/jquery-ui.js"></script>
    <script>
        $(document).ready(function() {
            $( "#accordion").accordion({
                heightStyle: "content",
                collapsible: "true"
            });
            
            $(".accordion .ui-accordion-header").css("background-color","blue");
        });
    </script>
</head>

<body>
    <div id='container'>
        <div id='header'>
            <h1><img src="cloud.png" alt=""></img>-based File Synchronization</h1>
        </div>
        
        <div id="nav">
            <form id="logout" action="logout.php" method="post">
                <input type="submit" id="logout_button" value="Logout">
            </form>
            <ul>
                <li><a href="home.php">Home</a></li>
                <li><a href="tutorial.php">Tutorial<a></li>
                <li><a href="FAQ.php">FAQ</a></li>
                <li><a href="about.php">About Us</a></li>
            </ul>
        </div>
                
        <div id='content'>

            
            <div id="accordion">
            <?php
                session_start();
                    if(!isset($_SESSION['username'])){
                        header("Location: login.php");
                    }
                $db = mysql_connect("localhost", "root", "12345");
               
                if (!$db) {
                    die('Could not connect: ' . mysql_error());
                }
                
                mysql_select_db("fileshare", $db);
                  
                $sql = 'SELECT * FROM files WHERE user = "'.$_SESSION['username'].'"GROUP BY name';
                $result = mysql_query($sql);
                $count = mysql_result($result,0);
                if($count == 0){
                    echo '<div id="wrap"><h3>You have no files currently on the server. Please consult the tutorial page for information on how to set up an instance.</h3></div>'.PHP_EOL;
                }
                while($row = mysql_fetch_array($result)){
                    $version = 'SELECT * FROM files WHERE name = "'.$row[name].'" ORDER BY date';
                    $v_result = mysql_query($version);
                    $url = $row["path"];
                    if(substr($row["name"],0,1) != "~"){
                        echo '<h3>'.$row["name"].'</h3>'.PHP_EOL;
                        echo '<div><p><a href= "http://'.$url.'" target="_blank">Download</a></p><br />'.PHP_EOL;
                        echo '<p>Past versions:<br /></p>'.PHP_EOL;
                        while($v_row = mysql_fetch_array($v_result)){
                            $v_url = $v_row["path"];
                            echo '<p>&nbsp;&nbsp<a href="http://'.$v_url.'" target="_blank">'.$v_row["date"].'</a></p>'.PHP_EOL;
                        }
                        echo '</div>'.PHP_EOL;
                    }
                }
                
                mysql_close();
            ?>
            </div>
        </div>
    </div>
</body>
</html>