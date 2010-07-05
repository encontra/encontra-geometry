package pt.inevo.encontra.geometry;

import java.util.ArrayList;
import java.util.Collections;

public class Line extends Entity2D implements Comparable<Line>{
	//PointArray _intersections;
	
    ArrayList <Point> _intersections=new ArrayList<Point>();

    public Point _start_point=new Point();
    public Point _end_point=new Point();
    
    void SetStartPoint(Point p){_start_point=p; CalculateFirstAndLastPoint(); }
    void SetEndPoint(Point p){_end_point=p; CalculateFirstAndLastPoint(); }
    Point GetStartPoint() { return _start_point; };
	Point  GetEndPoint() { return _end_point; };
	
	public Line(Point  start_point, Point  end_point){
		_start_point=start_point;
		 _end_point=end_point;
		 CalculateFirstAndLastPoint();
	}

	/***
	* @desc constructor
	* @param endpoints coordinates
	*/
	public Line(double x1, double y1, double x2, double y2){ 
		_start_point.x=x1;
		_start_point.y=y1;

		_end_point.x=x2;
		_end_point.y=y2;

		CalculateFirstAndLastPoint();
	}

	 

	/***
	* @desc adds an intersection point to the intersection points list
	* @param pointer to point2D entity
	* @note the point will be deleted by the line destructor
	*/
	void AddIntersectionPoint(Point p)
	{
		if (p!=null) _intersections.add(p);
	}

	/***
	* @desc sort the intersections list
	*/
	void SortIntersectionsList()
	{
		Collections.sort(_intersections); //,GraphicalPrimitives2D::Point2D::CompareOrder);
	}
	
/*
	#define CREATE_POINTS_FOR_INTERSECTION_ALGORITHMS()  \
		Point2D * a = &_start_point; \
		Point2D * b = &_end_point; \
		Point2D * c = line->GetStartPoint(); \
		Point2D * d = line->GetEndPoint(); 
*/
	

	/***
	* @desc selectors that set the attributes of this line
	* @param attribute value to be set
	*/
	//void Line2D::SetStartX(double x) { _start_point.SetX(x); }
	//void Line2D::SetStartY(double y) { _start_point.SetY(y); }
	//void Line2D::SetEndX(double x) { _end_point.SetX(x); }
	//void Line2D::SetEndY(double y) { _end_point.SetY(y); }


	// /***
	// * @desc calculate the bounding box of this line
	// */
	// void Line2D::CalculateBoundingBox()
	// {
//	 	// if bounding box already exists it will not calculate
//	 	if (_p_bounding_box)
//	 	  return;
//	     
//	 	// deleted when above lines added (25-Jun-2003)
//	     // DELETE_OBJECT(_p_bounding_box );
//	       
//	     double dx = (_end_point.GetX() - _start_point.GetX());
//	     double dy = (_end_point.GetY() - _start_point.GetY());
	//   
//	     _p_bounding_box= new Box2D(
//	         &_start_point, 
//	         Vector2D(&_start_point, dx, 0), 
//	         Vector2D(&_start_point, 0, dy));     
	//}

	/***
	* @desc calculate the first and Last point of this line
	*/
	void CalculateFirstAndLastPoint()
	{	
		if (_start_point.compareTo(_end_point)<0) {
			SetFirstPoint( _start_point );
			SetLastPoint( _end_point );		
		} else {
			SetFirstPoint( _end_point );
			SetLastPoint( _start_point );	
		}
	}

	/***
	* @param pointer to point
	* @return pointer to the opposite point of the one passed 
	*         or NULL if point passed don't belong to line
	*/
	Point GetOtherPoint(Point p)
	{
		if (p==_start_point)
			return _end_point;
		
		if (p==_end_point)
			return _start_point;

		return null;
	}

	/***
	* @return a pointer to the point on passed coordinates
	*/
	Point PointAt(double x, double y)
	{
		if (x == _start_point.x && y == _start_point.y) {
			return _start_point;
		}
		
		if (x == _end_point.x && y == _end_point.y) {
			return _end_point;
		}

		return null;
	}

	/**
	* @param a pointer to a line
	* @return true if line intersects with this, false otherwise
	*/
	boolean Intersects(Line line)
	{

		if (IntersectsProper(line))
			return true;

		Point a = _start_point;
		Point b = _end_point;
		Point c = line.GetStartPoint();
		Point d = line.GetEndPoint(); 
		
		return (c.Between(a,b) ||
			d.Between(a,b) ||
			a.Between(c,d) ||
			b.Between(c,d));
	}

	/***
	* @param a pointer to a line
	* @return true is line have a proper intersection with this, false otherwise
	* @note a proper intersection occurs when two segments intersects at a point 
	*      interior to both
	* @see O'Rourke, Joseph, "Computational Geometry in C, 2nd Ed.", pp.30 
	*/
	boolean IntersectsProper(Line line)
	{
		Point a = _start_point;
		Point b = _end_point;
		Point c = line.GetStartPoint();
		Point d = line.GetEndPoint();

		// Eliminate improper cases
		if (c.Collinear(a, b) || d.Collinear(a,b) || a.Collinear(c,d) || b.Collinear(c,d))
			return false;

		return ( c.Left(a,b) ^ d.Left(a,b) ) &&
			( a.Left(c,d) ^ b.Left(c,d) );
	}

	/**
	* @param a pointer to a line
	* @return a pointer to the intersection point between this line and the line passed
	* @note do not forget to delete the returned point elsewhere
	* @see O'Rourke, Joseph, "Computational Geometry in C, 2nd Ed.", pp.220-226
	*/
	Point IntersectionPoint(Line line)
	{

		// in case two lines share just vertices it was no intersection
		if (line._start_point.equals(_start_point) ||
			line._start_point.equals(_end_point) ||
			line._end_point.equals(_start_point) ||
			line._end_point.equals(_end_point))
			return null;
		
		double s, num, denom;

		Point a = _start_point;
		Point b = _end_point;
		Point c = line.GetStartPoint();
		Point d = line.GetEndPoint(); 

		// first we calculate the denominator of equation
		denom = a.GetX() * (d.GetY() - c.GetY()) +
			b.GetX() * (c.GetY() - d.GetY()) +
			d.GetX() * (b.GetY() - a.GetY()) +
			c.GetX() * (a.GetY() - b.GetY());

		// see if segments are parallel
		if (denom == 0.0)
			return null;

		num = a.GetX() * (d.GetY() - c.GetY()) +
			c.GetX() * (a.GetY() - d.GetY()) +
			d.GetX() * (c.GetY() - a.GetY());

		s = num/denom;
	/*
		num = c.GetX() * ( c.GetY() - b.GetY()) +
			b.GetX() * ( a.GetY() - c.GetY()) +
			c.GetX() * ( b.GetY() - a.GetY());

		t = num / denom;
	*/
		
		return new Point(a.GetX()+s*(b.GetX()-a.GetX()), a.GetY()+s*(b.GetY() - a.GetY()));
	}

	/***
	* @desc adds an intersection point to the intersection points list
	* @param pointer to point2D entity
	* @note the point will be deleted by the line destructor
	*/
	// void Line2D::AddIntersectionPoint(Point2D *p)
	// {
//	 	if (p)
//	 		_intersections.Add(p);
	// }
	// 
	// /***
	// * @desc sort the intersections list
	// */
	// void Line2D::SortIntersectionsList()
	// {
//	 	_intersections.Sort(Point2D::CompareOrder);
	// }

	/**
	* @desc compares two lines in term of order
	* @return negative, zero or positive value 
	*         according to whether the first element 
	*         passed to it is less than, equal to or 
	*         greater than the second one. 
	*/
	public int compareTo(Line other)
	{
		int result = GetFirstPoint().compareTo(other.GetFirstPoint());//Point2D::CompareOrder((**p_line1).GetFirstPoint(), (**p_line2).GetFirstPoint());

		if (result==0) {
			// in case lines share first point
			// we must order the lines by its slope

			double dx1 = GetLastPoint().GetX() - GetFirstPoint().GetX();
			double dy1 = GetLastPoint().GetY() - GetFirstPoint().GetY();
			double dx2 = other.GetLastPoint().GetX() - other.GetFirstPoint().GetX();
			double dy2 = other.GetLastPoint().GetY() - other.GetFirstPoint().GetY();

			// by definition of first and last point we are sure that dy > 0

			if (dx1>0 && dx2<0)
				// line 1 in 1st quadrant, line 2 in 2nd quadrant
				// this means line 2 cames first
				return 1;

			if (dx1<0 && dx2>0)
				// line 1 in 2nd quadrant, line 2 in 1st quadrant
				// this means line 1 cames first
				return -1;

			if (dx1==0) {
				// first line is vertical
				if (dx2>0)
					// second line in 1st quadrant
					// first line is previous
					return -1;

				if (dx2<0)
					// second line in 2nd quadrant
					// second line is previous
					return 1;
				// this should no happen
				return 0;
			}

			if (dx2==0) {
				// second line is vertical
				if (dx1>0)
					// first line in 1st quadrant
					// second line is previous
					return 1;

				if (dx1<0)
					// first line in 2nd quadrant
					// first line is previous
					return -1;

				// this should not happen
				return 0;
			}


			// calculate the slopes
			double m1 = dy1/dx1;
			double m2 = dy2/dx2;
			// line 1 and line 2 in 2nd quadrant
			if (m1>m2)
				return -1;
			if (m1<m2)
				return 1;
			
			// in this case we have the same slope in both lines, 
			// which means that both lines are coincident.
			return 0;
		}

		return result;
	}	

	/***
	* @desc perform rounding in entities coordinates
	*/
	void PerformRounding(double gamma)
	{
		_end_point.PerformRounding(gamma);
		_start_point.PerformRounding(gamma);
	}

	/***
	* @return true if lines overlap, false otherwise
	*/
	public static boolean Overlapping(Line line_1, Line line_2)
	{
		Point p1, p2, p3, p4;

		p1 = line_1.GetStartPoint();
		p2 = line_1.GetEndPoint();
		p3 = line_2.GetStartPoint();
		p4 = line_2.GetEndPoint();
		
		// first see of all endpoints are collinear,
		// then see if any endpoint of one line lies on the other line
		return (p1.Collinear(p3,p4) && p2.Collinear(p3,p4)) &&
			((line_1.Contains(p3) || line_1.Contains(p4)) ||
			 (line_2.Contains(p1) || line_2.Contains(p2)));
	}

	/***
	* @return true if lines are strictly overlapping, false otherwise
	*/
	public static boolean StrictOverlapping(Line line_1, Line line_2)
	{
		Point p1, p2, p3, p4;

		p1 = line_1.GetStartPoint();
		p2 = line_1.GetEndPoint();
		p3 = line_2.GetStartPoint();
		p4 = line_2.GetEndPoint();

		// check the case the lines are coincident
		if (((p1.equals(p3)) && (p2.equals(p4))) || ((p2.equals(p3)) && (p1.equals(p4))))
			return true;

		// first see of all endpoints are collinear,
		// then see if any endpoint of one line lies on the other line
		return (p1.Collinear(p3,p4) && p2.Collinear(p3,p4)) &&
			((line_1.StrictContains(p3) || line_1.StrictContains(p4)) ||
			 (line_2.StrictContains(p1) || line_2.StrictContains(p2)));
	}

	/***
	* @return a new simplified line if line_1 and line_2 overlaps, null otherwise
	*/
	public static Line SimplifiedLine(Line line_1, Line line_2)
	{
		if (Overlapping(line_1, line_2)){		
			if (line_1.Contains(line_2))
				return line_1;
			if (line_2.Contains(line_1))
				return line_2;

			Point new_line_start_point;
			Point new_line_end_point;

			// detects which point of <line_1> must be removed
			if (line_1.GetStartPoint().Between(line_2.GetStartPoint(), line_2.GetEndPoint())) {
				new_line_start_point = line_1.GetEndPoint();
			} else {
				new_line_start_point = line_1.GetStartPoint();
			}
			// detects which point of <line_2> must be removed
			if (line_2.GetStartPoint().Between(line_1.GetStartPoint(), line_1.GetEndPoint())) {
				new_line_end_point = line_2.GetEndPoint();
			} else {
				new_line_end_point = line_2.GetStartPoint();
			}

			// create a new line
			return new Line(new_line_start_point, new_line_end_point);
		}

		return null;

	}

	/***
	* @return true if <line_1> contains <line_2>, false otherwise
	*/
	public static boolean Contains(Line line_1, Line line_2)
	{
		return ( line_1.Contains(line_2.GetStartPoint()) && line_1.Contains(line_2.GetEndPoint()));
	}

	public boolean Contains(Point point) { return Contains(this, point); }

	public boolean Contains(Line line) { return Contains(this, line); }
	
	/***
	* @return true if <line_1> contains strictly <line_2>, false otherwise
	*/
	public static boolean StrictContains(Line line_1, Line line_2)
	{
		return ( line_1.StrictContains(line_2.GetStartPoint()) && line_1.StrictContains(line_2.GetEndPoint()));
	}

	public boolean StrictContains(Point point) { return StrictContains(this, point); }
	public boolean StrictContains(Line line) { return StrictContains(this, line); }
	
	/***
	* @return true if <line> contains <point>, false otherwise
	*/
	public static boolean Contains(Line line, Point point)
	{
		return point!=null && line!=null && point.Between(line.GetStartPoint(), line.GetEndPoint());
	}

	/***
	* @return true if <line> contains strictly <point>, false otherwise
	*/
	public static boolean StrictContains(Line line, Point point)
	{
		return point!=null && line!=null && point.StrictBetween(line.GetStartPoint(), line.GetEndPoint());
	}

	/***
	* @return distance from point p to this line
	*/
	public double Distance(Point p, Point nearest_point)
	{
		return Distance(GetStartPoint(), GetEndPoint(), p, nearest_point);
	}

	/***
	* @desc macro to set the neares point
	* @note used only in distance method, undefined a few lines below
	*/
	//#define SET_NEAREST_POINT(p) if (nearest_point) nearest_point.Set(p);

	public static double Distance(Point endpoint_1, Point endpoint_2, Point p) {
		return Distance(endpoint_1,endpoint_2,p,null);
	}
	
	/***
	* @desc distance between point p and line segment defined 
	*    by <endpoint_1> and <endpoint_2>
	*/
	public static double Distance(Point endpoint_1, Point endpoint_2, Point p, Point nearest_point)
	{
		Vector u=new Vector(endpoint_1, endpoint_2);
		Vector v=new Vector(endpoint_1, p);

		double c1 = Vector.dot(v, u);
		if (c1<=0) {
			if (nearest_point!=null) nearest_point=endpoint_1.clone();
			return p.DistanceTo(endpoint_1);
		}

		double c2 = Vector.dot(u, u);
		if (c2<=c1) {
			if (nearest_point!=null) nearest_point=endpoint_2.clone();
			return p.DistanceTo(endpoint_2);
		}

		Vector w = u.mul(c1/c2);

		Point q=new Point(endpoint_1.GetX()+w.getDX(), endpoint_1.GetY()+w.getDY()); 
		if (nearest_point!=null) nearest_point=q.clone();
		return p.DistanceTo(q);
	}



	public boolean equals(Line other) {
		Point p1, p2, p3, p4;

		p1 = GetStartPoint();
		p2 = GetEndPoint();
		p3 = other.GetStartPoint();
		p4 = other.GetEndPoint();

		return (p1.equals(p3) && p2.equals(p4)) || (p1.equals(p4) && p2.equals(p3));
	}

	/***
	* @desc performs a scale transformation on this line
	* @param x component of the translation
	* @param y component of the translation
	*/
	void Scale(double sx, double sy)
	{
		_start_point.Scale(sx,sy);
		_end_point.Scale(sx,sy);
		CalculateFirstAndLastPoint();
	}

	/***
	* @desc performs a translation transformation on this line
	* @param x component of the translation
	* @param y component of the translation
	*/
	void Translate(double dx, double dy)
	{
		_start_point.Translate(dx,dy);
		_end_point.Translate(dx,dy);
		CalculateFirstAndLastPoint();
	}


	/***
	* @desc performs a rotation transformation on this line
	* @param theta component of the translation
	*/
	void Rotate(double theta)
	{
		_start_point.Rotate(theta);
		_end_point.Rotate(theta);
		CalculateFirstAndLastPoint();
	}

	@Override
	public void CalculateBoundingBox() {
		// TODO Auto-generated method stub
		
	}

	boolean HasIntersections() { return (_intersections.size()>0); }	
	ArrayList <Point> GetIntersectionsList() { return _intersections; }

	
	/***
	* @desc computes the Minkowski Sum of this line
	* @return convex poligon representing the Minkowski sum 
	* @see documents/minkowski.vsd
	* @note do not forget to delete the returned polygon
	*/
	// Polygon2D * Line2D::GetMinkowskiSum(double pixel_size)
	// {
//	     Polygon2D *result = null;
//	     Point2D *p1, *p2;
//	     
//	     int order = Point2D::CompareOrder(&_start_point,&_end_point);
	// 
//	     // check if endpoint and startpoint are coincident
//	     if (order == 0) return null;
	// 
//	     if (order < 0) {
//	         // start point before end point: dy>0 or (dy=0 and dx>0)
//	         p1 = &_start_point;
//	         p2 = &_end_point;
//	     } else {        
//	         p1 = &_end_point;
//	         p2 = &_start_point;
//	     }
	// 
//	     double dx = p2.GetX() - p1.GetX();
//	     double dy = p2.GetY() - p1.GetY();
//	     
//	     // compute half pixel size
//	     double s = pixel_size/2.0f;
	// 
//	     result = new Polygon2D();
//	     
//	     // the Minkowski sum of a line segment is a convex polygon
//	     result.SetConvex(true);
	// 
//	     // case 1: dx = 0
//	     if (dx==0) {
//	         result.AddVertex(new Point2D(p1.GetX()-s, p1.GetY()-s)); // V1
//	         result.AddVertex(new Point2D(p1.GetX()+s, p1.GetY()-s)); // V2
	// 
//	         result.AddVertex(new Point2D(p2.GetX()+s, p2.GetY()+s)); // V3
//	         result.AddVertex(new Point2D(p2.GetX()-s, p2.GetY()+s)); // V4
	// 
//	         return result;
//	     }
	// 
//	     // case 2: dy = 0
//	     if (dy==0){
//	         result.AddVertex(new Point2D(p1.GetX()-s, p1.GetY()+s)); // V1
//	         result.AddVertex(new Point2D(p1.GetX()-s, p1.GetY()-s)); // V2
	// 
//	         result.AddVertex(new Point2D(p2.GetX()+s, p2.GetY()-s)); // V3
//	         result.AddVertex(new Point2D(p2.GetX()+s, p2.GetY()+s)); // V4
	// 
//	         return result;
//	     }
	// 
//	     // case 3: dx < 0
//	     if (dx<0){
//	         result.AddVertex(new Point2D(p1.GetX()-s, p1.GetY()-s)); // V1
//	         result.AddVertex(new Point2D(p1.GetX()+s, p1.GetY()-s)); // V2
//	         result.AddVertex(new Point2D(p1.GetX()+s, p1.GetY()+s)); // V3
	// 
//	         result.AddVertex(new Point2D(p2.GetX()+s, p2.GetY()+s)); // V4
//	         result.AddVertex(new Point2D(p2.GetX()-s, p2.GetY()+s)); // V5
//	         result.AddVertex(new Point2D(p2.GetX()-s, p2.GetY()-s)); // V6
	// 
//	         return result;
//	     }
	// 
//	     
//	     // case 4: dx > 0
//	     if (dx>0){
//	         result.AddVertex(new Point2D(p1.GetX()-s, p1.GetY()+s)); // V1
//	         result.AddVertex(new Point2D(p1.GetX()-s, p1.GetY()-s)); // V2
//	         result.AddVertex(new Point2D(p1.GetX()+s, p1.GetY()-s)); // V3
	// 
//	         result.AddVertex(new Point2D(p2.GetX()+s, p2.GetY()-s)); // V4
//	         result.AddVertex(new Point2D(p2.GetX()+s, p2.GetY()+s)); // V5
//	         result.AddVertex(new Point2D(p2.GetX()-s, p2.GetY()+s)); // V6
	// 
//	         return result;
//	     }
	// 
//	     DELETE_OBJECT(result);
//	     return null;
	// }

	
	
	double GetMaxX() {
		return Math.max(_start_point.x, _end_point.x);
	} 

	double GetMaxY() {
		return Math.max(_start_point.y, _end_point.y);
	}
	
	double GetMinX() {
		return Math.min(_start_point.x, _end_point.x);
	}
	
	double GetMinY() {
		return Math.min(_start_point.y, _end_point.y);
	}
	

}
