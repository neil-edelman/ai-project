/** @author Neil */

/** "ABORTING: Exception in s110121860.s110121860Player.choseMove()
 java.lang.NullPointerException" what does that mean? */

package s110121860;

/* package halma.CCMove; <- "is not public in halma.CCMove; cannot be accessed
 from outside package;" java packages; why do you have to be so confusing? */

import java.lang.Iterable;
import java.util.Iterator;
import java.lang.UnsupportedOperationException;
import java.util.NoSuchElementException;
import java.util.ArrayList; /* used in CCBoard as a return type */
import java.awt.Point;      /* used in CCMove as a return type */
import java.util.LinkedList;
import java.util.Stack;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;


import halma.CCBoard;
import halma.CCMove;

/* sometimes just I want to programme in C, you know . . . */

/* working dynimic variables */
/*private static Point a = new Point(), b = new Point();*/

/*
private Corner corner;
private Corner ally;
private Corner pieces[][] = new Corner[size][size];
private CCMove buffer[] = new CCMove[8]; :[
private Corner pieces[][] = new Corner[size][size];
 private LinkedList<CCMove> nextMoves = new LinkedList<CCMove>(); nextMoves picks from storedMove
//private Point[] myStones  = new Point[stones];
 private int    move = 0;
 */
/*private ArrayList<CCMove> tempMoves = new ArrayList<CCMove>(initialMoves);*/
//private CCMove storedMove[] = new CCMove[storedMoves];
//private static final int storedMoves = stones * 4 /* players */ * 2 /* just to be safe */;

class DDBoard extends CCBoard {

	/** we don't have the player id to fill this out at first, but it must be
	 filled out before we can play */
	/*private void initialise() {
		int metric, manhattan;
		
		*//* the null move *//*
		goNowhere = new CCMove(playerID, null, null);
		*//* localise! (fixme: too much space! do it in the Enum) *//*
		switch(playerID) {
			case 0:
				corner = Corner.TOP_LEFT;
				ally = Corner.BOTTOM_RIGHT;
				a.x = size - 1;
				a.y = size - 1;
				break;
			case 1:
				corner = Corner.BOTTOM_LEFT;
				ally   = Corner.TOP_RIGHT;
				a.x = size - 1;
				a.y = 0;
				break;
			case 2:
				corner = Corner.TOP_RIGHT;
				ally   = Corner.BOTTOM_LEFT;
				a.x = 0;
				a.y = size - 1;
				break;
			case 3:
				corner = Corner.BOTTOM_RIGHT;
				ally   = Corner.TOP_LEFT;
				a.x = 0;
				a.y = 0;
				break;
		}
		*//* the distance to the goal *//*
		for(int y = 0; y < size; y++) {
			for(int x = 0; x < size; x++) {
				b.x = x;
				b.y = y;
				metric    = metric(a, b);
				manhattan = manhattan(a, b);
				*//* the metric *//*
				constHill[y][x]  = (15 - metric) * param_distance;
				*//* augment squares in the goal zone to make them more attactive *//*
				if((metric <= 3) && (manhattan <= 4)) constHill[y][x] += param_bonus_endzone;
				*//* fixme: augment squares on the main diagonal? maybe not *//*
				System.err.printf("%3d", constHill[y][x]);
			}
			System.err.printf("\n");
		}
		*//* move buffer *//*
		for(int i = 0; i < storedMoves; i++) storedMove[i] = new CCMove(playerID, new Point(), new Point());
		*//* completed *//*
		isInitialised = true;
		
		System.err.print("Cool player on " + corner + " allied with " + ally + ".\n");
		
	}*/

	private static final int[][] moves= {{1,1}, {1,0}, {1,-1}, {0,-1}, {-1,-1}, {-1,0}, {-1,1}, {0,1}};
	
	public DDBoard() {
		super();
		System.err.print("DDBoard!!!!!!!!!!!!!!!\n");
	}
	@Override
	public Object clone() {
		return (DDBoard)super.clone();
		//return new DDBoard((HashMap<Point, Integer>) board.clone(), getTurnsPlayed(), getWinner(), getTurn(), getLastMoved(), (HashSet<Point>) lastPoints.clone());
	}
	
	/**
	 * Get all legal move for the current state of the board. 
	 * If the player is allowed to end his turn, then a move with from=null and to=null is included.
	 * NOTE: this will give moves for any player regardless of who calls this method.
	 * @return A list of all allowed moves for the current state of the board
	 * (Neil: ALL the moves)
	 */
	public ArrayList<CCMove> getLegalMoves(){
		ArrayList<CCMove> legalMoves= new ArrayList<CCMove>(10);
		Point lastMovedInTurn = getLastMoved();
		
		System.err.print("Yo!\n");
		if(lastMovedInTurn != null){
			/* if last move was a hop allow termination */
			legalMoves.add(new CCMove(getTurn(), null, null));
			
			/* allow all further hops with the same piece */
			Point from= lastMovedInTurn;
			for(int i=0;i<8; i++){
				int dx = moves[i][0];
				int dy = moves[i][1];
				Point to=new Point(from.x+2*dx, from.y+2*dy);
				CCMove move=new CCMove(getTurn(), from, to);
				if(isLegal(move))
					legalMoves.add(move);
			}
		}else{
			for( Entry<Point, Integer> entry: board.entrySet()){
				addLegalMoveForPiece(entry.getKey(), entry.getValue().intValue(), legalMoves);
			}
			if(checkIfWin(getTurn()))
				legalMoves.add(new CCMove(getTurn(), null, null));
		}
		return legalMoves;
	}

	/** (private access, copy here -Neil)
	 * Check if all player ID has all his pieces in his target corner
	 * @param ID player ID
	 * @return true if all pieces of player ID is in his target corner
	 */
	private boolean checkIfWin(int ID){
		assert(ID<4);
		boolean win=true;
		int base_id= ID^3;
		Integer IDInteger= new Integer(ID);
		
		for(Point p: bases[base_id]){
			win &= IDInteger.equals(board.get(p));
		}
		
		return win;
	}
}

class Jump {
	/* very high bounds; it would be interesting to see what it is actually */
	private static final int maxMoves = 64;//size * size >> 2;
	CCMove buffer[] = new CCMove[maxMoves];
	LinkedList<CCMove> moves = new LinkedList<CCMove>();
	Stack<CCMove> stack = new Stack<CCMove>();
	Stack<CCMove> bestStack = new Stack<CCMove>();
	int bestDelta;
	
	public Jump() {
		//for(int i = 0; i < maxMoves; i++) buffer[i] = new CCMove(playerID, new Point(), new Point());
		//for(int i = 0; i < stones; i++) myStones[i] = new Point();
	}
	
	/*public void jump(final Point start) {
		Corner c;
		
		int x = start.x;
		int y = start.y;
		int bestValue = 0;//hill[y][x];
		int bestX = x;
		int bestY = y;
		if(--x >= 0) {
			c = pieces[y][x];
			if(c == Corner.NONE) {
				pieces[y][x] = Corner.EXPLORED;
				value = hill[y][x];
				if(value > bestValue) {
					bestX = x;
					bestY = y;
				}
			}
			if(--y >= 0) {
				
			}
		}
	}*/
	
	private void neighbors(final Point p) {
		int x = p.x - 2;
		int y = p.y;
		//if(x-2 > 0 && pieces[y][x-2] == Corner.NONE && pieces[y][x-1] != Corner.NONE) {
			//tree.add();
		//}
	}

	/* an enum so that I know what the players are (fixme: could be improved) */
	enum Corner {
		TOP_RIGHT,
		TOP_LEFT,
		BOTTOM_LEFT,
		BOTTOM_RIGHT,
		NONE,
		EXPLORED
		/* fixme
		 public Corner(int confusing) {
		 switch(confusing) {
		 case 0: ordinal = Corner.TOP_LEFT; break;
		 case 1: ordinal = Corner.BOTTOM_LEFT; break;
		 case 2: ordinal = Corner.TOP_RIGHT; break;
		 case 3: ordinal = Corner.BOTTOM_RIGHT; break;
		 default: ordinal = Corner.NONE; break;
		 }
		 }*/
	};

}
