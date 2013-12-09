/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package validationMessages;

import gameServer.ChaturajiServlet;
import gameServer.MSG_PRIO;

import java.util.*;

import rules.PLAYER;
//import storage.DBHandler;
import net.sf.json.*;

/**
 *
 * @author julia
 */
public class EndGame{

    private final static int type = 6;
    
    // generate validMove message and send to all players in the game
    public static void buff(String gameID, Map<PLAYER, Integer> score){
        
        // generate JSONObject
        JSONObject JSONmsg = new JSONObject();

        // generatate JSONObject for data
        JSONObject JSONdata = new JSONObject();

        // generate JSONArray for the player list
        JSONArray JSONplayers = new JSONArray();
        
        // get players with highest score
        Set<PLAYER> highestScorePlayers = getHighestScorePlayers(score);
        
        // outcome of the game : win or draw
        String outcome = (highestScorePlayers.size() == 1) ? "win" : "draw";
                
        // construct data with the players(ENUM value + 1) with highest score
        for(PLAYER p : highestScorePlayers){
            JSONplayers.add(p.ordinal()+1);
        }
        
        // construct data
        JSONdata.put("outcome", outcome);
        JSONdata.put("player", JSONplayers.toString());
               
        // construct message
        JSONmsg.put("type", type);
        JSONmsg.put("data", JSONdata.toString());
        
        if(ChaturajiServlet.getBuff().get(gameID) != null){
            ChaturajiServlet.getBuff().get(gameID).put(MSG_PRIO.EndGame, JSONmsg.toString());
        }else{
            System.out.println("message buffer for GAME " + gameID + " is null");
        }
        
         
    }
    
    private static Set<PLAYER> getHighestScorePlayers(Map<PLAYER, Integer> score){
        
        Set<PLAYER> highestScorePlayers = new HashSet<PLAYER>();
        int highestScore = getHighestScore(score);
        highestScorePlayers.clear();
        
        for(Map.Entry<PLAYER, Integer> entry : score.entrySet()){
            if(entry.getValue() == highestScore){
                System.out.println("EndGame:: adding " + entry.getKey().toString() + " with score " + entry.getValue() + " to list");
                highestScorePlayers.add(entry.getKey());
            }
                
        }
        
        return highestScorePlayers;
    }
    
    private static int getHighestScore(Map<PLAYER, Integer> score){
        
        int highestScore = 0;
        for(Map.Entry<PLAYER, Integer> entry : score.entrySet()){
            if(highestScore == 0 || entry.getValue() > highestScore){
                highestScore = entry.getValue();
            }
        }
        
        System.out.println("EndGame:: highest score is " + highestScore);
        return highestScore;
    }
       
}
