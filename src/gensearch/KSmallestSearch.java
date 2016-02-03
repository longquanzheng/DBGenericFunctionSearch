package gensearch;

public class KSmallestSearch {
	
	
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
		 visit_num: num of visiting node in r-tree
		 speed_up: percent of speeding up compare to linear searching = visit_num / leaf_num * 100%
		 tuples as k tuples having smallest value of function udf 
		 <x1,x2,...,xn> 
		 ... 
	 * @param args
	 */
	public static void main(String[] args) {
		//for(int i=0; i<args.length; i++){
			//System.out.println(args[i]);
		//}
		
		/**
		 * 
		 */
		
	}

}
