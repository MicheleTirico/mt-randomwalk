package graphGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import org.graphstream.graph.Edge;
import org.graphstream.graph.EdgeRejectedException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSinkImages;
import org.graphstream.stream.file.FileSinkImages.LayoutPolicy;
import org.graphstream.stream.file.FileSinkImages.OutputType;
import org.graphstream.stream.file.FileSinkImages.Resolutions;
import org.graphstream.ui.graphicGraph.GraphPosLengthUtils;

import graphTool.GraphTool;
import statistical_and_math_tools.BinominalProbGenerator;
   
public class RandomWalkSeed {
	
	protected int idSeedInt = 0 ;
	private ArrayList<Seed> listSeed = new ArrayList<> ();
 	
	private Graph graph ;
	private  int  idNodeInt = 0 ,idEdgeInt = 0 ;
	double[] extLen   ;
	private int [] sizeGrid ;
	
	private boolean controlDegree ;
	private double rateBirth , rateRemove , deltaAng ; 
	private int maxSeedPerStepCr  , maxSeedPerStepRm ;
 		
	private static Random rdLen ,rdAng ;
	private static BinominalProbGenerator bpgCr, bpgRm ;

        public RandomWalkSeed ( ) { }
        
        public RandomWalkSeed ( int[ ] sizeGrid ) {
		this.sizeGrid = sizeGrid ;
		graph = new SingleGraph("g");	
		graph.setStrict(false);
	}
	
	public void setParamsCreateSeed (  int maxSeedPerStepCr  , double rateBirth  , boolean controlDegree) { 
		this.maxSeedPerStepCr  = maxSeedPerStepCr  ;
		this.rateBirth = rateBirth ;
		this.controlDegree = controlDegree ;
	}
	
	public void setParamsRemoveSeed (  int maxSeedPerStepRm , double rateRemove) {
		this.maxSeedPerStepRm = maxSeedPerStepRm ;
		this.rateRemove = rateRemove;
	}
	
	public void setSeedRandom ( int sdCr , int sdRm , int sdRdLen , int sdRdAng ) { 
		bpgCr = new BinominalProbGenerator(sdCr);
		bpgRm = new BinominalProbGenerator(sdRm);
		rdLen = new Random(sdRdLen) ;
		rdAng = new Random(sdRdAng) ;
	}
 
	public void setParamsVector ( double[]extLen, double deltaAng ) {
		this.extLen = extLen ;
		this.deltaAng = deltaAng ;	 
	}
	
// INITIALIZATION
// --------------------------------------------------------------------------------------------------------------------------------------------------	
	public void initStar ( int numNodes , double[] center, double radius ) {
		double angle = 0.01 + 2 * Math.PI / numNodes;
		Node nodeCenter = graph.addNode("init_"+Integer.toString(idNodeInt++));
		nodeCenter.addAttribute("xyz", center[0], center[1], 0);
		nodeCenter.addAttribute("hasSeed", false);
		while (idNodeInt < numNodes + 1) {  
			double coordX = center[0] + radius * Math.cos(idNodeInt * angle),
					coordY = center[1] + radius * Math.sin(idNodeInt * angle);

			Node n = graph.addNode("init_"+Integer.toString(idNodeInt++));
			n.addAttribute("xyz", coordX, coordY, 0);
			n.addAttribute("hasSeed", true);
			double[] sCoords = new double[] { coordX, coordY };
			listSeed.add(new Seed(sCoords, n, new ArrayList<Node>(Arrays.asList(nodeCenter)), computeAng(sCoords, center)));
			graph.addEdge("init_"+Integer.toString(idEdgeInt++), n, nodeCenter);
		} 
	}
	
	public void initStar ( int numNodes , double[] center, double radius, double initAngle  ) {
		double angle = initAngle + 2 * Math.PI / numNodes;
		Node nodeCenter = graph.addNode("init_"+Integer.toString(idNodeInt++));
		nodeCenter.addAttribute("xyz", center[0], center[1], 0);
		nodeCenter.addAttribute("hasSeed", false);
		while (idNodeInt < numNodes + 1) {  
			double coordX = center[0] + radius * Math.cos(idNodeInt * angle),
					coordY = center[1] + radius * Math.sin(idNodeInt * angle);

			Node n = graph.addNode("init_"+Integer.toString(idNodeInt++));
			n.addAttribute("xyz", coordX, coordY, 0);
			n.addAttribute("hasSeed", true);
			double[] sCoords = new double[] { coordX, coordY };
			listSeed.add(new Seed(sCoords, n, new ArrayList<Node>(Arrays.asList(nodeCenter)), computeAng(sCoords, center)));
			graph.addEdge("init_"+Integer.toString(idEdgeInt++), n, nodeCenter);
		} 
	}
		
	public void compute ( ) { 
		ArrayList<Seed> colNewSeed = createSeed(); 
		removeSeed(colNewSeed);
		Collection<Seed> listSeedToRemove = new HashSet<Seed>();
		Iterator<Seed> itSeed = listSeed.iterator();
		while (itSeed.hasNext()) {
			Seed s = itSeed.next(); 
			if (isSeedInSpace(s) == true ) {
				ArrayList<Node> path = s.getPath(2, true);
				Node sNode = s.getNode(), pNode = path.get(path.size() - 2);
				double[] sCoords = s.getCoords(), pCoords = GraphPosLengthUtils.nodePosition(pNode);
				Collection<Edge> collEdgeNear = graph.getEdgeSet();	
	 			double[] vecRw = getRandomWalk(sCoords, pCoords),  
	 					 fCoords = { +vecRw[0] + sCoords[0]  , +vecRw[1] + sCoords[1]  }; 
	 
				Edge ex = GraphTool.getEgeIntersecInEdgeSet(sCoords, fCoords, collEdgeNear);
				Collection<Edge> colXEdge = GraphTool.getEdgeSetIntersectWithsegment(sCoords, fCoords,collEdgeNear);
				colXEdge.add(ex);
				sNode.addAttribute("hasSeed" , false ) ;//sNode.addAttribute(  "ui.style", "fill-color: rgb(255,0,0);" ) ;					
				if (ex == null) {
					Node newNode = graph.addNode("init_"+Integer.toString(idNodeInt++));
					newNode.addAttribute("xyz", fCoords[0], fCoords[1], 0);//	newNode.addAttribute("ui.color", 1 );
					newNode.addAttribute("hasSeed" , true ) ;graph.addEdge("init"+Integer.toString(idEdgeInt++), newNode, sNode );//e.addAttribute( "ui.style", "fill-color: rgb(255,0,0);");
					s.setNode(newNode);
				} else { 
					Iterator<Edge> itEd = colXEdge.iterator();
					boolean test = false;
					while (test == false && itEd.hasNext()) {
						ex = itEd.next();
						Node n0 = ex.getNode0(), n1 = ex.getOpposite(n0);
						double[] n0Coords = GraphPosLengthUtils.nodePosition(n0),
								n1Coords = GraphPosLengthUtils.nodePosition(n1);
						double[] intersection = GraphTool.getCoordIntersectionLine(n0Coords, n1Coords, sCoords, fCoords);
						if (GraphTool.getEgeIntersecInEdgeSet(intersection, sCoords, collEdgeNear) == null) {
							Node interNode = graph.addNode("init_"+Integer.toString(idNodeInt++));
							interNode.addAttribute("hasSeed", false); // interNode.addAttribute("ui.color", 1 );
							interNode.setAttribute("xyz", intersection[0], intersection[1], 0);
							try {
								graph.addEdge("init_"+Integer.toString(idEdgeInt++), sNode, interNode );
								graph.addEdge("init_"+Integer.toString(idEdgeInt++), n0, interNode);
								graph.addEdge("init_"+Integer.toString(idEdgeInt++), n1, interNode);
								graph.removeEdge(ex);									//	 e0.addAttribute("ui.style", "fill-color: rgb(255,0,0);");e1.addAttribute("ui.style", "fill-color: rgb(255,0,0);");	 e2.addAttribute("ui.style", "fill-color: rgb(255,0,0);");
								test = true ; 
								listSeedToRemove.add(s);
							} catch (EdgeRejectedException exc) {
								test = false ; 
								listSeedToRemove.add(s);
	
							}
						}
					}
				} 
			} else {
 				listSeedToRemove.add(s);
			}
		}
		listSeedToRemove.stream().forEach(s -> listSeed.remove(s));
	}
	
	private boolean isSeedInSpace (Seed s) {
		double [] coords = s.getCoords();
		if ( coords[0] > 0 && coords[0] < sizeGrid[0] & 
				coords[1] > 0 && coords[1] < sizeGrid[1] ) 
				return true;
			else 	
				return false ;
	}
	
	private ArrayList<Seed> createSeed (   ) {
		ArrayList<Seed> collNewSeed = new ArrayList<Seed>() ;
		int p = 0;
		Iterator<Node> itNode = graph.getNodeIterator();
		while (p < maxSeedPerStepCr && itNode.hasNext()) {
			Node pNode = itNode.next();
			boolean hasSeed = pNode.getAttribute("hasSeed");
			if (pNode.getAttribute("scale") == null 
					&& hasSeed == false 
					) {//				try  {
					double[] pCoords = GraphPosLengthUtils.nodePosition(pNode);
					double prob = rateBirth * ((controlDegree== true ) ?  1.0 / pNode.getDegree() : 1.0);
					if (bpgCr.getNextBoolean(prob) ) {
						double[] oldCoords = GraphPosLengthUtils.nodePosition(pNode);
						double angle = rdAng.nextDouble() * Math.PI * 2;
						double[] newCoords = { oldCoords[0] + 0.01 * Math.cos(angle),
								oldCoords[1] + 0.01 * Math.sin(angle) };
						if (GraphTool.isSegmentIntersecInEdgeSet(newCoords, pCoords, graph.getEdgeSet()) == false) {
							Seed s = new Seed(newCoords, pNode, new ArrayList<Node>(Arrays.asList(pNode)),
							computeAng(newCoords, pCoords));
							listSeed.add(s);
							collNewSeed.add(s);
							p++;					
 						}
					}//				} catch (NullPointerException e) {	p++ ; }
			}	
		} 	// System.out.print(p + " / " );
 		return collNewSeed;
	}
	
	private void removeSeed ( ArrayList<Seed> colNewSeed) {
		int p = 0; 
		ArrayList<Seed> listSeedToRemove = new ArrayList<Seed>();
		Iterator<Seed> itSeed = listSeed.iterator();
		while (p < maxSeedPerStepRm && itSeed.hasNext()) {
			Seed s = itSeed.next(); 
			if (!colNewSeed.contains(s)) { // try {
					if (bpgRm.getNextBoolean(rateRemove) ) {
						listSeedToRemove.add(s);
 						p++;
					}// } catch (NullPointerException e) { p++; }
			}
		}
 		listSeedToRemove.stream().forEach(s -> listSeed.remove(s));		
	}

// COMPUTE RANDOM WALK
// ------------------------------------------------------------------------------------------------------------------------------
	private double[] getRandomWalk(double[] sCoords, double[] pCoords) {
		double  dist = GraphTool.getDistGeom(pCoords, sCoords) ,
				cos = (sCoords[0] - pCoords[0]) / dist,
				ang = Math.acos(cos), 
				newAng = ang + (rdAng.nextBoolean() == true ? 1 : -1) * deltaAng,
				radius = extLen[0] + (extLen[1] - extLen[0]) * rdLen.nextDouble();
		return new double[] {  radius * Math.cos(newAng),
				radius * Math.sin(newAng) * (sCoords[1] - pCoords[1] >= 0 ? 1 : -1) } ; /// * * signY */ *sin ;
	}
	
	private double computeAng(double[] sCoords, double[] pCoords) {
		return Math.acos((sCoords[0] - pCoords[0]) / GraphTool.getDistGeom(pCoords, sCoords));
	}
	
// GET AND SET METHODS 
// ------------------------------------------------------------------------------------------------------------------------------
	public Graph getGraph() {	return graph; }
	
	public int getNumSeed() {	return listSeed.size(); }
	
	public void getImage(String path, String nameFile) {
		graph.addAttribute("ui.screenshot", path + "/" + nameFile + ".png");
	}
	 
	public void getScreenShot ( String path, String nameFile ) throws IOException {
		 FileSinkImages pic = new FileSinkImages(OutputType.PNG, Resolutions.QSXGA);
		 pic.setLayoutPolicy(LayoutPolicy.NO_LAYOUT);
		 pic.writeAll(graph, path + "/" + nameFile + ".png");
	}
	
}
