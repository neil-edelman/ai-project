/** @author Neil */

/* packages confuse me */
package s110121860;

/* java stuff */
import java.util.LinkedList;

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
		NONE
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

	/* the board is n x n (I assume the real variable is somewhere in the
	 bowels of the code, but I do not want to go searching though it)
	 assert(n < alpabet.size()) */
	private static final int size   = 16;
	private static final int stones = 13;
	private static final int bufMoves = stones * 4 /* players */ * 2 /* just to be safe */;
	private static final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	/* the params */
	private static final int param_distance = 3;

	/* working dynimic variables */
	private static Point a = new Point(), b = new Point();
	/*private ArrayList<CCMove> tempMoves = new ArrayList<CCMove>(initialMoves);*/
	private CCMove bufMove[] = new CCMove[bufMoves];

	/* convenient things */
	private boolean isInitialised = false;
	private boolean isMoving      = false;
	private int    move = 0;
	private Corner corner;
	private Corner ally;
	private Corner pieces[][] = new Corner[size][size];
	private int constHill[][] = new int[size][size];
	private int hill[][]      = new int[size][size];
	private LinkedList<CCMove> nextMoves;
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
		if(pt == null) return "(null)";
		if(pt.x < 0 || pt.y < 0 || pt.x >= size || pt.y >= size) return "(range)";
		return "" + alphabet.charAt(pt.y) + (pt.x+1);
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

		/* the null move */
		goNowhere = new CCMove(playerID, null, null);
		/* localise! */
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
				constHill[y][x] = -metric(a, b) * param_distance;
				System.err.printf("%3d", constHill[y][x]);
			}
			System.err.printf("\n");
		}
		/* move buffer */
		for(int i = 0; i < bufMoves; i++) bufMove[i] = new CCMove(playerID, new Point(), new Point());
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
		return new CCBoard();
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
				if(my < stones && p == corner) {
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

	/** the agent */
	public Move chooseMove(Board theboard) {
		Point from, to;
		CCMove best = null;
		/* although you are allowed to pass in Halma, the game doesn't let you :[ */
		int bestDelta = Integer.MIN_VALUE /* 0 */, delta;

		/* assert input */
		if(theboard == null) return null;

		/* this happens once */
		if(!isInitialised) initialise();

		/* is doing a move? (this is a bad system :[ ) */
		if(isMoving) {
			
		}

		/* this is sketchy */
		CCBoard board = (CCBoard)theboard;

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
			if(from == null || to == null) {
				System.err.print("s110121860Player: ridiculous!!!\n");
				return goNowhere;
			}
			delta = this.hill[to.y][to.x] - this.hill[from.y][from.x];
			if(bestDelta < delta) {
				bestDelta = delta;
				best = move;
				System.err.print("the best so far ");
			}
			System.err.print(pt2string(from) + " -> " + pt2string(to) + " [" + delta + "]\n");
		}

		/* FIXME: uhhh, the null move is a very valid move, but it balks;
		 hopefully it doesn't come up */
		return best;
	}

	/** "Exception in s110121860.s110121860Player.choseMove()
	 java.lang.NullPointerException" okay */
	public Move choseMove() {
		System.err.print("s110121860Player: this is CRAZY!!!\n");
		return null;
	}
}

/** no */
/*class P2D extends Point {
	@Override
	public String toString() {
		return "(" + x + "," + y + ")";
	}
}*/
