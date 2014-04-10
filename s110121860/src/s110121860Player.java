/** @author Neil */

/* packages confuse me */
package s110121860;

/* java stuff */
import java.util.LinkedList;
import java.util.Stack;
import java.lang.Exception;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

/* import game stuff */
import java.util.ArrayList; /* used in CCBoard as a return type */
import boardgame.Board;
import java.awt.Point;      /* used in CCMove as a return type */
import boardgame.Move;
import boardgame.Player;
import halma.CCBoard;
import halma.CCMove;

/** such confusion */
public class s110121860Player extends Player {

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

	/* the board is n x n */
	private static final int size   = CCBoard.SIZE /* 16 */;
	private static final int stones = 13;
	/* fixme: probably be better as an ArrayList */
	private static final int storedMoves = stones * 4 /* players */ * 2 /* just to be safe */;
	private static final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final int[][] moves = {{1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {0, 1}};

	/* the params */
	private static final int param_distance      = 1;
	private static final int param_bonus_endzone = 10;

	/* working dynimic variables */
	private static Point a = new Point(), b = new Point();
	/*private ArrayList<CCMove> tempMoves = new ArrayList<CCMove>(initialMoves);*/
	private CCMove storedMove[] = new CCMove[storedMoves];

	/* convenient things */
	private boolean isInitialised = false;
	// private boolean isMoving   = false; accessed from nextMoves.isEmpty()
	private int    move = 0;
	private Corner corner;
	private Corner ally;
	private Corner pieces[][] = new Corner[size][size];
	private int constHill[][] = new int[size][size];
	private int hill[][]      = new int[size][size];
	private LinkedList<CCMove> nextMoves = new LinkedList<CCMove>(); /* nextMoves picks from storedMove */
	private Point[] myStones  = new Point[stones];

	/* used at the end of jumps */
	private CCMove goNowhere;

	/** agent constructor with no args */
	public s110121860Player() {
		this("110121860");
	}

	/** agent constructor
	 @param s the name (I assume?) */
	public s110121860Player(String s) {
		super(s);
		for(int i = 0; i < stones; i++) myStones[i] = new Point();
	}

	/** @param pt
	 @return the letter mapped though some crazy transformation as a string */
	private static String pt2string(final Point pt) {
		assert(size < alphabet.length());
		if(pt == null) return "(null)";
		if(pt.x < 0 || pt.y < 0 || pt.x >= size || pt.y >= size) return "(range)";
		return "" + alphabet.charAt(pt.y) + (pt.x+1);
	}

	/** @param m move
	 @return the string representation of a move */
	private static String move2string(final CCMove m) {
		if(m == null) return "(null)";
		return pt2string(m.getFrom()) + "->" + pt2string(m.getTo());
	}

	/** @param a
	 @param b
	 @return the distance according to the metric */
	private static int metric(final Point a, final Point b) {
		return Math.max(Math.abs(b.x - a.x), Math.abs(b.y - a.y));
	}

	/** @param a
	 @param b Points
	 @return Manhattan distance between a and b */
	private static int manhattan(final Point a, final Point b) {
		int d = 0;
		d += (a.x < b.x) ? b.x - a.x : a.x - b.x;
		d += (a.y < b.y) ? b.y - a.y : a.y - b.y;
		//System.err.print("("+a.x+","+a.y+")-("+b.x+","+b.y+")="+d+"\n");
		return d;
	}

	/** we don't have the player id to fill this out at first, but it must be
	 filled out before we can play (probably best to . . . whatever) */
	private void initialise() {
		int metric, manhattan;

		/* the null move */
		goNowhere = new CCMove(playerID, null, null);
		/* localise! (fixme: too much space! do it in the Enum) */
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
		/* the distance to the goal */
		for(int y = 0; y < size; y++) {
			for(int x = 0; x < size; x++) {
				b.x = x;
				b.y = y;
				metric    = metric(a, b);
				manhattan = manhattan(a, b);
				/* the metric */
				constHill[y][x]  = (15 - metric) * param_distance;
				/* the goal zone is enhanced */
				if((metric <= 3) && (manhattan <= 4)) constHill[y][x] += param_bonus_endzone;
				System.err.printf("%3d", constHill[y][x]);
			}
			System.err.printf("\n");
		}
		/* move buffer */
		for(int i = 0; i < storedMoves; i++) storedMove[i] = new CCMove(playerID, new Point(), new Point());
		/* fixme: augment squares on the diagonal */
		/* fixme: */
		/* fixme: augment squares in the goal zone to make them more attactive */
		/* completed */
		isInitialised = true;

		System.err.print("Cool player on " + corner + " allied with " + ally + ".\n");

	}

	/** do something mysterious */
	public Board createBoard() {
		/* I have no idea what this does or why it's here */
		System.err.print("createBoard FTW!!!!\n");
		return new DDBoard();
	}

	/** chooseMove calls this each time */
	private void getPieces(CCBoard board) {
		Integer piece;
		Corner p;
		int my = 0;

		for(int y = 0; y < size; y++) {
			for(int x = 0; x < size; x++) {
				a.x = x;
				a.y = y;
				/* getPieces is good but it allocates memory every time */
				if((piece = board.getPieceAt(a)) == null) {
					p = Corner.NONE;
				} else {
					switch(piece) {
						case 0:    p = Corner.TOP_LEFT;     break;
						case 1:    p = Corner.BOTTOM_LEFT;  break;
						case 2:    p = Corner.TOP_RIGHT;    break;
						case 3:    p = Corner.BOTTOM_RIGHT; break;
						default:   p = Corner.NONE;         break;
					}
				}
				pieces[y][x] = p;
				/* save the index */
				if(p == corner) {
					assert(my < stones);
					myStones[my].x = x;
					myStones[my].y = y;
					my++;
				}
			}
		}
		for(int i = 0; i < my; i++) {
			System.err.print(pt2string(myStones[i]) + ";");
		}
		System.err.print("(" + my + ")\n");
		assert(my == stones);
	}

	/** prints the board */
	public String toString() {
		String s = "";
		for(int y = 0; y < size; y++) {
			for(int x = 0; x < size; x++) {
				/* this is messed up; first y and then x */
				s += "" + pieces[x][y].ordinal() + "";
			}
			s += "\n";
		}
		return s;
	}

	/** return the best moves for each player based on hill climbing */
	LinkedList<CCMove> bestSequence(final Point start) {
		int stored = 0;
		int currentBest;
		int x, y;

		//board.isLegal(CCMove);
		try {
			/*int dx = moves[i][0];
			int dy = moves[i][1];
			Point to=new Point(from.x+2*dx, from.y+2*dy);
			CCMove move=new CCMove(getTurn(), from, to);
			if(isLegal(move))
				legalMoves.add(move);
			
			if(start == null || pieces[start.y][start.x] != corner) throw new Exception("doesn't make sense in bestMove");
			x = start.x;
			y = start.y;
			if(x+1 < size) {
				if(pieces[y][x+1] == Corner.NONE) {
					pieces[y][x+1] = Corner.EXPLORED;
				}
			}*/
		} catch (Exception e) {
			System.err.print("No way! " + e.getMessage() + ".\n");
		}
		return null;
	}

	/** the agent */
	public Move chooseMove(Board theboard) {
		Point from, to;
		CCMove best = null;
		/* although you are allowed to pass in Halma, the game doesn't let
		 you :[; therefore we allow negaive moves */
		int bestDelta = Integer.MIN_VALUE /* 0 */, delta;

		/* assert input */
		if(theboard == null) return null;

		/* this happens once */
		if(!isInitialised) initialise();

		/* is doing a move? (this is a bad system :[ ) */
		for(CCMove move : storedMove) System.err.print(move2string(move) + ";");
		System.err.print("<- stored moves (" + storedMove.length + ")\n");

		/* this is sketchy */
		DDBoard board = (DDBoard)theboard;

		/* get the pieces (by going a long way around) */
		getPieces(board);

		System.err.println(this);

		/* get moves 1 square away */
		/*ArrayList<CCMove> moves = board.getLegalMoves();*/
		/* get all moves (fixme) */
		ArrayList<CCMove> moves = board.getLegalMoves();

		System.err.print("\nChoice of moves for player " + corner + " (#" + playerID + "):\n");

		/* stroke the hill */
		for(int y = 0; y < size; y++) {
			for(int x = 0; x < size; x++) {
				hill[y][x] = constHill[y][x];
			}
		}

		/* output */
		for(CCMove move : moves) {
			from = move.getFrom();
			to   = move.getTo();
			/* signals we can stop */
			if(from == null || to == null) {
				delta = 0;
			} else {
				delta = this.hill[to.y][to.x] - this.hill[from.y][from.x];
			}
			if(bestDelta < delta) {
				bestDelta = delta;
				best = move;
				System.err.print("the best so far ");
				System.err.print(move2string(move) + " [" + delta + "]\n");
			}
		}

		/* FIXME: uhhh, the null move is a very valid move, but it balks;
		 hopefully it doesn't come up (ie no moves possible) */
		return (best == null) ? goNowhere : best;
	}

	/** "Exception in s110121860.s110121860Player.choseMove()
	 java.lang.NullPointerException" okay */
	public Move choseMove() {
		System.err.print("s110121860Player: this is CRAZY!!!\n");
		return null;
	}

	/* these go with the class Sequence */
	private static boolean isVisited[][] = new boolean[size][size];
	protected CCMove consider              = new CCMove(playerID, new Point(), new Point());

	/** a sequence of CCMove moves */
	class Sequence {

		private Sequence parent;
		private int height;
		private Point here      = new Point();
		private CCMove buffer[] = new CCMove[8];
		private CCMove move[]   = new CCMove[8];

		/** cool sol'n but needs constant malloc/free;
		 fixme: have a pool of Sequences that gets allocated once */
		private Sequence(Sequence parent, Point point) {
			/* alloc memory :[ */
			for(int i = 0; i < 8; i++) buffer[i] = new CCMove(playerID, new Point(), new Point());
			/* fill in */
			this.parent = parent;
			/* visit it */
			this.here.x = point.x;
			this.here.y = point.y;
			this.height = hill[here.y][here.x];
			/* get neighbors */
			isVisited[here.y][here.x] = true;
			getAdjacent(point, 2);
			/* recurse in all (8) directions */
			for(int i = 0; i < 8; i++) {
				if(move[i] == null) continue;
				new Sequence(this, move[i].getTo());
			}
		}

		/** which move is best starting at start
		 @param start
		 @return a sequence of moves */
		public static int best(Point start) {

			/* clear out all isVisited */
			for(int y = 0; y < size; y++) {
				for(int x = 0; x < size; x++) {
					isVisited[y][x] = false;
				}
			}

			return new Sequence(null, start);
		}

		/** fills the move[]
		 @param from  the point that you want the adjecent
		 @param steps the 'steps' neighbor */
		private void getAdjacent(final Point from, final int steps) {
			int dx, dy;

			for(int i = 0; i < 8; i++) {
				dx = moves[i][0];
				dy = moves[i][1];
				a.x = from.x + steps * dx;
				a.y = from.y + steps * dy;
				consder.from = from;
				consder.to   = a;
				if(isLegal(consder) && !isVisited[a.y][a.x]) {
					move[i] = buffer[i];
					move[i].from.x = from.x;
					move[i].from.y = from.y;
					move[i].x = a.x;
					move[i].y = a.y;
					isVisited[a.y][a.x] = true;
				} else {
					move[i] = null;
				}
			}

		}
	}

	class Jump {
		/* very high bounds; it would be interesting to see what it actually is */
		private static final int maxMoves = size * size >> 2;
		CCMove buffer[] = new CCMove[maxMoves];
		LinkedList<CCMove> moves = new LinkedList<CCMove>();
		Stack<CCMove> stack = new Stack<CCMove>();
		Stack<CCMove> bestStack = new Stack<CCMove>();
		int bestDelta;

		public Jump() {
			//for(int i = 0; i < maxMoves; i++) buffer[i] = new CCMove(playerID, new Point(), new Point());
		}

		public void jump(final Point start) {
			Corner c;

			int x = start.x;
			int y = start.y;
			int bestValue = hill[y][x];
			int bestX = x;
			int bestY = y;
			if(--x >= 0) {
				c = pieces[y][x];
				if(c == Corner.NONE) {
					pieces[y][x] = Corner.EXPLORED;
					/*value = hill[y][x];
					if(value > bestValue) {
						
						bestX = x;
						bestY = y;
					}*/
				}
				if(--y >= 0) {
					
				}
			}
		}

		private void neighbors(final Point p) {
			int x = p.x - 2;
			int y = p.y;
			if(x-2 > 0 && pieces[y][x-2] == Corner.NONE && pieces[y][x-1] != Corner.NONE) {
				//tree.add();
			}
		}
	}
}

/* sometimes you just want to programme in C, you know . . . */
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
	public ArrayList<CCMove> getLegalMoves(){
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
