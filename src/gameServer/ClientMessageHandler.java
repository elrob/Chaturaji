/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gameServer;

import org.apache.catalina.websocket.WsOutbound;

import rules.ChessBoard;
import rules.Move;
//import rules.PLAYER;
import storage.DBHandler;
import validationMessages.InvalidMove;
//import validationMessages.ValidMove;
import adminMessages.GameStart;


//import java.util.Vector;
//import java.util.Set;
//import java.nio.CharBuffer;

import net.sf.json.*;

//import java.util.Iterator;

//import lobbyServer.GameLobbyServlet;

/**
 *
 * @author julia
 */
public class ClientMessageHandler extends Thread{

    private final int JOIN = 14;
    private final int SUBMIT_MOVE = 1;
    //private static final int QUIT = 12;
    private WsOutbound out;
    private String msg;
    
    public ClientMessageHandler(String msg, WsOutbound out){
        this.out = out;
        this.msg = msg;
    }
    
    public void run(){
        handleClientMessage(msg, out);
    }

    // handle message sent by clients
    protected void handleClientMessage(String msg, WsOutbound out) {

        // convert message string to JSON object
        JSONObject JSONmsg = (JSONObject) JSONSerializer.toJSON(msg);
        JSONObject JSONdata = (JSONObject) JSONSerializer.toJSON(JSONmsg.get("data"));

        // get message type
        int msgType = JSONmsg.getInt("type");


        // string for clientID, sent in the first message JOIN
        String clientID;

        // string for gameID
        String gameID;

        // switch message type
        switch (msgType) {

            case JOIN:
                // first message received when the websocket is created
                // N.B. at this time, other players in the same game are already notified by JoinGameHandler
                clientID = (String) JSONdata.get("name");

                // put in map a clientID-outbound pair
                ChaturajiServlet.clientMap.put(clientID, out);

                gameID = DBHandler.gameIn(clientID);

                System.out.println(clientID + " joined Game " + gameID + ", out: " + out.toString());

                System.out.println("start game " + gameID + "? " + DBHandler.playerReady(gameID));

                // if this game does not already have a message buffer, create
                if (!ChaturajiServlet.getBuff().containsKey(gameID)) {

                    ChaturajiServlet.getBuff().put(gameID, new MessageBuffer(gameID));
                    System.out.println("message buff for GAME " + gameID + " added!");
                }
                 
                // if this game does not already have a game logic handler, create
                if(!ChaturajiServlet.getHandlerMap().containsKey(gameID)){
                    System.out.print("ChaturajiServelt::adding logicHandler for GAME " + gameID);
                    ChaturajiServlet.getHandlerMap().put(gameID, new GameLogicHandler());
                    System.out.println(", handler is " + ChaturajiServlet.getHandlerMap().get(gameID));
                }
            
                //System.out.println("ClientMessageHandler1:: getting player list");
                if(!DBHandler.getPlayerList(gameID).contains("AI")){ // player list does not contain "AI", not AI vs AI game
                    
                    // check if game should start
                    if (DBHandler.playerReady(gameID)) {

                        // WS: send message to all players: game starts
                        GameStart.send(gameID);

                        /**
                         * ******
                         * // get the game board the player is in ChessBoard boardIn
                         * = GameLobbyServlet.getGameLobbyMap().get(gameID);
                            *******
                         */
                        // move if it's AI's turn
                        if(ChaturajiServlet.getHandlerMap().get(gameID)!= null){
                            // let AI move
                            ChaturajiServlet.getHandlerMap().get(gameID).makeAIMove(gameID);
                        }
                    }
                    
                }else{ // player list contains "AI", AI vs AI case
                    
                    new AICompeteHandler(gameID);
                }

                break;

            case SUBMIT_MOVE:

                // get clientID from the outbound
                clientID = ChaturajiServlet.getClientID(out);

                // get from and to squares in message data
                String fromS = (String) JSONdata.get("from_sq");
                String toS = (String) JSONdata.get("to_sq");

                // ensure clientID is not empty
                if (clientID.equals("")) {
                    System.exit(1);
                }

                // get the gameID of the game the player is in
                gameID = DBHandler.gameIn(clientID);

                /**
                 * ******
                 * // get the game board the player is in ChessBoard boardIn =
                 * GameLobbyServlet.getGameLobbyMap().get(gameID);
                 * System.out.println("getting game:" + boardIn);
                    *******
                 */
                // get the game board the player is in
                ChessBoard boardIn = GameLogicHandler.createBoard(gameID);

                Move move = new Move(fromS, toS);
                if (boardIn.submitMove(move, false, true)) { // not virtual, send message

                    // send all buffered broadcasting messages to clients in correct order
                    ChaturajiServlet.getBuff().get(gameID).send();

                    // move if it's AI's turn
                    if(ChaturajiServlet.getHandlerMap().get(gameID)!= null){
                            // let AI move
                            ChaturajiServlet.getHandlerMap().get(gameID).makeAIMove(gameID);
                    }
                    
                } else {

                    // send message to client: invalid move
                    InvalidMove.send(clientID);
                }

                break;

            default:
                System.out.println("error message from client!");
                System.exit(1);
        }
    }
    
}
