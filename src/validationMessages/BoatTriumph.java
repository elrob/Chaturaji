/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package validationMessages;

import gameServer.ChaturajiServlet;
import gameServer.MSG_PRIO;
import rules.ChessSquare;
import net.sf.json.*;

/**
 *
 * @author julia
 */
public class BoatTriumph{
    

    private final static int type = 11;
    
    // generate validMove message and send to all players in the game
    public static void buff(String gameID, ChessSquare[] taken_pieces_pos, String taking_piece_pos){
        
        // generate JSONObject
        JSONObject JSONmsg = new JSONObject();

        // generatate JSONObject for data
        JSONObject JSONdata = new JSONObject();

        // generate JSONArray for taken piece positions
        JSONArray JSONtakens = new JSONArray();
    
        // construct taken piece position array
        for(int i = 0; i < 3; i++){
            JSONtakens.add(taken_pieces_pos[i].toString());
        }
        
        // construct data
        JSONdata.put("tBoat", taking_piece_pos);
        JSONdata.put("boats", JSONtakens.toString());
               
        // construct message
        JSONmsg.put("type", type);
        JSONmsg.put("data", JSONdata.toString());
        
        if(ChaturajiServlet.getBuff().get(gameID) != null){
            ChaturajiServlet.getBuff().get(gameID).put(MSG_PRIO.BoatTriumph, JSONmsg.toString());
        }else{
            System.out.println("message buffer for GAME " + gameID + " is null");
        }
        
    
    }
   
}
