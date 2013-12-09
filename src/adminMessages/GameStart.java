/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package adminMessages;

import gameServer.ChaturajiServlet;
import net.sf.json.*;
import rules.PLAYER;
import storage.DBHandler;
        
/**
 *
 * @author julia
 */
public class GameStart {
    
    private final static int type = 10;
    
    // generate JSONObject
    private static JSONObject JSONmsg = new JSONObject();
    
    // generatate JSONObject for data
    private static JSONObject JSONdata = new JSONObject();
    
    
    public static void send(String gameID){
        
        // construct data
        JSONdata.put("p1", DBHandler.getPlayerID(gameID,PLAYER.P1));
        JSONdata.put("p2", DBHandler.getPlayerID(gameID,PLAYER.P2));
        JSONdata.put("p3", DBHandler.getPlayerID(gameID,PLAYER.P3));
        JSONdata.put("p4", DBHandler.getPlayerID(gameID,PLAYER.P4));
        
        // construct message
        JSONmsg.put("type", type);
        JSONmsg.put("data", JSONdata.toString());
        
        System.out.println("game start: " + JSONmsg.toString());
        ChaturajiServlet.broadcast(gameID, JSONmsg.toString());
    }
}
