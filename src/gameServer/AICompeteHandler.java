package gameServer;

import java.net.URI;
//import java.net.URISyntaxException;
//import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;

import rules.ChessBoard;
import rules.Move;
import rules.PLAYER;
import storage.DBHandler;
import adminMessages.NewPlayerJoined;
import net.sf.json.*;


public class AICompeteHandler implements WebSocket.OnTextMessage {
    
    private Connection connection;
    private static WebSocketClientFactory factory;
	
    
    // server2 (master server) url
    private final String server2URL = "ws://batch1.doc.ic.ac.uk:8080/Chaturaji/WSHandler?type=server&ais=2";
    
    // types of messages
    private final int VALID_MOVE = 2;
    private final int START_GAME = 10;
    
    // chessboard object for this game
    private ChessBoard cb = null;
    
    // gameID
    private String gameID;
    
    // list of AI identifiers
    private HashMap<PLAYER, String> AIIDs = new HashMap<PLAYER, String>();
    
    
    public AICompeteHandler(String gID) {

        this.gameID = gID;

        // create board object
        cb = new ChessBoard(gID, true);

        factory = new WebSocketClientFactory();
        try {
                factory.start();
                WebSocketClient client = factory.newWebSocketClient();
                client.open(new URI(server2URL), this);
        } 
        catch (Exception e) {
                e.printStackTrace();
        }
    }

    @Override
    public void onClose(int arg0, String arg1) {
            // Nothing to do
        System.out.println("ws to server 2 closed");
    }

    @Override
    public void onOpen(Connection arg0) {
            connection = arg0;
            System.out.println("ws to server 2 open");
    }

    @Override
    public void onMessage(String msg) {
        System.out.println( "====AICH2::got message: " + msg ); 
        handlerServerMessage(msg);
    }
        
        
    // deal with messages received from master server
    public void handlerServerMessage(String msg){
        // convert message string to JSON object
        JSONObject JSONmsg = (JSONObject) JSONSerializer.toJSON(msg);
        JSONObject JSONdata = (JSONObject) JSONSerializer.toJSON(JSONmsg.get("data"));

        // get message type
        int msgType = JSONmsg.getInt("type");
        
        switch(msgType){
            
            case VALID_MOVE:
                // get from and to squares in message data
                String fromS = (String) JSONdata.get("from_sq");
                String toS = (String) JSONdata.get("to_sq");
                
                Move move = new Move(fromS, toS);
                
                if(!DBHandler.getMoveList(gameID).contains(fromS+toS)){ // ensure this is a move made by the other server
                    
                    // update board with the move made by an AI
                    if (cb.submitMove(move, false, true)) { // not virtual, send message

                        // send all buffered broadcasting messages to client in correct order
                        ChaturajiServlet.getBuff().get(gameID).send();

                        // make AI move, assume AIs from slave server is named S1AI1, S1AI2
                        makeAIMove();                   

                    }
                }
                
                break;
                
            case START_GAME:
                
                if(AIIDs.isEmpty()){
                    
                    // put ordered AI players into map
                    AIIDs.put(PLAYER.P1, (String)JSONdata.get("p1"));
                    AIIDs.put(PLAYER.P2, (String)JSONdata.get("p2"));
                    AIIDs.put(PLAYER.P3, "S2");//(String)JSONdata.get("p3"));
                    AIIDs.put(PLAYER.P4, "S2");//(String)JSONdata.get("p4"));

                    // send player-joint messages to client
                    for(Map.Entry<PLAYER, String> entry: AIIDs.entrySet())
                        NewPlayerJoined.send(gameID, entry.getValue());

                    // generate JSONObject
                    JSONObject JSONmsg2 = new JSONObject();

                    // generatate JSONObject for data
                    JSONObject JSONdata2 = new JSONObject();

                    // construct data
                    JSONdata2.put("p1", AIIDs.get(PLAYER.P1));
                    JSONdata2.put("p2", AIIDs.get(PLAYER.P2));
                    JSONdata2.put("p3", AIIDs.get(PLAYER.P3));
                    JSONdata2.put("p4", AIIDs.get(PLAYER.P4));

                    // construct message
                    JSONmsg2.put("type", 10); // 1 for start game message
                    JSONmsg2.put("data", JSONdata2.toString());

                    // forward start game message to client
                    ChaturajiServlet.broadcast(gameID, JSONmsg2.toString());

                    // make AI move, assume AIs from slave server is named S1AI1, S1AI2
                    makeAIMove();
                }
                
                break;
                
            default:
                System.out.println("other message from server2:" + msg);
                //System.exit(1);
        }
        
    }
    
    // make slave server AI move and send move to master server and client observer
    protected void makeAIMove(){
       
        // check to see if the first/next player is AI of S1, if yes, make the move
        while (AIIDs.get(cb.currentPlayer()).substring(0, 2).equals("S1") && cb.AIMove()) {

            //System.out.println("AICH::current gameID is " + gameID + ", current AI ID is " + AIIDs.get(cb.currentPlayer()));

            // send AI move to master server
            sendS2_AIMove();
                
            // check if the end of game is reached
            if (!ChaturajiServlet.getBuff().get(gameID).contains(MSG_PRIO.EndGame)) {                
                
                // not end of game, send messages
                ChaturajiServlet.getBuff().get(gameID).send();

            } else {
                
                // end of game, send messages and break loop
                ChaturajiServlet.getBuff().get(gameID).send();

                break;
            }
        }
    }
    
    // send AI move to master server
    private void sendS2_AIMove(){
        
        // get AI move
        Move m = ChaturajiServlet.getBuff().get(gameID).getValidMove();

        // generate JSONObject
        JSONObject JSONmsg = new JSONObject();

        // generatate JSONObject for data
        JSONObject JSONdata = new JSONObject();

        // construct data
        JSONdata.put("from_sq", m.getFromSq().toString());
        JSONdata.put("to_sq", m.getToSq().toString());

        // construct message
        JSONmsg.put("type", 1); // 1 for submit move message
        JSONmsg.put("data", JSONdata.toString());

        System.out.println("====AICH::sending message to master server: " + JSONmsg.toString());
        
        try{
            // submit move to master server
            connection.sendMessage(JSONmsg.toString());
            
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
