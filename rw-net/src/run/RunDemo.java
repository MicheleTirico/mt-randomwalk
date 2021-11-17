/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package run;

import graphGenerator.RandomWalkSeed;
import graphTool.StoreNetwork;
import graphViz.HandleVizStype;
import java.io.IOException;
import java.util.Date;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

/**
 *
 * @author mtirico
 */
public class RunDemo {

    public static void run( int arg1/*,int arg3,int arg4*/) throws IOException  {
//        System.out.println("ciao " + arg1);
  //      Graph g = new SingleGraph(Integer.toString(args));
   //     System.out.println(g.getId());
        
        int pos = 0 ;
        RandomWalkSeed rws ;
        int[] sizeGrid = new int[] {128,128} ;
               
        int idSim = 0 ,
            stepMax = 300 ,
            stepToPrint = 400 ; 
        
        boolean vizLayers = false;
 		
        // initialization
        double[] center = {sizeGrid[0]/2 ,sizeGrid[1]/2 } ;
        int  numNodes = 4 ;
        double radius = 1;
                
 	// vector 
        double  minLen = 0.01 ,
                maxLen = 0.3 ,
                deltaAng = 0.05 ;
 	double[] extLen = new double[] { minLen , maxLen } ;

 		// seed random 
        int sdCr = arg1, // Integer.parseInt(args[pos++]) , 
            sdRm = 1 ,
            seedRdLen = 1, // arg3 , 
            seedRdAng = 2 ; // arg4 ;
 		
        // max seed	per step	
        int maxSeedPerStepCr = 1000 ,
            maxSeedPerStepRm = 1000 ; 

        double  rateRemove = 0.005,
            rateBirth  = 0.001 ; 
 		
//		System.out.println(rateRemove +" " + rateBirth);
		boolean controlDegree = true;
                
		rws = new RandomWalkSeed(sizeGrid);
		rws.setSeedRandom(sdCr, sdRm, seedRdLen, seedRdAng );
		rws.setParamsCreateSeed(  maxSeedPerStepCr, rateBirth, controlDegree);
		rws.setParamsRemoveSeed(  maxSeedPerStepRm, rateRemove);
		rws.initStar(numNodes, center, radius);
		rws.setParamsVector(extLen, deltaAng  * Math.PI  );
		 
		Graph net = rws.getGraph() ; 
		boolean store = false ;
		String path = "" ;

		boolean storePng = false;
		String pathPng = "";
				
		String nameFile = "v1-" + String.format("%.3f", minLen) + "_v2-" + String.format("%.3f", deltaAng) + "_v3-"
				+ String.format("%.4f", rateRemove) + "_v4-" + String.format("%.4f", rateBirth) + "_";
		
		System.out.println("name file = " + nameFile) ;
		StoreNetwork sNet = new StoreNetwork(store , true  , true, true , path, "storeNet", nameFile , net) ;
		sNet.init();
 
		if ( vizLayers ) {
			net.display(false) ;
			HandleVizStype netViz = new HandleVizStype( net ,HandleVizStype.stylesheet.manual , "seed", 1) ;
			netViz.setupIdViz(false , net, 20 , "black");
			netViz.setupDefaultParam (net, "black", "black", 1 , 0.5 );
			netViz.setupVizBooleanAtr(true, net, "black", "red" , false , false ) ;
			netViz.createSquare(true, net ,0, sizeGrid[1]);
		}
		
		net.getNodeSet().stream().forEach(n-> n.addAttribute("hasSeed", false));
		long 	T0 = System.currentTimeMillis() , 
				T = 0 ;
		double t = 0 ;// ,	numSeed = RandomWalkSeed.geListSeed().size() ; 
			 
		while ( t <= stepMax) {
			if ( t / (double) stepToPrint - (int)(t / (double) stepToPrint ) < 0.0001 && t > 0 ) {
				T = System.currentTimeMillis() - T0;
				System.out.println("runTime of " + nameFile +" step -> " + t + " " + new Date().toString() + " speed -> "  + t / (T/1000f) + " step/s , seeds -> "  + rws.getNumSeed()  +  ", nodes -> " + net.getNodeCount()) ;	
				;
			}
			net.stepBegins(t);
			net.addAttribute("seed", rws.getNumSeed());
			rws.compute();
			t++; 
		}
		System.out.println("sim: "+ idSim + " finish " + nameFile + " " + new Date().toString() + " speed -> "  + t / (T/1000f) + " step/s , seeds -> "  + rws.getNumSeed()  +  ", nodes -> " + net.getNodeCount() 	 ) ; 

		if (storePng) 
			net.addAttribute("ui.screenshot",  pathPng ) ;
		sNet.close();
	
 	}
}
