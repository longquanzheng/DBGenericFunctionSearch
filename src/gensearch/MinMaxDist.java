package gensearch;

import java.util.ArrayList;
import java.util.Collections;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class MinMaxDist {

	public static void main(String[] args) {
		Expression exp = new ExpressionBuilder("x1*x2*x3").variables("x1","x2","x3").build();
		double[] mbrS={1,2,3}, mbrT={4,5,6};
		RTreeNode rtn = new RTreeNode();
		rtn.MBR_S = mbrS;
		rtn.MBR_T = mbrT;
		System.out.println(calc(exp,3,rtn));
	}

	//1. get all vertexes into array
	//2. sort all vertexes according to value in exp into a sorted array (desc order)
	//3. for each hyperplane, get all vertexes on it(half of all in MBR)
	//   then match them into the sorted array, and get the first one as max,
	//   then compare to get the smallest one as MinMax
	public static double calc(Expression exp, int numDimensions, RTreeNode node){
		
		//1.1 gen all indexes of all vertexes
		ArrayList<Vertex> vertexes = new ArrayList<Vertex>();
		genAllVertexes( numDimensions, 0, "", vertexes);
		
		//1.2 calc all vals of vertexes, and gen hashMap for them		 
		vertexes.forEach(vertex->{
			vertex.eval(exp,numDimensions,node.MBR_S,node.MBR_T);
		});
		
		//2.1 sort (in increasing order)
		Collections.sort(vertexes);
		
		//3.1 for each hyperplane, get all vertexes on it using excluding method!!!!
		// and get the max of them using the sorted array
		double min = Double.POSITIVE_INFINITY;
		for(int i=0; i<numDimensions; i++){
			double currMax=Double.POSITIVE_INFINITY;
			//MBR_S
			for(int j=vertexes.size()-1; j>=0; j--){
				if(vertexes.get(j).index.charAt(i)=='0'){
					currMax = vertexes.get(j).expVal;
					break;
				}
			}
			if(currMax<min){
				min = currMax;
			}
			//MBR_T
			for(int j=vertexes.size()-1; j>=0; j--){
				if(vertexes.get(j).index.charAt(i)=='1'){
					currMax = vertexes.get(j).expVal;
					break;
				}
			}
			if(currMax<min){
				min = currMax;
			}
		}
		
		return min;
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


