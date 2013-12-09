/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lobbyServer;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import storage.DBHandler;

/**
 *
 * @author julia
 */
public class GameListHandler {
    
    public static void showGamesInfo(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{
        
        // put list of games in request object as "gamesInfo"
        String gameList = DBHandler.gameJSON().toString();
        request.setAttribute("gamesInfo", gameList);
        
        System.out.println("GameListHandler::game list is :: " + gameList);
        
        // set fresh rate of the lobby page
        //response.setHeader("Refresh", "3");
        
        // forward request
        request.getRequestDispatcher("lobby.jsp").forward(request, response);
        
    }
}
