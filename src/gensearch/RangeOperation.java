package gensearch;

import net.objecthunter.exp4j.operator.Operator;

public class RangeOperation {

    private Operator op;

    public RangeOperation(Operator op) {
        this.op = op;
    }

    public Range applyRangeOperation(Range left, Range right) {
        switch (op.getSymbol()) {
            case "+":
                return rangePlus(left, right);
            case "-":
                return rangePlus(left, rangeNegative(right));
            case "*":
                return rangeMulti(left, right);
            case "/":
                return rangeDivide(left, right);
            case "^":
                return rangePower(left, right);
            default:
                throw new IllegalArgumentException("We don't support this operator [" + op.getSymbol() + "] yet!!!");
        }
    }

    public Range applyRangeOperation(Range arg) {
        switch (op.getSymbol()) {
            case "+":
                return arg;
            case "-":
                return rangeNegative(arg);
            default:
                throw new IllegalArgumentException("We don't support this operator [" + op.getSymbol() + "] yet!!!");
        }
    }

    /**
     * base, exp
     * any ,>1 integer or 1/3,
     * or >0, 1/2 or 1/3
     *
     * @param base
     * @param exp
     */
    private double mypow(double base, double exp) {
        if (exp % 1 == 0) {
            return Math.pow(base, exp);
        } else if (exp == (1f / 2f)) {
            return Math.sqrt(base);
        } else if (exp == (1f / 3f)) {
            return Math.cbrt(base);
        } else {
            throw new IllegalArgumentException("We don't support this yet!!!");
        }
    }

    /**
     * base, exp
     * any ,>1 integer,
     * or >0, >1
     * or >0, 1/2 or 1/3
     *
     * @param base
     * @param exp
     * @return
     */
    private Range rangePower(Range base, Range exp) {

        if (exp.datas.length > 1 || exp.getLeft() != exp.getRight() || exp.getLeft() < 0) {
            throw new IllegalArgumentException("We don't support this yet!!!");
        }
        double[][] ranges = new double[base.getNum()][2];
        for (int i = 0; i < base.getNum(); i++) {
            if (exp.getLeft() == (1.0 / 2.0) && (base.datas[i][Range.LEFT] < 0 || base.datas[i][Range.RIGHT] < 0)) {
                throw new IllegalArgumentException("cannot sqrt negative number");
            }
            if (exp.getLeft() > 1 && exp.getLeft() % 2 == 0 && base.datas[i][Range.LEFT] * base.datas[i][Range.RIGHT] < 0) {
                ranges[i][Range.LEFT] = 0;
                ranges[i][Range.RIGHT] = Math.max(mypow(base.datas[i][Range.LEFT], exp.getLeft()), mypow(base.datas[i][Range.RIGHT], exp.getLeft()));
            } else {
                ranges[i][Range.LEFT] = Math.min(mypow(base.datas[i][Range.LEFT], exp.getLeft()), mypow(base.datas[i][Range.RIGHT], exp.getLeft()));
                ranges[i][Range.RIGHT] = Math.max(mypow(base.datas[i][Range.LEFT], exp.getLeft()), mypow(base.datas[i][Range.RIGHT], exp.getLeft()));
            }

        }
        return new Range(ranges);

    }

    private Range rangeDivide(Range left, Range right) {
        int seperateNum = 0;
        for (int i = 0; i < right.getNum(); i++) {
            if (right.datas[i][Range.LEFT] < 0 && right.datas[i][Range.RIGHT] > 0) {
                seperateNum++;
            }
        }
        double[][] new_right = new double[right.getNum() + seperateNum][2];
        int j = 0;
        for (int i = 0; i < right.getNum(); i++) {
            if (right.datas[i][Range.LEFT] < 0 && right.datas[i][Range.RIGHT] > 0) {
                new_right[j][Range.LEFT] = Double.NEGATIVE_INFINITY;
                new_right[j][Range.RIGHT] = 1 / right.datas[i][Range.LEFT];
                j++;
                new_right[j][Range.LEFT] = 1 / right.datas[i][Range.RIGHT];
                new_right[j][Range.RIGHT] = Double.POSITIVE_INFINITY;
                j++;
            } else {
                new_right[j][Range.LEFT] = 1 / right.datas[i][Range.RIGHT];
                new_right[j][Range.RIGHT] = 1 / right.datas[i][Range.LEFT];
                j++;
            }
        }

        return rangeMulti(left, new Range(new_right));
    }

    private Range rangeMulti(Range left, Range right) {
        double[][] ranges = new double[left.getNum() * right.getNum()][2];
        int cnt = 0;
        for (int i = 0; i < left.getNum(); i++) {
            for (int j = 0; j < right.getNum(); j++) {
                ranges[cnt][Range.LEFT] = minMultiRes(left.datas[i], right.datas[j]);
                ranges[cnt][Range.RIGHT] = maxMultiRes(left.datas[i], right.datas[j]);
                cnt++;
            }
        }
        return new Range(ranges);
    }

    private double maxMultiRes(double[] left, double[] right) {
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                double tmp = left[i] * right[j];
                if (tmp > max) {
                    max = tmp;
                }
            }
        }
        return max;
    }

    private double minMultiRes(double[] left, double[] right) {
        double min = Double.POSITIVE_INFINITY;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                double tmp = left[i] * right[j];
                if (tmp < min) {
                    min = tmp;
                }
            }
        }
        return min;
    }

    private Range rangeNegative(Range arg) {
        double[][] ranges = new double[arg.getNum()][2];
        for (int i = 0; i < arg.getNum(); i++) {
            ranges[i][Range.LEFT] = -arg.datas[i][Range.RIGHT];
            ranges[i][Range.RIGHT] = -arg.datas[i][Range.LEFT];
        }
        return new Range(ranges);
    }

    private Range rangePlus(Range left, Range right) {
        double[][] ranges = new double[left.getNum() * right.getNum()][2];
        int cnt = 0;
        for (int i = 0; i < left.getNum(); i++) {
            for (int j = 0; j < right.getNum(); j++) {
                ranges[cnt][Range.LEFT] = left.datas[i][Range.LEFT] + right.datas[j][Range.LEFT];
                ranges[cnt][Range.RIGHT] = left.datas[i][Range.RIGHT] + right.datas[j][Range.RIGHT];
                cnt++;
            }
        }
        return new Range(ranges);
    }


    public static void main(String[] args) {
        // Unit Test!!!
        Operator op = new Operator("^", 2, true, Operator.PRECEDENCE_POWER) {
            @Override
            public double apply(final double... args) {
                return args[0] + args[1];
            }
        };

        //double[][] ranges1 = { {1,2},{-3,4}, {-8,-1} };
        double[][] ranges1 = {{10, 20}, {-2, 9}, {-15, -8}};
        //double[][] ranges1 = {{10, 20}, {2, 9}, {5, 8}};

        Range r1 = new Range(ranges1);
        Range r2 = new Range(1f/3f,1f/3f);

        RangeOperation rop = new RangeOperation(op);
        System.out.println(rop.applyRangeOperation(r1, r2));

        System.out.println(r1.hasChangedSign());
    }

}
