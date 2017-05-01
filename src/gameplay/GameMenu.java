
package gameplay;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPanel;
import planetwar1.Vars;

public class GameMenu extends JPanel{
    
    public GameMenu(){
        
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(Vars.appWidth/3,Vars.appHeight);
    }
}
