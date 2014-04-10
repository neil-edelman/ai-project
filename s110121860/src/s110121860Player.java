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
				/* augment squares in the goal zone to make them more attactive */
				if((metric <= 3) && (manhattan <= 4)) constHill[y][x] += param_bonus_endzone;
				/* fixme: augment squares on the main diagonal? maybe not */
				System.err.printf("%3d", constHill[y][x]);
			}
			System.err.printf("\n");
		}
		/* move buffer */
		for(int i = 0; i < storedMoves; i++) storedMove[i] = new CCMove(playerID, new Point(), new Point());
		/* the Sequence now has enough info to store all it's stuff */
		//Sequence.initialise(playerID);
		/* completed */
		isInitialised = true;

		System.err.print("Cool player on " + corner + " allied with " + ally + ".\n");

	}

	/** do something mysterious */
	public Board createBoard() {
		/* I have no idea what this does or why it's here */
		System.err.print("createBoard FTW!!!!\n");
		return new CCBoard();//DDBoard();
	}

	/** chooseMove calls this each time */
	private void updatePieces(CCBoard board) {
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
					/* fixme: this should go in the Enum constructor */
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

		/* whoa, polymorism */
		CCBoard board = (CCBoard)theboard; //DDBoard board = (DDBoard)theboard;

		/* update legal moves on this portion of the Sequence
		 (don't be fooled by the get) */
		ArrayList<CCMove> moves = board.getLegalMoves();
		updatePieces(board);

		/* stroke the hill */
		for(int y = 0; y < size; y++) {
			for(int x = 0; x < size; x++) {
				hill[y][x] = constHill[y][x];
			}
		}

		System.err.println(this);

		Sequence zero = Sequence.find(myStones[0], hill, board);

		System.err.print("Choose move for player " + corner + " (#" + playerID + "):\n");

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

}
