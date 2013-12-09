/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package validationMessages;

import gameServer.ChaturajiServlet;
import gameServer.MSG_PRIO;
import storage.DBHandler;
import net.sf.json.*;

/**
 *
 * @author julia
 */
public class ValidMove{
    
    private final static int type = 2;
    
    // generate validMove message and send to all players in the game
    public static void buff(String gameID, String fromSq, String toSq){
        
        // store this valid move in database
        DBHandler.updateMoveRecord(gameID, fromSq + toSq);
        //System.out.println("updateResult is " + DBHandler.getMoveList(gameID));
        //System.out.println("====storing move to database: fromSq=" + fromSq + ", toSq=" + toSq);
        
        // generate JSONObject
        JSONObject JSONmsg = new JSONObject();
    
        // generatate JSONObject for data
        JSONObject JSONdata = new JSONObject();
        
        // construct data
        JSONdata.put("from_sq", fromSq);
        JSONdata.put("to_sq", toSq);
               
        // construct message
        JSONmsg.put("type", type);
        JSONmsg.put("data", JSONdata.toString());
        
        if(ChaturajiServlet.getBuff().get(gameID) != null){
            ChaturajiServlet.getBuff().get(gameID).put(MSG_PRIO.ValidMove, JSONmsg.toString());
        }else{
            System.out.println("message buffer for GAME " + gameID + " is null");
        }
    }
}
