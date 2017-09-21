/*
 * Author: Mitchell Proulx
 * Purpose: A bunch of simple tranformations for practice.
 */

import java.awt.Frame;
import java.awt.event.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt .*;

public class Transformations implements GLEventListener {
  public static final boolean TRACE = true;

  public static final String WINDOW_TITLE = "OpenGL Transformations";
  public static final int INITIAL_WIDTH = 640;
  public static final int INITIAL_HEIGHT = 640;

  public static void main(String[] args) {
    final Frame frame = new Frame(WINDOW_TITLE);

    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });

    final GLProfile profile = GLProfile.get(GLProfile.GL2);
    final GLCapabilities capabilities = new GLCapabilities(profile);
    final GLCanvas canvas = new GLCanvas(capabilities);
    try {
      canvas.addGLEventListener((GLEventListener)(self().getConstructor().newInstance()));
    } catch (Exception e) {
      assert(false);
    }
    canvas.setSize(INITIAL_WIDTH, INITIAL_HEIGHT);

    frame.add(canvas);
    frame.pack();
    frame.setVisible(true);

    System.out.println("\nEnd of processing.");
  }

  private static Class<?> self() {
    // This gives us the containing class of a static method 
    return new Object() { }.getClass().getEnclosingClass();
  }

  @Override
  public void init(GLAutoDrawable drawable) {
    // Called when the canvas is (re-)created - use it for initial GL setup
    if (TRACE)
      System.out.println("-> executing init()");

    final GL2 gl = drawable.getGL().getGL2();
    gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
  }

  @Override
  public void display(GLAutoDrawable drawable) {
    // Draws the display
    if (TRACE)
      System.out.println("-> executing display()");

    final GL2 gl = drawable.getGL().getGL2();
    gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

    gl.glLineWidth(3.0f);
    
    gl.glLoadIdentity();
    gl.glColor3f(0.5f, 0.5f, 0.5f);
    drawShape(gl);

    // top: just translate
    gl.glLoadIdentity();
    gl.glTranslatef(0.0f, 0.7f, 0);
    gl.glColor3f(1.0f, 0.0f, 0.0f);
    drawShape(gl);

    // left: rotate by 30 degrees
    gl.glLoadIdentity();
    gl.glTranslatef(-0.7f, 0.0f, 0);
    gl.glRotatef(30, 0, 0, 1);
    gl.glColor3f(0.0f, 1.0f, 0.0f);
    drawShape(gl);

    // right: scale by 30% in x and 175% in y
    gl.glLoadIdentity();
    gl.glTranslatef(0.7f, 0.0f, 0);
    gl.glScalef(0.3f, 1.75f, 0);
    gl.glColor3f(0.0f, 0.0f, 1.0f);
    drawShape(gl);

    // bottom: mirror horizontally and rotate by -20 degrees

     gl.glLoadIdentity();
     gl.glTranslatef(0.0f, -0.7f, 0);
    // or:
    //gl.glLoadMatrixf(new float[] {1,0,0,0,0,1,0,0,0,0,1,0, /*last column:*/ 0,-0.7f,0,1}, 0);

    gl.glRotatef(-20, 0, 0, 1);
    gl.glScalef(-1.0f, 1.0f, 0);
    gl.glColor3f(1.0f, 0.0f, 1.0f);
    drawShape(gl);
}
  
  public void drawShape(GL2 gl) {
    gl.glBegin(GL2.GL_POLYGON);
    gl.glVertex2f(-0.12f, -0.2f);
    gl.glVertex2f(0.12f, -0.2f);
    gl.glVertex2f(0.2f, 0.02f);
    gl.glVertex2f(0.2f, 0.2f);
    gl.glVertex2f(0.0f, 0.2f);
    gl.glVertex2f(-0.2f, 0.02f);
    gl.glEnd();
    
    gl.glBegin(GL2.GL_LINES);
    gl.glColor3f(1, 1, 1);
    gl.glVertex2f(0.1f, 0.0f);
    gl.glVertex2f(0.0f, 0.0f);
    gl.glColor3f(1, 1, 0);
    gl.glVertex2f(0.0f, 0.0f);
    gl.glVertex2f(0.0f, 0.1f);
    gl.glEnd();
  }

  @Override
  public void dispose(GLAutoDrawable drawable) {
    // Called when the canvas is destroyed (reverse anything from init) 
    if (TRACE)
      System.out.println("-> executing dispose()");
  }

  @Override
  public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
    // Called when the canvas has been resized
    if (TRACE)
      System.out.println("-> executing reshape(" + x + ", " + y + ", " + width + ", " + height + ")");

    final GL2 gl = drawable.getGL().getGL2();

    gl.glViewport(x, y, width, height);
    
    gl.glMatrixMode(GL2.GL_PROJECTION);
    gl.glLoadIdentity();
    gl.glOrthof(-1.0f, 1.0f, -1.0f, 1.0f, 0.0f, 1.0f);
	// gl.glOrthof(ar < 1 ? -1.0f : -ar, ar < 1 ? 1.0f : ar, ar > 1 ? -1.0f : -1/ar, ar > 1 ? 1.0f : 1/ar, 0.0f, 1.0f);
    gl.glMatrixMode(GL2.GL_MODELVIEW);
    gl.glLoadIdentity();
  }
}
