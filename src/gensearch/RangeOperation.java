package gensearch;

import net.objecthunter.exp4j.operator.Operator;

public class RangeOperation {

	private Operator op;
	public RangeOperation(Operator op){
		this.op = op;
	}
	
	public Range applyRangeOperation(Range left, Range right){
		switch(op.getSymbol()){
		case "+":
			return rangePlus(left,right);
		case "-":
			return rangePlus(left, rangeNegative(right));
		case "*":
			break;
		case "/":
			break;
			default:
				
		}
		return null;
	}
	
	public Range applyRangeOperation(Range arg){
		return null;
	}
	
	private Range rangeNegative(Range arg){
		double[][] ranges = new double[arg.getNum()][2];
		for(int i=0; i<arg.getNum(); i++){
			ranges[i][Range.LEFT] = -arg.datas[i][Range.RIGHT];
			ranges[i][Range.RIGHT] = -arg.datas[i][Range.LEFT];
		}
		return new Range(ranges);
	}
	
	private Range rangePlus(Range left, Range right) {
		double[][] ranges = new double [left.getNum() * right.getNum()][2];
		int cnt = 0;
		for(int i=0; i<left.getNum(); i++){
			for(int j=0; j<right.getNum();j++){
				ranges[cnt][Range.LEFT] = left.datas[i][0]+right.datas[j][Range.LEFT];
				ranges[cnt][1] = left.datas[i][Range.RIGHT]+right.datas[j][Range.RIGHT];
			}
		}
		return new Range(ranges);
	}

	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
