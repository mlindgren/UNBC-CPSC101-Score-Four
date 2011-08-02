package cpsc101.team3.scorefour;

/**
* This class describes Pegs on the board.
*/
public class Peg
{
    // Constants
    private static final int STACK_SIZE = 4;

    private Bead[] stack;     // array of beads
    private int numBeads = 0;     // how many bead the peg is holding
    private int[] myLocation;   // Location of peg on board
    private Board myBoard;    // copy of board

    /**
    * constructor--sets the x and y coordinates of peg on board and fills stack with null
    * @param b The Board the Peg is attached to
    * @param x The Peg's x-coordinate on the board
    * @param y The Peg's y-coordinate on the board
    */
    public Peg(Board b, int x, int y) {
        stack = new Bead[STACK_SIZE];
        myLocation = new int[2];

        myBoard = b;
        myLocation[0] = x;
        myLocation[1] = y;

        for (int i=0; i < stack.length; i++) { stack[i]=null; }
    }

    /**
    * takes in depth as an argument and returns the bead at that myLocation
    * @param depth The depth of the bead on the peg (0 is lowest; 3 highest.)
    */
    protected Bead getBead(int depth) { return stack[depth]; }

    /**
    * Returns the Board the current Peg is attached to.
    */
    protected Board getBoard()
    {
        return myBoard;
    }

    /**
    * returns the depth of the next bead to be dropped
    */
    protected int checkNextDrop() { return numBeads; }

    /**
    * checks if the peg is full, and if not, drops a bead on the lowest possible point.
    * @param c The color of the bead to be placed
    */
    protected Bead dropBead(Color c) throws PegFullException
    {
        Bead newBead = new Bead(this, c, numBeads);
        if (numBeads < stack.length)
        {
            stack[numBeads] = newBead;
            numBeads++;
        }
        else
        {
            throw new PegFullException(this);
        }

        return newBead;
    }


    /**
    * Checks if the Peg is full.
    */
    protected boolean checkFull()
    {
        if(numBeads == stack.length)
        {
            return true;
        }

        return false;
    }

    /**
    * returns the peg's "formatted" myLocation on the board as a string.
    * (Formatted meaning in the format "A4", etc.)
    */
    protected String getFormattedLocation() {
        int[] myLocation = getLocation();
        return Board.getStringFromCoords(myLocation[0], myLocation[1]);
    }

    /**
    * returns the peg's "unformatted" myLocation on the board as an array of
    * two integers.
    */
    protected int[] getLocation() { return this.myLocation; }
}
