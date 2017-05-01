
package planetwar1;

import gameplay.Planet;
import gameplay.Squad;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

public class HowToPlayView extends GameplayView{
    int phase = 0;
    int textX = 50;
    int textY = 50;
    int textX2 = 50;
    int textY2 = 50;
    String message = "Right Click To Cycle Through Percent To Send";
    
    public HowToPlayView(){
        super();
    }
    
    public void start(int i){
        generatePlanets();
        t = new Thread(this);
        t.start();
    }
    
    public void generatePlanets(){
        planets.add(new Planet(Vars.appWidth/3,Vars.appHeight/3,
                Color.BLUE,Vars.startArmy,true,0));
        planets.add(new Planet(Vars.appWidth-(Vars.appWidth/3),Vars.appHeight-(Vars.appHeight/3),
                Color.RED,Vars.startArmy,true,1));
    }
    
    @Override
    public void cyclePercent(){
        if(phase == 0){
            phase = 1;
            moveText("Click And Drag From The Blue To The Red Planet To Attack", 50, 200);
        }
        super.cyclePercent();
    }
    
    @Override
    public void attack(Squad s, Planet dest){
        if(phase == 1){
            phase = 2;
            planets.add(new Planet(Vars.appWidth/3+130,Vars.appHeight/3-25,
                Color.BLUE,Vars.startArmy,true,0));
            moveText("Click And Drag Form The Blue To The Other Blue Planet To Reinforce",175,300);
        }
        
        else if(phase == 5){
            moveText("Tutorial Complete, Click To Exit...",Vars.appWidth/2-85,Vars.appHeight/2);
            phase = 6;
        }
        if(phase == 1 || phase == 3 ||  phase >= 5)
            super.attack(s,dest);
    }
    
    @Override
    public void support(Squad s, Planet dest){
        super.support(s, dest);
        if(phase == 2){
            planets.add(new Planet(Vars.appWidth/3+250,Vars.appHeight/3+25,
                Color.MAGENTA,30,false,2));
            moveText("Now Take Over That Neutral Planet",235,300);
            phase = 3;
        }
    }
    
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        g.setColor(Color.WHITE);
        g.drawString(message, (int)Math.round(textX), (int)Math.round(textY));
    }
    
    public void moveText(String s, int x, int y){
        textX2 = x;
        textY2 = y;
        message = s;
    }
    
    int moveSpeed = 125;
    public void moveText(){
        double time = Vars.refreshRate/1000.0;
        double distance = Math.sqrt((double)Math.pow(textX2-textX,2)+Math.pow(textY2-textY,2));
        if(distance > 0){
            textX += Math.round((moveSpeed*time)*((double)textX2-textX)/distance);
            textY += Math.round((moveSpeed*time)*((double)textY2-textY)/distance);
        }
    }
    
    @Override
    public void checkGame(){
        int[] check = {-2,-1,1,2};
        for(int i=0; i<check.length; i++){
            if(textX+check[i] == textX2)
                textX = textX2;
            if(textY+check[i] == textY2)
                textY = textY2;
        }
        if(textX != textX2 || textY != textY2)
            moveText();
        
        if(phase == 4 && selectedPlanets.size() >= 2){
            moveText("Now Click On Or Drag To The Red Enemy",
                    Vars.appWidth-(Vars.appWidth/3)-135,Vars.appHeight-(Vars.appHeight/3)+90);
            phase = 5;
        }
        if(phase == 3){
            int count = 0;
            for(Planet p:planets){
                if(p.isPlayer())
                    count++;
            }
            if(count == 3){   
                moveText("Click Each Of Your Planets Or Double-Click To Select All",185,300);
                phase = 4;
            }
        }
        super.checkGame();
    }
    
    @Override
    public void mouseClicked(MouseEvent me) {
        if(phase > 5)
            exit = true;
        else
            super.mouseClicked(me);
    }
        
}
