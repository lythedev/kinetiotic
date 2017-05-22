import java.awt.geom.Point2D;

public class Collision {
	Vector normal, backtrack;
	Point2D loc;
	Point2D vertex; //loc = point on surface, vertex = point inside
	
	
	public Collision(){
		normal = new Vector();
		backtrack = new Vector();
		loc = new Point2D.Double();
		vertex = new Point2D.Double();
	}


	@Override
	public String toString() {
		return "Collision [normal=" + normal + ", backtrack=" + backtrack + ", loc=" + loc + ", vertex=" + vertex + "]";
	}

}
