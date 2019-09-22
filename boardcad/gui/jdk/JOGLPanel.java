package boardcad.gui.jdk;
/*
import board.NurbsBoard;
import cadcore.NurbsSurface;
import cadcore.NurbsPoint;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.CheckboxMenuItem;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.PopupMenu;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.*;
import com.jogamp.opengl.glu.*;
import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.image.TextureLoader;
import com.sun.j3d.utils.universe.PlatformGeometry;
import com.sun.j3d.utils.universe.SimpleUniverse;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import com.sun.j3d.utils.universe.ViewingPlatform;


//=========================================================JOGL Panel
class JOGLPanel extends Panel
{
	private static final long serialVersionUID = 1L;
	private JOGLThreeDView rendered_view;
	

	public JOGLPanel(BoardHandler board_handler)
	{

		rendered_view=new JOGLThreeDView(board_handler, this);
		setLayout(new GridLayout(1,1));

		add(rendered_view);
		doLayout();
		redraw();

		rendered_view.setSize(getPreferredSize());
		rendered_view.doLayout();


	}
    
	public void redraw()
	{
		rendered_view.repaint();
	}
	
	public JOGLThreeDView get3DView()
	{
		return rendered_view;
	}
}

//=========================================================ShapingBay

class ShapingBay
{
    
    private GLU glu;

    public float view_rotx = 0.0f;
    public float view_roty = 0.0f;
    public float view_rotz = 0.0f;
 
    protected void init( GL2 gl, int width, int height )
    {
        glu = new GLU();                         // get GL Utilities
        gl.glClearColor(0.4f, 0.4f, 0.4f, 0.0f); // set background (clear) color
        gl.glClearDepth(1.0f);      // set clear depth value to farthest
        gl.glEnable(GL.GL_DEPTH_TEST); // enables depth testing
        gl.glDepthFunc(GL.GL_LEQUAL);  // the type of depth test to do
        gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST); // best perspective correction
 //       gl.glShadeModel(GL_SMOOTH); // blends colors nicely, and smoothes out lighting
    }
    
    
    protected void setup( GL2 gl, int width, int height )
    {
        if (height == 0) height = 1;   // prevent divide by zero
        float aspect = (float)width / height;

        // Set light parameters.
        float SHINE_ALL_DIRECTIONS = 1;
        float[] lightPos = {0.0f, 0.0f, 10.0f, SHINE_ALL_DIRECTIONS};
        float[] lightColorAmbient = {0.2f, 0.2f, 0.2f, 1f};
        float[] lightColorSpecular = {0.6f, 0.6f, 0.6f, 1f};
        gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, lightPos, 0);
        gl.glLightfv(GL.GL_LIGHT0, GL.GL_AMBIENT, lightColorAmbient, 0);
//        gl.glLightfv(GL_LIGHT0, GL_SPECULAR, lightColorSpecular, 0);
        gl.glLightfv(GL_LIGHT0, GL_DIFFUSE, lightColorSpecular, 0);
        
        // Enable lighting in GL.
        gl.glEnable(GL.GL_LIGHTING);
        gl.glEnable(GL.GL_LIGHT0);

        // Set material properties.
        float[] rgba = {1.0f, 1.0f, 1.0f, 1.0f};
        gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT, rgba, 0);
//        gl.glMaterialfv(GL_FRONT, GL_SPECULAR, rgba, 0);
        gl.glMaterialfv(GL.GL_FRONT, GL.GL_DIFFUSE, rgba, 0);
//        gl.glMaterialf(GL_FRONT, GL_SHININESS, 0.5f);
        gl.glShadeModel(GL.GL_FLAT);

        
        // Set the view port (display area) to cover the entire window
        gl.glViewport(0, 0, width, height);
        
        // Setup perspective projection, with aspect ratio matches viewport
        gl.glMatrixMode(GL_PROJECTION);  // choose projection matrix
        gl.glLoadIdentity();             // reset projection matrix
        glu.gluPerspective(45.0, aspect, 0.1, 100.0); // fovy, aspect, zNear, zFar
   
        // Enable the model-view transform
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity(); // reset
        
    }
    
    protected void render( GL2 gl, int width, int height )
    {
        float red[] = { 0.8f, 0.1f, 0.0f, 1.0f };
        float green[] = { 0.0f, 0.8f, 0.2f, 1.0f };
        float blue[] = { 0.2f, 0.2f, 1.0f, 1.0f };
 
        gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear color and depth buffers
        gl.glLoadIdentity();                 // reset the model-view matrix
        gl.glTranslatef(0.0f, 0.0f, -4.0f); // translate 'into the screen
 
        
        // Rotate the board based on how the user dragged the mouse around
        gl.glPushMatrix();
        gl.glRotatef(view_rotx, 1.0f, 0.0f, 0.0f);
        gl.glRotatef(view_roty, 0.0f, 1.0f, 0.0f);
        gl.glRotatef(view_rotz, 0.0f, 0.0f, 1.0f);

        // Get the deck surface from BoardCAD
        BoardHandler boardhandler=boardcad.gui.jdk.BoardCAD.getInstance().getBoardHandler();
        NurbsBoard board=boardhandler.getActiveBoard();
        NurbsSurface deck=board.getDeck();
        NurbsPoint p1,p2,p3,n1,n2,n3;
        
        
        float length=board.get_length();
        float scale=length/2.0f; //500.0f;
        
        gl.glBegin(GL_TRIANGLES);
        if(board.triangle_array!=null)
        {
            for(int i=0;i<board.triangle_count;i++)
            {
                p1=(NurbsPoint)board.triangle_array[i].vertices[0].clone();
                n1=(NurbsPoint)board.triangle_array[i].normal.clone();
                p2=(NurbsPoint)board.triangle_array[i].vertices[1].clone();
                n2=(NurbsPoint)board.triangle_array[i].normal.clone();
                p3=(NurbsPoint)board.triangle_array[i].vertices[2].clone();
                n3=(NurbsPoint)board.triangle_array[i].normal.clone();
                gl.glNormal3f((float)n1.x, (float)n1.z, (float)n1.y);
                gl.glVertex3f((float)(p1.x-scale)/scale, (float)p1.z/scale, (float)p1.y/scale);
                gl.glNormal3f((float)n2.x, (float)n2.z, (float)n2.y);
                gl.glVertex3f((float)(p2.x-scale)/scale, (float)p2.z/scale, (float)p2.y/scale);
                gl.glNormal3f((float)n3.x, (float)n3.z, (float)n3.y);
                gl.glVertex3f((float)(p3.x-scale)/scale, (float)p3.z/scale, (float)p3.y/scale);
                
            }
            
        }
        else
        {
        for(double s=deck.getMinS();s<deck.getMaxS();s=s+0.25)
        {
            for(double t=deck.getMinT();t<deck.getMaxT();t=t+0.25)
            {
                p1=deck.getPoint(s,t);
                n1=deck.getNormal(s,t);
                p2=deck.getPoint(s,t+0.25);
                n2=deck.getNormal(s,t+0.25);
                p3=deck.getPoint(s+0.25,t);
                n3=deck.getNormal(s+0.25,t);
                
                gl.glNormal3f((float)n1.x, (float)n1.z, (float)n1.y);
                gl.glVertex3f((float)(p1.x-scale)/scale, (float)p1.z/scale, (float)p1.y/scale);
                gl.glNormal3f((float)n2.x, (float)n2.z, (float)n2.y);
                gl.glVertex3f((float)(p2.x-scale)/scale, (float)p2.z/scale, (float)p2.y/scale);
                gl.glNormal3f((float)n3.x, (float)n3.z, (float)n3.y);
                gl.glVertex3f((float)(p3.x-scale)/scale, (float)p3.z/scale, (float)p3.y/scale);
                
                p1=deck.getPoint(s,t+0.25);
                n1=deck.getNormal(s,t+0.25);
                p2=deck.getPoint(s+0.25,t+0.25);
                n2=deck.getNormal(s+0.25,t+0.025);
                p3=deck.getPoint(s+0.25,t);
                n3=deck.getNormal(s+0.25,t);
                
                gl.glNormal3f((float)n1.x, (float)n1.z, (float)n1.y);
                gl.glVertex3f((float)(p1.x-scale)/scale, (float)p1.z/scale, (float)p1.y/scale);
                gl.glNormal3f((float)n2.x, (float)n2.z, (float)n2.y);
                gl.glVertex3f((float)(p2.x-scale)/scale, (float)p2.z/scale, (float)p2.y/scale);
                gl.glNormal3f((float)n3.x, (float)n3.z, (float)n3.y);
                gl.glVertex3f((float)(p3.x-scale)/scale, (float)p3.z/scale, (float)p3.y/scale);
                
                
            }
        }
        }
        
        gl.glEnd();

    }
}


class JOGLThreeDView extends Panel implements ItemListener, MouseListener, MouseMotionListener
{

	private static final long serialVersionUID = 1L;
	private BoardHandler board_handler;
	private JOGLPanel design_panel;
	private JPopupMenu view_menu;
	private JCheckBoxMenuItem shade_item;
    
    private ShapingBay shapingbay;
    
    private int prevMouseX, prevMouseY;
    private boolean mouseRButtonDown = false;
    
    public GLJPanel gljpanel;
    
    
	public JOGLThreeDView(BoardHandler bh, JOGLPanel dp)
	{
 		setLayout(new GridLayout(1,1));
        
        GLProfile glprofile = GLProfile.getDefault();
        GLCapabilities glcapabilities = new GLCapabilities( glprofile );
        gljpanel = new GLJPanel( glcapabilities );
        add(gljpanel);
        
        shapingbay = new ShapingBay();
        
        gljpanel.addGLEventListener( new GLEventListener() {
            
            @Override
            public void reshape( GLAutoDrawable glautodrawable, int x, int y, int width, int height ) {
                shapingbay.setup( glautodrawable.getGL().getGL2(), width, height );
            }
            
            @Override
            public void init( GLAutoDrawable glautodrawable ) {
                shapingbay.init( glautodrawable.getGL().getGL2(), glautodrawable.getWidth(), glautodrawable.getHeight() );
            }
            
            @Override
            public void dispose( GLAutoDrawable glautodrawable ) {
            }
            
            @Override
            public void display( GLAutoDrawable glautodrawable ) {
                shapingbay.render( glautodrawable.getGL().getGL2(), glautodrawable.getWidth(), glautodrawable.getHeight() );
            }
        });
 
        addMouseListener(this);
        addMouseMotionListener(this);
 
        
	}
    
    // Methods required for the implementation of MouseListener
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    
    public void mousePressed(MouseEvent e)
    {
        prevMouseX = e.getX();
        prevMouseY = e.getY();
        if ((e.getModifiers() & e.BUTTON3_MASK) != 0)
        {
            mouseRButtonDown = true;
        }
    }
    
    public void mouseReleased(MouseEvent e)
    {
        if ((e.getModifiers() & e.BUTTON3_MASK) != 0)
        {
            mouseRButtonDown = false;
        }
    }
    
    public void mouseClicked(MouseEvent e)
    {
    }
    
    // Methods required for the implementation of MouseMotionListener
    public void mouseDragged(MouseEvent e)
    {
        int x = e.getX();
        int y = e.getY();
        Dimension size = e.getComponent().getSize();
        
        float thetaY = 360.0f * ( (float)(x-prevMouseX)/(float)size.width);
        float thetaX = 360.0f * ( (float)(prevMouseY-y)/(float)size.height);
        
        prevMouseX = x;
        prevMouseY = y;
        
        shapingbay.view_rotx += thetaX;
        shapingbay.view_roty += thetaY;
        
        gljpanel.display();
    }
    
    public void mouseMoved(MouseEvent e)
    {
    }
    
 
 
	public void itemStateChanged(ItemEvent e)
	{
		if(e.getSource()==shade_item)
		{
		}
	}
    
	class ThreeDMouse extends MouseAdapter
	{
		private JOGLThreeDView the_view;
		public ThreeDMouse(JOGLThreeDView v)
		{
			the_view=v;
		}
        
		public void mouseClicked(MouseEvent e)
		{
			if(e.isMetaDown())
				view_menu.show(the_view, e.getX(), e.getY());
		}
	}
    
	public void setBackgroundColor(Color color)
	{
        //		float[] components = color.getRGBComponents(null);
        //		mBackgroundNode.setColor(new Color3f(components[0], components[1], components[2]));
	}
    
}



*/
