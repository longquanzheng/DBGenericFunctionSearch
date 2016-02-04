package gensearch;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class KSmallestSearch {
	
	//counting visiting times of nodes
	private static long visit_cnt = 0;
	//store cache of nodes, for 0:MinDist,1:MinMaxDist values(non-leaf node), and the 0,1:value of exp (leaf node)
	private static HashMap<Long,double[]> nodeCache = new HashMap<Long,double[]>();
	
	/**
	 *input: 
		 numDimensions:num of dimensions, also num of vars in udf
		 numLevels:num of levels in r-tree
		 numSeps: number of separates for a R-tree node on one axis, for building a Perfect R-tree(no overlaps)
		 k:k smallest searching target number
		 udf: user defined function, using x1,x2...xn as var in each dimension
		 dudfs: every patial derivative of udf for each xi
				dudf for x1
				dudf for x2
				...
				dudf for xn
		 
		
	  output:
		 leaf_num: num of leaves in r-tree = fanout^levels
		           which fanout = (int)Math.pow(numSeps, numDimensions);
		 visit_num: num of visiting node in r-tree
		 speed_up: percent of speeding up compare to linear searching = visit_num / leaf_num * 100%
		 tuples as k tuples having smallest value of function udf 
		 <x1,x2,...,xn> 
		 ... 
	 * @param args
	 */
	public static void main(String[] args) {
		
		//1. Input data
		int numDimensions = 3;
		int numLevels = 3;
		int numSeps = 4;
		int k=1;
		
		Expression udf = new ExpressionBuilder("x1*x2*x3").variables("x1","x2","x3").build();
		Expression[] dudfs = new Expression[3]; 
		dudfs[0]= new ExpressionBuilder("x2*x3").variables("x2","x3").build();
		dudfs[1] = new ExpressionBuilder("x1*x3").variables("x1","x3").build();
		dudfs[2] = new ExpressionBuilder("x1*x2").variables("x1","x2").build();
		
		PerfectRTree prt = new PerfectRTree(numDimensions,numSeps,numLevels);
		prt.setDomain(1, 0, 100);
		prt.setDomain(2, -1000,1000);
		prt.buildRandomly();
		
		//2. searching the smallest one
		LinkedList<RTreeNode> activeNodes = new LinkedList<RTreeNode>();
		activeNodes.add(prt.root);
		Stack<LinkedList<RTreeNode>> prunedNodes = new Stack<LinkedList<RTreeNode>>();
		RTreeNode minNode = searchSmallest(udf, dudfs, activeNodes, prunedNodes);
		
		//3. searching the remain k-1 ones
		//using the last activeNodes and prunedNodes
		
		//4. output
		int fanout = (int)Math.pow(numSeps, numDimensions);
		int leaf_num = (int)Math.pow(fanout, numLevels);
		System.out.println(visit_cnt);
		System.out.println(leaf_num);
		System.out.println(minNode);
		System.out.println(Math.round( visit_cnt*1.0/leaf_num * 100000.0 )/1000.0 + "%");
		System.out.println(nodeCache.get(minNode.id)[0]);
	}

	private static RTreeNode searchSmallest(
			Expression udf, Expression[] dudfs,
			LinkedList<RTreeNode> activeNodes,Stack<LinkedList<RTreeNode>> prunedNodes
		) {
		while(true){
			//if at the end, return the first one if there are more than 1 smallest 
			if(!activeNodes.getFirst().hasSubNodes()){
				return activeNodes.getFirst();
			}else{//go on to the the next level
				
				//1. collect all sub-nodes from activeNodes
				LinkedList<RTreeNode> tmpList = new LinkedList<RTreeNode>();
				for(RTreeNode node : activeNodes){
					for(RTreeNode subN : node.subNodes){
						tmpList.add(subN);
					}
				}
				
				//2. prune and store the disgarded into prunedList, and make another activeNodes
				//2.1 get min of MinMaxDist
				double min_MinMaxDist = Double.POSITIVE_INFINITY;
				for(RTreeNode node : tmpList){
					double minmaxdist = getMinMaxDist(udf,dudfs, node);
					if(minmaxdist < min_MinMaxDist){
						min_MinMaxDist = minmaxdist;
					}
				}
				
				activeNodes.clear();
				//2.2 prune the nodes that having MinDist>min_MinMaxDist
				//    when MinDist<= min_MinMaxDist, put into activeNodes
				for (Iterator<RTreeNode> iterator = tmpList.iterator(); iterator.hasNext();) {
					RTreeNode node = iterator.next();
					double mindist = getMinDist(udf,dudfs, node);
					if(mindist <= min_MinMaxDist){
						activeNodes.add(node);
						iterator.remove();
					}
				}
				// the remaining is in prunedNodes
				if(! tmpList.isEmpty()){
					prunedNodes.add(tmpList);
				}
			}
		}
	}

	private static double getMinDist(Expression udf, Expression[] dudfs, RTreeNode node) {
		double[] res = getMinNMinMaxDist(udf,dudfs,node);
		return res [MinMaxDist.IDX_MINDIST];
	}

	private static double getMinMaxDist(Expression udf, Expression[] dudfs, RTreeNode node) {
		double[] res = getMinNMinMaxDist(udf,dudfs,node);
		return res [MinMaxDist.IDX_MINMAXDIST];
	}
	
	private static double[] getMinNMinMaxDist(Expression udf, Expression[] dudfs, RTreeNode node) {
		if(nodeCache.containsKey(node.id)){
			return nodeCache.get(node.id);
		}
		visit_cnt ++;
		
		//if node is leaf then applyUdf to get directly
		if(!node.hasSubNodes()){
			double[] res = new double[2];
			res[0] = res[1] = applyUdf(udf,node);
			return res;
		}else{//otherwise try to use MBR only
			//1.0 check range first, using vertexes only when all dudfs are consistent
			int i=0;
			int numDimensions = dudfs.length;
			for(;i<dudfs.length;i++){
				RangeExpression rexp = new RangeExpression(dudfs[i]);
				//set ranges of vars
				for(int varIdx = 0; varIdx < numDimensions; varIdx++){
					rexp.setVariable("x"+(varIdx+1), new Range(node.MBR_S[varIdx],node.MBR_T[varIdx]));
				}
				Range r = rexp.evaluate();
				if(r.hasChangedSign()){
					break;
				}
			}
			
			//2.0
			if(i<dudfs.length){//not all vars are consistent, then use the sub-Nodes
				//2.1 get min and minmax of all sub-Nodes 
				LinkedList<double[]> resList = new LinkedList<double[]> ();
				for(RTreeNode sn : node.subNodes){
					resList.add( getMinNMinMaxDist(udf, dudfs, sn) );
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
				nodeCache.put(node.id, res);
				return res;
				
			}else{ // all are consistent, use the vertexes
				double[] res = MinMaxDist.calc(udf, dudfs.length, node);
				nodeCache.put(node.id, res);
				return res;
			}
		}
	}

	private static double applyUdf(Expression udf, RTreeNode leafNode) {
		if(nodeCache.containsKey(leafNode.id)){
			return nodeCache.get(leafNode.id)[0];
		}else{
			visit_cnt++;
			for(int i=0; i< leafNode.val.length; i++){
				udf.setVariable("x"+(i+1), leafNode.val[i]);
			}
			double[] expVal = new double[2];
			expVal[0] = udf.evaluate();
			expVal[1] = expVal[0];
			nodeCache.put(leafNode.id, expVal);
			return expVal[0];
		}
	}

}
