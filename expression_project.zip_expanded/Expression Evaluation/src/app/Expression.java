package app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

import structures.Stack;

public class Expression {

	public static String delims = " \t*+-/()[]";

	/**
	 * Populates the vars list with simple variables, and arrays lists with arrays
	 * in the expression. For every variable (simple or array), a SINGLE instance is
	 * created and stored, even if it appears more than once in the expression. At
	 * this time, values for all variables and all array items are set to zero -
	 * they will be loaded from a file in the loadVariableValues method.
	 * 
	 * @param expr   The expression
	 * @param vars   The variables array list - already created by the caller
	 * @param arrays The arrays array list - already created by the caller
	 */
	public static void makeVariableLists(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
		StringTokenizer st = new StringTokenizer(expr, delims); // Breaks expr into tokens: a*b+A[b] -> a,b,A,b
		String currentToken = "";
		while (st.hasMoreTokens()) {
			currentToken = st.nextToken();
			int indexOfTknInExpr = expr.indexOf(currentToken); // position of token in Expr
			int tokenLength = currentToken.length(); // length of token in Expr
			// IF: the current token beings with a letter, meaning it's a variable, then DO
			// EX: varA -> begins with a letter, not a number
			if (Character.isLetter(currentToken.charAt(0))) {
				// IF the token is the last character in the expr, then it must be variable
				// because no [ after it
				if (indexOfTknInExpr + tokenLength > expr.length() - 1) {
					Variable temp = new Variable(currentToken);
					if (!vars.contains(temp)) {
						vars.add(temp);
						// System.out.println("added " + temp.toString());
					}
				}
				// The character after token is '[', meaning token is an array
				else if (expr.charAt(indexOfTknInExpr + tokenLength) == '[') {
					//System.out.println(currentToken);
					Array temp = new Array(currentToken);
					if (!arrays.contains(temp)) {
						arrays.add(temp);
					}
				}
				// If token is not an array, it must be a variable
				else {
					Variable temp = new Variable(currentToken);
					if (!vars.contains(temp)) {
						vars.add(temp);
						// System.out.println("added " + temp.toString());
					}
				}
			}
			// end of if statement
		}
		// end of while loop
	}

	/**
	 * Loads values for variables and arrays in the expression
	 * 
	 * @param sc Scanner for values input
	 * @throws IOException If there is a problem with the input
	 * @param vars   The variables array list, previously populated by
	 *               makeVariableLists
	 * @param arrays The arrays array list - previously populated by
	 *               makeVariableLists
	 */
	public static void loadVariableValues(Scanner sc, ArrayList<Variable> vars, ArrayList<Array> arrays)
			throws IOException {
		while (sc.hasNextLine()) {
			StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
			int numTokens = st.countTokens();
			String tok = st.nextToken();
			Variable var = new Variable(tok);
			Array arr = new Array(tok);
			int vari = vars.indexOf(var);
			int arri = arrays.indexOf(arr);
			if (vari == -1 && arri == -1) {
				continue;
			}
			int num = Integer.parseInt(st.nextToken());
			if (numTokens == 2) { // scalar symbol
				vars.get(vari).value = num;
			} else { // array symbol
				arr = arrays.get(arri);
				arr.values = new int[num];
				// following are (index,val) pairs
				while (st.hasMoreTokens()) {
					tok = st.nextToken();
					StringTokenizer stt = new StringTokenizer(tok, " (,)");
					int index = Integer.parseInt(stt.nextToken());
					int val = Integer.parseInt(stt.nextToken());
					arr.values[index] = val;
				}
			}
		}
	}

	/**
	 * Evaluates the expression.
	 * 
	 * @param vars   The variables array list, with values for all variables in the
	 *               expression
	 * @param arrays The arrays array list, with values for all array items
	 * @return Result of evaluation
	 */
	public static float evaluate(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
		
		Stack<Float> values = new Stack<Float>();
		Stack<String> operatorStack = new Stack<String>();
		Stack<String> currentOpStack = new Stack<String>();
		int positionofOpenParen = 0, positionOfCloseParen = 0, positionofOpenBrac = 0, positionOfCloseBrac = 0;
		String operation;

		// FORMATTING
		expr = expr.replaceAll(" ", "");
		expr = expr.replaceAll("\t", "");

		for (int i = 0; i < vars.size(); i++) {
			int indexOfVar = expr.indexOf(vars.get(i).name);
			int varLength = vars.get(i).name.length();
			if ( indexOfVar != -1 && (indexOfVar + varLength > expr.length() - 1) ) {
				expr = expr.replace(vars.get(i).name, "" + vars.get(i).value);
			}
			else if(indexOfVar != -1 && expr.charAt(indexOfVar+varLength) != '[') {
				expr = expr.replace(vars.get(i).name, "" + vars.get(i).value);
			}
		}
		
		while(expr.contains("[")) {
			for(int i1 = 0; i1 < expr.length(); i1++) {
				if(expr.charAt(i1) == '[') {
					positionofOpenBrac = i1;
				}
			}
			
			for(int i1 = positionofOpenBrac; i1 < expr.length(); i1++) {
				if(expr.charAt(i1) == ']') {
					positionOfCloseBrac = i1;
					break;
				}
			}
			String needsToGo = expr.substring(positionofOpenBrac+1, positionOfCloseBrac);
			expr = expr.replace(needsToGo, "" + evaluate(expr.substring(positionofOpenBrac+1, positionOfCloseBrac), vars, arrays));
			for(int i1 = 0; i1 < expr.length(); i1++) {
				if(expr.charAt(i1) == '[') {
					positionofOpenBrac = i1;
				}
			}
			for(int i1 = positionofOpenBrac; i1 < expr.length(); i1++) {
				if(expr.charAt(i1) == ']') {
					positionOfCloseBrac = i1;
					break;
				}
			}
			int arrayNameLength = 0;
			for(int i = positionofOpenBrac-1; i >= 0; i--) {
					if(Character.isLetter(expr.charAt(i))) {
						arrayNameLength++;
					} else if(!Character.isLetter(expr.charAt(i))) {
						break;
					}
			}
			String subInArrayHere = expr.substring(positionofOpenBrac-arrayNameLength, positionOfCloseBrac+1);
			String letter = expr.substring(positionofOpenBrac-arrayNameLength, positionofOpenBrac);
			Array here = null;
			for(Array a: arrays) {
				if(a.toString().charAt(0) == (letter.charAt(0))) {
					here = a;
				}
			}
			Array locatedWithinHere = arrays.get(arrays.indexOf(here));
			int v = (int)Float.parseFloat(expr.substring(positionofOpenBrac+1, positionOfCloseBrac));
			int replacedArrayinExpr = locatedWithinHere.values[v];
			expr = expr.replace(subInArrayHere, "" + replacedArrayinExpr);
		}
		
		while(expr.contains("(")) {
			
			for(int i = 0; i < expr.length(); i++) {
				if(expr.charAt(i) == '(') {
					positionofOpenParen = i;
				}
			}
			for(int i = positionofOpenParen; i < expr.length(); i++) {
				if(expr.charAt(i) == ')') {
					positionOfCloseParen = i;
					break;
				}
			}
			String needsToGo = expr.substring(positionofOpenParen, positionOfCloseParen+1);
			expr = expr.replace(needsToGo, "" + evaluate(expr.substring(positionofOpenParen+1, positionOfCloseParen), vars, arrays));
		}
		
		if(expr.charAt(0) == '-') {
			expr = expr.replace(expr, "0+" + expr);
		}
		expr = handleRepeatedOperators(expr);
		
		
		//StringTokenizer st = new StringTokenizer(expr, delims, true);
		// if token is a number, add it to values stack; if token is operator, add to
		// operator stack
		
		StringTokenizer st1 = new StringTokenizer(expr, delims, false);
		values = fillValuesStack(st1, expr);
		StringTokenizer st2 = new StringTokenizer(expr, delims, true);
		operatorStack = fillOperatorStack(st2);
		//fillAllStacks(st, values, operatorStack);
		
		if(operatorStack.isEmpty()) {
			return values.pop();
		}
		if(values.size() == 1) {
			if(expr.charAt(0) == '-') {
				return Float.parseFloat(expr.substring(0, 2)); 
			}
		}
		
		int size = operatorStack.size();
		for (int i = 0; i < size; i++) {
			
			operation = operatorStack.pop();
			currentOpStack.push(operation);

			if (operationHasPriority(operation, operatorStack)) {
				float val1 = values.pop();
				float val2 = values.pop();
				float ans = calculate(val1, val2, operation);
				values.push(ans);
				
				//clear currentOpStack as it can only have one thing
				currentOpStack.pop();
			}
			//Since that operation DOES NOT have priority, move on to the next operation in the stack
			else {
				operation = operatorStack.pop();
				//the priority operation gets pushed ahead of the non-priority one
				currentOpStack.push(operation);
				
				//we need to skip past this value in the stack in order to 
				float skipThisVal = values.pop();
				float val1 = values.pop();
				float val2 = values.pop();
				float ans = calculate(val1, val2, operation);
				values.push(ans);
				//put back the value we skipped
				values.push(skipThisVal);
				
				currentOpStack.pop();
				//put the non-priority operator back into its position---technically one below where it once was before 
				//this iteration
				operatorStack.push(currentOpStack.pop());
			}
			
		}

		return values.pop();
	}

	private static String handleRepeatedOperators(String expr) {
		boolean canLeaveMethod = false;
		while(canLeaveMethod == false) {
			StringTokenizer tokens = new StringTokenizer(expr, delims, true);
			ArrayList<String> list = new ArrayList<String>();
			while(tokens.hasMoreTokens()){
				list.add(tokens.nextToken());
			}
			
			for(int s = 0; s < list.size(); s++) {
				if(expr.charAt(0) == '-') {
					expr = expr.replace(expr, "0+" + expr);
				}
				if(list.get(s).equals("*") && list.get(s+1).equals("-") ) {
					String redundant = "*-";
					expr = expr.replace(redundant, "*");
					String temp = list.get(s-1);
					expr = replaceFirst(expr, temp, "-" + temp);
				}
				if(list.get(s).equals("/") && list.get(s+1).equals("-") ) {
					String redundant = "/-";
					expr = expr.replace(redundant, "/");
					String temp = list.get(s-1);
					expr = replaceFirst(expr, temp, "-" + temp);
				}
				if(list.get(s).equals("-") && list.get(s+1).equals("-") ) {
					String redundant = "--";
					expr = expr.replace(redundant, "+");
				}
				if(list.get(s).equals("+") && list.get(s+1).equals("-") ) {
					String redundant = "+-";
					expr = expr.replace(redundant, "-");
				}
			}
			canLeaveMethod = true;
			for(int s = 0; s < list.size(); s++) {
				if( (list.get(s).equals("*") && list.get(s+1).equals("-")) ||
						(list.get(s).equals("/") && list.get(s+1).equals("-") ) ||
						(list.get(s).equals("-") && list.get(s+1).equals("-") ) ||
						(list.get(s).equals("+") && list.get(s+1).equals("-") )
						) {
					canLeaveMethod = false;
				}
			}
		}
		return expr;
	}

	private static float calculate(float val1, float val2, String operation) {
		float ans = 0;
		switch (operation) {
			case "*":
				ans = (val1 * val2);
				break;
			case "/":
				ans = (val1 / val2);
				break;
			case "+":
				ans = (val1 + val2);
				break;
			case "-":
				ans = (val1 - val2);
				break;
		}
		return ans;
	}

	private static boolean operationHasPriority(String operation, Stack<String> operatorStack) {
		boolean priority = true;

		// priority is false when current operation is + or - and next operation is * or
		// /
		if ((!operatorStack.isEmpty()) && (operation.equals("+") || operation.equals("-"))
				&& (operatorStack.peek().equals("*") || operatorStack.peek().equals("/"))) {
			priority = false;
		}
		
		//IN THE CASE OF TWO SUBTRACTIONS NEXT TO EACHOTHER: 13-4-12
		if( (!operatorStack.isEmpty()) && ( operation.equals("-") && operatorStack.peek().equals("-") )  ) {
			priority = true;
		}
		
		//IN THE CASE OF TWO DIVISIONS NEXT TO EACHOTHER: 12/3/4
		if( (!operatorStack.isEmpty()) && ( operation.equals("/") && operatorStack.peek().equals("/") ) ) {
			priority = true;
		}
		
		return priority;
	}
	
	private static String replaceFirst(String text, String search, String replace)
	{
	  int pos = text.indexOf(search);
	  if (pos < 0)
	  {
	    return text;
	  }
	  return text.substring(0, pos) + replace + text.substring(pos + search.length());
	}
	
	private static Stack<Float> fillValuesStack(StringTokenizer st, String expr){
		Stack<Float> returnStack = new Stack<Float>();
		Stack<Float> temp = new Stack<Float>();
		String currToken;
		while(st.hasMoreTokens()) {
			currToken = st.nextToken();
			if(currToken.charAt(currToken.length()-1) == 'E') {
				String scienctificNotation = currToken + "-" + st.nextToken();
				temp.push(Float.parseFloat(scienctificNotation));
			}
			else if(Character.isDigit(currToken.charAt(0))) {
				temp.push(Float.parseFloat(currToken));
			}
			
		}
		while(!temp.isEmpty()) {
			returnStack.push(temp.pop());
		}
		return returnStack;
	}
	
	private static Stack<String> fillOperatorStack(StringTokenizer st){
		Stack<String> returnStack = new Stack<String>();
		Stack<String> temp = new Stack<String>();
		String currToken;
		while(st.hasMoreTokens()) {
			currToken = st.nextToken();
			if( (currToken.charAt(currToken.length()-1) == 'E') && st.nextToken() == "-") {
				currToken = st.nextToken();
			}
			else if ( (currToken.equals("*") || currToken.equals("/") || currToken.equals("+") || currToken.equals("-")) 
					 ) {
				temp.push(currToken);
			} 
		}
		while(!temp.isEmpty()) {
			returnStack.push(temp.pop());
		}
		return returnStack;
	}
	
}
