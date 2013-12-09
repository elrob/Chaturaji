/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package adminMessages;

import gameServer.ChaturajiServlet;

        

import net.sf.json.*;

/**
 *
 * @author julia
 */
public class NewPlayerJoined {
    
    private final static int type = 9;
    
    // generate JSONObject
    private static JSONObject JSONmsg = new JSONObject();
    
    // generatate JSONObject for data
    private static JSONObject JSONdata = new JSONObject();
    
    public static void send(String gameID, String clientID){
        
        // construct data
        JSONdata.put("name", clientID);
        
        // construct message
        JSONmsg.put("type", type);
        JSONmsg.put("data", JSONdata.toString());
                
        System.out.println("NewPlayerJoined:: sending new player joined:" + JSONmsg.toString());
        ChaturajiServlet.broadcast(gameID, JSONmsg.toString());
    }
}
