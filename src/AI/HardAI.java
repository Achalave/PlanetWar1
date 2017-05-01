
package AI;
import gameplay.Planet;
import gameplay.Squad;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import planetwar1.GameplayView;
import planetwar1.Vars;

public class HardAI implements Runnable{
    
    
    public static Planet current = null;
    
    ArrayList<Planet> planets = new ArrayList<Planet>();
    ArrayList<Planet> enemies = new ArrayList<Planet>();
    ArrayList<Planet> neutral = new ArrayList<Planet>();
    ArrayList<Planet> underAttack = new ArrayList<Planet>();
    Planet giant = null;
    int countdown = 400;
    int optimumArmy;
    boolean shouldRun = true;
    Thread t;
    double counter = 0;
    
    public HardAI(){
        Vars.speedEnemy = 100;
        Vars.speedPlayer = 100;
        Vars.armyIncreaseRate = 2;
        for(int i=1; calculateFactor(i)<=Vars.minFactor;i++)
            optimumArmy = i;
        optimumArmy += 7;
        gatherPlanets();
        startTimeSync();
    }
    public HardAI(boolean b){
        for(int i=1; calculateFactor(i)<=Vars.minFactor;i++)
            optimumArmy = i;
        optimumArmy += 7;
        gatherPlanets();
        startTimeSync();
    }
    //Refreshes the planet catagories
    private void gatherPlanets(){
        planets.clear();
        enemies.clear();
        neutral.clear();
        underAttack.clear();
        //Fill all of them
        for(Planet p:GameplayView.planets){
            if(p.isEnemy())
                planets.add(p);
            else if(p.isPlayer())
                enemies.add(p);
            else if(!p.isEnemy() && !p.isPlayer())
                neutral.add(p);
        }
        //Sort all of them by army
        if(planets.size()>1)
            Collections.sort(planets);
        if(enemies.size()>1)
            Collections.sort(enemies);
        if(neutral.size() > 1)
            Collections.sort(neutral);
        //Collect the planets under attack
        ArrayList<Squad> squads = GameplayView.squads;
        for(int i=0; i<squads.size();i++){
            Squad s = squads.get(i);
            if(s.isPlayer() && s.getDestination().isEnemy())
                if(!underAttack.contains(s.getDestination()))
                    underAttack.add(s.getDestination());
        }
    }
    
    //Retrieves the best enemy to attack
    private Planet getBestEnemy(){
        Planet best = null;
        for(Planet p:enemies){
            if(p.getArmy() < 20)
                return p;
            if(best == null||calculateFactor(p.getArmy())>calculateFactor(best.getArmy()))
                best = p;
        }
        return best;
    } 
    private double calculateFactor(int p){
        double factor = (p*Vars.factor);
        if(factor <= 0)
            factor = 1;
        else if(factor < Vars.minFactor)
            factor = Vars.minFactor;
        return factor;
    }
    
    private Planet getBestNeutral(Planet p){
        if(neutral.size()<=1)
            return null;
        current = p;
        ArrayList<Planet> temp = new ArrayList<Planet>();
        temp.add(neutral.get(0));
        for(int i=1; i<neutral.size(); i++)
            if(neutral.get(i).getArmy() <= temp.get(0).getArmy()+10)
                temp.add(neutral.get(i));
        Collections.sort(temp, new Comparator() {
            public int compare(Object o1, Object o2) {
                Planet a = (Planet)o1;
                Planet b = (Planet)o2;
                if(getDistance(a,HardAI.current)<getDistance(b,HardAI.current))
                    return 1;
                return -1;
            }
        });
        if(temp.get(0).getArmy()>50)
            return null;
        return temp.get(0);

    }
    
    public void reinforce(){
        ArrayList<Planet> temp = new ArrayList<Planet>(); 
        for(Planet p:planets)
            if(p.getArmy()<=10 || (underAttack.contains(p) && p.getArmy()<optimumArmy))
                temp.add(p);
        for(Planet p:temp){
            Planet best = null;
            for(Planet k:planets){
                if(k.getArmy() > 15 && !underAttack.contains(k))
                    if(k != p &&(best == null||(k != p && getDistance(p,k) < getDistance(best,p))))
                        best = k;
            }
            if(best != null)
                best.sendTroops(p, 25);
        }
    }
    
    public void evaid(){
        for(Planet p:underAttack){
            if(p.getArmy() >= optimumArmy){
                Planet evacSite = null;
                if(giant != null && !underAttack.contains(giant))
                    evacSite = giant;
                else if(planets.size() > 1)
                    for(Planet k:planets)
                        if(k != p && !underAttack.contains(k) 
                                && (evacSite == null || k.getArmy()<evacSite.getArmy()))
                            evacSite = k;
                double percent = optimumArmy/(double)p.getArmy();
                percent -= 1;
                percent *= 100;
                percent = Math.ceil(-percent);
                if(evacSite != null)
                    p.sendTroops(evacSite, (int)percent);
                else
                    p.sendTroops(getBestEnemy(), (int)percent);
            }
        }
    }
    
    private int getTotalTroops(){
        int counter = 0;
        for(Planet p:planets)
            counter += p.getArmy();
        for(Squad s:GameplayView.squads)
            if(!s.isPlayer())
                counter += s.getTroops();
        return counter;
    }
    private int getTotalPlayerTroops(){
        int counter = 0;
        for(Planet p:enemies)
            counter += p.getArmy();
        for(Squad s:GameplayView.squads)
            if(s.isPlayer())
                counter += s.getTroops();
        return counter;
    }
    
    private double getDistance(Planet p, Planet p2){
        double x = Math.pow((double)p.getX()-p2.getX(),2);
        double y = Math.pow((double)p.getY()-p2.getY(),2);
        return Math.pow(x+y, 1/2);
    }
            
    private void designateGiant(){
        if(planets.size()>3 || getBestNeutral(planets.get(0)) == null 
                && giant == null){
            ArrayList<Integer> distance = new ArrayList<Integer>();
            for(int i=0; i<planets.size(); i++){
                distance.add(0);
                for(Planet p:enemies)
                    distance.set(i, (int)Math.round(distance.get(i)
                            +getDistance(planets.get(i),p)));
            }
            int smallest = 0;
            for(int i=0;i<distance.size();i++){
                if(giant==null||smallest<distance.get(i)){
                    smallest = i;
                    giant = planets.get(i);
                } 
            }
        }
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

    public void timeExpired() {
        if(GameplayView.gameOver > 0)
            stopTimeSync();
        else{
            gatherPlanets();
            reinforce();
            designateGiant();
            evaid();
            if(getTotalTroops() < 35 && getTotalTroops() < getTotalPlayerTroops()
                    && planets.size()>1)
                return;
            for(Planet p:planets){
                Planet e = getBestEnemy();
                Planet n = null;
                if(counter > 7)
                    n = getBestNeutral(p);
                //Good Neutral oppertunity
                if(n != null && n.getArmy()<10){
                    p.sendTroops(n, 10);
                }
                //Reballance planets
                if(planets.size()<enemies.size() 
                        && getTotalTroops() < getTotalPlayerTroops()){
                        p.sendTroops(e, 25);
                }
                //Attack with big, Mostly for beginning game
                else if(p.getArmy()>120){
                    p.sendTroops(e, 50);
                }
                //Finish off player
                else if(p.getArmy() > 10
                        && p.getArmy() > e.getArmy()
                        && planets.size() >= enemies.size()){
                    p.sendTroops(e, 50);
                }
                //Instigate Hardcore Tactics
                else if(p.getArmy() > optimumArmy
                        && (GameplayView.planets.size()<7
                        || (e.getArmy()<20 && getDistance(e,p) <= 100))){
                    p.sendTroops(e,50);     
                }
                else if(p.getArmy()>optimumArmy && giant != p && !underAttack.contains(p)){
                    //List of conditions under which it is best to attack player
                    if(n == null 
                            || (e != null&&getDistance(p,e) < getDistance(p,n))
                            || n.getArmy() > 55 
                            || n.getArmy() < 15 
                            || planets.size()-2 > enemies.size()
                            || enemies.size()-2 > planets.size()
                            || (e != null&&calculateFactor(e.getArmy()) >= 3.5))
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
                counter += .02;
                timeExpired();
            }catch(InterruptedException e){}
            catch(IndexOutOfBoundsException e){}
            catch(NullPointerException e){}
        }
    }
    
    
}
