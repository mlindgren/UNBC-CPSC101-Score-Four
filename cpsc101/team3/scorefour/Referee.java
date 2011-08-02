/****************************
* Referee class
* @author: Mitch
* Updated March 16th, 2008
* 10:44pm
****************************/

package cpsc101.team3.scorefour;

// Import statements we'll need
import com.sun.j3d.utils.picking.*;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.image.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;

// Class declaration
public class Referee extends JFrame
{

// Constants for program configuration
private static final int WINDOW_WIDTH = 700,
                        WINDOW_HEIGHT = 500,
                        DISPLAY_WIDTH = 640,
                        DISPLAY_HEIGHT = 400;
private static final String WINDOW_TITLE = "Java Score-4 Game by Team 3";
private static final float PEG_SPACING = 3.0f,
                           LEAST_BEAD_DEPTH = -1.0f,
                           BEAD_RADIUS = 0.5f;
private static final Color3f WHITE = new Color3f(1.0f, 1.0f, 1.0f),
                             BLACK = new Color3f(0.0f, 0.0f, 0.0f);
private static final Point3d DEFAULT_POV = new Point3d(25.0f, 12.0f, 8.0f),
                             ORIGIN = new Point3d(4.5f, 0, 4.5f);
private static final Appearance BEAD_BLACK_AP = new Appearance();
private static final BoundingSphere DEFAULT_BOUNDING_SPHERE = new BoundingSphere(ORIGIN, 100.0);


// Instance variables
private SimpleUniverse myUniverse;
private BranchGroup myGroup;
private TransformGroup boardGroup;
private float currentPOVrot = 0;
private PickCanvas myPickCanvas;
private Canvas3D scene;
private Board myBoard;
private JPanel displayPanel;
private JLabel myStatus = new JLabel(); // Status message
private boolean playable = true; // Whether or not a human can currently play on the board
private byte currentTurn = 0; // Which player's turn it is
private Player myPlayers[] = new Player[2];
private static int debug3D = 0;
private static Referee myWindow;
private static settingsEditor mySettingsWindow;

    /**
    * Constructor method. Creates a new Referee frame for the game to be played on.
    */
    public Referee(boolean whiteIsHuman, boolean blackIsHuman)
    {

        // Set up the BEAD_BLACK_AP Appearance
        BEAD_BLACK_AP.setMaterial(new Material(BLACK, BLACK, BLACK, WHITE, 128));

        // Create a panel to contain the 3D canvas
        displayPanel = new JPanel();
        displayPanel.setBorder(BorderFactory.createTitledBorder("Display"));

        // (Re)start the game
        restart(whiteIsHuman, blackIsHuman);

        // Configure the frame and add our panels
        setTitle(WINDOW_TITLE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Add the 3D display to the frame
        add(displayPanel, BorderLayout.PAGE_START);

        // Create and add the status label
        add(myStatus, BorderLayout.CENTER);

        // Create a restart button
        JButton restartButton = new JButton("New game");
        restartButton.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e){ mySettingsWindow.setVisible(true); } } );
        add(restartButton, BorderLayout.LINE_END);

        // Make the frame visible
        setVisible(true);

    }

    /**
    * Initializes everything, including creating a new board, resetting
    * 3D rendering, and so forth.
    * @param whiteIsHuman Defines whether or not the white player is a human (if not, then he is a computer)
    * @param blackIsHuman Defines whether or not the black player is a human (if not, then he is a computer)
    */
    private void restart(boolean whiteIsHuman, boolean blackIsHuman)
    {
        // Clean up the universe if one exists already
        if(myUniverse != null)
        {
            myUniverse.cleanup();
        }

        // Remove the scene from the display,
        // if one exists already
        if(displayPanel != null && scene != null)
        {
            displayPanel.remove(scene);
        }

        // Create a new board
        if(debug3D != 0)
        {
            System.out.println("Debug3D");
            myBoard = new Board(1);
        }
        else
        {
            myBoard = new Board();
        }

        // Initialize the 3D scene, including the board
        initialize3D();

        // Add the scene to the display panel
        displayPanel.add(scene);

        // Set up the players
        if(whiteIsHuman)
        {
            myPlayers[0] = new Player(Color.WHITE);
        }
        else
        {
            myPlayers[0] = new Player(Color.WHITE, "cpsc101.team3.scorefour.ComputerOpponent3");
            myPlayers[0].startPipe();
        }

        if(blackIsHuman)
        {
            myPlayers[1] = new Player(Color.BLACK);
        }
        else
        {
            myPlayers[1] = new Player(Color.BLACK, "cpsc101.team3.scorefour.ComputerOpponent3");
            myPlayers[1].startPipe();
        }

        // Make sure the right turn number is set
        currentTurn = 0;

        // Set the status label
        myStatus.setText(myPlayers[currentTurn].getColor() + " player's turn.");

        // Check whether the first (white) player is a human or a computer
        if(!whiteIsHuman)
        {
            // If not, get the move from the computer
            int moveCoords[] = new int[1];
            try
            {
                moveCoords = Board.getCoordsFromString(myPlayers[currentTurn].getMove());

                // Place the bead
                place3DBead(moveCoords[0], moveCoords[1], myPlayers[currentTurn].getColor());
            }
            catch (badCoordinateException e)
            {
                System.out.println("Warning: Received bad coordinates from computer opponent.  Discarding this move.");
                System.out.println(e.getMessage());
            }
        }
        else
        {
            // If so, make the board playable
            playable = true;
        }

    }


    /**
    * Re-initializes 3D graphics whenever the game is started/restarted.
    */
    private void initialize3D()
    {

            // Get a graphics configuration and create a new 3D canvas
            GraphicsConfiguration myConfig = SimpleUniverse.getPreferredConfiguration();
            scene = new Canvas3D(myConfig);
            scene.setSize(640, 400);

            // Add a mouse listener to the display
            scene.addMouseListener(new displayMouseListener());
            scene.addKeyListener(new refKeyListener());

            // Set up the universe
            myUniverse = new SimpleUniverse(scene);

            // Create a new SimpleUniverse and BranchGroup to output
            // graphics to
            myGroup = new BranchGroup();
            myGroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
            myGroup.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);

            // Load the background texture and apply it to the scene
            TextureLoader bgTexLoader = new TextureLoader(getClass().getClassLoader().getResource("background.jpg"), new Container());
            Background myBackground = new Background(bgTexLoader.getImage());
            myBackground.setApplicationBounds(DEFAULT_BOUNDING_SPHERE);
            myGroup.addChild(myBackground);

            // Create a white light that shines for 100m from the origin
            Color3f lightColor = new Color3f(1.8f, 1.8f, 1.8f);
            Vector3f lightDirection  = new Vector3f(-10.0f, -10.0f, -10.0f);
            DirectionalLight theLight = new DirectionalLight(lightColor, lightDirection);
            theLight.setInfluencingBounds(DEFAULT_BOUNDING_SPHERE);
            myGroup.addChild(theLight);

            // Set clipping distance to 50 units
            myUniverse.getViewer().getView().setBackClipDistance(50.0f);

            // Draw pegs in our BranchGroup
            initialize3DBoard(myGroup);

            // Enable picking on the BranchGroup
            enablePicking(myGroup);

            // Adjust the view and add the branch group to it
            myUniverse.getViewingPlatform().setNominalViewingTransform();
            myUniverse.addBranchGraph(myGroup);

            // Create a new PickCanvas on our 3D display to enable
            // picking of 3D objects
            myPickCanvas = new PickCanvas(scene, myGroup);
            myPickCanvas.setMode(PickCanvas.GEOMETRY);
    }


    /**
    * Draws 3-dimensional representations of pegs on the canvas by adding them to the given
    * BranchGroup.
    * @param bg The BranchGroup to draw pegs to.
    */
    private void initialize3DBoard(BranchGroup bg)
    {
        // Create a Transform3D which holds the initial state of the board
        Transform3D boardTransform = new Transform3D();
        boardTransform.lookAt(DEFAULT_POV, ORIGIN, new Vector3d(0, 1, 0));

        // Assign our boardGroup to a new TransformGroup, using
        // boardTransform as the default transformation
        boardGroup = new TransformGroup(boardTransform);
        boardGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        boardGroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
        boardGroup.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);

        // Loop through all our pegs in the x and y directions
        // This should be tied more directly to the Peg objects
        // on the Board, but that will have to come later
        for(float x = 0f; x < PEG_SPACING * 4; x += PEG_SPACING)
        {
            for(float z = 0f; z < PEG_SPACING * 4; z += PEG_SPACING)
            {
                // Create a new vector to translate the pegs to
                Vector3f theVector = new Vector3f(x, 0.0f, z);
                Transform3D theTransform = new Transform3D();
                theTransform.setTranslation(theVector);
                TransformGroup theTG = new TransformGroup(theTransform);

                // Add our new TransformGroup to the BranchGroup
                theTG.addChild(new Cylinder(0.15f, 4.0f));
                boardGroup.addChild(theTG);

            }
        }

        // Create a transform group for the base of the board
        // (which sits at the bottom of the pegs.)
        Transform3D boardBaseTx = new Transform3D();
        boardBaseTx.setTranslation(new Vector3f(4.5f, -2.0f, 4.5f));
        TransformGroup boardBaseGroup = new TransformGroup();
        boardBaseGroup.setTransform(boardBaseTx);

        // Load texture for the board and set its attributes
        TextureLoader boardTexLoader = new TextureLoader(getClass().getClassLoader().getResource("board.jpg"), new Container());
        Texture boardTex = boardTexLoader.getTexture();
        boardTex.setBoundaryModeS(Texture.WRAP);
        boardTex.setBoundaryModeT(Texture.WRAP);
        boardTex.setBoundaryColor(new Color4f(1.0f, 1.0f, 1.0f, 0.0f));

        TextureAttributes boardTexAttr = new TextureAttributes();
        boardTexAttr.setTextureMode(TextureAttributes.MODULATE);

        // Create an appearance for the board based on the texture
        // we've loaded
        Appearance boardAp = new Appearance();
        boardAp.setTexture(boardTex);
        boardAp.setTextureAttributes(boardTexAttr);

        // Set the appearance's material properties
        boardAp.setMaterial(new Material(WHITE, BLACK, WHITE, BLACK, 128));

        // Add a box (the actual 3D representation of the board base)
        // to the TransformGroup
        boardBaseGroup.addChild(new com.sun.j3d.utils.geometry.Box(4.65f, 0.5f, 4.65f, Primitive.GENERATE_NORMALS + Primitive.GENERATE_TEXTURE_COORDS, boardAp));

        // Add the boardBaseGroup to the boardGroup
        boardGroup.addChild(boardBaseGroup);

        // Add it all to the scene
        bg.addChild(boardGroup);
    }

    /**
    * Adjusts (rotates) the point of view of the 3D scene.
    * @param rot The amount the scene should be rotated on the X-axis, in radians
    */
    private void setPOV(float rot)
    {
        try
        {
            // Keep track of the current rotation
            currentPOVrot += rot;

            // Create a new Transform3D to rotate the POV
            Transform3D newTx = new Transform3D();

            // Figure out which axis to rotate around
            newTx.setRotation(new AxisAngle4f(0, 1, 0, currentPOVrot));

            // Create a new Point3d for the POV and rotate it
            // currentPOVrot radians from the DEFAULT_POV
            Point3d newPOV = new Point3d();
            newTx.transform(DEFAULT_POV, newPOV);

            // Look at the board from our new POV
            newTx.lookAt(newPOV, ORIGIN, new Vector3d(0f, 1f, 0f));

            // Updat the board's transform
            boardGroup.setTransform(newTx);

        }
        catch(Exception e)
        {
            // Do nothing...
        }
    }

    /**
    * Places a 3-dimensional representation of a bead on the board,
    * and calls the board's PlaceBead function to create a new Bead
    * Object on our Board object.  Then, it switches turns.
    * @param x The x-coordinate of the bead (in terms of board coordinates, not 3D coordinates)
    * @param y The y-coordinate of the bead (in terms of board coordinates, not 3D coordinates)
    * @param bColor The color of the bead to place
    */
    private void place3DBead(int x, int y, Color bColor)
    {
        try
        {
            // Place the bead on the actual board
            int droppedDepth = myBoard.placeBead(myBoard.getStringFromCoords(x, y), bColor);

            // Create a new Transform3D to put the bead in the right spot
            Transform3D curBeadTx = new Transform3D();
            curBeadTx.setTranslation(new Vector3f(x * PEG_SPACING, LEAST_BEAD_DEPTH + (droppedDepth * BEAD_RADIUS * 2) , y * PEG_SPACING));

            // Create a new TransformGroup to place the current bead in
            TransformGroup curBeadGroup = new TransformGroup(curBeadTx);
            if(bColor == Color.WHITE)
            {
                curBeadGroup.addChild(new Sphere(0.5f));
            }
            else
            {
                curBeadGroup.addChild(new Sphere(0.5f, BEAD_BLACK_AP));
            }

            // Create a new BranchGroup and add the current
            // bead's TransformGroup to it (sigh)
            BranchGroup newBeadBranch = new BranchGroup();
            newBeadBranch.addChild(curBeadGroup);

            // System.out.print("Adding bead."); // Debug

            // Add the new branch to the scene branch
            boardGroup.addChild(newBeadBranch);

            // Send a move note to the other player if
            // it is not a human player
            int otherPlayerNum = 0;
            switch(currentTurn)
            {
                case 0:
                    otherPlayerNum = 1;
                    break;
                case 1:
                    otherPlayerNum = 0;
                    break;
            }
            if(!myPlayers[otherPlayerNum].isHuman())
            {
                myPlayers[otherPlayerNum].writeMoveNote(bColor, Board.getStringFromCoords(x, y));
            }

            switch(myBoard.checkGame())
            {
                case WIN:
                    myStatus.setText(myPlayers[currentTurn].getColor() + " wins!");
                    playable = false;
                    System.out.println("Win ocurred!");
                    break;
                case DRAW:
                    myStatus.setText("The game is a draw.");
                    playable = false;
                    System.out.println("Draw.");
                    break;
                default:
                    // Nothing happened, so it's the other player's turn
                    flipTurns();
                    break;
            }

        }
        catch (PegFullException e)
        {
            System.out.println(e.getMessage());
            System.out.println("Warning: Discarding current move.");
            flipTurns();
        }


    }

    /**
    * Switches the turns after a move is played.  If the player
    * whose turn it now is is a computer, it gets the move from the player.
    * Otherwise, it simply sets the board to be playable, and updates
    * the status text displayed on the frame.
    */
    private void flipTurns()
    {
        // Adjust the turn number
        switch(currentTurn)
        {
            case 0:
                currentTurn++;
                break;
            case 1:
                currentTurn--;
                break;
            default:
                currentTurn = 1;
                break;
        }

        if(!myPlayers[currentTurn].isHuman())
        {
            // Prevent the player from placing another bead, and wait
            // for the computer's move
            playable = false;
            myStatus.setText(myPlayers[currentTurn].getColor() + " player's turn.  Waiting for move from computer.");

            // Get coordinates for the next move from the computer opponent
            int moveCoords[] = new int[1];
            try
            {
                moveCoords = Board.getCoordsFromString(myPlayers[currentTurn].getMove());

                // Place the bead
                place3DBead(moveCoords[0], moveCoords[1], myPlayers[currentTurn].getColor());
            }
            catch (badCoordinateException e)
            {
                System.out.println("Warning: Received bad coordinates from computer opponent.  Discarding this move.");
                System.out.println(e.getMessage());
            }

        }
        else
        {
            // Update the status message
            myStatus.setText(myPlayers[currentTurn].getColor() + " player's turn.");

            // Make sure the board is playable again
            playable = true;
        }
    }

    /**
    * Enables geometry-based picking on a node and
    * all of its children.  This function is modified
    * from one written by Greg Hopkins for his
    * "Joy of Java 3D" tutorial. See:
    * www.java3d.org/tutorial.doc
    *
    * @param node The node to enable picking on.
    */
    private void enablePicking(Node theNode) {
        theNode.setPickable(true);
        theNode.setCapability(Node.ENABLE_PICK_REPORTING);
        try
        {
           Group theGroup = (Group) theNode;
           for (java.util.Enumeration e = theGroup.getAllChildren(); e.hasMoreElements();)
           {
              enablePicking((Node) e.nextElement());
           }
        }
        catch(ClassCastException e)
        {
            // If the node is not a group, then it will not have any children,
            // so we ignore this exception.
        }
        try
        {
              Shape3D theShape = (Shape3D) theNode;
              PickTool.setCapabilities(theNode, PickTool.INTERSECT_FULL);
              for (java.util.Enumeration e = theShape.getAllGeometries(); e.hasMoreElements();)
              {
                 Geometry g = (Geometry) e.nextElement();
                 g.setCapability(Geometry.ALLOW_INTERSECT);
              }
        }
        catch(ClassCastException e)
        {
           // If the node is not a Shape3D, we do not need to set picking on it,
           // so we also ignore this exception
        }
    }


    /**
    * Main function.  Creates a new settings editor
    * window, which in turn will create a Referee
    * window once the okay button is pressed.
    */
    public static void main(String args[])
    {

        if(args.length > 0 && args[0].equals("-v"))
        {
            debug3D = 1;
        }

        // Bring up the settings window
        mySettingsWindow = new settingsEditor();

    }

    /**
    * Mouse listener for the 3D display.
    */
    public class displayMouseListener extends MouseAdapter
    {

        /**
        * Called when the mouse is clicked on the 3D display.
        * When this happens, we want to figure out where the mouse
        * was clicked, and if the board is currently playable,
        * place a bead of the appropriate color on the Peg
        * that was clicked (if any.)  This function does that.
        */
        public void mouseClicked(MouseEvent e)
        {
            // Set the shape location of the PickCanvas based on the MouseEvent
            myPickCanvas.setShapeLocation(e);

            // Get a PickResult from the PickCanvas
            PickResult pResult = myPickCanvas.pickClosest();

            if(pResult != null)
            {
                // Get a reference to the primitive which was picked
                Primitive pickedPrimitive = (Primitive) pResult.getNode(PickResult.PRIMITIVE);

                // Check that a Peg (cylinder) was picked
                if(pickedPrimitive instanceof com.sun.j3d.utils.geometry.Cylinder)
                {
                    // The parent of the picked primitive will be a TransformGroup, so
                    // we can figure out the location of the primitive based on information
                    // the TransformGroup gives up
                    int pickedBoardX, pickedBoardY;
                    TransformGroup pickedTxGrp = (TransformGroup) pickedPrimitive.getParent();

                    // Get the TransformGroup's Transform3D and convert it to a vector
                    Transform3D pickedTx3D = new Transform3D();
                    Vector3f pickedTxVector = new Vector3f();
                    pickedTxGrp.getTransform(pickedTx3D);
                    pickedTx3D.get(pickedTxVector);

                    // Get the coordinates
                    // PickedBoardX and PickedBoardY are defined in terms of
                    // board X and Y coordinates, not 3D coordinates, so we
                    // have to convert them first.
                    pickedBoardX = (int) (pickedTxVector.x / PEG_SPACING);
                    pickedBoardY = (int) (pickedTxVector.z / PEG_SPACING);

                    System.out.println("Picked peg [" + pickedBoardX + ", " + pickedBoardY
                                       + "] (actual coords [" + pickedTxVector.x + ", "
                                       + pickedTxVector.z + "])"); //*/ // Debug

                    if(playable)
                    {
                        place3DBead(pickedBoardX, pickedBoardY, myPlayers[currentTurn].getColor());
                    }
                }
            }
        }
    }

    /**
    * Key listener for the 3D display.
    */
    public class refKeyListener extends KeyAdapter
    {
        /**
        * Rotates the 3D display's
        * point of view left or right
        * when the left or right keys are pressed.
        */
        public void keyPressed(KeyEvent e)
        {
            switch(e.getKeyCode())
            {
                case KeyEvent.VK_LEFT:
                    setPOV(-0.1f);
                    break;
                case KeyEvent.VK_RIGHT:
                    setPOV(0.1f);
                    break;
            }
        }
    }

    /**
    * @author: Ethan (comments by Mitch)
    * This class describes the settings editor frame, which allows the user to
    * decide whether each player should be a human or a computer.
    */
    public static class settingsEditor extends JFrame implements ActionListener
    {

        JPanel panel;
        JRadioButton whiteHuman, whiteComputer, blackHuman, blackComputer;
        ButtonGroup whiteButtonGroup, blackButtonGroup;
        JLabel whiteLabel, blackLabel;
        JButton okayButton;

        /**
        * Constructor method.  Sets up the frame.
        */
        public settingsEditor()
        {

            setTitle("New game");
            setResizable(false);
            setSize(150, 250);

            addWindowListener(new WindowAdapter() { public void windowClosing(WindowEvent e){ if(myWindow == null){ System.exit(0); } } });

            panel = new JPanel();
            panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));

            whiteLabel = new JLabel("White player:");
            whiteHuman = new JRadioButton("Human");
            whiteComputer = new JRadioButton("Computer");
            whiteButtonGroup = new ButtonGroup();

            panel.add(whiteLabel);

            whiteButtonGroup.add(whiteHuman);
            whiteButtonGroup.add(whiteComputer);

            panel.add(whiteHuman);
            panel.add(whiteComputer);

            blackLabel = new JLabel("Black player:");
            blackHuman = new JRadioButton("Human");
            blackComputer = new JRadioButton("Computer");
            blackButtonGroup = new ButtonGroup();

            panel.add(blackLabel);

            blackButtonGroup.add(blackHuman);
            blackButtonGroup.add(blackComputer);

            panel.add(blackHuman);
            panel.add(blackComputer);

            okayButton = new JButton("Start game");

            okayButton.addActionListener(this);

            panel.add(okayButton);

            //buildPanel();
            add(panel);
            setVisible(true);
        }

        /**
        * When the okay button is pressed, this function checks that the settings have been filled out.
        * If they have, it creates a new referee window (if necessary) and restarts the game with the
        * appropriate settings.
        */
        public void actionPerformed(java.awt.event.ActionEvent e)
        {
            if((blackHuman.isSelected() || blackComputer.isSelected()) && (whiteHuman.isSelected() || whiteComputer.isSelected()))
            {
                if(myWindow == null)
                {
                    myWindow = new Referee(whiteHuman.isSelected(), blackHuman.isSelected());
                }
                else
                {
                    myWindow.restart(whiteHuman.isSelected(), blackHuman.isSelected());
                }
            }

            setVisible(false);

        }

    }

}
