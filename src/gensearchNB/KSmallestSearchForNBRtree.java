package gensearchNB;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;

import com.newbrightidea.util.Node;
import com.newbrightidea.util.RTree;

import gensearch.MinMaxDist;
import gensearch.Range;
import gensearch.RangeExpression;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class KSmallestSearchForNBRtree {
	
	//counting visiting times of nodes
	private static long visit_cnt = 0;
	
	private static long notconsistent_cnt = 0;
	private static long consistent_cnt = 0;
	
	//store cache of nodes, for 0:MinDist,1:MinMaxDist values(non-leaf node), and the 0,1:value of exp (leaf node)
	private static HashMap<Long,double[]> nodeCache = new HashMap<Long,double[]>();
	private static long cache_hits = 0;
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
	
	public static void main1(String[] args) {
		String[] fs={
				"((x1-x2)^2+(x3-x4)^2)^(1/2)/(x5-x6)",
				"(x1-x2)/( (x5-x6)*((x1-x2)^2+(x3-x4)^2)^(1/2) )",
				"(x2-x1)/( (x5-x6)*((x1-x2)^2+(x3-x4)^2)^(1/2) )",
				"(x3-x4)/( (x5-x6)*((x1-x2)^2+(x3-x4)^2)^(1/2) )",
				"(x4-x3)/( (x5-x6)*((x1-x2)^2+(x3-x4)^2)^(1/2) )",
				"-((x1-x2)^2+(x3-x4)^2)^(1/2) / (x5-x6)^2",
				"((x1-x2)^2+(x3-x4)^2)^(1/2) / (x5-x6)^2",
				};
		 test(6,10,32,fs,100,100000);
	}
	
	
	public static void main(String[] args) {
		String[] fs={
				"((x1-1)^2+(x2-2)^2)^(1/2)+((x1-3)^2+(x2-4)^2)^(1/2)",
				"(x1-1)/((x1-1)^2+(x2-2)^2)^(1/2)+(x1-3)/((x1-3)^2+(x2-4)^2)^(1/2)",
				"(x2-2)/((x1-1)^2+(x2-2)^2)^(1/2)+(x2-4)/((x1-3)^2+(x2-4)^2)^(1/2)",
				};
		 test(2,10,32,fs,100,100000);
	}
	
	public static void main2(String[] args) {
		String[] fs={
				"((x1-100)^2+(x2-200)^2)^(1/2)+((x1-1000)^2+(x2-2000)^2)^(1/2)",
				"(x1-100)/((x1-100)^2+(x2-200)^2)^(1/2)+(x1-1000)/((x1-1000)^2+(x2-2000)^2)^(1/2)",
				"(x2-200)/((x1-100)^2+(x2-200)^2)^(1/2)+(x2-2000)/((x1-1000)^2+(x2-2000)^2)^(1/2)",
				};
		 test(2,10,32,fs,500,100000);
	}
	
	public static void main3(String[] args) {

		String[] fs={
				"x1*x2*x3",
				"x2*x3",
				"x1*x3",
				"x1*x2",
				};
		 test(3,32,64,fs,100000,100000);

    }
	
	
	public static void test(int numDim,int minNum,int maxNum,String[] fs, double valRange,int cnt ) {
		//1. Input data
		String[] xs = new String[numDim];
		for(int i=0;i<numDim;i++){
			xs[i] = "x"+(i+1);
		}
		Expression udf = new ExpressionBuilder(fs[0])
				.variables(xs).build();
		Expression[] dudfs = new Expression[numDim];
		
		for(int i=0;i<numDim;i++){
			dudfs[i]= new ExpressionBuilder(fs[1+1])
					.variables(xs).build();
		}
		
		RTree<Double> rt = new RTree<Double>(maxNum,minNum,numDim,RTree.SeedPicker.QUADRATIC);
		float[] pt = new float[numDim];
		
		double min = Double.MAX_VALUE;
		for(int i=1; i<=cnt; i++){
			
			for(int j=0;j<numDim;j++){
				pt[j] = (float) (valRange * Math.random());
				udf.setVariable("x"+(j+1), pt[j]);
			}
			
			double val = udf.evaluate();
			rt.insert(pt, val);
			if(val<min){
				min = val;
			}
            System.out.println(i);
		}
		
		//2. searching the smallest one
		LinkedList<Node> activeNodes = new LinkedList<Node>();
		activeNodes.add(rt.getRoot() );
		Stack<LinkedList<Node>> prunedNodes = new Stack<LinkedList<Node>>();
		Node minNode = searchSmallest(udf, dudfs, activeNodes, prunedNodes);
		
		//3. searching the remain k-1 ones 
		//using the last activeNodes and prunedNodes
		
		//4. output
		printOutput(minNode,cnt,min,notconsistent_cnt,consistent_cnt);
		
	}

	static void printOutput(Node minNode, double cnt, double min, long notconsistent_cnt2, long consistent_cnt2){
		System.out.println("cache_hits:"+cache_hits);
		System.out.println("visit_cnt rate:"+visit_cnt+"/"+cnt);
		System.out.println("saving rate:"+Math.round( (1- visit_cnt*1.0/(cnt*1.0) ) *10000)/100.0+"%") ;
		System.out.println("$real min:"+min);
		System.out.println("$calc min:"+minNode);
		System.out.println("nonconsistent:"+notconsistent_cnt2+",consistent:"+consistent_cnt2);
	}
	
    

	//RTree.Node
	private static Node searchSmallest(
			Expression udf, Expression[] dudfs,
			LinkedList<Node> activeNodes,Stack<LinkedList<Node>> prunedNodes
		) {
		while(true){
			//if at the end, return the first one if there are more than 1 smallest 
			if(activeNodes.getFirst().isEntry()){
				return activeNodes.getFirst();
			}else{//go on to the the next level
				
				//1. collect all sub-nodes from activeNodes
				LinkedList<Node> tmpList = new LinkedList<Node>();
				for(Node node : activeNodes){
					System.out.println("node children cnt:"+node.children.size());
					for(Node subN : node.children){
						tmpList.add(subN);
					}
				}
				
				//2. prune and store the disgarded into prunedList, and make another activeNodes
				//2.1 get min of MinMaxDist
				double min_MinMaxDist = Double.POSITIVE_INFINITY;
				for(Node node : tmpList){
					double minmaxdist = getMinMaxDist(udf,dudfs, node);
					if(minmaxdist < min_MinMaxDist){
						min_MinMaxDist = minmaxdist;
					}
				}
				
				activeNodes.clear();
				//2.2 prune the nodes that having MinDist>min_MinMaxDist
				//    when MinDist<= min_MinMaxDist, put into activeNodes
				for (Iterator<Node> iterator = tmpList.iterator(); iterator.hasNext();) {
					Node node = iterator.next();
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

	private static double getMinDist(Expression udf, Expression[] dudfs, Node node) {
		double[] res = getMinNMinMaxDist(udf,dudfs,node);
		return res [MinMaxDist.IDX_MINDIST];
	}

	private static double getMinMaxDist(Expression udf, Expression[] dudfs, Node node) {
		double[] res = getMinNMinMaxDist(udf,dudfs,node);
		return res [MinMaxDist.IDX_MINMAXDIST];
	}
	
	private static double[] getMinNMinMaxDist(Expression udf, Expression[] dudfs, Node node) {
		if(nodeCache.containsKey(node.id)){
			cache_hits ++;
			return nodeCache.get(node.id);
		}
		visit_cnt ++;
		
		//if node is leaf then applyUdf to get directly
		if(node.isEntry()){
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
					rexp.setVariable("x"+(varIdx+1), new Range(node.MBR_S()[varIdx],node.MBR_T()[varIdx]));
				}
				Range r = rexp.evaluate();
				if(r.hasChangedSign()){
					break;
				}
			}
			
			//2.0
			if(i<dudfs.length){//not all vars are consistent, then use the sub-Nodes
				//System.out.println("not cons");
				notconsistent_cnt++;
				//2.1 get min and minmax of all sub-Nodes 
				LinkedList<double[]> resList = new LinkedList<double[]> ();
				for(Node sn : node.children){
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
				//System.out.println("yes cons");
				consistent_cnt++;
				double[] res = MinMaxDistNB.calc(udf, dudfs.length, node);
				nodeCache.put(node.id, res);
				return res;
			}
		}
	}

	private static double applyUdf(Expression udf, Node leafNode) {
		if(nodeCache.containsKey(leafNode.id)){
			cache_hits++;
			return nodeCache.get(leafNode.id)[0];
		}else{
			//visit_cnt++;
			for(int i=0; i< leafNode.val().length; i++){
				udf.setVariable("x"+(i+1), leafNode.val()[i]);
			}
			double[] expVal = new double[2];
			expVal[0] = udf.evaluate();
			expVal[1] = expVal[0];
			nodeCache.put(leafNode.id, expVal);
			return expVal[0];
		}
	}

}
