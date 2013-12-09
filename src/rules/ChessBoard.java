package rules;               // COMMENT OUT FOR SERVER INTEGRATION

import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;

import validationMessages.BoatTriumph;
import validationMessages.EndGame;
import validationMessages.PawnPromotion;
import validationMessages.PlayerOut;
import validationMessages.SuddenDeath;
import validationMessages.UpdateScore;
import validationMessages.ValidMove;

public class ChessBoard {
/*
	public static void main(String[] args) {
		
		ChessBoard cb  = new ChessBoard("1", true);
		//ChessBoard cb  = new ChessBoard();
		
		cb.resetBoard();
		cb.printBoard();
		
		AIPlayerHandler ai = new AIPlayerHandler(cb);
		
		Move move = ai.getMove();
		while (ai.aiMove(move)) {
			cb.printBoard();
			if(!gameover)
				move = ai.getMove();
			else
				break;
		}
		cb.printBoard();
		
	}
*/
	/************************************constants************************************/
		
	// constant for initializing the number of moves left when only two players are left alive
	private final int MOVELEFT = 10;
	

	/************************************attributes***********************************/
	// unique game ID for the board
	private String gameID;
	
	// keep track of whose turn it is
	private PLAYER curPlayer;
	private boolean gameover;
	
	// a map to represent the chess board
	private Map<ChessSquare,ChessPieces> board = new HashMap<ChessSquare, ChessPieces>();
	
	// a map to represent the number of pieces each player has:
	// PLAYER   number of pieces ( King, Elephant, Knight, Boat, Pawn ) (represented by "pieceCounter")
	// P1          11114
	// P2          11114
	// ...          ...
	private Map<PLAYER,int[]> playerPieces = new HashMap<PLAYER,int[]>();
	
	// a map to represent the number of pieces each player has captured:
	// PLAYER   number of pieces ( King, Elephant, Knight, Boat, Pawn )
	// P1          00000
	// P2          10000
	// ...          ...
	private Map<PLAYER, int[]> playerCaptures = new HashMap<PLAYER, int[]>();
	
	// stores the score of each player:
	// P1 -- 0
	// P2 -- 5
	// ...  ...
        private Map<PLAYER, Integer> score= new HashMap<PLAYER,Integer> ();

        // stores the alive/dead status of each player, true for alive, false for dead
        // P1 -- true
        // P2 -- true
        // ...   ...
        private Map<PLAYER, Boolean> playerAlive = new HashMap<PLAYER, Boolean>();

        // keep track of the number of moves left for each player when there are only two players alive
        private Map<PLAYER, Integer> moveCount = new HashMap<PLAYER, Integer>();


        // AI handler for the board
        // ...
        AIPlayerHandler ai;    
    
    
    
    /************************************** methods ************************************/
    
	/* Constructor */
    
        public ChessBoard(String gID, boolean setAI){
    	
		gameID = gID;
		if (setAI) {
			// generate AI thread
			 ai = new AIPlayerHandler(this);
		}
			
		resetBoard();
	}
	
	// reset the board: clear pieces, create pieces, 
	// initialize curPlayer, score, playerAlive, playerPieces and playerCaptures
	public void resetBoard(){
		
		// empty the board
		board.clear();
	
	    // Now create new pieces, placing them in their starting positions
		// kings
	    board.put( new ChessSquare("H4"), new King(this,PLAYER.P1) );
	    board.put( new ChessSquare("D1"), new King(this,PLAYER.P2) );
	    board.put( new ChessSquare("A5"), new King(this,PLAYER.P3) );
	    board.put( new ChessSquare("E8"), new King(this,PLAYER.P4) );
	    
	    // elephants
	    board.put( new ChessSquare("H3"), new Elephant(this,PLAYER.P1) );
	    board.put( new ChessSquare("C1"), new Elephant(this,PLAYER.P2) );
	    board.put( new ChessSquare("A6"), new Elephant(this,PLAYER.P3) );
	    board.put( new ChessSquare("F8"), new Elephant(this,PLAYER.P4) );
	   
	    // knights
	    board.put( new ChessSquare("H2"), new Knight(this,PLAYER.P1) );
	    board.put( new ChessSquare("B1"), new Knight(this,PLAYER.P2) );
	    board.put( new ChessSquare("A7"), new Knight(this,PLAYER.P3) );
	    board.put( new ChessSquare("G8"), new Knight(this,PLAYER.P4) );

	    // boats
	    board.put( new ChessSquare("H1"), new Boat(this,PLAYER.P1) );
	    board.put( new ChessSquare("A1"), new Boat(this,PLAYER.P2) );
	    board.put( new ChessSquare("A8"), new Boat(this,PLAYER.P3) );
	    board.put( new ChessSquare("H8"), new Boat(this,PLAYER.P4) );
	    
	    
	    
	    // pawns for player P1
	    for(char row = '1'; row <= '4'; row++)
	        board.put( new ChessSquare(row, 'G'), new Pawn(this, PLAYER.P1) );
	    
	    // pawns for player P2
	    for(char col = 'A'; col <= 'D'; col++)
	    	board.put( new ChessSquare('2', col), new Pawn(this, PLAYER.P2) );
	    
	    // pawns for player P3
	    for(char row = '5'; row <= '8'; row++)
	    	board.put( new ChessSquare(row, 'B'), new Pawn(this, PLAYER.P3) );
	    
	    // pawns for player P4
	    for(char col = 'E'; col <= 'H'; col++)
	    	board.put( new ChessSquare('7', col), new Pawn(this, PLAYER.P4) );
	    
	    // player P1 gets to move first
	    curPlayer = PLAYER.P1;
	    
	    // initialize score of each player to 0
	    score.put(PLAYER.P1,0);
	    score.put(PLAYER.P2,0);
	    score.put(PLAYER.P3,0);
	    score.put(PLAYER.P4,0);
	    
	    // initialize the alive/dead status of each player to alive
	    playerAlive.put(PLAYER.P1, true);
	    playerAlive.put(PLAYER.P2, true);
	    playerAlive.put(PLAYER.P3, true);
	    playerAlive.put(PLAYER.P4, true);
	    
	    // initialize the number of each kind of pieces each player has
		// int array to represent the number of each kind of piece each player has
		// pieceCounter[0] -- King
		// pieceCounter[1] -- Elephant
		// pieceCounter[2] -- Knight
		// pieceCounter[3] -- Boat
		// pieceCounter[4] -- Pawn
	    playerPieces.put(PLAYER.P1,new int[] { 1, 1, 1, 1, 4});
	    playerPieces.put(PLAYER.P2,new int[] { 1, 1, 1, 1, 4});
	    playerPieces.put(PLAYER.P3,new int[] { 1, 1, 1, 1, 4});
	    playerPieces.put(PLAYER.P4,new int[] { 1, 1, 1, 1, 4});
	    
	    // initialize the number of each kind of pieces each player has captured
		// int array to represent the number of each kind of pieces each player has captured
		// capturedCounter[0] -- King
		// capturedCounter[1] -- Elephant
		// capturedCounter[2] -- Knight
		// capturedCounter[3] -- Boat
		// capturedCounter[4] -- Pawn
	    playerCaptures.put(PLAYER.P1, new int[] { 0, 0, 0, 0, 0});
	    playerCaptures.put(PLAYER.P2, new int[] { 0, 0, 0, 0, 0});
	    playerCaptures.put(PLAYER.P3, new int[] { 0, 0, 0, 0, 0});
	    playerCaptures.put(PLAYER.P4, new int[] { 0, 0, 0, 0, 0});
	    
	    System.out.println("A new chaturaji game is started!");
	}

	// a player wants to submit a move
	public boolean submitMove(Move move, boolean virtual, boolean sendMsg){
		
		ChessSquare fromSq = move.getFromSq();
		ChessSquare toSq = move.getToSq();
		//Move move = new Move(fromSquare,toSquare);
		
		//check valid move
		if (!virtual) {

			if(fromSq.equals(toSq))
			{	
				System.out.println("Destination square same as source square!");
				return false;
			}
			
			// ensure the source and destination are valid squares
			if(!fromSq.valid()||!toSq.valid())
			{
				System.out.println("Source or destination square is invalid!");
				return false;
			}
			
			// ensure there is a piece at the source square
			if(!board.containsKey(fromSq))
			{	
				System.out.println("There is no pieces at position " + fromSq.toString() + "!");
				return false;
			}
			
			// ensure the piece is of the right side (whose turn it is)
			if(board.get(fromSq).getPlayer()!=curPlayer)
			{
				System.out.println("It is not your turn!");
				return false;
			}
		
		}	

		
		// get the piece object of the source square
		ChessPieces piece= board.get(fromSq);

		// check if there is a piece at the destination square.
		ChessPieces taken_piece = board.get(toSq);

		if (!virtual) {
		
			if(!piece.validMoves().contains(toSq))
			{
				System.out.println(piece.getName()+ " cannot move from " + fromSq + " to "+ toSq + "!");
				return false;
			}
			
		}

		// now the move is valid
		board.put(toSq,piece);
		board.remove(fromSq);
		move.setPrevPlayer(curPlayer);
		
		if (!virtual) {
		
			// if everything is OK, output the move.
			System.out.println(piece.getName()+ " moves from " + fromSq + " to "+ toSq);
			
		}
		
		// check if any piece captured
		if(taken_piece != null)
		{
			//System.out.println("taking" + taken_piece.getName());                       // uncomment to print out piece taken to screen
			
			move.setCapturedPiece(taken_piece);
			
			// update score each time a piece is taken
			updateScore(piece.getPlayer(), taken_piece, 1, sendMsg);
			
			// update playerPieces map
			updatePieceCounter(taken_piece.getPlayer(),taken_piece, -1);
			
			// update playerCaptures map
			updateCaptured(piece.getPlayer(), taken_piece, 1);
			
		}
		
		
		if(piece.getName().equals("Boat")) {
			checkBoatTriumph(move, sendMsg);
		}
		
		// check for any pawns on the board eligible for promotion
		checkPawnPromotion(move, sendMsg);
		
		// check if this player is dead
		if(taken_piece != null /*&& taken_piece.getName().equals("King")*/) {

			PLAYER taken_player = taken_piece.getPlayer();
			if(checkDeath(taken_player) && playerAlive.get(taken_player)) { // added "playerAlive.get(taken_player)" to ensure a dead player is not examined any more
				
				playerAlive.put(taken_player, false);
				move.setDeadPlayer(taken_player);
				//System.out.println("Player " + taken_player.toString() + " is out!");
				
				if(sendMsg){
                                    //////// COMMENT OUT FOR SERVER INTEGRATION ///////
                                    //////// buff message to client indicating a player is out ////////
                                    PlayerOut.buff(gameID, taken_player);			
                                }
                                
			}
		}
		
		if(aliveCount() <= 2) { 
                        	
    		//System.out.println("Alive players:");
    		//printPlayerAlive();
    	
                    if(aliveCount() == 2 && moveCount.size() == 0){ // the first time to hit 2 player scenario
    			
                        if(!virtual){
                            
                            //System.out.println("start the counter");
                            // initialize moveCount map with players still alive and moves left = MOVELEFT
                            for(Map.Entry<PLAYER, Boolean> entry: playerAlive.entrySet()){
                                    if(entry.getValue())
                                            moveCount.put(entry.getKey(), MOVELEFT);   
                            }
                            
                        }
                        
                        if(sendMsg){
                            
                            //////// COMMENT OUT FOR SERVER INTEGRATION ///////
                            ///////// buff message to clients indicating there are only 10 moves for each player ////////
                            SuddenDeath.buff(gameID);
                        }
    				
                    } else if(aliveCount() == 2 && moveCount.size() == 2){ // still two players left alive
    			
                        // decrease the move count of the player who just submitted a move
    			// if (moveCount.containsKey(piece.getPlayer()))
                        if(!virtual && moveCount.containsKey(piece.getPlayer()))
                            moveCount.put(piece.getPlayer(), moveCount.get(piece.getPlayer())-1);
    			
    			int countA = 0;
    			int countB = 0;
                        //????????
    			//System.out.println("current player has " + moveCount.get(piece.getPlayer()) + " moves left");
    			if (moveCount.containsKey(piece.getPlayer()))			
    				countA = moveCount.get(piece.getPlayer()); // move count for the player who just made a move
    			if (nextPlayer() != PLAYER.INVALID_PLAYER) 
    				if (moveCount.containsKey(nextPlayer()))
    					countB = moveCount.get(nextPlayer()); // move count for the opponent player
    			if (!virtual) {
    				System.out.println("Moves left: " + countA + " " + countB);
    			}

    			// check if moves of the two players have both expired
    			if(countA == 0 && countB == 0){
    				
    				move.setGameOver();
    				//System.out.println("GAME OVER!@1");
                                
    				if(!virtual){
                                    
                                    if(sendMsg){
                                        //////// COMMENT OUT FOR SERVER INTEGRATION ///////
                                        ///////// buff message to clients indicating game over with game result ///////////
                                        EndGame.buff(gameID, score);

                                                                    //////// COMMENT OUT FOR SERVER INTEGRATION ///////
                                        //////// buff message to clients indicating valid move ////////
                                        //System.out.println("GAME OVER!@1");
                                        //System.out.println("--------valid move 1");
                                        ValidMove.buff(gameID, move.getFromSq().toString(), move.getToSq().toString());
                                    }
                                    gameover = true;
                                    return true;
                                    
    				}else {
    					//gameover = false;
                                        return false;
    				}
    			}
    			
                    } else if(aliveCount() == 1){// one of the two players is dead (either king captured or left with only king)
    			
			move.setGameOver();
    			//System.out.println("GAME OVER!@2");
                        
                        if(!virtual){
                            
                            if(sendMsg){
                                //////// COMMENT OUT FOR SERVER INTEGRATION ///////
                                //////// buff message to clients indicating game over with game result ///////////
                                EndGame.buff(gameID, score);

                                //////// COMMENT OUT FOR SERVER INTEGRATION ///////
                                //////// buff message to clients indicating valid move ////////
                                //System.out.println("GAME OVER!@2");
                                //System.out.println("--------valid move 2");
                                ValidMove.buff(gameID, move.getFromSq().toString(), move.getToSq().toString());
                            }
                            
                            gameover = true;
                            return true;
                            
                        }else {
                            gameover = false;
                            return false;
                        }
                    }
                        
                        
		}
                
		// next player's turn
			
		//System.out.println(move.getPrevPlayer());
		//System.out.println(currentPlayer());		
			
		curPlayer = nextPlayer();
		//printPlayerAlive();
		//System.out.println(currentPlayer());		

		if (!virtual) {
                    
                    System.out.println("Next player is " + curPlayer.toString());
                    
                    if(sendMsg){
                        //////// COMMENT OUT FOR SERVER INTEGRATION ///////
                        //////// buff message to clients indicating valid move ////////
                        //System.out.println("--------valid move 2");
                        ValidMove.buff(gameID, move.getFromSq().toString(), move.getToSq().toString());
                    }
                }
		//printScore();
		return true;
	}
		
	
	// count the number of players that are still alive
	public int aliveCount(){
		int count = 0;
		for(Map.Entry<PLAYER, Boolean> entry: playerAlive.entrySet()){
			if(entry.getValue())
				count++;
		}
		return count;
	}
	
	// check if player p is dead
	public boolean checkDeath(PLAYER p){

		int[] pieces = playerPieces.get(p);
		
		if(pieces[0] < 1 || ((pieces[1] < 1) &&
				(pieces[2] < 1) &&
				(pieces[3] < 1) &&
				(pieces[4] < 1))) // the player has no king or the player has king but no other pieces
			return true;
		return false;
	
	}
	
	// update the captures record of a certain player
	public void updateCaptured(PLAYER p, ChessPieces cp, int i){
		
		// get the name of the piece
		String pieceType = cp.getName();
		
		if(pieceType.equals("King")){ // no counter check at this moment (in case King captured twice)
			
			playerCaptures.get(p)[0] += i ;
			
		}else if(pieceType.equals("Elephant")){
			
			playerCaptures.get(p)[1] += i;
			
		}else if(pieceType.equals("Knight")){
			
			playerCaptures.get(p)[2] += i;
			
		}else if(pieceType.equals("Boat")){
			
			playerCaptures.get(p)[3] += i;
			
		}else if(pieceType.equals("Pawn")){
			
			playerCaptures.get(p)[4] += i;
			
		}else{
			
			System.out.println("Invalid piece type!");
			System.exit(1);
			
		}
	}
	
	public Map<PLAYER, Integer> getScoreMap() {
		return score;
	}
	
	public Map<PLAYER,int[]> getPlayerPieceMap() {
		return playerPieces;
	}
	
	public Map<PLAYER,int[]> getPlayerCaptures() {
		return playerCaptures;
	}
	
	// update the score of a certain player
	public void updateScore(PLAYER p, ChessPieces cp, int i, boolean sendMsg){
		
		if( cp.getName().equals("King") && ((playerCaptures.get(p)[0] == 2 & i>0) || (playerCaptures.get(p)[0] == 3 & i<0))){
			// add an extra 54 points if the player has already captured 2 kings and now capturing another king
			if (i > 0)
				score.put(p, score.get(p) + 54 + 5);
			else
				score.put(p, score.get(p) - 54 - 5);
		}else{
			if (i > 0)
				score.put(p, score.get(p) + cp.getScore() );
			else //unmake a move
				score.put(p, score.get(p) - cp.getScore() );
		}
		//System.out.append("1sendMsg is " + sendMsg);
                if(sendMsg){
                    //////// COMMENT OUT FOR SERVER INTEGRATION ///////
                    ////////buff message to client indicating new score ////////	
                    UpdateScore.buff(gameID, score);
                }
		
	}
	
	// update the number of each kind of pieces a certain player has
	public void updatePieceCounter(PLAYER p, ChessPieces cp, int i)
	{
		// get the name of the piece
		String pieceType = cp.getName();
		// do is +, undo is -
		if(pieceType.equals("King")){ // no counter check at this moment (in case King captured twice)
			
			playerPieces.get(p)[0] += i;
			
		}else if(pieceType.equals("Elephant")){
			
			playerPieces.get(p)[1] += i;
			
		}else if(pieceType.equals("Knight")){
			playerPieces.get(p)[2] += i;
			
		}else if(pieceType.equals("Boat")){
			
			playerPieces.get(p)[3] += i;
			
		}else if(pieceType.equals("Pawn")){
			
			playerPieces.get(p)[4] += i;
			
		}else{
			
			System.out.println("Invalid piece type!");
			System.exit(1);
			
		}
	}
	
	// get the turn
	public PLAYER currentPlayer(){
		return curPlayer;
	}
	
	// get the next turn
    public PLAYER nextPlayer(){
    	  	
    	switch(curPlayer){
    		case P1: 
    			if(playerAlive.get(PLAYER.P2)){
    				return PLAYER.P2;
    			}else if(playerAlive.get(PLAYER.P3)){
    				return PLAYER.P3;
    			}else if(playerAlive.get(PLAYER.P4)){
    				return PLAYER.P4;
    			}else{
    				return PLAYER.INVALID_PLAYER;
    			}
    		case P2: 
    			if(playerAlive.get(PLAYER.P3)){
    				return PLAYER.P3;
    			}else if (playerAlive.get(PLAYER.P4)){
    				return PLAYER.P4;
    			}else if(playerAlive.get(PLAYER.P1)){
    				return PLAYER.P1;
    			}else{
    				return PLAYER.INVALID_PLAYER;
    			}
    		case P3: 
    			if(playerAlive.get(PLAYER.P4)){
    				return PLAYER.P4;
    			}else if(playerAlive.get(PLAYER.P1)){
    				return PLAYER.P1;
    			}else if(playerAlive.get(PLAYER.P2)){
    				return PLAYER.P2;
    			}else{
    				return PLAYER.INVALID_PLAYER;
    			}
    		case P4:
    			if(playerAlive.get(PLAYER.P1)){
    				return PLAYER.P1;
    			}else if(playerAlive.get(PLAYER.P2)){
    				return PLAYER.P2;
    			}else if(playerAlive.get(PLAYER.P3)){
    				return PLAYER.P3;
    			}else{
    				return PLAYER.INVALID_PLAYER;
    			}
    		default: return PLAYER.INVALID_PLAYER;
    	}
    }
    
    // check if a certain square is occupied by any piece
    public boolean occupied(ChessSquare sq){
    	//System.out.println(board.containsValue(findSquare(sq.toString())));
    	return (board.containsKey(sq));
    }
    
    // check if a specific square "sq" is occupied by a piece that does not belong to player "p"
    public boolean occupiedByOpponent(ChessSquare sq, PLAYER p){
    	if(board.get(sq) == null)  return false;
    	else return (board.get(sq).getPlayer()!= p);    	
    }
    
    // get the position of "piece"
    public ChessSquare positionOf(ChessPieces piece){
    	   	
    	for ( Map.Entry<ChessSquare, ChessPieces> entry : board.entrySet())
    	{
    		if(entry.getValue() == piece)
    			return entry.getKey();
    	}
       	return ChessSquare.invalidSquare();
    }   

    public void checkBoatTriumph(Move move, boolean sendMsg) {	
    	
    	ChessSquare sq = move.getToSq();
    	ChessPieces piece = board.get(sq);
    	
    	ChessSquare[] sq_boats1 = new ChessSquare[]{sq.right(), sq.upper_right(), sq.up()};
    	ChessSquare[] sq_boats2 = new ChessSquare[]{sq.up(), sq.upper_left(), sq.left()};
    	ChessSquare[] sq_boats3 = new ChessSquare[]{sq.left(), sq.lower_left(), sq.down()};
    	ChessSquare[] sq_boats4 = new ChessSquare[]{sq.down(), sq.lower_right(), sq.right()};
    	
    	if(checkThreeBoats(sq_boats1, move)) {
    		move.getBoatTriumph()[0] = 1;
    		removeThreeBoats(sq_boats1, piece, sendMsg);
                if(sendMsg){
                    //////// COMMENT OUT FOR SERVER INTEGRATION ///////
                    //////// buff message to clients indicating boat triumph ////////
                    BoatTriumph.buff(gameID, sq_boats1, positionOf(piece).toString());
                }
    	}
    	
    	else if(checkThreeBoats(sq_boats2, move)) {
    		move.getBoatTriumph()[0] = 2;
    		removeThreeBoats(sq_boats2, piece, sendMsg);
                if(sendMsg){
                    //////// COMMENT OUT FOR SERVER INTEGRATION ///////
                    //////// buff message to clients indicating boat triumph ////////
                    BoatTriumph.buff(gameID, sq_boats2, positionOf(piece).toString());
                }
    		
    	}
    	else if(checkThreeBoats(sq_boats3, move)) {
    		move.getBoatTriumph()[0] = 3;
    		removeThreeBoats(sq_boats3, piece, sendMsg);
                if(sendMsg){
                    //////// COMMENT OUT FOR SERVER INTEGRATION ///////
                    //////// buff message to clients indicating boat triumph ////////
                    BoatTriumph.buff(gameID, sq_boats3, positionOf(piece).toString());
                }
    	}
    	else if(checkThreeBoats(sq_boats4, move)) {
    		move.getBoatTriumph()[0] = 4;
    		removeThreeBoats(sq_boats4, piece, sendMsg);
    		if(sendMsg){
                    //////// COMMENT OUT FOR SERVER INTEGRATION ///////
                    //////// buff message to clients indicating boat triumph ////////
                    BoatTriumph.buff(gameID, sq_boats4, positionOf(piece).toString());
                }
    	}
    	else {
    		return;
    	}
    		
    }
    
    public boolean checkThreeBoats(ChessSquare[] sq_boats, Move move) {
    	
    	for(int i = 0; i < sq_boats.length; i++) {
    		if(!sq_boats[i].valid() || board.get(sq_boats[i]) == null ||!(board.get(sq_boats[i]).getName()).equals("Boat")) {
    			return false;
    		}
    		else
    			move.setBoatTriumph(i+1, board.get(sq_boats[i]).getPlayer().ordinal());	
    	}
    	
    	
    	return true;
    }
    
    public void removeThreeBoats(ChessSquare[] sq_boats, ChessPieces taking_piece, boolean sendMsg) {
    	
    	System.out.println("taking three boats");
    	
    	for(int i = 0; i < sq_boats.length; i++) {
    		
    		updateScore(taking_piece.getPlayer(), board.get(sq_boats[i]), 1, sendMsg);  	   	   		
    		updatePieceCounter(board.get(sq_boats[i]).getPlayer(),board.get(sq_boats[i]), -1);
    		updateCaptured(taking_piece.getPlayer(), board.get(sq_boats[i]), 1);
    		
    		board.remove(sq_boats[i]);
    	}
    	
    }
    
    // check if any player has pawn eligible for promotion
    public void checkPawnPromotion(Move move, boolean sendMsg){
    	PLAYER p;
    	
    	for(Map.Entry<PLAYER, Boolean> entry: playerAlive.entrySet()){
			if(entry.getValue()) {
				p = entry.getKey();		
				checkPawnPromotion(p, move, sendMsg);
			}
		}
    }
    
    public void checkPawnPromotion(PLAYER p, Move move, boolean sendMsg) {
        
    	if(playerPieces.get(p)[4] <= 2) {
    		
    		for(Map.Entry<ChessSquare, ChessPieces> entry: board.entrySet()){
    			if(entry.getValue().getName().equals("Pawn") && entry.getValue().getPlayer() == p) {
    				
    				switch(p) {
    		    	
    	    		case P1: if(entry.getKey().getCol() == 'A') 
    	    					checkPawnPromotion(p, entry.getKey(), move, sendMsg);
    	    				break;
    	    				
    	    		case P2: if(entry.getKey().getRow() == '8') 
    							checkPawnPromotion(p, entry.getKey(), move, sendMsg);
    						break;
    	    			
    	    		case P3: if(entry.getKey().getCol() == 'H') 
    							checkPawnPromotion(p, entry.getKey(), move, sendMsg);
    						break;
    						
    	    		case P4: if(entry.getKey().getRow() == '1') 
    							checkPawnPromotion(p, entry.getKey(), move, sendMsg);
    	    				break;
    	    				
    	    		default: System.out.println("Invalid Player!");
    	    				System.exit(1);
    	    	
    				}
    					    
    			}//end of if 
    			
    		}//end of for loop
    		
    		
    	}
    	
    	else 
    	    return;
    	
    }
          
   
    public void checkPawnPromotion(PLAYER p, ChessSquare sq, Move move, boolean sendMsg) {
    	
    	String[] toBoat = new String[] {"A1","A8","H1","H8"};
    	String[] toKnight = new String[] {"A2","A7","B1","B8","G1","G8","H2","H7"};
    	String[] toElephant = new String[] {"A3","A6","C1","C8","F1","F8","H3","H6"};
    	String[] toKing = new String[] {"A4","A5","D1","D8","E1","E8","H4","H5"};
    	 	
    	if(Arrays.asList(toKnight).contains(sq.toString()) && playerPieces.get(p)[2] == 0) {
			changePawn(2, sq, p, sendMsg);
			move.setPawnPromotion(sq,2);
    	}
	    if(Arrays.asList(toElephant).contains(sq.toString()) && playerPieces.get(p)[1] == 0) {
	    	changePawn(1, sq, p, sendMsg);
	    	move.setPawnPromotion(sq,1);
	    }
	    if(playerPieces.get(p)[4] == 1 && 
	    		playerPieces.get(p)[3] <= 1 && playerPieces.get(p)[2] == 0 && playerPieces.get(p)[1] == 0 && playerPieces.get(p)[0] <= 1) {
	    	    	
	    	if(Arrays.asList(toBoat).contains(sq.toString()) && playerPieces.get(p)[3] == 0) {
	    			changePawn(3, sq, p, sendMsg);
	    			move.setPawnPromotion(sq,3);
	    	}
	    	if(Arrays.asList(toKing).contains(sq.toString()) && playerPieces.get(p)[0] == 0) {
	    			changePawn(0, sq, p, sendMsg);
	    			move.setPawnPromotion(sq,0);
	    	}
	    	
	    } 		    
    }
          
    public void changePawn(int the_case, ChessSquare sq, PLAYER p, boolean sendMsg){
    	
    	
    	System.out.println("We are in case " + the_case + " of Pawn Promotion");
    	//board.remove(sq);	
    	playerPieces.get(p)[4]--;
    	
    	switch(the_case) {
    	
    		case 0: board.put( sq, new King(this,p));
    				playerPieces.get(p)[0]++;
                                if(sendMsg){
                                    //////// COMMENT OUT FOR SERVER INTEGRATION ///////
                                    //////// buff message to clients indicating pawn promotion to king ////////
                                    PawnPromotion.buff(gameID, sq.toString(), "king");
                                }
    				break;
    				
    		case 1: board.put( sq, new Elephant(this,p));
					playerPieces.get(p)[1]++;
                                        if(sendMsg){
                                            //////// COMMENT OUT FOR SERVER INTEGRATION ///////
                                            //////// buff message to clients indicating pawn promotion to elephant ////////
                                            PawnPromotion.buff(gameID, sq.toString(), "elephant");
                                        }
					break;
    				
    		case 2: board.put( sq, new Knight(this,p));
					playerPieces.get(p)[2]++;
					playerPieces.get(p)[2]++;
                                        if(sendMsg){
                                            //////// COMMENT OUT FOR SERVER INTEGRATION ///////
                                            //////// buff message to clients indicating pawn promotion to knight ////////
                                            PawnPromotion.buff(gameID, sq.toString(), "knight");
                                        }
					break;
					
    		case 3: board.put( sq, new Boat(this,p));
    				playerPieces.get(p)[3]++;
    				if(sendMsg){
                                    //////// COMMENT OUT FOR SERVER INTEGRATION ///////
                                    //////// buff message to clients indicating pawn promotion to boat////////
                                    PawnPromotion.buff(gameID, sq.toString(), "boat");
                                }
    				break;
    				
    		default: System.out.println("Invalid Pawn Promotion!");
    				System.exit(1);
    	
    	}
    	
    }
    
   
    public void printScore() {
    	for (Entry<PLAYER, Integer> entry : score.entrySet()) {
    	    System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
    	}
    }
    
    public void printPlayerAlive() {
    	for (Entry<PLAYER, Boolean> entry : playerAlive.entrySet()) {
    	    System.out.println("Player = " + entry.getKey() + ", Alive = " + entry.getValue());
    	}
    }
    
    public void printPlayerPiece() {
    	for (Entry<PLAYER, int[]> entry : playerPieces.entrySet()) {
    		System.out.print("Player = " + entry.getKey());
    		for(int i = 0; i < entry.getValue().length; i++) {
    			System.out.print(" Has pieces  = " + entry.getValue()[i]);
    		}
    		System.out.println("");
    	}
    }
    
    public void printPlayerCaptures() {
    	for (Entry<PLAYER, int[]> entry : playerCaptures.entrySet()) {
    		System.out.print("Player = " + entry.getKey() + " ");
    		for(int i = 0; i < entry.getValue().length; i++) {
    			System.out.print("Captures of type " + i + " = " + entry.getValue()[i] + " ");
    		}
    		System.out.println("");
    	}
    }
    
    
    // printBoard outputs the current state of the board to the screen
    public void printBoard () {
    	
    	System.out.println("===CURRENT STATE OF THE BOARD===");
    	System.out.println("  _______________________________");
    	
    	for (int i=8; i >= 1; i--) {
    		System.out.print(i);
    		for (int j=1; j <= 8; j++) {
    			//System.out.print('|');
    			String file = String.valueOf((char)(j+64));
    			String rank = String.valueOf((char)(i+48));
    			String square = file + rank;
    			ChessSquare testSq = new ChessSquare (square);
    			if (board.containsKey(testSq)) {
    				ChessPieces testP = board.get(testSq);
    				System.out.print("|" + convertName(testP.getName()) + testP.getPlayer());
    			}
    			else System.out.print("|   ");
    		}
    		System.out.println("|");
    		System.out.print(" ");
    		for (int k=1; k <= 8; k++) {
    			System.out.print("|___");
    		}
    		System.out.println("|");
    	}
    	
    	for (char c='A'; c <= 'H'; c++) {
    		System.out.print("   " + c);
    	}
    	System.out.println();
    	System.out.println();
    	
    	System.out.println("===================================");

    }
    
    // convertName() takes the string type of a piece (e.g. Pawn) and converts to a one-word notation (P)
    public String convertName (String longName) {
    	
    	if (longName.equals("Pawn"))
    		return "P";
    	if (longName.equals("King"))
    		return "K";
    	if (longName.equals("Knight"))
    		return "N";
    	if (longName.equals("Elephant"))
    		return "E";
    	if (longName.equals("Boat"))
    		return "B";
    	return "U"; // Unknown type
    }
    
    public Map<ChessSquare,ChessPieces> getChessboard() {
    	return this.board;
    }
   
    
    public void unmakeMove(Move move, boolean sendMsg) {
    	
    	ChessSquare fromSq = move.getFromSq();
		ChessSquare toSq = move.getToSq();
		
		if (!move.getPawnPromotion().isEmpty()) {
			
		
			for(Map.Entry<ChessSquare, Integer> entry: move.getPawnPromotion().entrySet()){
				board.put(entry.getKey(), new Pawn(this, board.get(entry.getKey()).getPlayer()));	
				playerPieces.get(board.get(entry.getKey()).getPlayer())[entry.getValue()]--;
				playerPieces.get(board.get(entry.getKey()).getPlayer())[4]++;
			}
			move.getPawnPromotion().clear();
    	}
		
		ChessPieces piece= board.get(toSq);
		board.put(fromSq, piece);
		board.remove(toSq);
		
		if(move.getCapturedPiece() != null)
		{		
				
			board.put(toSq, move.getCapturedPiece());
			
			updateScore(piece.getPlayer(), move.getCapturedPiece(), -1, sendMsg);
			
			updatePieceCounter(move.getCapturedPiece().getPlayer(),move.getCapturedPiece(), 1);
			
			updateCaptured(piece.getPlayer(), move.getCapturedPiece(), -1);
			
			move.setCapturedPiece(null);
			
		}
    	
    	if (move.getDeadPlayer() != null) {
    		playerAlive.put(move.getDeadPlayer(), true);
    		move.setDeadPlayer(null);
    	}
    	
    	if (move.getBoatTriumph()[0] != -1) {
    		System.out.println("boatTriumph Array looks like this before unmakeMove(): " + Arrays.toString(move.getBoatTriumph()));
    		putBackThreeBoats(toSq, piece, move, sendMsg);
    	}
    	
    	//System.out.println("Previous move in unmakeMove() is: " + move.getFromSq() + move.getToSq());
    	//printPlayerAlive();
    	//System.out.println("Current player in unmakeMove() 1: " + currentPlayer());
    	curPlayer = move.getPrevPlayer();
    	//System.out.println("Previous player in unmakeMove(): " + move.getPrevPlayer());
    	//System.out.println("Current player in unmakeMove() 2: " + currentPlayer());
    	
    	return;
    }
    
    public void putBackThreeBoats(ChessSquare sq, ChessPieces cp, Move move, boolean sendMsg) {
    	
    	switch(move.getBoatTriumph()[0]) {
    	
		case 1: ChessSquare[] sq_boats1 = new ChessSquare[]{sq.right(), sq.upper_right(), sq.up()};
				addBoats(sq_boats1,cp,move, sendMsg);
				break;
				
		case 2: ChessSquare[] sq_boats2 = new ChessSquare[]{sq.up(), sq.upper_left(), sq.left()};
				addBoats(sq_boats2,cp,move, sendMsg);
				break;
			
		case 3: ChessSquare[] sq_boats3 = new ChessSquare[]{sq.left(), sq.lower_left(), sq.down()};
				addBoats(sq_boats3,cp,move, sendMsg);
				break;
				
		case 4: ChessSquare[] sq_boats4 = new ChessSquare[]{sq.down(), sq.lower_right(), sq.right()};
				addBoats(sq_boats4,cp,move, sendMsg);
				break;
				
		default: System.out.println("No boat Triumph!");
				System.exit(1);
	
		}
    	
    	move.setBoatTriumph(0, -1);
    }
    
    
    public void addBoats(ChessSquare[] sq_boats, ChessPieces cp, Move move, boolean sendMsg){
    	
    	for (int i = 0; i < sq_boats.length; i++) {
    		board.put(sq_boats[i], new Boat(this, PLAYER.values()[move.getBoatTriumph()[i+1]]));
    		//playerPieces.get(PLAYER.values()[move.getBoatTriumph()[i+1]])[3]++;
    		
    		updateScore(cp.getPlayer(), board.get(sq_boats[i]), -1, sendMsg);
			updatePieceCounter(board.get(sq_boats[i]).getPlayer(),board.get(sq_boats[i]), 1);	
			updateCaptured(cp.getPlayer(), board.get(sq_boats[i]), -1);
			
			move.setBoatTriumph(i+1, -1);
    	}
    	
    }
    
    public PLAYER previousPlayer(){
    	switch(curPlayer){
    		case P1: 
    			if(playerAlive.get(PLAYER.P4)){
    				return PLAYER.P4;
    			}else if(playerAlive.get(PLAYER.P3)){
    				return PLAYER.P3;
    			}else if(playerAlive.get(PLAYER.P2)){
    				return PLAYER.P2;
    			}else{
    				return PLAYER.INVALID_PLAYER;
    			}
    		case P2: 
    			if(playerAlive.get(PLAYER.P1)){
    				return PLAYER.P1;
    			}else if (playerAlive.get(PLAYER.P4)){
    				return PLAYER.P4;
    			}else if(playerAlive.get(PLAYER.P3)){
    				return PLAYER.P3;
    			}else{
    				return PLAYER.INVALID_PLAYER;
    			}
    		case P3: 
    			if(playerAlive.get(PLAYER.P2)){
    				return PLAYER.P2;
    			}else if(playerAlive.get(PLAYER.P1)){
    				return PLAYER.P1;
    			}else if(playerAlive.get(PLAYER.P4)){
    				return PLAYER.P4;
    			}else{
    				return PLAYER.INVALID_PLAYER;
    			}
    		case P4:
    			if(playerAlive.get(PLAYER.P3)){
    				return PLAYER.P3;
    			}else if(playerAlive.get(PLAYER.P2)){
    				return PLAYER.P2;
    			}else if(playerAlive.get(PLAYER.P1)){
    				return PLAYER.P1;
    			}else{
    				return PLAYER.INVALID_PLAYER;
    			}
    		default: {
    			return PLAYER.INVALID_PLAYER;
    		}
    	}
    }  


    public Map<PLAYER, Boolean> getAlivePlayers() {
    	return playerAlive;
    }
    
        
    // submitMove method for AI
    public boolean AIMove(){
    	Move move = ai.getMove();
        System.out.println("in AIMove, move is from " + move.getFromSq().toString() + " to " + move.getToSq().toString());
	return ai.aiMove(move);
    }
    
    public Move getAIMove(){
        return ai.getMove();
    }
    
    public boolean getGameOver(){
        return gameover;
    }
}

