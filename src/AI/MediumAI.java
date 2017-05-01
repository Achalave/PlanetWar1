
package AI;
import gameplay.Planet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import planetwar1.GameplayView;
import planetwar1.Vars;

public class MediumAI implements Runnable{
    
    
    public static Planet current = null;
    
    ArrayList<Planet> planets = new ArrayList<Planet>();
    ArrayList<Planet> enemies = new ArrayList<Planet>();
    ArrayList<Planet> neutral = new ArrayList<Planet>();
    int countdown = 400;
    boolean shouldRun = true;
    Thread t;
    
    public MediumAI(){
        Vars.speedEnemy = 85;
        Vars.speedPlayer = 85;
        Vars.armyIncreaseRate = 1.5;
        gatherPlanets();
        startTimeSync();
    }
    public MediumAI(boolean b){
        gatherPlanets();
        startTimeSync();
    }
    //Refreshes the planet catagories
    private void gatherPlanets(){
        planets.clear();
        enemies.clear();
        neutral.clear();
        for(Planet p:GameplayView.planets){
            if(p.isEnemy())
                planets.add(p);
            else if(p.isPlayer())
                enemies.add(p);
            else if(!p.isEnemy() && !p.isPlayer())
                neutral.add(p);
        }
        if(planets.size()>1)
            Collections.sort(planets);
        if(enemies.size()>1)
            Collections.sort(enemies);
        if(neutral.size() > 1)
            Collections.sort(neutral);
    }
    //Retrieves the best enemy to attack
    private Planet getBestEnemy(){
        Planet best = null;
        for(Planet p:enemies){
            if(p.getArmy() < 20)
                return p;
            if(best == null||calculateFactor(p)>calculateFactor(best))
                best = p;
        }
        return best;
    } 
    private double calculateFactor(Planet p){
        if(p != null){
            double factor = (int)(p.getArmy()*Vars.factor);
            if(factor <= 0)
                factor = 1;
            else if(factor < .7)
                factor = .7;
            return factor;
        }
        return -1;
    }
    
    private Planet getBestNeutral(Planet p){
        if(!neutral.isEmpty()){
        current = p;
        ArrayList<Planet> temp = new ArrayList<Planet>();
        temp.add(neutral.get(0));
        for(int i=1; i<neutral.size(); i++)
            if(neutral.get(i).getArmy() <= temp.get(0).getArmy()+10)
                temp.add(neutral.get(i));
        //Collections.sort(temp, );
        Collections.sort(temp, new Comparator() {
            public int compare(Object o1, Object o2) {
                Planet a = (Planet)o1;
                Planet b = (Planet)o2;
                if(getDistance(a,MediumAI.current)<getDistance(b,MediumAI.current))
                    return 1;
                return -1;
            }
        });
        return temp.get(0);
        }
        return null;
    }
    
    public void reinforce(){
        ArrayList<Planet> temp = new ArrayList<Planet>(); 
        for(Planet p:planets)
            if(p.getArmy()<=10)
                temp.add(p);
        for(Planet p:temp){
            Planet best = null;
            for(Planet k:planets){
                if(k.getArmy() > 15)
                    if(k != p &&(best == null||(k != p && getDistance(p,k) < getDistance(best,p))))
                        best = k;
            }
            if(best != null)
                best.sendTroops(p, 25);
        }
    }
    
    private double getDistance(Planet p, Planet p2){
        double x = Math.pow((double)p.getX()-p2.getX(),2);
        double y = Math.pow((double)p.getY()-p2.getY(),2);
        return Math.pow(x+y, 1/2);
    }
            
            
    //TimeSync
    //--------
    private void startTimeSync(){
        if(t==null || !t.isAlive()){
            shouldRun = true;
            t = new Thread(this);
            t.start();
        }
    }
    
    private void stopTimeSync(){
        shouldRun = false;
    }
    
    int temp = 0;

    public void timeExpired() {
        if(GameplayView.gameOver > 0)
            stopTimeSync();
        else{
            gatherPlanets();
            reinforce();
            for(Planet p:planets){
                if(p.getArmy()>20){
                    Planet e = getBestEnemy();
                    Planet n = getBestNeutral(p);
                    boolean attackPlayer = false;
                    if(n == null || (e != null&&getDistance(p,e) < getDistance(p,n))
                            || calculateFactor(e) >= 3.5)
                        attackPlayer = true;
                    else if(n.getArmy() > 55 || n.getArmy() < 15 
                            || planets.size()-2 > enemies.size()
                            || enemies.size()-2 > planets.size())
                        attackPlayer = true;
                    
                    if(attackPlayer)
                        p.sendTroops(e, 25);
                    else
                        p.sendTroops(n, 25);
                }
            }
        }
    }

    @Override
    public void run() {
        long previous = 0;
        while(shouldRun){
            try{
                previous = System.currentTimeMillis();
                while(System.currentTimeMillis()-previous < countdown)
                    Thread.sleep(20);
                timeExpired();
            }catch(InterruptedException e){}
        }
    }
    
    
}
