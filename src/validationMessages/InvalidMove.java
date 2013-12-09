/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package validationMessages;

import gameServer.ChaturajiServlet;
import net.sf.json.JSONObject;

/**
 *
 * @author julia
 */
public class InvalidMove{
    

    private final static int type = 3;
    
    
    
    // generate validMove message and send to all players in the game
    public static void send(String clientID){
         
        // generate JSONObject
        JSONObject JSONmsg = new JSONObject();
    
        // generatate JSONObject for data
        JSONObject JSONdata = new JSONObject();
    
        // construct message
        JSONmsg.put("type", type);
        JSONmsg.put("data", JSONdata.toString());
        
        ChaturajiServlet.replyOne(clientID, JSONmsg.toString());
    
    }
       
}
