package rules;

//package Rules;


public class ChessSquare {
	private char row;
	private char col;
	
	public ChessSquare(String sqPosition) {
		this.row = sqPosition.charAt(1);
		this.col = sqPosition.charAt(0);
	}
	
	public ChessSquare(char _row, char _col) {
		this.row = _row;
		this.col = _col;
	}
	
	public boolean valid() {
		return (row >= '1') && (row <= '8') && (col >= 'A') && (col <= 'H');
	}
	
	public char getRow() {
		return row;
	}
	
	public char getCol() {
		return col;
	}
	
	public ChessSquare left() {
		return (new ChessSquare(row, (char)(col -1)));
	}
	
	public ChessSquare right()
	{
	    return (new ChessSquare(row, (char)(col+1)));
	}

	public ChessSquare up()
	{
	    return (new ChessSquare((char)(row+1), col));
	}

	public ChessSquare down()
	{
	    return (new ChessSquare((char)(row-1), col));
	}

	public ChessSquare upper_left()
	{
	    return (new ChessSquare((char)(row+1), (char)(col-1)));
	}

	public ChessSquare upper_right()
	{
	    return (new ChessSquare((char)(row+1), (char)(col+1)));
	}

	public ChessSquare lower_left()
	{
	    return (new ChessSquare((char)(row-1), (char)(col-1)));
	}

	public ChessSquare lower_right()
	{
	    return (new ChessSquare((char)(row-1), (char)(col+1)));
	}
	
	/* not sure about it */
	public static ChessSquare invalidSquare() {
		return (new ChessSquare('0','0'));
	}
	
	public String toString()
	{
	    return ( col + "" +  row);
	}
	
	// overrides the equals() method of the main java Object, used for comparisons in collections
	@Override public boolean equals(Object other) {
	    boolean result = false;
	    if (other instanceof ChessSquare) {
	        ChessSquare that = (ChessSquare) other;
	        result = (this.getRow() == that.getRow() && this.getCol() == that.getCol());
	    }
	    return result;
	}
	
	// needed for comparison operations in hashed data structures
    @Override public int hashCode() {
        return (41 * (41 + (int)getRow()) + (int)getCol());
    }
	
}
