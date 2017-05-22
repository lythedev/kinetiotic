import java.awt.geom.Line2D;
import java.util.LinkedList;

import com.jogamp.opengl.GL2;

public class PolygonI extends java.awt.Polygon{
	
	float[] color = {1f, 1f, 1f};

	private static final long serialVersionUID = 3667474502211044217L;

	public void draw(GL2 gl, Entity camera){
		
		gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
		gl.glBegin (GL2.GL_POLYGON);
		gl.glColor3f(color[0], color[1], color[2]);
		for(int i = 0; i<npoints; i++){
		    gl.glVertex3f(xpoints[i]-(float)camera.getBounds2D().getX(), ypoints[i]-(float)camera.getBounds2D().getY(), 0);
		}
		gl.glEnd();
	}

	public void addPoint(float x, float y) {
		addPoint((int)x, (int)y);
	}

	public void addPoint(double x, double y) {
		addPoint((int)x, (int)y);
	}

	public void translate(double x, double y) {
		// TODO Auto-generated method stub
		translate((int)x, (int)y);
	}
	
	public LinkedList<Line> getLines() {
		LinkedList<Line> ret = new LinkedList<Line>();
		if(npoints > 1){
			if(npoints > 2){
				for(int i = 0; i < npoints-1; i++){
					ret.add(new Line(xpoints[i],
	                                 ypoints[i],
	                                 xpoints[i+1],
	                                 ypoints[i+1]));
				}
			}
			ret.add(new Line(xpoints[npoints-1],
			                 ypoints[npoints-1],
			                 xpoints[0],
			                 ypoints[0]));
		}
		
		return ret;
		
	}
}
