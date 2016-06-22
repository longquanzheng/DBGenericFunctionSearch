package qlong.hntree;

import java.awt.Color;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class HNTree {

    public final int numDim, minChildren, maxChildren;

    public HNTreeNode root;

    private static Color[] colors = { Color.RED, Color.BLACK, Color.BLUE, Color.CYAN, Color.ORANGE, Color.PINK, Color.GREEN, };

    private float baseRange = 0;
    public static void main(String[] args) {
        HNTree t = new HNTree(2, 1, 5);
        t.baseRange = 1000;
        double[][] pts = { { 67.0, 490.0 }, { 237.0, 474.0 }, { 121.0, 476.0 }, { 114.0, 235.0 }, { 354.0, 411.0 }, { 354.0, 175.0 }, { 132.0, 413.0 }, { 121.0, 351.0 }, { 117.0, 192.0 }, { 282.0, 109.0 }, { 344.0, 449.0 }, { 44.0, 29.0 },
                { 214.0, 202.0 }, { 488.0, 418.0 }, { 266.0, 123.0 }, { 41.0, 243.0 }, { 225.0, 57.0 }, { 186.0, 420.0 }, { 351.0, 453.0 }, { 139.0, 173.0 }, { 48.0, 70.0 }, { 268.0, 180.0 }, { 496.0, 388.0 }, { 234.0, 71.0 }, { 271.0, 327.0 },
                { 160.0, 405.0 }, { 24.0, 497.0 }, { 118.0, 70.0 }, { 258.0, 371.0 }, { 337.0, 202.0 }, { 132.0, 445.0 }, { 304.0, 47.0 }, { 485.0, 146.0 }, { 308.0, 351.0 }, { 415.0, 455.0 }, { 38.0, 259.0 }, { 317.0, 433.0 }, { 25.0, 379.0 },
                { 208.0, 71.0 }, { 10.0, 83.0 }, { 1.0, 167.0 }, { 319.0, 344.0 }, { 100.0, 151.0 }, { 419.0, 348.0 }, { 320.0, 131.0 }, { 34.0, 363.0 }, { 108.0, 168.0 }, { 154.0, 349.0 }, { 450.0, 157.0 }, { 219.0, 38.0 }, { 131.0, 496.0 },
                { 480.0, 260.0 }, { 182.0, 79.0 }, { 317.0, 68.0 }, { 405.0, 11.0 }, { 247.0, 291.0 }, { 334.0, 332.0 }, { 435.0, 449.0 }, { 332.0, 423.0 }, { 271.0, 71.0 }, { 245.0, 175.0 }, { 400.0, 307.0 }, { 172.0, 433.0 }, { 152.0, 340.0 },
                { 431.0, 183.0 }, { 430.0, 7.0 }, { 190.0, 373.0 }, { 415.0, 174.0 }, { 48.0, 261.0 }, { 384.0, 334.0 }, { 20.0, 173.0 }, { 0.0, 94.0 }, { 486.0, 34.0 }, { 149.0, 141.0 }, { 91.0, 74.0 }, { 239.0, 22.0 }, { 0.0, 149.0 },
                { 398.0, 18.0 }, { 100.0, 155.0 }, { 92.0, 189.0 }, { 435.0, 86.0 }, { 26.0, 114.0 }, { 157.0, 377.0 }, { 26.0, 202.0 }, { 217.0, 250.0 }, { 231.0, 417.0 }, { 324.0, 435.0 }, { 64.0, 135.0 }, { 410.0, 206.0 }, { 315.0, 388.0 },
                { 420.0, 384.0 }, { 143.0, 92.0 }, { 260.0, 72.0 }, { 412.0, 487.0 }, { 68.0, 78.0 }, { 45.0, 356.0 }, { 280.0, 188.0 }, { 475.0, 250.0 }, { 103.0, 130.0 }, { 302.0, 341.0 }, };
        for (int i = 0; i < 200; i++) {
            float[] pt = new float[2];
            // pt[0] = pt[1] = 1;
            pt[0] = (float) (int) (Math.random() * t.baseRange);
            pt[1] = (float) (int) (Math.random() * t.baseRange);
            // pt[0] = (float) pts[i][0];
            // pt[1] = (float) pts[i][1];
            t.insert(pt);
            System.out.println(i + "-insert:" + pt[0] + "," + pt[1] + "...");

        }
        System.out.println(t);
    }

    @Override
    public String toString() {
        // String s = "root:"+root.nodeId+"#\n";
        Visulizer.start();
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
        addRec(mbrS[0], mbrS[1], mbrT[0] - mbrS[0], mbrT[1] - mbrS[1], color);

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
        // if (maxChildren < Math.pow(2, numDim)) {
        // throw new IllegalArgumentException("too small maxChildren");
        // }
        // if (minChildren > (int) (maxChildren / Math.pow(2, numDim))) {
        // throw new IllegalArgumentException("too big minChildren");
        // }
        this.numDim = numDim;
        this.minChildren = minChildren;
        this.maxChildren = maxChildren;
        root = null;
    }

    public void insert(float[] point) {
        if (root == null) {
            // to keep logic simple, we don't let root to be an entry node
            root = new HNTreeNode(null, point, point, true);
        }

        HNTreeNode leaf = chooseLeaf(root, point);
        leaf.insert(point);
        if (leaf.children.size() > maxChildren) {
            treatOverflow(leaf);
        }

    }

    private void treatOverflow(HNTreeNode node) {
        int sizeInc = (int) Math.pow(2, numDim);
        List<List<HNTreeNode>> split = new LinkedList<List<HNTreeNode>>();
        split.add(node.children);
        Random rand = new Random(System.currentTimeMillis());
        // int x0to1 = rand.nextInt(2);
        // int x2to3 = rand.nextInt(2) + 2;
        // System.out.println(x0to1);
        // System.out.println(x2to3);
        // split = split2PartsAtDim(split, x0to1);
        // split = split2PartsAtDim(split, x2to3);
        // split = split2PartsAtDim(split, 4);
        // split = split2PartsAtDim(split, 5);
        for (int i = 0; i < numDim; i++) {
            // split to 2 parts for each dimension
            split = split2PartsAtDim(split, i);
        }

        // if (split.size() != sizeInc) {
        // System.err.println("split size=" + split.size());
        // }

        if (node == root) {
            // MBR newmbr = MBR.generateMBR(newnodes);
            // HNTreeNode newroot = new HNTreeNode(null, node.getMBR().getS(),
            // node.getMBR().getT(), false);

            // update parent
            // for (HNTreeNode n : newnodes) {
            // n.parent = newroot;
            // }
            // node.parent = newroot;
            List<HNTreeNode> newnodes = buildNodes(split, node, node.isLeaf());
            // root = newroot;
            // newroot.children = newnodes;
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

    private List<List<HNTreeNode>> split2PartsAtDim(List<List<HNTreeNode>> partList, int i) {
        List<List<HNTreeNode>> nextList = new LinkedList<List<HNTreeNode>>();
        for (List<HNTreeNode> part : partList) {
            int size2 = part.size() / 2;
            int size1 = part.size() - size2;
            if (size1 == 0 || size2 == 0) {
                System.err.println("error!not enough nodes to split!");
            }
            // System.out.println(size1 + "__" + size2);
            Collections.sort(part, HNTreeNode.mbrComparator(i));
            List<HNTreeNode> list1 = new LinkedList<HNTreeNode>();
            List<HNTreeNode> list2 = new LinkedList<HNTreeNode>();
            int j = 0;
            for (; j < size1; j++) {
                list1.add(part.get(j));
            }
            for (; j < size1 + size2; j++) {
                list2.add(part.get(j));
            }
            nextList.add(list1);
            nextList.add(list2);
        }
        return nextList;
    }

    private HNTreeNode chooseLeaf(HNTreeNode target, float[] point) {
        if (target.isLeaf()) {
            return target;
        } else {
            // minimize overlap area=> least area enlarge=> smallest area
            if (target.children.get(0).isLeaf()) {
                double minOverlapEnlarge = Double.POSITIVE_INFINITY;
                double minVolumnIncrease = Double.POSITIVE_INFINITY;
                double minVolumn = Double.POSITIVE_INFINITY;

                HNTreeNode nextTarget = null;
                for (HNTreeNode n : target.children) {
                    MBR currMBR = n.getMBR();
                    MBR deltaMBR = MBR.tryAddPoint(currMBR, point);

                    double overlapEnlarge = 0;
                    double volumnIncrease = 0;
                    double currVolumn = currMBR.volumn();

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
                    if (overlapEnlarge < minOverlapEnlarge 
                        || (overlapEnlarge == minOverlapEnlarge && volumnIncrease < minVolumnIncrease)
                        ||(overlapEnlarge == minOverlapEnlarge && volumnIncrease == minVolumnIncrease && currVolumn < minVolumn)) {
                        minOverlapEnlarge = overlapEnlarge;
                        minVolumnIncrease = volumnIncrease;
                        minVolumn = currVolumn;
                        nextTarget = n;
                    } 
                }

                return chooseLeaf(nextTarget, point);

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

                return chooseLeaf(nextTarget, point);
            }
        }
    }
}
