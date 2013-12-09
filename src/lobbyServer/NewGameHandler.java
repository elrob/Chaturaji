/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lobbyServer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import javax.servlet.ServletException;

//import rules.ChessBoard;
import storage.DBHandler;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author julia
 */
public class NewGameHandler {
    
    protected static void newGame(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{
        
        String newGameID = DBHandler.nextGameID();
        System.out.println("New game ID is "+ newGameID);
        String clientID = (String) request.getSession().getAttribute("userName");
        int NoAI = 4 - Integer.parseInt(request.getParameter("humans")); // number of AIs needed
        JSONArray players = new JSONArray();
        JSONObject gameInfo = new JSONObject();
        
        assert( (NoAI <= 4) && (NoAI >= 0) ) : "Number of AIs not valid!";
        System.out.println("NewGameHandler::no AI is " + NoAI);
        
        // check how many AIs are needed
        
        if(NoAI <= 3){ // <= 3 AIs needed (at least one human player)
            
            // add entry in the GAME table
            DBHandler.addNewGameEntry(newGameID, clientID);
            
            for(int i = 1; i <= NoAI; i++){
                
                DBHandler.addNewPlayer(newGameID, "AI."+i);
            
            }
            
            /********
            // create game board and associate it with the gameID by storing the gameID - board pair in gameMap
            GameLobbyServlet.getGameLobbyMap().put(newGameID, new ChessBoard(newGameID, true));             
            System.out.println("adding game: " + GameLobbyServlet.getGameLobbyMap().get(newGameID));
            ********/
            
                    
            // put players in a json array
            //System.out.println("NewGameHandler:: getting player list");
            for(String s : DBHandler.getPlayerList(newGameID)){
                players.add(s);
            }

            
        }else{ // 4 AI game
            
            DBHandler.addNewGameEntry(newGameID, clientID);
            DBHandler.addNewPlayer(newGameID, "AI");
            DBHandler.addNewPlayer(newGameID, "AI");
            DBHandler.addNewPlayer(newGameID, "AI");
            //System.out.println("4AI");
            
            // set player list to all nulls
            for(int i = 0; i < 4; i++)
                players.add("null");
            
            /********
            // create game board and associate it with the gameID by storing the gameID - board pair in gameMap
            GameLobbyServlet.getGameLobbyMap().put(newGameID, new ChessBoard(newGameID, true));
            System.out.println("adding 4 AI game:" + GameLobbyServlet.getGameLobbyMap().get(newGameID));
            ********/
            
        }
        
        // generate JSON object for game information: gameID + player list
        gameInfo.put("gameID", Integer.parseInt(newGameID));
        gameInfo.put("userNames", players);

        // put game information in request object as "gameInfo"
        request.setAttribute("gameInfo", gameInfo.toString());
        System.out.println("passing gameInfo to chaturajiTheGame.jsp: " + gameInfo.toString());

        // forward request
        request.getRequestDispatcher("chaturajiTheGame.jsp").forward(request, response);
        
        // lobby WS: send messages to all clients to update game list
        LobbyWSServlet.updateClients();
    }
    
}
