/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lobbyServer;

import java.io.IOException;
//import java.io.PrintWriter;
//import java.util.HashMap;
//import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
//import javax.swing.*;

//import org.apache.catalina.websocket.WsOutbound;

//import rules.ChessBoard;
import storage.DBHandler;

/**
 *
 * @author julia
 */
public class GameLobbyServlet extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	/********
    // stores all games on the server
    // gameID -- ChessBoard object pair
    protected static final Map<String, ChessBoard> gameLobbyMap = new HashMap<String, ChessBoard>();
    
    
    // getter for gameLobbyMap
    public static Map<String, ChessBoard> getGameLobbyMap(){
        return gameLobbyMap;
    }
    ********/
    
    public void init(){
        new DBHandler();
    }
    

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
    	
        HttpSession session = request.getSession();
        //System.out.println("session id = " + session.getId());// + ", session = " + session.toString());
        if(session.isNew()){
             System.out.println("new session");
            // check login info:
            // if invalid info, invalidate session and redirect back to login page (page1)
            // if valid info, redirect to page2
        
            String userName = request.getParameter("userName");
            System.out.println(userName);
            // String password = request.getParameter("password");

            
            if(userName != null && DBHandler.validateUser(userName)){//, password)){ // valid login info

                session.setAttribute("userName", userName);
                GameListHandler.showGamesInfo(request, response);
                // ||
                // \/
                // redirect to page2
                // using the same servlet(GameLobbyServlet) in page2 as in page1
            }else{ // invalid login info
               
                //JOptionPane.showMessageDialog(null, "Sorry, incorrect password!");
                
                request.setAttribute("result", "username invalid or already in use.");//"username or password incorrect!");
                request.getRequestDispatcher("login.jsp").forward(request, response);
            } 
                
        }else{// already logged in
            System.out.println("old session");
            // request from page2:
            // 1. join an existing game (2a)
            // 2. create a new game（2b)
            if(request.getParameter("submitJoinGame") != null){
                
                //JOptionPane.showMessageDialog(null, "join game button");
                // deal with 1
                JoinGameHandler.joinGame(request, response);
                
            }else if(request.getParameter("submitStartGame") != null){
                
                //JOptionPane.showMessageDialog(null, "start new game button");
                // deal with 2
                NewGameHandler.newGame(request, response);
                
            }else if(request.getParameter("backToLobby") != null){
                // make sure this button can only be pressed after the game is over    
                
                GameListHandler.showGamesInfo(request, response);
            
            }else{ // just refreshing lobby page
                
                GameListHandler.showGamesInfo(request, response);
            }
            
        }
        
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	request.setCharacterEncoding("UTF-8");
        processRequest(request, response);
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	request.setCharacterEncoding("UTF-8");
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
    
    
    @Override
    public void destroy(){
        System.out.println("Destroying GameLobbyServlet");
        //DBHandler.dropTables();
        DBHandler.DBclose();
    }
}
