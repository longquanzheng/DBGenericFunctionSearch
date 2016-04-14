/* 
* Copyright 2014 Frank Asseg
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License. 
*/
package gensearch;


/**
 * Class representing the builtin functions available for use in expressions
 */
public class RangeFunctions {
    private static final int INDEX_SIN = 0;
    private static final int INDEX_COS = 1;
    private static final int INDEX_TAN = 2;
    private static final int INDEX_LOG = 3;
    private static final int INDEX_LOG1P = 4;
    private static final int INDEX_ABS = 5;
    private static final int INDEX_ACOS = 6;
    private static final int INDEX_ASIN = 7;
    private static final int INDEX_ATAN = 8;
    private static final int INDEX_CBRT = 9;
    private static final int INDEX_CEIL = 10;
    private static final int INDEX_FLOOR = 11;
    private static final int INDEX_SINH = 12;
    private static final int INDEX_SQRT = 13;
    private static final int INDEX_TANH = 14;
    private static final int INDEX_COSH = 15;
    private static final int INDEX_POW = 16;
    private static final int INDEX_EXP = 17;
    private static final int INDEX_EXPM1 = 18;
    private static final int INDEX_LOG10 = 19;
    private static final int INDEX_LOG2 = 20;

    private static final RangeFunction[] builtinFunctions = new RangeFunction[21];

    static {
        builtinFunctions[INDEX_COS] = new RangeFunction("cos") {
            @Override
            public Range apply(Range... ranges) {
                Range arg = ranges[0];
                //System.out.println(arg);
                double[][] datas = new double[arg.datas.length][2];
                for(int i=0;i<arg.datas.length;i++){
                    double left = arg.datas[i][Range.LEFT], right = arg.datas[i][Range.RIGHT];
                    Range cos_range_res = cosRange(left,right);
                    datas[i][Range.LEFT] = cos_range_res.getLeft();
                    datas[i][Range.RIGHT] = cos_range_res.getRight();
                }
                return new Range(datas);

            }
        };

    }


    private static Range cosRange(double left, double right){
        if( right>= left+Math.PI*2){
            return new Range(-1,1);
        }
        double left_div = left/Math.PI, right_div = right/Math.PI;
        if(left_div % 1f != 0){
            left_div = Math.ceil(left_div);
        }
        if(right_div % 1f !=0){
            right_div = Math.floor(right_div);
        }
        //System.out.println("states:"+left+","+left_div+","+right+","+right_div);
        double min = Math.min(Math.cos(left),Math.cos(right));
        double max = Math.max(Math.cos(left),Math.cos(right));
        while(left_div<=right_div){
            min = Math.min(min, Math.cos(left_div*Math.PI));
            max = Math.max(max, Math.cos(left_div*Math.PI));
            left_div += 1;
        }
        return new Range(min,max);
    }

    /**
     * Get the builtin function for a given name
     * @param name te name of the function
     * @return a Function instance
     */
    public static RangeFunction getBuiltinFunction(final String name) {

        if (name.equals("cos")) {
            return builtinFunctions[INDEX_COS];
        } else {
            return null;
        }
    }

}
