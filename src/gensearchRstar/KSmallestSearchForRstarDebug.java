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

public class KSmallestSearchForRstarDebug {
	
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
	
	public static void main7(String[] args) {
		//try bad case
		String[] fs={
				"1/(x1-x2)^2",
				"-2/(x1-x2)^3",
				"2/(x1-x2)^3",
				};
		 test(2,10,32,fs,1,100000,5);
	}
	
	public static void main(String[] args) {
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
		 test(6,4,8,fs,100,5000,1);
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
		double[][] data = {

				{91.89842224121094,69.83403015136719,46.84357833862305,1.93837571144104,82.04381561279297,88.44558715820312},
				{65.25174713134766,23.693906784057617,33.16978073120117,72.08086395263672,87.26610565185547,73.88578796386719},
				{96.448486328125,73.70773315429688,91.79283905029297,72.02082061767578,41.00385284423828,9.374876022338867},
				{44.45531463623047,57.44092559814453,44.757484436035156,6.438243389129639,14.878033638000488,94.81375885009766},
				{20.765554428100586,35.02088928222656,10.86559009552002,4.819947242736816,92.3674545288086,22.965534210205078},
				{20.77608871459961,33.04037857055664,71.58048248291016,21.433847427368164,29.314220428466797,82.25415802001953},
				{26.913070678710938,70.82585144042969,86.51998901367188,11.189830780029297,49.1322021484375,14.837486267089844},
				{64.90493774414062,46.22950744628906,36.509246826171875,24.243812561035156,35.488319396972656,61.64756774902344},
				{24.681385040283203,21.38998031616211,29.91168212890625,58.6725959777832,80.50431060791016,41.39146423339844},
				{32.860904693603516,13.993331909179688,64.13507843017578,26.689743041992188,66.70064544677734,74.28636169433594},
				{39.07231521606445,27.87906265258789,88.52684783935547,44.92534255981445,75.9941177368164,75.61444854736328},
				{18.768829345703125,58.23857116699219,52.97258758544922,95.93067169189453,60.02311706542969,9.513802528381348},
				{2.0067927837371826,64.44646453857422,44.97715759277344,17.406139373779297,89.80462646484375,33.69767379760742},
				{73.0816879272461,99.48692321777344,49.35701370239258,22.237754821777344,57.05628204345703,53.378597259521484},
				{98.08647155761719,24.16336441040039,2.363574504852295,99.25250244140625,88.21498107910156,83.36744689941406},
				{85.4632339477539,70.4236068725586,35.4847526550293,78.09174346923828,79.95763397216797,35.358238220214844},
				{63.47852325439453,78.2450942993164,3.6670174598693848,96.61784362792969,82.52436065673828,70.47358703613281},
				{24.222488403320312,40.09391403198242,94.53326416015625,94.1128921508789,65.94132232666016,1.3462270498275757},
				{78.10609436035156,94.92301177978516,6.549501895904541,74.9384994506836,61.85968017578125,82.8998031616211},
				{38.74238204956055,17.372730255126953,38.160213470458984,75.47753143310547,98.62510681152344,84.5553970336914},
				{4.3152570724487305,18.080913543701172,30.750192642211914,0.6858145594596863,43.764163970947266,0.25490906834602356},
				{67.77044677734375,77.03889465332031,75.91974639892578,27.49493980407715,51.65120315551758,86.32166290283203},
				{10.05247974395752,66.419677734375,12.936616897583008,51.114925384521484,35.97178268432617,54.07319641113281},
				{54.740562438964844,23.368328094482422,72.2470932006836,50.91275405883789,58.93345642089844,15.999394416809082},
				{71.32491302490234,21.433006286621094,43.42206573486328,97.3538818359375,50.13610076904297,46.396995544433594},
				{51.47019958496094,74.61698150634766,74.65113067626953,6.288771152496338,46.683956146240234,73.22178649902344},
				{77.32917022705078,26.872034072875977,20.5859375,65.88374328613281,27.52018165588379,44.63378143310547},
				{93.628662109375,17.096446990966797,6.617648601531982,32.411354064941406,5.0481648445129395,86.75654602050781},
				{62.122520446777344,23.64590835571289,42.13461685180664,80.04229736328125,27.767562866210938,87.47486114501953},
				{55.297428131103516,39.752471923828125,0.4643535614013672,36.270286560058594,41.728294372558594,52.019317626953125},
				{59.0735969543457,92.24998474121094,57.5162467956543,76.64128112792969,94.86260223388672,0.4321485161781311},
				{35.744571685791016,19.238248825073242,99.00215148925781,51.4744987487793,46.400447845458984,34.01414489746094},
				{18.56812286376953,69.83287048339844,32.85353088378906,83.11860656738281,49.251708984375,30.194116592407227},
				{99.21099853515625,81.43171691894531,80.34516906738281,88.2824478149414,3.0318491458892822,24.05257225036621},
				{45.5584716796875,50.90807342529297,28.684473037719727,27.85464859008789,61.835323333740234,64.52141571044922},
				{78.05691528320312,18.177927017211914,57.5755615234375,71.55572509765625,17.38033103942871,57.05970764160156},
				{1.5339395999908447,69.4854507446289,61.60174560546875,40.363162994384766,44.15379333496094,8.79736328125},
				{61.04302978515625,87.7446517944336,74.20728302001953,82.96568298339844,79.00887298583984,11.607856750488281},
				{75.35218048095703,96.04236602783203,13.557190895080566,79.02488708496094,39.57847595214844,41.61790084838867},
				{21.821304321289062,74.06143951416016,57.50634002685547,90.67772674560547,51.07390594482422,27.025171279907227},
				{59.110321044921875,65.27376556396484,67.14869689941406,1.8635510206222534,22.682815551757812,69.95021057128906},
				{97.88629913330078,23.269332885742188,4.713242530822754,63.50714874267578,76.94685363769531,81.00897979736328},
				{68.84759521484375,79.95516967773438,81.63249969482422,70.57128143310547,8.206336975097656,49.408695220947266},
				{7.288681507110596,16.44046974182129,62.07680892944336,27.18515396118164,10.404096603393555,77.13031005859375},
				{25.359769821166992,12.521812438964844,82.70471954345703,49.57234191894531,37.13778305053711,27.091514587402344},
				{89.33828735351562,74.53390502929688,13.346567153930664,27.89312171936035,15.671370506286621,94.44822692871094},
				{57.768638610839844,49.112022399902344,19.271753311157227,30.37114906311035,45.43196105957031,3.1905500888824463},
				{76.32499694824219,25.735939025878906,22.984424591064453,22.617708206176758,45.0344352722168,51.1598014831543},
				{11.728431701660156,90.84217834472656,42.47992706298828,76.76258087158203,30.73805046081543,93.23199462890625},
				{45.961341857910156,96.29029083251953,79.8445053100586,17.148954391479492,85.02854919433594,30.793127059936523},
				
				
				};
		
		for(int i=0; i<data.length; i++){
			
			for(int j=0;j<numDim;j++){
				pt[j] = (float)data[i][j];
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
		if(! ("Entry: "+min).equals(minPt.toString()) ){
			System.err.println("not equal!!!!"+minPt);
			System.out.println(  applyUdfPt(udf,minPt) );
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
				System.out.println("not cons");
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
