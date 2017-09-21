/*
 * Author: Mitchell Proulx
 * Purpose: Display the house in abstract positions using
 * custom and premade vertex transformations.
 * 
 */
import java.awt.Frame;
import java.awt.event.*;
import java.util.ArrayList;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.*;

public class HouseTransforms implements GLEventListener {
	public static final boolean TRACE = true;
	public static final String WINDOW_TITLE = "Mitchell Proulx"; // TODO: change
	public static final int INITIAL_WIDTH = 640;
	public static final int INITIAL_HEIGHT = 640;
	public static final int NUM_SECTORS = 8;
	public static final float SECTOR_DIV = 2.75f;
	public static final float[][] IDENTITY = { {1, 0, 0}, {0, 1, 0}, {0, 0, 1} };

	public static ArrayList<Poly> leftSideObjs = new ArrayList<Poly>();
	public static ArrayList<Center> leftSideCenters = new ArrayList<Center>();
	public static ArrayList<Poly> rightSideObjs = new ArrayList<Poly>();
	public static ArrayList<Center> rightSideCenters = new ArrayList<Center>();
	public static ArrayList<Poly> allObjs = new ArrayList<Poly>();
	public static ArrayList<Color> allColors = new ArrayList<Color>();

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
			Object self = self().getConstructor().newInstance();
			self.getClass().getMethod("setup", new Class[] { GLCanvas.class }).invoke(self, canvas);
			canvas.addGLEventListener((GLEventListener)self);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		canvas.setSize(INITIAL_WIDTH, INITIAL_HEIGHT);
		canvas.setAutoSwapBufferMode(true);

		frame.add(canvas);
		frame.pack();
		frame.setVisible(true);

		System.out.println("\nEnd of processing.");
	}

	private static Class<?> self() {
		// This ugly hack gives us the containing class of a static method 
		return new Object() { }.getClass().getEnclosingClass();
	}
	class Matrix {
		float[][] grid;
		Matrix() {
			grid = new float[3][3];
		}	
	}
	class Vertex {
		float x, y;
		Vertex(float x, float y){
			this.x = x;
			this.y = y;
		}
	}
	class Triangle {
		Vertex v1, v2, v3;
		Color c;
		Triangle(Vertex v1, Vertex v2, Vertex v3, Color c) {
			this.v1 = v1;
			this.v2 = v2;
			this.v3 = v3;
			this.c = c;
		}
	}
	class Center {
		Vertex [] objs;
		Center() {
			objs = new Vertex[5];
		}
	}
	class Poly {
		ObjectName name;
		ArrayList<Triangle> triangles;
		Poly(){
			this.triangles = new ArrayList<Triangle>();
		}
		Poly(int n){
			if ( n == 0 ) {
				this.name = name.TRUNK;
			} else if ( n == 1) {
				this.name = name.LEAVES;
			} else if ( n == 2) {
				this.name = name.FLOWER;
			} else if ( n == 3) {
				this.name = name.ROOF;	
			}else{
				this.name = name.HOUSE;	
			}
			this.triangles = new ArrayList<Triangle>();
		}
	}

	class Color {
		float r, g, b;
		Color(float r, float g, float b) {
			this.r = r;
			this.g = g;
			this.b = b;
		}
	}
	private int width, height; // the real viewport width and height
	private float[][] colours = new float[][] {
			{ 0.0f, 0.0f, 1.0f },
			{ 0.0f, 1.0f, 0.0f },
			{ 1.0f, 0.0f, 0.0f },
			{ 0.0f, 1.0f, 1.0f },
			{ 1.0f, 0.0f, 1.0f },
			{ 1.0f, 1.0f, 0.0f },
			{ 0.0f, 0.5f, 0.5f },
			{ 0.5f, 0.0f, 0.5f },
			{ 0.5f, 0.5f, 0.0f },
			{ 0.5f, 0.5f, 0.5f }
	};
	private float[][] vertices = new float[][] {
			{ -0.58074534f, -0.19254655f },
			{ -0.18012422f, 0.27329195f },
			{ -0.12732917f, 0.11490691f },
			{ 0.31987584f, 0.4223603f },
			{ 0.16770184f, 0.018633604f },
			{ 0.3167702f, -0.009316742f },
			{ 0.67701864f, 0.11801243f },
			{ 0.48447204f, 0.012422323f },
			{ 0.6801243f, 0.6335404f },
			{ 0.7111802f, 0.49068332f },
			{ -0.03726709f, 0.50310564f },
			{ 0.027950287f, 0.6552795f },
			{ 0.24844718f, 0.8447205f },
			{ 0.42857146f, 0.8354038f },
			{ 0.7391305f, 0.40062118f },
			{ 0.69875777f, 0.25155282f },
			{ -0.012422323f, 0.108695626f },
			{ 0.12111807f, 0.062111855f },
			{ 0.11801243f, -0.8043478f },
			{ 0.29192543f, -0.8291925f },
			{ 0.33850932f, -0.31055897f },
			{ 0.70807457f, -0.03726709f },
			{ 0.7298137f, -0.102484465f },
			{ 0.38198757f, 0.3726709f },
			{ -0.13975155f, -0.8664596f },
			{ 0.49068332f, -0.89751554f },
			{ 0.3757764f, -0.63975155f },
			{ 0.33229816f, -0.863354f },
			{ 0.43478262f, -0.64906836f },
			{ 0.34161496f, -0.67701864f },
			{ 0.41925466f, -0.6242236f },
			{ 0.35093164f, -0.5279503f },
			{ 0.51552796f, -0.5496894f },
			{ 0.47515535f, -0.57142854f },
			{ 0.48757768f, -0.6552795f },
			{ 0.45341623f, -0.7111801f },
			{ 0.40993786f, -0.7298137f },
			{ 0.34472048f, -0.69875777f },
			{ 0.38198757f, -0.72670805f },
			{ 0.41614914f, -0.51863354f },
			{ 0.4627329f, -0.5279503f },
			{ -0.66770184f, -0.4751553f },
			{ -0.6708075f, -0.6180124f },
			{ -0.5745342f, -0.48447204f },
			{ -0.5652174f, -0.6086956f },
			{ -0.7515528f, -0.42236024f },
			{ -0.76397514f, -0.8757764f },
			{ -0.6708075f, -0.4751553f },
			{ -0.5310559f, -0.41925466f },
			{ -0.66770184f, -0.47204965f },
			{ -0.5248447f, -0.86024845f },
			{ -0.6614907f, -0.47826087f },
			{ -0.6708075f, -0.878882f },
			{ -0.66770184f, -0.47826087f },
			{ -0.95341617f, -0.8913044f },
			{ -0.9285714f, -0.11801243f },
			{ -0.742236f, -0.07142854f },
			{ -0.39440995f, -0.015527904f },
			{ -0.31987578f, -0.85714287f },
			{ -0.71428573f, 0.26086962f },
			{ -0.6552795f, -0.055900574f },
			{ -0.29813665f, -0.12422359f },
			{ -0.19875777f, -0.052794993f },
			{ -0.7049689f, 0.42546582f },
			{ -0.9751553f, -0.19875777f },
			{ -1f, -0.12111801f }
	};

	private enum ObjectName {
		TRUNK(0), LEAVES(1), FLOWER(2), ROOF(3), HOUSE(4);
		private int i;
		private ObjectName(int i) { this.i = i; }
		public int i() { return i; }
	};

	private float[][] centres = new float[][] {
			// trunk
			{ 0.23602486f, -0.31366456f },
			// leaves
			{ 0.31987584f, 0.4223603f },
			// flower
			{ 0.40683234f, -0.63043475f },
			// roof
			{ -0.6863354f, 0.07453418f },
			// house
			{ -0.6180124f, -0.5372671f }
	};

	private int[][][] objects = new int[][][] {
			// trunk
			{
				{ 18, 19, 3, 8 },
				{ 20, 21, 22, 8 },
				{ 23, 3, 19, 8 },
				{ 24, 18, 19, 8 },
				{ 25, 19, 24, 8 }
			},

			// leaves
			{
				{ 1, 2, 3, 6 },
				{ 4, 5, 3, 6 },
				{ 3, 6, 7, 6 },
				{ 8, 9, 3, 6 },
				{ 10, 3, 11, 6 },
				{ 12, 13, 3, 1 },
				{ 3, 14, 15, 1 },
				{ 16, 17, 3, 1 }
			},

			// flower
			{
				{ 26, 27, 28, 9 },
				{ 29, 30, 31, 2 },
				{ 33, 34, 30, 2 },
				{ 35, 36, 30, 2 },
				{ 37, 38, 30, 2 },
				{ 39, 40, 30, 2 }
			},

			// roof
			{
				{ 59, 55, 60, 0, },
				{ 59, 57, 60, 0, },
				{ 61, 62, 63, 9, },
				{ 64, 65, 63, 9, },
				{ 61, 59, 63, 9, },
				{ 64, 59, 63, 9, }
			},

			// house
			{
				{ 41, 42, 43, 4, },
				{ 44, 43, 42, 4, },
				{ 45, 46, 41, 7, },
				{ 48, 45, 41, 7, },
				{ 48, 50, 43, 7, },
				{ 43, 41, 48, 7, },
				{ 52, 46, 53, 7, },
				{ 44, 52, 42, 7, },
				{ 52, 50, 44, 7, },
				{ 44, 43, 50, 7, },
				{ 54, 55, 46, 3, },
				{ 46, 56, 55, 3, },
				{ 56, 48, 45, 3, },
				{ 56, 57, 48, 3, },
				{ 57, 58, 48, 3, },
				{ 58, 50, 48, 3, }
			}
	};

	public void setup(final GLCanvas canvas) {
		// Called for one-time setup
		if (TRACE)
			System.out.println("-> executing setup()");
	}
	
	void initObjects() {
		// put all the colors in an array list for easy manipulation
		for (int n = 0; n < colours.length; n++) {
			allColors.add(new Color(colours[n][0], colours[n][1], colours[n][2]));
		}
		// put all the objects into 
		for (int i = 0; i < objects.length; i++) {					// each object polygon
			Poly newObj = new Poly(i);
			for (int j = 0; j < objects[i].length; j++) {			// each triangle with color

				// index of each vertex and color in the vertices and color array
				int k = 0;
				Vertex v1 = new Vertex(vertices[objects[i][j][k]][0], vertices[objects[i][j][0]][1]);
				Vertex v2 = new Vertex(vertices[objects[i][j][k+1]][0], vertices[objects[i][j][1]][1]);
				Vertex v3 = new Vertex(vertices[objects[i][j][k+2]][0], vertices[objects[i][j][2]][1]);
				Color c = new Color(colours[objects[i][j][k+3]][0], colours[objects[i][j][k+3]][1], colours[objects[i][j][k+3]][2]);
				Triangle newTri = new Triangle(v1, v2, v3, c);
				//printTriangle(newTri);
				newObj.triangles.add(newTri);
			}
			allObjs.add(newObj);
		}
		// calculate the middle of every object, for each section, on the left side
		for (int q = 0; q < leftSidePos.length; q++) {
			float adjustX = leftSidePos[q][0];
			float adjustY = leftSidePos[q][1];

			Center newCenter = new Center(); 
			for (int s = 0; s < allObjs.size(); s++) { 
				float centerX = centres[s][0] * (INITIAL_WIDTH/NUM_SECTORS);
				float centerY = centres[s][1] * (INITIAL_WIDTH/NUM_SECTORS);		
				newCenter.objs[s] = new Vertex(centerX+adjustX, centerY+adjustY);
			}
			leftSideCenters.add(newCenter);
		}
		// calculate the middle of every object, for each section, on the right side
		for (int r = 0; r < rightSidePos.length; r++) {
			float adjustX = rightSidePos[r][0];
			float adjustY = rightSidePos[r][1];

			Center newCenter = new Center(); 
			for (int s = 0; s < allObjs.size(); s++) { 
				float centerX = centres[s][0] * (INITIAL_WIDTH/NUM_SECTORS);
				float centerY = centres[s][1] * (INITIAL_WIDTH/NUM_SECTORS);		
				newCenter.objs[s] = new Vertex(centerX+adjustX, centerY+adjustY);
			}
			rightSideCenters.add(newCenter);
		}
	}
	void initLeft() {
		for (int i = 0; i < allObjs.size(); i++) {						// for each polygon object
			int numTriangles = allObjs.get(i).triangles.size();
			leftSideObjs.add(new Poly(i));
			for (int j = 0; j < numTriangles; j++) { 					// for each triangle in the object		
				Triangle theOne = allObjs.get(i).triangles.get(j);
				for (int q = 0; q < leftSidePos.length; q++) {
					float adjustX = leftSidePos[q][0];
					float adjustY = leftSidePos[q][1];

					Vertex temp1 = new Vertex((theOne.v1.x * (INITIAL_WIDTH/NUM_SECTORS)) + adjustX, (theOne.v1.y * (INITIAL_HEIGHT/NUM_SECTORS)) + adjustY);
					Vertex temp2 = new Vertex((theOne.v2.x * (INITIAL_WIDTH/NUM_SECTORS)) + adjustX, (theOne.v2.y * (INITIAL_HEIGHT/NUM_SECTORS)) + adjustY);
					Vertex temp3 = new Vertex((theOne.v3.x * (INITIAL_WIDTH/NUM_SECTORS)) + adjustX, (theOne.v3.y * (INITIAL_HEIGHT/NUM_SECTORS)) + adjustY);

					Color newColor = new Color(allObjs.get(i).triangles.get(j).c.r, allObjs.get(i).triangles.get(j).c.g,allObjs.get(i).triangles.get(j).c.b);
					// save the left side sector coordinates
					leftSideObjs.get(i).triangles.add(new Triangle(temp1, temp2, temp3, newColor));
				}
			}
		}
	}
	void initRight() {
		for (int i = 0; i < allObjs.size(); i++) {							// for each polygon object
			int numTriangles = allObjs.get(i).triangles.size();
			rightSideObjs.add(new Poly(i));
			for (int j = 0; j < numTriangles; j++) { 						// for each triangle in the object		
				Triangle theOne = allObjs.get(i).triangles.get(j);
				for (int q = 0; q < rightSidePos.length; q++) {
					float adjustX = rightSidePos[q][0];						// adjust to the correct position	x
					float adjustY = rightSidePos[q][1];						// adjust to the correct position	y

					Vertex temp1 = new Vertex((theOne.v1.x * (INITIAL_WIDTH/NUM_SECTORS)) + adjustX, (theOne.v1.y * (INITIAL_HEIGHT/NUM_SECTORS)) + adjustY);
					Vertex temp2 = new Vertex((theOne.v2.x * (INITIAL_WIDTH/NUM_SECTORS)) + adjustX, (theOne.v2.y * (INITIAL_HEIGHT/NUM_SECTORS)) + adjustY);
					Vertex temp3 = new Vertex((theOne.v3.x * (INITIAL_WIDTH/NUM_SECTORS)) + adjustX, (theOne.v3.y * (INITIAL_HEIGHT/NUM_SECTORS)) + adjustY);

					Color newColor = new Color(allObjs.get(i).triangles.get(j).c.r, allObjs.get(i).triangles.get(j).c.g,allObjs.get(i).triangles.get(j).c.b);
					rightSideObjs.get(i).triangles.add(new Triangle(temp1, temp2, temp3, newColor));
				}
			}
		}
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		// Called when the canvas is (re-)created - use it for initial GL setup
		if (TRACE)
			System.out.println("-> executing init()");

		final GL2 gl = drawable.getGL().getGL2();
		gl.glClearColor(0.1f, 0.15f, 0.1f, 0.0f);
		initObjects();
		initLeft();
		initRight();
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		// Draws the display
		if (TRACE)
			System.out.println("-> executing display()");

		final GL2 gl = drawable.getGL().getGL2();
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		drawRightSide(gl);
		drawLeftSide(gl);
		drawSectorLines(gl);
	}
	
	public float[][] multiply(float[][] a, float[][] b) {
		float[][] result = new float[3][3];
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++)
				for (int k = 0; k < 3; k++)
					result[i][j] += a[i][k] * b[k][j];
		return result;
	}
	
	public Vertex shearVertex(Vertex v, float x) {
		float[][] shear = { {1, x, 0}, 
				{0, 1, 0},
				{0, 0, 1}};
		float[][] thisVertex = { {v.x, 0, 0}, 
				{v.y, 0, 0},
				{  1, 0, 0}};
		float [][] transIdentity = multiply(IDENTITY, shear);
		float [][] result = multiply(transIdentity, thisVertex);
		return new Vertex(result[0][0], result[1][0]);		
	}

	public Vertex translateVertex(Vertex v, float x, float y) {
		float[][] translate = { {1, 0, x}, 
				{0, 1, y},
				{0, 0, 1}};
		float[][] thisVertex = { {v.x, 0, 0}, 
				{v.y, 0, 0},
				{  1, 0, 0}};
		float [][] transIdentity = multiply(IDENTITY, translate);
		float [][] result = multiply(transIdentity, thisVertex);
		return new Vertex(result[0][0], result[1][0]);		
	}

	public Vertex rotateVertex(Vertex v, float angle) {
		float rads = (float) Math.toRadians(angle);		
		float cos = (float) Math.cos(rads);
		float sin = (float) Math.sin(rads);

		float[][] rotate = { {cos, -sin, 0}, 
				{sin,  cos, 0},
				{  0,    0, 1}};
		float[][] thisVertex = { {v.x, 0, 0}, 
				{v.y, 0, 0},
				{  1, 0, 0}};
		float [][] IdentityRotate = multiply(IDENTITY, rotate);
		float [][] result = multiply(IdentityRotate, thisVertex);
		return new Vertex(result[0][0], result[1][0]);
	}

	public Vertex scaleVertex(Vertex v, float x, float y) {
		float[][] scale = { {x, 0, 0}, 
				{0, y, 0},
				{0, 0, 1}};	
		float [][] scaleIdentity = multiply(IDENTITY, scale);
		float[][] thisVertex = { {v.x, 0, 0}, 
				{v.y, 0, 0},
				{  1, 0, 0}};
		float [][] result = multiply(scaleIdentity, thisVertex);
		//printMatrix(result);
		return new Vertex(result[0][0], result[1][0]);
	}

	public Vertex getLeftsideSectorCenter(int secNum) {
		// [0|1]
		// [2|3]
		// [4|5]
		// [6|7]
		Vertex center = null;
		switch(secNum) {
		case 0:
		case 2:
		case 4:
		case 6:
			center = new Vertex(7*(-(INITIAL_WIDTH/8)) + INITIAL_WIDTH , ((secNum+1)*(-(INITIAL_HEIGHT/8)) + INITIAL_HEIGHT) );
			break;
		case 1:
		case 3:
		case 5:
		default:
			center = new Vertex(-5*(INITIAL_WIDTH/8) + INITIAL_WIDTH , (secNum*(-(INITIAL_HEIGHT/8)) + INITIAL_HEIGHT) );
			break;

		}
		return center;
	}
	private void drawLeftsideSector1(GL2 gl) {
		final int SECTOR = 0;	// MINUS ONE, STARTS AT ZERO
		for (int i = 0; i < leftSideObjs.size(); i++) {						// for each object
			for (int j = SECTOR; j < leftSideObjs.get(i).triangles.size(); j+=8) {
				// for each triangle	
				Triangle theOne = leftSideObjs.get(i).triangles.get(j);
				gl.glBegin(GL2.GL_TRIANGLES);
				gl.glColor3f(theOne.c.r, theOne.c.g, theOne.c.b);
				gl.glVertex2f(theOne.v1.x, theOne.v1.y);
				gl.glVertex2f(theOne.v2.x, theOne.v2.y);
				gl.glVertex2f(theOne.v3.x, theOne.v3.y);
				gl.glEnd();
			}
		}
	}

	private void drawLeftsideSector2(GL2 gl) {
		System.out.println("PRINTING SECTOR [ 2 ]");
		// 2. Rotate the scene by 30 degrees (π/6 radians) around its centre.
		
		final int SECTOR = 1;	// MINUS ONE, STARTS AT ZERO		
		for (int i = 0; i < leftSideObjs.size(); i++) {						// for each object
			for (int j = SECTOR; j < leftSideObjs.get(i).triangles.size(); j+=8) {
				// for each triangle	
				Triangle theOne = leftSideObjs.get(i).triangles.get(j);
				// center of sector 2 , right side
				Vertex center = getLeftsideSectorCenter(SECTOR);
				// CREATE AN ARRAY OF VERTICES TO BE TRANSFORMED
				Vertex [] transformed = new Vertex[3];
				transformed[0] = new Vertex(theOne.v1.x, theOne.v1.y);
				transformed[1] = new Vertex(theOne.v2.x, theOne.v2.y);
				transformed[2] = new Vertex(theOne.v3.x, theOne.v3.y);

				gl.glBegin(GL2.GL_TRIANGLES);
				gl.glColor3f(theOne.c.r, theOne.c.g, theOne.c.b);
				for (int n = 0; n < transformed.length; n++) {
					// RUN EACH VERTEX IN THE TRIANGLE THROUGH THE MATRIX MULTIPLICATION
					// translate to the origin
					Vertex trans = translateVertex(transformed[n], -center.x, -center.y);
					// rotate by 30 degrees
					Vertex rotate = rotateVertex(trans, 30);
					// translate back to the center of the sector
					Vertex result = translateVertex(rotate, center.x, center.y);
					gl.glVertex2f(result.x, result.y);
				}
				gl.glEnd();
			}
		}
	}

	private void drawLeftsideSector3(GL2 gl) {
		System.out.println("PRINTING SECTOR [ 3 ]");
		//  3. Scale the scene non-uniformly by 50% horizontally and 125% vertically, without changing the position of its centre.

		final int SECTOR = 2;	// MINUS ONE, STARTS AT ZERO
		for (int i = 0; i < leftSideObjs.size(); i++) {						// for each object
			for (int j = SECTOR; j < leftSideObjs.get(i).triangles.size(); j+=8) {
				// for each triangle	
				Triangle theOne = leftSideObjs.get(i).triangles.get(j);
				// center of sector 2 , right side
				Vertex center = getLeftsideSectorCenter(SECTOR);
				// CREATE AN ARRAY OF VERTICES TO BE TRANSFORMED
				Vertex [] transformed = new Vertex[3];
				transformed[0] = new Vertex(theOne.v1.x, theOne.v1.y);
				transformed[1] = new Vertex(theOne.v2.x, theOne.v2.y);
				transformed[2] = new Vertex(theOne.v3.x, theOne.v3.y);

				gl.glBegin(GL2.GL_TRIANGLES);
				gl.glColor3f(theOne.c.r, theOne.c.g, theOne.c.b);
				for (int n = 0; n < transformed.length; n++) {
					// RUN EACH VERTEX IN THE TRIANGLE THROUGH THE MATRIX MULTIPLICATION
					// translate to the origin
					Vertex trans = translateVertex(transformed[n], -center.x, -center.y);
					// rotate by 30 degrees
					Vertex scale = scaleVertex(trans, 0.75f, 1.25f);
					// translate back to the center of the sector
					Vertex result = translateVertex(scale, center.x, center.y);
					gl.glVertex2f(result.x, result.y);
				}
				gl.glEnd();
			}
		}
	}

	private void drawLeftsideSector4(GL2 gl) {
		System.out.println("PRINTING SECTOR [ 4 ]");
		// 4. Scale each object in the scene individually by 60% (uniformly), without moving the object's centre.

		final int SECTOR = 3;	// MINUS ONE, STARTS AT ZERO
		for (int i = 0; i < leftSideObjs.size(); i++) {						// for each object
			for (int j = SECTOR; j < leftSideObjs.get(i).triangles.size(); j+=8) {
				// for each triangle	
				Triangle theOne = leftSideObjs.get(i).triangles.get(j);
				// CREATE AN ARRAY OF VERTICES TO BE TRANSFORMED
				Vertex [] transformed = new Vertex[3];
				transformed[0] = new Vertex(theOne.v1.x, theOne.v1.y);
				transformed[1] = new Vertex(theOne.v2.x, theOne.v2.y);
				transformed[2] = new Vertex(theOne.v3.x, theOne.v3.y);

				gl.glBegin(GL2.GL_TRIANGLES);
				gl.glColor3f(theOne.c.r, theOne.c.g, theOne.c.b);
				for (int n = 0; n < transformed.length; n++) {
					// RUN EACH VERTEX IN THE TRIANGLE THROUGH THE MATRIX MULTIPLICATION
					// get the left side objects center and translate it to the origin
					Vertex trans = translateVertex(transformed[n], -leftSideCenters.get(SECTOR).objs[i].x, -leftSideCenters.get(SECTOR).objs[i].y);
					// scale uniformly by 60%
					Vertex scale = scaleVertex(trans, 0.6f, 0.6f);
					// translate back to the center of the sector
					Vertex result = translateVertex(scale, leftSideCenters.get(SECTOR).objs[i].x, leftSideCenters.get(SECTOR).objs[i].y);
					gl.glVertex2f(result.x, result.y);
				}
				gl.glEnd();
			}
		}
	}

	private void drawLeftsideSector5(GL2 gl) {
		System.out.println("PRINTING SECTOR [ 5 ]");
		// 5. Rotate the house and roof as a group around the centre of the house, and the tree trunk, leaves, and flower as a group around the 
		//    centre of the trunk, by -22.5° (π/8 radians) without moving those centres.

		ObjectName name = null;
		final int SECTOR = 4;	// MINUS ONE, STARTS AT ZERO
		for (int i = 0; i < leftSideObjs.size(); i++) {						// for each object
			for (int j = SECTOR; j < leftSideObjs.get(i).triangles.size(); j+=8) {
				// for each triangle	
				Triangle theOne = leftSideObjs.get(i).triangles.get(j);
				float houseCenterX = leftSideCenters.get(SECTOR).objs[name.HOUSE.i()].x;
				float houseCenterY = leftSideCenters.get(SECTOR).objs[name.HOUSE.i()].y;
				float trunkCenterX = leftSideCenters.get(SECTOR).objs[name.TRUNK.i()].x;
				float trunkCenterY = leftSideCenters.get(SECTOR).objs[name.TRUNK.i()].y;
				// CREATE AN ARRAY OF VERTICES TO BE TRANSFORMED
				Vertex [] transformed = new Vertex[3];
				transformed[0] = new Vertex(theOne.v1.x, theOne.v1.y);
				transformed[1] = new Vertex(theOne.v2.x, theOne.v2.y);
				transformed[2] = new Vertex(theOne.v3.x, theOne.v3.y);
				Vertex result = null;
				Vertex trans = null;
				Vertex rotate = null;

				gl.glBegin(GL2.GL_TRIANGLES);
				gl.glColor3f(theOne.c.r, theOne.c.g, theOne.c.b);
				for (int n = 0; n < transformed.length; n++) {

					if (i == name.HOUSE.i() || i == name.ROOF.i()) {
						trans = translateVertex(transformed[n], -houseCenterX, -houseCenterY);
						rotate = rotateVertex(trans, -22.5f);
						result = translateVertex(rotate, houseCenterX, houseCenterY);
						gl.glVertex2f(result.x, result.y);
					}else if (i == name.TRUNK.i() || i == name.LEAVES.i() || i == name.FLOWER.i()) {
						trans = translateVertex(transformed[n], -trunkCenterX, -trunkCenterY);
						rotate = rotateVertex(trans, -22.5f);
						result = translateVertex(rotate, trunkCenterX, trunkCenterY);
						gl.glVertex2f(result.x, result.y);
					}else{
						gl.glVertex2f(transformed[n].x, transformed[n].y);
					}
				}
				gl.glEnd();
			}
		}
	}

	private void drawLeftsideSector6(GL2 gl) {
		System.out.println("PRINTING SECTOR [ 6 ]");
		/*
		 * 6. Draw the tree so that its centre x-position is the house's original centre x-position, and the house's x centre on the tree's 
     original x centre. In other words, swap the horizontal positions of the two. Move the flower with the tree, but also swap the side it 
     is on. The horizontal distance from the centre of the tree to the centre of the flower is the same, it is just on the other side of 
     the tree. The flower is not reflected.
		 */

		ObjectName name = null;
		float thePosition = 0.0f;
		final int SECTOR = 5;	// MINUS ONE, STARTS AT ZERO

		for (int i = 0; i < leftSideObjs.size(); i++) {						// for each object
			for (int j = SECTOR; j < leftSideObjs.get(i).triangles.size(); j+=8) {
				// for each triangle	
				Triangle theOne = leftSideObjs.get(i).triangles.get(j);
				float houseCenterX = leftSideCenters.get(SECTOR).objs[name.HOUSE.i()].x;
				float trunkCenterX = leftSideCenters.get(SECTOR).objs[name.TRUNK.i()].x;
				float flowerCenterX = leftSideCenters.get(SECTOR).objs[name.FLOWER.i()].x;

				// CREATE AN ARRAY OF VERTICES TO BE TRANSFORMED
				Vertex [] transformed = new Vertex[3];
				transformed[0] = new Vertex(theOne.v1.x, theOne.v1.y);
				transformed[1] = new Vertex(theOne.v2.x, theOne.v2.y);
				transformed[2] = new Vertex(theOne.v3.x, theOne.v3.y);

				Vertex trans = null;

				gl.glBegin(GL2.GL_TRIANGLES);
				gl.glColor3f(theOne.c.r, theOne.c.g, theOne.c.b);
				for (int n = 0; n < transformed.length; n++) {
					if (i == name.HOUSE.i() || i == name.ROOF.i()) {
						// swap the center x positions with trunk
						thePosition = Math.abs(trunkCenterX - houseCenterX);
						trans = translateVertex(transformed[n], thePosition, 8);
						gl.glVertex2f(trans.x, trans.y);
					}else if (i == name.TRUNK.i() || i == name.LEAVES.i()) {
						// swap the center x positions with house
						thePosition = Math.abs(houseCenterX - trunkCenterX);
						trans = translateVertex(transformed[n], -thePosition, 8);
						gl.glVertex2f(trans.x, trans.y);
					}else if (i == name.FLOWER.i()) {
						thePosition = 2*(Math.abs(trunkCenterX - flowerCenterX));
						thePosition += Math.abs(houseCenterX - trunkCenterX);
						trans = translateVertex(transformed[n], -thePosition, 8);
						gl.glVertex2f(trans.x, trans.y);
					}else{
						gl.glVertex2f(transformed[n].x, transformed[n].y);
					}
				}
				gl.glEnd();
			}
		}
	}

	private void drawLeftsideSector7(GL2 gl) {
		System.out.println("PRINTING SECTOR [ 7 ]");
		/*
		 *      7. Windstorm! Shear the house by -50% in the x direction from its centre (note that this does move the base of the house). Lift the 
     roof from its original position by 0.5 units in the y direction, and rotate it around its centre by 15° (π/12 radians). The trunk of
      the tree is in its original spot. The flower is mirrored around the x axis, along its centre. 
		 */

		ObjectName name = null;
		final int SECTOR = 6;	// MINUS ONE, STARTS AT ZERO
		for (int i = 0; i < leftSideObjs.size(); i++) {						// for each object
			for (int j = SECTOR; j < leftSideObjs.get(i).triangles.size(); j+=8) {
				// for each triangle	
				Triangle theOne = leftSideObjs.get(i).triangles.get(j);

				// CREATE AN ARRAY OF VERTICES TO BE TRANSFORMED
				Vertex [] transformed = new Vertex[3];
				transformed[0] = new Vertex(theOne.v1.x, theOne.v1.y);
				transformed[1] = new Vertex(theOne.v2.x, theOne.v2.y);
				transformed[2] = new Vertex(theOne.v3.x, theOne.v3.y);

				/*
				 * Each of the triangles that make up the leaves of the tree are "exploded" from their original 
				 * location by being transformed along a vector from the centre of the leaves to the centre of 
				 * each individual leaf. Calculate the centre of a leaf (triangle) as the average of the three points.
				 */

				Vertex trans = null, shear = null, rotate = null, scale = null, transback = null, result = null;

				gl.glBegin(GL2.GL_TRIANGLES);
				gl.glColor3f(theOne.c.r, theOne.c.g, theOne.c.b);
				for (int n = 0; n < transformed.length; n++) {
					if (i == name.HOUSE.i()) {
						// house is sheared by -50%
						float houseCenterX = leftSideCenters.get(SECTOR).objs[name.HOUSE.i()].x;
						float houseCenterY = leftSideCenters.get(SECTOR).objs[name.HOUSE.i()].y;

						trans = translateVertex(transformed[n], -houseCenterX, -houseCenterY);
						shear = shearVertex(trans, -0.5f);
						result = translateVertex(shear, houseCenterX + (houseCenterX/2 + houseCenterX/6), houseCenterY);
						gl.glVertex2f(result.x, result.y);

					}else if (i == name.ROOF.i()) {
						// roof is sheared, rotated and translated
						float roofCenterX = leftSideCenters.get(SECTOR).objs[name.ROOF.i()].x;
						float roofCenterY = leftSideCenters.get(SECTOR).objs[name.ROOF.i()].y;

						trans = translateVertex(transformed[n], -roofCenterX, -roofCenterY);
						rotate = rotateVertex(trans, 15);								
						transback = translateVertex(rotate, roofCenterX, roofCenterY);
						result = translateVertex(transback, -5, (roofCenterY/2.5f));
						gl.glVertex2f(result.x, result.y);

					}else if (i == name.FLOWER.i()) {
						/*
						 * The flower is mirrored around the x axis, along its centre.
						 */
						float flowerCenterX = leftSideCenters.get(SECTOR).objs[name.FLOWER.i()].x;
						float flowerCenterY = leftSideCenters.get(SECTOR).objs[name.FLOWER.i()].y;

						trans = translateVertex(transformed[n], -flowerCenterX, -flowerCenterY);	
						scale = scaleVertex(trans, -1.0f, 1.0f);
						rotate = rotateVertex(scale, 180);
						result = translateVertex(rotate, flowerCenterX, flowerCenterY);		
						gl.glVertex2f(result.x, result.y);

					}else if (i == name.LEAVES.i()) {
						//the leaves are exploded out wards from the middle of the leaves 
						float leavesCenterX = leftSideCenters.get(SECTOR).objs[name.LEAVES.i()].x;
						float leavesCenterY = leftSideCenters.get(SECTOR).objs[name.LEAVES.i()].y;

						float avgX = (transformed[0].x + transformed[1].x + transformed[2].x)/3.0f;
						float avgY = (transformed[0].y + transformed[1].y + transformed[2].y)/3.0f;
						float deltaX = avgX - leavesCenterX;

						trans = translateVertex(transformed[n], -leavesCenterX, -leavesCenterY);	// move to origin
						// move the leaf HERE
						transback = translateVertex(trans, deltaX, avgY);						// move leaf along path
						result = translateVertex(transback, leavesCenterX, 0.0f);		// move back
						gl.glVertex2f(result.x, result.y);

					}else{
						gl.glVertex2f(transformed[n].x, transformed[n].y);
					}
				}
				gl.glEnd();
			}
		}

	}

	private void drawLeftsideSector8(GL2 gl) {
		System.out.println("PRINTING SECTOR [ 8 ]");

		/*
		 8. The tree is transformed to the centre of the scene, and the flower along with it. The cluster of leaves is drawn repeatedly 
     "clock-style" in 12 evenly-spaced positions around the centre of the scene. The leaves are 1 unit away from the centre of the scene 
     and are uniformly scaled by 50% in both directions.
		 */

		ObjectName name = null;
		Vertex trans = null, scale = null, rotate = null, result = null;
		final int SECTOR = 7;	// minus one, starts at zero
		Vertex [] circlePoints = new Vertex[12];

		float trunkCenterX = leftSideCenters.get(SECTOR).objs[name.TRUNK.i()].x;
		float trunkCenterY = leftSideCenters.get(SECTOR).objs[name.TRUNK.i()].y;
		float leavesCenterX = leftSideCenters.get(SECTOR).objs[name.LEAVES.i()].x;
		float leavesCenterY = leftSideCenters.get(SECTOR).objs[name.LEAVES.i()].y;

		for (int i = 0; i < leftSideObjs.size(); i++) {						// for each object
			for (int j = SECTOR; j < leftSideObjs.get(i).triangles.size(); j+=8) {
				// for each triangle	
				Triangle theOne = leftSideObjs.get(i).triangles.get(j);
				Vertex center = getLeftsideSectorCenter(SECTOR);

				// CREATE AN ARRAY OF VERTICES TO BE TRANSFORMED
				Vertex [] transformed = new Vertex[3];
				transformed[0] = new Vertex(theOne.v1.x, theOne.v1.y);
				transformed[1] = new Vertex(theOne.v2.x, theOne.v2.y);
				transformed[2] = new Vertex(theOne.v3.x, theOne.v3.y);

				gl.glBegin(GL2.GL_TRIANGLES);

				for (int n = 0; n < transformed.length; n++ ) {
					if (i == name.TRUNK.i() || i == name.FLOWER.i()) { 
						trans = translateVertex(transformed[n], -trunkCenterX, -trunkCenterY);
						result = translateVertex(trans, center.x, center.y);
						gl.glColor3f(theOne.c.r, theOne.c.g, theOne.c.b);
						gl.glVertex2f(result.x, result.y);
					}else if (i == name.HOUSE.i() || i == name.ROOF.i()) {
						// DO NOT DISPLAY
					}else if (i == name.LEAVES.i()) {
						// displace leaves 1 unit away from the center
						float displaceX = Math.abs(center.x - INITIAL_WIDTH/4.5f);
						// radius from the center of the sector to one unit away from the center
						float radius = displaceX - center.x;
						int counter = 0;
						// this will create a parametric circle with 12 points
						for(float t = 0; counter < 12 && t < 2 * Math.PI; t += Math.PI / 6) {
							float x = (float)Math.cos(t) * radius + center.x;
							float y = (float)Math.sin(t) * radius + center.y;
							Vertex newPoint = new Vertex(x, y);
							trans = translateVertex(newPoint, center.x, center.y);
							scale = scaleVertex(trans, 0.5f, 0.5f);
							circlePoints[counter] = scale;
							counter++;
						}
					}else{
						gl.glVertex2f(transformed[n].x, transformed[n].y);
					}
				}
				gl.glEnd();	
			}
		}

		for (int z = 0; z < circlePoints.length; z ++) {

			gl.glBegin(GL2.GL_TRIANGLES);
			for (int m = SECTOR; m < leftSideObjs.get(name.LEAVES.i()).triangles.size(); m+=8) {
				// for each triangle	
				Triangle temp = leftSideObjs.get(name.LEAVES.i()).triangles.get(m);
				// for vertex 1
				gl.glColor3f(temp.c.r, temp.c.g, temp.c.b);
				trans = translateVertex(temp.v1, -leavesCenterX, -leavesCenterY);
				rotate = rotateVertex(trans, z*30);
				scale = scaleVertex(rotate, 0.5f, 0.5f);
				result = translateVertex(scale, circlePoints[z].x, circlePoints[z].y);
				gl.glVertex2f(result.x, result.y);
				// for vertex 2
				trans = translateVertex(temp.v2, -leavesCenterX, -leavesCenterY);
				rotate = rotateVertex(trans, z*30);
				scale = scaleVertex(rotate, 0.5f, 0.5f);
				result = translateVertex(scale, circlePoints[z].x, circlePoints[z].y);
				gl.glVertex2f(result.x, result.y);
				// for vertex 3
				trans = translateVertex(temp.v3, -leavesCenterX, -leavesCenterY);
				rotate = rotateVertex(trans, z*30);
				scale = scaleVertex(rotate, 0.5f, 0.5f);
				result = translateVertex(scale, circlePoints[z].x, circlePoints[z].y);
				gl.glVertex2f(result.x, result.y);
			}
		}
		gl.glEnd();
	}

	public Vertex getRightsideSectorCenter(int secNum) {
		// Depending on the input sector number, it will return the vertex center for that sector for quick reference
		// [0|1]
		// [2|3]
		// [4|5]
		// [6|7]
		Vertex center = null;
		switch(secNum) {
		case 0:
		case 2:
		case 4:
		case 6:
			center = new Vertex(3*(-(INITIAL_WIDTH/8)) + INITIAL_WIDTH , ((secNum+1)*(-(INITIAL_HEIGHT/8)) + INITIAL_HEIGHT) );
			break;
		case 1:
		case 3:
		case 5:
		default:
			center = new Vertex(-(INITIAL_WIDTH/8) + INITIAL_WIDTH , (secNum*(-(INITIAL_HEIGHT/8)) + INITIAL_HEIGHT) );
			break;

		}
		return center;
	}

	private void drawRightsideSector1(GL2 gl) {
		System.out.println("PRINTING SECTOR [ 1 ]");
		final int SECTOR = 0;	// MINUS ONE, STARTS AT ZERO
		
		for (int i = 0; i < rightSideObjs.size(); i++) {						// for each object
			for (int j = SECTOR; j < rightSideObjs.get(i).triangles.size(); j+=8) {
				// for each triangle	
				Triangle theOne = rightSideObjs.get(i).triangles.get(j);
				gl.glBegin(GL2.GL_TRIANGLES);
				gl.glColor3f(theOne.c.r, theOne.c.g, theOne.c.b);
				gl.glVertex2f(theOne.v1.x, theOne.v1.y);
				gl.glVertex2f(theOne.v2.x, theOne.v2.y);
				gl.glVertex2f(theOne.v3.x, theOne.v3.y);
				gl.glEnd();
			}	
		}
	}

	// RIGHT SIDE using openGL functions 
	private void drawRightsideSector2(GL2 gl) {
		System.out.println("PRINTING SECTOR [ 2 ]");
		// 2. Rotate the scene by 30 degrees (π/6 radians) around its centre.

		final int SECTOR = 1;	// MINUS ONE, STARTS AT ZERO		
		for (int i = 0; i < rightSideObjs.size(); i++) {						// for each object
			for (int j = SECTOR; j < rightSideObjs.get(i).triangles.size(); j+=8) {
				// for each triangle	
				Triangle theOne = rightSideObjs.get(i).triangles.get(j);
				// center of sector 2 , right side
				Vertex center = getRightsideSectorCenter(SECTOR);

				// ORDER OF OPERATIONS IN REVERSE BECAUSE OF THE STACK	
				gl.glMatrixMode(GL2.GL_MATRIX_MODE);
				gl.glLoadIdentity();
				gl.glPushMatrix();
				// translate back to center of the sector
				gl.glTranslatef( center.x, center.y, 0.0f);
				//rotate 30 degrees
				gl.glRotatef(30f, 0, 0, 1);
				// translate to origin
				gl.glTranslatef( -center.x, -center.y, -0.0f);
				gl.glPushMatrix();
				
				gl.glBegin(GL2.GL_TRIANGLES);
				gl.glColor3f(theOne.c.r, theOne.c.g, theOne.c.b);
				gl.glVertex2f(theOne.v1.x, theOne.v1.y);
				gl.glVertex2f(theOne.v2.x, theOne.v2.y);
				gl.glVertex2f(theOne.v3.x, theOne.v3.y);
				gl.glEnd();
				gl.glPopMatrix();
			}
		}

	}
	private void drawRightsideSector3(GL2 gl) {
		System.out.println("PRINTING SECTOR [ 3 ]");
		//  3. Scale the scene non-uniformly by 50% horizontally and 125% vertically, without changing the position of its centre.

		final int SECTOR = 2;	// MINUS ONE, STARTS AT ZERO
		for (int i = 0; i < rightSideObjs.size(); i++) {						// for each object
			for (int j = SECTOR; j < rightSideObjs.get(i).triangles.size(); j+=8) {
				// for each triangle	
				Triangle theOne = rightSideObjs.get(i).triangles.get(j);

				// center of sector 2 , right side
				Vertex center = getRightsideSectorCenter(SECTOR);

				// ORDER OF OPERATIONS IN REVERSE BECAUSE OF THE STACK	
				gl.glMatrixMode(GL2.GL_MATRIX_MODE);
				gl.glLoadIdentity();
				gl.glPushMatrix();

				// translate back to center of the sector
				gl.glTranslatef( center.x, center.y, 0.0f);
				// non-uniformly by 75% horizontally and 125% vertically
				gl.glScalef(0.75f, 1.25f, 0.0f);		
				// translate to origin
				gl.glTranslatef( -center.x, -center.y, -0.0f);
				gl.glPushMatrix();
				
				gl.glBegin(GL2.GL_TRIANGLES);
				gl.glColor3f(theOne.c.r, theOne.c.g, theOne.c.b);
				gl.glVertex2f(theOne.v1.x, theOne.v1.y);
				gl.glVertex2f(theOne.v2.x, theOne.v2.y);
				gl.glVertex2f(theOne.v3.x, theOne.v3.y);
				gl.glEnd();
				gl.glPopMatrix();
			}
		}
	}
	private void drawRightsideSector4(GL2 gl) {
		System.out.println("PRINTING SECTOR [ 4 ]");
		// 4. Scale each object in the scene individually by 60% (uniformly), without moving the object's centre.

		final int SECTOR = 3;	// MINUS ONE, STARTS AT ZERO
		for (int i = 0; i < rightSideObjs.size(); i++) {						// for each object
			for (int j = SECTOR; j < rightSideObjs.get(i).triangles.size(); j+=8) {
				// for each triangle	
				Triangle theOne = rightSideObjs.get(i).triangles.get(j);

				// ORDER OF OPERATIONS IN REVERSE BECAUSE OF THE STACK	
				gl.glMatrixMode(GL2.GL_MATRIX_MODE);
				gl.glLoadIdentity();
				gl.glPushMatrix();

				// translate back to center of each object
				gl.glTranslatef(rightSideCenters.get(SECTOR).objs[i].x, rightSideCenters.get(SECTOR).objs[i].y, 0.0f);
				// uniformly by 60%
				gl.glScalef(0.6f, 0.6f, 0.0f);		
				// translate to origin
				gl.glTranslatef(-rightSideCenters.get(SECTOR).objs[i].x, -rightSideCenters.get(SECTOR).objs[i].y, -0.0f);
				gl.glPushMatrix();
				
				gl.glBegin(GL2.GL_TRIANGLES);
				gl.glColor3f(theOne.c.r, theOne.c.g, theOne.c.b);
				gl.glVertex2f(theOne.v1.x, theOne.v1.y);
				gl.glVertex2f(theOne.v2.x, theOne.v2.y);
				gl.glVertex2f(theOne.v3.x, theOne.v3.y);
				gl.glEnd();
				gl.glPopMatrix();
			}
		}
	}
	private void drawRightsideSector5(GL2 gl) {
		System.out.println("PRINTING SECTOR [ 5 ]");
		// 5. Rotate the house and roof as a group around the centre of the house, and the tree trunk, leaves, and flower as a group around the 
		//    centre of the trunk, by -22.5° (π/8 radians) without moving those centres.

		ObjectName name = null;
		final int SECTOR = 4;	// MINUS ONE, STARTS AT ZERO
		for (int i = 0; i < rightSideObjs.size(); i++) {						// for each object
			for (int j = SECTOR; j < rightSideObjs.get(i).triangles.size(); j+=8) {
				// for each triangle	
				Triangle theOne = rightSideObjs.get(i).triangles.get(j);
				float houseCenterX = rightSideCenters.get(SECTOR).objs[name.HOUSE.i()].x;
				float houseCenterY = rightSideCenters.get(SECTOR).objs[name.HOUSE.i()].y;
				float trunkCenterX = rightSideCenters.get(SECTOR).objs[name.TRUNK.i()].x;
				float trunkCenterY = rightSideCenters.get(SECTOR).objs[name.TRUNK.i()].y;

				// ORDER OF OPERATIONS IN REVERSE BECAUSE OF THE STACK	
				gl.glMatrixMode(GL2.GL_MATRIX_MODE);
				gl.glLoadIdentity();
				gl.glPushMatrix();

				// if house or roof objects, rotate around the centre of the house
				if (i == name.HOUSE.i() || i == name.ROOF.i()) {
					// translate back to center of each object
					gl.glTranslatef(houseCenterX, houseCenterY, 0.0f);
					gl.glRotatef(-22.5f, 0, 0, 1);		// ROLL
					// translate to origin
					gl.glTranslatef(-houseCenterX, -houseCenterY, -0.0f);
					gl.glPushMatrix();
				}

				// if trunk, leaves or flower, rotate around the trunk
				if (i == name.TRUNK.i() || i == name.LEAVES.i() || i == name.FLOWER.i()) {
					// translate back to center of each object
					gl.glTranslatef(trunkCenterX, trunkCenterY, 0.0f);
					gl.glRotatef(-22.5f, 0, 0, 1);		// ROLL
					/// translate to origin
					gl.glTranslatef(-trunkCenterX, -trunkCenterY, -0.0f);
					gl.glPushMatrix();
				}

				gl.glBegin(GL2.GL_TRIANGLES);
				gl.glColor3f(theOne.c.r, theOne.c.g, theOne.c.b);
				gl.glVertex2f(theOne.v1.x, theOne.v1.y);
				gl.glVertex2f(theOne.v2.x, theOne.v2.y);
				gl.glVertex2f(theOne.v3.x, theOne.v3.y);
				gl.glEnd();
				gl.glPopMatrix();
			}
		}
	}
	private void drawRightsideSector6(GL2 gl) {
		System.out.println("PRINTING SECTOR [ 6 ]");
		/*
		 * 6. Draw the tree so that its centre x-position is the house's original centre x-position, and the house's x centre on the tree's 
     original x centre. In other words, swap the horizontal positions of the two. Move the flower with the tree, but also swap the side it 
     is on. The horizontal distance from the centre of the tree to the centre of the flower is the same, it is just on the other side of 
     the tree. The flower is not reflected.
		 */

		ObjectName name = null;
		float thePosition = 0.0f;
		final int SECTOR = 5;	// MINUS ONE, STARTS AT ZERO

		for (int i = 0; i < rightSideObjs.size(); i++) {						// for each object
			for (int j = SECTOR; j < rightSideObjs.get(i).triangles.size(); j+=8) {
				// for each triangle	
				Triangle theOne = rightSideObjs.get(i).triangles.get(j);
				float houseCenterX = rightSideCenters.get(SECTOR).objs[name.HOUSE.i()].x;
				float trunkCenterX = rightSideCenters.get(SECTOR).objs[name.TRUNK.i()].x;
				float flowerCenterX = rightSideCenters.get(SECTOR).objs[name.FLOWER.i()].x;

				// ORDER OF OPERATIONS IN REVERSE BECAUSE OF THE STACK	
				gl.glMatrixMode(GL2.GL_MATRIX_MODE);
				gl.glLoadIdentity();
				gl.glPushMatrix();

				if (i == name.HOUSE.i() || i == name.ROOF.i()) {
					// swap the center x positions with trunk
					thePosition = Math.abs(trunkCenterX - houseCenterX);
					gl.glTranslatef(thePosition, 0.0f, 0.0f);
					gl.glPushMatrix();
				}
				if (i == name.TRUNK.i() || i == name.LEAVES.i()) {
					// swap the center x positions with house
					thePosition = Math.abs(houseCenterX - trunkCenterX);
					gl.glTranslatef(-thePosition, 0.0f, 0.0f);
					gl.glPushMatrix();
				}

				if (i == name.FLOWER.i()) {
					thePosition = 2*(Math.abs(trunkCenterX - flowerCenterX));
					thePosition += Math.abs(houseCenterX - trunkCenterX);
					gl.glTranslatef(-thePosition, 0.0f, 0.0f);
					gl.glPushMatrix();
				}
				gl.glBegin(GL2.GL_TRIANGLES);
				gl.glColor3f(theOne.c.r, theOne.c.g, theOne.c.b);
				gl.glVertex2f(theOne.v1.x, theOne.v1.y);
				gl.glVertex2f(theOne.v2.x, theOne.v2.y);
				gl.glVertex2f(theOne.v3.x, theOne.v3.y);
				gl.glEnd();
				gl.glPopMatrix();
			}
		}
	}
	private void drawRightsideSector7(GL2 gl) {
		System.out.println("PRINTING SECTOR [ 7 ]");
		/*
		 *      7. Windstorm! Shear the house by -50% in the x direction from its centre (note that this does move the base of the house). Lift the 
     roof from its original position by 0.5 units in the y direction, and rotate it around its centre by 15° (π/12 radians). The trunk of
      the tree is in its original spot. The flower is mirrored around the x axis, along its centre. 
		 */

		// DRAW RIGHT SIDE
		ObjectName name = null;
		final int SECTOR = 6;	// MINUS ONE, STARTS AT ZERO
		for (int i = 0; i < rightSideObjs.size(); i++) {						// for each object
			for (int j = SECTOR; j < rightSideObjs.get(i).triangles.size(); j+=8) {
				// for each triangle	
				Triangle theOne = rightSideObjs.get(i).triangles.get(j);

				// ORDER OF OPERATIONS IN REVERSE BECAUSE OF THE STACK	
				gl.glMatrixMode(GL2.GL_MATRIX_MODE);
				gl.glLoadIdentity();
				gl.glPushMatrix();

				if (i == name.HOUSE.i()) {

					float houseCenterX = rightSideCenters.get(SECTOR).objs[name.HOUSE.i()].x;
					float houseCenterY = rightSideCenters.get(SECTOR).objs[name.HOUSE.i()].y;

					// move the house to its center position
					gl.glTranslatef(houseCenterX, houseCenterY, 0.0f);
					// move the house slight to stay within the sector
					float moveX = (houseCenterX/16);
					// position 4 == shear by -50% 
					float[] shear = { 1, 0, 0, 0, -0.5f, 1, 0, 0, 0, 0, 1, 0, moveX, 0, 0, 1};
					gl.glMultMatrixf(shear, 0);
					// translate to origin
					gl.glTranslatef(-houseCenterX, -houseCenterY, -0.0f);
					gl.glPushMatrix();

					gl.glBegin(GL2.GL_TRIANGLES);
					gl.glColor3f(theOne.c.r, theOne.c.g, theOne.c.b);
					gl.glVertex2f(theOne.v1.x, theOne.v1.y);
					gl.glVertex2f(theOne.v2.x, theOne.v2.y);
					gl.glVertex2f(theOne.v3.x, theOne.v3.y);
					gl.glEnd();

					gl.glPopMatrix();

				}else if (i == name.ROOF.i()) {

					float roofCenterX = rightSideCenters.get(SECTOR).objs[name.ROOF.i()].x;
					float roofCenterY = rightSideCenters.get(SECTOR).objs[name.ROOF.i()].y;

					gl.glTranslatef(roofCenterX, roofCenterY, 0.0f);		// center of roof
					gl.glRotatef(15, 0, 0, 1);								// rotate 15 degrees
					gl.glTranslatef(-roofCenterX, -roofCenterY, -0.0f);		// origin
					gl.glTranslatef(0, (roofCenterY/SECTOR_DIV), 0.0f);
					gl.glPushMatrix();

					gl.glBegin(GL2.GL_TRIANGLES);
					gl.glColor3f(theOne.c.r, theOne.c.g, theOne.c.b);
					gl.glVertex2f(theOne.v1.x, theOne.v1.y);
					gl.glVertex2f(theOne.v2.x, theOne.v2.y);
					gl.glVertex2f(theOne.v3.x, theOne.v3.y);
					gl.glEnd();

					gl.glPopMatrix();

				}else if (i == name.FLOWER.i()) {
					/*
					 * The flower is mirrored around the x axis, along its centre.
					 */
					float flowerCenterX = rightSideCenters.get(SECTOR).objs[name.FLOWER.i()].x;
					float flowerCenterY = rightSideCenters.get(SECTOR).objs[name.FLOWER.i()].y;

					gl.glTranslatef(flowerCenterX, flowerCenterY, 0.0f);		// center of roof
					gl.glScalef(-1.0f, 1.0f, 1.0f);
					gl.glRotatef(180, 0, 0, 1);
					gl.glTranslatef(-flowerCenterX, -flowerCenterY, -0.0f);		// origin
					gl.glPushMatrix();

					gl.glBegin(GL2.GL_TRIANGLES);
					gl.glColor3f(theOne.c.r, theOne.c.g, theOne.c.b);
					gl.glVertex2f(theOne.v1.x, theOne.v1.y);
					gl.glVertex2f(theOne.v2.x, theOne.v2.y);
					gl.glVertex2f(theOne.v3.x, theOne.v3.y);
					gl.glEnd();

					gl.glPopMatrix();

				}else if (i == name.LEAVES.i()) {

					float leavesCenterX = rightSideCenters.get(SECTOR).objs[name.LEAVES.i()].x;
					float leavesCenterY = rightSideCenters.get(SECTOR).objs[name.LEAVES.i()].y;

					/*
					 * Each of the triangles that make up the leaves of the tree are "exploded" from their original 
					 * location by being transformed along a vector from the centre of the leaves to the centre of 
					 * each individual leaf. Calculate the centre of a leaf (triangle) as the average of the three points.
					 */

					float avgX = (theOne.v1.x + theOne.v2.x + theOne.v3.x)/3.0f;
					float avgY = (theOne.v1.y + theOne.v2.y + theOne.v3.y)/3.0f;
					float deltaX = avgX - leavesCenterX;

					gl.glTranslatef(leavesCenterX, 0.0f, 0.0f);
					// move the leaf HERE
					gl.glTranslatef(deltaX, avgY, 0.0f);						// move leaf along path
					gl.glTranslatef(-leavesCenterX, -leavesCenterY, 0.0f);		// move to origin
					gl.glPushMatrix();

					gl.glBegin(GL2.GL_TRIANGLES);
					gl.glColor3f(theOne.c.r, theOne.c.g, theOne.c.b);
					gl.glVertex2f(theOne.v1.x, theOne.v1.y);
					gl.glVertex2f(theOne.v2.x, theOne.v2.y);
					gl.glVertex2f(theOne.v3.x, theOne.v3.y);
					gl.glEnd();

					gl.glPopMatrix();

				}else{
					gl.glBegin(GL2.GL_TRIANGLES);
					gl.glColor3f(theOne.c.r, theOne.c.g, theOne.c.b);
					gl.glVertex2f(theOne.v1.x, theOne.v1.y);
					gl.glVertex2f(theOne.v2.x, theOne.v2.y);
					gl.glVertex2f(theOne.v3.x, theOne.v3.y);
					gl.glEnd();
					gl.glPopMatrix();
				}
			}
		}

	}
	private void drawRightsideSector8(GL2 gl) {
		System.out.println("PRINTING SECTOR [ 8 ]");

		/*
		 *      8. The tree is transformed to the centre of the scene, and the flower along with it. The cluster of leaves is drawn repeatedly 
     "clock-style" in 12 evenly-spaced positions around the centre of the scene. The leaves are 1 unit away from the centre of the scene 
     and are uniformly scaled by 50% in both directions.
		 */

		ObjectName name = null;
		final int SECTOR = 7;	// minus one, starts at zero
		ArrayList<Vertex> circlePoints = new ArrayList<Vertex>();
		
		float leavesCenterX = rightSideCenters.get(SECTOR).objs[name.LEAVES.i()].x;
		float leavesCenterY = rightSideCenters.get(SECTOR).objs[name.LEAVES.i()].y;
		float trunkCenterX = rightSideCenters.get(SECTOR).objs[name.TRUNK.i()].x;
		float trunkCenterY = rightSideCenters.get(SECTOR).objs[name.TRUNK.i()].y;
		
		for (int i = 0; i < rightSideObjs.size(); i++) {						// for each object
			for (int j = SECTOR; j < rightSideObjs.get(i).triangles.size(); j+=8) {
				// for each triangle	
				Triangle theOne = rightSideObjs.get(i).triangles.get(j);

				// ORDER OF OPERATIONS IN REVERSE BECAUSE OF THE STACK	
				gl.glMatrixMode(GL2.GL_MATRIX_MODE);
				gl.glLoadIdentity();
				gl.glPushMatrix();

				Vertex center = getRightsideSectorCenter(SECTOR);

				if (i == name.TRUNK.i() || i == name.FLOWER.i()) { 
					gl.glTranslatef(center.x, center.y, 0.0f);
					gl.glTranslatef(-trunkCenterX, -trunkCenterY, 0.0f);
					gl.glPushMatrix();

					gl.glBegin(GL2.GL_TRIANGLES);
					gl.glColor3f(theOne.c.r, theOne.c.g, theOne.c.b);
					gl.glVertex2f(theOne.v1.x, theOne.v1.y);
					gl.glVertex2f(theOne.v2.x, theOne.v2.y);
					gl.glVertex2f(theOne.v3.x, theOne.v3.y);
					gl.glEnd();

					gl.glPopMatrix();

				}else if (i == name.HOUSE.i() || i == name.ROOF.i()) {
					// DO NOT DISPLAY
				}else if (i == name.LEAVES.i()) {
					// displace leaves 1 unit away from the center
					float displaceX = Math.abs(center.x - center.x/8);
					// radius from the center of the sector to one unit away from the center
					float radius = displaceX - center.x;
					int counter = 1;
					// this will create a parametric circle , keep a vertex for each 12 points
					for(float t = 0; t < 2 * Math.PI && counter < 13; t += Math.PI / 6) {

						float x = (float)Math.cos(t) * radius + center.x;
						float y = (float)Math.sin(t) * radius + center.y;
						circlePoints.add(new Vertex(x, y));	

						// ORDER OF OPERATIONS IN REVERSE BECAUSE OF THE STACK	
						gl.glMatrixMode(GL2.GL_MATRIX_MODE);
						gl.glLoadIdentity();
						gl.glPushMatrix();
						// translate the bundle of leaves to their new center in the circle and rotate them
						gl.glTranslatef(x, y, 0.0f);
						gl.glScalef(0.5f, 0.5f, 1.0f);
						gl.glRotatef(counter*30, 0, 0, 1);
						gl.glTranslatef(-leavesCenterX, -leavesCenterY, 0.0f);
						gl.glPushMatrix();
						counter++;
						
						for (int m = SECTOR; m < rightSideObjs.get(name.LEAVES.i()).triangles.size(); m+=8) {
							// for each triangle	
							Triangle temp = rightSideObjs.get(name.LEAVES.i()).triangles.get(m);

							gl.glBegin(GL2.GL_TRIANGLES);
							gl.glColor3f(temp.c.r, temp.c.g, temp.c.b);
							gl.glVertex2f(temp.v1.x, temp.v1.y);
							gl.glVertex2f(temp.v2.x, temp.v2.y);
							gl.glVertex2f(temp.v3.x, temp.v3.y);
							gl.glEnd();						
						}
						gl.glPopMatrix();
					}
				}else{
					gl.glBegin(GL2.GL_TRIANGLES);
					gl.glColor3f(theOne.c.r, theOne.c.g, theOne.c.b);
					gl.glVertex2f(theOne.v1.x, theOne.v1.y);
					gl.glVertex2f(theOne.v2.x, theOne.v2.y);
					gl.glVertex2f(theOne.v3.x, theOne.v3.y);
					gl.glEnd();
					gl.glPopMatrix();
				}
			}
		}
	}
	public void drawLeftSide(GL2 gl) {	
		// Cannot use any OpenGL matrix functions, must create your own 3x3 2D transformation 
		// Matrices by filling in the values multiply them, and apply them to the vertices before you draw them
		drawLeftsideSector1(gl);
		drawLeftsideSector2(gl);
		drawLeftsideSector3(gl);
		drawLeftsideSector4(gl);
		drawLeftsideSector5(gl);
		drawLeftsideSector6(gl);
		drawLeftsideSector7(gl);
		drawLeftsideSector8(gl);
	}
	public void drawRightSide(GL2 gl) {
		// Use the OpenGL simple matrix functions (glTranslate, etc.); do not load matrices. 
		drawRightsideSector1(gl);
		drawRightsideSector2(gl);
		drawRightsideSector3(gl);
		drawRightsideSector4(gl);
		drawRightsideSector5(gl);
		drawRightsideSector6(gl);
		drawRightsideSector7(gl);
		drawRightsideSector8(gl);
	}
	public void drawSectorLines(GL2 gl) {
		// middle vertical
		gl.glLineWidth(2.5f);
		gl.glBegin(GL2.GL_LINES);
		gl.glColor3f(0.90f, 0.90f, 0.90f);
		gl.glVertex2f(INITIAL_WIDTH/2, 0);
		gl.glVertex2f(INITIAL_WIDTH/2, INITIAL_HEIGHT);
		gl.glEnd();	
		// left vertical
		gl.glLineWidth(1.0f);
		gl.glBegin(GL2.GL_LINES);
		gl.glVertex2f(INITIAL_WIDTH/4, 0);
		gl.glVertex2f(INITIAL_WIDTH/4, INITIAL_HEIGHT);
		gl.glEnd();	
		// right vertical
		gl.glLineWidth(1.0f);
		gl.glBegin(GL2.GL_LINES);
		gl.glVertex2f(INITIAL_WIDTH/4 + (INITIAL_WIDTH/2), 0);
		gl.glVertex2f(INITIAL_WIDTH/4 + (INITIAL_WIDTH/2), INITIAL_HEIGHT);
		gl.glEnd();	
		// middle horizontal
		gl.glLineWidth(1.0f);
		gl.glBegin(GL2.GL_LINES);
		gl.glColor3f(0.90f, 0.90f, 0.90f);
		gl.glVertex2f(0, INITIAL_HEIGHT/2);
		gl.glVertex2f(INITIAL_WIDTH, INITIAL_HEIGHT/2);
		gl.glEnd();	
		// bottom horizontal 
		gl.glLineWidth(1.0f);
		gl.glBegin(GL2.GL_LINES);
		gl.glVertex2f(0, INITIAL_HEIGHT/4);
		gl.glVertex2f(INITIAL_WIDTH, INITIAL_HEIGHT/4);
		gl.glEnd();	
		// top horizontal
		gl.glLineWidth(1.0f);
		gl.glBegin(GL2.GL_LINES);
		gl.glVertex2f(0, INITIAL_HEIGHT/4 + (INITIAL_HEIGHT/2));
		gl.glVertex2f(INITIAL_WIDTH, INITIAL_HEIGHT/4 + (INITIAL_HEIGHT/2));
		gl.glEnd();	
	}

	float leftSidePos[][] = new float [][] {
			//[ 0 | 1 ]
			//[ 2 | 3 ]
			//[ 4 | 5 ]
			//[ 6 | 7 ]
			{(INITIAL_WIDTH/NUM_SECTORS), (INITIAL_HEIGHT-INITIAL_HEIGHT/NUM_SECTORS)},						// TOP-LEFT 		[0]
			{((INITIAL_WIDTH/2)-INITIAL_WIDTH/NUM_SECTORS)+5, (INITIAL_HEIGHT-INITIAL_HEIGHT/NUM_SECTORS)},	// TOP-RIGHT		[1]
			{((INITIAL_WIDTH/2)-INITIAL_WIDTH/SECTOR_DIV), (INITIAL_HEIGHT-INITIAL_HEIGHT/SECTOR_DIV)},		// TOP-MID-LEFT		[2]
			{(INITIAL_WIDTH/SECTOR_DIV)+10, (INITIAL_HEIGHT-INITIAL_HEIGHT/SECTOR_DIV)},					// TOP-MID-RIGHT	[3]
			{((INITIAL_WIDTH/2)-INITIAL_WIDTH/SECTOR_DIV), (INITIAL_HEIGHT/SECTOR_DIV)},					// BOTTOM-MID-LEFT	[4]
			{(INITIAL_WIDTH/SECTOR_DIV)+10, (INITIAL_HEIGHT/SECTOR_DIV)},									// BOTTOM-MID-RIGHT	[5]
			{(INITIAL_WIDTH/NUM_SECTORS), (INITIAL_HEIGHT/NUM_SECTORS)},									// BOTTOM-LEFT 		[6]
			{(INITIAL_WIDTH/SECTOR_DIV)+10, ((INITIAL_HEIGHT/2)-INITIAL_HEIGHT/SECTOR_DIV)}					// BOTTOM-RIGHT		[7]
	};
	float rightSidePos[][] = new float [][] {
			//[ 0 | 1 ]
			//[ 2 | 3 ]
			//[ 4 | 5 ]
			//[ 6 | 7 ]
			{(INITIAL_WIDTH-(INITIAL_WIDTH/SECTOR_DIV)), (INITIAL_HEIGHT-(INITIAL_HEIGHT/NUM_SECTORS))},	// TOP-LEFT			[0]
			{(INITIAL_WIDTH-INITIAL_WIDTH/NUM_SECTORS)+5, (INITIAL_HEIGHT-INITIAL_HEIGHT/NUM_SECTORS)},		// TOP-RIGHT		[1]
			{(INITIAL_WIDTH-INITIAL_WIDTH/SECTOR_DIV), (INITIAL_HEIGHT-INITIAL_HEIGHT/SECTOR_DIV)},			// TOP-MID-LEFT 	[2]
			{(INITIAL_WIDTH-(INITIAL_WIDTH/NUM_SECTORS))+5, (INITIAL_HEIGHT-(INITIAL_HEIGHT/SECTOR_DIV))},	// TOP-MID-RIGHT	[3]
			{(INITIAL_WIDTH-INITIAL_WIDTH/SECTOR_DIV), (INITIAL_HEIGHT/SECTOR_DIV)},						// BOTTOM-MID-LEFT 	[4]
			{(INITIAL_WIDTH-INITIAL_WIDTH/NUM_SECTORS)+5, ((INITIAL_HEIGHT/2)-INITIAL_HEIGHT/NUM_SECTORS)},	// BOTTOM-MID-RIGHT	[5]
			{(INITIAL_WIDTH-INITIAL_WIDTH/SECTOR_DIV), ((INITIAL_HEIGHT/2)-(INITIAL_HEIGHT/SECTOR_DIV))},	// BOTTOM-LEFT 		[6]
			{(INITIAL_WIDTH-INITIAL_WIDTH/NUM_SECTORS)+5, (INITIAL_HEIGHT/NUM_SECTORS)}						// BOTTOM-RIGHT		[7]
	};

	@Override
	public void dispose(GLAutoDrawable drawable) {
		// Called when the canvas is destroyed (reverse anything from init) 
		if (TRACE)
			System.out.println("-> executing dispose()");
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		// Called when the canvas has been resized
		// Note: glViewport(x, y, width, height) has already been called so don't bother if that's what you want
		if (TRACE)
			System.out.println("-> executing reshape(" + x + ", " + y + ", " + width + ", " + height + ")");

		final GL2 gl = drawable.getGL().getGL2();
		
		gl.glViewport(x, y, width, height);
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glOrtho(0, INITIAL_WIDTH, 0, INITIAL_HEIGHT, 0, 1.0);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl. glLoadIdentity();
		this.width = width;
		this.height = height;
	}
}
