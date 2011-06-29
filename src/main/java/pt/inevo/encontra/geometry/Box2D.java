package pt.inevo.encontra.geometry;

public class Box2D extends Entity2D {
	Point m_point;
	Vector m_vi;
	Vector m_vj;

	Point m_center; 

	boolean m_is_axes_aligned;
	

	/***
	* @desc constructor
	* @param another box
	*/
	Box2D(double x_min, double y_min, double x_max, double y_max)
	{
		Point p=new Point(x_min, y_min);
		m_point = p;
		m_vi = new Vector(p, x_max-x_min, 0);
		m_vj = new Vector(p, 0, y_max-y_min);
	}

	/***
	* @desc constructor
	* @param point 
	* @param vector
	* @param vector
	*/
	Box2D( Point point,  Vector  vi,  Vector vj) {
	  m_point = point;
	  m_vi = vi;
	  m_vj = vj;

	  CalculateAlignment();
	}


	/***
	* @return bounding box of this Box2D
	*/
	@Override
	Box2D GetBoundingBox()
	{
	  Point p=new Point(GetMinX(), GetMinY());

	  return new Box2D(
	    p, 
	    new Vector(p,GetMaxX()-GetMinX(),0),
	    new Vector(p,0,GetMaxY()-GetMinY()));
	}

	/***
	* @param pointer to Box2D
	* @param pointer to Box2D
	* @return bounding box of the two Box2D passed 
	*/
	Box2D GetBoundingBox(Box2D box2d_a, Box2D box2d_b)
	{
	  if (box2d_a==null) {
	    if (box2d_b!=null)
	      return box2d_b.clone();
	    else
	      return null;
	  }
	  
	  if (box2d_b==null)
	    return box2d_a.clone();

	  double a_min_x=box2d_a.GetMinX();
	  double b_min_x=box2d_b.GetMinX();
	  double minX = Math.min(a_min_x,b_min_x);

	  double a_min_y=box2d_a.GetMinY();
	  double b_min_y=box2d_b.GetMinY();
	  double minY = Math.min(a_min_y,b_min_y);
	  
	  double a_max_x=box2d_a.GetMaxX();
	  double b_max_x=box2d_b.GetMaxX();
	  double maxX = Math.min(a_max_x,b_max_x);

	  double a_max_y=box2d_a.GetMaxY();
	  double b_max_y=box2d_b.GetMaxY();
	  double maxY = Math.min(a_max_y,b_max_y);

	  Point p=new Point(minX,minY);

	  return new Box2D(p, new Vector(p,maxX-minX,0), new Vector(p,0,maxY-minY));
	}

	Point GetPoint() { return m_point; };  
	Vector GetVi() { return m_vi; };  
	Vector GetVj() { return m_vj; };  
	
	protected Box2D clone(){
		return new Box2D(GetPoint(),GetVi(),GetVj());
	}

	/***
	* @return maximum x coordinate of this box
	*/
	double GetMaxX()
	{
	  double x = m_point.getX();
	  double i_dx = x+m_vi.getDX();
	  double j_dx = x+m_vj.getDX();
	  double max_i = Math.max(x,i_dx);

	  return Math.max(max_i,j_dx);
	}

	/***
	* @return maximum y coordinate of this box
	*/
	double GetMaxY()
	{
	  double y = m_point.getY();
	  double i_dy = y+m_vi.getDY();
	  double j_dy = y+m_vj.getDY();
	  double max_i = Math.max(y,i_dy);

	  return Math.max(max_i,j_dy);
	}

	/***
	* @return minimum x coordinate of this box
	*/
	double GetMinX()
	{
	  double x = m_point.getX();
	  double i_dx = x+m_vi.getDX();
	  double j_dx = x+m_vj.getDX();
	  double min_i = Math.min(x,i_dx);

	  return Math.min(min_i,j_dx);
	}

	/***
	* @return minimum y coordinate of this box
	*/
	double GetMinY()
	{
	  double y = m_point.getY();
	  double i_dy = y+m_vi.getDY();
	  double j_dy = y+m_vj.getDY();
	  double min_i = Math.min(y,i_dy);

	  return Math.min(min_i,j_dy);
	}



	/**
	* @desc execute a translation of this box
	*/
	void Translate(double dx, double dy)
	{
	  m_point.translate(dx, dy);
	}


	/**
	* @desc scale the box by (sx,sy)
	* @note: attention to the box center
	*/
	void Scale(double sx, double sy)
	{
	  m_point.scale(sx, sy);
	  m_vi.Scale(sx,sy);
	  m_vj.Scale(sx,sy);
	}

	/**
	* @desc rotate the box by theta
	* @note: attention to the box center
	*/
	void Rotate(double theta)
	{
		m_point.rotate(theta);
		m_vi.Rotate(theta);
		m_vj.Rotate(theta);
	}



	/**
	* @return Point2D representing the box center
	*/
	Point GetCenter()
	{
	  double x = m_point.getX()+m_vi.getDX()/2+m_vj.getDX()/2;
	  double y = m_point.getY()+m_vi.getDY()/2+m_vj.getDY()/2;

	  return new Point(x,y);
	}

	/**
	* @return witdh of the box 
	*/
	double GetWidth()
	{
	  return GetMaxX()-GetMinX();
	}

	/**
	* @return box height
	*/
	double GetHeigth()
	{
	  return GetMaxY()-GetMinY();
	}


	/***
	* @desc determines if box is axes-aligned or not
	*/
	void CalculateAlignment()
	{
		m_is_axes_aligned = (m_vi.IsAxisAligned() && m_vj.IsAxisAligned());		
	}

	/***
	* @returns true if this box contains <box>, false otherwise
	*/
	boolean Contains(Box2D box)
	{
		if (box==null) return false;

		if (m_is_axes_aligned) {
			return (GetMinX()<=box.GetMinX()) && (GetMaxX()>=box.GetMaxX()) &&
				(GetMinY()<=box.GetMinY()) && (GetMaxY()>=box.GetMaxY());		
		} else {
			// TODO: determine this case to use not axes-aligned boxes 
			// for now returns always false
			return false;
		}
	}

	boolean Contains(Point p)
	{
		if (p==null) return false;

		if (m_is_axes_aligned) {
			return (GetMinX()<=p.getX()) && (GetMaxX()>=p.getX()) &&
				(GetMinY()<=p.getY()) && (GetMaxY()>=p.getY());
		} else {
			// TODO: determine this case to use not axes-aligned boxes 
			// for now returns always false
			return false;
		}
	}

	@Override
	public void calculateBoundingBox() {
		// TODO Auto-generated method stub
		
	}


}
