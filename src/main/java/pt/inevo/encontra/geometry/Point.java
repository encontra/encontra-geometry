package pt.inevo.encontra.geometry;


public class Point extends Entity2D implements Comparable<Point>{
	
	public double x;
	public double y;
	
	int _index;
	void SetIndex(int index) { _index=index; };
	int GetIndex() { return _index; }; 
	
	public Point() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Point(double xx, double yy) {
		super();
		x=xx; y=yy;
		// TODO Auto-generated constructor stub
	}

	public int compareTo(Point o) {
		if (GetY() < o.GetY())
			return -1;
		else
			if (GetY() == o.GetY()) {
				if (GetX() < o.GetX())
					return -1;
				else 
					if (GetX() == o.GetX())
						return 0;
			}
		
		// p1 is greater than p2
		return 1;
	}

	public double GetY() {
		return y;
	}

	public double GetX() {
		return x;
	}

	public void SetX(double d) {
		x=d;
	}
	
	public void SetY(double d) {
		y=d;
	}
	
	/***
	* @desc performs a scale transformation on this point
	* @param sx component of the scale
	* @param sy component of the scale
	*/
	void Scale(double sx, double sy)
	{
	  SetX(GetX()*sx);
	  SetY(GetY()*sy);
	}


	/***
	* @desc performs a translation transformation on this point
	* @param dx component of the translation
	* @param dy component of the translation
	*/
	void Translate(double dx, double dy)
	{
	  SetX(GetX()+dx);
	  SetY(GetY()+dy);
	}

	/***
	* @desc performs a rotation transformation on this point
	* @param theta component of the translation
	* @see J. D. Foley, A. van Dam, S. K. Feiner, J. F. Hughes, 
	*      Computer Graphics: Principles and Practice, Addison-Wesley, 2nd ed in C, 1990, chaper 5.
	*/
	void Rotate(double theta)
	{
		double rotated_x = GetX()*Math.cos(theta)-GetY()*Math.sin(theta);
		double rotated_y = GetX()*Math.sin(theta)+GetY()*Math.cos(theta);

		SetX(rotated_x);
		SetY(rotated_y);
	}

	/***
	* @desc calculates the distance between this point and another
	* @param p point
	* @return distance
	*/
	public double DistanceTo(Point p)
	{
	  if (p==null)
	    return 0.0f;

	  return Math.sqrt(SquareDistanceTo(p));
	}


	/***
	* @desc calculates the square of the distance between this point 
	*     and another
	* @param p point 
	* @return distance
	*/
	public double SquareDistanceTo(Point p)
	{
		return Math.sqrt(Math.abs(GetX()-p.GetX()))+Math.sqrt(Math.abs(GetY()-p.GetY()));
	}

	
	@Override
	protected Point clone(){
		Point np=new Point(GetX(),GetY());
		np.SetID(this.GetID());
		return np;
	}



	/***
	* @desc fills the first and last point attribute
	*/
	void CalculateFirstAndLastPoint()
	{
		SetFirstPoint(this);
		SetLastPoint(this);
	}

	public boolean equals(Point p) {
		return (x == p.x) && (y == p.y); 
	}






	/**
	* @desc compares two points in term of order
	* @return negative, zero or positive value 
	*         according to whether the distance of <p1> to its owner is less than, equal to or 
	*         greater than the distance of <p2> to its owner. 
	*/
	public static int CompareOrderUsingDistanceToOwner(Point p1, Point p2)
	{
	    // if any of the points have no owner entity they cannot be compared
	    if ((p1._owner_entity==null) || (p2._owner_entity==null)) return 0;

	    Point first_point_owner_p1 = p1._owner_entity.GetFirstPoint();
	    Point first_point_owner_p2 = p2._owner_entity.GetFirstPoint();
	    
	    // if any of the points owner entity have no first point computed 
	    // then the points cannot be compared
	    if (first_point_owner_p1==null || first_point_owner_p2==null) return 0;

	    double sqr_distance_p1 = p1.SquareDistanceTo(first_point_owner_p1);
	    double sqr_distance_p2 = p2.SquareDistanceTo(first_point_owner_p2);

	    if (sqr_distance_p1<sqr_distance_p2)
	        return -1;
	    
	    if (sqr_distance_p1 > sqr_distance_p2)
	        return 1;

	    return 0;
	}

	/***
	* @desc indicates if this point is collinear with a and b
	* @return true if is collinear, false otherwise
	* @see O'Rourke, Joseph, "Computational Geometry in C, 2nd Ed.", pp.29
	*/
	public boolean Collinear(Point a, Point b)
	{
		return Area(a, b, this) == 0.0f;
	}


	/***
	* @desc indicates if this point is at left of the directed line from a to b
	* @return true if is at left, false otherwise
	* @see O'Rourke, Joseph, "Computational Geometry in C, 2nd Ed.", pp.29
	*/
	boolean Left(Point a, Point b)
	{
		return Area(a,b, this) > 0.0f;
	}

	/***
	* @return true is this point is betwen a and b
	* @note c must be collinear with a and b
	* @see O'Rourke, Joseph, "Computational Geometry in C, 2nd Ed.", pp.32
	*/
	boolean Between(Point a, Point b)
	{
		// if this point is not collinear with a and b
		// then it cannot be between this two points
		if (! Collinear(a,b)) 
			return false;

		if (a.GetX() != b.GetX()) 
			return ((a.GetX()<=x) && (x<=b.GetX())) ||
				((a.GetX()>=x) && (x >= b.GetX()));
		else
			return ((a.GetY()<=y) && (y<=b.GetY())) ||
				((a.GetY()>=y) && (y >=b.GetY()));
	}

	/***
	* @return true is this point is betwen a and b,
	*        but is different from a and b
	* @note c must be collinear with a and b
	* @see O'Rourke, Joseph, "Computational Geometry in C, 2nd Ed.", pp.32
	*/
	boolean StrictBetween(Point a, Point b)
	{
		// first check if this point is between a and b
		if (! Between(a,b)) 
			return false;

		// if is between, lets check if it is not coincident with
		// one of them
		return (!a.equals(this) && !(b.equals(this)));
	}

	/***
	* @desc performs the rounding of coordinates according with gamma
	*/
	void PerformRounding(double gamma)
	{
		x=Round(x, gamma);
		y=Round(y, gamma);
	}

	/***
	* @desc performs the rounding of value pointed by <value> according with 
	*       value pointed by <gamma>
	*/
	double Round(double value, double gamma)
	{
		double v = value / gamma;

		double v_ceil = Math.ceil(v);

		if (Math.abs(v-v_ceil)<=0.5)
			value = v_ceil * gamma;
		else
			value = Math.floor(v) * gamma;
		return value;
	}

	@Override
	public void CalculateBoundingBox() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public String toString() {
		return "P"+GetID()+" ("+x+","+y+")";
	}
	
	
	
}
