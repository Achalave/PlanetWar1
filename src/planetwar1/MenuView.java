
package planetwar1;
import gameplay.Comet;
import gameplay.Explosion;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
public class MenuView extends JPanel implements ActionListener,ChangeListener ,Runnable{
    
    boolean cometing = true;
    boolean main = true;
    boolean options = false;
    boolean play = false;
    boolean custom = false;
    int maxComets = 10;
    int numPlanets = (int)(Math.random()*10)+4;
    
    //For options
    JPanel enemyColor;
    JPanel playerColor;
    JPanel neutralColor;
    JButton cometToggle;
    
    //For custom game
    int dificulty = 0;
    JPanel AISelect;
    JSlider[] sliders = new JSlider[4];
    JLabel[] labels = new JLabel[4];
    
    FlowLayout menuLayout = new FlowLayout(FlowLayout.CENTER,0,0);
    FlowLayout gameplayLayout = new FlowLayout();
    public static ArrayList<Comet> comets = new ArrayList<Comet>();
    public static ArrayList<Explosion> explosions = new ArrayList<Explosion>();
    Thread t;
    GameplayView game;
    
    public MenuView(){
        startupMenu();
        t = new Thread(this);
        t.start();
    }
    
    public void startupMenu(){
        removeAll();
        requestFocus();
        cometing = true;
        menuLayout.setVgap((Vars.appHeight/2)-40);
        setLayout(menuLayout);
        setBackground(Color.BLACK);
        setupMain();
    }
    
    public void shutdownMenu(){
        removeAll();
        comets.clear();
        cometing = false;
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
    
    private void setupMain(){
        main = true;
        options = false;
        play = false;
        this.removeAll();
        setLayout(menuLayout);
        JButton button = newButton("Play");
        add(button);
        button = newButton("Options");
        add(button);
        button = newButton("How To Play");
        add(button);
        validate();
        repaint();
    }
    
    public void setupPlay(){
        main = false;
        play = true;
        options = false;
        this.removeAll();
        JButton button = newButton("Easy");
        add(button);
        button = newButton("Medium");
        add(button);
        button = newButton("Hard");
        add(button);
        button = newButton("Custom");
        add(button);
        button = newButton("Back");
        add(button);
        validate();
        repaint();
    }
    
    public void setupOptions(){
        main = false;
        play = false;
        options = true;
        this.removeAll();
        
        JPanel over = new JPanel();
        BoxLayout b = new BoxLayout(over,BoxLayout.Y_AXIS);
        over.setLayout(b);
        over.setBackground(Color.BLACK);
        //Add back button
        over.add(newButton("Back"));
        JPanel colors = new JPanel();
        colors.setBackground(Color.BLACK);
        //Add the player's color chooser
        playerColor = new JPanel();
        b = new BoxLayout(playerColor,BoxLayout.Y_AXIS);
        playerColor.setLayout(b);
        JLabel jl = new JLabel("Player");
        jl.setAlignmentX(CENTER_ALIGNMENT);
        playerColor.add(jl);
        playerColor.add(newButton("Red"));
        playerColor.add(newButton("Green"));
        playerColor.add(newButton("Blue"));
        playerColor.add(newButton("Orange"));
        colors.add(playerColor);
        
        //Add the nuetral's color chooser
        neutralColor = new JPanel();
        b = new BoxLayout(neutralColor,BoxLayout.Y_AXIS);
        neutralColor.setLayout(b);
        jl = new JLabel("Neutral");
        jl.setAlignmentX(CENTER_ALIGNMENT);
        neutralColor.add(jl);
        neutralColor.add(newButton("Magenta"));
        neutralColor.add(newButton("Gray"));
        neutralColor.add(newButton("Yellow"));
        neutralColor.add(newButton("Orange"));
        colors.add(neutralColor);
        //Add the computer's color chooser
        enemyColor = new JPanel();
        b = new BoxLayout(enemyColor,BoxLayout.Y_AXIS);
        enemyColor.setLayout(b);
        jl = new JLabel("Computer");
        jl.setAlignmentX(CENTER_ALIGNMENT);
        enemyColor.add(jl);
        enemyColor.add(newButton("Red"));
        enemyColor.add(newButton("Green"));
        enemyColor.add(newButton("Blue"));
        enemyColor.add(newButton("Orange"));
        colors.add(enemyColor);
        over.add(colors);
        
        //Add commet toggle
        if(!Vars.comets)
            cometToggle = newButton("Enable Comets");
        else
            cometToggle = newButton("Disable Comets");
        over.add(cometToggle);
        add(over);
        refreshOptions();
        validate();
        repaint();
    }
    
    public void refreshOptions(){
        if(enemyColor != null)
            enemyColor.setBackground(Vars.enemyColor);
        if(playerColor != null)
            playerColor.setBackground(Vars.playerColor);
        if(neutralColor != null)
            neutralColor.setBackground(Vars.neutralColor);
    }
    
    public void setupGameView(){
        shutdownMenu();
        setLayout(gameplayLayout);
        removeAll();
        game = new GameplayView();
        add(game);
        setVisible(true);
        validate();
        repaint();
    }
    
    public void setupCustomGame(){
        main = false;
        options = false;
        play = false;
        custom = true;
        this.removeAll();
        
        JPanel over = new JPanel();
        BoxLayout b = new BoxLayout(over,BoxLayout.Y_AXIS);
        over.setLayout(b);
        over.setBackground(Color.BLACK);
        
        //Add back button
        over.add(newButton("Back"));
        
        //Add number of planets slider
        JPanel p = new JPanel();
        b = new BoxLayout(p,BoxLayout.Y_AXIS);
        p.setLayout(b);
        p.setBackground(Color.BLACK);
        JLabel l = new JLabel("Number of Planets: "+numPlanets);
        l.setForeground(Color.WHITE);
        p.add(l);
        JSlider planets = new JSlider(4,20);
        sliders[0] = planets;
        labels[0] = l;
        planets.addChangeListener(this);
        planets.setValue((int)Math.round(numPlanets));
        p.add(planets);
        over.add(p);
        
        //Add planet regen rate slider
        p = new JPanel();
        b = new BoxLayout(p,BoxLayout.Y_AXIS);
        p.setLayout(b);
        p.setBackground(Color.BLACK);
        l = new JLabel("Planet Regeneration Rate (Percent Per Second): "+Vars.armyIncreaseRate);
        l.setForeground(Color.WHITE);
        p.add(l);
        JSlider regen = new JSlider(1,6);
        sliders[1] = regen;
        labels[1] = l;
        regen.addChangeListener(this);
        regen.setValue((int)Math.round(Vars.armyIncreaseRate));
        p.add(regen);
        over.add(p);
        
        //Add the speed choosers
        JPanel speeds = new JPanel();
        speeds.setBackground(Color.BLACK);
        
        p = new JPanel();
        b = new BoxLayout(p,BoxLayout.Y_AXIS);
        p.setLayout(b);
        p.setBackground(Color.BLACK);
        l = new JLabel("Player Squad Speed (Pixels Per Second): "+Vars.speedPlayer);
        l.setForeground(Color.WHITE);
        p.add(l);
        JSlider playerSpeed = new JSlider(50,250);
        sliders[2] = playerSpeed;
        labels[2] = l;
        playerSpeed.addChangeListener(this);
        playerSpeed.setValue(Vars.speedPlayer);
        p.add(playerSpeed);
        speeds.add(p);
        
        p = new JPanel();
        b = new BoxLayout(p,BoxLayout.Y_AXIS);
        p.setLayout(b);
        p.setBackground(Color.BLACK);
        l = new JLabel("Computer Squad Speed (Pixels Per Second): "+Vars.speedEnemy);
        l.setForeground(Color.WHITE);
        p.add(l);
        JSlider enemySpeed = new JSlider(50,250);
        sliders[3] = enemySpeed;
        labels[3] = l;
        enemySpeed.addChangeListener(this);
        enemySpeed.setValue(Vars.speedEnemy);
        p.add(enemySpeed);
        speeds.add(p);
        over.add(speeds);
        
        //Add AI Selection
        AISelect = new JPanel();
        AISelect.setBackground(Color.BLACK);
        AISelect.add(newButton("Easy"));
        AISelect.add(newButton("Medium"));
        AISelect.add(newButton("Hard"));
        over.add(AISelect);
        
        over.add(newButton("Create Game"));
        
        over.setSize(Vars.appHeight, Vars.appWidth);
        add(over);
        validate();
        repaint();
    }
    
    public void setupHowToPlay(){
        shutdownMenu();
        setLayout(gameplayLayout);
        removeAll();
        game = new HowToPlayView();
        add(game);
        setVisible(true);
        validate();
        repaint();
    }
    //Component Methods
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(Vars.appWidth,Vars.appHeight);
    }
    
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        try{
            for(Comet c:comets)
                c.draw(g);
            for(Explosion e:explosions)
                    e.draw(g);
        }catch(Exception e){}
        g.setColor(Color.RED);
        Font font = new Font("Arial", Font.PLAIN, 50);
        g.setFont(font);
        g.drawString("Planet War I", Vars.appWidth/2-135, 250);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if(main){
            if(ae.getActionCommand().equals("Play"))
                setupPlay();
            else if(ae.getActionCommand().equals("Options"))
                setupOptions();
            else if(ae.getActionCommand().equals("How To Play")){
                setupHowToPlay();
                game.start(0);
            }
        }
        else if(play){
            
            if(ae.getActionCommand().equals("Easy")){
                setupGameView();
                game.start(0);
            }
            else if(ae.getActionCommand().equals("Medium")){
                setupGameView();
                game.start(1);
            } 
            else if(ae.getActionCommand().equals("Hard")){
                setupGameView();
                game.start(2);
            }
            else if(ae.getActionCommand().equals("Custom")){
                setupCustomGame();
            }
            else if(ae.getActionCommand().equals("Back")){
                setupMain();
            } 
        }
        else if(options){
            if(ae.getSource() == cometToggle){
                Vars.comets = !Vars.comets;
                if(!Vars.comets)
                    cometToggle.setText("Enable Comets");
                else
                    cometToggle.setText("Disable Comets");
            }
            else if(contains(playerColor.getComponents(),(JButton)ae.getSource())){
                if(ae.getActionCommand().equals("Red"))
                    Vars.playerColor = Color.RED;
                if(ae.getActionCommand().equals("Green"))
                    Vars.playerColor = Color.GREEN;
                if(ae.getActionCommand().equals("Blue"))
                    Vars.playerColor = Color.BLUE;
                if(ae.getActionCommand().equals("Orange"))
                    Vars.playerColor = Color.ORANGE;
            }
            else if(contains(enemyColor.getComponents(),(JButton)ae.getSource())){
                if(ae.getActionCommand().equals("Red"))
                    Vars.enemyColor = Color.RED;
                if(ae.getActionCommand().equals("Green"))
                    Vars.enemyColor = Color.GREEN;
                if(ae.getActionCommand().equals("Blue"))
                    Vars.enemyColor = Color.BLUE;
                if(ae.getActionCommand().equals("Orange"))
                    Vars.enemyColor = Color.ORANGE;
            }
            else if(contains(neutralColor.getComponents(),(JButton)ae.getSource())){
                if(ae.getActionCommand().equals("Magenta"))
                    Vars.neutralColor = Color.MAGENTA;
                if(ae.getActionCommand().equals("Gray"))
                    Vars.neutralColor = Color.GRAY;
                if(ae.getActionCommand().equals("Yellow"))
                    Vars.neutralColor = Color.YELLOW;
                if(ae.getActionCommand().equals("Orange"))
                    Vars.neutralColor = Color.ORANGE;
            }
            else if(ae.getActionCommand().equals("Back")){
                setupMain();
            }
        }
        else if(custom){
            if(ae.getActionCommand().equals("Back")){
                setupPlay();
            }
            else if(ae.getActionCommand().equals("Easy")){
                AISelect.setBackground(Color.GREEN);
                dificulty = 0;
            }
            else if(ae.getActionCommand().equals("Medium")){
                AISelect.setBackground(Color.YELLOW);
                dificulty = 1;
            }
            else if(ae.getActionCommand().equals("Hard")){
                AISelect.setBackground(Color.RED);
                dificulty = 2;
            }
            else if(ae.getActionCommand().equals("Create Game")){
                setupGameView();
                game.start(dificulty,numPlanets);
            }
        }
        
    }
    
    private boolean contains(Component[] c, JButton b){
        for(Component i:c)
            if(i == b)
                return true;
        return false;
    }
    
    @Override
    public void run() {
        
        int wait = 1000;
        try{
            while(true){
                Thread.sleep(wait);
                wait = 25;
                Vars.appHeight = getHeight();
                Vars.appWidth = getWidth();
                if(options)
                    refreshOptions();
                if(comets.size() < maxComets && cometing){
                    if((int)(Math.random()*50) == 0){
                        if(Math.random() <= .5)
                            comets.add(new Comet(-100,(int)(Math.random()*Vars.appHeight)));
                        else
                            comets.add(new Comet((int)(Math.random()*Vars.appHeight),-100));
                    }     
                }
                if(!cometing && game != null && game.exit){
                    startupMenu();
                }
                cleanComets();
                cleanExplosions();
                repaint();
            }
        }catch(InterruptedException e){}
        catch(NullPointerException e){
            e.printStackTrace();
        }
    }
    
    public void cleanComets(){
        for(int i=0; i<comets.size();i++){
            if(comets.get(i).hasArrived()){
                comets.remove(i);
                i--;
            }
        }
    }

    private void cleanExplosions(){
        for(int i=0; i<explosions.size(); i++)
            if(explosions.get(i) != null && !explosions.get(i).isExploding()){
                explosions.remove(i);
                i--;
            }      
    }
    
    @Override
    public void stateChanged(ChangeEvent ce) {
        int i = 0;
        for(; i<sliders.length; i++){
            if(sliders[i] == ce.getSource())
                break;
        }
        if(i == 0){
            numPlanets = sliders[i].getValue();
            labels[i].setText("Number of Planets: "+numPlanets);
        }
        else if(i == 1){
            Vars.armyIncreaseRate = sliders[i].getValue();
            labels[i].setText("Planet Regeneration Rate (Percent Per Second): "+Vars.armyIncreaseRate);
        }
        else if(i == 2){
            System.out.println(Vars.speedPlayer);
            Vars.speedPlayer = sliders[i].getValue();
            labels[i].setText("Player Squad Speed (Pixels Per Second): "+Vars.speedPlayer);
        }
        else if(i == 3){
            Vars.speedEnemy = sliders[i].getValue();
            labels[i].setText("Computer Squad Speed (Pixels Per Second): "+Vars.speedEnemy);
        }
    }
    
    
    
}
