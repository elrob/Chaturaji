/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lobbyServer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
//import java.util.Date;
import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;
import org.apache.catalina.websocket.WsOutbound;

import storage.DBHandler;

/**
 *
 * @author julia
 */
public class LobbyWSServlet extends WebSocketServlet{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// stores all clients that are connected to this server using websocket
    // clientID - websocket outbount pair
    protected static final HashSet<WsOutbound> LobbyClientMap = new HashSet<WsOutbound>();
    
    @Override  
    protected StreamInbound createWebSocketInbound(String arg0, HttpServletRequest request) {  
        return new LobbyMessageInbound();  
    }  
  
    class LobbyMessageInbound extends MessageInbound {  
  
        @Override  
        protected void onOpen(WsOutbound outbound) {   
            System.out.println("Lobby ws open");
            LobbyClientMap.add(outbound);
            super.onOpen(outbound);  
        }  
  
        @Override  
        protected void onClose(int status) {  
            System.out.println("Lobby ws close");
            LobbyClientMap.remove(getWsOutbound());
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
            
            System.out.println("LobbyWSServlet::on text message: " + msg);
            
            //clientMap.put(clientID, this.getWsOutbound());
            //System.out.println("size of map is now " + clientMap.size());
            
            //broadcast(msg); 
            //replyOne(clientID, msg);
        }  
  
    }  
    
    public static void updateClients(){
        
        String gameList = DBHandler.gameJSON().toString();
        
        for(WsOutbound w:LobbyClientMap){
            
            CharBuffer buffer = CharBuffer.wrap(gameList);  

            try {  
                System.out.println("LobbyWSServlet::sending msg to clients: " + gameList);
                
                w.writeTextMessage(buffer);  
                w.flush();  
                
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }
    }
}
