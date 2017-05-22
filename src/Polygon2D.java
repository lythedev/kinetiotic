import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.LinkedList;

import com.jogamp.opengl.GL2;

public class Polygon2D extends Path2D.Double {

	float[] color = {1f, 1f, 1f};
	
	boolean filled = false;
	
	Polygon2D() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public void draw(GL2 gl, Entity camera){
		if(filled){
			gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
		}else{
			gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
		}
		
		gl.glBegin (GL2.GL_POLYGON);
		gl.glColor3f(color[0], color[1], color[2]);
		for(Point2D.Double p : getPoints()){
			gl.glVertex3d(p.getX()-(float)camera.getBounds2D().getX(), p.getY()-(float)camera.getBounds2D().getY(), 0);
		}
//		for(int i = 0; i<npoints; i++){
//		    gl.glVertex3f(xpoints[i]-(float)camera.getBounds2D().getX(), ypoints[i]-(float)camera.getBounds2D().getY(), 0);
//		}
		gl.glEnd();
	}
	
	public void translate(double x, double y){
		AffineTransform at = new AffineTransform();
		at.translate(x, y);
		transform(at);
	}
	
	public void translate(Vector v){
		translate(v.x, v.y);
	}
	
	public LinkedList<Point2D.Double> getPoints(){
		LinkedList<Point2D.Double> ret = new LinkedList<Point2D.Double>();
		PathIterator pi = getPathIterator(null);
		float[] values = new float[6];
		while (!pi.isDone()) {
		    int type = pi.currentSegment(values);
		    if (type == PathIterator.SEG_LINETO) {
		    	Point2D.Double p = new Point2D.Double(values[0], values[1]);
		    	ret.add(p);
//		        x = values[0];
//		        y = values[1];
		    }
		    else if (type == PathIterator.SEG_MOVETO) {
		    	Point2D.Double p = new Point2D.Double(values[0], values[1]);
		    	ret.add(p);
//		        x = 0;
//		        y = 0;
		    }
		    else {
		        // SEG_MOVETO, SEG_QUADTO, SEG_CUBICTO
		    }
		    pi.next();
		}
		return ret;
		
	}
	
	public LinkedList<Line> getLines() {
		LinkedList<Line> ret = new LinkedList<Line>();
		LinkedList<Point2D.Double> points = getPoints();
		//System.out.println(points);
		if(points.size() > 1){
			if(points.size() > 2){
				for(int i = 1; i < points.size(); i++){
//					System.out.println("i-1: " + (i-1));
//					System.out.println("size: " + points.size());
//					System.out.println(points.get(i-1));
//					System.out.println(points.get(i));
					ret.add(new Line(points.get(i-1),points.get(i)));
				}
			}
			ret.add(new Line(points.getLast(),points.getFirst()));
		}
//		System.out.println(ret);
		return ret;
		
	}

}
