import com.jogamp.nativewindow.WindowClosingProtocol;
import com.jogamp.opengl.*;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.newt.opengl.GLWindow;

import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.Iterator;
//import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

public class Game {
	// Set this to true to make the game loop exit.
	private static boolean shouldExit;

	// The previous frame's keyboard state.
	private static boolean kbPrevState[] = new boolean[256];

	// The current frame's keyboard state.
	private static boolean kbState[] = new boolean[256];

	// Size of the window
	private static int[] windowSize = new int[] { 500, 600 };

	private static int wallThickness = 100;

	private static int[] mapBounds = new int[] { 4096, 4096 };

	float[] bgColor = { 0, 0, 0 };

	Entity camera;

	private double grav = 0;// .3;

	public PolygonI drawing;

	public Line cutLine;
	public Point2D.Double lastCutPoint;

	int tempEntityCount = 0;

	int penalties = 0;
	int levelsCleared = 0;

	int highScore = 0;
	int score = 0;

	double baseEVariance = 80;
	double baseEHeight = 20;

	double maxPaddleAngle = 90; // in degrees

	double paddleWidth = 120;

	boolean stageOver = false;
	boolean loss = false;

	LinkedList<Entity> entities;
	Entity paddle, wedge;

	public void buildLevel(int level) {

		bgColor[0] = 0;
		bgColor[1] = 0;
		bgColor[2] = 0;

		entities = new LinkedList<Entity>();
		stageOver = false;
		// entities.add(camera);

		LinkedList<Point2D> tr = new LinkedList<Point2D>();
		// tr.add(new Point2D.Double(200, 200));
		// tr.add(new Point2D.Double(220, 220));
		// tr.add(new Point2D.Double(200, 240));
		// tr.add(new Point2D.Double(180, 220));
		tr.add(new Point2D.Double(200, 200));
		tr.add(new Point2D.Double(207, 215));
		tr.add(new Point2D.Double(193, 215));
		wedge = new Entity(tr);// Entity(0, 15, 20, 20, 0, 0);
		wedge.solid = true;
		wedge.moving = true;
		wedge.damped = false;
		wedge.gravity = grav;
		wedge.velocity = new Vector(2, -7);
		wedge.name = "wedge";
		wedge.collisionCategory = 1;
		wedge.flatspeed = true;
		wedge.baseSpeed = 5;
		wedge.poly.filled = true;
		wedge.poly.color[0] = .9f;
		wedge.poly.color[1] = .2f;
		wedge.poly.color[2] = .1f;
		entities.add(wedge);

		Random r = new Random();

		LinkedList<Point2D> eplist = new LinkedList<Point2D>();
		eplist.add(new Point2D.Double(2, 2));
		eplist.add(new Point2D.Double(windowSize[0] - 4, 2));
		eplist.add(new Point2D.Double(windowSize[0] - 4, baseEHeight + 10 * level));
		// add jank points
		double ewidth = windowSize[0] - 4;
		int spikes = levelsCleared * 2;
		double separation = ewidth / (spikes);
		for (int i = 1; i <= spikes; i++) {

			double x = ewidth - (separation * i);
			double y = baseEHeight + 5 * level + (r.nextDouble() * baseEVariance);
			eplist.add(new Point2D.Double(x, y));
		}
		eplist.add(new Point2D.Double(2, baseEHeight + 10 * level));

		Entity enemyblock = new Entity(eplist);// (0+2, 0, windowSize[0]-4,
												// 50+10*level, 0, 0);
		enemyblock.solid = true;
		enemyblock.moving = true;
		enemyblock.damped = false;
		enemyblock.gravity = grav;
		// enemyblock.velocity = new Vector(0, 0);
		enemyblock.collisionCategory = 0;
		enemyblock.breakable = true;
		enemyblock.name = "enemy";
		// enemyblock.poly.filled = true;
		entities.add(enemyblock);

		Entity floor = new Entity(-wallThickness, windowSize[1], wallThickness * 2 + windowSize[0], wallThickness, 0,
				0);
		floor.solid = true;
		floor.moving = false;
		floor.name = "floor";
		floor.density = Double.POSITIVE_INFINITY;
		floor.gravity = 0;
		floor.collisionCategory = 3;// loss if colliding with a 1, delete any 0s
		// floor.elasticity = 1;
		entities.add(floor);

		Entity lwall = new Entity(-wallThickness, -wallThickness, wallThickness, wallThickness * 2 + windowSize[1], 0,
				0);
		lwall.solid = true;
		lwall.moving = false;
		lwall.name = "lwall";
		lwall.density = Double.POSITIVE_INFINITY;
		lwall.gravity = 0;
		lwall.collisionCategory = 4; // 4 is nothing special
		entities.add(lwall);

		Entity rwall = new Entity(windowSize[0], -wallThickness, wallThickness, wallThickness * 2 + windowSize[1], 0,
				0);
		rwall.solid = true;
		rwall.moving = false;
		rwall.name = "rwall";
		rwall.density = Double.POSITIVE_INFINITY;
		rwall.gravity = 0;
		rwall.collisionCategory = 4; // 4 is nothing special
		entities.add(rwall);

		Entity ceil = new Entity(-wallThickness, -wallThickness, wallThickness * 2 + windowSize[0], wallThickness, 0,
				0);
		ceil.solid = true;
		ceil.moving = false;
		ceil.name = "ceil";
		ceil.density = Double.POSITIVE_INFINITY;
		ceil.gravity = 0;
		ceil.collisionCategory = 2;// victory if colliding with 1, regular with
									// anything else
		entities.add(ceil);

		paddle = new Entity(100, windowSize[1] - 30, (int) paddleWidth, 30, 0, 0);
		paddle.solid = true;
		paddle.moving = false;
		paddle.name = "ceil";
		paddle.density = Double.POSITIVE_INFINITY;
		paddle.gravity = 0;
		paddle.baseSpeed = 5;
		paddle.collisionCategory = 5;// lost points on collision with 0. delete
										// the 0
		paddle.poly.filled = true;
		// paddle.poly.color[0] = .5f;
		// paddle.poly.color[1] = .7f;
		// paddle.poly.color[2] = .5f;
		entities.add(paddle);
	}

	public void run() {
		GLProfile gl2Profile;

		try {
			// Make sure we have a recent version of OpenGL
			gl2Profile = GLProfile.get(GLProfile.GL2);
		} catch (GLException ex) {
			System.out.println("OpenGL max supported version is too low.");
			System.exit(1);
			return;
		}

		// Create the window and OpenGL context.
		GLWindow window = GLWindow.create(new GLCapabilities(gl2Profile));
		window.setSize(windowSize[0], windowSize[1]);
		window.setTitle("Java Template");
		window.setVisible(true);
		window.setDefaultCloseOperation(WindowClosingProtocol.WindowClosingMode.DISPOSE_ON_CLOSE);
		window.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent keyEvent) {
				if (keyEvent.isAutoRepeat()) {
					return;
				}
				kbState[keyEvent.getKeyCode()] = true;
			}

			@Override
			public void keyReleased(KeyEvent keyEvent) {
				if (keyEvent.isAutoRepeat()) {
					return;
				}
				kbState[keyEvent.getKeyCode()] = false;
			}
		});

		camera = new Entity(0, 0, windowSize[0], windowSize[1], .15f, 2f);
		camera.minAcc = 1f;
		camera.visible = false;
		camera.moving = true;
		camera.solid = false;
		camera.gravity = 0;
		camera.damped = true;
		camera.name = "camera";

		drawing = new PolygonI();
		cutLine = new Line();
		lastCutPoint = new Point2D.Double(0, 0);

		buildLevel(levelsCleared);

		window.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				short button = e.getButton();
				if (button == 1) {
					drawing.addPoint(e.getX() + camera.getBounds2D().getX(), e.getY() + camera.getBounds2D().getY());
				}
				if (button == 3) {
					cutLine = new Line(lastCutPoint.x, lastCutPoint.y, e.getX() + camera.getBounds2D().getX(),
							e.getY() + camera.getBounds2D().getY());
					lastCutPoint.x = e.getX() + camera.getBounds2D().getX();
					lastCutPoint.y = e.getY() + camera.getBounds2D().getY();
				}
				if (button == 2) {
					System.out.println("mouse x: " + (e.getX() + camera.getBounds2D().getX()));
					System.out.println("mouse y: " + (e.getY() + camera.getBounds2D().getY()));
				}

			}

			@Override
			public void mouseDragged(MouseEvent arg0) {

			}

			@Override
			public void mouseEntered(MouseEvent arg0) {

			}

			@Override
			public void mouseExited(MouseEvent arg0) {

			}

			@Override
			public void mouseMoved(MouseEvent arg0) {

			}

			@Override
			public void mousePressed(MouseEvent arg0) {

			}

			@Override
			public void mouseReleased(MouseEvent arg0) {

			}

			@Override
			public void mouseWheelMoved(MouseEvent arg0) {

			}
		});

		// setup fps counter
		window.setUpdateFPSFrames(3, null);

		// Setup OpenGL state.
		window.getContext().makeCurrent();
		GL2 gl = window.getGL().getGL2();
		gl.glViewport(0, 0, windowSize[0], windowSize[1]);
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glOrtho(0, windowSize[0], windowSize[1], 0, 0, 100);
		gl.glEnable(GL2.GL_TEXTURE_2D);
		gl.glEnable(GL2.GL_BLEND);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		
		/*try {
			Sound.playClipLoop(new File("sound/bg.wav"));
		} catch (Exception e) {
			System.out.println("Trouble playing sound");
			e.printStackTrace();
		}*/

		// Game initialization goes here.

		// LinkedList<Point2D> tp = new LinkedList<Point2D>();
		// tp.add(new Point2D.Double(100, 0));
		// tp.add(new Point2D.Double(200, 50));
		// tp.add(new Point2D.Double(100, 100));
		// tp.add(new Point2D.Double(0, 50));
		// Entity testP = new Entity(tp);
		// testP.solid = true;
		// testP.moving = true;
		// testP.damped = false;
		// testP.gravity = grav;
		// testP.velocity = new Vector(.25, 0);
		// testP.breakable = true;
		// testP.name = "testP";
		// entities.add(testP);

		boolean spacepressed = false;
		boolean steppressed = false;
		boolean cutpressed = false;
		boolean paused = true;
		boolean framestep = false;
		
		// The game loop
		long lastFrameNS;
		long curFrameNS = System.nanoTime();
		while (!shouldExit) {
			System.arraycopy(kbState, 0, kbPrevState, 0, kbState.length);
			lastFrameNS = curFrameNS;
			curFrameNS = System.nanoTime();
			long deltaTimeMS = (curFrameNS - lastFrameNS) / 1000000;

			// Actually, this runs the entire OS message pump.
			window.display();

			if (!window.isVisible()) {
				shouldExit = true;
				break;
			}
			camera.update(deltaTimeMS);

			// System.out.println("x: "+ testP.xAcc + ", y: " + testP.yAcc);

			// Game logic goes here.
			if (kbState[KeyEvent.VK_ESCAPE]) {
				shouldExit = true;
			}
			if (kbState[KeyEvent.VK_UP]) {
				// wedge.poly.translate(0, -1);
				paddle.poly.translate(0, -paddle.baseSpeed);
			}
			if (kbState[KeyEvent.VK_DOWN]) {
				// wedge.poly.translate(0, 1);
				paddle.poly.translate(0, paddle.baseSpeed);
			}

			if (kbState[KeyEvent.VK_LEFT]) {
				// wedge.poly.translate(-1, 0);
				paddle.poly.translate(-paddle.baseSpeed, 0);
			}
			if (kbState[KeyEvent.VK_RIGHT]) {
				// wedge.poly.translate(1, 0);
				paddle.poly.translate(paddle.baseSpeed, 0);
			}
			if (kbState[KeyEvent.VK_W]) {
				camera.nudge(0, -1);
			}
			if (kbState[KeyEvent.VK_S]) {
				camera.nudge(0, 1);
			}

			if (kbState[KeyEvent.VK_A]) {
				camera.nudge(-1, 0);
			}
			if (kbState[KeyEvent.VK_D]) {
				camera.nudge(1, 0);
			}
			if (kbState[KeyEvent.VK_U]) {
				wedge.velocity.y -= 1;
			}
			if (kbState[KeyEvent.VK_J]) {
				wedge.velocity.y += 1;
			}

			if (kbState[KeyEvent.VK_H]) {
				wedge.velocity.x -= 1;
			}
			if (kbState[KeyEvent.VK_K]) {
				wedge.velocity.x += 1;
			}
			if (kbState[KeyEvent.VK_R]) {
				levelsCleared = 0;
				buildLevel(levelsCleared);
			}

			if (kbState[KeyEvent.VK_SPACE]) {
				if (!spacepressed) {
					spacepressed = true;
					if (paused) {
						paused = false;
					} else {
						paused = true;
					}
				}
			} else {
				spacepressed = false;
			}
			if (kbState[KeyEvent.VK_PERIOD]) {
				// System.out.println("step");
				if (!steppressed) {
					steppressed = true;
					framestep = true;
				}
			} else {
				steppressed = false;
			}
			if (kbState[KeyEvent.VK_BACK_SPACE]) {
				drawing.reset();
			}
			if (kbState[KeyEvent.VK_ENTER]) {
				LinkedList<Point2D> drawingPoints = new LinkedList<Point2D>();
				for (int i = 0; i < drawing.npoints; i++) {
					drawingPoints.add(new Point2D.Double(drawing.xpoints[i], drawing.ypoints[i]));
				}
				Entity tempEntity = new Entity(drawingPoints);
				tempEntity.solid = true;
				tempEntity.moving = true;
				tempEntity.damped = false;
				tempEntity.gravity = grav;
				tempEntity.velocity = new Vector(0, .25);
				tempEntity.name = "tempEntity #" + tempEntityCount;
				entities.add(tempEntity);
				tempEntityCount++;
				drawing.reset();
			}

			if (kbState[KeyEvent.VK_BACK_SLASH]) {
				// cut everything intersecting the cutline
				if (!cutpressed) {
					LinkedList<Entity> newentities = new LinkedList<Entity>();
					for (Entity e : entities) {
						if (e.getBounds2D().intersectsLine(cutLine)) {
							System.out.println("cut: " + e.name);
							newentities.addAll(e.cut(cutLine));

						}
					}
					System.out.println(newentities);
					entities.addAll(newentities);
					System.out.println("done cutting");
					cutpressed = true;
				}
			} else {
				cutpressed = false;
			}

			/*
			 * if(camera.x < 0){ camera.x = 0; camera.xAcc = 0; }else
			 * if(camera.x > mapBounds[0]-camera.w){ camera.x =
			 * mapBounds[0]-camera.w; camera.xAcc = 0; } if(camera.y < 0){
			 * camera.y = 0; camera.yAcc = 0; }else if(camera.y >
			 * mapBounds[1]-camera.h){ camera.y = mapBounds[1]-camera.h;
			 * camera.yAcc = 0; }//
			 */

			System.out.println();
			gl.glClearColor(bgColor[0], bgColor[1], bgColor[2], 1);
			gl.glClear(GL2.GL_COLOR_BUFFER_BIT);

			// update and clean up entities marked for delete
			Iterator<Entity> it = entities.iterator();
			while (it.hasNext()) {
				Entity e = it.next();
				if (e.markedForDelete) {
					System.out.println("cleaned up an entity");
					it.remove();
				} else {
					if (!paused || framestep) {
						e.physicsResolved = false;
						e.update(deltaTimeMS);
					}
					e.draw(gl, camera);
				}
			}

			int iterations = 1;
			int cuts = 0;
			// int cutcutoff = 10;

			if (!paused || framestep) {
				float mincolor = .1f;
				for (int i = 0; i < bgColor.length; i++) {
					float color = bgColor[i];
					if (color < mincolor) {
						bgColor[i] = 0;
					} else {
						bgColor[i] = bgColor[i] * .97f;
					}
				}
				cuts = 0;
				for (int i = 0; i < iterations; i++) {
					LinkedList<Entity> newEntities = new LinkedList<Entity>();
					for (Entity e1 : entities) {
						if (!e1.physicsResolved) {
							for (Entity e2 : entities) {
								if (e1 != e2 && !e2.physicsResolved && (e1.moving || e2.moving)) {
									if (e1.getBounds2D().intersects(e2.getBounds2D())) {
										Area a1 = new Area(e1.poly);
										Area a2 = new Area(e2.poly);
										a1.intersect(a2);
										if (!a1.isEmpty()) {
											// polygons intersect
											// resolve collision

											if (e1.collisionCategory == 0) {
												if (e2.collisionCategory == 3) {
													e1.markedForDelete = true;
												}
											}
											if (e1.collisionCategory == 1 && e2.collisionCategory == 3) {
												// loss
												System.out.println("loss");
												// score = score + 20 +
												// levelsCleared*10 - penalties;
												loss = true;
												penalties = 0;
												levelsCleared = 0;
												// buildLevel(levelsCleared);
												stageOver = true;
												paused = true;
											} else if (e1.collisionCategory == 1 && e2.collisionCategory == 2) {
												try {
													Sound.playClip(new File("sound/horn.wav"));
													Sound.playClip(new File("sound/yay.wav"));
													Sound.playClip(new File("sound/clap.wav"));
												} catch (Exception e) {
													System.out.println("Trouble playing level up sound");
													e.printStackTrace();
												}
												// win
												System.out.println("win");
												// win = true;
												score = score + 10 + levelsCleared * 5 - penalties;
												penalties = 0;
												levelsCleared++;
												// buildLevel(levelsCleared);
												stageOver = true;
												paused = true;
											} else if (e1.collisionCategory == 5 && e2.collisionCategory == 0) {
												penalties++;
												e2.markedForDelete = true;
												bgColor[0] = 1f;
											} else if(e1.collisionCategory == 4 && e2.collisionCategory == 1){
												try {
													Sound.playClip(new File("sound/collide.wav"));
												} catch (Exception e) {
													System.out.println("Trouble playing collide with wall sound");
													e.printStackTrace();
												}
                        					} 
											boolean doCut = (!e1.markedForDelete && !e2.markedForDelete
													&& e1.collisionCategory == 1 && e2.collisionCategory == 0);
											
											if (doCut) {
												try {
													Sound.playClip(new File("sound/glass_break.wav"));
												} catch (Exception e) {
													System.out.println("Trouble playing collide with enemy sound");
													e.printStackTrace();
												}
                        					}
											// if(e1.collisionCategory == 1 &&
											// e2.collisionCategory == 5){
											//
											// }else{
											newEntities.addAll(e1.collide(e2, doCut));
											// bgColor[1] = 1f;
											// }

											if (e1.collisionCategory == 5 && e2.collisionCategory == 1) {
												try {
													Sound.playClip(new File("sound/collide.wav"));
												} catch (Exception e) {
													System.out.println("Trouble playing collide with paddle sound");
													e.printStackTrace();
												}
												// double maxPaddleAngleR =
												// Math.toRadians(maxPaddleAngle);
												double paddleMid = paddle.getBounds2D().getX()
														+ paddle.getBounds2D().getWidth() / 2;
												double wedgeMid = wedge.getBounds2D().getX()
														+ wedge.getBounds2D().getWidth() / 2;
												double offset = wedgeMid - paddleMid; // how
																						// far
																						// right
																						// of
																						// center
																						// the
																						// wedge
																						// is
												double percentOffset = offset / paddle.getBounds2D().getWidth();
												wedge.velocity.add(5 * percentOffset, 0);
												// double adjustedAngle =
												// maxPaddleAngle *
												// percentOffset * 2;
												// double angleR =
												// Math.toRadians(adjustedAngle);
												// Vector wedgeVel = new
												// Vector(0, 1);
												// wedgeVel.rotate(-angleR);
												// wedgeVel.mult(5);
												//
												// e2.velocity = wedgeVel;
												//// e2.velocity.mult(e2.baseSpeed);
												// e2.deltaVelocity.mult(0);
												//// double wedgeBottom =
												// e2.getBounds2D().getY() +
												// e2.getBounds2D().getHeight();
												//// double paddleTop =
												// paddle.getBounds2D().getY();
												//// e2.poly.translate(new
												// Vector(0,
												// paddleTop-wedgeBottom-10));
												//
												// System.out.println("paddle.getBounds2D().getWidth():
												// " +
												// paddle.getBounds2D().getWidth());
												// System.out.println("paddleMid:
												// " + paddleMid);
												// System.out.println("wedgeMid:
												// " + wedgeMid);
												// System.out.println("offset: "
												// + offset);
												// System.out.println("percentOffset:
												// " + percentOffset);
												// System.out.println("adjustedAngle:
												// " + adjustedAngle);
												// System.out.println("wedgeVel:
												// " + wedgeVel);
											}

											System.out.println(e1.name + " + " + e2.name);
											System.out.println(e1 + " + " + e2);
										}
									}
								}
							}
						}
					}
					entities.addAll(newEntities);
				}
				System.out.println("----- physics end -----" + " penalties: " + penalties);
			}
			framestep = false;

			LinkedList<Line> testLines = new LinkedList<Line>();
			Line testLine = new Line((float) wedge.getBounds2D().getCenterX(), (float) wedge.getBounds2D().getCenterY(),
					wedge.velocity);
			testLines.add(testLine);

			// Collision testcol = wedge.getCollisionInfo(testP);
			// System.out.println("collision normal\n x: " + testcol.normal.x +
			// ", y: " + testcol.normal.y );
			// System.out.println("backtrack\n x: " + testcol.backtrack.x + ",
			// y: " + testcol.backtrack.y );
			// System.out.println("loc\n x: " + testcol.loc.x + ", y: " +
			// testcol.loc.y );
			// System.out.println("vertex\n x: " + testcol.vertex.x + ", y: " +
			// testcol.vertex.y );
			// Line normalL = new Line(testcol.loc.getX(), testcol.loc.getY(),
			// testcol.normal);
			// normalL.color[0] = .5f;
			// normalL.color[1] = .5f;
			// normalL.color[2] = 1f;
			// testLines.add(normalL);
			// Line backTrackL = new Line(testcol.vertex.getX(),
			// testcol.vertex.getY(), testcol.backtrack);
			// backTrackL.color[0] = .2f;
			// backTrackL.color[1] = 1f;
			// backTrackL.color[2] = .2f;
			// testLines.add(backTrackL);

			/*
			 * Collision testcol2 = testP.getCollisionInfo2(wedge,
			 * wedge.velocity); System.out.println("collision normal\n x: " +
			 * testcol2.normal.x + ", y: " + testcol2.normal.y );
			 * System.out.println("backtrack\n x: " + testcol2.backtrack.x +
			 * ", y: " + testcol2.backtrack.y ); Line normalL2 = new
			 * Line(testcol2.loc.getX(), testcol2.loc.getY(), testcol2.normal);
			 * normalL2.color[0] = 1f; normalL2.color[1] = .5f;
			 * normalL2.color[2] = 1f; testLines.add(normalL2); Line backTrackL2
			 * = new Line(testcol2.vertex.getX(), testcol2.vertex.getY(),
			 * testcol2.backtrack); backTrackL2.color[0] = 1f;
			 * backTrackL2.color[1] = 1f; backTrackL2.color[2] = .2f;
			 * testLines.add(backTrackL2);//
			 */

			for (Line l : testLines) {
				l.draw(gl, camera);
			}

			drawing.draw(gl, camera);
			cutLine.draw(gl, camera);

			if (score > highScore) {
				highScore = score;
			}
			if (loss) {
				score = 0;
				loss = false;
			}

			window.setTitle("Level: " + (levelsCleared + 1) + " | Score: " + score + " | High Score: " + highScore);
			if (stageOver) {
				buildLevel(levelsCleared);
			}

			// System.out.println(window.getLastFPS());
		}
	}

}
