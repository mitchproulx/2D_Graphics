/*
 * Author: Mitchell Proulx
 * Purpose: Creates a scene full of interactable
 * objects that can be duplicated, rotated, 
 * scaled and moved around the screen.
 * 
 */
import javax.swing.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.*;

public class ObjectSandbox implements GLEventListener, MouseListener, MouseMotionListener  {
	public static final boolean TRACE = true;

	public static final String WINDOW_TITLE = "Mitchell Proulx"; // TODO: change
	public static final int INITIAL_WIDTH = 640;
	public static final int INITIAL_HEIGHT = 600;
	public static final float[][] IDENTITY = { {1, 0, 0}, {0, 1, 0}, {0, 0, 1} };

	// Name of the input file path and scene file
	public static final String INPUT_PATH_NAME = "resources/";
	public static final String INPUT_SCENE_NAME = "in.scn";

	public static ArrayList<Model> allModels = new ArrayList<Model>();
	public static ArrayList<Shape> allShapes = new ArrayList<Shape>();

	public static Shape selected;
	public static boolean objSelected = false, selectedMoved = false, selectedRotate = false, selectedResize = false, objControl = false;
	public static int selectedID = -1;
	public static float xClicked, yClicked, xDragged, yDragged;

	public static void main(String[] args) {
		final JFrame frame = new JFrame(WINDOW_TITLE);

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (TRACE)
					System.out.println("closing window '" + ((JFrame)e.getWindow()).getTitle() + "'");
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
			canvas.addMouseListener((MouseListener)self);
			canvas.addMouseMotionListener((MouseMotionListener)self);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		canvas.setSize(INITIAL_WIDTH, INITIAL_HEIGHT);

		frame.getContentPane().add(canvas);
		frame.pack();
		frame.setVisible(true);

		if (TRACE)
			System.out.println("-> end of main().");
	}
	private static Class<?> self() {
		// This gives us the containing class of a static method 
		return new Object() { }.getClass().getEnclosingClass();
	}
	public void setup(GLCanvas canvas) {
		// Called for one-time setup
		if (TRACE)
			System.out.println("-> executing setup()");

		readScene(INPUT_PATH_NAME + INPUT_SCENE_NAME);
		initModels();
	}
	@Override
	public void init(GLAutoDrawable drawable) {
		// Called when the canvas is (re-)created - use it for initial GL setup
		if (TRACE)
			System.out.println("-> executing init()");

		final GL2 gl = drawable.getGL().getGL2();

		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		gl.glHint(GL2.GL_LINE_SMOOTH_HINT, GL2.GL_NICEST);
		gl.glEnable(GL2.GL_LINE_SMOOTH);
		gl.glEnable(GL2.GL_BLEND);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
	}
	public void initModels() {
		Vertex rotate = null, scale = null, result = null;
		int shapeCount = 0, currShape = 0;

		for (int i = 0; i < allModels.size(); i++) {							// FOR EACH OBJECT
			for (int q = 0; q < allModels.get(i).scenes.size(); q++) {			// FOR EACH SCENE
				currShape = shapeCount;
				allShapes.add(new Shape(allModels.get(i).name));
				allShapes.get(currShape).id = currShape;
				shapeCount++;

				Scene theScene = allModels.get(i).scenes.get(q);
				allShapes.get(currShape).scenes.add(theScene);

				for (int j = 0; j < allModels.get(i).faces.size(); j++) {		// FOR EACH FACE			
					int vertexCount = 0;
					Vertex v1 = null, v2 = null, v3 = null, v4 = null;
					Color theColor = allModels.get(i).faces.get(j).color;

					if(allModels.get(i).faces.get(j).vertex.size() > 2) {									// assuming all objects have 3+ vertices
						if (allModels.get(i).faces.get(j).vertex.size() == 3) {
							/*You may assume that all the polygons in the models are convex, and are either triangles or quadrilaterals*/

							for (int n = 0; n < allModels.get(i).faces.get(j).vertex.size(); n++) {			// FOR EACH VERTEX (3 = TRIANGLE)
								Vertex theVertex = allModels.get(i).faces.get(j).vertex.get(n);				// get the current vertex from corresponding model and face
								rotate = rotateVertex(theVertex, theScene.theta);							// rotate
								scale = scaleVertex(rotate, theScene.sx, theScene.sy);						// scale
								result = translateVertex(scale, theScene.x, theScene.y);					// translate
								allShapes.get(currShape).allVertices.add(result);

								switch(vertexCount){
								case 0:
									v1 = new Vertex(result.x, result.y);
									vertexCount++;
									break;
								case 1:
									v2 = new Vertex(result.x, result.y);
									vertexCount++;
									break;
								case 2:
								default:
									v3 = new Vertex(result.x, result.y);
									Triangle newTri = new Triangle(v1, v2, v3);
									newTri.color = new Color(theColor.r, theColor.g, theColor.b);
									allShapes.get(currShape).triangles.add(newTri);
									v1 = null;
									v2 = null;
									v3 = null;
									vertexCount = 0;
									break;
								}
							}
						}else {		// FOR EACH VERTEX (4+ = QUADS), break them each into triangles

							for (int n = 0; n < allModels.get(i).faces.get(j).vertex.size(); n++) {			

								Vertex theVertex = allModels.get(i).faces.get(j).vertex.get(n);
								rotate = rotateVertex(theVertex, theScene.theta);
								scale = scaleVertex(rotate, theScene.sx, theScene.sy);
								result = translateVertex(scale, theScene.x, theScene.y);
								allShapes.get(currShape).allVertices.add(result);

								// get the next four points of the quad, make 2 triangles
								switch(vertexCount){
								case 0:
									v1 = new Vertex(result.x, result.y);
									vertexCount++;
									break;
								case 1:
									v2 = new Vertex(result.x, result.y);
									vertexCount++;
									break;
								case 2:
									v3 = new Vertex(result.x, result.y);
									vertexCount++;
									break;
								case 3:
								default:
									v4 = new Vertex(result.x, result.y);
									if (v2.x != v4.x && v2.y != v4.y) {

										// split quad into 2 triangles
										Triangle newTri1 = new Triangle(v1, v2, v3);
										newTri1.color = new Color(theColor.r, theColor.g, theColor.b);
										allShapes.get(currShape).triangles.add(newTri1);

										Triangle newTri2 = new Triangle(v1, v4, v3);
										newTri2.color = new Color(theColor.r, theColor.g, theColor.b);
										allShapes.get(currShape).triangles.add(newTri2);
									}
									v1 = null;
									v2 = null;
									v3 = null;
									v4 = null;
									vertexCount = 0;
									break;
								}

							}
						}	// vertices
					}
				}	// faces
			}	// scenes
		} // objects	
		initBoundingBoxes();
	}

	void initBoundingBoxes() {
		// find the min and max vertices in each shape and the middle USING VERTICES of the object
		for (int m = 0; m < allShapes.size(); m++) {
			float minX = 0.0f, minY = 0.0f;
			float maxX = 0.0f, maxY = 0.0f;

			for (int b = 0; b < allShapes.get(m).allVertices.size(); b++) {
				Vertex theVertex = allShapes.get(m).allVertices.get(b);

				if (minX == 0.0f){
					minX = theVertex.x;
				}
				if (minY == 0.0f){
					minY = theVertex.y;
				}
				if (maxX == 0.0f){
					maxX = theVertex.x;
				}
				if (maxY == 0.0f){
					maxY = theVertex.y;
				}
				if (theVertex.x < minX) {
					minX = theVertex.x;
				}
				if (theVertex.x > maxX) {
					maxX = theVertex.x;
				}
				if (theVertex.y < minY) {
					minY = theVertex.y;
				}
				if (theVertex.y > maxY) {
					maxY = theVertex.y;
				}
			}
			allShapes.get(m).middle = new Vertex(((maxX + minX)/2), ((maxY + minY)/2));
			allShapes.get(m).box = new BoundingBox(minX, minY, maxX, maxY);
		}
	}
	void displayModels(GL2 gl) {
		int shapeCount = 0;
		int currShape = 0;

		for (int i = 0; i < allModels.size(); i++) {							// FOR EACH OBJECT
			for (int q = 0; q < allModels.get(i).scenes.size(); q++) {			// FOR EACH SCENE
				currShape = shapeCount;
				shapeCount++;
				Scene theScene = allModels.get(i).scenes.get(q);

				for (int j = 0; j < allModels.get(i).faces.size(); j++) {		// FOR EACH FACE			
					Color theColor = allModels.get(i).faces.get(j).color;
					gl.glColor3f(theColor.r, theColor.g, theColor.b);
					if(allModels.get(i).faces.get(j).vertex.size() > 2) {		// assuming all objects have 3+ vertices
						if (allModels.get(i).faces.get(j).vertex.size() == 3) {

							/* You may assume that all the polygons in the models are convex, and are either triangles or quadrilaterals */
							gl.glBegin(GL2.GL_TRIANGLES);
							for (int n = 0; n < allModels.get(i).faces.get(j).vertex.size(); n++) {			// FOR EACH VERTEX (3 = TRIANGLE)
								Vertex theVertex = allModels.get(i).faces.get(j).vertex.get(n);				// get the current vertex from corresponding model and face
								Vertex rotate = rotateVertex(theVertex, theScene.theta);	
								Vertex scale = scaleVertex(rotate, theScene.sx, theScene.sy);					
								Vertex result = translateVertex(scale, theScene.x, theScene.y);					
								gl.glVertex2f(result.x, result.y);
							}
							gl.glEnd();
						}else {																				// FOR EACH VERTEX (4+ = QUADS)
							gl.glBegin(GL2.GL_QUADS);
							for (int n = 0; n < allModels.get(i).faces.get(j).vertex.size(); n++) {			
								Vertex theVertex = allModels.get(i).faces.get(j).vertex.get(n);
								Vertex rotate = rotateVertex(theVertex, theScene.theta);
								Vertex scale = scaleVertex(rotate, theScene.sx, theScene.sy);
								Vertex result = translateVertex(scale, theScene.x, theScene.y);
								gl.glVertex2f(result.x, result.y);
							}
							gl.glEnd();
						}
					}
				}	// faces
			}	// scenes
		} // objects
	}

	void displayShapes(GL2 gl) {
		// all the objects are displayed
		for (int m = 0; m < allShapes.size(); m++) {
			for (int b = 0; b < allShapes.get(m).triangles.size(); b++) {
				Triangle currTri = allShapes.get(m).triangles.get(b);
				gl.glBegin(GL2.GL_TRIANGLES);
				gl.glColor3f(currTri.color.r, currTri.color.g, currTri.color.b);
				gl.glVertex2f(currTri.v1.x, currTri.v1.y);
				gl.glVertex2f(currTri.v2.x, currTri.v2.y);
				gl.glVertex2f(currTri.v3.x, currTri.v3.y);
				gl.glEnd();
			}
		}
	}

	void displaySelected(GL2 gl) {
		// display the selected object
		if (objSelected == true) {
			if (selected != null) {
				for (int m = 0; m < selected.triangles.size(); m++) {
					Triangle currTri = selected.triangles.get(m);
					gl.glBegin(GL2.GL_TRIANGLES);
					gl.glColor3f(currTri.color.r, currTri.color.g, currTri.color.b);
					gl.glVertex2f(currTri.v1.x, currTri.v1.y);
					gl.glVertex2f(currTri.v2.x, currTri.v2.y);
					gl.glVertex2f(currTri.v3.x, currTri.v3.y);
					gl.glEnd();
				}
			}
		}
	}

	void highlightSelected(GL2 gl){
		float padding = 2.0f;
		float x1, y1, x2, y2, x3, y3, x4, y4;

		if (objSelected == true && selected != null) {
			float minX = selected.box.minX, minY = selected.box.minY;
			float maxX = selected.box.maxX, maxY = selected.box.maxY;

			// add padding to the bounding box
			minX -= padding;
			minY -= padding;
			maxX += padding;
			maxY += padding;

			// draw a dotted line as a border to the objects
			gl.glPointSize(1.0f);
			gl.glBegin(GL2.GL_POINTS);				
			gl.glColor3f(1.0f, 1.0f, 1.0f);
			for (float t = 0.0f; t <= 1.0f; t += 0.03) {
				x1 = minX - minX * t + minX * t;
				y1 = minY - minY * t + maxY * t;
				gl.glVertex2f(x1, y1);

				x2 = minX - minX * t + maxX * t;
				y2 = maxY - maxY * t + maxY * t;
				gl.glVertex2f(x2, y2);

				x3 = maxX - maxX * t + maxX * t;
				y3 = maxY - maxY * t + minY * t;
				gl.glVertex2f(x3, y3);

				x4 = maxX - maxX * t + minX * t;
				y4 = minY - minY * t + minY * t;
				gl.glVertex2f(x4, y4);	
			}
			gl.glEnd();

			Vertex rotate = new Vertex(((maxX + minX)/2), maxY);
			Vertex resize = new Vertex(maxX, maxY);
			selected.setAnchorPoints(rotate, resize);

			// draw a quad around the anchor/control points so we can see them
			float padX = 5;
			float padY = 5;
			// rotate control point
			gl.glBegin(GL2.GL_QUADS);
			gl.glColor3f(0.0f, 1.0f, 0.0f);
			gl.glVertex2f(selected.rotateAnchor.x-padX, selected.rotateAnchor.y-padY);
			gl.glVertex2f(selected.rotateAnchor.x-padX, selected.rotateAnchor.y+padY);
			gl.glVertex2f(selected.rotateAnchor.x+padX, selected.rotateAnchor.y+padY);
			gl.glVertex2f(selected.rotateAnchor.x+padX, selected.rotateAnchor.y-padY);
			gl.glEnd();

			// resize control point
			gl.glBegin(GL2.GL_QUADS);
			gl.glColor3f(1.0f, 0.0f, 1.0f);
			gl.glVertex2f(selected.resizeAnchor.x-padX, selected.resizeAnchor.y-padY);
			gl.glVertex2f(selected.resizeAnchor.x-padX, selected.resizeAnchor.y+padY);
			gl.glVertex2f(selected.resizeAnchor.x+padX, selected.resizeAnchor.y+padY);
			gl.glVertex2f(selected.resizeAnchor.x+padX, selected.resizeAnchor.y-padY);
			gl.glEnd();		
		}
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		// Draws the display
		if (TRACE)
			System.out.println("-> executing display()");
		final GL2 gl = drawable.getGL().getGL2();

		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

		// TODO: Add code here
		displayShapes(gl);
		displaySelected(gl);
		highlightSelected(gl);
	}

	void recalculateBounding() {
		// get the bounding box USING TRIANGLES that make the shapes
		boolean found = false;
		float minX = 0.0f, minY = 0.0f, maxX = 0.0f, maxY = 0.0f;

		for (int i = 0; i < allShapes.size() && !found; i++) {
			if(allShapes.get(i).id == selected.id) {
				found = true;
				for(int n = 0; n < allShapes.get(i).triangles.size(); n++) {
					Triangle theTriangle = allShapes.get(i).triangles.get(n);
					Vertex [] tri = {theTriangle.v1, theTriangle.v2, theTriangle.v3};

					for (Vertex theVertex: tri) {
						if (minX == 0.0f){
							minX = theVertex.x;
						}
						if (minY == 0.0f){
							minY = theVertex.y;
						}
						if (maxX == 0.0f){
							maxX = theVertex.x;
						}
						if (maxY == 0.0f){
							maxY = theVertex.y;
						}
						if (theVertex.x < minX) {
							minX = theVertex.x;
						}
						if (theVertex.x > maxX) {
							maxX = theVertex.x;
						}
						if (theVertex.y < minY) {
							minY = theVertex.y;
						}
						if (theVertex.y > maxY) {
							maxY = theVertex.y;
						}
					}	
				}
				// get the middle of the bounding box
				allShapes.get(i).middle = new Vertex(((maxX + minX)/2), ((maxY + minY)/2));
				selected.middle = new Vertex(((maxX + minX)/2), ((maxY + minY)/2));
				// stores the coordinates
				allShapes.get(i).box = new BoundingBox(minX, minY, maxX, maxY);
				selected.box = new BoundingBox(minX, minY, maxX, maxY);
			}
		}
	}
	void objectResized(float x, float y, MouseEvent e){
		boolean found = false;
		selectedRotate = false;
		selectedMoved = false;
		if (selectedResize == true && selected != null) {
			Vertex middlePoint = selected.middle;
			for (int i = 0; i < allShapes.size() && !found; i++) {
				if(allShapes.get(i).id == selected.id) {
					found = true;
					for(int n = 0; n < allShapes.get(i).triangles.size(); n++) {
						Triangle theTriangle = allShapes.get(i).triangles.get(n); 
						// translate all the triangles of the dragged shape
						Vertex [] scaled = new Vertex[3], translated = new Vertex[3], transBack = new Vertex[3];
						Vertex [] minMaxTrans = new Vertex[2], minMaxScale = new Vertex[2], minMaxBack = new Vertex[2];

						//scale up going to the right, scale down going left
						
						float scaleX = 1.0f;
						float scaleY = 1.0f;
						
						if (x != 0) {
							if (x < 0) {
								scaleX = 1.025f;
							}else{
								scaleX = 0.975f;
							}
						}else{
							scaleX = 1.0f;
						}
						if (y != 0) {
							if (y > 0) {
								scaleY = 1.025f;
							}else{
								scaleY = 0.975f;
							}
						}else{
							scaleY = 1.0f;
						}
						
						// vertex 1 - return object to the origin
						translated[0] = translateVertex(theTriangle.v1, -middlePoint.x, -middlePoint.y);
						scaled[0] = scaleVertex(translated[0], scaleX, scaleY);
						transBack[0] = translateVertex(scaled[0], middlePoint.x, middlePoint.y);
						theTriangle.v1 = new Vertex(transBack[0].x, transBack[0].y);
						selected.triangles.get(n).v1 = new Vertex(transBack[0].x, transBack[0].y);
						// vertex 2 - return object to the origin
						translated[1] = translateVertex(theTriangle.v2, -middlePoint.x, -middlePoint.y);
						scaled[1] = scaleVertex(translated[1], scaleX, scaleY);
						transBack[1] = translateVertex(scaled[1], middlePoint.x, middlePoint.y);
						theTriangle.v2 = new Vertex(transBack[1].x, transBack[1].y);
						selected.triangles.get(n).v2 = new Vertex(transBack[1].x, transBack[1].y);
						// vertex 3 - return object to the origin
						translated[2] = translateVertex(theTriangle.v3, -middlePoint.x, -middlePoint.y);
						scaled[2] = scaleVertex(translated[2], scaleX, scaleY);
						transBack[2] = translateVertex(scaled[2], middlePoint.x, middlePoint.y);
						theTriangle.v3 = new Vertex(transBack[2].x, transBack[2].y);
						selected.triangles.get(n).v3 = new Vertex(transBack[2].x, transBack[2].y);

						// now fit the bounding box to the scaled object
						recalculateBounding();
						Vertex min = new Vertex(selected.box.minX, selected.box.minY);
						minMaxTrans[0] = translateVertex(min, -middlePoint.x, -middlePoint.y);
						minMaxScale[0] = scaleVertex(minMaxTrans[0], scaleX, scaleY);
						minMaxBack[0] = translateVertex(minMaxScale[0], middlePoint.x, middlePoint.y);
						selected.box.minX = minMaxBack[0].x;
						selected.box.minY = minMaxBack[0].y;
						allShapes.get(i).box.minX = minMaxBack[0].x;
						allShapes.get(i).box.minY = minMaxBack[0].y;

						Vertex max = new Vertex(selected.box.maxX, selected.box.maxY);
						minMaxTrans[1] = translateVertex(max, -middlePoint.x, -middlePoint.y);
						minMaxScale[1] = scaleVertex(minMaxTrans[1], scaleX, scaleY);
						minMaxBack[1] = translateVertex(minMaxScale[1], middlePoint.x, middlePoint.y);
						selected.box.maxX = minMaxBack[1].x;
						selected.box.maxY = minMaxBack[1].y;	
						allShapes.get(i).box.maxX = minMaxBack[1].x;
						allShapes.get(i).box.maxY = minMaxBack[1].y;
					}
				}
			}
		}
	}

	void objectRotated(float x, float y, MouseEvent e){
		boolean found = false;
		selectedResize = false;
		selectedMoved = false;

		if (selectedRotate == true && selected != null) {
			Vertex rotatePoint = selected.rotateAnchor;
			Vertex clickPoint = new Vertex(e.getX(),INITIAL_HEIGHT-e.getY());
			Vertex middlePoint = selected.middle;
			//System.out.println("click= [" + clickPoint.x + "," + clickPoint.y + "]");

			float Ax = rotatePoint.x - middlePoint.x;
			float Ay = rotatePoint.y - middlePoint.y;
			float Bx = clickPoint.x - middlePoint.x;
			float By = clickPoint.y - middlePoint.y;
			float dotProd = ((Ax * Bx) + (Ay * By));
			float vectorA = (float) Math.sqrt(((Ax*Ax) + (Ay*Ay)));
			float vectorB = (float) Math.sqrt(((Bx*Bx) + (By*By)));
			float magnitudeAB = vectorA * vectorB;
			float theAngle = (float) Math.toDegrees(Math.acos(dotProd/magnitudeAB));


			// get the difference between the last angle and the current angle
			float angle = theAngle - selected.currAngle;
			selected.currAngle = theAngle;
			if (x > 0 || y > 0) {
				angle = angle*-1;
			}

			for (int i = 0; i < allShapes.size() && !found; i++) {
				if(allShapes.get(i).id == selected.id) {
					found = true;
					for(int n = 0; n < allShapes.get(i).triangles.size(); n++) {
						Triangle theTriangle = allShapes.get(i).triangles.get(n);
						Vertex [] translated = new Vertex[3];	
						Vertex [] rotated = new Vertex[3];
						Vertex [] transBack = new Vertex[3];

						// vertex 1 - return object to the origin
						translated[0] = translateVertex(theTriangle.v1, -middlePoint.x, -middlePoint.y);
						rotated[0] = rotateVertex(translated[0], angle);
						transBack[0] = translateVertex(rotated[0], middlePoint.x, middlePoint.y);
						theTriangle.v1 = new Vertex(transBack[0].x, transBack[0].y);
						selected.triangles.get(n).v1 = new Vertex(transBack[0].x, transBack[0].y);
						// vertex 2 - return object to the origin
						translated[1] = translateVertex(theTriangle.v2, -middlePoint.x, -middlePoint.y);
						rotated[1] = rotateVertex(translated[1], angle);
						transBack[1] = translateVertex(rotated[1], middlePoint.x, middlePoint.y);
						theTriangle.v2 = new Vertex(transBack[1].x, transBack[1].y);
						selected.triangles.get(n).v2 = new Vertex(transBack[1].x, transBack[1].y);
						// vertex 3 - return object to the origin
						translated[2] = translateVertex(theTriangle.v3, -middlePoint.x, -middlePoint.y);
						rotated[2] = rotateVertex(translated[2], angle);
						transBack[2] = translateVertex(rotated[2], middlePoint.x, middlePoint.y);
						theTriangle.v3 = new Vertex(transBack[2].x, transBack[2].y);
						selected.triangles.get(n).v3 = new Vertex(transBack[2].x, transBack[2].y);
					}
				}
			}
			recalculateBounding();
		}
	}

	void objectTranslated(float x, float y, MouseEvent e) {
		boolean found = false;
		selectedRotate = false;
		selectedResize = false;

		if (selectedMoved == true  && selected != null) {
			for (int i = 0; i < allShapes.size() && !found; i++) {
				if(allShapes.get(i).id == selected.id) {
					found = true;
					for(int n = 0; n < allShapes.get(i).triangles.size(); n++) {
						Triangle theTriangle = allShapes.get(i).triangles.get(n);
						int size = allShapes.get(i).triangles.size();

						// translate all the triangles of the dragged shape
						Vertex [] translated = new Vertex[3];
						// vertex 1
						translated[0] = translateVertex(theTriangle.v1, -x, y);
						theTriangle.v1 = new Vertex(translated[0].x, translated[0].y);
						selected.triangles.get(n).v1 = new Vertex(translated[0].x, translated[0].y);
						// vertex 2
						translated[1] = translateVertex(theTriangle.v2, -x, y);
						theTriangle.v2 = new Vertex(translated[1].x, translated[1].y);	
						selected.triangles.get(n).v2 = new Vertex(translated[1].x, translated[1].y);
						// vertex 3
						translated[2] = translateVertex(theTriangle.v3, -x, y);
						theTriangle.v3 = new Vertex(translated[2].x, translated[2].y);	
						selected.triangles.get(n).v3 = new Vertex(translated[2].x, translated[2].y);

						// move the resize and rotate anchors to follow the translated shape
						Vertex moveResize = new Vertex(selected.resizeAnchor.x, selected.resizeAnchor.y);
						Vertex moveRotate = new Vertex(selected.rotateAnchor.x, selected.rotateAnchor.y);
						selected.resizeAnchor = translateVertex(moveResize, -x, y);
						selected.rotateAnchor = translateVertex(moveRotate, -x, y);
						allShapes.get(i).resizeAnchor = translateVertex(moveResize, -x, y);
						allShapes.get(i).rotateAnchor = translateVertex(moveRotate, -x, y);

						// move the bounding box to follow the shape
						Vertex min = new Vertex(selected.box.minX, selected.box.minY);
						Vertex mid = new Vertex(selected.middle.x, selected.middle.y);
						Vertex max = new Vertex(selected.box.maxX, selected.box.maxY);
						Vertex transMin = translateVertex(min, -x/size, y/size);
						Vertex transMid = translateVertex(mid, -x/size, y/size);
						Vertex transMax = translateVertex(max, -x/size, y/size);
						selected.box.minX = transMin.x;
						selected.box.minY = transMin.y;
						selected.middle = new Vertex(transMid.x, transMid.y);
						selected.box.maxX = transMax.x;
						selected.box.maxY = transMax.y;				
						allShapes.get(i).box.minX = transMin.x;
						allShapes.get(i).box.minY = transMin.y;
						allShapes.get(i).box.maxX = transMax.x;
						allShapes.get(i).box.maxY = transMax.y;
						allShapes.get(i).middle = new Vertex(transMid.x, transMid.y);
					}
				}
			}
		}	
	}

	void objectDragged(float x, float y, MouseEvent e) {
		if (selected != null && objControl == false && selectedMoved == true) {
			objectTranslated(x, y, e);
		}
		if (selected != null && objControl == true && selectedRotate == true) {
			objectRotated(x, y, e);
		}
		if (selected != null && objControl == true && selectedResize == true) {
			objectResized(x, y, e);
		}
	}
	boolean checkControl(Vertex min, Vertex max, Vertex point) {		// check to see if the mouse clicked in the are of the control point 
		boolean result = false;
		Vertex v1 = new Vertex(min.x, min.y);		// minX, minY == BOTTOM LEFT
		Vertex v2 = new Vertex(min.x, max.y);		// minX, maxY == TOP LEFT
		Vertex v3 = new Vertex(max.x, max.y);		// maxX, maxY == TOP RIGHT
		Vertex v4 = new Vertex(max.x, min.y);		// maxX, minY == BOTTOM RIGHT
		result = (pointInTriangle(point, v1, v3, v2) || pointInTriangle(point, v1, v3, v4));
		return result;
	}

	boolean clickInControlPoint(float x, float y, MouseEvent e) {		// check to see if click is on a control point, and which one it is
		float newY = INITIAL_HEIGHT - y;	// translate y from java to openGL
		Vertex thePoint = new Vertex(x, newY);
		float padX = 10;
		float padY = 10;
		boolean result = false, clickRotateP = false, clickResizeP = false;

		if (objSelected == true && selected != null) {
			if (selected.resizeAnchor != null) {
				Vertex minResize = new Vertex((selected.resizeAnchor.x - padX), (selected.resizeAnchor.y - padY));
				Vertex maxResize = new Vertex((selected.resizeAnchor.x + padX), (selected.resizeAnchor.y + padY));
				clickResizeP = checkControl(minResize, maxResize, thePoint);
			}
			if (selected.rotateAnchor != null) {
				Vertex minRotate = new Vertex((selected.rotateAnchor.x - padY), (selected.rotateAnchor.y - padY));
				Vertex maxRotate = new Vertex((selected.rotateAnchor.x + padY), (selected.rotateAnchor.y + padY));
				clickRotateP = checkControl(minRotate, maxRotate, thePoint);
			}
			result = (clickResizeP || clickRotateP);
		}
		if (clickRotateP) {
			selectedRotate = true;
			selectedResize = false;
			selectedMoved = false;
			System.out.println("\n< ROTATE POINT >");
		}
		if (clickResizeP) {
			selectedResize = true;
			selectedRotate = false;
			selectedMoved = false;
			System.out.print("\n< RESIZE POINT >");
		}
		if (!clickResizeP && !clickRotateP) {
			selectedResize = false;
			selectedRotate = false;
			selectedMoved = false;
		}
		return result;
	}

	boolean pointInTriangle(Vertex pt, Vertex v1, Vertex v2, Vertex v3) {
		float x1 = (v2.x - v1.x) * (pt.y - v1.y) - (v2.y - v1.y) * (pt.x - v1.x); 
		float x2 = (v3.x - v2.x) * (pt.y - v2.y) - (v3.y - v2.y) * (pt.x - v2.x); 
		float x3 = (v1.x - v3.x) * (pt.y - v3.y) - (v1.y - v3.y) * (pt.x - v3.x); 
		return (x1 > 0 && x2 > 0 && x3 > 0) || (x1 < 0 && x2 < 0 && x3 < 0);
	}

	Triangle clickInTriangle(float x, float y, MouseEvent e, boolean duplicate) {
		float newY = INITIAL_HEIGHT - y;	// translate y from java to openGL
		Triangle result = null;
		Vertex thePoint = new Vertex(x,newY);
		boolean found = false;

		for (int m = 0; m < allShapes.size() && !found; m++) {
			for (int b = 0; b < allShapes.get(m).triangles.size() && !found; b++) {
				Triangle currTri = allShapes.get(m).triangles.get(b);

				if (pointInTriangle(thePoint, currTri.v1, currTri.v2, currTri.v3)) {
					result = new Triangle(currTri.v1, currTri.v2, currTri.v3);
					selected = allShapes.get(m);
					objSelected = true;
					selectedID = selected.id;
					found = true;
					objControl = false;

					if (duplicate == false) {
						result.num = b;	
						System.out.println("OBJECT <" + allShapes.get(m).name + "> AT = [" + (int)x + ", " + (int)newY + "]");
					}else{
						System.out.println("DUPLICATE??");
						duplicateObject();
					}

				}else{
					if (objSelected == true) {
						if (clickInControlPoint(x, y, e)){
							objControl = true;
							System.out.println("CONTROL POINT AT = [" + (int)x + ", " + (int)newY + "]" );
							found = true;
						}else{
							selectedMoved = false;
							selectedRotate = false;
							selectedResize = false;
							selected = null;
							objSelected = false;
							selectedID = -1;
							objControl = false;
						}
					}else{
						selectedMoved = false;
						selectedRotate = false;
						selectedResize = false;
						selected = null;
						objSelected = false;
						selectedID = -1;
						objControl = false;
					}
				}
			}
		}
		return result;
	}

	void duplicateObject() {
		selectedMoved = false;
		selectedRotate = false;
		selectedResize = false;

		float offsetXY = 5;
		if (selected != null) {
			Shape other = selected;
			Scene theScene = selected.scenes.get(0);
			Shape newDuplicate = new Shape(selected.name);

			newDuplicate.id = allShapes.size();
			newDuplicate.box = new BoundingBox(other.box.minX + offsetXY, other.box.minY + offsetXY, other.box.maxX + offsetXY, other.box.maxY + offsetXY);
			newDuplicate.middle = new Vertex(other.middle.x + offsetXY, other.middle.y + offsetXY);
			newDuplicate.resizeAnchor = new Vertex(other.resizeAnchor.x + offsetXY, other.resizeAnchor.y + offsetXY);
			newDuplicate.rotateAnchor = new Vertex(other.rotateAnchor.x + offsetXY, other.rotateAnchor.y + offsetXY);
			newDuplicate.scenes.add(new Scene(theScene.x, theScene.y, theScene.theta, theScene.sx, theScene.sy));
			// triangles
			for (int n = 0; n < other.triangles.size(); n++) {
				Triangle tempTri = other.triangles.get(n);
				Color tempColor = other.triangles.get(n).color;

				Vertex v1 = new Vertex(tempTri.v1.x + offsetXY, tempTri.v1.y + offsetXY);
				Vertex v2 = new Vertex(tempTri.v2.x + offsetXY, tempTri.v2.y + offsetXY);
				Vertex v3 = new Vertex(tempTri.v3.x + offsetXY, tempTri.v3.y + offsetXY);
				Color c = new Color(tempColor.r, tempColor.g, tempColor.b);
				newDuplicate.triangles.add(new Triangle(v1, v2, v3, c));
			}
			// vertices
			for (int j = 0; j < other.allVertices.size(); j++) {
				newDuplicate.allVertices.add(new Vertex(other.allVertices.get(j).x, other.allVertices.get(j).y));
			}
			allShapes.add(newDuplicate);
			selected = newDuplicate;
			objSelected = true;
			selectedID = selected.id;
		}
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
		float[][] thisVertex = { {v.x, 0, 0}, 
				{v.y, 0, 0},
				{  1, 0, 0}};
		float [][] scaleIdentity = multiply(IDENTITY, scale);
		float [][] result = multiply(scaleIdentity, thisVertex);
		return new Vertex(result[0][0], result[1][0]);
	}

	public float[][] multiply(float[][] a, float[][] b) {
		float[][] result = new float[3][3];

		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++)
				for (int k = 0; k < 3; k++)
					result[i][j] += a[i][k] * b[k][j];
		return result;
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
		// Note: glViewport(x, y, width, height) has already been called so don't bother if that's what you want
		if (TRACE)
			System.out.println("-> executing reshape(" + x + ", " + y + ", " + width + ", " + height + ")");

		final GL2 gl = drawable.getGL().getGL2();

		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glOrthof(0.0f, width, 0.0f, height, 0.0f, 1.0f);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
	}

	boolean pressed = false;

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub

		xClicked = xDragged;
		yClicked = yDragged;
		xDragged = e.getX();
		yDragged = e.getY();

		if (objSelected) {
			selectedMoved = true;
			objectDragged(xClicked-xDragged, yClicked-yDragged, e);	
		}
		((GLCanvas)e.getSource()).repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// You probably don't need this one
	}
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		System.out.println("Mouse clicked on (" + e.getX() + "," + e.getY() + ")");
		xClicked = e.getX();
		yClicked = e.getY();
		xDragged = xClicked;
		yDragged = yClicked;
		boolean duplicate = false;
		selectedMoved = false;
		selectedRotate = false;
		selectedResize = false;
		objControl = false;

		if (e.getClickCount() == 2 && !e.isConsumed()) {
			e.consume();
			//handle double click event.
			duplicate = true;
		}
		Triangle v = clickInTriangle(e.getX(), e.getY(), e, duplicate);
		if (v == null && !duplicate) {
			//System.out.println("NOT FOUND!");
		}
		((GLCanvas)e.getSource()).repaint();
	}
	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		System.out.println("Mouse pressed on (" + e.getX() + "," + e.getY() + ")");
		selectedMoved = false;
		selectedRotate = false;
		selectedResize = false;
		objControl = false;
		pressed = true;
		if (objSelected) {
			Triangle v = clickInTriangle(e.getX(), e.getY(), e, false);
			if (v == null) {
				//System.out.println("NOT FOUND!");
			}
		}
		((GLCanvas)e.getSource()).repaint();	
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		System.out.println("Mouse released on (" + e.getX() + "," + e.getY() + ")");
		selectedMoved = false;
		selectedRotate = false;
		selectedResize = false;
		objControl = false;
		((GLCanvas)e.getSource()).repaint();
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// You probably don't need this one
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// You probably don't need this one
	}

	private Model readModel(String filename) {
		// Use as you like
		BufferedReader input;
		String line;
		String[] tokens;
		float[] vertex;
		float[] colour;
		int[] face;

		int currentColourIndex = 0;
		Model model = new Model();
		ArrayList<Vertex> readVertices = new ArrayList<Vertex>();
		ArrayList<Color> readColors = new ArrayList<Color>();

		// these are for error checking (which you don't need to do)
		int lineCount = 0;
		int vertexCount = 0, colourCount = 0, faceCount = 0;

		try {
			input = new BufferedReader(new FileReader(filename));

			line = input.readLine();
			while (line != null) {
				lineCount++;
				tokens = line.split("\\s+");

				if (tokens[0].equals("v")) {
					assert tokens.length == 3 : "Invalid vertex specification (line " + lineCount + "): " + line;
					assert colourCount == 0 && faceCount == 0 && currentColourIndex == 0 : "Unexpected (late) vertex (line " + lineCount + "): " + line;

					vertex = new float[2];
					try {
						vertex[0] = Float.parseFloat(tokens[1]);
						vertex[1] = Float.parseFloat(tokens[2]);
					} catch (NumberFormatException nfe) {
						assert false : "Invalid vertex coordinate (line " + lineCount + "): " + line;
					}

					// TODO: process vertex array 
					System.out.printf("vertex %d: (%f, %f)\n", vertexCount + 1, vertex[0], vertex[1]);
					// store a new vertex for the model
					readVertices.add(new Vertex(vertexCount, vertex[0], vertex[1]));

					vertexCount++;
				} else if (tokens[0].equals("Kd")) {
					assert tokens.length == 4 : "Invalid colour specification (line " + lineCount + "): " + line;
					assert faceCount == 0 && currentColourIndex == 0 : "Unexpected (late) colour (line " + lineCount + "): " + line;

					colour = new float[3];
					try {
						colour[0] = Float.parseFloat(tokens[1]);
						colour[1] = Float.parseFloat(tokens[2]);
						colour[2] = Float.parseFloat(tokens[3]);
					} catch (NumberFormatException nfe) {
						assert false : "Invalid colour value (line " + lineCount + "): " + line;
					}
					for (float colourValue: colour) {
						assert colourValue >= 0.0f && colourValue <= 1.0f : "Colour value out of range (line " + lineCount + "): " + line;
					}

					// TODO: process colour array
					System.out.printf("colour %d: (%f %f %f)\n", colourCount + 1, colour[0], colour[1], colour[2]);
					// stores colors for the model
					readColors.add(new Color(colourCount, colour[0], colour[1], colour[2]));

					colourCount++;
				} else if (tokens[0].equals("usemtl")) {
					assert tokens.length == 2 : "Invalid material selection (line " + lineCount + "): " + line;

					try {
						currentColourIndex = Integer.parseInt(tokens[1]);
					} catch (NumberFormatException nfe) {
						assert false : "Invalid material index (line " + lineCount + "): " + line;
					}
					assert currentColourIndex >= 1 && currentColourIndex <= colourCount : "Material index out of range (line " + lineCount + "): " + line;

				} else if (tokens[0].equals("f")) {
					assert tokens.length > 1 : "Invalid face specification (line " + lineCount + "): " + line;

					face = new int[tokens.length - 1];
					try {
						for (int i = 1; i < tokens.length; i++) {
							face[i - 1] = Integer.parseInt(tokens[i]);
						}
					} catch (NumberFormatException nfe) {
						assert false : "Invalid vertex index (line " + lineCount + "): " + line;
					}
					for (int index: face) {
						assert index >= 1 && index <= vertexCount : "Vertex index out of range (line " + lineCount + "): " + line;
					}

					// TODO: process face array (uses colour @ currentColourIndex, or white if it is 0) 
					System.out.printf("face %d: [ ", faceCount + 1);

					// create the face object with its [number, currentColorIndex and color]
					model.faces.add(new Face(faceCount, currentColourIndex-1, readColors.get(currentColourIndex-1)));

					for (int index: face) {
						System.out.printf("%d ", index);

						// put the correct vertices in the model face
						model.faces.get(faceCount).vertex.add(readVertices.get(index-1));
					}
					System.out.printf("] using material %d\n", currentColourIndex);

					faceCount++;
				} else {
					assert false : "Invalid token at start of line (line " + lineCount + "): " + line;
				}

				line = input.readLine();
			}

			input.close();
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			assert false : "Error reading input file " + filename;
			return null;
		}
		allModels.add(model);
		return model;
	}

	private void readScene(String filename) {
		// Use as you like
		BufferedReader input;
		String line;
		String[] tokens;
		String modelFilename = null;
		Model model = null;

		int sceneCount = 0;

		// these are for error checking (which you don't need to do)
		int lineCount = 0;

		try {
			input = new BufferedReader(new FileReader(filename));

			line = input.readLine();
			while (line != null) {
				lineCount++;

				if (line.length() == 0) {
					modelFilename = null;

				} else if (modelFilename == null) {
					modelFilename = line;
					System.out.println("*** Reading model " + modelFilename);
					model = readModel(INPUT_PATH_NAME + modelFilename);

					if (model == null) {
						modelFilename = null;
					} else {
						modelFilename = line;
						// TODO: You may want to do something with "model" here 
						model.name = modelFilename;
					}

				} else {
					tokens = line.split("\\s+");

					if (tokens.length != 5) {
						assert false : "Invalid instance line (line " + lineCount + "): " + line;
					} else {
						try {
							int[] sceneData = new int[tokens.length];
							for (int i = 0; i < tokens.length; i++) {
								sceneData[i] = Integer.parseInt(tokens[i]);
							}

							if (model == null) {
								assert false : "Instance without model (line " + lineCount + "): " + line;
							} else {	

								// TODO: process scene data for current model [x y rotation scaleX scaleY]
								model.scenes.add(new Scene(sceneData[0], sceneData[1], sceneData[2], sceneData[3], sceneData[4]));
								System.out.println("* Adding instance at (" + sceneData[0] + "," + sceneData[1] + ") rotation " + sceneData[2] + " scale [" + sceneData[3] + " " + sceneData[4] + "]");

								sceneCount++;
								System.out.println("Scene= " + sceneCount);
							}
						} catch (NumberFormatException nfe) {
							assert false : "Invalid instance line (line " + lineCount + "): " + line;
						}
					}
				}
				line = input.readLine();
			}

			input.close();
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			assert false : "Error reading input file " + filename;
		}
	}

	// TODO: You can fill this in or change the return value from readModel()
	class Model {
		ArrayList<Face> faces;
		String name;
		ArrayList<Scene> scenes;
		BoundingBox box;
		Model() {
			this.faces = new ArrayList<Face>();
			this.scenes = new ArrayList<Scene>();
			this.name = "";
			this.box = null;
		}		
	}

	class Vertex {
		float x, y;
		int num;
		Vertex(int num, float x, float y) {
			this.num = num;
			this.x = x;
			this.y = y;
		}
		Vertex(float x, float y) {
			this.num = 0;
			this.x = x;
			this.y = y;
		}
	}
	class Color {
		int num;
		float r, g, b;
		Color(int num, float r, float g, float b) {
			this.num = num;
			this.r = r;
			this.g = g;
			this.b = b;
		}
		Color(float r, float g, float b) {
			this.r = r;
			this.g = g;
			this.b = b;
		}
	}
	class Face {
		ArrayList<Vertex> vertex;
		int num, colorNum;
		Color color;
		Face(int num, int colorNum, Color c) {
			this.num = num;
			this.colorNum = colorNum;
			this.color = c;
			this.vertex = new ArrayList<Vertex>();
		}
	}
	class Scene {
		float x, y, theta, sx, sy;
		Scene(float x, float y, float theta, float sx, float sy){
			this.x = x;
			this.y = y;
			this.theta = theta;
			this.sx = sx;
			this.sy = sy;
		}
	}
	class Shape {
		String name = "";
		int id = -1;
		float currAngle = 0.0f;
		ArrayList<Vertex> allVertices;
		ArrayList<Triangle> triangles;
		ArrayList<Scene> scenes;
		BoundingBox box;
		Vertex rotateAnchor, resizeAnchor, middle;
		Shape(String n) {
			this.name = n;
			this.triangles = new ArrayList<Triangle>();
			this.scenes = new ArrayList<Scene>();
			this.allVertices = new ArrayList<Vertex>();
			this.box = null;
			rotateAnchor = null;
			resizeAnchor = null;
		}
		void setAnchorPoints(Vertex rotate, Vertex resize) {
			this.rotateAnchor = rotate;
			this.resizeAnchor = resize;
		}
	}
	class Triangle {
		Vertex v1, v2, v3;
		int num;
		Color color;
		Triangle(Vertex v1,Vertex v2,Vertex v3) {
			this.num = 0;
			this.v1 = v1;
			this.v2 = v2;
			this.v3 = v3;
			color = null;
		}
		Triangle(Vertex v1,Vertex v2,Vertex v3, Color c) {
			this.num = 0;
			this.v1 = v1;
			this.v2 = v2;
			this.v3 = v3;
			color = c;
		}
	}
	class BoundingBox {
		float minX, minY, maxX, maxY;
		BoundingBox(float m1, float m2, float m3, float m4) {
			this.minX = m1;
			this.minY = m2;
			this.maxX = m3;
			this.maxY = m4;
		}
	}
}
