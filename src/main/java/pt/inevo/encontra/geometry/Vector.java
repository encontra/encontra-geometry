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

	  _dx=end_point.GetX()-start_point.GetX();
	  _dy=end_point.GetY()-start_point.GetY();
	}
	
	/***
	* @desc execute a translation of (dx,dy) on this vector
	*/
	void Translate(double dx, double dy)
	{
	  _point.Translate(dx,dy);
	}


	/***
	* @desc execute a scale of (sx,sy) on this vector
	*/
	void Scale(double sx, double sy)
	{
	  _point.Scale(sx,sy);
	  _dx*=sx;
	  _dy*=sy;
	}

	/***
	* @desc execute a rotation of theta on this vector
	*/
	void Rotate(double theta)
	{	
		Point p = new Point(_point.GetX()+getDX(), _point.GetY()+getDY());
		_point.Rotate(theta);
		p.Rotate(theta);

		_dx = p.GetX()-getDX();
		_dy = p.GetY()-getDY();

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
	
	
	@Override
	protected Vector clone(){
		return new Vector(_point,_dx,_dy);
	}
	
	@Override
	public void CalculateBoundingBox() {
		// TODO Auto-generated method stub
		
	}
}
