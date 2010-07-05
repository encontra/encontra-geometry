package pt.inevo.encontra.geometry;

import pt.inevo.encontra.geometry.Entity2D.FLAG;
//import pt.inevo.sbr.drawing.Drawing;
//import pt.inevo.sbr.swing.SVGViewer;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("serial")
public class LineSet extends ArrayList<Line>{

	static Logger log = Logger.getLogger(LineSet.class.getName());
	
	/************************************************************************/
	/* Local Macro to help creating a new line segment between two lines    */
	/*       and add it to a lines list                                     */
	/* @note this macro is undefined at the end of this file                */
	/************************************************************************/
	public void addNewSegment( Point p1, Point p2) {
		if (!p1.equals(p2)) add(new Line(p1, p2));
	}
	/***
	* @desc Removes intersections between lines
	* @return true if successfull, false otherwise
	* @see Bentley-Ottmann Algorithm
	*/
	public boolean RemoveIntersections()
	{
		//STARTING_PROCESS_MESSAGE("Line intersection removal");
		
		DetectSharedVertexes();
		
		// prior to removing overlapping, one must 
		// remove all zero length line, otherwise the results
		// will be unpredictable
		log.info("Removing zero length lines...");
		RemoveZeroLengthLines();

		// then we must remove line overlapping in order to run
		// the Bentley-Ottmann Algorithm 
		log.info("Removing overlappings...");
                RemoveOverlappings();

		// finally we detect intersections between lines
                log.info("Detecting intersections...");
		int intersection_count = DetectIntersections();
		
		log.info(String.format("Detected %d intersections", intersection_count));

		int i, j, count = size();
		Line line;

		LineSet created_lines_list=new LineSet();
		Point point, intersection;
		ArrayList<Point> intersections_list=new ArrayList<Point>();

		//ITERATE_ALL_ENTITIES(_lines_array,ResetFlag(FLAG_REMOVE));

		// sweep all lines
		for (i=0; i<count; i++) {
			line = get(i);
			line.ResetFlag(FLAG.REMOVE);
			
			if (line==null) {
				log.log(Level.SEVERE,"Major failure (NULL line) occurred during intersection removal.");
				break;
			}

			// check if current line has intersections
			if (line.HasIntersections()) {

				point = line.GetFirstPoint();
				line.SortIntersectionsList();
				intersections_list = line.GetIntersectionsList();			

				for (j=0; j<intersections_list.size(); j++){
					intersection = intersections_list.get(j);
					
					created_lines_list.addNewSegment(point, intersection);
					point = intersection;

				}	

				created_lines_list.addNewSegment(point, line.GetLastPoint());
	 			line.SetFlag(FLAG.REMOVE);
				
			} 
		}

		// Now lets remove the flagged lines
		for (i=this.size(); i>0; ) {
			line = this.get(--i);

			// check if line is flagged for removal
			if (line==null || line.HasFlag(FLAG.REMOVE)) { 
				// in that case, remove it from the array and delete it from memory
				this.remove(i);
			} 
		}
		
		this.addAll(created_lines_list);
		
		created_lines_list.clear();

		//ENDING_PROCESS_MESSAGE();
		return true;
	}

	public void DetectSharedVertexes() {
		// added by nfgs - fix shared vertexes
		for (Line l1 : this) {
			for (Line l2 : this) {
				if(l2==l1)
					continue;
				
				if (l1._start_point.equals(l2._start_point)) {
					l2.SetStartPoint(l1._start_point);
					
				} else if (l1._end_point.equals(l2._start_point)) {
					l2.SetStartPoint(l1._end_point);
				}
				else if (l1._start_point.equals(l2._end_point)) {
					l2.SetEndPoint(l1._start_point);
				} else if (l1._end_point.equals(l2._end_point)) {
					l2.SetEndPoint(l1._end_point);
				}
			}
		}
	}
	/***
	* @desc removes all lines with zero length 
	*/
	public void RemoveZeroLengthLines()
	{
		// sweep all lines
		for (int i = size(); i>0; i--) {
			Line  line = get(i-1);
			// find a zero length line
			if ((line.GetStartPoint()).equals(line.GetEndPoint())) {
				remove(i-1);
			}
		}
	}

	/***
	* @descr removes line overlappings
	* @note must be called before applying Bentley-Ottmann algorithm
	*/
	public void RemoveOverlappings()
	{
		int i, j, count = size();
		Line line, line_i, line_j;
                log.info(String.format("We've got %d lines to process!", count));
		// lets find overlapping lines
		for (i=0; i<count; i++) {
			line_i = get(i);
			for(j=i+1;j<count;j++) {
				line_j =  get(j);
				if (Line.Overlapping(line_i, line_j)) {			
					line = Line.SimplifiedLine(line_i, line_j);

					if (line == line_i) {
						// must remove line_j
						remove(j);
						//wxDELETE(line_j);
						j--;
						count--;
					} else {					
						if (line != line_j) { 
							// must remove both line_i and line_j and add a new one
							remove(j);
                                                        line_j=null;
							//wxDELETE(line_j);
							add(line);
						}

						// must remove line_i						
						remove(i);
                                                line_i=null;
						//wxDELETE(line_i);

						// update counters
						i--;
						count --;						

						// skip inner loop an go to next step of outer loop
						break;					
					}
				}

			}
		}
	}

	// Detect intersection between lines
	public int DetectIntersections()
	{
		//DetectSharedVertexes();
		
		//printLines();
		// first we sort the line array
		Sort();
		//printLines();
		
		// then declare the Active Line Segments Array (ALST)
		ArrayList<Line> active_line_segments_array = new ArrayList<Line>();

		int j, counter = this.size();
		double current_y = 0.0f, next_y;		
		Line current_line, other_line;
		boolean move_sweep_line = false;

		Point intersection = null;

		int result = 0;

				
		// now we sweep over the Lines Array, in this case is '_entity_array'
		for(int n=0; n<counter;n++) {
			current_line = get(n);
			next_y = current_line.GetFirstPoint().GetY();

			// see if current line belongs to next sweep line
			move_sweep_line = (next_y > current_y);

			// for each line in ALST
			for (j=active_line_segments_array.size(); j>0;) {
				other_line = active_line_segments_array.get(--j);

				// in case the current segment move the sweep line forward
				if (move_sweep_line) {			
					// we must see if other line ends before next 'y', and in that case
					// it should be removed from Active Line Segments Array			
					if (other_line.GetLastPoint().GetY() < next_y) {
						active_line_segments_array.remove(j);
						continue;
					}
				}

				// if the current line intersects with other line
				if (current_line.Intersects(other_line)) {
					intersection = current_line.IntersectionPoint(other_line);

					if (intersection!=null) {
						current_line.AddIntersectionPoint(intersection);
						other_line.AddIntersectionPoint(intersection.clone());
						result++;
						
					} 
					/* added by nfgs - shared vertices!
					else if (current_line._start_point.equals(other_line._start_point)) {
						other_line.SetStartPoint(current_line._start_point);
						
					} else if (current_line._end_point.equals(other_line._start_point)) {
						other_line.SetStartPoint(current_line._end_point);
					}
					else if (current_line._start_point.equals(other_line._end_point)) {
						other_line.SetEndPoint(current_line._start_point);
					} else if (current_line._end_point.equals(other_line._end_point)) {
						other_line.SetEndPoint(current_line._end_point);
					}*/
	
				}
			}

			// in case the current segment move the sweep line forward
			if (move_sweep_line) {				
				// we must move the sweep line
				current_y = next_y;
			}

			// now we add the current line to the Active Line Segments Array
			active_line_segments_array.add(current_line);

		}

		// at the end we remove the remaining lines in ALST
		// and delete the array
		active_line_segments_array.clear();

		
		return result;
	}

	public String getSVG() {
		Point [] bb=getBoundingBox();
		Point bottom_left=bb[0];
		Point top_right=bb[1];
		double bb_width = Math.abs(top_right.GetX()-bottom_left.GetX());
		double bb_height = Math.abs(top_right.GetY()-bottom_left.GetY());
		double swidth=Math.min(bb_width, bb_height)/1000;
		NumberFormat nf=NumberFormat.getInstance(Locale.US);
		String stroke_width=nf.format(swidth);
		String result="<svg xmlns=\"http://www.w3.org/2000/svg\"  version=\"1.2\" " +
				"width=\"100%\" height=\"100%\" viewBox=\""+bottom_left.x+" "+bottom_left.y+" "+bb_width+" "+bb_height+"\">\n";	
		for(Line line:this) {
			Point start=line.GetStartPoint();
			Point end=line.GetEndPoint();
			result+="<line x1=\""+start.x+"\" y1=\""+start.y+"\" x2=\""+end.x+"\" " +
					"y2=\""+end.y+"\" fill=\"none\" stroke=\"#000000\" stroke-width=\""+stroke_width+"\"/>";
		}
		result+="</svg>";
		return result;
		
	}
		
	private void printLines() {
		for(Line line:this) {
			Point start=line.GetStartPoint();
			Point end=line.GetEndPoint();
			System.out.println("P"+start.GetID()+" ("+start.x+","+start.y+") -> P"+end.GetID()+" ("+end.x+","+end.y+")");
		}
		
	}
	/***
	* @descr sort the lines
	*/
	public void Sort()
	{
		CalculateLinesFirstAndLastPoint();
		Collections.sort(this);
		//Sort(Line::CompareOrder);
	}

	/***
	* @descr calculates the bottom left point of all entities and its opposite point
	*/
	void CalculateLinesFirstAndLastPoint()
	{
		for (int i=0; i< this.size(); i++) 
			this.get(i).CalculateFirstAndLastPoint();

	}

	/***
	* @descr Performs a scale transformation on lines of this set
	*/
	void Scale(double sx, double sy)
	{
		for (int i=0; i<this.size();i++)
			this.get(i).Scale(sx,sy);
	}

	/***
	* @descr Performs a scale transformation on lines of this set
	*/
	void Translate(double dx, double dy)
	{
		for (int i=0; i<this.size();i++)
			this.get(i).Translate(dx,dy);

	}

	public Point[] getBoundingBox(){
		// compute the axis aligned bounding box
		Point top_right=new Point();
		Point bottom_left=new Point();
		boolean first = true;
		//printLines();
		for (int i=0; i<this.size(); i++) {
			Line line = this.get(i);
			if (line!=null) {
				Point a = line.GetStartPoint();
				Point b = line.GetEndPoint();

				if (a!=null && b!=null) {
					if (first) {
						top_right = new Point(line.GetMaxX(),line.GetMaxY());
						bottom_left = new Point(line.GetMinX(),line.GetMinY());
						first = false;
					} else {
						double max_x = line.GetMaxX();
						double min_x = line.GetMinX();
						double max_y = line.GetMaxY();
						double min_y = line.GetMinY();

						top_right.SetX(Math.max(top_right.GetX(), max_x));
						top_right.SetY(Math.max(top_right.GetY(), max_y));
						
						bottom_left.SetX(Math.min(bottom_left.GetX(), min_x));
						bottom_left.SetY(Math.min(bottom_left.GetY(), min_y));

					}
				} // if (a && b)...
			}
		}

		// determine the scale factor
		//double bb_width = Math.abs(top_right.GetX()-bottom_left.GetX());
		//double bb_height = Math.abs(top_right.GetY()-bottom_left.GetY());
		Point [] result=new Point[2];
		result[0]=bottom_left;
		result[1]=top_right;
		return result;
	}
	
	public boolean Normalize()
	{
		
		// compute the axis aligned bounding box
		Point top_right=new Point();
		Point bottom_left=new Point();
		boolean first = true;
		//printLines();
		for (int i=0; i<this.size(); i++) {
			Line line = this.get(i);
			if (line!=null) {
				Point a = line.GetStartPoint();
				Point b = line.GetEndPoint();

				if (a!=null && b!=null) {
					if (first) {
						top_right = new Point(line.GetMaxX(),line.GetMaxY());
						bottom_left = new Point(line.GetMinX(),line.GetMinY());
						first = false;
					} else {
						double max_x = line.GetMaxX();
						double min_x = line.GetMinX();
						double max_y = line.GetMaxY();
						double min_y = line.GetMinY();

						top_right.SetX(Math.max(top_right.GetX(), max_x));
						top_right.SetY(Math.max(top_right.GetY(), max_y));
						
						bottom_left.SetX(Math.min(bottom_left.GetX(), min_x));
						bottom_left.SetY(Math.min(bottom_left.GetY(), min_y));

					}
				} // if (a && b)...
			}
		}

		if (first) {
			log.log(Level.SEVERE,"Unable to perform line set normalization.");
			return false;
		}

		// determine the scale factor
		double bb_width = Math.abs(top_right.GetX()-bottom_left.GetX());
		double bb_height = Math.abs(top_right.GetY()-bottom_left.GetY());


		if (bb_width==0.0 && bb_height==0.0 ) {
			log.log(Level.SEVERE,"Empty bounding box. Could not normalize the line set.");
			return false;
		}

		double scale_factor= (1.0f)/Math.max(bb_width,bb_height);

		Translate(-bottom_left.GetX(), -bottom_left.GetY());
		
		//show("Normalize - translate");
		
		Scale(scale_factor,scale_factor);
		//show("Normalize - scale");
		
		//System.out.println("Normalize:");
		//printLines();
		
		return true;
	}

    /*
	public void show(){
		show("LineSet");
	}


	public void show(String title) {
		Drawing lines=new Drawing();
        String linesSvg=getSVG();

       lines.createFromSVG(linesSvg);
        lines.initialize();
        SVGViewer linesViewer=new SVGViewer();
        linesViewer.frame.setTitle(title);
        linesViewer.setSVG(lines.getSVGDocument());
	}*/

}
