
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.geom.Point2D.Float;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.jogamp.opengl.GL2;

public class Entity{
	
	public int collisionCategory = 0;
	
	double minArea = 50;
	
	public double gravity = .5f;
	
	
	public float dampening;
	public float minAcc = 0;
	public double elasticity = 1;
	public double density = 2;
	public double fragility = 20;
	
	public float baseSpeed = 5;
	public boolean flatspeed = false;
	
	public float lastx, lasty;
	
	public boolean markedForDelete = false;
	
	public boolean solid = false;
	public boolean moving = false;
	public boolean visible = true;
	public boolean damped = false;
	
	public boolean physicsResolved = false;
	
	public boolean breakable = false;
	
	double cutGapWidth = 2; // desired line length to cut out in pixels
	double slop = .00001;
	
	double cutCutoff = 3;
	double maxangle = .7;
	double maxvariance = .5;
	
	double passoffcut = .9;//how much of the cutlength to put into the branches
	
	//public float speed;
	//public Vector impulse, direction;
	public Vector velocity;
	public Vector deltaVelocity;
	public LinkedList<Vector> impulseLog;
	
	public Polygon2D poly;
	
	public String name = "unnamed";
	
	public Entity(){
		//speed = 0;
		velocity = new Vector(0,0);
		deltaVelocity = new Vector(0,0);
		impulseLog = new LinkedList<Vector>();
//		direction = new Vector(0,0);
//		impulse = new Vector(0,0);
	}
	
	public Entity(LinkedList<Point2D> points){
		this();
		poly = new Polygon2D();
		Iterator<Point2D> it = points.iterator();
		if(it.hasNext()){
			Point2D p = it.next();
			poly.moveTo(p.getX(), p.getY());
			while(it.hasNext()){
				p = it.next();
				poly.lineTo(p.getX(), p.getY());
			}
			poly.closePath();
		}
	}
	
	public Entity(float x, float y, float w, float h){
		this();
		poly = new Polygon2D();
		poly.moveTo(x, y);
		poly.lineTo(x+w, y);
		poly.lineTo(x+w, y+h);
		poly.lineTo(x, y+h);
		poly.closePath();
	}
	
	public Entity(float x, float y, float w, float h, float dampening, float baseSpeed){
		this(x, y, w, h);
		this.dampening = dampening;
		this.baseSpeed = baseSpeed;
	}
	
	
	public void nudge(float xMult, float yMult){
		velocity.x += xMult*baseSpeed;
		velocity.y += yMult*baseSpeed;
		//this.xAcc += xMult*baseSpeed;
		//this.yAcc += yMult*baseSpeed;
	}
	
	public void update(long deltaTimeMS){
		//lastx = x;
		//lasty = y;
		
		if(getArea() < minArea){
			markedForDelete = true;
		}else{
			if(moving){
				//poly.translate((xAcc*deltaTimeMS/16), yAcc*(deltaTimeMS/16));
				velocity.add(deltaVelocity);
				deltaVelocity.mult(0);
				
				if(flatspeed){
					Vector flat = new Vector(velocity.getDirection());
					flat.mult(baseSpeed);
					velocity = flat;
				}
				
				poly.translate(velocity.x, velocity.y);
				//x += xAcc*(deltaTimeMS/16);
				//y += yAcc*(deltaTimeMS/16);
				if(damped){
					velocity.mult(1-dampening);
					//xAcc = xAcc - (dampening*xAcc);//*(deltaTimeMS/16);
					//yAcc = yAcc - (dampening*yAcc);//*(deltaTimeMS/16);
				}
				velocity.y += gravity;
				
				//yAcc += gravity;
//				if(velocity.getMagnitude() < minAcc){
//					velocity.mult(0);
//				}
				/*if(Math.abs(xAcc) < minAcc){
					xAcc = 0;
				}
				if(Math.abs(yAcc) < minAcc){
					yAcc = 0;
				}//*/
			}else{
				velocity.mult(0);
			}
		}
		
		
	}
	
	//limit one cut per 
	public LinkedList<Entity> cut(final Line cutl){
		
		LinkedList<Point2D> intersections = new LinkedList<Point2D>();
		for(Line l : poly.getLines()){
			Point2D.Double intersection = cutl.intersection(l);
//			System.out.println("intersects: " + cutl.intersectsLine(l));
//			System.out.println("p1dist: " + l.ptSegDist(cutl.getP1()));
//			System.out.println("p2dist: " + l.ptSegDist(cutl.getP2()));
			if(cutl.intersectsLine(l) ){
				intersections.add(intersection);
			}else if(l.ptSegDist(cutl.getP1()) < slop){
				intersections.add(cutl.getP1());
			}else if(l.ptSegDist(cutl.getP2()) < slop){
				intersections.add(cutl.getP2());
			}
		}
		Collections.sort(intersections, new Comparator<Point2D>(){
			@Override
			public int compare(Point2D o1, Point2D o2) {
				
				return (int) Math.signum(cutl.getP1().distance(o1) - cutl.getP1().distance(o2));
			}
		});
		
//		System.out.println(intersections);
		
		// has the whole list of intersections, but only uses the first two points
		// so only one cut possible
		boolean in2 = false;
		
		LinkedList<Point2D> cutpoints = new LinkedList<Point2D>();
		if(intersections.size() > 0){
			cutpoints.add(intersections.get(0));
			if(intersections.size() > 1){
				cutpoints.add(intersections.get(1));
				Point2D zero = cutpoints.get(0);
				Point2D one = cutpoints.get(1);
				Point2D midpoint = new Point2D.Double((zero.getX()+one.getX())/2, (zero.getY()+one.getY())/2);
				if(!poly.contains(midpoint)){
					cutpoints.clear();
				}
			}
		}
		
		LinkedList<Entity> newshapes = new LinkedList<Entity>();
		
		LinkedList<Point2D> points1 = new LinkedList<Point2D>();
		LinkedList<Point2D> points2 = new LinkedList<Point2D>();
		

		if(cutpoints.size() >= 1){
			
		}
		if(cutpoints.size() > 1){
			for(Line l : poly.getLines()){
				if(!in2){
					points1.add(l.getP1());
				}else{
					points2.add(l.getP1());
				}
				for(Point2D cp : cutpoints){
					if(l.ptSegDist(cp)<slop){ //found line intersecting cutstart
						
						
						Vector cutLineVector = new Vector(l); 
						Vector gapHalfWidth = cutLineVector.getDirection(); //get direction the line being cut is pointing
						gapHalfWidth.mult(cutGapWidth/2);
						
						if(!in2){
							in2 = true;
//							cutstart = cp;
						}else{
							in2 = false;
							gapHalfWidth.mult(-1);
						}
						
						Point2D p1point = new Point2D.Double(cp.getX()-gapHalfWidth.x, cp.getY()-gapHalfWidth.y);
						Point2D p2point = new Point2D.Double(cp.getX()+gapHalfWidth.x, cp.getY()+gapHalfWidth.y);

						points1.add(p1point);
						points2.add(p2point);
						break;
						
					}
				}
				
			}
		}else if(cutpoints.size() == 1){
			for(Line l : poly.getLines()){
				points1.add(l.getP1());
				for(Point2D cp : cutpoints){
					if(l.ptSegDist(cp)<slop){ //found line intersecting cutstart
						//Vector pLine = new Vector(l.getP1().getX(), l.getP1().getY(), cp.x, cp.y);
						Vector cutLineVector = new Vector(l); 
						Vector gapHalfWidth = cutLineVector.getDirection(); //get direction the line being cut is pointing
						
//						if(cutLineVector.getMagnitude() < gapWidth){
//							gapWidth = cutLineVector.getMagnitude();
//						}
						
						gapHalfWidth.mult(cutGapWidth/2);
						
						Point2D.Double temp1 = new Point2D.Double();
						temp1.x = cp.getX()-gapHalfWidth.x;
						temp1.y = cp.getY()-gapHalfWidth.y;
						points1.add(temp1);
						
						if(poly.contains(cutl.getP2())){
							points1.add(cutl.getP2());
						}else if(poly.contains(cutl.getP1())){
							points1.add(cutl.getP1());
						}
						
						Point2D.Double temp2 = new Point2D.Double();
						temp2.x = cp.getX()+gapHalfWidth.x;
						temp2.y = cp.getY()+gapHalfWidth.y;
						points1.add(temp2);
						break;
					}
				}
				
			}
		}
		
		Entity e1 = new Entity(points1);
		Entity e2 = new Entity(points2);
		
		e1.copyProperties(this);
		e2.copyProperties(this);
		

		e1.name = name + 1;
		

		e2.name = name + 2;

		
		if(cutpoints.size() > 0){
			markedForDelete = true;
			newshapes.add(e1);
			if(cutpoints.size() > 1){
				newshapes.add(e2);
			}
			
		}else{
			System.out.println("Error: No cutpoints?");
		}
		
		System.out.println("newshapes: " + newshapes);
		
		
//		Point2D.Double cutstart = new Point2D.Double();
		return newshapes;
		
		
		
		
	}
	
	public LinkedList<Entity> collide(Entity e,  boolean doCut){
		LinkedList<Entity> newEntities = new LinkedList<Entity>();
//		System.out.println("velocity: " + velocity);
//		System.out.println("e.velocity: " + e.velocity);
		double res = (elasticity + e.elasticity)/2;//Math.min(restitution, e.restitution);
		Collision col = getCollisionInfo(e);
//		System.out.println(col);
		//float j = col.backtrack.getMagnitude();//magnitude (penetration)
		Vector n = col.normal.getDirection();//direction
		
		Vector rv = new Vector(velocity);
		rv.sub(e.velocity); //relative velocity
		
		double velocityAlongNormal = rv.dotProduct(n);
//		if(velocityAlongNormal < 0){
		double j = (-1 * (1+res) * velocityAlongNormal)/(1/getMass()+1/e.getMass());
		
		Vector impulse = new Vector(n);
		impulse.mult(j);
		Vector impulseA = new Vector(impulse);
		Vector impulseB = new Vector(impulse);
		impulseA.mult(1/getMass());
		impulseB.mult(1/e.getMass());
		
		impulseA.mult(.5);//cut in half because the mirrored collision will also be resolved
		impulseB.mult(.5);
//			velocity.add(impulseA);
//			e.velocity.sub(impulseB);
		deltaVelocity.add(impulseA);
 		e.deltaVelocity.sub(impulseB);

		
 		if(doCut && e.breakable && !breakable){

 			Random r = new Random();
 			double angle = ((r.nextDouble() - .5) * 2) * maxangle; //(between -1 and 1) * maxangle
 			Vector cut = velocity.getDirection();//col.normal.getDirection();
 			cut.mult(rv.getMagnitude()*fragility);
 			cut.rotate(angle);
 			cut.mult(1-passoffcut);
 			System.out.println(cut.getMagnitude());
 			Line l = new Line(col.loc, cut);
 			if(cut.getMagnitude()>cutCutoff){
 				newEntities.addAll(e.cut(l));
 			}
 			
			Vector stepbackcut = new Vector(cut);
			stepbackcut.sub(cut.getDirection()); //subtract 1 from the magnitude
//			stepbackcut.mult(.5);
			Point2D cutEnd = new Point2D.Double(col.loc.getX()+stepbackcut.x, col.loc.getY() + stepbackcut.y);
			double cut1variance = ((r.nextDouble() - .5) * 2) * maxvariance;
			Vector subcut1 = new Vector(cut);
			subcut1.rotate(((r.nextDouble() - .5) * 2) * maxangle);
			subcut1.mult(1/(1-passoffcut));
			subcut1.mult(passoffcut);
			subcut1.mult(1+cut1variance);
			Line sl1 = new Line(cutEnd, subcut1);
			
			double cut2variance = ((r.nextDouble() - .5) * 2) * maxvariance;
			Vector subcut2 = new Vector(cut);
			subcut2.rotate(((r.nextDouble() - .5) * 2) * maxangle);
			subcut2.mult(1/(1-passoffcut));
			subcut2.mult(passoffcut);
			subcut1.mult(1+cut2variance);
			Line sl2 = new Line(cutEnd, subcut2);
//			
			LinkedList<Entity> tempNewEntities = new LinkedList<Entity>();
			for(Entity broken : newEntities){
				tempNewEntities.addAll(broken.cut(sl1));
 			}
			LinkedList<Entity> tempNewEntities2 = new LinkedList<Entity>();
			for(Entity broken : tempNewEntities){
				tempNewEntities2.addAll(broken.cut(sl2));
 			}
			newEntities.addAll(tempNewEntities);
			newEntities.addAll(tempNewEntities2);
			
			int branches = 3;
//			tempNewEntities.addAll(subcut(newEntities));
//			newEntities.add
 			
 		}
 		
 		return newEntities;
		
//		physicsResolved = true;
//		e.physicsResolved = true;
	}
	
//	public LinkedList<Entity> subcut(LinkedList<Entity> in, Point2D prevCutOrigin, Vector prevCut, double depth, int branches){
//		LinkedList<Entity> newEntities = new LinkedList<Entity>();
////		newEntities.addAll(in);
//		LinkedList<Entity> tempNewEntities1 = new LinkedList<Entity>();
//		LinkedList<Entity> tempNewEntities2 = new LinkedList<Entity>();
//		
//		Random r = new Random();
//		double maxangle = .7;
//		
//		Vector stepbackcut = new Vector(prevCut);
//		stepbackcut.sub(prevCut.getDirection()); //subtract 1 from the magnitude
//		
//		Point2D cutEnd = new Point2D.Double(prevCutOrigin.getX()+stepbackcut.x, prevCutOrigin.getY() + stepbackcut.y);
//		
//		for(int i = 0; i < branches; i++){
//			//create cut here
//			Vector subcut1 = new Vector(prevCut.getDirection());
//			subcut1.rotate(((r.nextDouble() - .5) * 2) * maxangle);
//			subcut1.mult(depth);
//			Line sl1 = new Line(cutEnd, subcut1);
//			
//			
//			for(Entity e : in){
//				if(!e.markedForDelete){
//					tempNewEntities.addAll(e.cut(sl1));
//				}
//				
//			}
//			newEntities.addAll(tempNewEntities);
//		}
//		
//		return newEntities;
//	}
	
	public void draw(GL2 gl, Entity camera){
		//System.out.println("In Camera? " + intersects(camera));
		if(visible && intersects(camera.getBounds2D())){
			poly.draw(gl, camera);
		}
		
	}
	
	public boolean intersects(Rectangle2D b){
		return b.intersects(poly.getBounds2D());
	}
	
	public boolean intersects(Entity e){
		Area a = new Area(poly);
		a.intersect(new Area(e.poly));
		return !a.isEmpty();
	}
	
	public boolean intersects(Polygon2D p){
		if(intersects(p.getBounds2D())){
			//check polygon intersection
			//convert to Area type and check intersection
			return true;
		}else{
			return false;
		}
	}
	
	public Rectangle2D getBounds2D(){
		return poly.getBounds2D();
	}
	
	public double getMass(){
		return density*getArea();
	}
	
	public float getArea(){
		float area = 0;
		LinkedList<Point2D.Double> points = poly.getPoints();
		int j = points.size()-1;
		for(int i = 0; i<points.size(); i++){
			Point2D pi = points.get(i);
			Point2D pj = points.get(j);
			double xi = pi.getX();
			double yi = pi.getY();
			double xj = pj.getX();
			double yj = pj.getY();
			area += (xi+xj)*(yj-yi);
			j = i;
		}
//		int j = poly.npoints - 1;
//		for(int i = 0; i<poly.npoints; i++){
//			area += (poly.xpoints[i]+poly.xpoints[j])*(poly.ypoints[j]-poly.ypoints[i]);
//			j = i;
//		}
		return Math.abs(area/2);
	}
	
	public Collision getCollisionInfo(Entity e){
		Collision c1 = getCollisionInfo2(e);
		//Vector backtrack = new Vector(e.velocity);
		//backtrack.mult(-1f);
		Vector rv = new Vector(velocity);
		rv.sub(e.velocity);
		Collision c2 = e.getCollisionInfo2(this, rv);
		if(c1.backtrack.getMagnitude() > c2.backtrack.getMagnitude()){
			return c1;
		}else{
			return c2;
		}//*/
		//return c2;
	}
	
	public Collision getCollisionInfo2(Entity e){
		Vector backtrack = new Vector(velocity);
		backtrack.sub(e.velocity);//get relative velocity
		backtrack.mult(-1f);
		return getCollisionInfo2(e, backtrack);
	}
	
	//TODO: use relative velocity for backtrack
	public Collision getCollisionInfo2(Entity e, Vector backtrackIn){
		Vector backtrack = new Vector(backtrackIn);
		backtrack.slop();
		Polygon2D poly2 = e.poly;
		
		LinkedList<Point2D.Double> containedPoints = new LinkedList<Point2D.Double>();
		for(Point2D.Double p : poly.getPoints()){
			if(poly2.contains(p.getX(), p.getY())){
				containedPoints.add(p);
			}
		}
		
		LinkedList<Line> projections = new LinkedList<Line>();
		
		
		for(Point2D.Double p : containedPoints){
			projections.add(new Line((float)p.getX(), (float)p.getY(), backtrack));
		}
		
		Collision col = new Collision();
		Vector largest = new Vector();
		for(Line l : projections){
			LinkedList<Line> plines = poly2.getLines(); 
			for(Line l2 : plines){
				if(l.intersectsLine(l2)){
					double dist = l2.ptLineDist(l.getX1(), l.getY1()); //x1 and y1 are the origin
					if(dist > largest.getMagnitude()){
						Point2D.Double collided = l.intersection(l2);
						largest = new Vector(l.getX1() - collided.getX(), l.getY1()-collided.getY());
						//normal is perpendicular to L2
						double l2slope = (l2.y1-l2.y2)/(l2.x1-l2.x2);
//						System.out.println("l2slope: " + l2slope);
						double normalslope;//, xdir;
						float slopeRun = 1;

						if(l2.x2-l2.x1 == 0){
							slopeRun = 1;
							normalslope = 0;
							//xdir = 0;
						}else if(l2.y2-l2.y1 == 0){
							slopeRun = 0;
							normalslope = 1;
						}else{
							normalslope = -1/l2slope;
							//xdir = Math.signum(largest.x);
						}
						
//						System.out.println("normalslope: " + normalslope);
						col.normal = new Vector(slopeRun, normalslope);
						if(largest.x <= 0){
							col.normal.mult(-1);
						}
						col.normal.mult(-1);
						col.normal.mult(20);
						col.vertex = l.getP1();
						col.loc = collided;
					}
				}
			}
		}
		largest.mult(-1f);
		col.backtrack = largest;
		//calculate normal and store in col.normal
		
		return col;
	}
	
	public void copyProperties(Entity e){
		gravity = e.gravity;
		density = e.density;
		damped = e.damped;
		dampening = e.dampening;
		elasticity = e.elasticity;
		minAcc = e.minAcc;
		solid = e.solid;
		moving = e.moving;
		visible = e.visible;
		velocity = new Vector(e.velocity);
		deltaVelocity = new Vector(e.deltaVelocity);
		breakable = e.breakable;
		collisionCategory = e.collisionCategory;
		poly.color = e.poly.color;
		poly.filled = e.poly.filled;
	}
	
//	public Vector getVelocity(){
//		Vector v = new Vector(direction);
//		v.mult(speed);
//		return v;
//	}
//	
//	public void setVelocity(Vector v){
//		speed = v.getMagnitude();
//		direction = new Vector(v);
//		direction.mult(1/speed);
//	}
	
//	@Override
//	public String toString() {
//		return "Entity " + name;
//	}

}
