
package planetwar1;
import AI.EasyAI;
import AI.HardAI;
import AI.MediumAI;
import gameplay.Comet;
import gameplay.Explosion;
import gameplay.Planet;
import gameplay.Squad;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JButton;
import javax.swing.JPanel;
public class GameplayView extends JPanel implements
        MouseListener, MouseMotionListener,Runnable,ActionListener{
    
    
    //Variables
    public static ArrayList<Planet> planets = new ArrayList<Planet>();
    public static ArrayList<Squad> squads = new ArrayList<Squad>();
    public static ArrayList<Planet> selectedPlanets = new ArrayList<Planet>();
    public static ArrayList<Comet> comets = new ArrayList<Comet>();

    Thread t;
    int maxComets = 5;
    int mouseX,mouseY;
    boolean mousePressed = false;
    Planet touched;
    EasyAI easy;
    MediumAI normal;
    HardAI hard;
    public static int gameOver = 0;
    public boolean exit = false;
    
    //Constructors
    public GameplayView(){
        gameOver = 0;
        planets.clear();
        squads.clear();
        selectedPlanets.clear();
        comets.clear();
        add(newButton("Back"));
        this.setBackground(Color.black);
        setupListeners();
    }
    public JButton newButton(String s){
        JButton button = new JButton(s);
        button.setAlignmentX(JButton.CENTER_ALIGNMENT);
        button.setFocusable(false);
        button.addActionListener(this);
        button.setForeground(Color.RED);
        button.setSelected(false);
        return button;
    }
    public void start(int dificulty){
        generatePlanets((int)(Math.random()*10)+4,dificulty);
        t = new Thread(this);
        t.start();
        if(dificulty == 0)
            easy = new EasyAI();
        else if(dificulty == 1)
            normal = new MediumAI();
        else if(dificulty == 2)
            hard = new HardAI();
    }
    public void start(int dificulty,int planets){
        generatePlanets(planets,dificulty);
        t = new Thread(this);
        t.start();
        if(dificulty == 0)
            easy = new EasyAI(true);
        else if(dificulty == 1)
            normal = new MediumAI(true);
        else if(dificulty == 2)
            hard = new HardAI(true);
    }
    
    //Setup Methods
    //-------------
    private void setupListeners(){       
        addMouseListener(this);
        addMouseMotionListener(this);
        requestFocus();
    }
    
    //Planet Generation Methods
    private void generatePlanets(int num,int dificulty){
        int[] added = {5,10,0};
        for(int i=0; i<num; i++){
            Planet p = null;
            while(p ==null || !planetIsValid(p)){
                if(i == 0)
                    p = new Planet(randomX(),randomY(),Vars.playerColor,Vars.startArmy,true,0);
                else if(i == 1)
                    p = new Planet(randomX(),randomY(),Vars.enemyColor,Vars.startArmy+added[dificulty],true,1);
                else{
                    int army = (int)(Math.random()*50)+25;
                    p = new Planet(randomX(),randomY(),Vars.neutralColor,army,false,2);
                }
            }
            planets.add(p);
        }
    }
    private int randomX(){
        return (int)(Math.random()*(Vars.appWidth-Vars.maxPlanetSize))
                    +(Vars.maxPlanetSize/2+1);
    }
    private int randomY(){
        return (int)(Math.random()*(Vars.appHeight-Vars.maxPlanetSize-10))
                    +(Vars.maxPlanetSize/2-10);
    }
    private boolean planetIsValid(Planet p){
        for(Planet plan:planets)
            if(plan.isOverlaping(p))
                return false;
        return true;
    }
    
    //Attack method
    private void moveSelectedTo(Planet planet){
        for(Planet p: selectedPlanets)
            if(p != planet)
                p.sendTroops(planet,Vars.percentToSend);
    }
    
    public void cleanComets(){
        for(int i=0; i<comets.size();i++){
            if(comets.get(i).hasArrived()){
                comets.remove(i);
                i--;
            }
        }
    }
    
    private void cleanSquads(){
        for(int i=0; i<squads.size(); i++)
            if(squads.get(i).hasArrived()){
                //Transfer troops
                Planet dest = squads.get(i).getDestination();
                if((dest.isPlayer() != squads.get(i).isPlayer())
                        || (!dest.isEnemy() && !dest.isPlayer()))
                    attack(squads.get(i),dest);
                else
                    support(squads.get(i),dest);
                squads.remove(i);
                i--;
            }      
    }

    
    
    public void attack(Squad s, Planet dest){
        int troops = s.getTroops();
        if(dest.isEnemy() || dest.isPlayer())
            detractTroops(dest, troops);
        else
            dest.setArmy(dest.getArmy()-s.getTroops());
        //Convert if needed
        if(dest.getArmy() < 0){
            if(s.isPlayer()){
                dest.setColor(Vars.playerColor); 
                dest.makePlayer();
            }   
            else{
                dest.setColor(Vars.enemyColor);
                dest.makeEnemy();
            }
                dest.setArmy(Math.abs(dest.getArmy()));    
        }
    }
    private void detractTroops(Planet p, int t){
        double factor = p.getArmy()*Vars.factor;
        if(factor <= 0)
            factor = 1;
        else if(factor < Vars.minFactor)
            factor = Vars.minFactor;
        p.setArmy((int)(p.getArmy()-(t*factor)));
    }
    
    public void support(Squad s, Planet dest){
        dest.setArmy(dest.getArmy()+s.getTroops());
    }
    public void checkGame(){
        int player = 0;
        int enemy = 0;
        for(Planet p:planets)
            if(p.isEnemy())
                enemy++;
            else if(p.isPlayer())
                player++;
        if(player == 0 || enemy == 0)
            for(Squad s:squads)
                if(!s.isPlayer())
                    enemy++;
                else if(s.isPlayer())
                    player++;
        if(enemy == 0){
            gameOver = 1;
        }
        else if(player == 0){
            gameOver = 2;
        }
            
    }
    
    private void deselectPlanets(){
        selectedPlanets.clear();
    }
    //Component Methods
    //-----------------
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(Vars.appWidth,Vars.appHeight);
    }
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        try{
            //Draw Comets
            if(Vars.comets){
                for(Comet c:comets)
                    c.draw(g);
            }
            //Draw planets and squads
            for(Planet p:planets)
                p.draw(g); 
            for(Squad s:squads)
                s.draw(g);
        }catch(Exception e){}
        //Draw Lines for commands
        if(mousePressed)
            for(Planet p:selectedPlanets){
                g.setColor(Color.WHITE);
                g.drawLine(p.getX(), p.getY(), mouseX, mouseY);
            }
        //Draw Writing in top Left
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, 120, 18);
        g.setColor(Color.BLACK);
        
        if(gameOver == 1){
            g.drawString("YOU WIN", 3, 12);
            g.setColor(Color.WHITE);
            g.drawString("Click to return to menu...",6,33);
        }
        else if(gameOver == 2){
            g.drawString("GAME OVER", 3, 12);
            g.setColor(Color.WHITE);
            g.drawString("Click to return to menu...",6,33);
        }
        else
            g.drawString("Attack with "+Vars.percentToSend+"%", 6, 12);
        
                
    }
    
    
    
    //Thread
    //------
    @Override
    public void run() {        
        while(!exit){
            try {
                Thread.sleep(Vars.refreshRate);
                cleanSquads();
                if(Vars.comets)
                    cleanComets();
                if(comets.size() < maxComets && Vars.comets){
                    if((int)(Math.random()*(15*Vars.refreshRate)) == 0){
                        if(Math.random() <= .5)
                            comets.add(new Comet(-100,(int)(Math.random()*Vars.appHeight)));
                        else
                            comets.add(new Comet((int)(Math.random()*Vars.appHeight),-100));
                    }     
                }
                repaint();
                checkGame();
            } catch (InterruptedException ex) {}
        }
        repaint();
        
    }
    
    
    public void cyclePercent(){
        int[] percents = Vars.percents;
        int i = Arrays.binarySearch(percents, Vars.percentToSend);
        if(i+1<percents.length)
            Vars.percentToSend = percents[i+1];
        else
            Vars.percentToSend = percents[0];
    }
    //Mouse Listener
    //--------------
    @Override
    public void mouseClicked(MouseEvent me) {
        if(me.getButton()==MouseEvent.BUTTON3){
            cyclePercent();
        }
    }

    @Override
    public void mousePressed(MouseEvent me) {
        if(me.getButton() != MouseEvent.BUTTON3 && gameOver == 0){
            mousePressed = true;
            for(Planet p:planets)
                if(p.contains(me.getX(), me.getY()) && p.isPlayer()){
                    if(p == touched){
                        for(Planet pl:planets){
                            if(pl.isPlayer())
                                selectedPlanets.add(pl);
                        }
                    }
                    else{
                        touched = p;
                        selectedPlanets.add(p);
                        mouseX = p.getX();
                        mouseY = p.getY();
                    }
                
                    break;
                }
        }
        else if(me.getButton() != MouseEvent.BUTTON3)
            exit = true;    
    }

    @Override
    public void mouseReleased(MouseEvent me) {
        if(me.getButton() != MouseEvent.BUTTON3){
            mousePressed = false;
            Planet planet = null;
            for(Planet p:planets)
                if(p.contains(me.getX(), me.getY()))
                    planet = p;
            if(planet != null && planet != touched)
                moveSelectedTo(planet);
            if(planet != touched)
                deselectPlanets();
            if(planet != touched)
                touched = null;
        }
    }

    //Mouse Motion Listener
    //---------------------
    @Override
    public void mouseEntered(MouseEvent me) {
        
    }

    @Override
    public void mouseExited(MouseEvent me) {
        
    }

    @Override
    public void mouseDragged(MouseEvent me) {
        mouseX = me.getX();
        mouseY = me.getY();
    }

    @Override
    public void mouseMoved(MouseEvent me) {
        
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        exit = true;
    }
    
    

    
}
