package gensearch;
import java.util.ArrayList;
import java.util.Stack;

/*
 * V0.1 support +,-,*,/,() 
 */
@SuppressWarnings("rawtypes")
public class MathExpression {

    public static void main(String arg[]) {
        String s = "2000+6*7-5/(1+1)*7";
        
		ArrayList postfix = transform(s);
        for (int i = 0, len = postfix.size(); i < len; i++) {
            System.out.println(postfix.get(i));
        }
        calculate(postfix);
    }    

    //transform from mid-fix into postfix expression
    @SuppressWarnings("unchecked")
	public static ArrayList transform(String midfix) {
    	
        int len = midfix.length();
        int i;
        midfix=midfix+ '#';
        
        Stack<Character> stack = new Stack<Character>();
        stack.push('#');
        ArrayList postfix = new ArrayList();
        
        for (i = 0; i < len + 1; i++) {
            if (Character.isDigit(midfix.charAt(i))) {
            	int num= midfix.charAt(i)-'0';
                while (Character.isDigit(midfix.charAt(i+1))) {
                    i++;
                    num = num * 10 + midfix.charAt(i)-'0';
                } 
                postfix.add(num);
            } else {// not digit
                switch (midfix.charAt(i)) {
                case '(':
                    stack.push(midfix.charAt(i));
                    break;
                case ')':
                    while (stack.peek() != '(') {
                        postfix.add(stack.pop());
                    }
                    stack.pop();// eject '('
                    break;
                default:// for + - * / operators
                    while (stack.peek() != '#'
                            && compare(stack.peek(), midfix.charAt(i))) {
                        postfix.add(stack.pop());// 不断弹栈，直到当前的操作符的优先级高于栈顶操作符
                    }
                    
                    if (midfix.charAt(i) != '#') {// 如果当前的操作符不是'#'(结束符)，那么入操作符栈
                        stack.push(midfix.charAt(i));// 最后的标识符'#'是不入栈的
                    }
                    break;
                }
            }
        }
        return postfix;
    }

    //compare priority of operator
    //return true if peek is prior to cur
    public static boolean compare(char peek, char cur) {
        if (peek == '*'
                && (cur == '+' || cur == '-' || cur == '/' || cur == '*')) {// 如果cur是'('，那么cur的优先级高,如果是')'，是在上面处理
            return true;
        } else if (peek == '/'
                && (cur == '+' || cur == '-' || cur == '*' || cur == '/')) {
            return true;
        } else if (peek == '+' && (cur == '+' || cur == '-')) {
            return true;
        } else if (peek == '-' && (cur == '+' || cur == '-')) {
            return true;
        } else if (cur == '#') {// 这个很特别，这里说明到了中缀表达式的结尾，那么就要弹出操作符栈中的所有操作符到后缀表达式中
            return true;// 当cur为'#'时，cur的优先级算是最低的
        }
        return false;// 开括号是不用考虑的，它的优先级一定是最小的,cur一定是入栈
    }
    
    public static boolean calculate(ArrayList postfix){//后缀表达式的运算顺序就是操作符出现的先后顺序
        System.out.println("calculate");
        int i,res=0,size=postfix.size();
        Stack<Integer> stack_num=new Stack<Integer>();
        for(i=0;i<size;i++){
            if(postfix.get(i).getClass()==Integer.class){//说明是操作数，这个很有用啊！
                stack_num.push((Integer)postfix.get(i));
                System.out.println("push"+" "+(Integer)postfix.get(i));
            }else{//如果是操作符
                System.out.println((Character)postfix.get(i));
                int a=stack_num.pop();
                int b=stack_num.pop();//注意运算时的前者和后者
                switch((Character)postfix.get(i)){
                case '+':
                    res=b+a;
                    System.out.println("+ "+a+" "+b);
                    break;
                case '-':
                    res=b-a;
                    System.out.println("- "+a+" "+b);
                    break;
                case '*':
                    res=b*a;
                    System.out.println("* "+a+" "+b);
                    break;
                case '/':
                    res=b/a;
                    System.out.println("/ "+a+" "+b);
                    break;
                }
                stack_num.push(res);
                System.out.println("push"+" "+res);
            }
        }
        res=stack_num.pop();
        System.out.println("res "+" "+res);
        if(res==24){
            return true;
        }
        return false;
    }
	
}
