function setAIs(humans) {
    var AI_no = 4 - humans;
    document.getElementById('AIs').innerHTML = AI_no;
    
}

function loadTable(gameID, player_no, ai_no, users) {
    //alert("22loading table with " + gameID + "  " + player_no + "  " + ai_no + "  " + users);
    tabBody=document.getElementsByTagName("TBODY").item(0);
    row=document.createElement("TR");
    row.id = "rowID:" + gameID;
    row.onclick=function() { document.getElementById("selectedGame").value = gameID;
			     document.getElementById("gameStatus").innerHTML = gameID;
			   };
    cell1=document.createElement("TD");
    cell2=document.createElement("TD");
    cell3=document.createElement("TD");
    cell4=document.createElement("TD");
    textnode1=document.createTextNode(gameID);
    textnode2=document.createTextNode(player_no);
    textnode3=document.createTextNode(ai_no);
    textnode4=document.createTextNode(users.join(", "));
    cell1.appendChild(textnode1);
    cell2.appendChild(textnode2);
    cell3.appendChild(textnode3);
    cell4.appendChild(textnode4);
    row.appendChild(cell1);
    row.appendChild(cell2);
    row.appendChild(cell3);
    row.appendChild(cell4);
    tabBody.appendChild(row);
}
