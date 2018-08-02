package csguided;

/**
 * two dimensional grid cell
 * @author moarref
 *
 */
public class GridCell2D {

	private int x;
	private int y;
	
	public GridCell2D(int argX, int argY){
		x=argX;
		y=argY;
	}
	
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}
	
	public void setX(int argX){
		x=argX;
	}
	
	public void setY(int argY){
		y=argY;
	}
	
	public void print(){
		System.out.println("x = "+x+", y = "+y);
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
