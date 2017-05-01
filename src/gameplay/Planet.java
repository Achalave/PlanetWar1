
package gameplay;
import interfaces.Drawable;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import planetwar1.GameplayView;
import planetwar1.Vars;

public class Planet implements Drawable, Comparable<Planet>,Runnable{
    
    //Variables
    private Color color;
    
    private int countdown = Vars.countdownStart;
    private int army;
    private int size;
    private int x,y;
    private boolean growth = false;
    private boolean player = false;
    private boolean enemy = false;
    private boolean shouldRun = true;
    Thread t;
    //Constructor
    public Planet(int x, int y,Color c, int a, boolean g,int t){
        color = c;
        if(t == 0)
               player = true;
        else if(t == 1)
            enemy = true;
        this.x = x;
        this.y = y;
        army = a;
        growth = g;
        adjustSize();
        if(growth)
            startTimeSync();
    }
    
    //Accessor Methods
    //----------------
    public int getArmy(){return army;}
    //public Color getColor(){return color;}
    public boolean getGrowth(){return growth;}
    public int getX(){return x;}
    public int getY(){return y;}
    public int getSize(){return size;}
    public boolean isPlayer(){return player;}
    public boolean isEnemy(){return enemy;}
    
    public void setArmy(int a){army = a;}
    public void setColor(Color c){color = c;}
    public void setGrowth(boolean g){
        if(g != growth){
            if(g)
                startTimeSync();   
            else
                stopTimeSync();
        }
        growth = g;
    }
    public void makePlayer(){
        player = true;
        enemy = false;
        setGrowth(true);
    }
    public void makeEnemy(){
        player = false;
        enemy = true;
        setGrowth(true);
    }
    
    //Gameplay Methods
    //----------------
    public void adjustSize(){
        size = army/5;
        if(size < Vars.minPlanetSize)
            size = Vars.minPlanetSize;
        else if(size > Vars.maxPlanetSize)
            size = Vars.maxPlanetSize;
    }
    
    public boolean contains(int x, int y){
        double term1 = Math.pow(x-this.x, 2);
        double term2 = Math.pow(y-this.y, 2);
        double distance = Math.sqrt(term1+term2);
        if(distance <= size/2)
            return true;
        return false;
    }
    public boolean isOverlaping(Planet p){
        int size = Vars.maxPlanetSize;
        Rectangle r = new Rectangle(x-(size/2),y-(size/2),size,size);
        Rectangle r2 = new Rectangle(p.getX()-(size/2),
                p.getY()-(size/2),size,size);
        if(r.intersects(r2))
            return true;
        return false;
    }
    

    
    public void sendTroops(Planet p,int percent){
        if(p == null)
            return;
        int outTroops = (int)(army*(percent/100.0));
        army -= outTroops;
        adjustSize();
        int numberSquads = (int)Math.ceil((double)outTroops/Vars.maxPerSquad);
        int angle = (45*((int)(Math.random()*2)+1));
        for(int i=0; i<numberSquads; i++){
            int inSquad = Vars.maxPerSquad;
            if(inSquad > outTroops)
                inSquad = outTroops;
            outTroops -= inSquad;
            //Place Squad
            if(i == 0)
                GameplayView.squads.add(new Squad(this,x,y,p,inSquad,color,player));
            else{
                int level = (i/4)+1;
                int radius = level*Vars.diameter;
                int x = (int)Math.round((radius*Math.sin(angle)) + this.x);
                int y = (int)Math.round((radius*Math.cos(angle)) + this.y);
                angle += 90;
                if(i/4 > (i-1)/4)
                    angle+=45;
                GameplayView.squads.add(new Squad(this,x,y,p,inSquad,color,player));
            }
        } 
    }

    
    //Draw Method
    //-----------
    @Override
    public void draw(Graphics g) {
        //Draw Main Circle
        g.setColor(color);
        g.fillOval(x-(size/2), y-(size/2), size, size);
        
        //Draw Outline
        if(player && GameplayView.selectedPlanets.contains(this))
            g.setColor(Color.MAGENTA);
        else
            g.setColor(Color.LIGHT_GRAY);
        g.drawOval(x-(size/2), y-(size/2), size, size);
        
        //Display Army
        g.setColor(Color.YELLOW);
        g.drawString(army+"", x-(size/2), y+(size));
    }
    
    
    //TimeSync
    //--------
    
    @Override
    public void run() {
        long previous;
        while(shouldRun){
            try{
                previous = System.currentTimeMillis();
                while(System.currentTimeMillis()-previous < countdown)
                    Thread.sleep(20);
                timeExpired();
            }catch(InterruptedException e){}
        }
    }
    
    public void startTimeSync(){
        if(t==null || !t.isAlive()){
            shouldRun = true;
            t = new Thread(this);
            t.start();
        }
    }
    
    public void stopTimeSync(){
        shouldRun = false;
    }
    
    public void timeExpired() {
        //need to incriment troops
        int add = (int)Math.round(army*((Vars.armyIncreaseRate/100)*(Vars.countdownStart/1000)));
        if(add <= 0)
            add = 1;
        army += add;
        adjustSize();
    }
    
    public int getCountdown() {
        return countdown;
    }

    public void setCountdown(int c) {
        countdown = c;
    }

    @Override
    public int compareTo(Planet t) {
        if(t.getArmy() == army)
            return 0;
        if(t.getArmy() > army)
            return -1;
        return 1;
    }
    
}
