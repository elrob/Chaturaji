/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gameServer;

import java.util.TreeMap;

import rules.Move;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

/**
 * Message buffer class to buffer multiple messages BROADCATSED from server (one buffer for each game)
 * message priorities:
 * level 6: ValidMove
 * level 5: BoatTriumph
 * level 4: PawnPromotion
 * level 3: UpdateScore
 * level 2: PlayerOut
 * level 1: SuddenDeath
 * level 0: EndGame
 * 
 * @author julia
 */
public class MessageBuffer {
    
    // gameID associated with this buffer
    private String gameID;
    
    
    // Map to store messages with priorities
    private TreeMap<MSG_PRIO, String> msgBuff = new TreeMap<MSG_PRIO, String>();
    
    
    // constructor
    public MessageBuffer(String b_gameID){
        
        this.gameID = b_gameID;
    }
    
    
    // put new message into this buffer
    public void put(MSG_PRIO prio, String msg){
        
        msgBuff.put(prio, msg);
        System.out.println("buffering message: prio = " + prio + ", msg = " + msg);
    }
    
    // broadcast all messages with reference to their priorities
    public void send(){
        
        while(!msgBuff.isEmpty()){
            
            // send the highest priority message
            ChaturajiServlet.broadcast(gameID, msgBuff.lastEntry().getValue());
            // delete it from buffer
            msgBuff.remove(msgBuff.lastEntry().getKey());
            
        }
    }
    
    // check if a certain kind of message is in the buffer
    public boolean contains(MSG_PRIO msgType){
        
        System.out.println("buffer contains " + msgType.toString() + " is " + msgBuff.containsKey(msgType));
        return msgBuff.containsKey(msgType);
    }
    
    // get valid move message, used by the AICompeteHandler
    public Move getValidMove(){
        
        // convert message string to JSON object
        JSONObject JSONmsg = (JSONObject) JSONSerializer.toJSON(msgBuff.get(MSG_PRIO.ValidMove));
        JSONObject JSONdata = (JSONObject) JSONSerializer.toJSON(JSONmsg.get("data"));
        
        Move m = new Move((String)JSONdata.get("from_sq"), (String)JSONdata.get("to_sq"));
        
        System.out.println("msgBuffer:: got move from " + m.getFromSq() + " to " + m.getToSq());
        return m;
    }
}
