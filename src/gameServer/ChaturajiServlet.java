package gameServer;

import java.io.IOException;  
import java.nio.ByteBuffer;  
import java.nio.CharBuffer;  
//import java.text.SimpleDateFormat;  
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;  

import org.apache.catalina.websocket.WebSocketServlet;
import org.apache.catalina.websocket.WsOutbound;
import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.MessageInbound;
//import rules.ChessBoard;
import storage.DBHandler;
//import net.sf.json.*;
  

public class ChaturajiServlet extends WebSocketServlet {  
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// stores all clients that are connected to this server using websocket
    // clientID - websocket outbount pair
    protected static final Map<String, WsOutbound> clientMap = new HashMap<String, WsOutbound>();
    
    // stores message buffers for the games (one message buffer for each game)
    // message buffers only store broadcasting messages (i.e. InvalidMove not included)
    // this is for dealing with message sending order when a client submits a move
    // in other words, a message buffer for a certain game only stores the messages generated in the process of validating one move 
    protected static final Map<String, MessageBuffer> gamesMsgBuff = new HashMap<String, MessageBuffer>();
    
    // logic handler objects for each game
    protected static final Map<String, GameLogicHandler> handlerMap = new HashMap<String, GameLogicHandler>();
    
    // getter for gamesMsgBuff: used by ValidationMessages
    public static Map<String, MessageBuffer> getBuff(){
        
        return gamesMsgBuff;
        
    }
    
    
    // getter for handlerMap
    public static Map<String, GameLogicHandler> getHandlerMap(){
        
        return handlerMap;
        
    }
    
    /** 
     *  
     */  
      
    @Override  
    protected StreamInbound createWebSocketInbound(String arg0, HttpServletRequest request) {  
        return new ChaturajiMessageInbound();  
    }  
  
    class ChaturajiMessageInbound extends MessageInbound {  
  
        @Override  
        protected void onOpen(WsOutbound outbound) {   
            System.out.println("ws open");
            super.onOpen(outbound);  
        }  
  
        @Override  
        protected void onClose(int status) {  
            System.out.println("ws close");
            //String gameID = DBHandler.gameIn(getClientID(getWsOutbound()));
            //System.out.println("GAME " + gameID + " handler is " + handlerMap.get(gameID));
            new WSCloseHandler(getWsOutbound()).start();
            
            super.onClose(status);  
        }  
  
        @Override  
        protected void onBinaryMessage(ByteBuffer buffer) throws IOException {  
            // TODO Auto-generated method stub  
  
        }  
  
        @Override  
        protected void onTextMessage(CharBuffer buffer) throws IOException {  
            String msg = buffer.toString();  
            
            //-----------original code for testing-----------//
            //Date date = new Date();
            
            //SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");  
            //msg = " <font color=green>anonymous  " + sdf.format(date) + "</font><br/> " + msg;
            //------------end of original code--------------//
            System.out.println("on text message: " + msg);
            

            new ClientMessageHandler(msg, getWsOutbound()).start();
            
            //clientMap.put(clientID, this.getWsOutbound());
            //System.out.println("size of map is now " + clientMap.size());
            
            //broadcast(msg); 
            //replyOne(clientID, msg);
        }  
  
    }  
    
    
    public static void replyOne(String clientID, String msg){
        CharBuffer buff = CharBuffer.wrap(msg);
        WsOutbound outbound = clientMap.get(clientID);  
    
        try {  
        
            System.out.println("sending msg to " + clientID + ": " + msg);
            
            outbound.writeTextMessage(buff);  
            
            outbound.flush();  
            
        } catch (IOException e) {  
        
            e.printStackTrace();  
            
        }  
    
    }
        
      
    
    public static void broadcast(String gameID, String msg) {
            
        Vector<String> set = DBHandler.getPlayerList(gameID);
        System.out.println("ChaturajiServlet::broadcast: player list is " + set.toString());
            
        Iterator<String> it = set.iterator();
        
        while(it.hasNext()) {  
            
            String s = (String) it.next();
            
            if(!s.equals("null") && !s.substring(0, 2).equals("AI")){
                
                WsOutbound outbound = clientMap.get(s);  

                CharBuffer buffer = CharBuffer.wrap(msg);  

                try {  
                    
                    System.out.println("sending msg to " + s + ": " + msg);

                    outbound.writeTextMessage(buffer);  

                    outbound.flush();  

                } catch (IOException e) {  

                    e.printStackTrace();  

                }  
            }
           
        }  
    }  
    
    // get client ID using outbound object
    protected static String getClientID(WsOutbound out) {

        Set<String> set = clientMap.keySet();
        for (String s : set) {
            WsOutbound outbound = clientMap.get(s);
            if (outbound.equals(out)) {
                return s;
            }
        }
        return "";
    }
}  