package gensearch;

public class KNNSearch {
	
	//input: 
	// n:num of dimensions, also num of vars in udf
	// l:num of levels in r-tree
	// f:avg fan out of r-tree
	// k:kNN target
	// udf: user defined function, using x1,x2...xn as var
	// dudfs: patial derivatives of udf for each xi
	//		dudf for x1
	//		dudf for x2
	//		...
	//		dudf for xn
	
	//output:
	// leaf_num: num of leaves in r-tree
	// visit_num: num of visiting node in r-tree
	// speed_up: percent of speeding up compare to linear searching
	public static void main(String[] args) {
		for(int i=0; i<args.length; i++){
			System.out.println(args[i]);
		}
	}

}
