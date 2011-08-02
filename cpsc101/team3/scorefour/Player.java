package cpsc101.team3.scorefour;

import java.util.Scanner;
import java.io.*;

import ca.unbc.casper.cpsc101.JavaPipe;

/**
* This class describes a player in the game from the "point of view" of the referee.
* For human players, it essentially just keeps track of Color.  For computer players,
* though, it handles piping and I/O through the pipe.
*/
public class Player
{
    private Color myColor;
    private String myClassName; // Class name (for non-human Players.)
    private JavaPipe myPipe;
    private PrintWriter myOutput;
    private Scanner inputScanner;

    /**
    * Creates a player of unknown color.
    */
    public Player()
    {
        // Do nothing
    }

    /**
    * Creates a new human player with the given Color.
    * @param c The color of the human player
    */
    public Player(Color c)
    {
        myColor = c;
    }

    /**
    * Create a new Computer player with the
    * given Color and className.
    * @param c The color of the computer player
    * @param className The name of the computer player's class.  Must include the package name, if applicable.
    */
    public Player(Color c, String className)
    {
        myColor = c;
        myClassName = className;

        //System.out.println(getClass().getClassLoader().findClass(myClassName +  );

        // Create the pipe
        myPipe = new JavaPipe(myClassName);
        String myLoc[] = Player.class.getProtectionDomain().getCodeSource().getLocation().toString().split("/");
        if(myLoc[myLoc.length - 1].endsWith(".jar"))
        {
            myPipe.setClassPath(myLoc[myLoc.length - 1]);
            myPipe.setExecPath(myLoc[myLoc.length - 1]);
        }
        //javax.swing.JOptionPane.showMessageDialog(null, myLoc[myLoc.length - 1]); // Debug

    }

    /**
    * Starts the Pipe and sends an command to
    * the Computer player to set its color.
    */
    public void startPipe()
    {
        System.out.println("Starting pipe..."); // Debug

        // System.out.println(myPipe.getExecPath()); // Debug
        // System.out.println(myPipe.getClassPath());

        // Start the pipe and get I/O objects
        try
        {
            myPipe.start();
        }
        catch (Exception e)
        {
            System.out.print("Error!  Cannot start computer opponent.");
            System.exit(0);
        }

        myOutput = myPipe.getOutputWriter();

        // Create a Scanner to get input
        inputScanner = new Scanner(myPipe.getInputReader());

        // Send a note telling the computer
        // which pipe to play
        printOutput("play " + getStringFromColor(myColor) + ";");

        // Check that everything's working
        checkForOkay();

    }

    /**
    * Tells the computer to quit, and destroys
    * the pipe.
    */
    public void destroyPipe()
    {
        // Send a message to quit
        printOutput("quit;");

        // Destroy the pipe
        myPipe.destroy();

        // Dereference the pipe
        myPipe = null;
    }

    /**
    * Gets the computer's next move, and checks that
    * the coordinates provided by the computer are valid.
    */
    public String getMove() throws badCoordinateException
    {
        // Send a message for the computer to move
        printOutput("move;");

        // ...and get the input back
        String newMove = inputScanner.nextLine().trim().toUpperCase();

        // Verify the input we've received
        switch(newMove.charAt(0))
        {
            case 'A':
            case 'B':
            case 'C':
            case 'D':
                break;
            default:
                throw new badCoordinateException(newMove);
        }

        switch(newMove.charAt(1))
        {
            case '1':
            case '2':
            case '3':
            case '4':
                break;
            default:
                throw new badCoordinateException(newMove);
        }


        // Return the move we received
        // (only the first two characters)
        return newMove.substring(0, 2);
    }

    /**
    * Sends a note over the pipe that a move was played.
    * @param c The color which was played.
    * @param move The coordinates (in the format A4, etc.) of the move which was played
    */
    public void writeMoveNote(Color c, String move)
    {
        // Send a note that a move was played
        printOutput("note: " + getStringFromColor(c) + " plays " + move + ";");

        // Check that the computer is alive
        checkForOkay();
    }

    /**
    * Consumes "ok." lines.  Outputs debug message if
    * the response is anything other than "ok."
    */
    private void checkForOkay() throws java.util.NoSuchElementException
    {
        // System.out.println("Checking for okay..."); // Debug
        String nextLine = inputScanner.nextLine().trim().toLowerCase();

        if(!nextLine.equals("ok."))
        {
            System.out.println("Warning: Received bad input from computer opponent:\n"
                            + nextLine + "\n"
                            + "Computer opponent may be malfunctioning or noncompliant.\n"
                            + "Color: " + myColor + ", class: " + myClassName);
        }

        // System.out.println("Got okay from computer opponent."); // Debug
    }

    /**
    * Prints a string to the pipe
    * and automatically flushes it.
    * @param s The string to be printed
    */
    private void printOutput(String s)
    {
        myOutput.println(s);
        myOutput.flush();
    }

    /**
    * Sets the Player's Color.
    * @param c The color to be set
    */
    public void setColor(Color c)
    {
        myColor = c;
    }

    /**
    * Set the Player's class name (for non-human Players)
    * @param className The name of the class.  Must include the package name, if applicable.
    */
    public void setClassName(String className)
    {
        myClassName = className;
    }

    /**
    * Returns the Player's Color.
    */
    public Color getColor()
    {
        return myColor;
    }

    /**
    * Check if the Player is human.  Returns true
    * if human, false otherwise.
    */
    public boolean isHuman()
    {
        if(myClassName == null)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
    * Gets the appropriate lowercase string from
    * a Color enum.
    */
    private String getStringFromColor(Color c)
    {
        String colorString;
                switch(c)
                {
                    case BLACK:
                        colorString = "black";
                        break;
                    case WHITE:
                        colorString = "white";
                        break;
                    default:
                        colorString = "unknown";
                        break;
                }
        return colorString;
    }
}
