package cpsc101.team3.scorefour;

/**
* Exception thrown when something tries to drop a bead onto a full peg.
*/
public class PegFullException extends Exception
{
    /**
    * Constructor.  Creates a new PegFullException with a detail message about which
    * Peg caused the Exception.
    */
    public PegFullException(Peg p)
    {
        super("Something attempted to drop a bead onto the Peg at " + p.getFormattedLocation() + ", which is full.");
    }
}
