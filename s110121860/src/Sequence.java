/** @author Neil */

package s110121860;

import java.lang.Iterable;
import java.util.Iterator;
import java.lang.UnsupportedOperationException;
import java.util.NoSuchElementException;
import java.awt.Point;

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
	private static Sequence highestSoFar;

	/* class variables */
	private Sequence parent;
	private int dirFromParent;
	private int height;
	private Point here    = new Point();
	private CCMove move[] = new CCMove[8];
	/* the highest in the subtree rooted at this */
	private Sequence highest;

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
		if(highestSoFar == null || highestSoFar.height < height) highestSoFar = this;
		this.highest = highestSoFar;
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
		/*System.err.print("id#"+playerID+" board \\/\n"
						 +s110121860Player.board2string(board)
						 +"start: "
						 +s110121860Player.pt2string(start)+"\n");*/

		/* clear out all isVisited */
		for(int y = 0; y < size; y++) {
			for(int x = 0; x < size; x++) {
				isVisited[y][x] = false;
			}
		}

		/* reset highest */
		highestSoFar = null;

		/* do searching! */
		return new Sequence((Sequence)null, 0, start);
	}

	/** @return the highest jump we did on the last find()
	 @depreciated use getHighest */
	public static int highest() {
		if(highestSoFar == null) return Integer.MIN_VALUE; // shouldn't happen
		return highestSoFar.height;
	}

	/** @return the highest point in the Sequence */
	public int getHighest() { return this.highest.height; }

	/** @return iterator to the highest peak */
	public Iterator<CCMove> iterator() {
		assert(highest != null);
		return new SequenceIterator(this, highest);
	}

	/* this is the iterator */
	class SequenceIterator implements Iterator<CCMove> {
		Sequence it;
		Sequence goal;
		/** the iterator starts at the current node (the root, one assumes)
		 and is trying to get to the some node (ostensibly the highest) */
		public SequenceIterator(final Sequence start, final Sequence goal) {
			this.it   = start;
			this.goal = goal;
			
		}
		/** while it's not there */
		public boolean hasNext() { return !(goal == it); }
		/** the moves that you can do is bounded by a reasonable number, so I
		 wouldn't worry about this taking O(n) */
		public CCMove next() throws NoSuchElementException {
			Sequence i;
			CCMove move;
			for(i = goal; i != null && i.parent != it; i = i.parent);
			if(i == null) throw new NoSuchElementException("not connected");
			move = it.move[i.dirFromParent];
			it = i;
			return move;
		}
		public void remove() { throw new UnsupportedOperationException("can't do that"); }
	}

	/** fills the move[] with values from the adjacent nodes steps steps away
	 @param from  the value associtated with this Sequence
	 @param steps */
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

	/** CCBoard.isLegal stripped down so that it actually (maybe) works */
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
		String s = "Sequence: ";
		Iterator i = this.iterator();
		/* i.next() is Object? but that doesn't make any sense, clearly the docs
		 say: Iterator<E>: E next() */
		while(i.hasNext()) s += s110121860Player.move2string((CCMove)i.next()) + "; ";
		s += "done";
		return s;
	}
}
