package qlong.hntree;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class HNTreeNode {
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
    private static long id_cnt = 0;
    public final long id;

	public HNTreeNode parent;
	public List<HNTreeNode> children;
	
	private MBR mbr;
	private float[] val;
	
	private boolean isEntry;
	private boolean isLeaf;
	
	/***
	 * for internal node
	 */
	public HNTreeNode(HNTreeNode parent,float[] mbrS,float[] mbrT, boolean isLeaf2){
		id = id_cnt ++;
		isEntry = false;
		isLeaf = isLeaf2;
        mbr = new MBR(mbrS, mbrT);
		this.parent = parent;
		this.children = new LinkedList<HNTreeNode>();
	}
	
	/**
	 * For entry node
	 */
	private HNTreeNode(HNTreeNode parent,float[] point){
		id = id_cnt ++;
		isEntry = true;
		isLeaf = false;
		int numDim = point.length;
		
		val = new float[numDim];
		System.arraycopy(point, 0, val, 0, numDim);
		
		this.parent = parent;
	}
	
	public MBR getMBR(){
		return mbr;
	}
	
    public float[] getVal() {
		return val;
	}
	
	

	public boolean isEntry(){
		return isEntry;
	}
	
	public boolean isLeaf(){
		return isLeaf;
	}
	
	/**
	 * add an entry to a leaf
	 */
	public void insert(float[] point) {
		HNTreeNode entry = new HNTreeNode(this,point);
		this.children.add(entry);
        updateMBRRecursively(this, point);

	}

    private void updateMBRRecursively(HNTreeNode hnTreeNode, float[] point) {
        boolean hasChanged = hnTreeNode.mbr.updateMBR(point);
        if (hasChanged && hnTreeNode.parent != null) {
            updateMBRRecursively(hnTreeNode.parent, point);
        }
    }

    public static Comparator<HNTreeNode> mbrComparator(int i) {
        return new Comparator<HNTreeNode>() {
            @Override
            public int compare(HNTreeNode n1, HNTreeNode n2) {
                float[] pt1, pt2;
                if (n1.isEntry()) {
                    pt1 = n1.getVal();
                    pt2 = n2.getVal();
                } else {
                    pt1 = n1.getMBR().getS();
                    pt2 = n2.getMBR().getS();
                }
                float f = pt1[i] - pt2[i];
                int ret = f < 0 ? -1 : (f > 0 ? 1 : 0);
                return ret;
            }
        };
    }

    public float[] MBR_S() {
        if (this.isEntry) {
            return val;
        } else {
            return this.mbr.getS();
        }
    }

    public float[] MBR_T() {
        if (this.isEntry) {
            return val;
        } else {
            return this.mbr.getT();
        }
    }

    public double[] DMBR_S() {
        float[] res = MBR_S();
        double[] d = new double[res.length];
        for (int i = 0; i < res.length; i++) {
            d[i] = res[i];
        }
        return d;
    }

    public double[] DMBR_T() {
        float[] res = MBR_T();
        double[] d = new double[res.length];
        for (int i = 0; i < res.length; i++) {
            d[i] = res[i];
        }
        return d;
    }

    public void setIsLeaf(boolean b) {
        this.isLeaf = b;
    }

}
