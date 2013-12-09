<%@ page language="java" contentType="text/html; charset=UTF-8"  
    pageEncoding="UTF-8"%>  
<%  
    String path = request.getContextPath();  
    String WsBasePath = "ws://" + request.getServerName() + ":"  
            + request.getServerPort() + path + "/";  
%> 

<!DOCTYPE HTML>
<html>
	<title>Imperial Chaturaji!</title>
  <head>
      
      <script>
      	var myName;
      	var game_info;
      	var webSocketAddress;
//        var myName = 'testNameFifteen';
//        var game_info = [
//                         {"gameID": 1234, "playerList": ["boaty555Fifteen","nellyTheEllyFif","MrElephantFifte","ElmaTheElephant"]},
//                         {"gameID": 4321, "playerList": ["AI1","AI2","MrElephant"]},
//                         {"gameID": 2222, "playerList": ["AI1","AI2","Elmer"]},
//                         {"gameID": 1234, "playerList": ["boaty555Fifteen","nellyTheEllyFif","MrElephantFifte","ElmaTheElephant"]},
//                         {"gameID": 4321, "playerList": ["AI1","AI2","MrElephant"]},
//                         {"gameID": 2222, "playerList": ["AI1","AI2","Elmer"]},
//                         {"gameID": 1234, "playerList": ["boaty555Fifteen","nellyTheEllyFif","MrElephantFifte","ElmaTheElephant"]},
//                         {"gameID": 4321, "playerList": ["AI1","AI2","MrElephant"]},
//                         {"gameID": 2222, "playerList": ["AI1","AI2","Elmer"]},
//                         {"gameID": 1234, "playerList": ["boaty555Fifteen","nellyTheEllyFif","MrElephantFifte","ElmaTheElephant"]},
//                         {"gameID": 4321, "playerList": ["AI1","AI2","MrElephant"]},
//                         {"gameID": 2222, "playerList": ["AI1","AI2","Elmer"]},
//                         {"gameID": 1234, "playerList": ["boaty555Fifteen","nellyTheEllyFif","MrElephantFifte","ElmaTheElephant"]},
//                         {"gameID": 4321, "playerList": ["AI1","AI2","MrElephant"]},
//                         {"gameID": 2222, "playerList": ["AI1","AI2","Elmer"]}
//                         ];
       
       	myName = '<%=session.getAttribute("userName")%>';
       	game_info = JSON.parse('<%=request.getAttribute("gamesInfo")%>');
		webSocketAddress = '<%=WsBasePath + "LS"%>';
    </script>       
    <meta charset="UTF-8">
    <link rel="stylesheet" type="text/css" href="./lobby_table_style.css">
    <link rel="stylesheet" type="text/css" href="./lobby_style.css">
<!--     <script src="//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script> -->
<!--     <script src = "kinetic-v4.3.1.min.js"></script> -->
    <script src = "./lobby_script.js"></script>
    <script src = "./server.js"></script>
  </head>
	 
<body>
<header>
  <img src="./images/logo(white).svg" alt="Imperial Chaturaji Logo">
  <div>Imperial Chaturaji</div>
</header>
<section id="content">
  <section id="left">
  	<div class=left_panel>
<!--    		<img id="blue_elephant" src="./images/Blue_Elephant_Full.svg" alt="Blue Elephant Image">  -->
    	<div id="user">
    	</div>
    	<script> document.getElementById("user").innerHTML = myName; </script>
    </div>
    
<!--     <hr> -->
    
    <div class=left_panel>
    <div id ="selectedGame_static">
    	Selected game:
    </div>
    <div id="gameStatus"> 
    	Choose the game you wish to join from the Game Lobby
    </div>
    <div id = "joinGame">
    <form name="joinGame" action="GLS" method="POST" onSubmit="return checkJoinGame()">
	<input id="selectedGame" name="gameID"  type="hidden" value="">
	<input name="submitJoinGame" type="submit" value="Join Game">
    </form>  
    </div>
    </div>

<!-- 	<hr> -->

    <div class="left_panel">
    <form name="startGameForm">
	<div id="new_game">
	  Play with 
	  <select name="humans" onchange="setAIs(document.startGameForm.humans.options[document.startGameForm.humans.selectedIndex].value);">
	    <option value="0">0</option>
	    <option value="1">1</option>
	    <option value="2">2</option>
	    <option value="3">3</option>
	    <option value="4" selected>4</option>
	  </select>
	  human players and <div id="AIs">0</div> AIs.
	  </div>
	  <input type="submit" name="submitStartGame" value="Create Game">
<!-- 	  <input type="submit" name="submitStartGame" value="Start Game"> -->
	
		       <!--onclick="alert('Humans: ' +
			  document.startGameForm.humans.options[document.startGameForm.humans.selectedIndex].value
			  + '\nAIs: ' + document.getElementById('AIs').innerHTML);"-->
	</form>
	</div>

  </section>
  
  <section id="right"> 

<!--     <div id="gameLobby"> -->
<!--       Game Lobby -->
<!--     </div> -->

    <div align="center">
    <div id="tableBorder">
      <div id="gameTable" class="tableContainer">
<!-- 		<table border="0" cellpadding="0" cellspacing="0" width="70%" class="scrollTable"> -->
		<table class="scrollTable">
	  		<thead class="fixedHeader">
	    		<tr>
	      			<th><a href="#">Game Number</a></th>
	      			<th><a href="#">Players</a></th>
	      			<th><a href="#">AIs</a></th>
	      			<th><a href="#">Player IDs</a></th>
	    		</tr>
	  		</thead>
			<tbody class="scrollContent">
            </tbody>
	  	</table>
      </div>
      </div>
      <script> getGameInfo(); </script>
      <!--<script>
	  alert(document.getElementById("rowID:4547").childNodes[0].firstChild.textContent) </script>-->
    </div>	 
    
  </section>
</section>
</body>

</html>



