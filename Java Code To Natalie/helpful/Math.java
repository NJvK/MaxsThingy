package helpful;
import helpful.datastructures.DLList;
public class Math{
	public static int max(int[] nums){
		int max = nums[0];
		for (int num : nums){
			if (num > max) max = num;
		}
		return max;
	}
	public static int max(int num1, int num2){
		if (num1 > num2) return num1;
		return num2;
	}
	public static int min(int[] nums){
		int min = nums[0];
		for (int num : nums){
			if (num < min) min = num;
		}
		return min;
	}
	public static int min(int num1, int num2){
		if (num1 < num2) return num1;
		return num2;
	}
	public static double abs(double num){
		if (num > 0) return num;
		return num*-1;
	}
	public static int floor(double num){
		return (int) java.lang.Math.floor(num);
	}
	public static int ceil(double num){
		return (int) java.lang.Math.ceil(num);
	}
	public static double calculate(String eqn){ //turns a string equation into the result the equation produces
		char[] letters = eqn.toCharArray();
		DLList<Character> symbols = new DLList<Character>();
		DLList<Double> nums = new DLList<Double>();
		nums.add(0.0); //initalize nums list
		for (char letter : letters){
			//convert from human readable to machine
			if (letter == '[' || letter == '{') letter = '(';
			if (letter == ']' || letter == '}') letter = ')';
			//add to our last number
			if (Character.isDigit(letter)){
				double val = nums.get(nums.size()-1) * 10 + (int)letter-48;
				nums.set(nums.size()-1, val);
			}
			else if (letter == '+' || letter == '-' || letter == '*' || letter == '/' || letter == '^' || letter == '(' || letter == ')' || letter == '<' || letter == '>' || letter == '='){
				//valid symbol
				symbols.add(letter);
				if (letter != '(' && letter != ')') nums.add(0.0);
			}
		}
		return eval(symbols, nums);
	}
	private static double eval(DLList<Character> symbols, DLList<Double> nums){
		//System.out.println("\nEval called:\n" + symbols + "\n" + nums);
		if (symbols.size() == 0) return nums.get(0); //no equation to be done, return our first number in case there are more than 
		//find parenthesizes, do regression on them
		int parenthesesCounter = 0, symbolCount = 0, index = 0, startIndex = -1, endIndex = -1, numStart = -1, numEnd = -1;
		DLList<Character> innerSymbols = new DLList<Character>();
		DLList<Double> innerNums = new DLList<Double>();
		for (Character item : symbols){
			if (item.charValue() != '(' && item.charValue() != ')'){
				if (startIndex != -1) innerNums.add(nums.get(symbolCount));
				symbolCount++;
			}
			if (item.charValue() == '('){
				parenthesesCounter++;
				if (startIndex == -1){
					startIndex = index;
					numStart = symbolCount;
					index++;
					continue;
				}
			}
			if (item.charValue() == ')'){
				if (startIndex == -1) throw new IllegalArgumentException("Cannot have a closing parentheses without an opening parentheses before hand");
				parenthesesCounter--;
				endIndex = index;
				numEnd = symbolCount;
				if (parenthesesCounter == 0){
					innerNums.add(nums.get(symbolCount));
					double res = eval(innerSymbols, innerNums);
					//remove all the old symbols
					for (int i=0;i<endIndex-startIndex+1;i++){
						symbols.remove(startIndex);
					}
					//remove all the old numbers
					for (int i=0;i<numEnd-numStart+1;i++){
						nums.remove(numStart);
					}
					nums.add(numStart, res);
					//removed:
					startIndex = -1;
					
				}
			}
			if (startIndex != -1){
				//active parentheses
				innerSymbols.add(item);
			}
			index++;
		}
		if (symbols.size() == 0) return nums.get(0); //if we finished everything, return our answer
		//System.out.println("\nParentheses done:\n" + symbols + "\n" + nums);
		//smaller parentheses free equations now
		
		//find exponentials (^), act on them
		index = 0;
		for (Character item : symbols){
			if (item == '^'){
				nums.set(index, java.lang.Math.pow(nums.get(index), nums.get(index+1)));
				nums.remove(index+1);
				symbols.remove(index);
			}
			index++;
		}
		if (symbols.size() == 0) return nums.get(0); //if we finished everything, return our answer
		
		//System.out.println("\nExponentials done:\n" + symbols + "\n" + nums);
		
		//multiplcation and division in left to right order
		index = 0;
		for (Character item : symbols){
			if (item == '*'){
				nums.set(index, nums.get(index) * nums.get(index+1));
				nums.remove(index+1);
				symbols.remove(index);
			}
			if (item == '/'){
				nums.set(index, nums.get(index) * nums.get(index+1));
				nums.remove(index+1);
				symbols.remove(index);
			}
			index++;
		}
		if (symbols.size() == 0) return nums.get(0); //if we finished everything, return our answer
		
		//System.out.println("\nMultiplcation / division done:\n" + symbols + "\n" + nums);
		
		//Addition and subtration in left to right order
		index = 0;
		for (Character item : symbols){
			if (item == '+'){
				nums.set(index, nums.get(index) + nums.get(index+1));
				nums.remove(index+1);
				symbols.remove(index);
			}
			if (item == '-'){
				nums.set(index, nums.get(index) - nums.get(index+1));
				nums.remove(index+1);
				symbols.remove(index);
			}
			index++;
		}
		
		index = 0;
		for (Character item : symbols){
			if (item == '<'){
				nums.set(index, nums.get(index) < nums.get(index+1) ? 1.0 : 0.0);
				nums.remove(index+1);
				symbols.remove(index);
			}
			if (item == '>'){
				nums.set(index, nums.get(index) > nums.get(index+1) ? 1.0 : 0.0);
				nums.remove(index+1);
				symbols.remove(index);
			}
			if (item == '='){
				nums.set(index, nums.get(index) == nums.get(index+1) ? 1.0 : 0.0);
				nums.remove(index+1);
				symbols.remove(index);
			}
		}
		
		//System.out.println("\nAddition / subtraction done:\n" + symbols + "\n" + nums);
		return nums.get(0); //guarenteed to be done when it reaches this point
	}
}
