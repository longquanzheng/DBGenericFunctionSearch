package gensearch;

import java.util.ArrayList;
import java.util.Collections;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import rstar.spatial.HyperRectangle;

/**
 * vertex of MBR
 * @author qlong
 *
 */
public class Vertex implements Comparable<Vertex> {

	public String index;
	public double expVal;
	
	public Vertex(String index){
		this.index = index;
	}
	
	public Vertex(String index, double val){
		this.index = index;
		this.expVal = val;
	}
	
	public String toString(){
		return "("+index+"):"+expVal;
	}
	
	public static void main(String[] args) {
//		ArrayList<Vertex> list = new ArrayList<Vertex>();
//		list.add(new Vertex("",5));
//		list.add(new Vertex("",2));
//		list.add(new Vertex("",3));
//		Collections.sort(list);
//		System.out.println(list);
		double[] pt = {75.35218048095703,96.04236602783203,13.557190895080566,79.02488708496094,39.57847595214844,41.61790084838867,};
		double[] pt2 = {38.74238204956055,17.372730255126953,38.160213470458984,75.47753143310547,98.62510681152344,84.5553970336914,};
		Expression exp = new ExpressionBuilder("((x1-x2)^2+(x3-x4)^2)^(1/2)/(x5-x6)").variables("x1","x2","x3","x4","x5","x6").build();
		for(int i=1;i<=6;i++){
			exp.setVariable("x"+i, pt2[i-1]);
		}
		System.out.println(exp.evaluate());
	}

	public void eval(Expression exp, int numDimensions, double[] mBR_S, double[] mBR_T) {
		for(int i=1; i<=numDimensions; i++){
			String varN = "x"+i;
			if(index.charAt(i-1)=='0'){
				exp.setVariable(varN, mBR_S[i-1]);
			}else{
				exp.setVariable(varN, mBR_T[i-1]);
			}
			//System.out.println("i"+(i-1)+","+index.charAt(i-1)+","+mBR_S[i-1]+","+mBR_T[i-1]);
			
		}
		expVal = exp.evaluate();
		//System.out.println(expVal);
	}

	@Override
	public int compareTo(Vertex v2) {
		if(this.expVal==v2.expVal){
			return 0;
		}else if(this.expVal>v2.expVal){
			return 1;
		}else{
			return -1;
		}
	}

	public void eval(Expression exp, int numDimensions, HyperRectangle mbr) {
		for(int i=1; i<=numDimensions; i++){
			String varN = "x"+i;
			if(index.charAt(i-1)=='0'){
				exp.setVariable(varN, mbr.points[i-1][1]);
			}else{
				exp.setVariable(varN, mbr.points[i-1][0]);
			}
			
		}
		
		expVal = exp.evaluate();
		//System.out.println(expVal);
	}

	
}
