/** @author Neil */

package s110121860;
/* package halma.CCMove; <- "is not public in halma.CCMove; cannot be accessed
 from outside package;" java packages; why do you have to be so confusing? */

import java.awt.Point;
//import java.util.LinkedList;

import halma.CCBoard;
import halma.CCMove;

/** a sequence of CCMove moves and stuff */
class Sequence implements Iterable {

	/* constants */
	private static final int size      = CCBoard.SIZE /* 16 */;
	private static final int[][] moves = {{1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {0, 1}};

	/* "globals" */
	private static boolean isVisited[][] = new boolean[size][size];
	private static Point to              = new Point();
	private static Point mid             = new Point();
	private static int playerID;
	private static CCBoard board;
	private static int hill[][];
	//private static LinkedList<CCMove> path = new LinkedList<CCMove>();
	private static Sequence highest;

	/* class variables */
	private Sequence parent;
	private int dirFromParent;
	private int height;
	private Point here = new Point();
	/* private CCMove buffer[] = new CCMove[8]; :[ */
	private CCMove move[] = new CCMove[8];

	/** cool sol'n but needs constant malloc/free;
	 fixme: have a pool of Sequences */
	private Sequence(final Sequence parent, int dirFromParent, final Point point) {
		/* allocate */
		//for(int i = 0; i < 8; i++) buffer[i] = new CCMove(playerID, new Point(), new Point()); :[
		/* fill in */
		this.parent = parent;
		this.dirFromParent = dirFromParent;
		/* visit it */
		this.here.x = point.x;
		this.here.y = point.y;
		this.height = hill[here.y][here.x];
		/* get neighbors (sets move[]) */
		isVisited[here.y][here.x] = true;
		getAdjacent(point, 2);
		/* recurse in all (8) directions; fixme: bfs is way nicer */
		for(int i = 0; i < 8; i++) {
			if(move[i] == null) continue;
			//System.err.print("new Sequence("+this+", "+move[i].getTo()+");\n");
			new Sequence(this, i, move[i].getTo());
		}
		/* is it the highest we've seen? */
		if(highest == null || highest.height < height) highest = this;
	}

	/** return the bfs (fixme) with all the sequences starting at a peice
	 @param start
	 @param player_id the id of the player
	 @return a sequence of moves */
	public static Sequence find(Point start, final int hill[][], final CCBoard board) {
		Integer player;

		assert(start != null);
		assert(hill != null);
		assert(board != null);

		/* autoboxing */
		Integer playerID = board.getPieceAt(start);
		assert(playerID != null && playerID >= 0 && playerID < CCBoard.NUMBER_OF_PLAYERS);

		Sequence.playerID = playerID;
		Sequence.board    = board;
		Sequence.hill     = hill;
		//System.err.print("id#"+playerID+" board \\/\n"+board+"start: "+start+"\n");

		/* clear out all isVisited */
		for(int y = 0; y < size; y++) {
			for(int x = 0; x < size; x++) {
				isVisited[y][x] = false;
			}
		}

		/* reset highest */
		highest = null;

		/* do searching! */
		return new Sequence((Sequence)null, 0, start);
	}

	/** @return the highest jump we did on the last find() */
	public static int highest() {
		if(highest == null) return Integer.MIN_VALUE; // shouldn't happen
		return highest.height;
	}

	/** @return iterator to the highest peak */
	public Iterator . . . muhahahahahahaha

	/** fills the move[]
	 @param from  the point that you want the adjecent
	 @param steps the 'steps' neighbor */
	private void getAdjacent(final Point from, final int steps) {
		CCMove consider;
		int dx, dy;

		for(int i = 0; i < 8; i++) {
			dx = moves[i][0];
			dy = moves[i][1];
			to.x = from.x + steps * dx;
			to.y = from.y + steps * dy;
			/* the only way to access it is from the constructor :[ */
			consider = new CCMove(playerID, from, to);
			/*board.isLegal(consider) <- useless */
			if(isLegal(consider) && !isVisited[to.y][to.x]) {
				/*move[i] = buffer[i];
				move[i].from.x = from.x;
				move[i].from.y = from.y;
				move[i].x = to.x;
				move[i].y = to.y; :[ */
				move[i] = new CCMove(playerID, new Point(from), new Point(to)); /* :{{{ */
				isVisited[to.y][to.x] = true;
			} else {
				move[i] = null;
			}
		}
	}

	/** maybe this works */
	private boolean isLegal(final CCMove m) {
		if(m == null) return false;
		Point to = m.getTo();
		if(to.x < 0 || to.y < 0 || to.x >= size || to.y >= size) {
			//System.err.print("["+s110121860Player.move2string(m)+" outofbounds]");
			return false;
		}
		/* we want the to position to be empty */
		if(board.board.containsKey(to)) {
			//System.err.print("["+s110121860Player.move2string(m)+" blocked]");
			return false;
		}
		/* we assume the from is okay */
		Point from = m.getFrom();
		/* we have to have a piece in the centre */
		mid.x = from.x + to.x >> 1;
		mid.y = from.y + to.y >> 1;
		if(!board.board.containsKey(mid)) {
			//System.err.print("["+s110121860Player.move2string(m)+" nohop]");
			return false;
		}
		/* the game does not allow going out the opposing team's base; really?
		 we could get easily trapped! */
		boolean toIn   = board.bases[playerID ^ 3].contains(to);
		boolean fromIn = board.bases[playerID ^ 3].contains(from);
		if(!(!fromIn || (toIn && fromIn) )) {
			//System.err.print("["+s110121860Player.move2string(m)+" noleave]");
			return false;
		}
		/* it must be ones turn */
		if(m.getPlayerID() != playerID /* or we assume board.getTurn() */) {
			//System.err.print("["+s110121860Player.move2string(m)+" notturn]");
			return false;
		}
		return true;
	}

	public String toString() {
		String s = "";
		for(int i = 0; i < 8; i++) s += "(" + moves[i][0] + "," + moves[i][1] + "): " + s110121860Player.move2string(move[i]) + "|";
		return s;
	}
}

/* not used; sometimes just I want to programme in C, you know . . . */

class DDBoard extends CCBoard {
	
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
	/*public ArrayList<CCMove> getLegalMoves(){
		ArrayList<CCMove> legalMoves= new ArrayList<CCMove>(10);
		Point lastMovedInTurn = getLastMoved();
		
		System.err.print("Yo!\n");
		if(lastMovedInTurn != null){
			// if last move was a hop allow termination
			legalMoves.add(new CCMove(getTurn(), null, null));
			
			// allow all further hops with the same piece
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
	}*/
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
	/* very high bounds; it would be interesting to see what it actually is */
	private static final int maxMoves = 64;//size * size >> 2;
	CCMove buffer[] = new CCMove[maxMoves];
	//LinkedList<CCMove> moves = new LinkedList<CCMove>();
	//Stack<CCMove> stack = new Stack<CCMove>();
	//Stack<CCMove> bestStack = new Stack<CCMove>();
	int bestDelta;

	public Jump() {
		//for(int i = 0; i < maxMoves; i++) buffer[i] = new CCMove(playerID, new Point(), new Point());
	}

	public void jump(final Point start) {
		//Corner c;
		
		int x = start.x;
		int y = start.y;
		int bestValue = 0;//hill[y][x];
		int bestX = x;
		int bestY = y;
		if(--x >= 0) {
			/*c = pieces[y][x];
			if(c == Corner.NONE) {
				//pieces[y][x] = Corner.EXPLORED;
				value = hill[y][x];
				 if(value > bestValue) {
				 
				 bestX = x;
				 bestY = y;
				 }
			}*/
			if(--y >= 0) {
				
			}
		}
	}

	private void neighbors(final Point p) {
		int x = p.x - 2;
		int y = p.y;
		/*if(x-2 > 0 && pieces[y][x-2] == Corner.NONE && pieces[y][x-1] != Corner.NONE) {
			//tree.add();
		}*/
	}
}
