/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package validationMessages;

import gameServer.ChaturajiServlet;
import gameServer.MSG_PRIO;
import net.sf.json.JSONObject;

/**
 *
 * @author julia
 */
public class SuddenDeath{
    

    private final static int type = 7;
    
    // moves limit when only two players are on board
    private final static int limit = 10;
    
    // generate validMove message and send to all players in the game
    public static void buff(String gameID){
        // generate JSONObject
        JSONObject JSONmsg = new JSONObject();
    
        // generatate JSONObject for data
        JSONObject JSONdata = new JSONObject();
        
        // construct data
        JSONdata.put("moveLimit", limit);
               
        // construct message
        JSONmsg.put("type", type);
        JSONmsg.put("data", JSONdata.toString());
        
        if(ChaturajiServlet.getBuff().get(gameID) != null){
            ChaturajiServlet.getBuff().get(gameID).put(MSG_PRIO.SuddenDeath, JSONmsg.toString());
        }else{
            System.out.println("message buffer for GAME " + gameID + " is null");
        }
        
    
    }
       
}
