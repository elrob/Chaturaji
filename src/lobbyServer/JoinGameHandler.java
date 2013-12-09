/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lobbyServer;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import javax.servlet.ServletException;

import storage.DBHandler;
import adminMessages.NewPlayerJoined;
import net.sf.json.*;

/**
 *
 * @author julia
 */
public class JoinGameHandler {
    
    protected static void joinGame(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{
        
        String gameIDjoined = request.getParameter("gameID");
        System.out.println("gameID is " + gameIDjoined);
        String clientID = (String) request.getSession().getAttribute("userName");
        JSONArray players = new JSONArray();
        JSONObject gameInfo = new JSONObject();
          
        // WS: send message to other players in the same game: new player joined
        NewPlayerJoined.send(gameIDjoined, clientID);
        
        // update GAME table
        DBHandler.addNewPlayer(gameIDjoined, clientID);
              
        System.out.println("JoinGameHandler:: player " + clientID + " joined Game " + gameIDjoined);

        // put players in a json array
        System.out.println("JoinGameHandler:: getting player list");
        for(String s : DBHandler.getPlayerList(gameIDjoined)){
            players.add(s);
        }
        // generate JSON object for game information: gameID + player list
        gameInfo.put("gameID", gameIDjoined);
        gameInfo.put("userNames", players);
        
        // put game information in request object as "gameInfo"
        request.setAttribute("gameInfo", gameInfo.toString());
        // forward request
        request.getRequestDispatcher("chaturajiTheGame.jsp").forward(request, response);
        
        // lobby WS: send messages to all clients to update game list
        LobbyWSServlet.updateClients();
    }
}
