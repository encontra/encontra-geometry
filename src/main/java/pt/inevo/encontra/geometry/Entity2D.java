package pt.inevo.encontra.geometry;


public abstract class Entity2D {
	
	private static int IDCOUNT=0;
	
	private static int getNewID(){
		return IDCOUNT++;
	}
	
	public static class FLAG {
		public static short RESET=0x00;
		public static short VISITED=0x01;
		public static short REMOVE=0x02;
		public static short IGNORE=0x04;
		public static short IN_POLYGON=0x08;
		public static short KEEP=0x10;
	}
	
	private Point _first_point; 
	private Point _last_point; 

	private int _id;
	private short _flag;
    
	protected Box2D _p_bounding_box;
	
	protected Entity2D _owner_entity;
	
	Entity2D()
	{    
	    _p_bounding_box = null;
	    _first_point = null;	
		_last_point = null;	
		_owner_entity = null;
		_id = getNewID();
	}
	
	void ResetFlag(short mask) { _flag &= (~ mask); }
	
	public int GetID() { return _id; };
	public void SetID(int id) { _id = id; };
	
	Point GetFirstPoint() { return _first_point; };
	Point GetLastPoint() { return _last_point; };
	
	void SetFirstPoint(Point p) { _first_point = p; };
	void SetLastPoint(Point p) { _last_point = p; };
	
	Entity2D GetOwnerEntity() { return _owner_entity;};
	void SetOwnerEntity(Entity2D owner_entity) { _owner_entity = owner_entity;}
	
	public abstract void CalculateBoundingBox();
	
	/**
	* @desc returns the bounding box of the entity.Calculates it if does nort exist
	* @return Box2D entity representing the bounding box
	*/
	Box2D GetBoundingBox()
	{
		// if bounding box already exists do not calculate bounding box 
	    if (_p_bounding_box==null)
			CalculateBoundingBox();

	    return _p_bounding_box;
	}

	/**
	* @desc compares two entities in term of order
	* @return negative, zero or positive value 
	*         according to whether the first element 
	*         passed to it is less than, equal to or 
	*         greater than the second one. 
	*/
	// int Entity2D::CompareOrder(Entity2D ***p_e1, Entity2D ***p_e2)
	// {
//		return Point2D::CompareOrder((**p_e1)->_first_point, (**p_e2)->_first_point);
	// }

	/***
	* @desc Auxiliary method for calculation of areas
	* @see O'Rourke, Joseph, "Computational Geometry in C, 2nd Ed.", pp.27
	* @return area of triangle defined by points a, b, c 
	*/
	double Area(Point a, Point b, Point c)
	{
		if (a==null || b==null || c==null)
			return 0.0f;

		return (b.GetX() - a.GetX())*(c.GetY()-a.GetY())-
			(c.GetX()-a.GetX())*(b.GetY()-a.GetY());
	}

	// inline bool Entity2D::IsFirstPoint(Point2D *p)
	// {
//	 	return (*_first_point)==(*p); 
	// }
	// 
	// inline bool Entity2D::IsLastPoint(Point2D *p)
	// {
//	 	return (*_last_point)==(*p); 
	// }
	// 
	// /***
	// * @return a string containing the text with the type name of the entity
	// */
	// wxString Entity2D::Name(entity_type et)
	// {
//	     static wxString name[] = {
//	         "Point",
//	         "Line",
//	         "Polyline",
//	         "Polygon",
//	         "Vector",
//	         "Unknown"
//	     };
	// 
//	     return name[et];
	// }

	/***
	* @desc forces the axis aligned bounding box to be calculated
	*/
	void RecalculateBoundingBox()
	{
		CalculateBoundingBox();
	}

	public boolean CheckFlag(short flag, short mask){ 
		return ((flag & mask)!=0);
	}
	
	public boolean HasFlag(short f) {
		return _flag==f;
	}
	
	public short GetFlag() {
		return _flag;
	}


	public void SetFlag(short _flag) {
		this._flag = _flag;
	}


}
