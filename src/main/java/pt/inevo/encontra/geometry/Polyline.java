package pt.inevo.encontra.geometry;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class Polyline extends Entity2D {
	
	boolean _closed;
	ArrayList<Point> _vertex_array=new ArrayList<Point>();
	double _length;
	
	public static class POLYLINE_FLAG {
		public static short NORMAL_ORDER=0x00;
		public static short INVERSE_ORDER=0x01;
		public static short AT_END=0x02;
		public static short AT_BEGIN=0x04;
		public static short AT_MIDDLE=0x08;
	}
	
	/***
	* @desc constructor 
	*/
	public Polyline()
	{
		super();
	    _closed=false; 
	    _length=0.0f;
	    _p_bounding_box = null;
		CalculateFirstAndLastPoint();
	}


	/***
	* @desc constructor 
	*/
	public Polyline(Line line )
	{
		_closed = false;
		_length = 0.0f;
		_p_bounding_box = null;

		if (line!=null) {
			AddVertex(line.GetStartPoint().clone());
			AddVertex(line.GetEndPoint().clone());
		}

		CalculateFirstAndLastPoint();
	}

	public boolean AddVertex(Point vertex) {
		return AddVertex(vertex, POLYLINE_FLAG.AT_END);
	}
	
	public boolean AddVertex(Point vertex, short flag) {
		return AddVertex(vertex,flag,0);
	}
	
	int VertexCountInPolyline() {
		return GetVertexCount() - ( (GetVertexCount()>1)?((GetLastVertex() == GetFirstVertex())?1:0):0);
	}
	
	Point GetStartPoint() {
		// TODO - Check the ordering on this one ....
		if(GetFirstVertex().compareTo(GetLastVertex())<=0)
			return GetFirstVertex();
		else
			return GetLastVertex();
	}
	
	Point GetEndPoint() { 
		// TODO - Check the ordering on this one ....
		if(GetFirstVertex().compareTo(GetLastVertex())<=0)
			return GetLastVertex();
		else
			return GetFirstVertex();
	}
	Point GetLastVertex() { return _vertex_array.get(_vertex_array.size()-1); };
	Point GetFirstVertex() { return _vertex_array.get(0); };
	public int GetVertexCount() { return _vertex_array.size(); };
	public ArrayList<Point> GetVertexArray() { return _vertex_array; };
	public Point GetVertexAt(int n) { return (n<GetVertexCount())?_vertex_array.get(n):null; }


	@Override
	protected Polyline clone() {
		// TODO - Finnish clone method...
		Polyline polyline=new Polyline();
	
		polyline.SetClosed(false);
		polyline._length = 0.0f;
		polyline._p_bounding_box = null;

		for (int i=0; i<GetVertexCount();i++) 
			polyline.AddVertex(GetVertexAt(i).clone());

		polyline.CalculateFirstAndLastPoint();
		
		return polyline;
	}


	/***
	* @desc adds a vertex to the polyline
	* @param point representing a vertex
	* @note will be stored a pointer to the point passed on
	*       a vertex list, but this class DELETES the point
	*/
	boolean AddVertex(Point vertex, short flag, int pos)
	{
		if (vertex!=null) {

			// check if in case of middle insertion the position given is
			// at the begin or at the end of the polyline
			if(CheckFlag(flag, POLYLINE_FLAG.AT_MIDDLE))  {
				if (pos>=_vertex_array.size())
					flag = POLYLINE_FLAG.AT_END;
				
				if (pos==0)
					flag = POLYLINE_FLAG.AT_BEGIN;
			}

			if (_vertex_array.size() >0) {
				
				// calculates the length of the line if vertex added at begin or end
				if (CheckFlag(flag, POLYLINE_FLAG.AT_END)||CheckFlag(flag, POLYLINE_FLAG.AT_BEGIN)) {
					Point previous = CheckFlag(flag, POLYLINE_FLAG.AT_END)?GetLastVertex():_vertex_array.get(0);
				
					if (previous!=null)
						_length += previous.DistanceTo(vertex);
				}

				if (CheckFlag(flag, POLYLINE_FLAG.AT_MIDDLE)) {
					Point previous = _vertex_array.get(pos-1);
					Point next = _vertex_array.get(pos);

					if (previous!=null && next!=null) {
						_length -= previous.DistanceTo(next);
						_length += vertex.DistanceTo(previous);
						_length += vertex.DistanceTo(next);
					}
				}

			}
			vertex.SetOwnerEntity(this);
			
			if(CheckFlag(flag, POLYLINE_FLAG.AT_END))
				_vertex_array.add(vertex);
			
			if(CheckFlag(flag, POLYLINE_FLAG.AT_BEGIN))
				_vertex_array.add(0, vertex);

			if(CheckFlag(flag, POLYLINE_FLAG.AT_MIDDLE))
				_vertex_array.add(pos,vertex);
			
			// when a vertex is added it will be needed to recalculate the 
			// bounding box
			_p_bounding_box=null;
			return true;
		}
		
		return false;
	}

	/***
	* @desc calculate the bounding box of this polyline
	*/
	@Override
	public void CalculateBoundingBox()
	{
		
		// if bounding box already exists it will not calculate
		if (_p_bounding_box!=null)
			return;
		
		// a bounding box must have more than two vertices
		if (_vertex_array.size()<2) 
			return;
		
		double min_x=0.0f, max_x=0.0f, x;  
		double min_y=0.0f, max_y=0.0f, y;  
		Point vertex;
		boolean first=true;  
		
		for (int i=0; i<_vertex_array.size(); i++)
		{
			vertex = _vertex_array.get(i);    
			
			x=vertex.GetX();
			y=vertex.GetY();
			
			if (first) {
				first = false;
				min_x=max_x=x;
				min_y=max_y=y;
			} else {      
				min_x=Math.min(min_x,x);
				min_y=Math.min(min_y,y);
				
				max_x=Math.max(max_x,x);
				max_y=Math.max(max_y,y);
			}    
		}
		
			
		Point start_point=new Point(min_x, min_y);
		
		_p_bounding_box = new Box2D(
			start_point, 
			new Vector(start_point, max_x-min_x, 0), 
			new Vector(start_point, 0, max_y-min_y) 
			);
	}



	/***
	* @desc calculate the first and last point of this polyline
	*/
	void CalculateFirstAndLastPoint()
	{
	    // if there are only one vertex it is not a polyline
	    if (_vertex_array.size()<2){     
			if (_vertex_array.size()>0){ 
				SetFirstPoint(_vertex_array.get(0));		
				SetLastPoint(_vertex_array.get(0));		
			}
	        return;
		}
		
	    // let's see first if the polyline is closed, ie. is a polygon	
	    _closed |= GetFirstVertex() == GetLastVertex();
		
	    if (_closed) {
			// the case of the closed polyline
			// here we're going to find the first point by seeing them all		
			Point vertex=null, first_vertex=_vertex_array.get(0);
			
			
			for (int i=1; i<_vertex_array.size(); i++)
			{
				vertex = _vertex_array.get(i);
				
				// TODO - Check ordering for !Point2D::Ordered(first_vertex,vertex)
				if (first_vertex.compareTo(vertex)>0)				
					first_vertex = vertex;
				
			}
			
			if (first_vertex!=null) {
				SetFirstPoint(first_vertex);
				// in a closed polyline the first and last point coincide
				SetLastPoint(vertex);
			}
			
	    } else {
	        // case the polyline is not closed, just consider the
	        // extremities
			SetFirstPoint(GetFirstVertex());
			SetLastPoint(GetLastVertex());
	    }
	}

	/***
	* @return true if extremities of polylines are coincident, false otherwise
	*/
	// bool Polyline2D::CoincidentExtremities(Polyline2D *polyline1, Polyline2D *polyline2)
	// {
//	 	Point2D *p1f = polyline1.GetFirstVertex();
//	 	Point2D *p2f = polyline2.GetFirstVertex();
//	 	Point2D *p1l = polyline1.GetLastVertex();
//	 	Point2D *p2l = polyline2.GetLastVertex();
	// 	
//	 	return (((*p1f)==(*p2f) && (*p1l)==(p2l)) || ((*p1f)==(*p2l) && (*p1l)==(p2f)));
	// }

	/***
	* @param p  point
	* @return pointer to the opposite point of the one passed 
	*         or null if point passed don't belong to polyline
	*/
	Point GetOtherPoint(Point p)
	{
		if (_vertex_array.size()<2)
			return null;
		
		if (p==GetFirstVertex())
			return GetLastVertex();
		
		if (p==GetLastVertex())
			return GetFirstVertex();
		
		return null;
	}

	Point PointAt(double x, double y)
	{
		if (x == GetFirstVertex().GetX() && y == GetFirstVertex().GetY()) {
			return GetFirstVertex();
		}
		
		if (x == GetLastVertex().GetX() && y == GetLastVertex().GetY()) {
			return GetLastVertex();
		}
		
		return null;
	}

	/***
	* @desc this method adds a polyline to this one
	* @param polyline to add
	*/
	void AddPolyline(Polyline pl)
	{
	    // if pl is not defined does nothing
	    if (pl==null)
	        return;
		
	    // forces first and last point calculations
	    pl.CalculateFirstAndLastPoint();
	    CalculateFirstAndLastPoint();
		
	    // simple cases
	    if (_vertex_array.size()==0) {
			ExecuteJoin(pl);
			return;
	    }
		
	    if (pl._vertex_array.size()==0)
	        return;
		
	    Point this_polyline_first_vertex = GetFirstVertex();
	    Point other_polyline_first_vertex = pl.GetFirstVertex();
		
	    Point this_polyline_last_vertex = GetLastVertex();
		Point other_polyline_last_vertex = pl.GetLastVertex();
		
	    // NOTE: the case of coincident extremities is treated by 'ExecuteJoin()'
		
	    if ( this_polyline_last_vertex==other_polyline_first_vertex){
	        ExecuteJoin(pl, (short) (POLYLINE_FLAG.NORMAL_ORDER | POLYLINE_FLAG.AT_END));		
	        return;
	    }
	    
	    if ( this_polyline_first_vertex== other_polyline_last_vertex){
	        ExecuteJoin(pl, (short) (POLYLINE_FLAG.NORMAL_ORDER | POLYLINE_FLAG.AT_BEGIN));
	        return;
	    }
		
	    if ( this_polyline_last_vertex==other_polyline_last_vertex ){
	        ExecuteJoin(pl, (short) (POLYLINE_FLAG.INVERSE_ORDER | POLYLINE_FLAG.AT_END));		
	        return;
	    }
		
	    if ( this_polyline_first_vertex==other_polyline_first_vertex){
	        ExecuteJoin(pl, (short) (POLYLINE_FLAG.INVERSE_ORDER | POLYLINE_FLAG.AT_BEGIN));
	        return;
	    }
	}


	/***
	* @return a pointer to a newly created polyline representing the join of the
	*         polylines passed in teh argment
	*/
	public static Polyline Join(Polyline polyline1, Polyline polyline2)
	{
	    if (polyline1==null || polyline2==null)
	        return null;
		
	    Polyline pl = new Polyline();
		
	    pl.AddPolyline(polyline1);
	    pl.AddPolyline(polyline2);
		
	    return pl;
	}

	void ExecuteJoin(Polyline pl) {
		ExecuteJoin(pl, (short)(POLYLINE_FLAG.NORMAL_ORDER | POLYLINE_FLAG.AT_END));
	}
	
	void ExecuteJoin(Polyline pl, short flags)
	{
	    Point p;
	    int i = 0, count = pl.GetVertexCount();	
		
	    
	    // skips the first vertex, if necessary    
	    if (pl.GetVertexCount()>0 && GetVertexCount()>0) {        
	        if (CheckFlag(flags, POLYLINE_FLAG.INVERSE_ORDER)) {            
	            if ( (pl.GetLastVertex() == GetLastVertex()) || (pl.GetFirstVertex()) == GetFirstVertex())
	                i++;
	        } else {
	            if ( (pl.GetFirstVertex() == GetLastVertex()) || (pl.GetLastVertex()) == GetFirstVertex())
	                i++;
	        }
	    }
		
	    for( ; i<count; i++)
	        if (CheckFlag(flags, POLYLINE_FLAG.AT_END)) {
	            p = pl._vertex_array.get(CheckFlag(flags, POLYLINE_FLAG.INVERSE_ORDER)?count-i-1:i).clone();        
	            AddVertex(p, POLYLINE_FLAG.AT_END);
	        } else 
	            if (CheckFlag(flags, POLYLINE_FLAG.AT_BEGIN)){
	                p = pl._vertex_array.get(CheckFlag(flags, POLYLINE_FLAG.INVERSE_ORDER)?i:count-i-1).clone();                    
	                AddVertex(p, POLYLINE_FLAG.AT_BEGIN);
	            }
				
				// recalculates the first and last point
				CalculateFirstAndLastPoint();
				
	}

	/***
	* @desc perform rounding in polyline vertices coordinates
	*/
	void PerformRounding(double gamma)
	{
		Point p;

		for (int i=0; i<_vertex_array.size();i++) {
			p = _vertex_array.get(i);
			p.PerformRounding(gamma);
		}
	}

	boolean IsClosed() { return _closed; } ;
	double GetLength() { return _length; } ;
	
	void SetClosed(boolean closed) { _closed = closed; }
	
	/***
	* @desc removes unnecessary vertices from polyline, e.g. any number of collinear vertices
	*/
	void Simplify()
	{
		int vertex_count = _vertex_array.size();	

		// simplify just makes sense when the polyline have more than two vertices
		if (vertex_count>2) {

			int p1_index = IsClosed()?vertex_count-2:0;
			int p2_index = p1_index+1;

			Point point;
			
			for (int p3_index=IsClosed()?0:2;p3_index<vertex_count; p3_index++) {
				
				// see if three points are collinear
				if (_vertex_array.get(p3_index).Collinear(_vertex_array.get(p1_index),_vertex_array.get(p2_index))) {

					// in case they were, remove the middle one				
					point = _vertex_array.get(p2_index);				
					_vertex_array.remove(p2_index);
					//DELETE_OBJECT(point);
					vertex_count--;

					if (p2_index<p3_index) p3_index--;
	                if (p2_index<p1_index) p1_index--;
	                
				} else {
					p1_index = p2_index;
				}
				p2_index = p3_index;
			}
		}
	}

	/***
	* @desc performs vertext reduction, according to a given tolerance
	* @note executes in O(n) time
	*/
	void VertexReduction(double tolerance)
	{

		double squared_tolerance = Math.sqrt(tolerance);

		Point current_vertex;
		Point previous_vertex=null;
		int n, count = _vertex_array.size();

		// checks if polyline is not empty
		if (count>0) {
			previous_vertex = _vertex_array.get(count-1);
		
			// starts from second vertex
			for (int i=1; i<count-1; i++) {
				n = count-i-1;
				current_vertex = _vertex_array.get(n);

				// see if consecutive points are clustered too closely
				if (previous_vertex.SquareDistanceTo(current_vertex)<squared_tolerance) {
					// remove unnecessary vertices
					_vertex_array.remove(n);
					//DELETE_OBJECT(current_vertex);
				} else 
					previous_vertex = current_vertex;
			}
		}
	}

	/***
	* @desc performs a polyline simplification, using the Douglas-Peucker algorithm
	* @see David Douglas and Thomas Peucker "Algorithms for the reduction of the number 
	*    of points required to represent a digitized line or its caricature", The 
	*    Canadian Cartographer 10(2), pp. 112-122 (1973)
	*/
	void DouglasPeucker(double tolerance, int start_index, int end_index)
	{
		// see if there are vertices in between that can be used in simplification
		if (start_index>=end_index)
			// nothing else to simplify: stop recursion
			return;

		if (end_index>=_vertex_array.size()) 
			// wrong indices
			return;

		// lets start by finding the farthest point from line segment
		double maximum_distance=0.0f;
		double current_distance=0.0f;
		int maximum_index = start_index+1;

		// sweep all points...
		for (int i=start_index+1; i<end_index; i++) {
			current_distance = Line.Distance(
				GetVertexAt(start_index),
				GetVertexAt(end_index), 
				GetVertexAt(i));

			if (current_distance>maximum_distance) {
				maximum_distance = current_distance;
				maximum_index = i;
			}
		}

		// and here we have the index of the farthest point and its distance to
		// selected line segment
		
		// now lets see if the distance is bigger than tolerance
		if (maximum_distance>tolerance) {
			
			// in this case lets split the polyline at the farthest vertex
			DouglasPeucker(tolerance, start_index, maximum_index);
			DouglasPeucker(tolerance, maximum_index, end_index);
			
			// flag the vertex to avoid later removal
			GetVertexAt(maximum_index).SetFlag(FLAG.KEEP);

		}

	}

	/***
	* @desc performs the polyline simplification, with <tolerance>
	*/
	public void Simplify(double tolerance)
	{	

		DouglasPeucker(tolerance, 0, _vertex_array.size()-1);

		Point vertex;

		// flags first and last vertex
		vertex = GetFirstVertex();
		if (vertex!=null) vertex.SetFlag(FLAG.KEEP);

		vertex = GetLastVertex();
		if (vertex!=null) vertex.SetFlag(FLAG.KEEP);

		// if the vertex array have more than two vertices
		if (_vertex_array.size()>2) 
			// removes unflagged vertices
			for (int i=_vertex_array.size()-2; i>0;i--) 
				//--i;
				if (!_vertex_array.get(i).HasFlag(FLAG.KEEP)) {
					vertex = _vertex_array.get(i);
					_vertex_array.remove(i); 
					//DELETE_OBJECT(vertex);
				}
	}

	/***
	* @return true if polylines have a common vertex, false otherwise
	* @param p1, p2 pointers to polylines
	* @param i, j pointers to the indices of first common vertices found
	*/
	// old pointer solution - HaveCommonVertex(Polyline p1, Polyline p2, Integer i, Integer j)
	// TODO - Check if using Integers solves the pointer issues !
	int [] HaveCommonVertex(Polyline p1, Polyline p2)
	{
		// check if indices are defined
		Point v;
		Integer j;
		// sweeps all vertices in p1
		for (int i=0; i<p1.VertexCountInPolyline();i++) {
			v = p1.GetVertexAt(i);
			// see if current vertex of p1 is a vertex in p2
			if ( (j=p2.HasVertex(v))!=null) {
				// if it is we have a common vertex
				int [] res={i,j.intValue()};
				return res;		
			}
		}

		return null;
	}


	/***
	* @param p pointer to a point
	* @param index index of point if it is a vertex of this polyline
	* @return true if p is a vertex of this polyline, false otherwise
	*/
	Integer HasVertex(Point p)
	{
		Point v;
		// sweeps all vertices of polyline
		for (int i=0; i<VertexCountInPolyline();i++) {
			v = GetVertexAt(i);
			// see if point <p> is coincident with current vertex 
			if (p==v) {
				// in case index have been passed, updates its value
				return i;
			}
		}

		return null;
	}


	/***
	* @return pointer to vertex in position <index>+<offset> 
	* @note this works as a circular buffer of vertices, when reached 
	*       the end it jumps to the beggining and vice-versa
	*/
	Point GetVertexAt(int index, short offset)
	{
	 	if (GetVertexCount()<1)
	 		return null;
	 
	 	if (offset<0) {
	 		if (index==0) 
	 			index=VertexCountInPolyline();
	 	} 
	 	
	 	index=(index+offset)%(VertexCountInPolyline());
		
	 	return GetVertexAt(index);
	}


	/***
	* @desc performs a scale transformation on this polyline
	* @param x component of the scale
	* @param y component of the scale
	*/
	void Scale(double sx, double sy)
	{
		for (int i=0; i<_vertex_array.size();i++)
			_vertex_array.get(i).Scale(sx,sy);

		// in case of unioform scale
		if (sx==sy)
			_length *= sx;
		else {
			// lets re-calculate length
			RecalculateLength();
		}
	}

	/***
	* @desc performs a translation transformation on this polyline
	* @param x component of the translation
	* @param y component of the translation
	*/
	void Translate(double dx, double dy)
	{
		for (int i=0; i<_vertex_array.size();i++)
			_vertex_array.get(i).Translate(dx,dy);
	}

	/***
	* @desc performs a rotation transformation on this polyline
	* @param theta component of the translation
	*/
	void Rotate(double theta)
	{
		for (int i=0; i<_vertex_array.size();i++)
			_vertex_array.get(i).Rotate(theta);
	}

	/***
	* @desc recalculates the length of the polyline
	*/
	void RecalculateLength()
	{
		_length = 0;
		Point current, previous=null; 

		// sweeps all points of polyline
		for (int i=0;i<_vertex_array.size();i++){
			current = _vertex_array.get(i);
			if (previous!=null)
				_length+=current.DistanceTo(previous);
			previous = current;
		}
	}
	
	

	/***
	* @return string containing polylines coordinates
	*/
	public String AsString(boolean svg_format, String stroke_color, String fill_color)
	{
		Point vertex;
		String result = "";

		// checks format
		if (svg_format) {
			// adds svg polyline entity tag
			result+="<polyline fill=\""+fill_color+"\" stroke=\""+stroke_color+"\" stroke-width=\"0.005\"\n";
			result+="points=\"";
		}

		for (int i=0; i<GetVertexCount(); i++){
			vertex = GetVertexAt(i);
			NumberFormat nf=NumberFormat.getInstance(Locale.US);
			 if(svg_format)
				 result += nf.format(vertex.GetX())+","+nf.format(vertex.GetY())+" ";
			else
				result += nf.format(vertex.GetX())+'\t'+nf.format(vertex.GetY())+'\t';
		}		

		// checks format
		if (svg_format) {
			// adds svg polyline entity tag end symbol			
			result+="\"/>";
		}

		// adds the new line indicating the end of the polyline
		result += "\n";

		return result;		
	}

	public String AsVRML()
	{
		String s="Shape {\n\tgeometry"; 
		s+= " IndexedLineSet {\n";
		s+= "\t\tcoord Coordinate { point [ \n";
		for (int i=0; i<_vertex_array.size();i++) {
			Point vertex = _vertex_array.get(i);
			if (vertex!=null) {
				s+=String.format("\t\t\t %d %d %d", vertex.GetX(), vertex.GetY(), 0);
				if (i<_vertex_array.size()-1) 
					s+=",";
				else
					s+=" ]\n\t\t}";
				s+="\n";
			}
		}
		s+="\t\tcoordIndex [";
		for (int n=0; n<_vertex_array.size();n++){
			if (_vertex_array.get(n)!=null)
				s+=String.format(" %d", n);
		}
		s+=" ]\n";
		s+="\t}\n}\n\n";
		return s;
	}
	/***
	* @return number of time polyline intersects itself
	*/
	int Intersections()
	{
		int i, j;
		int counter=0;
		Line line_i, line_j;

		for (i=0; i<GetVertexCount()-2;i++){
			line_i = new Line(GetVertexAt(i), GetVertexAt(i+1));
			for (j=i+1; j<GetVertexCount()-1;j++) {
				line_j = new Line(GetVertexAt(j), GetVertexAt(j+1));			

				if (line_i.IntersectsProper(line_j))
					counter++;

				line_j=null;
			}
			line_i=null;
		}

		return counter;
	}

	/***
	* @returns the shorter distance between given point and this polyline
	*/
	double DistanceTo(Point point, Point nearest_point, int pred, int succ)
	{

		double shorter_distance = -1.0;
		double distance;

		Point  previous_vertex = GetFirstVertex();
		Point  current_vertex;
		Point auxiliar_point=new Point();

		for (int i=1; i<GetVertexCount(); i++) {
			current_vertex = GetVertexAt(i);				
			distance = Line.Distance(previous_vertex, current_vertex, point, (nearest_point!=null)?auxiliar_point:null);
			if (distance<=shorter_distance || shorter_distance<0){
				if (nearest_point!=null) nearest_point=auxiliar_point;
				if (pred>0) pred = i-1;
				if (succ>0) succ = i;
				shorter_distance = distance;
			}

			previous_vertex = current_vertex;		
		}

		return shorter_distance;
	}

	/***
	* @return a point at a distance of <lenght> in the extension of one of the extremities
	* @see file ../../Documentation/line_extension.tif
	*
	* @note do not forget to delete returned point elsewhere
	*/
	Point Extension(double length, short flag)
	{
		Point u, v;

		// if no valid flag given
		if (!CheckFlag(flag, POLYLINE_FLAG.AT_END) && !CheckFlag(flag, POLYLINE_FLAG.AT_BEGIN))
			return null;

		// if don't exist more than two vertices
		if (GetVertexCount()<2)
			return null;

		if (CheckFlag(flag, POLYLINE_FLAG.AT_END)) {
			u = GetLastVertex();
			v = GetVertexAt(GetVertexCount()-2);
		} else {
			u = GetFirstVertex();
			v = GetVertexAt(1);
		}

		double x = u.GetX()-v.GetX();
		double y = u.GetY()-v.GetY();

		double l = Math.sqrt(Math.sqrt(x)+Math.sqrt(y));

		/* to avoid div/0 */
		if (l==0.0f)
			return null;

		double ext_x = x*length/l;
		double ext_y = y*length/l;

		return new Point(u.GetX()+ext_x, u.GetY()+ext_y);
	}

	boolean  Intersects(Line line) {
		return Intersects(line,false);
	}
	
	/***
	* @desc indicates if polyline intersects line
	* @param proper indicates if is a proper intersection or not
	* @note a proper intersection occurs when two segments intersects at a point 
	*      interior to both
	*/
	boolean Intersects(Line line, boolean proper)
	{
		Point previous_vertex = null;
		Point current_vertex = null;

		boolean intersects;

//		bool (*fn) (Line2D*);
	//
//		if (proper)
//			fn = &Line2D::IntersectsProper;
//		else
//			fn = &Line2D::Intersects;

		// sweeps all vertices in polyline
		for (int i=0; i<VertexCountInPolyline();i++) {
			current_vertex = GetVertexAt(i);
			if (previous_vertex!=null) {
				Line aux_line=new Line(previous_vertex, current_vertex);

//				if (fn(&line))
//					return true;
				
				if (proper)
					intersects = aux_line.IntersectsProper(line);
				else
					intersects = aux_line.Intersects(line);

				if (intersects)
					return true;
			}
			previous_vertex = current_vertex;
		}

		return false;
	}

	/***
	* @desc indicates if <polyline> intersects this polyline
	*/
	boolean Intersects(Polyline polyline)
	{
		if (polyline==null) return false;
		
		boolean intersects=false;
		Line edge;

		Point current_vertex = null;	
		Point previous_vertex = polyline.GetFirstVertex();

		for (int i=1; i<polyline.GetVertexCount() && !intersects;i++) {
			current_vertex = polyline.GetVertexAt(i);
			edge = new Line(previous_vertex, current_vertex);

			intersects |= Intersects(edge);
			
			previous_vertex = current_vertex;
			edge=null;
		}

		return intersects;
	}


	/***
	* @return list of intersection points between <line> and this polyline
	* @note do not forget to delete the array and the intersection points elsewhere
	*/
	ArrayList<Point> IntersectionPoints(Line line)//, int [] indexes)
	{
		ArrayList<Point>intersections = new ArrayList<Point>();

		Point previous_vertex = null;
		Point current_vertex = null;

		// sweeps all vertices in polyline
		for (int i=0; i<VertexCountInPolyline();i++) {
			current_vertex = GetVertexAt(i);

			if (previous_vertex!=null) {
				Line aux_line=new Line(previous_vertex, current_vertex);
				if (aux_line.Intersects(line)){
					Point intersection = aux_line.IntersectionPoint(line);				
					
					if (intersection!=null) {
						intersections.add(intersection);
						//if (indexes)
						//	indexes.Add(i);
					}
				}
			}

			previous_vertex = current_vertex;
		}

		return intersections;
	}

	/***
	* @return a string with the text entry of this polygon on the output file
	*/
	// wxString Polyline2D::GetGeometricFeatures()
	// {
//	 	wxString result="";	
//	 	size_t i;
	// 
//	 	float * fvector = GetGeometricFeaturesVector();
	// 	
//	 	// writes the dimension of the vector and the id of the polygon
//	 	result += wxString::Format("%d\n%d", (int) fvector[0], GetID());
	// 
//	 	// writes the vector on the string
//	 	for (i=1;i<=fvector	[0];i++) 
//	 		result+=wxString::Format(" %f", fvector[i]);
	// 
//	 	FREE(fvector);
	// 
//	 	return result;
	// }
	// 
	/***
	* @return a vector with the features of this polyline
	* @note do not forget to delete returned pointer
	*/
	// float * Polyline2D::GetGeometricFeaturesVector()
	// {
//	 	size_t i, vector_length;
//	 	Point2D * vertex;	
//	 	float *fvector, *fresult;
//	 	Geometry g;
	// 
//	 	// creates a scribble with the polyline
//	 	g.newScribble();
//	 	g.newStroke();
//	 	for (i=0; i<VERTEX_COUNT_IN_POLYLINE(this);i++) {
//	 		vertex = GetVertexAt(i);
//	 		g.addPoint(vertex.GetX(), vertex.GetY());
//	 	}
	// 
//	 	// Add the first vertex at the end of the scribble
//	 	vertex = GetVertexAt(0);
//	 	g.addPoint(vertex.GetX(), vertex.GetY());
	// 
//	 	// retrieves the geometric features of thatt scribble
//	 	// parameter must be true in order to be according SBR
//	 	fvector = g.geometricFeatures(true);
//	 	vector_length = fvector[0]+1;
//	 	fresult= (float *)malloc(vector_length*sizeof(float));	
//	 	memcpy(fresult, fvector, (fvector[0]+1)*sizeof(float));
	// 
//	 	return fresult;
	// }

	/***
	* @return polyline representing the convex-hull of this polyline
	*/
	// Polygon2D * Polyline2D::GetConvexHull()
	// {
//	 	CIScribble * scribble = new CIScribble();
//	 	CIStroke * stroke = new CIStroke();
//	 	Point2D *vertex;
//	 	size_t i;
	// 
//	 	for (i=0; i<VERTEX_COUNT_IN_POLYLINE(this);i++) {
//	 		vertex = GetVertexAt(i);
//	 		stroke.addPoint(vertex.GetX(), vertex.GetY());
//	 	}
	// 
//	 	scribble.addStroke(stroke);
	// 
//	 	CIPolygon * polygon = scribble.convexHull();
	// 	
//	 	Polygon2D * polygon2D = new Polygon2D(polygon);
//	 	polygon2D.SetConvex(true);
	// 
//	 	DELETE_OBJECT(scribble);
	// 
//	 	return polygon2D;
	// }
}
