import com.jogamp.opengl.GL2;

public class Art {
	public static void drawLine(GL2 gl, float x1, float y1, float x2, float y2){
		gl.glBegin (GL2.GL_LINES);//static field
	    gl.glVertex3f(x1,y1,0);
	    gl.glVertex3f(x2,y2,0);
	    gl.glEnd();
	}
}
