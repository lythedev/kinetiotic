import java.awt.geom.Point2D;

public class Vector {
	public double x,y;
	
	public Vector(){
		x = 0;
		y = 0;
	}
	
	public Vector(double x, double y){
		this.x = x;
		this.y = y;
	}
	
	public Vector(Vector v){
		this.x = v.x;
		this.y = v.y;
	}
	
	public Vector(double x1, double y1, double x2, double y2){
		this.x = x2-x1;
		this.y = y2-y1;
	}
	
	public Vector(Line l){
		this(l.getP1().getX(), l.getP1().getY(), l.getP2().getX(),l.getP2().getY());
	}

	public void setDirection(double angle, double magnitude){
		x = Math.cos(angle) * magnitude;
		y = Math.sin(angle) * magnitude;
	}
	
	public void add(double xd, double yd){
		x += xd;
		y += yd;
	}
	
	public void add(Vector v){
		x += v.x;
		y += v.y;
	}
	
	public void sub(Vector v){
		x -= v.x;
		y -= v.y;
	}
	
	public void mult(double c){
		x*=c;
		y*=c;
	}
	
	public void mult(Vector v){
		x *= v.x;
		y *= v.y;
	}
	
	public void rotate(double theta){
		x = x * Math.cos(theta) - y * Math.sin(theta);
		y = x * Math.sin(theta) + y * Math.cos(theta);
	}
	
	public double dotProduct(Vector v){
		return x*v.x+y*v.y;
	}
	
	public double dotProduct(double x2, double y2){
		return x*x2+y*y2;
	}
	
	public double getMagnitude(){
		return Math.sqrt(x*x+y*y);
	}
	
	public Vector getDirection(){
		double m = getMagnitude();
		if(m == 0){
			return new Vector(0,0);
		}
		return new Vector(x/m, y/m);
	}
	
	public void slop(){
		x += Math.signum(x)*2.5;
		y += Math.signum(y)*2.5;
	}

	@Override
	public String toString() {
		return "Vector [x=" + x + ", y=" + y + "]";
	}
	
}
