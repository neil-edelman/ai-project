/** @author Neil */

/* packages confuse me */
package s110121860;

/* java stuff */
import java.lang.Exception;
import java.util.ArrayList; /* used in CCBoard as a return type */
import java.awt.Point;      /* used in CCMove as a return type */
import java.util.Iterator;

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
	private static final int param_diagonal      = 10;

	/* no move */
	private static CCMove done;

	/* convenient things */
	private boolean isInitialised = false;
	private boolean isEndingTurn = false;
	private CCBoard board; /* our copy */
	private int hill[][]       = new int[size][size];
	private Sequence[] myJumps = new Sequence[stones];
	private Iterator<CCMove> jumping;

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
		/* the optimum */
		CCMove best = null;
		Point from, to, myStone, myClosest = null;
		/* although you are allowed to pass in Halma, the game doesn't let
		 you :[; therefore we allow negative moves
		 (otherwise we would set them to zero) */
		int bestDeltaJump = Integer.MIN_VALUE;
		int bestDeltaJumpIndex = 0;
		int bestDelta     = Integer.MIN_VALUE;
		int delta, baseHeight, endHeight;

		assert(theboard != null);

		System.err.println("\n----\n" + this);

		/* this happens once the player_ids are set (chooseMove is called) */
		if(!isInitialised) {
			try {
				initialise();
			} catch(Exception e) {
				System.err.print("Error: " + e.getMessage() + "\n");
				return null;
			}
		}

		/* in the middle of jumping! */
		if(jumping != null) {
			/* cross my fingers */
			if(jumping.hasNext()) {
				System.err.print("Jumping on a pre-determined path!\n");
				best = jumping.next();
			} else {
				System.err.print("Stopping!\n");
				jumping = null;
				best = done;
			}
			System.err.print("Sending at jump: " + move2string(best) + ".\n----\n\n");
			return best;
		}

		/* update legal moves on this portion of the Sequence (whoa, polymorism) */
		CCBoard board = (CCBoard)theboard;
		ArrayList<CCMove> moves = board.getLegalMoves();
		ArrayList<Point>  myStones = board.getPieces(playerID);
		
		/* we just jumped one square, we should end our turn */
		if(isEndingTurn) {
			isEndingTurn = false;
			System.err.print("It's the end of our turn!?\n");
			if(moves.get(0).getTo() != null) {
				System.err.print("No! I'm confused, but will carry on.\n");
			} else {
				System.err.print("Sending at endturn: " + move2string(best) + ".\n----\n\n");
				return done;
			}
		}

		/* stroke the hill */
		/*for(int y = 0; y < size; y++) {
			for(int x = 0; x < size; x++) {
				hill[y][x] = constHill[y][x];
			}
		} <- didn't get there yet :[ */
		for(Point p : myStones) if(hill(p) > hill(myClosest)) myClosest = p;
		augmentHill(myClosest);

		/* output */
		System.err.println("Closest: " + pt2string(myClosest) + "\n");

		/* do single moves! */
		for(CCMove move : moves) {
			from = move.getFrom();
			to   = move.getTo();
			/* signals we can stop */
			if(from == null || to == null) {
				delta = 0;
			} else {
				delta = this.hill[to.y][to.x] - this.hill[from.y][from.x];
			}
			/* keep track of the max */
			if(bestDelta < delta) {
				bestDelta = delta;
				best = move;
				System.err.print("The current best delta is (" + move2string(move) + ") at " + delta + ".\n");
			}
		}

		/* do jump moves! */
		for(int i = 0; i < stones; i++) {
			myStone    = myStones.get(i);
			baseHeight = hill(myStone);
			myJumps[i] = Sequence.find(myStone, hill, board);
			endHeight  = myJumps[i].getHighest();
			delta      = endHeight - baseHeight;
			System.err.print("my #"+i+" is at " + pt2string(myStone) + " and has delta " + delta + " going " + myJumps[i] + ".\n");
			if(bestDeltaJump < delta) {
				bestDeltaJump      = delta;
				bestDeltaJumpIndex = i;
			}
		}
		System.err.print("The best delta_jump is #" + bestDeltaJumpIndex + " at " + bestDeltaJump + ".\n");

		/* we have two choices; be careful -- remember, it's not quite like real
		 halma where you can pass your turn, hence the second clause, where
		 we would get the smallest negiative value from the latter */
		if(bestDeltaJump > bestDelta && bestDeltaJump > 0) {
			System.err.print("The best delta is a jump!\n");
			jumping = myJumps[bestDeltaJumpIndex].iterator();
			assert(jumping.hasNext());
			best = jumping.next();
		} else {
			/* it is concivable that getLegalMoves returns empty, then we would be
			 up the creek, but hopefully it's good */
			if(best == null) best = done;
			isEndingTurn = true;
		}

		System.err.print("Sending at bottom: " + move2string(best) + ".\n----\n\n");
		return best;
	}

	/** we don't have the player id to fill this out at first, but it must be
	 filled out before we can play */
	private void initialise() throws Exception {
		Player p;
		int metric, manhattan;
		int q, r, s, t;

		/* generate a flag that tells the computer we're done */
		done = new CCMove(playerID, null, null);
		/* fixme! I don't have the internet :[ */
		//p = Player(playerID); lol
		switch(playerID) {
			case 0: p = Player.TOP_LEFT; break;
			case 1: p = Player.BOTTOM_LEFT; break;
			case 2: p = Player.TOP_RIGHT; break;
			case 3: p = Player.BOTTOM_RIGHT; break;
			default: throw new Exception("player ID was out of range");
		}
		Point b = p.oppositePoint();
		Point a = new Point();
		/* the distance to the goal */
		for(int y = 0; y < size; y++) {
			for(int x = 0; x < size; x++) {
				a.x = x;
				a.y = y;
				metric    = metric(a, b);
				manhattan = manhattan(a, b);
				/* the metric */
				hill[y][x]  = (15 - metric) * param_distance;
				/* augment squares in the goal zone to make them more attactive */
				if((metric <= 3) && (manhattan <= 4)) hill[y][x] += param_bonus_endzone;
				/* fixme: augment squares on the main diagonal dynanically
				 based on where you snake to */
				/* really conviluted way to tell diagonal */
				a.x += 1;
				q = metric(a, b);
				a.x -= 2;
				r = metric(a, b);
				a.x += 1;
				a.y += 1;
				s = metric(a, b);
				a.y -= 2;
				t = metric(a, b);
				a.y += 1;
				if(   (q == s) && (r == t)
				   || (q == t) && (r == s)) hill[y][x] += param_diagonal;
				System.err.printf("%3d", hill[y][x]);
			}
			System.err.printf("\n");
		}
		/* completed */
		isInitialised = true;

		System.err.print("Cool player on " + id2string(playerID) + " allied with " + id2string(playerID ^ 3) + ".\n");

	}

	Point a = new Point(), b = new Point(), c = new Point();

	/** this sets the hill to be a little bit steeper in certain points
	 that follow the path
	 @param p the players closest peice
	 fixme! todo! */
	private void augmentHill(Point p) {
		/* static Point a; ha it didn't like that */
		switch(playerID) {
			case 0:
				a.x = p.x + 1;
				a.y = p.y + 1; break;
			case 1:
			case 2:
			case 4:
		}
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

	/** lazy */
	private int hill(Point p) { return p != null ? hill[p.y][p.x] : Integer.MIN_VALUE; }

	/** prints the board (the right way around) */
	public String toString() {
		return "s110121860Player is " + id2string(playerID) + " #" + playerID + " on the board:\n" + board2string(board);
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
		return id2string(m.getPlayerID()) + ":" + pt2string(m.getFrom()) + "->" + pt2string(m.getTo());
	}

	/** an alternative representation where it equals the one shown graphically
	 (I was getting confused)
	 @param board
	 @return the string representation */
	public static String board2string(final CCBoard board) {

		if(board == null) return "(no board)";

		String s = "";

		Point p = new Point();
		Integer piece;
		for(int y = 0; y < size; y++) {
			for(int x = 0; x < size; x++) {
				p.x = y;
				p.y = x;
				piece = board.getPieceAt(p);
				/* autoboxing */
				s += (piece == null) ? "-" : piece;
			}
			s += "\n";
		}

		return s;
	}

	/* fixme: I'm messing it up */
	protected enum Player {
		TOP_LEFT(0),
		BOTTOM_LEFT(1),
		TOP_RIGHT(2),
		BOTTOM_RIGHT(3);

		private int v;

		private Player(int v) {
			this.v = v;
		}

		int value() {
			return v;
		}

		Point oppositePoint() {
			switch(v) {
				case 0: return new Point(size - 1, size - 1);
				case 1: return new Point(       0, size - 1);
				case 2: return new Point(size - 1, 0);
				case 3: return new Point(       0, 0);
			}
			return null;
		}

		/*public Enum<Player> player(int player) {
			return player;
		}*/
	};

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
