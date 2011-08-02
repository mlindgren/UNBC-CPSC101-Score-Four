/**
* Computer Opponent #3
*/

package cpsc101.team3.scorefour;

import java.util.Scanner;
import java.util.ArrayList;
import java.util.Random;

/**
* The "third" of our computer opponents.
* This computer opponent uses a priority-based
* system to determine the best move on the board.
*/
public class ComputerOpponent3
{

    // Constants for bead location priority
    private static final byte PRIORITY_MY_WIN = 0;
    private static final byte PRIORITY_ENEMY_WIN = 1;
    private static final byte PRIORITY_MY_TWO_CENTER = 2;
    private static final byte PRIORITY_ENEMY_TWO_CENTER = 3;
    private static final byte PRIORITY_MY_TWO_NORM = 4;
    private static final byte PRIORITY_ENEMY_TWO_NORM = 5;
    private static final byte PRIORITY_MY_ONE = 4;

    // Member variables
    private Board myBoard = new Board();
    private Color myColor = Color.UNKNOWN;
    private Color enemyColor = Color.WHITE;
    private Scanner inputScanner = new Scanner(System.in);
    private int curX = 1, curY = 2;
    private ArrayList<priorityBeadPlacement> myPriorities = new ArrayList<priorityBeadPlacement>();

    /**
    * Creates a new ComputerOpponent3 when the class is executed.
    */
    public static void main(String[] args) throws RuntimeException, PegFullException
    {
        new ComputerOpponent3();
    }

    /**
    * Constructor method.  Loops until the program is terminated by being killed
    * or by receiving a "quit;" command.  Uses the checkNextCommand function
    * to read input from the Referee and respond accordingly.
    */
    public ComputerOpponent3() throws RuntimeException, PegFullException
    {
        // Loop until we get a quit command
        // or the Pipe breaks
        while(true)
        {
            checkNextCommand(inputScanner.nextLine());
        }
    }

    /**
    * Restarts the opponent.  Dereferences the current board and
    * creates a new one, and resets stored colors.
    */
    private void restart()
    {
        myBoard = new Board();
        myColor = Color.UNKNOWN;
        enemyColor = Color.WHITE;
    }

    /**
    * Scans the board and make a move based on what
    * appears to be the best empty spot based on a number
    * of priorities.
    */
    private void makeMove() throws PegFullException
    {

        // Loop through each bead on the board and get priority placements
        // extending out from it
        for(int x = 0; x <= 3; x++)
        {
            for(int y = 0; y <= 3; y++)
            {
                for(int z = 0; z <= 3; z++)
                {
                    Bead curBead = myBoard.getPeg(x, y).getBead(z);
                    if(curBead != null)
                    {
                        // Loop through each direction extending out from each bead
                        for(int dX = -1; dX <= 1; dX++)
                        {
                            for(int dY = -1; dY <= 1; dY++)
                            {
                                for(int dZ = -1; dZ <= 1; dZ++)
                                {
                                    if(dX == 0 && dY == 0 && dZ == 0){ dZ = 1; }
                                    int direction[] = {dX, dY, dZ};

                                    // Number of beads in line so far
                                    int beadsInLine = 0;

                                    // In order for find lines on a given vector, we have to start from the
                                    // bead (if there is any) at the zeroth location on that vector.  This means
                                    // that for every non-zero element of the vector, we set our start bead
                                    // coordinate for that axis to zero.  Conversely, for every element of the
                                    // vector that is zero, we keep the coordinate of the current bead.
                                    // We want a Bead reference for the bead at that location, so we use the getPeg
                                    // function
                                    beadsInLine = curBead.checkUnblockedBeadsInLine(direction);

                                    // Also get the location of the first null bead in the direction
                                    int[] nextNullBeadLoc = curBead.getFirstNullBeadInDir(direction);

                                    // Flip the vector
                                    direction[0] = -(direction[0]);
                                    direction[1] = -(direction[1]);
                                    direction[2] = -(direction[2]);

                                    // Add the number of beads extending in the other direction
                                    // from the current bead
                                    beadsInLine += curBead.checkUnblockedBeadsInLine(direction);

                                    // Add one, since we're skipping the bead we start on
                                    beadsInLine += 1;

                                    // If we don't have the location of a null bead yet, check the other direction
                                    if(nextNullBeadLoc == null)
                                    {
                                        nextNullBeadLoc = curBead.getFirstNullBeadInDir(direction);
                                    }

                                    // Make sure there is an empty bead in the current line
                                    if(nextNullBeadLoc != null)
                                    {
                                        // Check which color the current bead is
                                        if(curBead.getColor() == myColor && myBoard.getPeg(nextNullBeadLoc[0], nextNullBeadLoc[1]).checkNextDrop() == nextNullBeadLoc[2])
                                        {
                                            switch(beadsInLine)
                                            {
                                                case 3: // We have a potential win for us
                                                    myPriorities.add(new priorityBeadPlacement(nextNullBeadLoc[0], nextNullBeadLoc[1], nextNullBeadLoc[2], PRIORITY_MY_WIN));
                                                    break;
                                                case 2: // Two beads in a row for us
                                                    byte curPriority = PRIORITY_MY_TWO_NORM;
                                                    if(nextNullBeadLoc[0] == 1 || nextNullBeadLoc[0] == 2 || nextNullBeadLoc[1] == 1 || nextNullBeadLoc[1] == 2)
                                                    {
                                                        curPriority = PRIORITY_MY_TWO_CENTER;
                                                    }
                                                    myPriorities.add(new priorityBeadPlacement(nextNullBeadLoc[0], nextNullBeadLoc[1], nextNullBeadLoc[2], curPriority));
                                                    break;
                                                case 1: // One bead, so we want to add another
                                                    myPriorities.add(new priorityBeadPlacement(nextNullBeadLoc[0], nextNullBeadLoc[1], nextNullBeadLoc[2], PRIORITY_MY_ONE));
                                                    break;
                                            }
                                        }
                                        else if(myBoard.getPeg(nextNullBeadLoc[0], nextNullBeadLoc[1]).checkNextDrop() == nextNullBeadLoc[2])
                                        {
                                            switch(beadsInLine)
                                            {
                                                case 3: // Enemy win
                                                    myPriorities.add(new priorityBeadPlacement(nextNullBeadLoc[0], nextNullBeadLoc[1], nextNullBeadLoc[2], PRIORITY_ENEMY_WIN));
                                                    break;
                                                case 2: // Enemy has two in a row
                                                    byte curPriority = PRIORITY_ENEMY_TWO_NORM;
                                                    if(nextNullBeadLoc[0] == 1 || nextNullBeadLoc[0] == 2 || nextNullBeadLoc[1] == 1 || nextNullBeadLoc[1] == 2)
                                                    {
                                                        curPriority = PRIORITY_ENEMY_TWO_CENTER;
                                                    }
                                                    myPriorities.add(new priorityBeadPlacement(nextNullBeadLoc[0], nextNullBeadLoc[1], nextNullBeadLoc[2], curPriority));
                                                    break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } // Dear God, the brackets!

        // Sort the priorities...
        java.util.Collections.sort(myPriorities);

        // Pick the best move (it'll be at index zero in the list)
        int bestMove[] = new int[3];
        try
        {
            bestMove = ((priorityBeadPlacement) myPriorities.get(0)).getLocation();
        }
        catch (IndexOutOfBoundsException e)
        {
            // System.out.println("error: " + e.getMessage()); // Debug
            // We don't have any priorities, so play a random move
            bestMove[0] = 1;
            bestMove[1] = 2;
            while(myBoard.getPeg(bestMove[0], bestMove[1]).checkFull())
            {
                bestMove[0]++;
                if(bestMove[0] > 3)
                {
                    bestMove[0] = 0;
                    bestMove[1]++;
                    if(bestMove[1] > 3)
                    {
                        bestMove[1] = 0;
                    }
                }
            }
        }

        // Place the bead and output the appropriate message
        myBoard.placeBead(bestMove[0], bestMove[1], myColor);
        System.out.println(Board.getStringFromCoords(bestMove[0], bestMove[1]) + ".");

        // Reset our priorities
        myPriorities = new ArrayList<priorityBeadPlacement>();


    }

    /**
    * Sets the computer opponent's color.
    * @param colorString The string representing the color to be set
    */
    private Color getColorFromString(String colorString)
    {
        Color retColor = Color.UNKNOWN;

        if(colorString.equals("BLACK"))
        {
            retColor = Color.BLACK;
        }
        else if(colorString.equals("WHITE"))
        {
            retColor = Color.WHITE;
        }
        else
        {
            retColor = Color.UNKNOWN;
        }

        return retColor;
    }

    /**
    * Reads an incoming move and place the appropriate bead on this
    * ComputerOpponent's representation of the board.
    * @param moveString A string representing the formatted coordinates of the move to be played.
    */
    private void readMove(String moveString) throws PegFullException
    {
        // System.out.println(moveString.substring(12));
        // System.out.println(moveString.substring(0, 5));

        // Place a bead at the appropriate spot...
        myBoard.placeBead(moveString.substring(12), getColorFromString(moveString.substring(0, 5)));
    }


    /**
    * Checks the next command.
    * @param c The command to be checked.
    */
    private void checkNextCommand(String c) throws RuntimeException, PegFullException
    {
        // Format the string properly
        c = c.trim().toUpperCase();

        if(c.startsWith("RESTART"))
        {
            restart();
            System.out.println("ok.");
        }
        else if(c.startsWith("QUIT"))
        {
            System.out.println("ok.");
            System.exit(0);
        }
        else if(c.startsWith("PLAY"))
        {
            myColor = getColorFromString(c.substring(5, 10));
            switch(myColor)
            {
                case BLACK:
                    // System.out.println("Playing black."); // Debug
                    enemyColor = Color.WHITE;
                    break;
                case WHITE:
                    // System.out.println("Playing white."); // Debug
                    enemyColor = Color.BLACK;
                    break;

            }
            System.out.println("ok.");
        }
        else if(c.startsWith("MOVE"))
        {
            makeMove();
        }
        else if(c.startsWith("NOTE:"))
        {
            readMove(c.substring(6));
            System.out.println("ok.");
        }
        else
        {
            throw new RuntimeException("Bad input was received from the referee:\n"
                                      + c + "\nDiscarding it.");
        }
    }

    /**
    * This class stores an x, y, and z location of a priority bead to be played.
    * It also stores the bead's priority.  The reason there is a special class
    * for priority bead placements is twofold - first, all priority beads are
    * actually null Beads on the board, so we cannot get locations from them.
    * Second, this class allows us to sort bead placements easily in an
    * ArrayList by using its compareTo() function.
    */
    public class priorityBeadPlacement implements Comparable<priorityBeadPlacement>
    {

        private int myX, myY, myZ;
        private byte myPriority;

        /**
        * Constructor method.  Creates a new priorityBeadPlacement
        * with the given coordinates and priority.
        * @param x The x-coordinate of the bead on the board.
        * @param y The y-coordinate of the bead on the board.
        * @param z The z-coordinate of the bead on the board.
        * @param priority A byte representing the bead's numeric priority.  Lower numbers are more important.
        */
        public priorityBeadPlacement(int x, int y, int z, byte priority)
        {
            myX = x;
            myY = y;
            myZ = z;
            myPriority = priority;
        }

        /**
        * Compares a priorityBeadPlacement to an Object.  If the Object
        * is also a priorityBeadPlacement, it compares the two based on
        * their priorities; otherwise, it simply returns zero.
        * @param o The object to compare to.
        */
        public int compareTo(priorityBeadPlacement o)
        {
            if(o instanceof priorityBeadPlacement)
            {
                return myPriority - ((priorityBeadPlacement) o).getPriority();
            }
            else
            {
                return 0;
            }
        }

        /**
        * Returns the priority of this priorityBeadPlacement.
        */
        public byte getPriority()
        {
            return myPriority;
        }

        /**
        * Returns the location of this priorityBeadPlacement as
        * an array of integers.
        */
        public int[] getLocation()
        {
            int myLocation[] = {myX, myY, myZ};
            return myLocation;
        }

    }

}
