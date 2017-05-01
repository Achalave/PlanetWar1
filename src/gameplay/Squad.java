
package gameplay;
import interfaces.Drawable;
import java.awt.Color;
import java.awt.Graphics;
import planetwar1.Vars;

public class Squad implements Drawable,Runnable{
    
    private int countdown = Vars.squadCountdownStart;
    private double curX,curY,desX,desY;
    private Planet dest;
    private int troops;
    private Color color;
    private boolean arrived = false;
    private boolean shouldRun = true;
    private Thread t;
    private boolean isPlayer;
    private boolean isEnemy;
    public Squad(Planet s,int x,int y,Planet des,int t,Color c, boolean player){
        curX = x;
        curY = y;
        dest = des;
        desX = des.getX();
        desY = des.getY();
        troops = t;
        color = c;
        isPlayer = player;
        isEnemy = !player;
        startTimeSync();
    }
    
    public void move(){
        double time = Vars.squadCountdownStart/1000.0;
        double distance = Math.sqrt(Math.pow(desX-curX,2)+Math.pow(desY-curY,2));
        if(isPlayer()){
            curX += (Vars.speedPlayer*time)*(desX-curX)/distance;
            curY += (Vars.speedPlayer*time)*(desY-curY)/distance;
        }
        else{
            curX += (Vars.speedEnemy*time)*(desX-curX)/distance;
            curY += (Vars.speedEnemy*time)*(desY-curY)/distance;
        }
        if(dest.contains((int)Math.round(curX), (int)Math.round(curY))){
            stopTimeSync();
            arrived = true;
        }
    }
    
    //Accessor Methods
    //----------------
    public int getTroops(){return troops;}
    public boolean hasArrived(){return arrived;}
    public Planet getDestination(){return dest;}
    public boolean isPlayer(){return isPlayer;}
    //public Color getColor(){return color;}
    
    //Drawable
    //--------
    @Override
    public void draw(Graphics g) {
        g.setColor(color);
        int d = Vars.diameter;
        g.fillOval((int)Math.round(curX),(int)Math.round(curY),d,d);
        g.setColor(Color.YELLOW);
        g.drawOval((int)Math.round(curX),(int)Math.round(curY),d,d);
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
        move();
    }

}
