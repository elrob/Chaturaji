/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gameServer;

import java.util.Iterator;
import java.util.Vector;

import lobbyServer.LobbyWSServlet;

import org.apache.catalina.websocket.WsOutbound;

import rules.PLAYER;
import storage.DBHandler;
import adminMessages.PlayerQuit;

/**
 *
 * @author julia
 */
public class WSCloseHandler extends Thread{
    
    private WsOutbound out;
    
    public WSCloseHandler(WsOutbound out){
        this.out = out;
    }
    
    public void run(){
        handleWSClose(out);
    }
    
    public void handleWSClose(WsOutbound out){
        
        String clientID = ChaturajiServlet.getClientID(out);
        String gameID = DBHandler.gameIn(clientID);
        System.out.println("handleWSClose: client " + clientID + " is in GAME " + gameID);
        
        // remove client's websocket
        ChaturajiServlet.clientMap.values().remove(out);    
        System.out.println(clientID + "\'s websocket deleted!");
        
        if(!gameID.equals("0")){ // ensure the game is in the database in case a websocket closes after game ends
            
            int ais = 0; // count the number of AIs in that game
            int nulls = 0;

            // set database record for that player to AI
            //System.out.println("ClientMessageHandler2:: getting player list");
            Vector<String> set = DBHandler.getPlayerList(gameID);

            // turn of the player that has quit
            int turn = set.indexOf(clientID);

            // set dataabase record for that player to be null
            DBHandler.setNull(clientID);

            Iterator<String> it = DBHandler.getPlayerList(gameID).iterator();

            // get the number of AIs in the game
            while(it.hasNext()) { 
                String s = (String) it.next();
                //System.out.println("s is " + s);
                if(s.substring(0, 2).equals("AI")){
                    ais++;
                }
                if(s.equals("null")){
                    nulls++;
                }
            }
            //System.out.println("ais is " + ais + " nulls is " +nulls);
            if(ais+nulls <= 3){ // ensure there were at least two human players in the game

                DBHandler.addNewPlayer(gameID, "AI"+(ais+1));

                System.out.println("now game " + gameID + " has players " + DBHandler.getPlayerList(gameID));

                // send player quit messsage to other players
                PlayerQuit.send(gameID, PLAYER.values()[turn], "AI"+(ais+1));

                if(ChaturajiServlet.getHandlerMap().get(gameID)!= null){
                    // let AI move
                    ChaturajiServlet.getHandlerMap().get(gameID).makeAIMove(gameID);
                }
                
            }else{

                // no players in the game anymore, clear storage on the game
                // delete message buffer and logic handler for the game            
                ChaturajiServlet.getBuff().remove(gameID);
                ChaturajiServlet.getHandlerMap().remove(gameID);
                System.out.println("handlerWSClose:message buff and logic handler for GAME " + gameID + " deleted!");

                // delete the game entry in database
                DBHandler.deleteGame(gameID);
                
                // lobby WS: send messages to all clients to update game list
                LobbyWSServlet.updateClients();
            }
        }
    }
}
