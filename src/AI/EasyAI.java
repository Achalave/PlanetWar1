
package AI;
import gameplay.Planet;
import java.util.ArrayList;
import java.util.Collections;
import planetwar1.GameplayView;
import planetwar1.Vars;


public class EasyAI implements Runnable{
    
    ArrayList<Planet> planets = new ArrayList<Planet>();
    ArrayList<Planet> enemies = new ArrayList<Planet>();
    ArrayList<Planet> neutral = new ArrayList<Planet>();
    int countdown = 400;
    private Thread t;
    private boolean shouldRun = true;
    
    public EasyAI(){
        Vars.speedEnemy = 80;
        Vars.speedPlayer = 80;
        Vars.armyIncreaseRate = 1;
        gatherPlanets();
        startTimeSync();
    }
    public EasyAI(boolean b){
        gatherPlanets();
        startTimeSync();
    }
    
    private void gatherPlanets(){
        planets.clear();
        enemies.clear();
        neutral.clear();
        for(Planet p:GameplayView.planets){
            if(p.isEnemy())
                planets.add(p);
            else if(p.isPlayer())
                enemies.add(p);
            else
                neutral.add(p);
            Collections.sort(planets);
            Collections.sort(enemies);
            Collections.sort(neutral);
        }
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
        if(GameplayView.gameOver > 0)
            stopTimeSync();
        else{
            try{
                gatherPlanets();
                //Attack the weakest enemy
                if(enemies.size() >= neutral.size()){
                    //Dont let the planets get too depleted
                    if(planets.get(planets.size()-1).getArmy() > 25)
                        planets.get(planets.size()-1).sendTroops
                                (enemies.get(enemies.size()-1),25);
                }
                //Attack the weakest neutral
                else{
                    if(planets.get(planets.size()-1).getArmy() > 25)
                        planets.get(planets.size()-1).sendTroops(neutral.get(0),25);
                }
            }catch(java.lang.Exception e){}
        }
    }


    public int getCountdown() {
        return countdown;
    }


    public void setCountdown(int c) {
        countdown = c;
    }
    
    
}
