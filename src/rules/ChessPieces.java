package rules;

//package Rules;


import java.util.HashSet;


public abstract class ChessPieces {
	protected PLAYER player;
	protected ChessBoard board;
	protected int score;
	
	public ChessPieces(ChessBoard b, PLAYER p) {
		board = b;
		player = p;
	}
	
	public PLAYER getPlayer() {
		return player;
	}
	
	public int getScore(){
		return score;
	}
	
	public abstract String getName();
	protected abstract void validMoves(ChessSquare sq, HashSet<ChessSquare> moves);
	
	public HashSet<ChessSquare> validMoves() {
		
		// Get the position of the piece on the board
	    ChessSquare position = board.positionOf(this);
	    HashSet<ChessSquare> moves = new HashSet<ChessSquare>();
	    // If the piece is on the board call the virtual function that return the set of valid moves
	    if (position.valid()) validMoves(position, moves);
	    
	    return moves;
	}
	
	protected void addValidMove(ChessSquare sq, HashSet<ChessSquare> moves) {
		if (sq.valid() && (!board.occupied(sq) || board.occupiedByOpponent(sq, player)))
			//System.out.println(board.occupied(sq));
			moves.add(sq);
	}
}


class King extends ChessPieces {
	
	public King(ChessBoard b, PLAYER p) {
		super(b,p);
		score=5;
	}
	
	public String getName() {
		return "King";
	}
	
	public void validMoves(ChessSquare sq, HashSet<ChessSquare> moves) {
		
		addValidMove(sq.up(), moves);
	    addValidMove(sq.down(), moves);
	    addValidMove(sq.left(), moves);
	    addValidMove(sq.right(), moves);
	    addValidMove(sq.upper_left(), moves);
	    addValidMove(sq.upper_right(), moves);
	    addValidMove(sq.lower_left(), moves);
	    addValidMove(sq.lower_right(), moves);
	}
}


class Knight extends ChessPieces {
	
	public Knight(ChessBoard b, PLAYER p) {
		super(b,p);
		score=3;
	}
	
	public String getName() {
		return "Knight";
	}
	
	public void validMoves(ChessSquare sq, HashSet<ChessSquare> moves) {
		
		addValidMove(sq.upper_left().up(), moves);
	    addValidMove(sq.upper_left().left(), moves);
	    addValidMove(sq.upper_right().up(), moves);
	    addValidMove(sq.upper_right().right(), moves);
	    addValidMove(sq.lower_left().down(), moves);
	    addValidMove(sq.lower_left().left(), moves);
	    addValidMove(sq.lower_right().down(), moves);
	    addValidMove(sq.lower_right().right(), moves);
	}
}

class Pawn extends ChessPieces {
	
	public Pawn(ChessBoard b, PLAYER p) {
		super(b,p);
		score=1;
	}
	
	public String getName() {
		return "Pawn";
	}
	
	public void validMoves(ChessSquare sq, HashSet<ChessSquare> moves) {
		
		switch (player)
	    {
	        case P1:
	        {
	            if (sq.left().valid() && !board.occupied(sq.left()))
	                moves.add(sq.left());
	          
	            if (sq.upper_left().valid() && board.occupiedByOpponent(sq.upper_left(), player))
	                moves.add(sq.upper_left());
	            if (sq.lower_left().valid() && board.occupiedByOpponent(sq.lower_left(), player))
	                moves.add(sq.lower_left());
	            break;
	        }

	        case P2:
	        {
	            if (sq.up().valid() && !board.occupied(sq.up()))
	                moves.add(sq.up());
	          
	            if (sq.upper_left().valid() && board.occupiedByOpponent(sq.upper_left(), player))
	                moves.add(sq.upper_left());
	            if (sq.upper_right().valid() && board.occupiedByOpponent(sq.upper_right(), player))
	                moves.add(sq.upper_right());
	            break;
	        }
	        
	        case P3:
	        {
	            if (sq.right().valid() && !board.occupied(sq.right()))
	                moves.add(sq.right());
	          
	            if (sq.lower_right().valid() && board.occupiedByOpponent(sq.lower_right(), player))
	                moves.add(sq.lower_right());
	            if (sq.upper_right().valid() && board.occupiedByOpponent(sq.upper_right(), player))
	                moves.add(sq.upper_right());
	            break;
	        }
	        
	        case P4:
	        {
	            if (sq.down().valid() && !board.occupied(sq.down()))
	                moves.add(sq.down());
	          
	            if (sq.lower_left().valid() && board.occupiedByOpponent(sq.lower_left(), player))
	                moves.add(sq.lower_left());
	            if (sq.lower_right().valid() && board.occupiedByOpponent(sq.lower_right(), player))
	                moves.add(sq.lower_right());
	            break;
	        }	        
	        
	    }
	}
}


class Elephant extends ChessPieces {
	
	public Elephant(ChessBoard b, PLAYER p) {
		super(b,p);
		score=4;
	}
	
	public String getName() {
		return "Elephant";
	}
	
	public void validMoves(ChessSquare sq, HashSet<ChessSquare> moves) {
		
		ChessSquare tmp = sq;
	    do {
	        tmp = tmp.up();
	        addValidMove(tmp, moves);
	    } while (tmp.valid() && !board.occupied(tmp));

	    tmp = sq;
	    do {
	        tmp = tmp.down();
	        addValidMove(tmp, moves);
	    } while (tmp.valid() && !board.occupied(tmp));

	    tmp = sq;
	    do {
	        tmp = tmp.left();
	        addValidMove(tmp, moves);
	    } while (tmp.valid() && !board.occupied(tmp));

	    tmp = sq;
	    do {
	        tmp = tmp.right();
	        addValidMove(tmp, moves);
	    } while (tmp.valid() && !board.occupied(tmp));
	}

}

class Boat extends ChessPieces {
	
	public Boat(ChessBoard b, PLAYER p) {
		super(b,p);
		score=2;
	}
	
	public String getName() {
		return "Boat";
	}
	
	public void validMoves(ChessSquare sq, HashSet<ChessSquare> moves) {
		
		addValidMove(sq.upper_left().upper_left(), moves);
	    addValidMove(sq.upper_right().upper_right(), moves);
	    addValidMove(sq.lower_left().lower_left(), moves);
	    addValidMove(sq.lower_right().lower_right(), moves);
	}
}








