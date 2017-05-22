import java.awt.Point;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import com.jogamp.opengl.GL2;

public class Line extends java.awt.geom.Line2D.Double{
	private static final long serialVersionUID = -6081550750542981853L;
	public float color[] = {1, .2f, .2f};
	
	public Line(){
		super();
	}
	
	public Line(float x1, float y1,float x2, float y2){
		super(x1, y1, x2, y2);
	}
	
	public Line(double x1, double y1, double x2, double y2){
		super(x1, y1, x2, y2);
	}
	

	public Line(float x, float y, Vector v){
		super(x, y, x+v.x, y+v.y);
	}
	
	public Line(double x, double y, Vector v) {
		super(x, y, x+v.x, y+v.y);
	}
	
	public Line(Point2D p, Vector v) {
		this(p.getX(),p.getY(), v);
	}
	
	public Line(Point2D.Double p1, Point2D.Double p2) {
		this(p1.getX(), p1.getY(), p2.getX(), p2.getY());
	}

	public Point2D.Double intersection(Line l){
		double d = (x1-x2)*(l.y1-l.y2) - (y1-y2)*(l.x1-l.x2);
		double xi = ((l.x1-l.x2)*(x1*y2-y1*x2)-(x1-x2)*(l.x1*l.y2-l.y1*l.x2))/d;
		double yi = ((l.y1-l.y2)*(x1*y2-y1*x2)-(y1-y2)*(l.x1*l.y2-l.y1*l.x2))/d;
		return new Point2D.Double(xi, yi);
	}
	
	public void draw(GL2 gl, Entity camera){
		gl.glBegin(GL2.GL_LINES);
		gl.glColor3f(color[0], color[1], color[2]);
		gl.glVertex2d(x1-(float)camera.getBounds2D().getX(), y1-(float)camera.getBounds2D().getY());
		gl.glVertex2d(x2-(float)camera.getBounds2D().getX(), y2-(float)camera.getBounds2D().getY());
		gl.glEnd();
	}
	
	
	@Override
	public String toString() {
		return "Line [x1=" + x1 + ", y1=" + y1 + ", x2=" + x2 + ", y2=" + y2 + "]";
	}
}
