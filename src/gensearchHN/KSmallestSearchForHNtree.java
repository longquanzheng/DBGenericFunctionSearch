package gensearchHN;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.Stack;

import gensearch.MinMaxDist;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import qlong.hntree.HNTree;
import qlong.hntree.HNTreeNode;

public class KSmallestSearchForHNtree {

    // counting visiting times of nodes
    private static long visit_cnt = 0;
    private static long entry_visit_cnt = 0;

    // store cache of nodes, for 0:MinDist,1:MinMaxDist values(non-leaf node),
    // and the 0,1:value of exp (leaf node)
    private static HashMap<Long, double[]> nodeCache = new HashMap<Long, double[]>();
    private static long cache_hits = 0;

    private static int cons_cnt = 0;
    private static int uncons_cnt = 0;
    private static int uncons_bad_cnt = 0;
    
    /**
     * input: numDimensions:num of dimensions, also num of vars in udf
     * numLevels:num of levels in r-tree numSeps: number of separates for a
     * R-tree node on one axis, for building a Perfect R-tree(no overlaps) k:k
     * smallest searching target number udf: user defined function, using
     * x1,x2...xn as var in each dimension dudfs: every patial derivative of udf
     * for each xi dudf for x1 dudf for x2 ... dudf for xn
     * 
     * 
     * output: leaf_num: num of leaves in r-tree = fanout^levels which fanout =
     * (int)Math.pow(numSeps, numDimensions); visit_num: num of visiting node in
     * r-tree speed_up: percent of speeding up compare to linear searching =
     * visit_num / leaf_num * 100% tuples as k tuples having smallest value of
     * function udf <x1,x2,...,xn> ...
     * 
     * @param args
     */
    public static void main(String[] args) {

        // 1. Input data
        int numDim = 6;
        int minCh = 8;
        int maxCh = 32;
        int entryNum = 5000000;

        String[] fs = { 
                "((x1-x2)^2+(x3-x4)^2)^(1/2)/(x5-x6)",

                "(x1-x2)/( (x5-x6)*((x1-x2)^2+(x3-x4)^2)^(1/2) )", 
                "(x2-x1)/( (x5-x6)*((x1-x2)^2+(x3-x4)^2)^(1/2) )", 
                "(x3-x4)/( (x5-x6)*((x1-x2)^2+(x3-x4)^2)^(1/2) )", 
                "(x4-x3)/( (x5-x6)*((x1-x2)^2+(x3-x4)^2)^(1/2) )",
                "-((x1-x2)^2+(x3-x4)^2)^(1/2) / (x5-x6)^2", 
                "((x1-x2)^2+(x3-x4)^2)^(1/2) / (x5-x6)^2", };

        Expression udf = new ExpressionBuilder(fs[0]).variables("x1", "x2", "x3", "x4", "x5", "x6").build();
        Expression[] dudfs = new Expression[numDim];
        dudfs[0] = new ExpressionBuilder(fs[1]).variables("x1", "x2", "x3", "x4", "x5", "x6").build();
        dudfs[1] = new ExpressionBuilder(fs[2]).variables("x1", "x2", "x3", "x4", "x5", "x6").build();
        dudfs[2] = new ExpressionBuilder(fs[3]).variables("x1", "x2", "x3", "x4", "x5", "x6").build();
        dudfs[3] = new ExpressionBuilder(fs[4]).variables("x1", "x2", "x3", "x4", "x5", "x6").build();
        dudfs[4] = new ExpressionBuilder(fs[5]).variables("x1", "x2", "x3", "x4", "x5", "x6").build();
        dudfs[5] = new ExpressionBuilder(fs[6]).variables("x1", "x2", "x3", "x4", "x5", "x6").build();

        HNTree rt = new HNTree(numDim, minCh, maxCh);

        double min = Double.MAX_VALUE;
        float[] pt = new float[numDim];

        Random rand = new Random(System.currentTimeMillis());

        for (int i = 0; i < entryNum; i++) {

            for (int j = 0; j < numDim; j++) {
                pt[j] = rand.nextFloat() * 100;
            }

            for (int j = 0; j < numDim; j++) {
                udf.setVariable("x" + (j + 1), pt[j]);
            }

            double val = udf.evaluate();
            rt.insert(pt);
            if (val < min) {
                min = val;
            }
            if (i % 5000 == 0 && i > 0) {
                System.out.println("insert ###################" + i + "$" + val + pt[0] + "," + pt[1]);
                resetStatis();
                // 2. searching the smallest one
                LinkedList<HNTreeNode> activeNodes = new LinkedList<HNTreeNode>();
                activeNodes.add(rt.root);
                Stack<LinkedList<HNTreeNode>> prunedNodes = new Stack<LinkedList<HNTreeNode>>();
                HNTreeNode minNode = searchSmallest(udf, dudfs, activeNodes, prunedNodes);

                // 3. searching the remain k-1 ones
                // using the last activeNodes and prunedNodes

                // 4. output
                System.out.println("cache_hits" + cache_hits);
                System.out.println("entryNum" + i);
                System.out.println("visit_cnt" + visit_cnt);
                System.out.println("entry_visit_cnt" + entry_visit_cnt);
                System.out.println("visit_cnt / entryNum" + Math.round(visit_cnt * 1.0 / i * 100000.0) / 1000.0 + "%");
                System.out.println("entry_visit_cnt / entryNum" + Math.round(entry_visit_cnt * 1.0 / i * 100000.0) / 1000.0 + "%");
                System.out.println("visit_cnt+entry_visit_cnt / entryNum" + Math.round((visit_cnt+entry_visit_cnt) * 1.0 / i * 100000.0) / 1000.0 + "%");
                System.out.println(minNode.id);
                System.out.println(min);
                double calcMin = applyUdf(udf, minNode);
                System.out.println(calcMin);
                if (min != calcMin) {
                    System.err.println("not match!");
                }
                System.out.println("cons_cnt" + cons_cnt);
                System.out.println("uncons_cnt" + uncons_cnt);
                System.out.println("uncons_bad_cnt" + uncons_bad_cnt);
                rand = new Random(System.currentTimeMillis());
            }
        }



    }

    private static void resetStatis() {
        visit_cnt = entry_visit_cnt = cache_hits = cons_cnt = uncons_cnt = uncons_bad_cnt = 0;

        // store cache of nodes, for 0:MinDist,1:MinMaxDist values(non-leaf
        // node), and the 0,1:value of exp (leaf node)
        nodeCache = new HashMap<Long, double[]>();

    }
    private static HNTreeNode searchSmallest(Expression udf, Expression[] dudfs, LinkedList<HNTreeNode> activeNodes, Stack<LinkedList<HNTreeNode>> prunedNodes) {
        while (true) {
            // if at the end, return the first one if there are more than 1
            // smallest
            if (activeNodes.getFirst().isEntry()) {
                return activeNodes.getFirst();
            } else {// go on to the the next level

                // 1. collect all sub-nodes from activeNodes
                LinkedList<HNTreeNode> tmpList = new LinkedList<HNTreeNode>();
                for (HNTreeNode node : activeNodes) {
                    for (HNTreeNode subN : node.children) {
                        tmpList.add(subN);
                    }
                }

                // 2. prune and store the disgarded into prunedList, and make
                // another activeNodes
                // 2.1 get min of MinMaxDist
                double min_MinMaxDist = Double.POSITIVE_INFINITY;
                for (HNTreeNode node : tmpList) {
                    double minmaxdist = getMinMaxDist(udf, dudfs, node);
                    if (minmaxdist < min_MinMaxDist) {
                        min_MinMaxDist = minmaxdist;
                    }
                }

                activeNodes.clear();
                // 2.2 prune the nodes that having MinDist>min_MinMaxDist
                // when MinDist<= min_MinMaxDist, put into activeNodes
                for (Iterator<HNTreeNode> iterator = tmpList.iterator(); iterator.hasNext();) {
                    HNTreeNode node = iterator.next();
                    double mindist = getMinDist(udf, dudfs, node);
                    if (mindist <= min_MinMaxDist) {
                        activeNodes.add(node);
                        iterator.remove();
                    }
                }
                // the remaining is in prunedNodes
                if (!tmpList.isEmpty()) {
                    prunedNodes.add(tmpList);
                }
            }
        }
    }

    private static double getMinDist(Expression udf, Expression[] dudfs, HNTreeNode node) {
        double[] res = getMinNMinMaxDist(udf, dudfs, node);
        return res[MinMaxDist.IDX_MINDIST];
    }

    private static double getMinMaxDist(Expression udf, Expression[] dudfs, HNTreeNode node) {
        double[] res = getMinNMinMaxDist(udf, dudfs, node);
        return res[MinMaxDist.IDX_MINMAXDIST];
    }

    private static double[] getMinNMinMaxDist(Expression udf, Expression[] dudfs, HNTreeNode node) {
        if (nodeCache.containsKey(node.id)) {
            cache_hits++;
            return nodeCache.get(node.id);
        }


        // if node is leaf then applyUdf to get directly
        if (node.isEntry()) {
            double[] res = new double[2];
            res[0] = res[1] = applyUdf(udf, node);
            return res;
        } else {// otherwise try to use MBR only
                // 1.0 check range first, using vertexes only when all dudfs are
                // consistent
            visit_cnt++;
            
            boolean isConsistent = MinMaxDistHN.isConsistent(dudfs,node);

            // 2.0
            if (!isConsistent) {// not all vars are consistent, then use the
                                   // sub-Nodes
                // 2.1 get min and minmax of all sub-Nodes
                uncons_cnt++;
                if (myJudgeBad(node.getMBR().getS()[4], node.getMBR().getT()[4], node.getMBR().getS()[5], node.getMBR().getT()[5])) {
                    uncons_bad_cnt++;
                }
                LinkedList<double[]> resList = new LinkedList<double[]>();
                for (HNTreeNode sn : node.children) {
                    resList.add(getMinNMinMaxDist(udf, dudfs, sn));
                }
                // 2.2 get the min of all min as min, the min of all minmax as
                // min max
                double[] res = new double[2];
                res[0] = res[1] = Double.POSITIVE_INFINITY;
                for (double[] ele : resList) {
                    if (ele[0] < res[0]) {
                        res[0] = ele[0];
                    }
                    if (ele[1] < res[1]) {
                        res[1] = ele[1];
                    }
                }
                // 2.3 cache it and return
                nodeCache.put(node.id, res);
                return res;

            } else { // all are consistent, use the vertexes
                cons_cnt++;
                double[] res = MinMaxDistHN.calc(udf, dudfs.length, node);
                nodeCache.put(node.id, res);
                return res;
            }
        }
    }

    private static boolean myJudgeBad(float x51, float x52, float x61, float x62) {

        if (x51 > x62 || x52 < x61) {
            return false;
        } else {
            return true;
        }
    }

    private static double applyUdf(Expression udf, HNTreeNode leafNode) {
        if (nodeCache.containsKey(leafNode.id)) {
            cache_hits++;
            return nodeCache.get(leafNode.id)[0];
        } else {
            entry_visit_cnt++;
            for (int i = 0; i < leafNode.getVal().length; i++) {
                udf.setVariable("x" + (i + 1), leafNode.getVal()[i]);
            }
            double[] expVal = new double[2];
            expVal[0] = udf.evaluate();
            expVal[1] = expVal[0];
            nodeCache.put(leafNode.id, expVal);
            return expVal[0];
        }
    }

}
