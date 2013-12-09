/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package validationMessages;

import gameServer.ChaturajiServlet;
import gameServer.MSG_PRIO;
import net.sf.json.JSONObject;

import java.util.Map;

import rules.PLAYER;

/**
 *
 * @author julia
 */
public class UpdateScore{
  
    private final static int type = 4;
    
    // generate validMove message and send to all players in the game
    public static void buff(String gameID, Map<PLAYER, Integer> score){
        
        // generate JSONObject
        JSONObject JSONmsg = new JSONObject();
    
        // generatate JSONObject for data
        JSONObject JSONdata = new JSONObject();
    
        // construct data
        JSONdata.put("p1", score.get(PLAYER.P1));
        JSONdata.put("p2", score.get(PLAYER.P2));
        JSONdata.put("p3", score.get(PLAYER.P3));
        JSONdata.put("p4", score.get(PLAYER.P4));
               
        // construct message
        JSONmsg.put("type", type);
        JSONmsg.put("data", JSONdata.toString());
        
        if(ChaturajiServlet.getBuff().get(gameID) != null){
            //System.out.println("buffering update score msg");
            ChaturajiServlet.getBuff().get(gameID).put(MSG_PRIO.UpdateScore, JSONmsg.toString());
        }else{
            System.out.println("message buffer for GAME " + gameID + " is null");
        }
        
    }
        
}
