
import java.awt.*; 
import java.applet.*; 
import java.awt.event.*;
import javax.swing.*;
import java.io.*;

public class Grapher extends Applet implements ActionListener, Runnable {
    Thread animation;
    Graphics offscreen;
    Image image;
    
    static final double pi=3.141592654;
    static final int REFRESH_RATE=10;
    static final int APP_WIDTH=1000;
    static final int APP_HEIGHT=600;
    static final int X_LIMIT_DEFAULT=3;
    static final int Y_LIMIT_DEFAULT=3;    
    
    TextField Equation, xLimit, yLimit, DETAIL;
    Button ZoomIn, ZoomOut;
    
    
    static int theta=0;
    static int phi=20;
    static int dist=9000;
    static int range=30;
    static int dim;
    static double xLim= X_LIMIT_DEFAULT;
    static double yLim= Y_LIMIT_DEFAULT;
    static double Coord3D[][]; //Represents Actual 3D coordinates of the graph points
    static double Pixel3D[][][]; //Represents Screen pixel representations of the graph points;
    static double Coord2D[][][];
    static double zScaleFactor=1;
    static double xyScaleFactor=1;
    static boolean isSpinning=true;
    static boolean Dragged=false;
    int GlobxI=0;
    int GlobyI=0;
    int GlobxF=0;
    int GlobyF=0;
    static int xSpeed, ySpeed;
    
    public void setup () {
        initializeButtons();
        //initializeArrays();
    }
    
    public void initializeArrays () {
        dim=2*range+1;
        Coord3D=new double [dim][dim];
        Pixel3D=new double [dim][dim][3];
        Coord2D=new double [dim][dim][2];
        String equation=Equation.getText();
        range=Integer.parseInt (DETAIL.getText());
        if (xLim==0)
            xLim=X_LIMIT_DEFAULT;
        if (yLim==0)
            yLim=Y_LIMIT_DEFAULT;
        double MaxMin=0;
       // System.out.println (xLim+" "+yLim);
        for (int i=-range;i<=range;i++) {
            for (int j=-range;j<=range;j++) {
                double temp=(i*xLim/range)*(j*yLim/range);
                if (String.valueOf(temp).equals("Infinity") || String.valueOf(temp).equals("-Infinity"))
                    temp=9999;
                else if (String.valueOf(temp).equals("NaN"))
                    temp=0;
                if (Math.abs(temp)>MaxMin)
                    MaxMin=temp;
                Coord3D [i+range][j+range]=temp;
            }
        }
        if (MaxMin!=0)
            zScaleFactor=250.0/MaxMin;
        zScaleFactor=300*xLim/range;
        xyScaleFactor=300.0/range;
        for (int i=-range;i<=range;i++) {
            for (int j=-range;j<=range;j++) {
                double x1=i*xyScaleFactor;
                double y1=j*xyScaleFactor;
                double z1=Coord3D[i+range][j+range]*zScaleFactor;
                double No1=((Math.cos (rad(theta))*Math.sin(rad(phi))*y1)+(Math.sin(rad(theta))*Math.sin(rad(phi))*x1)+Math.cos(rad(phi))*z1);//3d x
                double No2=(-Math.sin(rad(theta))*y1+Math.cos(rad(theta))*x1);//3d y
                double No3=((-Math.cos (rad(theta))*Math.cos(rad(phi))*y1)+(Math.sin(rad(theta))*Math.cos(rad(phi))*x1)+Math.sin(rad(phi))*z1);  
                Pixel3D [i+range][j+range][0] = No1;
                Pixel3D [i+range][j+range][1] = No2;
                Pixel3D [i+range][j+range][2] = No3;
                Coord2D [i+range][j+range][0] = (APP_WIDTH/2+(dist/(dist-No3))*No2);
                Coord2D [i+range][j+range][1] = (APP_HEIGHT/2-(dist/(dist-No3))*No1);
            }
        }
    }
    
    public double coslightTriangle1 (int i, int j) {
        double x11= Pixel3D [i][j][0];
        double y11= Pixel3D [i][j][1];
        double z11= Pixel3D [i][j][2];
        
        double x22= Pixel3D [i+1][j][0];
        double y22= Pixel3D [i+1][j][1];
        double z22= Pixel3D [i+1][j][2];
        
        double x33= Pixel3D [i+1][j+1][0];
        double y33= Pixel3D [i+1][j+1][1];
        double z33= Pixel3D [i+1][j+1][2];
   
        double l11=(y22-y11)*(z33-z11)-(z22-z11)*(y33-y11);
        double m11=(z22-z11)*(x33-x11)-(x22-x11)*(z33-z11);
        double n11=(x22-x11)*(y33-y11)-(y22-y11)*(x33-x11);
        double cosa=(n11)/Math.sqrt((m11*m11)+(n11*n11)+(l11*l11));
        return Math.pow((cosa),1);
    }
    
     public double coslightTriangle2 (int i, int j) {
        double x11= Pixel3D [i][j][0];
        double y11= Pixel3D [i][j][1];
        double z11= Pixel3D [i][j][2];
        
        double x22= Pixel3D [i][j+1][0];
        double y22= Pixel3D [i][j+1][1];
        double z22= Pixel3D [i][j+1][2];
        
        double x33= Pixel3D [i+1][j+1][0];
        double y33= Pixel3D [i+1][j+1][1];
        double z33= Pixel3D [i+1][j+1][2];
   
        double l11=(y22-y11)*(z33-z11)-(z22-z11)*(y33-y11);
        double m11=(z22-z11)*(x33-x11)-(x22-x11)*(z33-z11);
        double n11=(x22-x11)*(y33-y11)-(y22-y11)*(x33-x11);
        double cosa=(n11)/Math.sqrt((m11*m11)+(n11*n11)+(l11*l11));
        return Math.pow((cosa),1);
    }
    
    
    public double EquationSolverfalse (String s, double x, double y) {
        return x*x+y*y;
        
    }
    
    public void initializeButtons() {
        setLayout (new FlowLayout()); 
        Equation = new TextField("0",20);
        ZoomIn = new Button ("Zoom In");
        ZoomOut = new Button ("Zoom Out");
        DETAIL = new TextField(range+"", 5);
        add(Equation);
        add(ZoomIn);
        add(ZoomOut);
        add(DETAIL);
        ZoomIn.addActionListener(this);
        ZoomOut.addActionListener(this);
    }
    
    /** Real Paint method */
    public void paintGraph (Graphics g) {
        initializeArrays();
        int count=0;
        if (theta%360 < 360 && theta%360 >180) {
            for (int i=0;i<dim-1;i++) {
                for (int j=0;j<dim-1;j++) {
                    Color a;
                    if (count%2==0)
                        a=new Color (64, 255, 0);
                    else
                        a=new Color (32, 128,0);
                    drawGraph (g,i,j,a);
                    count++;
                }
                count++;
            }
        }
        else if (theta%360 < 180 && theta%360 >0) {
             for (int i=dim-2;i>=0;i--) {
                for (int j=dim-2;j>=0;j--) {
                    Color a;
                    if (count%2==0)
                        a=new Color (64, 255, 0);
                    else
                        a=new Color (32, 128,0);
                    drawGraph (g,i,j,a);
                    count++;
                }
                count++;
            }
        }
        g.setColor (Color.black);
    }
    
    public void drawGraph (Graphics g, int i, int j,Color cc) {
        int x1[]={(int)Coord2D[i][j][0], (int)Coord2D[i+1][j][0], (int)Coord2D[i+1][j+1][0],(int)Coord2D[i][j][0]};
        int y1[]={(int)Coord2D[i][j][1], (int)Coord2D[i+1][j][1], (int)Coord2D[i+1][j+1][1],(int)Coord2D[i][j][1]};
        double col=Math.abs(coslightTriangle1(i,j));  
        Color cc1=new Color ((int)(cc.getRed()*col), (int)(cc.getGreen()*col),(int)(cc.getBlue()*col));
        g.setColor(cc1);
        g.fillPolygon (x1,y1, 4);
        x1[1]=(int)Coord2D[i][j+1][0];
        y1[1]=(int)Coord2D[i][j+1][1];
        col=Math.abs(coslightTriangle2(i,j));
        cc1=new Color ((int)(cc.getRed()*col), (int)(cc.getGreen()*col),(int)(cc.getBlue()*col));
        g.setColor (cc1);
        g.fillPolygon (x1,y1, 4);
        
    }
    
    
    /**Applet Initializer Method */
    public void init () {
        setBackground (Color.black);
        image= createImage (APP_WIDTH, APP_HEIGHT);
        offscreen=image.getGraphics ();
        setup();
    }
    
    public void actionPerformed(ActionEvent evt) { 
        if (evt.getSource() == ZoomIn) {
            if (xLim>1 && yLim >1) {
                xLim-=1; yLim-=1;
            }
        } 
        if (evt.getSource() == ZoomOut) {
            xLim+=1;
            yLim+=1;
        } 
        repaint();
    }
     
   
    
    public boolean mouseDrag (Event e, int x, int y) {
        GlobxI=GlobxF;
        GlobyI=GlobyF;
        GlobxF=x;
        GlobyF=y;
        int incrementx=(GlobxF-GlobxI)/3;
        int incrementy=(GlobyF-GlobyI)/3;
        theta+=incrementx;
        phi+=incrementy;
        if (incrementx>=1 && incrementy>=1)
            repaint();
        return true;
    }
    
    public boolean mouseUp (Event e, int x, int y) {
        if (GlobxI==0 && GlobyI==0)
            return false;
        xSpeed=(GlobxF-GlobxI)/3;
        ySpeed=(GlobyF-GlobyI)/3;
        xSpeed=(xSpeed>10)?10:xSpeed;
        ySpeed=(ySpeed>10)?10:ySpeed;
        
        return true;
    }
    
    public boolean mouseDown (Event e, int x, int y) {
        isSpinning=false; Dragged=true;
        GlobxI=0;GlobyI=0;GlobxF=0;GlobyF=0;
        xSpeed=0;ySpeed=0;
        return true;
    }
    
    
    
    
    public boolean keyDown(Event e, int key) {   
        if (key == Event.UP) {
            phi-=5;
            repaint();
        }
        if (key == Event.DOWN) {
            phi+=5;
            repaint();
        }
        if (key == Event.RIGHT) {
            theta+=5;
            repaint();
        }
        if (key == Event.LEFT) {
            theta-=5;
            repaint();
        }
        
        isSpinning=false;
        return true;
    }
    
    /** Applet Start Method */
    public void start () {
        animation = new Thread (this);
        if (animation!=null) {
            animation.start ();
        }
    }
    
    /** Double-Buffered Overridden Update Method */
    public void update (Graphics g) {
        paint (g);
    }
    
    /** Double-Buddered Paint Method */
    public void paint (Graphics g) {
        offscreen.setColor (Color.black);
        offscreen.fillRect (0,0,APP_WIDTH,APP_HEIGHT);
        paintGraph (offscreen);
        offscreen.setColor (Color.red);
        offscreen.drawString ("Theta:",10,100);
        offscreen.drawString (theta%360+"",60,100);
        offscreen.drawString ("Phi:",10,120);
        offscreen.drawString (phi%360+"",60,120);
        g.drawImage (image,0,0,this);
    }
    
    /**Main Run Method */
    public void run () {
        while (true) {
            repaint ();
            if (isSpinning) 
                theta+=1;
            if (Dragged) {
                theta+=xSpeed;
                phi+=ySpeed;
            }
            phi=phi%360;
            theta=theta%360;
            if (theta<0)
                theta+=360;
            if (phi<0)
                phi+=360;
                
            try {
                Thread.sleep (REFRESH_RATE);
            } catch (Exception e) {};
        }
    }
    
    /** Apple Stop Method */
    public void stop () {
        if (animation!=null) {
            animation.stop();
            animation=null;
        }
    }
    
    double EquationSolver (String a, double x, double y) {
        int l=a.length();
        int i;
        int start=-1;
        for (i=0;i<l;i++) {
            if (a.charAt (i)== '(')
                start=i;
            if (a.charAt (i)== ')' && start >=0)
                return EquationSolver(a.substring (0,start)+Double.toString(EquationSolver(a.substring (start+1,i),x,y))+a.substring (i+1,l),x,y);  
        }
        for (i=0;i<l;i++){
            if (a.charAt (i)== '+')
                return (EquationSolver (a.substring (0,i),x,y))+(EquationSolver(a.substring (i+1,l),x,y));
        }
        if (isNumber (a)==true)
            return Double.parseDouble (a);
        if (a.charAt (0)=='-')
            return -EquationSolver(Double.toString(EquationSolver(a.substring (1),x,y)),x,y); 
        for (i=1;i<l;i++) {
            if (a.charAt (i)== '-' && a.charAt (i-1)!='+' && a.charAt (i-1)!='*' && a.charAt (i-1)!='/' && a.charAt (i-1)!='^')
                return EquationSolver(a.substring (0,i)+"+"+a.substring (i,l),x,y);
        }
        for (i=0;i<l;i++) {
            if (a.charAt (i)== '*')
                return (EquationSolver (a.substring (0,i),x,y))*(EquationSolver(a.substring (i+1,l),x,y));
        }
        for (i=0;i<l;i++) {
            if (a.charAt (i)== '/')
                return (EquationSolver (a.substring (0,i),x,y))/(EquationSolver(a.substring (i+1,l),x,y));
        }        
        for (i=0;i<l;i++) {
            if (a.charAt (i)== '^')
                return Math.pow ((EquationSolver (a.substring (0,i),x,y)),(EquationSolver(a.substring (i+1,l),x,y)));
        }
        if (a.equals("x"))
            return x;
        if (a.equals("y"))
            return y;
        if (isNumber (a)==true)
            return Double.parseDouble (a);
        System.out.println ("Error");
        System.exit(0);
        return 0;
    }
    
     public boolean isNumber (String a) {  
        try {  
            Double.parseDouble(a);  
            return true;  
        } catch (Exception e) {  
            return false;  
        }
    }
     
    public double rad (int a) {
        return a*(pi/180);
    }
}
    