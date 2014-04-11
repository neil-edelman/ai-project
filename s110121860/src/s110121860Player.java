/** @author Neil */

/* packages confuse me */
package s110121860;

/* java stuff */
import java.lang.Exception;
import java.util.ArrayList; /* used in CCBoard as a return type */
import java.awt.Point;      /* used in CCMove as a return type */

/* import game stuff */
import boardgame.Board;
import boardgame.Move;
import boardgame.Player;
import halma.CCBoard;
import halma.CCMove;

/** such confusion */
public class s110121860Player extends Player {

	/* convenient constants */
	private static final int size        = CCBoard.SIZE; /* 16 */
	private static final int stones      = 13; /* CCBoard.basePoints.lenght */
	private static final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	/* the params for hill-climbing */
	private static final int param_distance      = 1;
	private static final int param_bonus_endzone = 10;

	/* convenient things */
	private boolean isInitialised = false;
	private CCBoard board; /* our copy */
	private int constHill[][]  = new int[size][size];
	private int hill[][]       = new int[size][size];
	private Sequence[] myJumps = new Sequence[stones];

	/** agent constructor with no args */
	public s110121860Player() {
		this("110121860");
	}

	/** agent constructor
	 @param s the name (I assume?) */
	public s110121860Player(String s) {
		super(s);
	}

	/** the agent
	 @param theboard the CCBoard as a Board
	 @return the next CCMove as a Move */
	public Move chooseMove(Board theboard) {
		Point from, to, myStone;
		CCMove best = null;
		int height;
		/* although you are allowed to pass in Halma, the game doesn't let
		 you :[; therefore we allow negative moves */
		int bestDelta = Integer.MIN_VALUE /* 0 */, delta;

		assert(theboard != null);

		/* this happens once the player_ids are set */
		if(!isInitialised) initialise();

		/* whoa, polymorism */
		CCBoard board = (CCBoard)theboard; //DDBoard board = (DDBoard)theboard;

		/* update legal moves on this portion of the Sequence
		 (don't be fooled by the get) */
		ArrayList<CCMove> moves = board.getLegalMoves();
		ArrayList<Point>  myStones = board.getPieces(playerID);

		/* stroke the hill () */
		for(int y = 0; y < size; y++) {
			for(int x = 0; x < size; x++) {
				hill[y][x] = constHill[y][x];
			}
		}

		System.err.println(this);

		/* do jump moves! */
		for(int i = 0; i < stones; i++) {
			myStone = myStones.get(i);
			myJumps[i] = Sequence.find(myStone, hill, board);
			height = myJumps[i].getHighest();
			System.err.print("my #"+i+" is at " + pt2string(myStone) + " and has max height " + height + " going though " + myJumps[i] + ".\n");
		}

		System.err.print("Choose move for player " + id2string(playerID) + " (#" + playerID + "):\n");

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
		return (best == null) ? new CCMove(playerID, null, null) : best;
	}

	/** we don't have the player id to fill this out at first, but it must be
	 filled out before we can play */
	private void initialise() {
		int metric, manhattan;
		Point a = new Point();
		Point b = new Point();

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
		/* completed */
		isInitialised = true;

		System.err.print("Cool player on " + id2string(playerID) + " allied with " + id2string(playerID ^ 3) + ".\n");

	}

	/** do something mysterious */
	public Board createBoard() {
		System.err.print("createBoard FTW!!!!\n");
		board = new CCBoard();
		return board;
	}

	/** @param a
	 @param b
	 @return the distance according to the metric of one-square jumping */
	private static int metric(final Point a, final Point b) {
		return Math.max(Math.abs(b.x - a.x), Math.abs(b.y - a.y));
	}

	/** @param a
	 @param b Points
	 @return Manhattan distance between a and b
	 @depreciated now that I realise that the CCBoard has the ending zones */
	private static int manhattan(final Point a, final Point b) {
		int d = 0;
		d += (a.x < b.x) ? b.x - a.x : a.x - b.x;
		d += (a.y < b.y) ? b.y - a.y : a.y - b.y;
		//System.err.print("("+a.x+","+a.y+")-("+b.x+","+b.y+")="+d+"\n");
		return d;
	}

	/** prints the board (the right way around) */
	public String toString() {
		return "s110121860Player is " + playerID + " on the board:\n" + board2string(board);
	}

	/** @param pt
	 @return the letter mapped though some crazy transformation as a string */
	public static String pt2string(final Point pt) {
		assert(size < alphabet.length());
		if(pt == null) return "(null)";
		if(pt.x < 0 || pt.y < 0 || pt.x >= size || pt.y >= size) return "(range)";
		return "" + alphabet.charAt(pt.y) + (pt.x+1);
	}

	/** @param m move
	 @return the string representation of a move */
	public static String move2string(final CCMove m) {
		if(m == null) return "(null)";
		return m.getPlayerID() + ":" + pt2string(m.getFrom()) + "->" + pt2string(m.getTo());
	}

	/** I was fed up with it being backwards
	 @param board
	 @return the string representation */
	public static String board2string(final CCBoard board) {

		if(board == null) return "(no board)";

		String s = "";

		Point p = new Point();
		Integer piece;
		for(int y = 0; y < size; y++) {
			for(int x = 0; x < size; x++) {
				p.x = x;
				p.y = y;
				piece = board.getPieceAt(p);
				/* autoboxing */
				s += (piece == null) ? "-" : piece;
			}
			s += "\n";
		}

		return s;
	}

	/** @param id a player id
	 @return string representing where the player is sitting */
	public static String id2string(final int id) {
		switch(id) {
			case 0: return "top-left";
			case 1: return "bottom-left";
			case 2: return "top-right";
			case 3: return "bottom-right";
			default: return "" + id + "?";
		}
	}
}
