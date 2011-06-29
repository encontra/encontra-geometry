package pt.inevo.encontra.geometry;

public class Vector extends Entity2D implements Cloneable{
	
	public double getDX() { return _dx; }
	public double getDY() { return _dy; }

    public void setDX(double dx) { _dx=dx; }
	public void setDY(double dy) { _dy=dy; }

	private Point _point;
	private double _dx;
	private double _dy;
	
	public Vector()
	{
	  _dx = 0;
	  _dy = 0;
	}
	
	/***
	* @desc constructor 
	* @param origin point
	* @param x component
	* @param y component
	*/
	public Vector(Point point, double dx, double dy)
	{
	  _point = point;
	  _dx = dx;
	  _dy = dy;
	}

	/***
	* @desc constructor 
	* @param origin point
	* @param end point
	*/
	public Vector(Point start_point, Point end_point)
	{
	  _point = start_point;

	  _dx=end_point.getX()-start_point.getX();
	  _dy=end_point.getY()-start_point.getY();
	}
	
	/***
	* @desc execute a translation of (dx,dy) on this vector
	*/
	void Translate(double dx, double dy)
	{
	  _point.translate(dx, dy);
	}


	/***
	* @desc execute a scale of (sx,sy) on this vector
	*/
	void Scale(double sx, double sy)
	{
	  _point.scale(sx, sy);
	  _dx*=sx;
	  _dy*=sy;
	}

	/***
	* @desc execute a rotation of theta on this vector
	*/
	void Rotate(double theta)
	{	
		Point p = new Point(_point.getX()+getDX(), _point.getY()+getDY());
		_point.rotate(theta);
		p.rotate(theta);

		_dx = p.getX()-getDX();
		_dy = p.getY()-getDY();

	}

	/***
	* @return true if vector is aligned with one axis, false otherwise
	*/
	boolean IsAxisAligned()
	{
		return (getDX()==0 || getDY()==0);
	}
	
	/***
	* @return the dot product of vectors u and v
	*/
	public static double dot(Vector u, Vector v)
	{
		return (u._dx*v._dx+u._dy*v._dy);
	}
	
	/***
	* @desc operator * override 
	*/
	Vector mul(double c)
	{
	  Vector result = this.clone();
	  
	  result._dx*=c;
	  result._dy*=c;

	  return result;
	}

    /**
     * Returns the angle between the two vectors.
     * @param u
     * @return
     */
    public double angle(Vector u) {
        return (float)Math.abs(Math.atan2(this._dx*u.getDY() - this._dy*u.getDX() , Vector.dot(this, u)));
    }

	@Override
	protected Vector clone(){
		return new Vector(_point,_dx,_dy);
	}
	
	@Override
	public void calculateBoundingBox() {
		// TODO Auto-generated method stub
		
	}
}
