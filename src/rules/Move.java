package rules;

//package Rules;

import java.util.HashMap;
import java.util.Map;



public class Move {
	
	//public String from;
	//public String to;

	private ChessSquare fromSq;
	private ChessSquare toSq;
	
	private PLAYER prevPlayer;
	private ChessPieces capturedPiece = null;
	private PLAYER deadPlayer = null;
    private int[] boatTriumph = new int[] {-1,-1,-1,-1};
    private Map<ChessSquare,Integer> promotions = new HashMap<ChessSquare, Integer>();
    private boolean gameOver = false;
	
	public Move(String _from, String _to) {
		this.fromSq = new ChessSquare(_from);
		this.toSq = new ChessSquare(_to);
		
	}
	
	public void setFromSq(ChessSquare fsq) {
		fromSq = fsq;
	}

	public void setToSq(ChessSquare tsq) {
		toSq = tsq;
	}
	
	
	public ChessSquare getFromSq() {
		return this.fromSq;
	}

	public ChessSquare getToSq() {
		return this.toSq;
	}
	
	public Move clone(){
		return new Move(fromSq.toString(), toSq.toString());
	}
	
	public void setCapturedPiece(ChessPieces cp) {
		capturedPiece = cp;
	}
	
	public ChessPieces getCapturedPiece(){
		return capturedPiece;
	}
	
	public void setDeadPlayer(PLAYER p) {
		deadPlayer = p;
	}
	public PLAYER getDeadPlayer() {
		return deadPlayer;
	}
	
	public PLAYER getPrevPlayer() {
		return prevPlayer;
	}
	
	public void setPrevPlayer(PLAYER _prevPlayer) {
		prevPlayer = _prevPlayer;
	}
	
	public void setBoatTriumph(int index, int new_val) {
		boatTriumph[index] = new_val;
	}
	
	public int[] getBoatTriumph() {
		return boatTriumph;
	}
	public void setPawnPromotion(ChessSquare sqr, int new_val) {
		promotions.put(sqr, new_val);
	}
	public Map<ChessSquare, Integer> getPawnPromotion() {
		return promotions;
	}
	
	public void setGameOver() {
		gameOver = true;
	}
	
	public boolean getGameOver() {
		return gameOver;
	}

}
