
package gameplay;
import interfaces.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.geom.Area;
import java.util.ConcurrentModificationException;
import planetwar1.GameplayView;
import planetwar1.MenuView;
import planetwar1.Vars;

public class Comet implements Drawable,Runnable{

    private static final Color[] colors = {Color.DARK_GRAY, Color.LIGHT_GRAY,
        Color.GRAY};
    private Polygon comet = new Polygon();
    Explosion e;
    private Color color; 
    private double x,y,desX,desY,speed;
    private int countdown = Vars.cometCountdownStart;
    boolean arrived = false;
    boolean exploding = false;
    private double count = 0;
    private boolean shouldRun = true;
    private Thread t;
    double counter = 0;
    //Which view the commet is in
    int place = -1;
    
    public Comet(int x, int y){
        //Determine Color
        color = colors[(int)(Math.random()*colors.length)];
        
        this.x = x;
        this.y = y;
        
        generateComet();
        randomizeComet();
        placeComet();
        //Determine destination
        if(x < 0){
            desX = Vars.appWidth+100;
            desY = (int)(Math.random()*Vars.appHeight);
        }
        else if(y >= 0){
            desX = -100;
            desY = (int)(Math.random()*Vars.appHeight);
        }
        else if(y < 0 && x >= 0){
            desY = Vars.appHeight+100;
            desX = (int)(Math.random()*Vars.appWidth);
        }
        else{
            desY = -100;
            desX = (int)(Math.random()*Vars.appWidth);
        }
        
        //Determine Speed
        speed = (int)(Math.random()*Vars.maxCometSpeed)+50;
        
        startTimeSync();
        
    }
    
    public Polygon getShape(){
        return comet;
    }
    
    private void generateComet(){
        comet.addPoint(0, 0);
        comet.addPoint(5, -2);
        comet.addPoint(10, 1);
        comet.addPoint(15, 5);
        comet.addPoint(20, 10);
        comet.addPoint(17, 17);
        comet.addPoint(13, 19);
        comet.addPoint(10, 20);
        comet.addPoint(5, 20);
        comet.addPoint(2, 16);
    }
    
    
    private void randomizeComet(){
        int[] adjust = {-2,-1,0,1,2,3,4,5,8};
        for(int i=0; i<comet.npoints; i++){
            comet.xpoints[i] += adjust[(int)(Math.random()*adjust.length)];
            comet.ypoints[i] += adjust[(int)(Math.random()*adjust.length)];
        }
    }
    
    private void placeComet(){
        for(int i=0; i<comet.npoints; i++){
            comet.xpoints[i] += x;
            comet.ypoints[i] += y;
        }
            
    }
    
    public void explode(){
        if(place == 0){
            e = new Explosion(comet.xpoints[0], comet.ypoints[0]);
            MenuView.explosions.add(e);
            exploding = true;
            shouldRun = false;
            arrived = true;
        }
    }
    
    @Override
    public void draw(Graphics g) {
        if(MenuView.comets.contains(this)){
            place = 0;
        }
        else
            place = 1;
        if(!exploding){
            g.setColor(color);
            g.fillPolygon(comet);
        }
    }
    
    public void move(){
        double time = Vars.cometCountdownStart/1000.0;
        double distance = Math.sqrt(Math.pow(desX-x,2)+Math.pow(desY-y,2));
        for(int i=0; i<comet.npoints; i++){
            comet.xpoints[i] += (speed*time)*(desX-x)/distance;
            comet.ypoints[i] += (speed*time)*(desY-y)/distance;
        }
        
        if(count > 3 && isOffscreen()){
            stopTimeSync();
            arrived = true;
        }
    }
    
    public boolean isOffscreen(){
        for(int i=0; i<comet.npoints; i++){
            int xp = comet.xpoints[i];
            int yp = comet.ypoints[i];
            if((xp > 0 && xp < Vars.appWidth)&&(yp > 0 && yp<Vars.appHeight)){
                return false;
            }
        }
        return true;
    }
    
    //Accessors
    public boolean hasArrived(){
        if(!exploding)
            return arrived;
        else
            return !e.isExploding();
    }
    public int getX(){
        return (int)Math.round(x);
    }
    public int getY(){
        return (int)Math.round(y);
    }
    @Override
    public void run() {
        long previous;
        while(shouldRun){
            try{
                previous = System.currentTimeMillis();
                while(System.currentTimeMillis()-previous < countdown)
                    Thread.sleep(20);
                    timeExpired();
                    checkCollision();
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
        count += (double)Vars.cometCountdownStart/1000;
        move();
    }
    
    private void checkCollision(){
        try{
            
            for(Comet c:MenuView.comets){
                if(c != this){
                    for(int i=0; i<comet.npoints; i++){
                        Area a = new Area(comet);
                        a.intersect(new Area(c.getShape()));
                        if(!a.isEmpty()){  
                            this.explode();
                            return;
                        }
                    }
                }
            }
            for(Comet c:GameplayView.comets){
                if(c != this){
                    int[] x = comet.xpoints;
                    int[] y = comet.ypoints;
                    for(int i=0; i<comet.npoints; i++){
                        Area a = new Area(comet);
                        a.intersect(new Area(c.getShape()));
                        if(!a.isEmpty()){  
                            this.explode();
                            c.arrived = true;
                            return;
                        }
                    }
                }
            }
        }catch(ConcurrentModificationException e){
            this.explode();
        }
    }
}
