package cpsc101.team3.scorefour;

public class Bead
{
    private Color myColor;    // either BLACK, WHITE, or UNKNOWN
    private Peg myPeg;          // The Peg the Bead is on
    private int myLocation[] = new int[3]; // Bead's 3-dimensional location

    // constructor -- sets bead's coordinates and color
    public Bead(Peg p, Color c, int depth) {
        myPeg = p;
        myColor = c;

        int myPegLoc[] = myPeg.getLocation();
        myLocation[0] = myPegLoc[0];
        myLocation[1] = myPegLoc[1];
        myLocation[2] = depth;

    }

    // returns the color of a bead
    public Color getColor() { return myColor; }

    // returns the peg the bead is attached to
    public Peg getPeg()
    {
        return myPeg;
    }

    // returns the coordinates of the bead
    public int[] getLocation()
    {
        return myLocation;
    }

    /**
    * Returns the current bead's depth on the peg.
    */
    public int getDepth()
    {
        return myLocation[2];
    }

    /* This is the function of the bead that the board calls to check for a win
       it should be called every time a new bead is placed (on the new bead that
       was placed.) */
    public boolean checkWin()
    {

        /* This array can be thought of as a vector with three coordinates
           x, y, and z with element 0 corresponding to x, element 1
           corresponding to y, and element 2 correpsonding to z.  Each
             element has one of three possible values: -1, 0, or 1.  Thus,
           there are 27 possible values, one for each of the "cardinal"
           directions in 3 dimensions.  Basically, it tells the
           checkNextForWin function which direction it should look
           for the next bead in. */
        int[] direction = new int[3];

        /* The following nested for loops
           loop through all of the possible directions */
        for(int x = -1; x <= 1; x++)
        {
            for(int y = -1; y <= 1; y++)
            {
                for(int z = -1; z <= 1; z++)
                {

                    /* We have to exclude the direction (0, 0, 0) because
                       that would check the original bead forever, resulting
                       in an infinite loop. */
                    if(!(x == 0 && y == 0 && z == 0))
                    {
                        direction[0] = x;
                        direction[1] = y;
                        direction[2] = z;

                        // Number of beads in line so far
                        int beadsInLine = 0;

                        // In order for find lines on a given vector, we have to start from the
                        // bead (if there is any) at the zeroth location on that vector.  This means
                        // that for every non-zero element of the vector, we set our start bead
                        // coordinate for that axis to zero.  Conversely, for every element of the
                        // vector that is zero, we keep the coordinate of the current bead.
                        // We want a Bead reference for the bead at that location, so we use the getPeg
                        // function
                        beadsInLine = checkNextInLine(direction);

                        // Flip the vector
                        direction[0] = -(direction[0]);
                        direction[1] = -(direction[1]);
                        direction[2] = -(direction[2]);

                        beadsInLine += checkNextInLine(direction);

                        if(beadsInLine >= 3)
                        {
                            return true;
                        }


                    }
                }
            }
        }


        /* If we get to here and we haven't returned yet,
           it means a win was not found, so return false */
        return false;

}

    /* This function recursively checks beads in a straight line to determine whether or not
       a win has occurred.  To do so, it takes a direction and a number of required beads,
       and determines whether or not the next bead in the given direction is of the proper
       color.  If it is, it subtracts one from the number of required beads and calls itself
       again on the next bead in the line. */
    public int checkNextInLine(int[] direction)
    {

        /* Array to store the actual location of the next bead,
           so we don't have to keep adding location + direction. */
        int[] nextBeadLocation = {myLocation[0] + direction[0], myLocation[1] + direction[1], myLocation[2] + direction[2]};

        // Try to get a bead object based on the location we calculated
        // We might get an ArrayIndexOutOfBounds exception here, because it's possible that we're looking
        // at a non-existant bead.  That's okay; that just means we return false.
        Bead nextBead;
        try
        {
            nextBead = myPeg.getBoard().getPeg(nextBeadLocation[0], nextBeadLocation[1]).getBead(nextBeadLocation[2]);
        }
        catch(ArrayIndexOutOfBoundsException e)
        {
            // Should output something to debug here
            return 0;
        }

        // Make sure the nextBead isn't null.
        if(nextBead == null)
        {
            return 0;
        }

        /* If the next bead if of the appropriate color, then
           we need to run the checkNextForWin() function on it
           again. */
        if(nextBead.getColor() == myColor)
        {
            /* When we find a win, we'll have several "copies" of the
               checkNextForWin function on the stack, so we need to
               break out of all of them.  In order to do so,
               we'll make each function return true when the one
               it calls returns true. */
            return nextBead.checkNextInLine(direction) + 1;

        }

        /* If we get to this point without a win,
           we're done.  Return false.  */
        return 0;

    }

    // Checks for the number of beads in a line that are not blocked by a bead
    // of the opposing color
    public int checkUnblockedBeadsInLine(int[] direction)
    {
        int numBeads = 0, x = myLocation[0], y = myLocation[1], z = myLocation[2];
        while(x >= 0 && x <= 3 && y >= 0 && y <= 3 && z >= 0 && z <= 3)
        {
            x += direction[0];
            y += direction[1];
            z += direction[2];


            Bead nextBead = null;
            try
            {
                nextBead = myPeg.getBoard().getPeg(x, y).getBead(z);
            }
            catch (ArrayIndexOutOfBoundsException e)
            {
                // We've gone off the edge of the board,
                // return.
                return numBeads;
            }

            if(nextBead != null)
            {
                if(nextBead.getColor() == myColor)
                {
                    numBeads++;
                }
                else
                {
                    // If the line is blocked by an enemy bead, we
                    // DO NOT care about it
                    return -50;
                }
            }
        }

        return numBeads;


    }

    /* Gets the first null bead in a given direction */
    public int[] getFirstNullBeadInDir(int[] direction)
    {
        int[] nextBeadLocation = {myLocation[0] + direction[0], myLocation[1] + direction[1], myLocation[2] + direction[2]};

        // Try to get a bead object from the location
        Bead nextBead;
        try
        {
            nextBead = myPeg.getBoard().getPeg(nextBeadLocation[0], nextBeadLocation[1]).getBead(nextBeadLocation[2]);
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            // There's no bead in the given direction; return null
            return null;
        }

        // Check if the bead is null
        if(nextBead == null)
        {
            // If so, return its location
            return nextBeadLocation;
        }
        else
        {
            // Otherwise, check the next bead...
            return nextBead.getFirstNullBeadInDir(direction);
        }
    }



}
