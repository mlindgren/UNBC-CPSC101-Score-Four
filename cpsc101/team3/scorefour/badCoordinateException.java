package cpsc101.team3.scorefour;

public class badCoordinateException extends Exception
{
   public badCoordinateException(String originalCoords)
   {
      super("Received bad coordinates from computer opponent!  Coordinate string: " + originalCoords);
   }
}
