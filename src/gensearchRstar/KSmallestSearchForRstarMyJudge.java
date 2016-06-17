package gensearchRstar;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;


import gensearch.MinMaxDist;
import gensearch.Range;
import gensearch.RangeExpression;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import rstar.RStarTree;
import rstar.dto.PointDTO;
import rstar.nodes.RStarLeaf;
import rstar.nodes.RStarNode;
import rstar.spatial.SpatialPoint;

public class KSmallestSearchForRstarMyJudge {
	
	//counting visiting times of nodes
	private static long visit_cnt = 0;
	
	private static long notconsistent_cnt = 0;
	private static long consistent_cnt = 0;
	
	//store cache of nodes, for 0:MinDist,1:MinMaxDist values(non-leaf node), and the 0,1:value of exp (leaf node)
	private static HashMap<Long,double[]> nodeCache = new HashMap<Long,double[]>();
	private static HashMap<Float,double[]> ptCache = new HashMap<Float,double[]>();
	
	private static long cache_hits = 0;
	
	private static void resetStatis(){
		 visit_cnt = 0;
		
		notconsistent_cnt = 0;
		consistent_cnt = 0;
		
		//store cache of nodes, for 0:MinDist,1:MinMaxDist values(non-leaf node), and the 0,1:value of exp (leaf node)
		nodeCache = new HashMap<Long,double[]>();
		ptCache = new HashMap<Float,double[]>();
		
		cache_hits = 0;
	}
	/**
	 *input: 
	 * numDimensions:num of dimensions, also num of vars in udf
*		 numLevels:num of levels in r-tree
*		 numSeps: number of separates for a R-tree node on one axis, for building a Perfect R-tree(no overlaps)
*		 k:k smallest searching target number
*		 udf: user defined function, using x1,x2...xn as var in each dimension
*		 dudfs: every patial derivative of udf for each xi
*				dudf for x1
*				dudf for x2
*				...
*				dudf for xn
*		 
*		
*	  output:
*		 leaf_num: num of leaves in r-tree = fanout^levels
*		           which fanout = (int)Math.pow(numSeps, numDimensions);
*		 visit_num: num of visiting node in r-tree
*		 speed_up: percent of speeding up compare to linear searching = visit_num / leaf_num * 100%
*		 tuples as k tuples having smallest value of function udf 
*		 <x1,x2,...,xn> 
*		 ... 
	 * @param args
	 */
	public static void main(String[] args) {
		main9(args);
	}
	
	public static void main7(String[] args) {
		//try bad case
		String[] fs={
				"1/(x1-x2)^2",
				"-2/(x1-x2)^3",
				"2/(x1-x2)^3",
				};
		 test(2,10,32,fs,1,100000,5);
	}
	
	
	public static void main9(String[] args) {
		//the speed of 2 dim
		String[] fs={
				"((x1-x2)^2+(x3-x4)^2)^(1/2)/(x5-x6)",
				
				"(x1-x2)/( (x5-x6)*((x1-x2)^2+(x3-x4)^2)^(1/2) )",
				"(x2-x1)/( (x5-x6)*((x1-x2)^2+(x3-x4)^2)^(1/2) )",
				"(x3-x4)/( (x5-x6)*((x1-x2)^2+(x3-x4)^2)^(1/2) )",
				"(x4-x3)/( (x5-x6)*((x1-x2)^2+(x3-x4)^2)^(1/2) )",
				"-((x1-x2)^2+(x3-x4)^2)^(1/2) / (x5-x6)^2",
				"((x1-x2)^2+(x3-x4)^2)^(1/2) / (x5-x6)^2",
				};
		 test(6,4,8,fs,100,50000,1);
	}
	
	public static void main1(String[] args) {
		//speed of 1 dim
		String[] fs={
				"(x1-x2)/(x3-x4)",
				"1/(x3-x4)",
				"-1/(x3-x4)",
				"-(x1-x2)/(x3-x4)^2",
				"(x1-x2)/(x3-x4)^2"
				};
		 test(4,10,32,fs,1000,100000,1);
	}
	
	public static void main4(String[] args) {
		//sum of 2 distances
		String[] fs={
				"((x1-1)^2+(x2-2)^2)^(1/2)+((x1-3)^2+(x2-4)^2)^(1/2)",
				"(x1-1)/((x1-1)^2+(x2-2)^2)^(1/2)+(x1-3)/((x1-3)^2+(x2-4)^2)^(1/2)",
				"(x2-2)/((x1-1)^2+(x2-2)^2)^(1/2)+(x2-4)/((x1-3)^2+(x2-4)^2)^(1/2)",
				};
		 test(2,10,32,fs,100,100000,1);
	}
	
	public static void main2(String[] args) {
		//another sum of 2 distances
		String[] fs={
				"((x1-100)^2+(x2-200)^2)^(1/2)+((x1-1000)^2+(x2-2000)^2)^(1/2)",
				"(x1-100)/((x1-100)^2+(x2-200)^2)^(1/2)+(x1-1000)/((x1-1000)^2+(x2-2000)^2)^(1/2)",
				"(x2-200)/((x1-100)^2+(x2-200)^2)^(1/2)+(x2-2000)/((x1-1000)^2+(x2-2000)^2)^(1/2)",
				};
		 test(2,10,32,fs,500,100000,1);
	}
	
	public static void main0(String[] args) {
		//multiple function
		String[] fs={
				"x1*x2*x3",
				"x2*x3",
				"x1*x3",
				"x1*x2",
				};
		 test(3,32,64,fs,100,1000,1);

    }
	
	
	public static void test(int numDim,int minNum,int maxNum,String[] fs, double valRange,int cnt, double diffRate ) {
		//1. Input data
		String[] xs = new String[numDim];
		for(int i=0;i<numDim;i++){
			xs[i] = "x"+(i+1);
		}
		Expression udf = new ExpressionBuilder(fs[0])
				.variables(xs).build();
		Expression[] dudfs = new Expression[numDim];
		
		for(int i=0;i<numDim;i++){
			dudfs[i]= new ExpressionBuilder(fs[i+1])
					.variables(xs).build();
		}
		
		
		//RTree<Double> rt = new RTree<Double>(maxNum,minNum,numDim,RTree.SeedPicker.QUADRATIC);
		RStarTree rt = new RStarTree(numDim);
		float[] pt = new float[numDim];
		
		double min = Double.MAX_VALUE;
		for(int i=0; i<cnt; i++){
			
			for(int j=0;j<numDim;j++){
				if(j%2==0){
					pt[j] = (float) (valRange*diffRate * Math.random());
				}else{
					pt[j] = (float) (valRange * Math.random());
				}
				
				
			}
			if(pt[4]<pt[5]){
				float swp = pt[4];
				pt[4] = pt[5];
				pt[5] = swp;
			}
			for(int j=0;j<numDim;j++){
				udf.setVariable("x"+(j+1), pt[j]);
			}
			
			double val = udf.evaluate();
			rt.insert(new SpatialPoint(pt, (float) val));
			//rt.insert(pt, val);
			if(val<min){
				min = val;
			}
            System.out.println(i);
		}
		rt.save();
		System.out.println(rt);
		
		//2. searching the smallest one
		LinkedList<RStarNode> activeNodes = new LinkedList<RStarNode>();
		activeNodes.add(rt.getRoot() );
		Stack<LinkedList<RStarNode>> prunedNodes = new Stack<LinkedList<RStarNode>>();
		 SpatialPoint minPt = searchSmallest(rt,udf, dudfs, activeNodes, prunedNodes);
		 printOutput(udf, minPt,cnt,min,notconsistent_cnt,consistent_cnt);
//		 activeNodes.clear();
//		 activeNodes.add(rt.getRoot() );
//		 resetStatis();
//		 
//		 
//		 minPt = searchSmallest(rt,udf, dudfs, activeNodes, prunedNodes);
//		 printOutput(udf, minPt,cnt,min,notconsistent_cnt,consistent_cnt);
//		 activeNodes.clear();
//		 activeNodes.add(rt.getRoot() );
//		 resetStatis();
//		 
//		 minPt = searchSmallest(rt,udf, dudfs, activeNodes, prunedNodes);
//		 printOutput(udf, minPt,cnt,min,notconsistent_cnt,consistent_cnt);
		 
		 
		//3. searching the remain k-1 ones 
		//using the last activeNodes and prunedNodes
		
		//4. output
		
		
	}

	static void printOutput(Expression udf, SpatialPoint minPt, double cnt, double min, long notconsistent_cnt2, long consistent_cnt2){
		System.out.println("cache_hits:"+cache_hits);
		System.out.println("visit_cnt rate:"+visit_cnt+"/"+cnt);
		System.out.println("saving rate:"+Math.round( (1- visit_cnt*1.0/(cnt*1.0) ) *10000)/100.0+"%") ;
		System.out.println("$real min:"+min);
		System.out.println("$calc min:"+minPt.getOid());
		double calcMin = applyUdfPt(udf,minPt);
		if(min!=calcMin ){
			System.err.println("not equal!!!!"+minPt);
		}
		System.out.println("nonconsistent:"+notconsistent_cnt2+",consistent:"+consistent_cnt2);
	}
	
    

	//RTree.Node
	private static SpatialPoint searchSmallest(
			RStarTree rt, Expression udf, Expression[] dudfs,
			LinkedList<RStarNode> activeNodes,Stack<LinkedList<RStarNode>> prunedNodes
		) {
		while(true){
			//if at the end, return the first one if there are more than 1 smallest 
			if(activeNodes.getFirst().isLeaf()){
				
				LinkedList<SpatialPoint> tmpList = new LinkedList<SpatialPoint>();
				
				for(RStarNode node : activeNodes){
					RStarLeaf leafnode = (RStarLeaf)node;
					System.out.println("node children cnt:"+leafnode.childPointers.size());
					for(long pid : leafnode.childPointers){
						 PointDTO dto = rt.storage.loadPoint(pid);
						 SpatialPoint subN = new SpatialPoint(dto);
						tmpList.add(subN);
					}
				}
				
				double min_MinMaxDist = Double.POSITIVE_INFINITY;
				SpatialPoint minPt = null;
				for(SpatialPoint pt : tmpList){
					double minmaxdist = applyUdfPt(udf,pt);
					
					if(minmaxdist < min_MinMaxDist){
						min_MinMaxDist = minmaxdist;
						minPt = pt;
					}
				}
				return minPt;
				
			}else{//go on to the the next level
				
				//1. collect all sub-nodes from activeNodes
				LinkedList<RStarNode> tmpList = new LinkedList<RStarNode>();
				for(RStarNode node : activeNodes){
					System.out.println("curr nodeId:"+node.nodeId+", node children cnt:"+node.childPointers.size());
					for(Long pid : node.childPointers){
						RStarNode subN = rt.loadNode(pid);
						tmpList.add(subN);
					}
				}
				
				//2. prune and store the disgarded into prunedList, and make another activeNodes
				//2.1 get min of MinMaxDist
				double min_MinMaxDist = Double.POSITIVE_INFINITY;
				for(RStarNode node : tmpList){
					double minmaxdist = getMinMaxDist(rt,udf,dudfs, node);
					if(minmaxdist < min_MinMaxDist){
						min_MinMaxDist = minmaxdist;
					}
				}
				
				System.out.println("min of minmax:"+min_MinMaxDist);
				activeNodes.clear();
				//2.2 prune the nodes that having MinDist>min_MinMaxDist
				//    when MinDist<= min_MinMaxDist, put into activeNodes
				for (Iterator<RStarNode> iterator = tmpList.iterator(); iterator.hasNext();) {
					RStarNode node = iterator.next();
					System.out.println("mbr of this node:"+node.nodeId+"--mbr:"+node.mbr);
					double mindist = getMinDist(rt,udf,dudfs, node);
					if(mindist <= min_MinMaxDist){
						activeNodes.add(node);
						iterator.remove();
						System.out.println("add to next round");
					}else{
						System.out.println("skip!"+mindist);	
					}
					
				}
				// the remaining is in prunedNodes
				if(! tmpList.isEmpty()){
					prunedNodes.add(tmpList);
				}
			}
		}
	}

	private static double getMinDist(RStarTree rt,Expression udf, Expression[] dudfs, RStarNode node) {
		double[] res = getMinNMinMaxDist(rt,udf,dudfs,node);
		return res [MinMaxDist.IDX_MINDIST];
	}

	private static double getMinMaxDist(RStarTree rt, Expression udf, Expression[] dudfs, RStarNode node) {
		double[] res = getMinNMinMaxDist(rt,udf,dudfs,node);
		return res [MinMaxDist.IDX_MINMAXDIST];
	}
	
	private static double[] getMinNMinMaxDist(RStarTree rt,Expression udf, Expression[] dudfs, RStarNode node) {
		if(nodeCache.containsKey(node.getNodeId())){
			cache_hits ++;
			return nodeCache.get(node.getNodeId());
		}
		visit_cnt ++;
		
		//if node is leaf then applyUdf to get directly
//		if(node.isEntry()){
//			double[] res = new double[2];
//			res[0] = res[1] = applyUdf(udf,node);
//			return res;
//		}else{//otherwise try to use MBR only
		
		
		
			//1.0 check range first, using vertexes only when all dudfs are consistent
			int i=0;
			int numDimensions = dudfs.length;
			Range r = null;
			for(;i<dudfs.length;i++){
				RangeExpression rexp = new RangeExpression(dudfs[i]);
				//set ranges of vars
				for(int varIdx = 0; varIdx < numDimensions; varIdx++){
					//rexp.setVariable("x"+(varIdx+1), new Range(node.MBR_S()[varIdx],node.MBR_T()[varIdx]));
					rexp.setVariable("x"+(varIdx+1), new Range( node.getMBR().points[varIdx][1],node.getMBR().points[varIdx][0] ));
				}
				 r= rexp.evaluate();
				if(r.hasChangedSign()){
					break;
				}
			}
			
			//2.0
			if(i<dudfs.length){//not all vars are consistent, then use the sub-Nodes
				System.out.println("not cons:"+myJudge(node.mbr.points));
				notconsistent_cnt++;
				//2.1 get min and minmax of all sub-Nodes 
				LinkedList<double[]> resList = new LinkedList<double[]> ();
				//2.1.1 if is leaf than get from applyUdf
				if(node.isLeaf()){
					RStarLeaf leafnode = (RStarLeaf)node;
					for(long pid : leafnode.childPointers){
						PointDTO dto = rt.storage.loadPoint(pid);
						SpatialPoint subN = new SpatialPoint(dto);
						double dist = applyUdfPt(udf,subN);
						double[] res = new double[2];
						res[0] = res[1] = dist;
						resList.add(res);
					}
				}else{
					for(long pid : node.childPointers){
						RStarNode sn = rt.loadNode(pid);
						resList.add( getMinNMinMaxDist(rt,udf, dudfs, sn) );
					}
				}
				
				
				//2.2 get the min of all min as min, the min of all minmax as min max
				double[] res = new double[2];
				res[0] = res[1] = Double.POSITIVE_INFINITY;
				for(double[] ele : resList){
					if(ele[0]<res[0]){
						res[0] = ele[0];
					}
					if(ele[1]<res[1]){
						res[1] = ele[1];
					}
				}
				
				//2.3 cache it and return 
				nodeCache.put(node.nodeId, res);
				return res;
				
			}else{ // all are consistent, use the vertexes
				System.out.println("yes cons");
				consistent_cnt++;
				double[] res = MinMaxDistRstar.calc(udf, dudfs.length, node);
				nodeCache.put(node.nodeId, res);
				System.out.println("nodeId:"+node.nodeId+" cons res:"+res[0]+","+res[1]);
				return res;
			}
		//}
	}

	private static boolean myJudge(float[][] points) {
		double x11 = points[4][1];
		double x12 = points[4][0];
		double x21 = points[5][1];
		double x22 = points[5][0];
		
		if(x11>x22 || x12<x21){
			return true;
		}else{
			return false;
		}
	}
	private static double applyUdfPt(Expression udf, SpatialPoint pt) {
		if(ptCache.containsKey(pt.getOid())){
			cache_hits++;
			return ptCache.get(pt.getOid())[0];
		}else{
			visit_cnt++;
			for(int i=0; i< pt.getCords().length; i++){
				udf.setVariable("x"+(i+1), pt.getCords()[i]);
			}
			double[] expVal = new double[2];
			expVal[0] = udf.evaluate();
			expVal[1] = expVal[0];
			ptCache.put(pt.getOid(), expVal);
			return expVal[0];
		}
	}
	
//	private static double applyUdf(Expression udf, RStarNode leafNode) {
//		if(nodeCache.containsKey(leafNode.nodeId)){
//			cache_hits++;
//			return nodeCache.get(leafNode.nodeId)[0];
//		}else{
//			//visit_cnt++;
//			for(int i=0; i< leafNode.val().length; i++){
//				udf.setVariable("x"+(i+1), leafNode.val()[i]);
//			}
//			double[] expVal = new double[2];
//			expVal[0] = udf.evaluate();
//			expVal[1] = expVal[0];
//			nodeCache.put(leafNode.id, expVal);
//			return expVal[0];
//		}
//	}

}
