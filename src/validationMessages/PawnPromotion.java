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
public class PawnPromotion{
    

    private final static int type = 8;
    
    // generate validMove message and send to all players in the game
    public static void buff(String gameID, String sq, String pieceType){
        
        // generate JSONObject
        JSONObject JSONmsg = new JSONObject();

        // generatate JSONObject for data
        JSONObject JSONdata = new JSONObject();
        
        // construct data
        JSONdata.put("sq", sq);
        JSONdata.put("pieceType", pieceType);
        
        // construct message
        JSONmsg.put("type", type);
        JSONmsg.put("data", JSONdata.toString());
        
        if(ChaturajiServlet.getBuff().get(gameID) != null){
            ChaturajiServlet.getBuff().get(gameID).put(MSG_PRIO.PawnPromotion, JSONmsg.toString()); 
        }else{
            System.out.println("message buffer for GAME " + gameID + " is null");
        }
        
    }
       
}
