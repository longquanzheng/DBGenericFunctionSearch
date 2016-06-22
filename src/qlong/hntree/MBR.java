package qlong.hntree;

import java.util.List;

public class MBR {
	
	private float[] _mbrS;
	private float[] _mbrT;
	
    public String toString() {
        String s = "[";
        for (int i = 0; i < _mbrS.length; i++) {
            s += _mbrS[i] + ", ";
        }
        s += "],\t[";
        for (int i = 0; i < _mbrS.length; i++) {
            s += _mbrT[i] + ", ";
        }
        s += "],\t";
        return s;
    }
	public MBR(float[] s, float[] t){
		int numDim = s.length;
		_mbrS = new float[numDim];
		_mbrT = new float[numDim];
		System.arraycopy(s, 0, _mbrS, 0, numDim);
		System.arraycopy(t, 0, _mbrT, 0, numDim);
	}
	
	public static void main(String[] args) {
        float[] s = { 1, 2, 3 }, t = { 4, 5, 6 };
        MBR m = new MBR(s, t);
        System.out.println(m);
	}

    public float[] getS() {
        return _mbrS;
    }

    public float[] getT() {
        return _mbrT;
    }

    public static MBR tryAddPoint(MBR currMBR, float[] point) {
        MBR newmbr = new MBR(currMBR.getS(), currMBR.getT());
        boolean changeFlag = false;
        for (int i = 0; i < point.length; i++) {
            if (newmbr.getS()[i] > point[i]) {
                newmbr.getS()[i] = point[i];
                changeFlag = true;
            }
            if (newmbr.getT()[i] < point[i]) {
                newmbr.getT()[i] = point[i];
                changeFlag = true;
            }
        }
        if (changeFlag) {
            return newmbr;
        } else {
            return null;
        }
	}

    public void updateMBR(List<HNTreeNode> split) {
        for (HNTreeNode n : split) {
            float[] pt1 = n.getMBR().getS();
            float[] pt2 = n.getMBR().getT();
            this.updateMBR(pt1);
            this.updateMBR(pt2);
        }
    }

    public boolean updateMBR(float[] point) {
        boolean flag = false;
        for (int i = 0; i < point.length; i++) {
            if (this.getS()[i] > point[i]) {
                this.getS()[i] = point[i];
                flag = true;
            }
            if (this.getT()[i] < point[i]) {
                this.getT()[i] = point[i];
                flag = true;
            }
        }
        return flag;
    }

    public static double calcOverlap(MBR mbr1, MBR mbr2) {
        double res = 1;
        for (int i = 0; i < mbr1.getS().length; i++) {
            if (mbr1.getS()[i] > mbr2.getS()[i]) {
                MBR tmp = mbr1;
                mbr1 = mbr2;
                mbr2 = tmp;
            }
            res *= intersect(mbr1.getS()[i], mbr1.getT()[i], mbr2.getS()[i], mbr2.getT()[i]);
        }
        return res;
	}

    private static double intersect(float s1, float t1, float s2, float t2) {
        if (t1 <= s2) {
            return 0;
        } else if (t1 > t2) {
            return (t2 - s2);
        } else if (t1 > s2) {
            return (t1 - s2);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public double volumn() {
        double res = 1;
        for (int i = 0; i < this.getS().length; i++) {
            double len = this.getT()[i] - this.getS()[i];
            res *= len;
        }
        return res;
    }



    public static MBR generateMBR(List<HNTreeNode> split) {
        if (split.get(0).isEntry()) {
            float[] first = split.get(0).getVal();
            MBR mbr = new MBR(first, first);
            for (HNTreeNode n : split) {
                mbr.updateMBR(n.getVal());
            }
            return mbr;
        } else {
            MBR first = split.get(0).getMBR();
            MBR mbr = new MBR(first.getS(), first.getT());
            mbr.updateMBR(split);
            return mbr;
        }

    }

}
