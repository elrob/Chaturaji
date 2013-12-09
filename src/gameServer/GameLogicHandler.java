/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gameServer;

import rules.ChessBoard;
import rules.Move;
import storage.DBHandler;
import lobbyServer.LobbyWSServlet;

/**
 *
 * @author julia
 */
public class GameLogicHandler {
    
    // create board using information stored in database
    protected static ChessBoard createBoard(String gameID) {

        ChessBoard board = new ChessBoard(gameID, true); // modify later

        // Get move list
        String moveList = DBHandler.getMoveList(gameID);
        //System.out.println("moveList is " + moveList);

        if (!moveList.equals("null")) {

            String[] moveArray = moveList.split(",");

            //System.out.println("====moveArray length is " + moveArray.length);

            for (int i = 0; i < moveArray.length; i++) {

                String fromSq = "" + moveArray[i].charAt(0) + moveArray[i].charAt(1);
                String toSq = "" + moveArray[i].charAt(2) + moveArray[i].charAt(3);
                //System.out.println("=====move is from " + fromSq + " to " + toSq);
                Move move = new Move(fromSq, toSq);
                board.submitMove(move, false, false); // not virtual, do not send message

            }
        }

        System.out.println("====recreating game:" + board);
        return board;
    }
    
    
    protected synchronized void makeAIMove(String gameID){
       
        // get the game board the player is in
        ChessBoard boardIn = createBoard(gameID);
        
        System.out.println("ClientMessageHandler::makeAIMove:game over = " + boardIn.getGameOver());
        
        // check to see if the first/next player is AI, if yes, make the move
        while (DBHandler.checkGameID(gameID) 
                && !boardIn.getGameOver() 
                && DBHandler.getPlayerID(gameID, boardIn.currentPlayer()).substring(0, 2).equals("AI")
                && boardIn.AIMove()
                && ChaturajiServlet.getBuff().get(gameID) != null) {
            
            System.out.println("current gameID is " + gameID + ", current player ID is " + DBHandler.getPlayerID(gameID, boardIn.currentPlayer()));
        
            // send messages
            ChaturajiServlet.getBuff().get(gameID).send();
                                     
        }
        
        if(DBHandler.checkGameID(gameID) && boardIn.getGameOver()){
            // end of game
            // delete message buffer and logic handler for the game            
            ChaturajiServlet.getBuff().remove(gameID);
            ChaturajiServlet.getHandlerMap().remove(gameID);
            System.out.println("makeAIMove:message buff and logic handler for GAME " + gameID + " deleted!");

            // delte game in database
            DBHandler.deleteGame(gameID);
            
            // lobby WS: send messages to all clients to update game list
            LobbyWSServlet.updateClients();
        } 
    }
}
