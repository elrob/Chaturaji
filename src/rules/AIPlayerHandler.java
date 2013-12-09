package rules;

//package Rules;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import rules.ChessBoard;
import rules.ChessPieces;
import rules.ChessSquare;
import rules.Move;
import rules.PLAYER;

public class AIPlayerHandler {
	
	private ChessBoard chessBoard;
	//public int maxDepth = 4;
	private long startTime;
        private final int timeLimit = 5;
        
	public AIPlayerHandler (ChessBoard cb) {
		this.chessBoard = cb;
	}
        
        public void setStartTime(){
            startTime = System.currentTimeMillis();
        }
		 
	public boolean aiMove(Move move) {
                //System.out.println("AIPH::AI submitting move");
		return this.chessBoard.submitMove(move, false, true); // not virtual, send message
		//System.out.println("Current player according to ai board " + chessBoard.currentPlayer());

	}
	
	public void aiUndoMove(Move move) {
		this.chessBoard.unmakeMove(move, false);
	}
	
	
	public Move getMove() {
            setStartTime();
            Move bestMove = null;
            Move bestMove_prelim = null;
            System.out.println("Current player according to ai board " + chessBoard.currentPlayer());

		//PLAYER thinkingPlayer = chessBoard.currentPlayer();
            System.out.println("Thinking....");
            int i = 0;
            for(i = 1; System.currentTimeMillis() - startTime < timeLimit*1000; i++) {
                bestMove_prelim = getBestMove(i);
                if(bestMove_prelim != null) {
                    bestMove = bestMove_prelim;
                }
                else
                    break;
            }
            
           System.out.println("Done Thinking... thinking level is " + i); 
            return bestMove;
	}
	
	private Move getBestMove(int i) {
		//System.out.println("Current player according to ai board " + chessBoard.currentPlayer());

		//PLAYER thinkingPlayer = chessBoard.currentPlayer();
		//System.out.println("Thinking....");
		java.util.List<Move> possibleMoves = this.getAllValidMoves(chessBoard.currentPlayer());
		
		int[] max_value = new int[] {-999999,-999999,-999999,-999999};
                int[] score = new int[] {0,0,0,0};
		Move bestMove = null;
		PLAYER actualCurrent = null;
	
		for (Move move: possibleMoves) {
			actualCurrent = chessBoard.currentPlayer();
			
                        //System.out.println("AIPH::AI generating move");
			this.chessBoard.submitMove(move, true, false); // virtual, do not send message
			
                        //System.out.println("Number of current player is " + actualCurrent + " at number " + actualCurrent.ordinal());
			//System.out.println(Arrays.toString(maxN(maxDepth-1, move, actualCurrent)));
			score = maxN(i-1, move, actualCurrent); // get int value of p
			
			this.chessBoard.unmakeMove(move, false); // not send message
			
                        if(score[0] == 66666) {
                            if(i==1)
                                return bestMove;
                            else
                                return null;
                        }
                        
			if(score[actualCurrent.ordinal()] > max_value[actualCurrent.ordinal()]) {
				max_value = score;
				bestMove = move;
			}
			//System.out.println("Score is " + score);
		}
		
		//System.out.println("Done Thinking....");
		//System.out.println("Current player according to ai board " + chessBoard.currentPlayer());
		//System.out.println("Score is " + score);
		if (bestMove == null) {
			System.out.println("move is null");
			System.out.println("Current player" + chessBoard.currentPlayer());
			System.out.println("max_score is " + score[0] + " "+ score[1] + " "+ score[2] + " "+ score[3]);
			System.out.println("max_value is " + max_value[0] + " "+ max_value[1] + " "+ max_value[2] + " "+ max_value[3]);
		}
		return bestMove;
		
	}
	
	
	public int[] maxN(int depth, Move move_prev, PLAYER thinkingPlayer) {
            if(System.currentTimeMillis() - startTime > timeLimit *1000) {
                return new int[] {66666,66666,66666,66666};
            }

		//if (!chessBoard.getAlivePlayers().get(thinkingPlayer)) { 
			//System.out.println("Result of evaluation is " + evalFunction());
			//return evalFunction();
		//}
		
		// if depth is 0 or game is over, stop recursing
    	if(depth == 0 || move_prev.getGameOver()) {
			//System.out.println("Result of evaluation is " + Arrays.toString(evalFunction()));
    		return evalFunction();
    	}
    	java.util.List<Move> possibleMoves = this.getAllValidMoves(chessBoard.currentPlayer());
    	int[] max_value = new int[] {-999999,-999999,-999999,-999999};
    	int score[] = new int[] {0,0,0,0};
    	PLAYER actualCurrent = null;
    	

    	
    	for(Move move: possibleMoves) {
			actualCurrent = chessBoard.currentPlayer();
    		this.chessBoard.submitMove(move, true, false);
			score = maxN(depth-1, move, thinkingPlayer); // get int value of p
			//System.out.println("Actual current is " + actualCurrent.ordinal());
			//System.out.println("Score is " + Arrays.toString(score));
			this.chessBoard.unmakeMove(move, false);
			if (chessBoard.currentPlayer() != null) {
				if(score[actualCurrent.ordinal()] > max_value[actualCurrent.ordinal()]) {
					max_value = score;
				}
			}
			//System.out.println("Score is ");
			//System.out.println(Arrays.toString(max_value));

    	}
    	//System.out.println("State of max_value is " + Arrays.toString(max_value));
	    return max_value;
}
	
	public int[] evalFunction() {
		
		int[] eva = new int[] {0,0,0,0};
		int[] material = new int[] {-999,-999,-999,-999};
		int[] mobility = new int[] {-999,-999,-999,-999};
		int[] threats = new int [] {-999,-999,-999,-999};
		int[] taken = new int[] {-999,-999,-999,-999};
		//java.util.List<Move> validMoves[] = new ArrayMove<Move>();
		
		
		for(Map.Entry<PLAYER, Boolean> entry: chessBoard.getAlivePlayers().entrySet()){

			if(entry.getValue()) {
				

				int p = entry.getKey().ordinal();		
				
				material[p] = chessBoard.getPlayerPieceMap().get(entry.getKey())[0] * 1000 + chessBoard.getPlayerPieceMap().get(entry.getKey())[1] * 40 + chessBoard.getPlayerPieceMap().get(entry.getKey())[2] * 40 + chessBoard.getPlayerPieceMap().get(entry.getKey())[3] * 20 + chessBoard.getPlayerPieceMap().get(entry.getKey())[4] * 10;				
				//score[p] = chessBoard.getScoreMap().get(entry.getKey()) * 10;
				taken[p] = chessBoard.getPlayerCaptures().get(entry.getKey())[0] * 500 + chessBoard.getPlayerCaptures().get(entry.getKey())[1] * 40 + chessBoard.getPlayerCaptures().get(entry.getKey())[2] * 40 + chessBoard.getPlayerCaptures().get(entry.getKey())[3] * 20 + chessBoard.getPlayerCaptures().get(entry.getKey())[4] * 10;
				
				java.util.List<Move> possibleMoves = getAllValidMoves(PLAYER.values()[p]);
				for(int i=0; i < possibleMoves.size(); i++) {
					if (chessBoard.getChessboard().containsKey(possibleMoves.get(i).getToSq())) {
						if (chessBoard.getChessboard().get(possibleMoves.get(i).getToSq()).getName() == "Pawn"){
							threats[chessBoard.getChessboard().get(possibleMoves.get(i).getToSq()).getPlayer().ordinal()] -= 10;
                                                       // threats[p] += 5;
						}
						if (chessBoard.getChessboard().get(possibleMoves.get(i).getToSq()).getName() == "Boat"){
							threats[chessBoard.getChessboard().get(possibleMoves.get(i).getToSq()).getPlayer().ordinal()] -= 20;
                                                       // threats[p] += 15;
						}
						if (chessBoard.getChessboard().get(possibleMoves.get(i).getToSq()).getName() == "Knight"){
							threats[chessBoard.getChessboard().get(possibleMoves.get(i).getToSq()).getPlayer().ordinal()] -= 40;
                                                      //  threats[p] += 15;
						}
						if (chessBoard.getChessboard().get(possibleMoves.get(i).getToSq()).getName() == "Elephant"){
							threats[chessBoard.getChessboard().get(possibleMoves.get(i).getToSq()).getPlayer().ordinal()] -= 40;
                                                       // threats[p] += 20;
						}
						if (chessBoard.getChessboard().get(possibleMoves.get(i).getToSq()).getName() == "King"){
							threats[chessBoard.getChessboard().get(possibleMoves.get(i).getToSq()).getPlayer().ordinal()] -= 3000;
                                                        //threats[p] += 40;
						}
					}
				}
				mobility[p] = getNumValidMoves(entry.getKey());
			}
		}
		

		for(int i = 0; i <eva.length; i ++) {
                    if(chessBoard.aliveCount() > 2)
			eva[i] = material[i] + mobility[i] + threats[i] + taken[i];
                    else
                        eva[i] = material[i] + threats[i] + taken[i]*30;
		}
		
		//System.out.println(Arrays.toString(eva));
		//System.out.println("p1 " + p1 +" p2 " + p2 + " p3 " + p3 + " p4 " + p4);
		
    	return eva;
    }

	public int getNumValidMoves(PLAYER p) {
            int count = 0;
            int value = 0;
            
		Map<ChessSquare,ChessPieces> board = chessBoard.getChessboard();
		
    	for(Map.Entry<ChessSquare, ChessPieces> entry: board.entrySet()){
    
    		if(entry.getValue().getPlayer() == p) {
    			if(entry.getValue().getName() == "Pawn") {
                            value = 5;
                            switch(p) {
                                case P1: 
                                    value += (int)(-(entry.getKey().getCol() - 'G')) * 20;
                                    break;
                                case P2:
                                    value += (int)(entry.getKey().getRow() - '2') *20;
                                    break;
                                case P3:
                                    value += (int)(entry.getKey().getCol() - 'B') *20;
                                    break;
                                case P4:
                                    value += (int)(-(entry.getKey().getRow() - '7')) *20;
                                    break;
                            }
                        }
                        else if(entry.getValue().getName() == "Boat")
                            value = 20;
                        else if(entry.getValue().getName() == "Knight")
                            value = 20;
                        else if(entry.getValue().getName() == "Elephant")
                            value = 20;
                        else if(entry.getValue().getName() == "King")
                            value = 80;
                        
    			count += entry.getValue().validMoves().size() * value;
    		} 	
    	}
    	//System.out.println("mobility is " + count);
    	return count;
	}
	
	public java.util.List<Move> getAllValidMoves(PLAYER player) {
    	
		Map<ChessSquare,ChessPieces> board = chessBoard.getChessboard();
		java.util.List<Move> possibleMoves = new ArrayList<Move>();
		Move add_a_move = new Move("00", "00");
		
    	for(Map.Entry<ChessSquare, ChessPieces> entry: board.entrySet()){
    
    		if(entry.getValue().getPlayer() == player) {
    			
    		    for (Iterator<ChessSquare> iterator = entry.getValue().validMoves().iterator(); iterator.hasNext();) {
        			add_a_move.setFromSq(entry.getKey());
        			add_a_move.setToSq(iterator.next());
        			
        			//System.out.println("Looking at " + chessBoard.currentPlayer() + " 's possible moves.");
    			
        			possibleMoves.add(add_a_move.clone());
    		    }
    		} 	
    	}
    	return possibleMoves;
	
	}
	
	
	
}

