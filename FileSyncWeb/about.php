<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>CS283 Cloud-base File Synchronization</title>

    <meta http-equiv='content-type' content='text/html; charset=utf-8'/>
    <meta http-equiv='charset' content='utf-8'/>
    <meta name='keywords' content='fileshare,files,transfer,cs283,networks,project'/>
    <meta name='description' content='Cloud-based file synchronization'/>
    <link rel="stylesheet" href="http://code.jquery.com/ui/1.10.2/themes/smoothness/jquery-ui.css" />
    <link rel="stylesheet" href="colorbox.css" />
    <link rel="stylesheet" type="text/css" href="cloud.css"/>
	<script type="text/javascript" src="http://code.jquery.com/jquery-1.9.1.js"></script>
    <script type="text/javascript" src="http://code.jquery.com/ui/1.10.2/jquery-ui.js"></script>
	<script type="text/javascript" src="http://maps.google.com/maps/api/js?sensor=false"></script>
    <script type="text/javascript" src="https://raw.github.com/jbdemonte/gmap3/master/gmap3.js"></script>
	
    <script src="jquery.colorbox-min.js"></script>
    <script>
       jQuery(document).ready(function() {
        $('#map').width("600px").height("350px").css({"margin":"auto"}).gmap3({
          map: { 
			options: { 
				maxZoom:16, 
				center:[36.144733,-86.803083], 
				mapTypeId: google.maps.MapTypeId.SATELLITE, 
				mapTypeControl: true,
				mapTypeControlOptions: {style: google.maps.MapTypeControlStyle.DROPDOWN_MENU},
				navigationControl: true,
				scrollwheel: true,
				streetViewControl: true }},
		  marker: {latLng:[36.144733,-86.803083]},
        }, "autofit" );
		
		$(".group").colorbox({rel:'group', transition:"fade"});
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
                <li><a href="tutorial.php">Tutorial</a></li>
                <li><a href="FAQ.php">FAQ</a></li>
                <li><a href="about.php">About Us</a></li>
            </ul>
        </div>
                
        <div id='content'>
			<div id="wrap">  
                <h3>About Us</h3>
				
				<a class="group" href="pic1.png"><img src="pic1.png"/></a>
				<a class="group" href="pic3.png"><img src="pic3.png"/></a>
				<a class="group" href="pic2.png"><img src="pic2.png"/></a>
				
				<br/><br/>
                
				<div id="map"></div>
            </div>  
        </div>
    </div>
</body>
</html>