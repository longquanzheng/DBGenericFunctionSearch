package gensearch;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class MinDist {

	public static void main(String[] args) {
		Expression exp = new ExpressionBuilder("x1*x2*x3").variables("x1","x2","x3").build();
		double[] mbrS={1,2,3}, mbrT={4,5,6};
		RTreeNode rtn = new RTreeNode();
		rtn.MBR_S = mbrS;
		rtn.MBR_T = mbrT;
		System.out.println(calc(exp,3,rtn));
	}

	public static double calc(Expression exp, int numDimensions, RTreeNode node){
		double min = Double.POSITIVE_INFINITY;
		double[] comb = new double[numDimensions];
		min = calcRecursively(exp,numDimensions,0,min,node.MBR_S,node.MBR_T,comb);
		return min;
	}

	private static double calcRecursively(Expression exp, int numDimensions, int curr, 
			double min, double[] mbr_S,double[] mbr_T, double[] comb) {
		if(curr == numDimensions-1){//at the last
			double newV;
			//MBR_S
			comb[curr] = mbr_S[curr];
			for(int i=1; i<=numDimensions; i++){
				String varN = "x"+i;
				exp.setVariable(varN, comb[i-1]);
			}
			newV = exp.evaluate();
			//System.out.println(newV);
			if(newV<min){
				min=newV;
			}
			//MBR_T
			comb[curr] = mbr_T[curr];
			for(int i=1; i<=numDimensions; i++){
				String varN = "x"+i;
				exp.setVariable(varN, comb[i-1]);
			}
			newV = exp.evaluate();
			//System.out.println(newV);
			if(newV<min){
				min=newV;
			}
			return min;
		}else{//fill into comb[curr] and goto next
			//MBR_S
			comb[curr] = mbr_S[curr];
			min = calcRecursively(exp,numDimensions,curr+1,min,mbr_S,mbr_T,comb);
			//MBR_T
			comb[curr] = mbr_T[curr];
			return calcRecursively(exp,numDimensions,curr+1,min,mbr_S,mbr_T,comb);
		}
	}
}
