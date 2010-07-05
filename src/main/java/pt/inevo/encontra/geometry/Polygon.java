package pt.inevo.encontra.geometry;


public class Polygon extends Polyline{
	
	double _area;
	
	/***
	* @desc constructor 
	*/
	public Polygon()
	{
		super();
	    Initialize();
	}

	
	@Override
	protected Polygon clone() {
		// TODO - Finnish clone method...
		Polygon polygon=new Polygon();
	
		for (int i=0;i<GetVertexCount();i++) {
			polygon.AddVertex(GetVertexAt(i).clone());
		}
		
		return polygon;
	}


	/***
	* @desc initializes the class members
	*/
	void Initialize()
	{
		_closed=true; 
	    _area=0.0f;    
		
	// 
//	 	_is_convex = false;
	}


	/***
	* @desc macro to detect if a ray from <ray_origin> along a test axis,
	*     in this case we use a +X axis, crosses the edge defined by the
	*     two endpoints <endpoint_1> and <endpoint_2>
	*
	* @see O'Rourke, Joseph, "Computational Geometry in C, 2nd Ed.", 
	*    pp.243, figure 7.8
	*
	*/
	public boolean RayIsCrossingEdge(Point ray_origin, Point endpoint_1, Point endpoint_2){
		return ((
			(endpoint_1.GetY() > ray_origin.GetY() && 
				endpoint_2.GetY() <= ray_origin.GetY()
			) 
			|| 
			(
				endpoint_2.GetY() > ray_origin.GetY() &&
				endpoint_1.GetY() <= ray_origin.GetY()
			) 
		) && 
		(endpoint_1.GetX()+(ray_origin.GetY()-endpoint_1.GetY())/ 
			(endpoint_2.GetY()-endpoint_1.GetY())*(endpoint_2.GetX()-endpoint_1.GetX()) < ray_origin.GetX())
	);
	}


	/***
	* @returns true if this is a polygoon (closed polyline) and <point> lays inside 
	*
	* @see Haines, Eric, "Point in Polygon Strategies", Graphic Gems IV, ed Paul Heckbert,
	*    Academic Press, pp.24-46, 1994
	*
	* @see O'Rourke, Joseph, "Computational Geometry in C, 2nd Ed.", pp.239-245
	*/
	public boolean Contains(Point point, boolean strict)
	{
		// in case of an open polyline the point cannot be inside
		if (!IsClosed()) return false;

		// first lets see if point is inside the polygon bounding box
	    Box2D  bb = GetBoundingBox();
	    
	    if (bb==null) {
	        //log.error("Unable to determine bounding box for \'Point in Polygon\' check.");
	        return false;
	    }
	    
		if (!bb.Contains(point)) 
			// if point not inside of bounding box, then we can be sure
			// that point are not inside the polygon
			return false;

		boolean inside = false;
		//size_t vertex_count = _vertex_array.GetCount();
		int vertex_count = VertexCountInPolyline();

		if (vertex_count>0) {
			
			Point current_vertex;
			Point previous_vertex = _vertex_array.get(VertexCountInPolyline()-1);
			
			for (int i=0; i<vertex_count;i++) {
				current_vertex = _vertex_array.get(i);
				
				// let's see if point is lays on the edge
				if (point.Between(current_vertex, previous_vertex)) {
					// in this case point is considered INSIDE the polygon
	                // or not, depending on strict value
					return !strict;
				}

				if (RayIsCrossingEdge(point, previous_vertex, current_vertex))
					inside = !inside;

				previous_vertex = current_vertex;
			}
		}

		return inside;
	}

    public boolean Contains(Polyline polyline) {
    	return Contains(polyline,false);
    }
    
	public boolean Contains(Point point) {
		return Contains(point,false);
	}
	
	/***
	* @returns true if this is a polygoon (closed polyline) and <polyline> lay inside 
	* @note this algorithm runs in O(n*m) where n is the number of vertices of this
	*     polygon and m is the number of vertices of the compared polyline
	*/
	public boolean Contains(Polyline polyline, boolean strict)
	{
		Point current_inner_vertex = null;
		Point previous_inner_vertex = null;
		Point current_outter_vertex = null;
		Point previous_outter_vertex = null;
		Point p;
	    int i;
	    Integer outter_vertex_index;
		
		// first lets see if all vertices in polyline lay inside or in the border of the polygon
		for (i=0; i<polyline.VertexCountInPolyline();i++) {
			current_inner_vertex = polyline.GetVertexAt(i);

			// if any vertex are outside the polygon the the polyline 
			// ar not inside the polygon
			if (!Contains(current_inner_vertex))
				return false;
		}

	    // now lets see if all vertices of polyline are conicident with vertices in this polygon
		for (i=0; i<polyline.VertexCountInPolyline()+1;i++) {
			current_inner_vertex = polyline.GetVertexAt(i%polyline.VertexCountInPolyline());
	        
			// in case any vertex not coincident then it lays inside
			// because we have already tested this before
			if ((outter_vertex_index=HasVertex(current_inner_vertex))==null)
	            return true;

			current_outter_vertex= GetVertexAt(outter_vertex_index);

	        //in case not in first point
	        if (previous_outter_vertex!=null && previous_inner_vertex!=null) {
	            // if coincident vertices not consecutive in this polygon
				if (ConsecutiveVertices(current_outter_vertex,previous_outter_vertex)!=null)
				{	
					// lets see if middle point of this vertex lays inside polygon
					// start by calculating the middle point of the line segment 
					// with endpoints coincident with the vertices

					// first calculate the displacement of middle 
	                double x = (current_inner_vertex.GetX()-previous_inner_vertex.GetX())/2;
	                double y = (current_inner_vertex.GetY()-previous_inner_vertex.GetY())/2;                
					
					x+=previous_inner_vertex.GetX();
					y+=previous_inner_vertex.GetY();

	                p = new Point(x,y);

					// and then test if such point is contained inside the polygon
	                if (!Contains(p, strict)) {
	                    p=null;
	                    return false;
	                }
	                p=null;
	            }
	        }
	        
	        previous_outter_vertex = current_outter_vertex;
			previous_inner_vertex = current_inner_vertex;
	    }
		 
	    
		return true;
	}

	/***
	* @returns true if <line> lays inside this polygon
	*/
	public boolean Contains(Line line, boolean strict)
	{
		if (line==null) return false;

		Point p1 = line.GetStartPoint();
		Point p2 = line.GetEndPoint();

		if (Contains(p1, strict) && Contains(p2, strict)){
			int i;
			Line edge;
			boolean intersects = false;
			Point current_vertex, previous_vertex = GetFirstVertex();

			for(i=1;i<VertexCountInPolyline() && !intersects;i++){
				current_vertex = GetVertexAt(i);
				edge = new Line(previous_vertex, current_vertex);
				
				intersects |= line.Intersects(edge);

				edge=null;
				previous_vertex = current_vertex;
			}	

			return !intersects;
		}

		return false;
	}

	/***
	* @return true if <polygon> is disjoint of this polygon
	* @note this method only works weel if no line segment 
	*     intersection exists, i.e.,  if intersection removal 
	*     have already been made
	*/
	boolean Disjoint(Polygon polygon)
	{
		// checks if any point of given polygon lays inside (or in 
		// the border) of this polygon
		for (int i=0; i<polygon.VertexCountInPolyline(); i++)
			if (Contains(polygon.GetVertexAt(i)))
				return false;

		// if all points of given polygon are outside this polygon
		// then they are disjoint.
		// Note that no line segment intersection exists.
		return true;
	}

	public static boolean IsAdjacent( Polygon p1, Polygon p2) {
		return IsAdjacent(p1, p2,false);
	}
	
	public boolean IsAdjacent(Polygon p) {
		return IsAdjacent(p,false);
	}
	
	public boolean IsAdjacent(Polygon p, boolean strict) { 
		return IsAdjacent(this, p, strict); 
	}

	
	/****
	* @return true if <p1> is adjacent to <p2>, false otherwise 
	* @param p1, p2 pointers to polylines
	* @param strict true quest for strictly adjacent polygons, if false
	*      considers adjacent two polygons that share only one vertice
	* @note two polygons are strictly adjacent if they share at least an edge
	*/

	public static boolean IsAdjacent(Polygon p1, Polygon p2, boolean strict)
	{

		int i, j;
		Point v1, v2;
		Point previous_v1 = p1.GetLastVertex();
		Point previous_v2 = p2.GetLastVertex();
		Line line1, line2;

		for (i=0; i<p1.VertexCountInPolyline();i++) {
			v1 = p1.GetVertexAt(i);

			line1 = new Line(previous_v1, v1);
			
			for (j=0; j<p2.VertexCountInPolyline();j++) {
				v2 = p2.GetVertexAt(j);
				line2 = new Line(previous_v2, v2);

				if ((strict?Line.StrictOverlapping(line1, line2):Line.Overlapping(line1, line2))) {
					line1=null;
					line2=null;
					return true;
				}
					
				
				line2=null;
				previous_v2 = v2;
			}

			line1=null;
			previous_v1 = v1;
		}

		return false;

		
	}

	/****
	* @return {i,j,lenght} if <p1> have single adjacency with <p2>, false otherwise 
	* @param p1, p2 pointers to polygons
	* @param i, j pointers to the indices of first overlapped edges
	* @param length pointer to the number of overlapped edges. Signal represents
	*        the order of adjacent edges in p2 (+/-)
	* @note two polylines have single adjacency if they share at least one edge 
	*       and all edges they share are consecutive in both polygons
	*/
	Object [] SingleAdjacency(Polygon p1, Polygon p2) // old pointer solution, int i, int j, long length)
	{
		int i, j; 
		long length;
		int n1, n2;
		length = 0;

		// in case p1 and p2 have not common vertices then they cannot have single adjacency	
		int [] n1_n2=HaveCommonVertex(p1,p2);
		if (n1_n2==null)
			return null;
		n1=n1_n2[0];
		n2=n1_n2[1];
		i=n1;
		j=n2;
		length++;

		// in case n1 is the last vertex in p1 it is not a simple adjacency
		if (n1==p1.VertexCountInPolyline())
			return null;

		boolean adjacency=true;
		
		Integer index;
		int triggering_counter = 0;
		short increment = 0;
		Point v1, v2;

		// lets calculate the increment
		// note that the order of this conditions is relevant,
		// because we need to check first the cases on which 
		// n1 is increasing
		if (p1.GetVertexAt(n1,(short)1)==p2.GetVertexAt(n2,(short)1)) {
			increment = 1;
		} else {
			if (p1.GetVertexAt(n1,(short)1)==p2.GetVertexAt(n2,(short)-1)) {
				increment = -1;
			} else {
				if (p1.GetVertexAt(n1,(short)-1)==p2.GetVertexAt(n2,(short)-1)) {
					increment = 1;
				} else {
					if (p1.GetVertexAt(n1,(short)-1)==p2.GetVertexAt(n2,(short)1)) {
						increment = -1;
					} else 
						// if none of the above happens, then this is not a simple adjacency
						return null;
				}
			}
		}
		
		// sweep remaining vertices of p1 (note that last vertex equals the first)
		for (n1++;n1<p1.GetVertexCount()-1;n1++) {
			v1 = p1.GetVertexAt(n1);

			// in an adjacency status
			if (adjacency)
				// lets increment iterator for <p2> (forward or backward)
				// note that last vertex is the same than the first, so we have to 
				// jump from first vertex to the one before the last
				if (increment<0) {
					if (n2==0)
						n2=p2.VertexCountInPolyline();
				} 
				n2=(n2+increment)%(p2.VertexCountInPolyline());
			// otherwise keep n2 unchanged
			
			// lets update vertex <v2> 
			v2 = p2.GetVertexAt(n2);
			
			// check if vertices are common
			if (v1==v2) {
				// in case of coincident vertices
				if (!adjacency) 
					// lets trigger status
					adjacency=true; triggering_counter++;

				length++;
			} else {
				// in case of different vertices
				if (adjacency) 
					adjacency=false; triggering_counter++;
				
				// lets see if this vertex of <p1> is a vertex of <p2>
				if ((index=p2.HasVertex(v1))!=null){
					// in this case status must change and trigger counter must advance
					adjacency=true; triggering_counter++;
					
					// and <n2> must jump to index of common vertex in <p2>
					n2 = index;

					// now the first common vertices must change
					i = n1;
					j = n2;

					// increment the counter of common vertices
					length++;
				}
			}		
		}

		// gives a sign to length, to know the order of adjacent edges in p2
		length *= increment;

		// returns true if status has only been triggered at os two times
		if(triggering_counter<=2) {
			Object [] res={new Integer(i),new Integer(j),new Long(length)};
			return res;
		} else {
			return null;
		}
	}



	/***
	* @desc simplifies current polygon, subtracting <p>, in case there are
	*       an single relationship between them
	* @return true if polygon were changed, false otherwise
	*/
	boolean Minus(Polygon p)
	{
		
		// in cases different than single adjacency, do nothing
		// other reason for this call is to determine 
		// first overlapping vertices, with indices i and j
		// and the length of the adjacency
		Object [] i_j_l=SingleAdjacency(this, p);
		if (i_j_l==null)
			return false;

		int i=((Integer)i_j_l[0]).intValue();
		int j=((Integer)i_j_l[1]).intValue();
		long l=((Long)i_j_l[2]).longValue();
		
		long n;
		int m=i;
		Point v;	
		boolean changes = false;
		
	    // in case the first or last vertices are coincident, we need to
		// remove one of them now (in this case we remove the last vertex)
	    if (GetFirstVertex()==GetLastVertex()) {
			v = GetLastVertex();
			v=null;
			_vertex_array.remove(_vertex_array.size()-1);        
		}

		// now lets reset remove flag from all vertices
		for (int f=0;f<GetVertexCount();f++)
			_vertex_array.get(f).ResetFlag(FLAG.REMOVE);
		
		// then lets flag vertices for removal from polygon
		for (n=0;n<Math.abs(l)-2;n++) {	
			// advance index to next position
			m=(m+1)%(GetVertexCount());
				
	        // flag the vertex for removal
			v = _vertex_array.get(m);		
			v.SetFlag(FLAG.REMOVE);
	        
	        // in case of removal of vertices before <i> one must update <i>
	        if (m<=i) i--;

			// if we are here than something changed in polygon
			changes |= true;
		}

		// and remove the vertices from the list and delete the points			
		for (int in=_vertex_array.size(); in>0; )
			if (_vertex_array.get(--in).HasFlag(FLAG.REMOVE)) {
				_vertex_array.remove(in);
			}
			
		// and now lets add the new ones
	    m = i;    
		for (n=0;n<(long) p.VertexCountInPolyline()-Math.abs(l);n++) {				
        	if ((l>0?-1:1)<0) { 
        		if (j==0)
        			j=p.VertexCountInPolyline();
        	} 
        	j=(j+(l>0?-1:1))%(p.VertexCountInPolyline());
			v = p.GetVertexAt(j);
	  
	        m=(m+1)%(GetVertexCount());
	        _vertex_array.add(m, v.clone());        
	        
			// if we are here than something changed in polygon
			changes |= true;
		}

		// now we need to reconstruct the polygon
		if ((GetFirstVertex()!=GetLastVertex()))		
	        _vertex_array.add(GetFirstVertex().clone());		

		// in case there were changes, lets recalculate the axis aligned bounding box
		if (changes) {		
			RecalculateBoundingBox();
		}

		return changes;
	}

	
	/***
	* @return Short if vertices <v1> and <v2> belongs to this polygon and are consecutive
	*/
	Short ConsecutiveVertices(Point v1, Point v2)
	{
		// if any vertex are undefined, then they cannot be consecutive
		if (v1==null || v2==null)
			return null;

		Integer index_v1, index_v2;

		// if any vertex does not exist in this polygonm then
		// they cannot be consecutive
		if ((index_v1=HasVertex(v1))==null || 
			(index_v2=HasVertex(v2))==null)
			return null;

		// see if <v2> is next vertex
		if (((index_v1+1)%(VertexCountInPolyline()))==index_v2) {
			return 1;
		}

		// see if <v2> is previous vertex
		if (((index_v1-1)%(VertexCountInPolyline()))==index_v2) {
			return -1;
		}

		return null;	
	}


	@Override
	public String toString() {
		String res="Polygon "+this.GetID()+" :";
		for(Point p:_vertex_array) {
			res+=p.toString();
		}
		return res;
	}
	
	
}
