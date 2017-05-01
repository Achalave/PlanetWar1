 
package planetwar1;
import java.awt.Color;
public class Vars {
    public static int refreshRate = 25;
    public static int appWidth = 900;
    public static int appHeight = 700;
    public static boolean comets = true;
    
    public static int percentToSend = 25;
    public static final int[] percents = {10,25,50,75,100};
    
    //Planet
    public static double minFactor = .7;
    public static int startArmy = 150;
    public static double factor = .015;
    public static Color playerColor = Color.BLUE;
    public static Color enemyColor = Color.RED;
    public static Color neutralColor = Color.MAGENTA;
    public static int minPlanetSize = 20;
    public static int maxPlanetSize = 50;
    public static int maxPerSquad = 20;
    public static int preferedNumberSquads = 5;
    public static int countdownStart = 2000;
    public static double armyIncreaseRate = 1; //percent per second
    
    //Squad
    public static int diameter = 10;
    public static int squadCountdownStart = 50;
    public static int speedPlayer = 75; //per second
    public static int speedEnemy = 75;
    
    //Comet
    public static int cometCountdownStart = 50;
    public static int maxCometSpeed = 60;
}
