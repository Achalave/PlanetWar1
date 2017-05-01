
package gameplay;
import interfaces.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import planetwar1.Vars;

public class Explosion implements Runnable, Drawable{
    private Polygon exp1;
    private Polygon exp2;
    private Polygon exp3;
    private int x,y;
    private boolean exploding = true;
    Thread t;
    double countdown;
    
    public Explosion(int x, int y){
        this.x = x;
        this.y = y;
        countdown = (Math.random())+1.25;
        generateExplosion();
        randomizeExplosion();
        placeExplosion();
    }
    
    public boolean isExploding(){
        return exploding;
    }
    
    public void generateExplosion(){
        exp1 = new Polygon();
        exp2 = new Polygon();
        exp3 = new Polygon();
        Polygon[] p = {exp1,exp2,exp3};
        for(int i=0; i<3; i++){
            double angle = (int)(Math.random()*45);   
            int times = (int)(Math.random()*5)+4;
            for(int k=0; k<times; k++){
                int r = 50-(15*i);
                if(k%2 == 0)
                    r /= 2;
                int x = (int)Math.round(r*Math.cos(angle));
                int y = (int)Math.round(r*Math.sin(angle));
                p[i].addPoint(x, y);
                angle+=360.0/times;
            }
        }
        
        
    }
    
    private void randomizeExplosion(){
        Polygon[] p = {exp1,exp2,exp3};
        int[] adjust = {-2,-1,0,1,2,3,4};
        for(int i=0; i<3; i++){
            for(int k=0; k<p[i].npoints; k++){
                p[i].xpoints[k] += adjust[(int)(Math.random()*adjust.length)];
                p[i].ypoints[k] += adjust[(int)(Math.random()*adjust.length)];
            }
        }
        
    }
    
    private void placeExplosion(){
        for(int i=0; i<exp1.npoints; i++){
            exp1.xpoints[i] += x;
            exp1.ypoints[i] += y;
        }
        for(int i=0; i<exp2.npoints; i++){
            exp2.xpoints[i] += x;
            exp2.ypoints[i] += y;
        }
        for(int i=0; i<exp3.npoints; i++){
            exp3.xpoints[i] += x;
            exp3.ypoints[i] += y;
        }
    }
    
    public void start(){
        if(t == null || !t.isAlive()){
            t = new Thread(this);
            t.start();
        }
    }
    
    @Override
    public void run() {
        
        int sleep = 100;
        try{
            while(exploding){
                Thread.sleep(sleep);
                generateExplosion();
                placeExplosion();
                randomizeExplosion();
                countdown -= (sleep/1000.0);
                if(countdown <= 0)
                    exploding = false;
            }
        }catch(InterruptedException e){} 
    }

    @Override
    public void draw(Graphics g) {
        start();
        if(exploding){
            g.setColor(Color.RED);
            g.fillPolygon(exp1);
            g.setColor(Color.ORANGE);
            g.fillPolygon(exp2);
            g.setColor(Color.YELLOW);
            g.fillPolygon(exp3);
        }
    }
    
}
