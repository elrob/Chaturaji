var SOCKET;
initWS(); // WEBSOCKET CONNECTION ESTABLISHED HERE

function getMyName() {
        //var myName = "zerocaleb";
	return myName;
};   

function getGameInfo() {
    
    var parent = document.getElementsByTagName("TBODY").item(0);
    while(parent.firstChild){
        parent.removeChild(parent.firstChild);
    }
    
    for(var game_no = 0; game_no < game_info.length; game_no++) {
    	gameId = game_info[game_no].gameID;
    	var namesArray = game_info[game_no].playerList;
    	var number_ai = 0;
    	var number_player = namesArray.length;
        var playerCount = 0;
        for(var j = 0; j < number_player; j++) {
        	if(namesArray[j] != null){
                playerCount++;
                if(namesArray[j][0] == 'A' && namesArray[j][1] == 'I')
                    number_ai++;
            }
        }
        if( playerCount == 4) {
        	playerCount = "FULL";
        }
        else {
        	playerCount = playerCount + "/4";
        }
        //alert("loading table with " + gameId + "  " + playerCount + "  " + number_ai + "  " + namesArray);
        loadTable(gameId, playerCount, number_ai, namesArray);
    }
    return;
};


  
function checkJoinGame() {
    var selected = document.getElementById("selectedGame").value;
    //alert("selected is " + selected);
    //alert("status is " + document.getElementById("rowID:" + selected).childNodes[1].firstChild.textContent == "FULL");

    if(selected == "") {
        document.getElementById("gameStatus").innerHTML = "Game not selected!";
        document.getElementById("selectedGame").value = "";
        return false;
    }else if(document.getElementById("rowID:" + selected).childNodes[1].firstChild.textContent == "FULL") {
        document.getElementById("gameStatus").innerHTML = "Game is Full!";
        document.getElementById("selectedGame").value = "";
        return false;
    }else
        return true;
}


function initWS() {
    try {
        SOCKET = new WebSocket(webSocketAddress);
    } catch (e) {
        alert("Sorry, your browser does not support HTML5 and/or WebSockets!");
    }
    SOCKET.onopen = function () {
        //alert("WebSocket connection established!");
    };
    SOCKET.onclose = function () {
        //alert("WebSocket connection broken!");
    };
    SOCKET.onmessage = function (msg) {
        //alert(msg.data);
        game_info = JSON.parse(msg.data);
        getGameInfo();
    };
}