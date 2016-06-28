package qlong.hntree;

import java.awt.Color;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import gensearchHN.MinMaxDistHN;
import net.objecthunter.exp4j.Expression;

public class HNTree {

    private static final String KEY_SorT = "MBRS_OR_MBRT";

    private static final int VALUE_SorT_S = 0;
    private static final int VALUE_SorT_T = 1;

    private static final String KEY_axis = "axis";

    private static final String KEY_index = "index";

    private static final String KEY_MIN_CONSISTENT_0 = "min_consistent_0";
    private static final String KEY_MIN_CONSISTENT_1 = "min_consistent_1";

    public final int numDim, minChildren, maxChildren;

    public HNTreeNode root;

    private static Color[] colors = { Color.RED, Color.BLACK, Color.BLUE, Color.CYAN, Color.ORANGE, Color.PINK, Color.GREEN, };

    private int baseRange = 0;

    private Expression[] dudfs = null;

    public static void main(String[] args) {
        HNTree t = new HNTree(2, 8, 32);
        t.baseRange = 500;
        boolean useData = false;
        int entryNum = 1000;

        double[][] pts = { { 67.0, 490.0 }, { 237.0, 474.0 }, { 121.0, 476.0 }, { 114.0, 235.0 }, { 354.0, 411.0 }, { 354.0, 175.0 }, { 132.0, 413.0 }, { 121.0, 351.0 }, { 117.0, 192.0 }, { 282.0, 109.0 }, { 344.0, 449.0 }, { 44.0, 29.0 },
                { 214.0, 202.0 }, { 488.0, 418.0 }, { 266.0, 123.0 }, { 41.0, 243.0 }, { 225.0, 57.0 }, { 186.0, 420.0 }, { 351.0, 453.0 }, { 139.0, 173.0 }, { 48.0, 70.0 }, { 268.0, 180.0 }, { 496.0, 388.0 }, { 234.0, 71.0 }, { 271.0, 327.0 },
                { 160.0, 405.0 }, { 24.0, 497.0 }, { 118.0, 70.0 }, { 258.0, 371.0 }, { 337.0, 202.0 }, { 132.0, 445.0 }, { 304.0, 47.0 }, { 485.0, 146.0 }, { 308.0, 351.0 }, { 415.0, 455.0 }, { 38.0, 259.0 }, { 317.0, 433.0 }, { 25.0, 379.0 },
                { 208.0, 71.0 }, { 10.0, 83.0 }, { 1.0, 167.0 }, { 319.0, 344.0 }, { 100.0, 151.0 }, { 419.0, 348.0 }, { 320.0, 131.0 }, { 34.0, 363.0 }, { 108.0, 168.0 }, { 154.0, 349.0 }, { 450.0, 157.0 }, { 219.0, 38.0 }, { 131.0, 496.0 },
                { 480.0, 260.0 }, { 182.0, 79.0 }, { 317.0, 68.0 }, { 405.0, 11.0 }, { 247.0, 291.0 }, { 334.0, 332.0 }, { 435.0, 449.0 }, { 332.0, 423.0 }, { 271.0, 71.0 }, { 245.0, 175.0 }, { 400.0, 307.0 }, { 172.0, 433.0 }, { 152.0, 340.0 },
                { 431.0, 183.0 }, { 430.0, 7.0 }, { 190.0, 373.0 }, { 415.0, 174.0 }, { 48.0, 261.0 }, { 384.0, 334.0 }, { 20.0, 173.0 }, { 0.0, 94.0 }, { 486.0, 34.0 }, { 149.0, 141.0 }, { 91.0, 74.0 }, { 239.0, 22.0 }, { 0.0, 149.0 },
                { 398.0, 18.0 }, { 100.0, 155.0 }, { 92.0, 189.0 }, { 435.0, 86.0 }, { 26.0, 114.0 }, { 157.0, 377.0 }, { 26.0, 202.0 }, { 217.0, 250.0 }, { 231.0, 417.0 }, { 324.0, 435.0 }, { 64.0, 135.0 }, { 410.0, 206.0 }, { 315.0, 388.0 },
                { 420.0, 384.0 }, { 143.0, 92.0 }, { 260.0, 72.0 }, { 412.0, 487.0 }, { 68.0, 78.0 }, { 45.0, 356.0 }, { 280.0, 188.0 }, { 475.0, 250.0 }, { 103.0, 130.0 }, { 302.0, 341.0 }, };

        if (useData) {
            entryNum = pts.length;
        }
        for (int i = 0; i < entryNum; i++) {
            float[] pt = new float[2];

            if (useData) {
                pt[0] = (float) pts[i][0];
                pt[1] = (float) pts[i][1];
            } else {
                pt[0] = (float) (int) (Math.random() * t.baseRange);
                pt[1] = (float) (int) (Math.random() * t.baseRange);
            }
            t.insert(pt);
            System.out.println(i + "-insert:" + pt[0] + "," + pt[1] + "...");

        }
        System.out.println(t);
    }

    @Override
    public String toString() {
        // String s = "root:"+root.nodeId+"#\n";
        Visulizer.start(this.baseRange + 100);
        return toString(1, root);
    }

    private String toString(int curr, HNTreeNode node) {
        String ret = "";
        String tab = "";
        for (int i = 0; i < curr; i++) {
            tab += "##\t";
        }
        ret = tab + node.id + "$$" + node.getMBR() + "\n";
        float[] mbrS = node.getMBR().getS();
        float[] mbrT = node.getMBR().getT();
        
        Color color = colors[curr - 1];
        addRec(mbrS[0], mbrS[1], mbrT[0] - mbrS[0] + curr, mbrT[1] - mbrS[1] + curr, color);

        // Color randomColor = new Color((float) Math.random(), (float)
        // Math.random(), (float) Math.random());
        
        if (node.isLeaf()) {
            for (HNTreeNode sn : node.children) {
                String valS = "<";
                for (int i = 0; i < sn.getVal().length; i++) {
                    valS += "" + sn.getVal()[i] + ",";
                }
                addRec(sn.getVal()[0], sn.getVal()[1], 2, 2, color);

                valS += ">\n";
                // System.out.println("*" + valS);
                ret += tab + "+++++" + valS;
            }
        } else {
            for (HNTreeNode sn : node.children) {
                ret += toString(curr + 1, sn);
            }
        }

        return ret;
    }

    private void addRec(float x, float y, float w, float h, Color color) {
        Visulizer.comp.addRec(x, baseRange - y - h, w, h, color);
    }

    public HNTree(int numDim, int minChildren, int maxChildren) {
        if (minChildren > (maxChildren / 2)) {
            throw new IllegalArgumentException("too big minChildren");
        }
        this.numDim = numDim;
        this.minChildren = minChildren;
        this.maxChildren = maxChildren;
        root = null;
    }


    public HNTree(int numDim2, int minCh, int maxCh, Expression[] dudfs) {
        this(numDim2, minCh, maxCh);
        this.dudfs = dudfs;
    }

    public void insert(float[] point) {
        if (root == null) {
            // to keep logic simple, we don't let root to be an entry node
            root = new HNTreeNode(null, point, point, true);
        }

        HNTreeNode leaf = chooseSubTree(root, point);
        leaf.insert(point);
        if (leaf.children.size() > maxChildren) {
            treatOverflow(leaf);
        }

    }

    private void treatOverflow(HNTreeNode node) {
        LinkedList<List<HNTreeNode>> split;

        Map<String, Integer> axisNidx = chooseAxisNIndex(node.children, false);
        split = split2Parts(node.children, axisNidx);

        boolean tryDeeperSplit = false;// = node.isLeaf();
        // axisNidx.get(KEY_MIN_CONSISTENT) == 0)
        if (tryDeeperSplit) {
            int deeperSplit = 0;
            if (axisNidx.get(KEY_MIN_CONSISTENT_0) == 0 && split.get(0).size() > minChildren * 2) {
                deeperSplit++;
            }
            if (axisNidx.get(KEY_MIN_CONSISTENT_1) == 0 && split.get(1).size() > minChildren * 2) {
                deeperSplit++;
            }

            Map<String, Integer> moreAxisNidx = null;
            while (deeperSplit > 0) {
                deeperSplit--;
                moreAxisNidx = chooseAxisNIndex(split.get(0), true);
                List<List<HNTreeNode>> moreSplit = split2Parts(split.get(0), moreAxisNidx);
                split.remove(0);

                if (moreAxisNidx.get(KEY_MIN_CONSISTENT_0) == 0 && moreSplit.get(0).size() > minChildren * 2) {
                    deeperSplit++;
                    split.addFirst(moreSplit.get(0));
                } else {
                    split.addLast(moreSplit.get(0));
                }
                if (moreAxisNidx.get(KEY_MIN_CONSISTENT_1) == 0 && moreSplit.get(1).size() > minChildren * 2) {
                    deeperSplit++;
                    split.addFirst(moreSplit.get(1));
                } else {
                    split.addLast(moreSplit.get(1));
                }

            }
        }

        if (node == root) {
            List<HNTreeNode> newnodes = buildNodes(split, node, node.isLeaf());
            root.children = newnodes;
            root.setIsLeaf(false);
        } else {
            List<HNTreeNode> newnodes = buildNodes(split, node.parent, node.isLeaf());
            HNTreeNode parent = node.parent;
            Iterator<HNTreeNode> it = parent.children.iterator();
            while (it.hasNext()) {
                HNTreeNode ch = it.next();
                if (ch == node) {
                    it.remove();
                    break;
                }
            }
            parent.getMBR().updateMBR(newnodes);
            parent.children.addAll(newnodes);
            if (parent.children.size() > maxChildren) {
                treatOverflow(parent);
            }
        }

    }


    private Map<String, Integer> chooseAxisNIndex(List<HNTreeNode> sortedList, boolean deeper) {
        if (sortedList.size() < maxChildren + 1 && !deeper) {
            System.err.println("NO!!");
            throw new RuntimeException("error!");
        }

        double minimumMargin = Double.POSITIVE_INFINITY;
        double minimumOverlap = Double.POSITIVE_INFINITY;
        double minimumVolumn = Double.POSITIVE_INFINITY;
        int maxConsistentSize = Integer.MIN_VALUE;
        int minPart0Consistent = 1;
        int minPart1Consistent = 1;

        Map<String, Integer> ret = new HashMap<String,Integer>();
        for (int axis = 0; axis < numDim; axis++) {

            // for M+1(maxChildren) entries, there are M-2m+2 distributions, m
            // is minChildren
            // 1<=k<=M-2m+2, the kth distribution is the 2 groups that, first
            // group is the
            // the first (m-1)+k entries, the second group is the remaining


            // by lower bound and upper bound
            for (int t = 0; t < 2; t++) {
                if (t == 0) {
                    Collections.sort(sortedList, HNTreeNode.mbrComparatorS(axis));
                } else {
                    Collections.sort(sortedList, HNTreeNode.mbrComparatorT(axis));
                }
                int numDistribution =  0;
                if(!deeper){
                    // numDistribution = maxChildren - 2 * minChildren + 2;
                    numDistribution = sortedList.size() - 2 * minChildren + 1;
                }else{
                    // should both use this formula
                    numDistribution = sortedList.size() - 2 * minChildren + 1;
                }
                for (int k = 1; k <= numDistribution; k++) {
                    // one distribution starts here
                    double currMargin = 0;
                    double currOverlap = 0;
                    double currVolumn = 0;
                    int currConsistentSize = 0;
                    int currPart0Consistent = 1;
                    int currPart1Consistent = 1;

                    MBR group1MBR = new MBR(numDim);
                    MBR group2MBR = new MBR(numDim);
                    for (int i = 0; i < sortedList.size(); i++) {
                        if (i < minChildren - 1 + k) {
                            group1MBR.updateMBR(sortedList.get(i));
                        } else {
                            group2MBR.updateMBR(sortedList.get(i));
                        }
                    }

                    if (dudfs != null) {
                        if (MinMaxDistHN.isConsistent(dudfs, group1MBR.DS(), group1MBR.DT())) {
                            currConsistentSize += minChildren - 1 + k;
                        } else {
                            currPart0Consistent = 0;
                        }

                        if (MinMaxDistHN.isConsistent(dudfs, group2MBR.DS(), group2MBR.DT())) {
                            currConsistentSize += sortedList.size() - (minChildren - 1 + k);
                        } else {
                            currPart1Consistent = 0;
                        }
                    }

                    currMargin = group1MBR.margin() + group2MBR.margin();
                    currOverlap = MBR.calcOverlap(group1MBR, group2MBR);
                    currVolumn = group1MBR.volumn() + group2MBR.margin();
                    
                    if ( (currConsistentSize>maxConsistentSize)
                            ||(currConsistentSize==maxConsistentSize && currMargin < minimumMargin) 
                            || (currConsistentSize==maxConsistentSize && currMargin == minimumMargin && currOverlap < minimumOverlap) 
                            || (currConsistentSize==maxConsistentSize && currMargin == minimumMargin && currOverlap == minimumOverlap && currVolumn < minimumVolumn)) {
                        minimumMargin = currMargin;
                        minimumOverlap = currOverlap;
                        minimumVolumn = currVolumn;
                        maxConsistentSize = currConsistentSize;
                        minPart0Consistent = currPart0Consistent;
                        minPart1Consistent = currPart1Consistent;
                        if (t == 0) {
                            ret.put(KEY_SorT, VALUE_SorT_S);
                        } else {
                            ret.put(KEY_SorT, VALUE_SorT_T);
                        }
                        ret.put(KEY_axis, axis);
                        ret.put(KEY_index, k);
                    }
                }
            }
        }
        ret.put(KEY_MIN_CONSISTENT_0, minPart0Consistent);
        ret.put(KEY_MIN_CONSISTENT_1, minPart1Consistent);
        return ret;
    }

    private List<HNTreeNode> buildNodes(List<List<HNTreeNode>> lists, HNTreeNode parent, boolean isLeaf) {
        List<HNTreeNode> nodes = new LinkedList<HNTreeNode>();
        for (List<HNTreeNode> list : lists) {
            MBR mbr = MBR.generateMBR(list);
            HNTreeNode newnode = new HNTreeNode(parent, mbr.getS(), mbr.getT(), isLeaf);
            newnode.children = list;
            for (HNTreeNode sn : list) {
                sn.parent = newnode;
            }
            nodes.add(newnode);
        }
        return nodes;
    }

    private LinkedList<List<HNTreeNode>> split2Parts(List<HNTreeNode> whole, Map<String, Integer> axisNidx) {
        int SorT = axisNidx.get(KEY_SorT);
        int axis = axisNidx.get(KEY_axis);
        int index = axisNidx.get(KEY_index);
        LinkedList<List<HNTreeNode>> parts = new LinkedList<List<HNTreeNode>>();
        // for (List<HNTreeNode> part : partList) {

        int size1 = minChildren - 1 + index;
        int size2 = whole.size() - size1;
        if (size1 < minChildren || size2 < minChildren) {
            System.err.println("error number of nodes to split!");
            throw new RuntimeException("error!");
        }
        // System.out.println(size1 + "__" + size2);
        if (SorT == 0) {
            Collections.sort(whole, HNTreeNode.mbrComparatorS(axis));
        } else {
            Collections.sort(whole, HNTreeNode.mbrComparatorT(axis));
        }

        List<HNTreeNode> list1 = new LinkedList<HNTreeNode>();
        List<HNTreeNode> list2 = new LinkedList<HNTreeNode>();
        int j = 0;
        for (; j < size1; j++) {
            list1.add(whole.get(j));
        }
        for (; j < size1 + size2; j++) {
            list2.add(whole.get(j));
        }

        int minPart0Consistent = axisNidx.get(KEY_MIN_CONSISTENT_0);
        int minPart1Consistent = axisNidx.get(KEY_MIN_CONSISTENT_1);
        if (minPart0Consistent < minPart1Consistent || (minPart0Consistent == minPart1Consistent && list1.size() > list2.size())) {
            parts.add(list1);
            parts.add(list2);
        } else {
            parts.add(list2);
            parts.add(list1);
            axisNidx.put(KEY_MIN_CONSISTENT_0, minPart1Consistent);
            axisNidx.put(KEY_MIN_CONSISTENT_1, minPart0Consistent);
        }
        // }
        return parts;
    }

    private HNTreeNode chooseSubTree(HNTreeNode target, float[] point) {
        if (target.isLeaf()) {
            return target;
        } else {
            // minimize overlap area=> least area enlarge=> smallest area
            if (target.children.get(0).isLeaf()) {
                double minOverlapEnlarge = Double.POSITIVE_INFINITY;
                double minVolumnIncrease = Double.POSITIVE_INFINITY;
                double minVolumn = Double.POSITIVE_INFINITY;
                int minConsistent = Integer.MAX_VALUE;

                HNTreeNode nextTarget = null;
                for (HNTreeNode n : target.children) {
                    MBR currMBR = n.getMBR();
                    MBR deltaMBR = MBR.tryAddPoint(currMBR, point);

                    double overlapEnlarge = 0;
                    double volumnIncrease = 0;
                    double currVolumn = currMBR.volumn();
                    int currConsistent = 0;

                    if (dudfs != null) {
                        boolean currMBRcon = MinMaxDistHN.isConsistent(dudfs, currMBR.DS(), currMBR.DT());
                        boolean deltaMBRcon = false;
                        if (deltaMBR != null) {
                            deltaMBRcon = MinMaxDistHN.isConsistent(dudfs, deltaMBR.DS(), deltaMBR.DT());
                        } else {
                            deltaMBRcon = currMBRcon;
                        }

                        if (currMBRcon && !deltaMBRcon) {
                            currConsistent = 3;
                        } else if (!currMBRcon && !deltaMBRcon) {
                            currConsistent = 2;
                        } else if (currMBRcon && deltaMBRcon) {
                            currConsistent = 1;
                        } else {
                            System.err.println("error delta MBR!");
                            throw new RuntimeException("error!");
                        }
                    }

                    if (deltaMBR != null) {// if the MBR change after adding the
                        volumnIncrease = deltaMBR.volumn() - currMBR.volumn();
                        // point
                        for (HNTreeNode otherN : target.children) {
                            if (otherN != n) {
                                // new overlap - old overlap
                                overlapEnlarge += MBR.calcOverlap(deltaMBR, otherN.getMBR()) 
                                        - MBR.calcOverlap(currMBR, otherN.getMBR());
                            }
                        }
                    }
                    if ( (currConsistent<minConsistent)
                        || (currConsistent==minConsistent && overlapEnlarge < minOverlapEnlarge) 
                        || (currConsistent==minConsistent && overlapEnlarge == minOverlapEnlarge && volumnIncrease < minVolumnIncrease)
                        || (currConsistent==minConsistent && overlapEnlarge == minOverlapEnlarge && volumnIncrease == minVolumnIncrease && currVolumn < minVolumn)) {
                        minOverlapEnlarge = overlapEnlarge;
                        minVolumnIncrease = volumnIncrease;
                        minVolumn = currVolumn;
                        nextTarget = n;
                        minConsistent = currConsistent;
                    } 
                }

                return chooseSubTree(nextTarget, point);

            } else {
                // least area enlarge=> smallest area
                double minVolumnIncrease = Double.POSITIVE_INFINITY;
                double minVolumn = Double.POSITIVE_INFINITY;

                HNTreeNode nextTarget = null;
                for (HNTreeNode n : target.children) {
                    MBR currMBR = n.getMBR();
                    MBR deltaMBR = MBR.tryAddPoint(currMBR, point);

                    double volumnIncrease = 0;
                    double currVolumn = currMBR.volumn();

                    if (deltaMBR != null) {// if the MBR change after adding the
                        volumnIncrease = deltaMBR.volumn() - currMBR.volumn();
                    }
                    if ((volumnIncrease < minVolumnIncrease) 
                        || (volumnIncrease == minVolumnIncrease && currVolumn < minVolumn)) {
                        minVolumnIncrease = volumnIncrease;
                        minVolumn = currVolumn;
                        nextTarget = n;
                    }
                }

                return chooseSubTree(nextTarget, point);
            }
        }
    }
}
