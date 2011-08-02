package cpsc101.team3.scorefour;

public class Board
{

    private Peg[][] pegGrid;  // a 2-d array of pegs
    private Bead lastBead;    // stores a reference to the last bead played

    // constructor--loads 16 pegs
    public Board() {
        pegGrid = new Peg[4][4];
        loadPegs();
    }

    public Board(int egg)
    {
        // Call other constructor
        this();

        System.out.println("Called with egg");

        if(egg != 0)
        {
            System.out.println("CPSC 101 Score Four game by Team 3, W2008");
        }
    }

    // loads 16 new pegs
    public void loadPegs() {
        for(int x = 0; x<4; x++) {
            for (int y = 0; y<4; y++) {
                pegGrid[x][y] = new Peg(this, x, y);
            }
        }
    }

    // returns specific peg
    public Peg getPeg(int x, int y) { return pegGrid[x][y]; }

    // puts bead on peg
    public int placeBead (String position, Color c) throws PegFullException{
        int[] coords = getCoordsFromString(position);
        /* System.out.println(coords[0]);
        System.out.println(coords[1]);
        System.out.println(pegGrid[coords[0]][coords[1]]); */ // Debug
        lastBead = pegGrid[coords[0]][coords[1]].dropBead(c);

        return lastBead.getDepth();
    }

    // puts bead on peg
    public int placeBead(int x, int y, Color c) throws PegFullException
    {
        lastBead = pegGrid[x][y].dropBead(c);

        return lastBead.getDepth();
    }

    public GameState checkGame() {

        // returns GameState.WIN for win if checkWin() returns true
        if (lastBead.checkWin()) { return GameState.WIN; }

        // cycles through pegs checking if they are full
        // returns GameState.CONTINUE for no if at least one peg is empty
        for(int x = 0; x<4; x++) {
            for (int y = 0; y<4; y++) {
                if(!getPeg(x, y).checkFull()) {
                    return GameState.CONTINUE;
                }
            }
        }
        // if it makes it this far then all pegs are full so draw
        return GameState.DRAW;
    }

    /**
    * Takes a string in the format A4 and returns an array
    * of integer coordinates.
    * @param str The string to get coordinates from
    */
    public static int[] getCoordsFromString(String str)
    {
        // Convert it to lower case first, and create the array
        str = str.toLowerCase();
        int coords[] = new int[2];

        // Parse the first coordinate from the letter
        // by subtracting the numeric value for 'a' (10) from
        // the numeric value from the character (thus a = 0, b = 1, etc)
        coords[0] = Character.getNumericValue(str.charAt(0)) - 10;

        // Second coordinate is just the given number minus 1
        coords[1] = Integer.parseInt(str.substring(1, 2)) - 1;

        return coords;
    }

    public static String getStringFromCoords(int x, int y)
    {
        // Add the unicode value for 'A' to the x-coordinate
        // Thus we get 0 = A, 1 = B, and so forth.
        x += 0x41;

        // Get the proper string from the character represented
        // by the unicode value x and the integer y + 1
        String retVal = Character.toString(((char) x)) + Integer.toString(y + 1);
        return retVal;
    }


}
