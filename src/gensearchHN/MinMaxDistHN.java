package gensearchHN;

import java.util.ArrayList;
import java.util.Collections;

import gensearch.Vertex;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import qlong.hntree.HNTreeNode;

public class MinMaxDistHN {

	public static void main(String[] args) {
		//Expression exp = new ExpressionBuilder("x1*x2*x3").variables("x1","x2","x3").build();
		//float[] mbrS={1,2,3}, dim={3,3,3};
		int numDim = 6;
		Expression exp = new ExpressionBuilder("((x1-x2)^2+(x3-x4)^2)^(1/2)/(x5-x6)").variables("x1","x2","x3","x4","x5","x6").build();
		float[] mbrs = {38.742382f,	17.37273f,	2.3635745f,	72.080864f,	87.266106f,	73.88579f};			
		float[] mbrt = {98.08647f,	24.163364f,	38.160213f,	99.2525f,	98.62511f,	84.5554f};	
		float[] dim = new float[mbrs.length];
		for(int i=0;i<mbrs.length;i++){
			dim[i] = mbrt[i]-mbrs[i];
		}
        HNTreeNode rtn = new HNTreeNode(null, mbrs, mbrt, false);
		double[] res = calc(exp,6,rtn);
		System.out.println(res[IDX_MINDIST]+"  "+ res[IDX_MINMAXDIST]);
	}

	public static final int IDX_MINDIST = 0;
	public static final int IDX_MINMAXDIST = 1;
	
	/**
	 * 
	 * Apply when all dudfs are consistent in MBR
	1. get all vertexes into array
	2. sort all vertexes according to value in exp into a sorted array (desc order)
	3. for each hyperplane, get all vertexes on it(half of all in MBR)
	   then match them into the sorted array, and get the first one as max,
	   then compare to get the smallest one as MinMax
	4. return [0:MinDist 1:MinMaxDist]
	 
	 **/
    public static double[] calc(Expression exp, int numDimensions, HNTreeNode node) {
		double[] res = new double[2];
		res[IDX_MINDIST] = Double.POSITIVE_INFINITY;
		res[IDX_MINMAXDIST] = Double.POSITIVE_INFINITY;
		
		//1.1 gen all indexes of all vertexes
		ArrayList<Vertex> vertexes = new ArrayList<Vertex>();
		genAllVertexes( numDimensions, 0, "", vertexes);
		
		//1.2 calc all vals of vertexes, and gen hashMap for them	
		for(Vertex vertex:vertexes){
            vertex.eval(exp, numDimensions, node.DMBR_S(), node.DMBR_T());
			if(vertex.expVal < res[IDX_MINDIST]){
				res[IDX_MINDIST] = vertex.expVal;
			}
		}
		/*
		vertexes.forEach(vertex->{
			vertex.eval(exp,numDimensions,node.MBR_S(),node.MBR_T());
			if(vertex.expVal < res[IDX_MINDIST]){
				res[IDX_MINDIST] = vertex.expVal;
			}
		});
		*/
		
		//2.1 sort (in increasing order)
		Collections.sort(vertexes);
		
		//3.1 for each hyperplane, get all vertexes on it using excluding method!!!!
		// and get the max of them using the sorted array
		for(int i=0; i<numDimensions; i++){
			double currMax=Double.POSITIVE_INFINITY;
			//MBR_S
			for(int j=vertexes.size()-1; j>=0; j--){
				if(vertexes.get(j).index.charAt(i)=='0'){
					currMax = vertexes.get(j).expVal;
					break;
				}
			}
			if(currMax<res[IDX_MINMAXDIST]){
				res[IDX_MINMAXDIST] = currMax;
			}
			//MBR_T
			for(int j=vertexes.size()-1; j>=0; j--){
				if(vertexes.get(j).index.charAt(i)=='1'){
					currMax = vertexes.get(j).expVal;
					break;
				}
			}
			if(currMax<res[IDX_MINMAXDIST]){
				res[IDX_MINMAXDIST] = currMax;
			}
		}
		
		return res;
	}

	private static void genAllVertexes( int numDimensions, int currDim, String prefix, ArrayList<Vertex> vertexes) {
		if(currDim == numDimensions-1){//last one, write into vertexes
			String idx = prefix+"0";
			Vertex v = new Vertex(idx);
			vertexes.add(v);
			idx = prefix+"1";
			v = new Vertex(idx);
			vertexes.add(v);
		}else{//add prefix and goto next
			genAllVertexes(numDimensions,currDim+1,prefix+"0",vertexes);
			genAllVertexes(numDimensions,currDim+1,prefix+"1",vertexes);
		}
	}

	
}


