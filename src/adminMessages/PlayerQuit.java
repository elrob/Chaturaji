/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package adminMessages;

import gameServer.ChaturajiServlet;

        

import net.sf.json.*;
import rules.PLAYER;

/**
 *
 * @author julia
 */
public class PlayerQuit {
    
    private final static int type = 13;
    
    // generate JSONObject
    private static JSONObject JSONmsg = new JSONObject();
    
    // generatate JSONObject for data
    private static JSONObject JSONdata = new JSONObject();
    
    public static void send(String gameID, PLAYER p, String newID){
        
        // construct data
        JSONdata.put("player", p.ordinal()+1);
        JSONdata.put("newID", newID);
        
        // construct message
        JSONmsg.put("type", type);
        JSONmsg.put("data", JSONdata.toString());
        
        ChaturajiServlet.broadcast(gameID, JSONmsg.toString());
    }
}
