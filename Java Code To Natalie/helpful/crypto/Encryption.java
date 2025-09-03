package helpful.crypto;
import java.util.Random; //Only used for unit testing, not needed for actual encryption / decryption
public class Encryption{
	/* Notes:
	 * How this works: (frontend)
	 ** Get your data as a long array of bytes, get a key also as an array of bytes - it can be literally anything, but all 0s will make your data trivially easy to decipher, then choose which cube size you want to use, each cube size will change how the data is moved around and changed by the key
	 ** Out comes your encrypted data and you're on your way
	 ** Decryption is very similar, send in the encrypted data, same key and same cube size and out will pop your original data
	 ** After decryption, there will be space filling 0s so either encode the size of the data or have a standard to parse out the 0s, the 0s were used to ensure data was moved around and mixed properly despite not fitting into a cube by itself
	 ** Only downside is data is confined to its cube, so if you have a large amount of data, I would recommend a large cube to mix it up throughly otherwise you will have distinict chunks with no mixing of values
	
	 * How this works: (backend)
	 ** With the getIndex function, it will turn the 4 dimensions on the n * n * n cube into a single array index so memory isn't overburdend by 3D lists which could get stupid massive compared to 1D with the same elements, espically if the side lengths get to the theoretical maximum
	 ** so each "cube" is a single continuous array with many different (6) ways to access the same element depending on which face is used, the manipulation methods ensure the 1D array is treated like the 3D version its simulating
	 ** The process will go through the key, seperate out the bit-lengths needed to preform operations (see function parseInstructions) and initalize the variables used in the encryption
	 ** For each instruction generated from the key, it will preform the rotation or shfit on the face for the x, y, depth, coordinate - rotation uses depth to determine how many squares get rotated from the surface - shifting uses depth to determine how far the elements will be shifted into the face
	 ** After the instruction is preformed, it does a deterministic general shift / rotation on every face, XOR the list based on the key, then another general shift / rotation on every face to ensure every bit of data moves around to XOR with as much of the key as possible
	 ** Loop for all the instructions given, once done copy the finished array into the return array, as stated above, I would recommend using larger cubes to ensure every piece of data goes everywhere / mingles with every part of the key
	 ** We then return the entire list now completely overriden with the scrambled information after doing the computation on as many cubes as we can make with the data given
	
	 * Key Useage during encryption / decryption
	 ** Step 1. Go through the key, identify all command instructions i.e. rotation and shifting, apply instructions in order of key
	 ** Step 2. Between each instruction do a step in the scrambling sequence and apply it - intended to ensure data is fully mixed - makes it more difficult to do an analysis on data / key
	 ** Step 3. Xor key into data after the scrambling step
	 ** Step 4. Done, all data is completely scrambled and xor'd into oblivion
	 
	 * key packing:
	 ** Bit sizes:				1			3		log_2(n)	log_2(n)	log_2(n)	-> Total: 3log_2(n)+4 -> n=2: 7 -> n=4: 10 -> n=8: 13
	 ** Name:					Rotation?	Face?	x			y			depth
	 ** Shift (into face):  	0			0-5		0-n			0-n			0-n
	 ** Rotate (Clock wise):	1			0-5		0-n			0-n			0-n
	 ** 
	 
	 * Key Sizes:
	 ** Note: These will be the standard byte sizes but is if fully capable of doing this scramble with any whole number of bytes i.e. 5 bytes but not 31 bits = 4.875 bytes
	 ** 128 bits / 256 bits / 512 bits -> 16 bytes / 32 bytes / 64 bytes
	 ** Bit size	N value		Instruction Count	xor size
	 ** 128 bits:	n = 2		IC = 18				128
	 ** 128 bits:	n = 4		IC = 12				128
	 ** 128 bits:	n = 8		IC = 9				128
	 **
	 ** 256 bits:	n = 2		IC = 36				256
	 ** 256 bits:	n = 4		IC = 25				256
	 ** 256 bits:	n = 8		IC = 19				256
	 **
	 ** 512 bits:	n = 2		IC = 73				512
	 ** 512 bits:	n = 4		IC = 51				512
	 ** 512 bits:	n = 8		IC = 39				512
	 */
	
	//Get and Set methods are intended to be overridden by subclassess for ease of extensions
	public   byte get(byte[] array, int index){
		return array[index];
	}
	public   void set(byte[] array, int index, byte value){
		array[index] = value;
	}
	public   int sizeOfArray(int n){
		System.out.println("Ecnryption size of array being run");
		return n*n*n;
	}
	
	//turns an x,y,depth coordinate on this face for an n by n by n cube into the index of that coordinate for use in rotation and shifting functions during encryption / decryption
	//Note: There are multiple different ways to refer to the same index in the list, 6 of them, 1 for every face, due to how the cube works
	protected   int getIndex(int face, int x, int y, int depth, int n){
		if (depth < 0 || depth >= n) throw new IllegalArgumentException("Depth value of " + depth + " must be between 0 and " + (n-1) + " inclusive");
		if (x < 0 || x >= n) throw new IllegalArgumentException("X value of " + x + " must be between 0 and " + (n-1) + " inclusive");
		if (y < 0 || y >= n) throw new IllegalArgumentException("Y value of " + y + " must be between 0 and " + (n-1) + " inclusive");
		if (face < 0 || face >= 6) throw new IllegalArgumentException("The face of the cube must be between 0 and 5 inclusive, got " + face);
		switch (face){ //all these equations have been worked out by hand to get the list coordinates wanted for the given face
			case 0:
				return depth*n*n + y*n + x;
			case 1:
				return y*n*n + x + (n-1-depth)*n;
			case 2:
				return y*n*n - x*n + n*n - depth - 1;
			case 3:
				return y*n*n - x + depth*n + n - 1;
			case 4:
				return y*n*n + x*n + depth;
			case 5:
				return (n-1-depth)*n*n + y*n - x + n - 1;
		}
		return -1; //bad data, should never be called
	}
	
	//Rotates the face of the n by n by n cube towards clockwise facing the face down depth layers, i.e. depth =2 -> rotates top 2 layers clock wise, each layer stays distinct but still rotates just the same
	//Returns the array passed in after the rotation step is complete
	protected   byte[] rotate(byte[] array, int face, int x, int y, int depth, int n){
		if (array == null ? true : array.length == 0) throw new IllegalArgumentException("Array cannot be null and must contain at least 1 element");
		if (face < 0 || face >= 6) throw new IllegalArgumentException("Face must be within the bounds of 0 and 5 inclusive, got " + face);
		if (x < 0 || x >= n) throw new IllegalArgumentException("X value must be within the bounds of 0 and " + (n-1) + " inclusive, got " + x);
		if (y < 0 || y >= n) throw new IllegalArgumentException("Y value must be within the bounds of 0 and " + (n-1) + " inclusive, got " + y);
		if (depth < 0 || depth >= n) throw new IllegalArgumentException("Depth value must be within the bounds of 0 and " + (n-1) + " inclusive, got " + depth);
		if (n < 0) throw new IllegalArgumentException("N value must be greater than 0, got " + n);
		
		byte temp = 0; //holds the temporary value while moving the values in the array
		int startIndex = 0; //the first index, starts at our starting square before changing value to make the program more human readable
		int tempIndex = 0; //second index for use to keep track of where the other value is coming from
		for (int d=0;d<depth+1;d++){//for each depth layer specifed, rotate the point clockwise
		
			startIndex = getIndex(face, x, y, d, n); //starting square
			tempIndex = getIndex(face, relToAbs(absToRel(x, n), n), relToAbs(-absToRel(y, n), n), d, n); //where the value comes from - i.e. bottom left corner //x, -y
			
			temp = get(array, startIndex); //array[startIndex]; //store our starting square
			set(array, startIndex, get(array, tempIndex)); //array[startIndex] = array[tempIndex]; //replace starting square
			
			startIndex = tempIndex;
			tempIndex = getIndex(face, relToAbs(-absToRel(x, n), n), relToAbs(-absToRel(y, n), n), d, n); //bottom right corner //-x, -y
			set(array, startIndex, get(array, tempIndex)); //array[startIndex] = array[tempIndex]; //replace next square
			
			startIndex = tempIndex;
			tempIndex = getIndex(face, relToAbs(-absToRel(x, n), n), relToAbs(absToRel(y, n), n), d, n); //top right corner //-x, y
			set(array, startIndex, get(array, tempIndex)); //array[startIndex] = array[tempIndex]; //replace next square
			
			startIndex = tempIndex;
			set(array, startIndex, temp); //array[startIndex] = temp; //replace top right square with our original value, thus rotation clock wise
		}
		return array; // array will be changed and this return is not required to access the newly changed cube
	}
	//Exact duplicate of rotate function but rotation in the counter clockwise direction
	protected   byte[] rotateCCW(byte[] array, int face, int x, int y, int depth, int n){
		if (array == null ? true : array.length == 0) throw new IllegalArgumentException("Array cannot be null and must contain at least 1 element");
		if (face < 0 || face >= 6) throw new IllegalArgumentException("Face must be within the bounds of 0 and 5 inclusive, got " + face);
		if (x < 0 || x >= n) throw new IllegalArgumentException("X value must be within the bounds of 0 and " + (n-1) + " inclusive, got " + x);
		if (y < 0 || y >= n) throw new IllegalArgumentException("Y value must be within the bounds of 0 and " + (n-1) + " inclusive, got " + y);
		if (depth < 0 || depth >= n) throw new IllegalArgumentException("Depth value must be within the bounds of 0 and " + (n-1) + " inclusive, got " + depth);
		if (n < 0) throw new IllegalArgumentException("N value must be greater than 0, got " + n);
		
		byte temp; //holds the temporary value while moving the values in the array
		int startIndex = 0; //the first index, starts at our starting square before changing value to make the program more human readable
		int tempIndex = 0; //second index for use to keep track of where the other value is coming from
		for (int d=0;d<depth+1;d++){//for each depth layer specifed, rotate the point clockwise
		
			startIndex = getIndex(face, x, y, d, n); //starting square
			tempIndex = getIndex(face, relToAbs(-absToRel(x, n), n), relToAbs(absToRel(y, n), n), d, n); //where the value comes from - i.e. top right corner //-x, y
			
			temp = get(array, startIndex); //array[startIndex]; //store our starting square
			set(array, startIndex, get(array, tempIndex)); //array[startIndex] = array[tempIndex]; //replace starting square
			
			startIndex = tempIndex;
			tempIndex = getIndex(face, relToAbs(-absToRel(x, n), n), relToAbs(-absToRel(y, n), n), d, n); //bottom right corner //-x, -y
			set(array, startIndex, get(array, tempIndex)); //array[startIndex] = array[tempIndex]; //replace next square
			
			startIndex = tempIndex;
			tempIndex = getIndex(face, relToAbs(absToRel(x, n), n), relToAbs(-absToRel(y, n), n), d, n); //bottom left corner //x, -y
			set(array, startIndex, get(array, tempIndex)); //array[startIndex] = array[tempIndex]; //replace next square
			
			startIndex = tempIndex;
			set(array, startIndex, temp); //array[startIndex] = temp; //replace top right square with our original value, thus rotation clock wise
		}
		return array; // array will be changed and this return is not required to access the newly changed cube
	}
	//Rotates the entire depth level clockwise, and only rotates the desired level
	protected   byte[] rotateLevel(byte[] array, int face, int depth, int n){
		if (array == null ? true : array.length == 0) throw new IllegalArgumentException("Array cannot be null and must contain at least 1 element");
		if (face < 0 || face >= 6) throw new IllegalArgumentException("Face must be within the bounds of 0 and 5 inclusive, got " + face);
		if (depth < 0 || depth >= n) throw new IllegalArgumentException("Depth value must be within the bounds of 0 and " + (n-1) + " inclusive, got " + depth);
		if (n < 0) throw new IllegalArgumentException("N value must be greater than 0, got " + n);
		/* Notes:
		 * Rotations with cubes of odd side values (n) will result with rotations that are not intuitive to humans - it will rotate deterministiclly but to a human it looks like a weird jumble 
		 */
		for (int x=0;x<(n>>1);x++){
			for (int y=0;y<(n>>1);y++){
				rotate(array, face, x, y, depth, n); //rotates each index in the top left corner of the square projection, thereby rotating the entire level of the cube at the specified depth
				if (depth > 0) rotateCCW(array, face, x, y, depth-1, n); //undoes the rotation on the levels not wanted while not rotating everything by being negative
			}
		}
		return array;
	}
	//Rotates the entire depth level counter clockwise, and only rotates the desired level
	protected   byte[] rotateCCWLevel(byte[] array, int face, int depth, int n){
		if (array == null ? true : array.length == 0) throw new IllegalArgumentException("Array cannot be null and must contain at least 1 element");
		if (face < 0 || face >= 6) throw new IllegalArgumentException("Face must be within the bounds of 0 and 5 inclusive, got " + face);
		if (depth < 0 || depth >= n) throw new IllegalArgumentException("Depth value must be within the bounds of 0 and " + (n-1) + " inclusive, got " + depth);
		if (n < 0) throw new IllegalArgumentException("N value must be greater than 0, got " + n);
		/* Notes:
		 * Rotations with cubes of odd side values (n) will result with rotations that are not intuitive to humans - it will rotate deterministiclly but to a human it looks like a weird jumble 
		 */
		for (int x=0;x<(n>>1);x++){
			for (int y=0;y<(n>>1);y++){
				rotateCCW(array, face, x, y, depth, n); //rotates each index in the top left corner of the square projection, thereby rotating the entire level of the cube at the specified depth
				if (depth > 0) rotate(array, face, x, y, depth-1, n); //undoes the rotation on the levels not wanted while not rotating everything by being negative
			}
		}
		return array;
	}
	//Rotation helper functions
	//turns the absolute x/y index into relative values for the face - used exclusively for rotations on a face -> See Function: rotate
	protected   int absToRel(int value, int n){
		return value < (n>>1) ? value-(n>>1) : value-(n>>1)+1;
	}
	//turns the relative value/y index into absolute value for the face - used exclusively for rotation on a face -> See Function: rotate
	protected   int relToAbs(int value, int n){
		return value <= 0 ? value+(n>>1) : value+(n>>1)-1;
	}
	
	//Shifts the values of the n by n by n cube into the face depth layers deep, i.e. depth = 2 -> shifts the index on top of the face 2 layers down so it will now have a depth of 3 instead of 1
	//Returns the array passed in after the shifting step is complete
	protected   byte[] shift(byte[] array, int face, int x, int y, int depth, int n){
		if (array == null ? true : array.length == 0) throw new IllegalArgumentException("Array cannot be null and must contain at least 1 element");
		if (face < 0 || face >= 6) throw new IllegalArgumentException("Face must be within the bounds of 0 and 5 inclusive, got " + face);
		if (x < 0 || x >= n) throw new IllegalArgumentException("X value must be within the bounds of 0 and " + (n-1) + " inclusive, got " + x);
		if (y < 0 || y >= n) throw new IllegalArgumentException("Y value must be within the bounds of 0 and " + (n-1) + " inclusive, got " + y);
		//No issues raised with depth out of bounds because it wraps the value to be within bounds
		if (n < 0) throw new IllegalArgumentException("N value must be greater than 0, got " + n);
		/* Notes:
		 * This function is intended to try and do the entire shift all at once if possible so there only needs to be a single value being held away from the array
		 * In the case the depth given (aka depth offset) doesn't eventually cover every depth in this x,y coord on this face, it will do a second pass at the depth not yet shifted
		 */
		depth = wrap(depth, 0, n); //ensures the depth value stays valid, even if the desired affect is a shift upwards / negative depth
		byte temp = 0; // Holds the temporary value while moving the values in the array
		long depthFlag = 0L; // Holds the true / false values of if we've moved this position yet
		int startIndex = 0; // Holds the destination index for the movement through the array
		int endIndex = 0; //Holds the source index for the movment through the array
		int tempDepth = 0; //Holds the current depth value for the loop to refer to
		for (int d=0;d<depth;d++){
			startIndex = getIndex(face, x, y, d, n); //initalise our starting location
			temp = get(array, startIndex); //array[startIndex]; //save the value from the array
			tempDepth = wrap(d-depth, 0, n); //get the element that will shift into this element
			depthFlag = flagSet(depthFlag, true, d); //ensure we don't overwrite this shifted element later - possibly reverting the shift
			while(!flagGet(depthFlag, tempDepth)){ //for as long as the depth hasn't been moved before
				endIndex = getIndex(face, x, y, tempDepth, n); //get the source of the move
				set(array, startIndex, get(array, endIndex)); ///array[startIndex] = array[endIndex]; //do the move
				startIndex = endIndex; //then move up the start index
				depthFlag = flagSet(depthFlag, true, tempDepth); //update our flags
				tempDepth = wrap(tempDepth - depth, 0, n); //update our working depth
			}
			set(array, startIndex, temp); ///array[startIndex] = temp; //add back in our original value at new position
		}
		return array; // array will be changed to reflect the most recent shift
	}
	//Wraps a number num between top-1 and bottom inclusive i.e. wrap(2, 0, 5) -> 2 while wrap(7, 0, 5) -> 2 and wrap(-3, 0, 5) -> 2
	//Returns the wrapped number num
	public   int wrap(int num, int bottom, int top){ 
		if (top == bottom) return num; //stack overflow protection and div by 0 protection
		if (top < bottom) throw new IllegalArgumentException("Top value of " + top + " cannot be smaller than bottom value of " + bottom);
		//includes bottom, excludes top
		if (num < top && num >= bottom){ //if we are within the range specified, no more needs to be done
			return num;
		}
		if (num < bottom){ //we are below the range specified, add the difference between top and bottom and try again
			return wrap(num+top-bottom, bottom, top);
		}
		if (num >= top){ //We are above the range specified, modulo what ever the excess above top is then add in the bottom
			return ((num-top)%(top-bottom))+bottom;
		}
		return num;
	}
	
	//performs the XOR operation on all elements between data and carry. if data is larger than carry, carry will XOR again to cover the entirety of data
	//The two arrays don't need to be same length, both need to have at least 1 element and not null
	protected   byte[] listXOR(byte[] data, byte[] carry){
		if (data == null ? true : data.length == 0) throw new IllegalArgumentException("Data array must not be null and must have at least 1 value stored within it");
		if (carry== null ? true : carry.length== 0) throw new IllegalArgumentException("Carry array must now be null and must have at least 1 value stored within it");
		//System.out.print("XOR FN: " + data.length + ", " + carry.length + " -- ");
		//for (byte num : data){System.out.print(num + ", ");}
		//System.out.print(" -- ");
		//for (byte num : carry){System.out.print(num + ", ");}
		//System.out.print(" -->> ");
		if (data.length < carry.length){ // data is less than carry, speical flow made to quickly XOR and leave
			for (int i=0;i<data.length;i++){ 
				data[i] ^= carry[i];
			}
			//for (byte num : data){System.out.print(num + ", ");}
			//System.out.println();
			return data;
		}
		//either carry and data are the same length or data is bigger than carry
		int offset = 0;
		for (int i=0;i<data.length;i++){
			data[i] ^= carry[i - offset*carry.length]; //ensures carry is always within bounds of the array, despite i being outside those bounds
			if (i+1-offset*carry.length >= carry.length) 
				offset++;
		}
		//for (byte num : data){System.out.print(num + ", ");}
		//System.out.println();
		return data;
	}
	
	//Encrypts the data to be unrelated to the original passed in at first glance, Returns newly scrambled list based on data pased in, size of the new list will be a multiple of the perfect cube of size n passed in
	public   byte[] encrypt(byte[] data, byte[] key, int n){
		//ensure information is of decent quality
		if (n < 1 || n > 32) throw new IllegalArgumentException("Size of the cubes must be between 1 and 32 exclusive, got " + n); //arbitrary upper bounds to keep values reasonable, actual upper bounds is 512 / 2**9 b/c this will give the signed integer limit when cubed which is how java stores arrays, with ints
		//if ((n&0b1) == 1 && false) throw new IllegalArgumentException("Size of the cubes must be even, got " + n);
		if (data == null ? true : data.length==0) throw new IllegalArgumentException("Data array must not be null and must have at least 1 value stored within it");
		if (key == null ? true : key.length == 0) throw new IllegalArgumentException("Key array must not be null and must have at least 1 value stored within it");
		
		//crack the data into cubes which the rotations will do work on
		int arraySize = sizeOfArray(n);
		byte[] tempArray = new byte[arraySize];
		byte[] send = new byte[data.length%(arraySize) == 0 ? data.length : (data.length/(arraySize)+1)*arraySize];
		System.out.println(data.length + ", " + arraySize + ", " + n);
		int varSize = (int)(Math.log(n) / Math.log(2)); //size of the floating size variables, i.e. x / y / depth
		int instructionSize = (int)(4 + 3* varSize); //size of each instruction
		int[] instructions = new int[key.length*8 / instructionSize + (key.length*8%instructionSize == 0?0:1)]; //must be ints to have enough bits for n>=2. n=512 is the maximum size this instruction list can hold / allow
		parseInstructions(instructions, key, instructionSize); //parses the key array with 8-bit segments into the instruction's segments
		int face, x, y, depth, instruction; //setting aside space outside of loop, slightly more memory efficent (don't worry about it tho for your own projects)
		int bitMask = (int)(Math.pow(2, varSize)-1); //the bit mask used to isolate the x, y, depth values from the instruction
		
		for (int i=0;i<send.length/(arraySize);i++){ //goes through all the cubes we have set space for
			tempArray = new byte[tempArray.length]; //resets tempArray so it will be filled with 0s
			System.arraycopy(data, i * tempArray.length, tempArray, 0, Math.min(data.length - i*tempArray.length, tempArray.length)); //copies all the data into their proper cubes
			
			//now cubes[n] is properly filled out, we can encrypt it as it gets parsed
			for (int k=0;k<instructions.length;k++){
				instruction = instructions[k];
				face = ((instruction >>> instructionSize-1-3) & 0b111) % 6; //wrap it around from 0 to 5
				x = (instruction >>> instructionSize-1-3-varSize) & bitMask; //gets the first variable size variable from the instruction
				y = (instruction >>> instructionSize-1-3-varSize*2) & bitMask; //gets the second variable size variable from the instruction
				depth = instruction & bitMask; //gets the last variable size variable from the instruction, no need to shift, we just need to mask to get our value
				
				if (((instruction >>> instructionSize-1) & 0b1) == 0){
					//the instruction is saying to do a shift
					shift(tempArray, face, x, y, depth, n);
				}
				else{
					//the instruction is saying do a rotation
					rotate(tempArray, face, x, y, depth, n);
				}
				
				//do a general shift / rotation / XOR here, scrambles the entire cube vs just a single subsection of it
				for (face=0;face<6;face++){
					for (int d=0;d<n;d+=2){
						rotateLevel(tempArray, face, d, n); //rotates the even levels on every face clockwise
					}
					for (x=0;x<n;x++){
						for (y=0;y<n;y++){
							if (((x+y)&1) == 0) shift(tempArray, face, x, y, x+y, n); //shifts every other index into the cube based on where it lies on the face
						}
					}
				}
				listXOR(tempArray, key);
				for (face=0;face<6;face++){
					for (int d=1;d<n;d+=2){
						rotateLevel(tempArray, face, d, n); //rotates the even levels on every face clockwise
					}
					for (x=0;x<n;x++){
						for (y=0;y<n;y++){
							if (((x+y)&1) == 1) shift(tempArray, face, x, y, x+y, n); //shifts every other index into the cube based on where it lies on the face
						}
					}
				}
			}
			
			//and then put the scrambled data back where it was
			System.arraycopy(tempArray, 0, send, i*tempArray.length,  Math.min(send.length - i*tempArray.length, tempArray.length));
		}
		return send;
	}
	//Decrypts the data to be as it was before the encryption process
	public   byte[] decrypt(byte[] data, byte[] key, int n){
		//ensure information is of decent quality
		if (n < 1 || n > 32) throw new IllegalArgumentException("Size of the cubes must be between 1 and 32 exclusive, got " + n); //arbitrary upper bounds to keep values reasonable, actual upper bounds is 512 / 2**9 b/c this will give the signed integer limit when cubed which is how java stores arrays, with ints
		//if ((n&0b1) == 1 && false) throw new IllegalArgumentException("Size of the cubes must be even, got " + n);
		if (data == null ? true : data.length==0) throw new IllegalArgumentException("Data array must not be null and must have at least 1 value stored within it");
		if (key == null ? true : key.length == 0) throw new IllegalArgumentException("Key array must not be null and must have at least 1 value stored within it");
		
		int arraySize = sizeOfArray(n);
		byte[] tempArray = new byte[arraySize];
		byte[] send = new byte[data.length%(arraySize) == 0 ? data.length : (data.length/(arraySize)+1)*arraySize];
		int varSize = (int)(Math.log(n) / Math.log(2)); //size of the floating size variables, i.e. x / y / depth
		int instructionSize = (int)(4 + 3* varSize); //size of each instruction
		int[] instructions = new int[key.length*8 / instructionSize + (key.length*8%instructionSize==0?0:1)]; //must be ints to have enough bits for n>=2. n=512 is the maximum size this instruction list can hold / allow
		parseInstructions(instructions, key, instructionSize); //parses the key array with 8-bit segments into the instruction's segments
		int face, x, y, depth, instruction; //setting aside space outside of loop, slightly more memory efficent (don't worry about it tho for your own projects)
		int bitMask = (int)(Math.pow(2, varSize)-1); //the bit mask used to isolate the x, y, depth values from the instruction
		
		for (int i=0;i<send.length/(arraySize);i++){
			tempArray = new byte[tempArray.length]; //resets tempArray so it will be filled with 0s
			System.arraycopy(data, i*tempArray.length, tempArray, 0, Math.min(data.length - i*tempArray.length, tempArray.length));
			
			for (int k=instructions.length-1;k>=0;k--){
				
				//do a general shift / rotation / XOR here
				for (face=5;face>=0;face--){
					
					for (y=0;y<n;y++){
						for (x=0;x<n;x++){
							if (((x+y)&1) == 1)shift(tempArray, face, x, y, -(x+y), n); //shifts every other index into the cube based on where it lies on the face
						}
					}
					for (int d=1;d<n;d+=2){
						rotateCCWLevel(tempArray, face, d, n); //rotates the even levels on every face clockwise
					}
				}
				listXOR(tempArray, key);
				for (face=5;face>=0;face--){
					
					for (y=0;y<n;y++){
						for (x=0;x<n;x++){
							if (((x+y)&1) == 0) shift(tempArray, face, x, y, -(x+y), n); //shifts every other index into the cube based on where it lies on the face
							
						}
					}
					for (int d=0;d<n;d+=2){
						rotateCCWLevel(tempArray, face, d, n); //rotates the even levels on every face clockwise
					}
				}
				
				instruction = instructions[k];
				face = ((instruction >>> instructionSize-1-3) & 0b111) % 6; //wrap it around from 0 to 5
				x = (instruction >>> instructionSize-1-3-varSize) & bitMask; //gets the first variable size variable from the instruction
				y = (instruction >>> instructionSize-1-3-varSize*2) & bitMask; //gets the second variable size variable from the instruction
				depth = instruction & bitMask; //gets the last variable size variable from the instruction, no need to shift, we just need to mask to get our value
				
				if (((instruction >>> instructionSize-1) & 0b1) == 0){
					//the instruction is saying to do a shift
					shift(tempArray, face, x, y, n-depth, n);
				}
				else{
					//the instruction is saying do a rotation, so do three to go back to the original
					rotateCCW(tempArray, face, x, y, depth, n);
				}
				
			}
			
			System.arraycopy(tempArray, 0, send, i*tempArray.length,  Math.min(send.length - i*tempArray.length, tempArray.length));
		}
		
		return send;
	}
	//Parses the instructions of the key based on the key packing docket above, takes the instrction list to return, the key array, and how big the instructions are in that order
	protected   int[] parseInstructions(int[] instructions, byte[] key, int size){
		if (size <= 0 || size > 32) throw new IllegalArgumentException("Instruction size must be between 0 and 33 exclusive, got " + size);
		if (instructions == null ? true : (key.length*8/size + (key.length*8%size == 0 ? 0:1)) > instructions.length) throw new IllegalArgumentException("Instruction return list must not be null, and must be able to store the entire key's instructions"); //instruction list can be length = 0 when the key list is too small to do an instruction - not advised
		if (key == null ? true : key.length == 0) throw new IllegalArgumentException("Key list must not be null and of a size greater than 0");
		if (instructions.length == 0 || key.length * 8 < size) return instructions; //edge case if key doesn't have the information to provide scrambling instructions
		//2. count bits supplied to instruction counter, move on when we have enough or we don't have more bits to suck from the key index
		int bitCounter = 0;
		int instruction = 0;
		int instructionCount = 0;
		int bitsNeeded = 0;
		int bitMask = (int)(Math.pow(2, size)-1);
		for (int i=0;i<key.length;i++){
			bitsNeeded = Math.min(size - bitCounter, 8); //we can only take 8 bits at a time, upper bound of 8, flow prevents this from becoming less than or equal to 0
			byte keyPos = key[i];
			if (bitsNeeded < 8){
				//filling out the last of the instruction without fully comsuming the key value
				instruction = (instruction << bitsNeeded) | (key[i] >>> (8-bitsNeeded)) & bitMask; //gets the needed bits from the front of the key value
				instructions[instructionCount++] = instruction; //all full of needed bits, we can store it now
				instruction = 0; //reset for next key loop
				keyPos = (byte)((keyPos | 0xff << (8-bitsNeeded)) ^ (0xff << (8-bitsNeeded))); //clears the part of the key that was just consumed
				
				int subCount = 1;
				for (subCount = 1;subCount<8 && (8-bitsNeeded-size*subCount) > 0;subCount++){
					instructions[instructionCount++] = ((keyPos >>> (8-bitsNeeded-size*subCount)) & bitMask); //puts in the value of the key that'll fit in the instruction, this only activates if instruction will be filled by what is left of the key
				}
				
				keyPos = (byte)((keyPos | 0xff << (8-bitsNeeded-size*(subCount-1))) ^ (0xff << (8-bitsNeeded-size*(subCount-1)))); //clears the section of key just consumed by the loop
				
				instruction = ((keyPos) & bitMask); //shifts our value over for the needed space, then puts in the value of the key that'll fit in the instruction
				bitCounter = (8-bitsNeeded-size*(subCount-1)); //number of bits caried from the key to the instruction
				continue;
			}
			
			instruction = (instruction << 8) | key[i]; //moves our instruction over and adds this key
			bitCounter += 8; //8 bits were added 
			
			if (bitCounter >= size){//special case where instruction is filled perfectly, store it and reset
				instructions[instructionCount++] = instruction; //storing of the value we care about
				instruction = 0; //reset after storage
				bitCounter = 0; //reset back to 0 bits in our instruction
			}
		}
		if (bitCounter != 0) instructions[instructionCount] = instruction;
		return instructions;
	}
	
	//Unit Testing:
	public   boolean unitTest(String name, boolean printResponses){
		boolean all = name.toLowerCase().equals("all"), returnValue = true;
		if (name.toLowerCase().equals("decryption") || all){ //testing the decryption part of the program by assuming encryption works correctly - can use random values for data / key
			if (printResponses)System.out.println("Decryption Unit Test:");
			
			//  test = Test 1
			byte[] original = new byte[] {1,2,3,4,5,6,7,8};
			byte[] test = new byte[original.length];
			System.arraycopy(original, 0, test, 0, original.length);
			byte[] key = new byte[] {(byte)0b10000001};
			
			test = encrypt(test, key, 2);
			test = decrypt(test, key, 2);
			
			boolean val = equals(original, test);
			boolean send = val;
			//if (send) send = val;
			if (printResponses)System.out.println("Test 1: " + (val ? "Pass" : "Failure"));
			
			//Test 2 = randomized inputs of set size, and with randomly sized / input key
			Random rand = new Random();
			rand.nextBytes(original);
			System.arraycopy(original, 0, test, 0, original.length);
			key = new byte[rand.nextInt(128)+1]; //random size key
			rand.nextBytes(key);
			
			test = encrypt(test, key, 2);
			test = decrypt(test, key, 2);
			
			val = equals(original, test);
			if (send) send = val;
			if (printResponses)System.out.println("Test 2: " + (val ? "Pass" : "Failure"));
			
			//Test 3 = randomized size / inputs for key and data
			original = new byte[rand.nextInt(128)+1];
			rand.nextBytes(original);
			test = new byte[original.length];
			System.arraycopy(original, 0, test, 0, test.length);
			key = new byte[rand.nextInt(128)+1];
			rand.nextBytes(key);
			
			test = encrypt(test, key, 2);
			test = decrypt(test, key, 2);
			
			val = equals(original, test);
			if (send) send = val;
			if (printResponses)System.out.println("Test 3: " + (val ? "Pass" : "Failure"));
			
			//return method
			if (!all) return send;
			if (all && returnValue) returnValue = send;
			if (printResponses)System.out.println();
		}
		if (name.toLowerCase().equals("encryption") || all){ //must be deterministic, cannot use random values, most encryption testing will take place in decryption testing grounds
			if (printResponses)System.out.println("Encryption Unit Test: ");
			
			byte[] test = new byte[] {1, 0, 0, 0, 0, 0, 0, 0};
			byte[] key = new byte[] {(byte)0};
			test = encrypt(test, key, 2);
			boolean val = equals(test, new byte[] {0, 0, 0, 0, 0, 0, 0, 1});
			boolean send = val;
			if (printResponses)System.out.println("Test 1: " + (val ? "Pass" : "Failure"));
			
			if (!all) return send;
			if (all && returnValue) returnValue = send;
			if (printResponses)System.out.println();
		}
		if (name.toLowerCase().equals("parsing") || all){ //testing the instruction parsing function to ensure the function gives the proper instructions for a given key
			if (printResponses)System.out.println("Parsing Unit Test: ");
			
			byte[] key = new byte[] {(byte)0b10000100};
			int[] instruct = new int[2];
			parseInstructions(instruct, key, 4);
			boolean val = equals(instruct, new int[] {8,4});
			boolean send = val;
			if (printResponses)System.out.println("Test 1: " + (val ? "Pass" : "Failure"));
			
			key = new byte[] {(byte)0b10000001};
			instruct = new int[2];
			parseInstructions(instruct, key, 7);
			val = equals(instruct, new int[] {64,1});
			if (send) send = val;
			if (printResponses)System.out.println("Test 2: " + (val ? "Pass" : "Failure"));
			
			if (!all) return send;
			if (all && returnValue) returnValue = send;
			if (printResponses)System.out.println();
		}
		if (name.toLowerCase().equals("index") || all){ //testing the getIndex function to ensure it returns the indexs expected in the order they are expected
			if (printResponses)System.out.println("Getting Index Unity Test:");
			
			int[] array = new int[8];
			int index = 0;
			for (int d=0;d<2;d++){
				for (int y=0;y<2;y++){
					for (int x=0;x<2;x++){
						array[index++] = getIndex(0, x, y, d, 2)+1;
					}
				}
			}
			boolean val = equals(array, new int[] {1,2,3,4,5,6,7,8});
			boolean send = val;
			if (printResponses)System.out.println("Test 1: " + (val ? "Pass" : "Failure"));
			
			index=0;
			for (int d=0;d<2;d++){
				for (int y=0;y<2;y++){
					for (int x=0;x<2;x++){
						array[index++] = getIndex(1, x, y, d, 2)+1;
					}
				}
			}
			val = equals(array, new int[] {3,4,7,8,1,2,5,6});
			if (send) send = val;
			if (printResponses)System.out.println("Test 2: " + (val ? "Pass" : "Failure"));
			
			index=0;
			for (int d=0;d<2;d++){
				for (int y=0;y<2;y++){
					for (int x=0;x<2;x++){
						array[index++] = getIndex(2, x, y, d, 2)+1;
					}
				}
			}
			val = equals(array, new int[] {4,2,8,6,3,1,7,5});
			if (send) send = val;
			if (printResponses)System.out.println("Test 3: " + (val ? "Pass" : "Failure"));
			
			index=0;
			for (int d=0;d<2;d++){
				for (int y=0;y<2;y++){
					for (int x=0;x<2;x++){
						array[index++] = getIndex(3, x, y, d, 2)+1;
					}
				}
			}
			val = equals(array, new int[] {2,1,6,5,4,3,8,7});
			if (send) send = val;
			if (printResponses)System.out.println("Test 4: " + (val ? "Pass" : "Failure"));
			
			index=0;
			for (int d=0;d<2;d++){
				for (int y=0;y<2;y++){
					for (int x=0;x<2;x++){
						array[index++] = getIndex(4, x, y, d, 2)+1;
					}
				}
			}
			val = equals(array, new int[] {1,3,5,7,2,4,6,8});
			if (send) send = val;
			if (printResponses)System.out.println("Test 5: " + (val ? "Pass" : "Failure"));
			
			index=0;
			for (int d=0;d<2;d++){
				for (int y=0;y<2;y++){
					for (int x=0;x<2;x++){
						array[index++] = getIndex(5, x, y, d, 2)+1;
					}
				}
			}
			val = equals(array, new int[] {6,5,8,7,2,1,4,3});
			if (send) send = val;
			if (printResponses)System.out.println("Test 6: " + (val ? "Pass" : "Failure"));
			
			if (!all) return send;
			if (all && returnValue) returnValue = send;
			if (printResponses)System.out.println();
		}
		if (name.toLowerCase().equals("rotate") || all){ //testing to ensure rotations on faces work correctly, when printing, will print relative / absolute conversion, no output without printing (only rotation, no coord translation)
			if (printResponses)System.out.println("Rotation Unit Test:");
			
			byte[] array = new byte[] {1,2,3,4,5,6,7,8};
			rotate(array, 0, 0, 0, 1, 2);
			rotate(array, 0, 0, 0, 1, 2);
			rotate(array, 0, 0, 0, 1, 2);
			rotate(array, 0, 0, 0, 1, 2);
			boolean val = equals(array, new byte[] {1,2,3,4,5,6,7,8});
			boolean send = val;
			if (printResponses) System.out.println("Test 1: " + (val ? "Pass" : "Failure"));
			
			array = new byte[] {1,2,3,4,5,6,7,8};
			rotate(array, 1, 0, 0, 1, 2);
			rotateCCW(array, 1, 0, 0, 1, 2);
			val = equals(array, new byte[] {1,2,3,4,5,6,7,8});
			if (send) send = val;
			if (printResponses)System.out.println("Test 2: " + (val ? "Pass" : "Failure"));
			
			array = new byte[27];
			for (int i=0;i<array.length;i++){array[i] = (i<9 ? (byte)(i+1) : 0);}
			print(array);
			rotateLevel(array, 0, 0, 3);
			print(array);
			
			if (printResponses){
				System.out.println("\nRelative & Absolute translation sub test:");
				System.out.println("Absolute to Relative:");
				System.out.println(absToRel(0, 2) == -1 ? "Pass" : "Failure");
				System.out.println(absToRel(1, 2) == 1 ? "Pass" : "Failure");
				System.out.println("Relative to Absolute:");
				System.out.println(relToAbs(-1, 2) == 0 ? "Pass" : "Failure");
				System.out.println(relToAbs(1 , 2) == 1 ? "Pass" : "Failure");
			}
			
			if (!all) return send;
			if (all && returnValue) returnValue = send;
			if (printResponses)System.out.println();
		}
		if (name.toLowerCase().equals("shift") || all){ //testing to ensure the shifting works correctly and data moves as intended
			if (printResponses)System.out.println("Shift Unit Test:");
			
			byte[] array = new byte[] {1, 2, 3, 4, 5, 6, 7, 8};
			shift(array, 0, 0, 0, 1, 2);
			boolean val = equals(array, new byte[] {5,2,3,4,1,6,7,8});
			boolean send = val;
			if (printResponses)System.out.println("Test 1: " + (val ? "Pass" : "Failure"));
			
			array = new byte[] {1, 2, 3, 4, 5, 6, 7, 8};
			shift(array, 0, 1, 1, 1, 2);
			val = equals(array, new byte[] {1,2,3,8,5,6,7,4});
			if (send) send = val;
			if (printResponses)System.out.println("Test 2: " + (val ? "Pass" : "Failure"));
			
			array = new byte[] {1, 0, 0, 0, 0, 0, 0, 0};
			shift(array, 0, 0, 0, 1, 2);
			val = equals(array, new byte[] {0, 0, 0, 0, 1, 0, 0, 0});
			if (send) send = val;
			if (printResponses)System.out.println("Test 3: " + (val ? "Pass" : "Failure"));
			
			array = new byte[] {1, 0, 0, 0, 0, 0, 0, 0};
			shift(array, 0, 0, 0, 2, 2);
			val = equals(array, new byte[] {1, 0, 0, 0, 0, 0, 0, 0});
			if (send) send = val;
			if (printResponses)System.out.println("Test 4: " + (val ? "Pass" : "Failure"));
			
			array = new byte[] {1,2,3,4,5,6,7,8};
			shift(array, 0, 0, 0, -1, 2);
			val = equals(array, new byte[] {5,2,3,4,1,6,7,8});
			if (send) send = val;
			if (printResponses) System.out.println("Test 5: " + (val ? "Pass" : "Failure"));
			
			if (!all) return send; //if only this section, return results
			if (all && returnValue) returnValue = send; //collect results from this section
			if (printResponses)System.out.println();
		}
		if (name.toLowerCase().equals("flags") || all){ //testing to make sure the flag indexs work correctly and information is properly stored and recalled
			if (printResponses)System.out.println("Flag Unit Test:");
			
			boolean val = flagGet(flagSet(0L, true, 15), 15); //check if val is true to determine pass / not
			boolean send = val; //if we already failed, make sure to keep track of it - value to return after we are done
			if (printResponses)System.out.println("Test 1: " + (val ? "Pass" : "Failure"));
			
			val = flagGet(flagSet(0L, true, 63), 63);
			if (send) send = val; //only overwrite send if things are still working
			if (printResponses)System.out.println("Test 2: " + (val ? "Pass" : "Failure"));
			
			if (!all) return send; //we have finished, turn in the results
			if (all && returnValue) returnValue = send; //collect results from this section
			if (printResponses)System.out.println();
		}
		if (name.toLowerCase().equals("wrap") || all){ //tests in range, above range, and below range to be accurate
			if (printResponses)System.out.println("Wrapping Unit Test:");
			
			boolean val = wrap(2, 1, 5) == 2;
			boolean send = val;
			if (printResponses)System.out.println("Test 1: " + (val ? "Pass" : "Failure"));
			
			val = wrap(6, 1, 5) == 2;
			if (send) send = val;
			if (printResponses)System.out.println("Test 2: " + (val ? "Pass" : "Failure"));
			
			val = wrap(-3, 1, 5) == 1;
			if (send) send = val;
			if (printResponses)System.out.println("Test 2: " + (val ? "Pass" : "Failure"));
			
			if (!all) return send; //if only this section, return results
			if (all && returnValue) returnValue = send; //collect results from this section
			if (printResponses)System.out.println();
		}
		return all ? returnValue : false; //return results collected or nothing if input was wrong
	}
	//Function used to ensure rotation and shifting happen correctly by comparing the two arrays passed in -> See Function: unitTest
	public   boolean equals(byte[] array1, byte[] array2){
		if (array1.length != array2.length) return false; //different sizes means they can't be completely equal
		for (int i=0;i<array1.length;i++){
			if (array1[i] != array2[i]) return false; //difference detected, no way they are all equal now
		}
		return true; //no differences detected
	}
	public   boolean equals(int[] array1, int[] array2){ //same exact equals method as byte array above, just for ints
		if (array1.length != array2.length) return false;
		for (int i=0;i<array1.length;i++){
			if (array1[i] != array2[i]) return false;
		}
		return true;
	}
	public   void print(byte[] array){
		for (byte num : array){
			System.out.print(num + ", ");
		}
		System.out.println();
	}
	public   void print(int[] array){
		for (int num : array){
			System.out.print(num + ", ");
		}
		System.out.println();
	}
	
	//Sets the bit at flagIndex of flagStorage to 0 / 1 depending on if setValue is false / true respectfully
	public   long flagSet(long flagStorage, boolean setValue, int flagIndex){
		if (flagIndex < 0 || flagIndex >= 64) throw new IllegalArgumentException("Flag Index must be between 0 and 63 inclusive, got " + flagIndex);
		if (setValue){
			return flagStorage | (1<<flagIndex); //ensures the flag is set to a 1 or true in this 64 bit storage medium
		}
		return flagStorage & ~(1<<flagIndex); //ensures the flag is set to a 0 or false in this 64 bit storage medium
	}
	//Returns the boolen value of flagStorage at flagIndex depending on if the bit is 0 / 1
	public   boolean flagGet(long flagStorage, int flagIndex){
		if (flagIndex < 0 || flagIndex >= 64) throw new IllegalArgumentException("Flag Index must be between 0 and 63 inclusive, got " + flagIndex);
		return ((flagStorage >> flagIndex) & 0b1) == 1;
	}
}