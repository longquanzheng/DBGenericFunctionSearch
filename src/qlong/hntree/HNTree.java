package qlong.hntree;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class HNTree {

    public final int numDim, minChildren, maxChildren;

    public HNTreeNode root;

    public static void main(String[] args) {
        HNTree t = new HNTree(2, 1, 5);
        for (int i = 0; i < 50; i++) {
            float[] pt = new float[2];
            // pt[0] = pt[1] = 1;
            pt[0] = (float) (int) (Math.random() * 100);
            pt[1] = (float) (int) (Math.random() * 100);
            t.insert(pt);
            System.out.println(i + "-insert:" + pt[0] + "," + pt[1] + "...");
        }
        System.out.println(t);
    }

    @Override
    public String toString() {
        // String s = "root:"+root.nodeId+"#\n";
        return toString(1, root);
    }

    private String toString(int curr, HNTreeNode node) {
        String ret = "";
        String tab = "";
        for (int i = 0; i < curr; i++) {
            tab += "##\t";
        }
        ret = tab + node.id + "$$" + node.getMBR() + "\n";
        if (node.isLeaf()) {
            for (HNTreeNode sn : node.children) {
                String valS = "<";
                for (int i = 0; i < sn.getVal().length; i++) {
                    valS += "" + sn.getVal()[i] + ",";
                }
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

    public HNTree(int numDim, int minChildren, int maxChildren) {
        if (maxChildren < Math.pow(2, numDim)) {
            throw new IllegalArgumentException("too small maxChildren");
        }
        if (minChildren > (int) (maxChildren / Math.pow(2, numDim))) {
            throw new IllegalArgumentException("too big minChildren");
        }
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
        for (int i = 0; i < numDim; i++) {
            // split to 2 parts for each dimension
            split = split2PartsAtDim(split, i);
        }
        assert (split.size() == sizeInc);

        if (node == root) {
            List<HNTreeNode> newnodes = buildNodes(split, node);
            MBR newmbr = MBR.generateMBR(newnodes);
            HNTreeNode newroot = new HNTreeNode(null, newmbr.getS(), newmbr.getT(), false);
            // update parent
            for (HNTreeNode n : newnodes) {
                n.parent = newroot;
            }
            root = newroot;
            newroot.children = newnodes;
        } else {
            List<HNTreeNode> newnodes = buildNodes(split, node);
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


    private List<HNTreeNode> buildNodes(List<List<HNTreeNode>> lists, HNTreeNode basedNode) {
        List<HNTreeNode> nodes = new LinkedList<HNTreeNode>();
        for (List<HNTreeNode> list : lists) {
            MBR mbr = MBR.generateMBR(list);
            HNTreeNode n = new HNTreeNode(basedNode.parent, mbr.getS(), mbr.getT(), basedNode.isLeaf());
            n.children = list;
            for (HNTreeNode sn : list) {
                sn.parent = n;
            }
            nodes.add(n);
        }
        return nodes;
    }

    private List<List<HNTreeNode>> split2PartsAtDim(List<List<HNTreeNode>> partList, int i) {
        List<List<HNTreeNode>> nextList = new LinkedList<List<HNTreeNode>>();
        for (List<HNTreeNode> part : partList) {
            int size2 = part.size() / 2;
            int size1 = part.size() - size2;
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
