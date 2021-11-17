package graphGenerator;

import java.util.ArrayList;
import java.util.Arrays;

import org.graphstream.graph.Node;
import org.graphstream.ui.graphicGraph.GraphPosLengthUtils;

public class Seed {

	private ArrayList<Node> path = new ArrayList<Node>();
	private double[] coordsXY = new double[2];
	private Node node;
	private String id;
	private double ang;

        public Seed () {
            
        };
        
	public Seed( double[] coordsXY, Node node, ArrayList<Node> path, double ang) {
		id = "0";//  Integer.toString(RandomWalkSeed.idSeedInt++);
		this.coordsXY = coordsXY;
		this.node = node;
		this.path = path;
		this.ang = ang;
		path.add(node);
		node.addAttribute("haveSeed", true);

	}

// PATH 
// ---------------------------------------------------------------------------------------------------------------------------------------------
	public void setPath(ArrayList<Node> path) {this.path = path; }

	public ArrayList<Node> getPath() {return path; }

	public ArrayList<Node> getPath(int depth) {
            ArrayList<Node> list = new ArrayList<Node>();
            int pos = 0;
            while (pos < depth)
			try {
				list.add(path.get(pos));
				pos++;
			} catch (IndexOutOfBoundsException e) {
				return list;
			}
		return list;
	}

	public ArrayList<Node> getPath(int depth, boolean getlatestNodes) {
		if (getlatestNodes)
			return new ArrayList<Node>(path.subList(Math.max(0, path.size() - depth), path.size()));
		else
			return new ArrayList<Node>(path.subList(0, Math.min(depth, path.size())));
	}

// GET AND SET 
// --------------------------------------------------------------------------------------------------------------------------------------------------
	public String getId() {	return id; }

	public double getAng() { return ang; }

	public void setAng(double ang) { this.ang = ang; }

	public void setNode(Node node) {
            this.node = node;
            path.add(node);
            this.setCoords(GraphPosLengthUtils.nodePosition(node));
            node.addAttribute("hasSeed" , true ) ;
	}

	public void setCoords(double[] coordsXY) { this.coordsXY = coordsXY; }
        
	public Node getNode() { return node; }
	
        public double[] getCoords() {return coordsXY; }

	public void setFather(Node father) {
		ArrayList<Node> list = new ArrayList<Node>(Arrays.asList(father));
		list.addAll(path);
		path = list;
	}

	public void initPath(Node father) { path.add(father); }

}

