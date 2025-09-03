package helpful.connection;
public class ByteTranslater{
	//This class is supposed to be a more comprehensive byte translater to translate entire lists at a time
	//It translates with the assumption for each section that index 0 is the most significant digit and grows rightward
	public static byte[] toBytes(long... nums){
		if (nums == null) throw new NullPointerException("Array cannot be null");
		byte[] send = new byte[nums.length*8];
		
		for (int n=0;n<nums.length;n++){
			long num = nums[n];
			for (int i=0;i<8;i++){
				send[7-i+8*n] = (byte)(num & 0xFF);
				num >>= 8;
			}
		}
		
		return send;
	}
	public static byte[] toBytes(int... nums){
		if (nums == null) throw new NullPointerException("Array cannot be null");
		byte[] send = new byte[nums.length*4];
		
		for (int n=0;n<nums.length;n++){
			int num = nums[n];
			for (int i=0;i<4;i++){
				send[3-i+4*n] = (byte)(num & 0xFF);
				num >>= 8;
			}
		}
		
		return send;
	}
	public static byte[] toBytes(char... letters){
		if (letters == null) throw new NullPointerException("Array cannot be null");
		byte[] send = new byte[letters.length*2];
		
		for (int n=0;n<letters.length;n++){
			char num = letters[n];
			for (int i=0;i<2;i++){
				send[1-i+2*n] = (byte)(num & 0xFF);
				num >>= 8;
			}
		}
		
		return send;
	}
	//Low precision bytes, a single byte is allocated to each char, good enough for english alphabet
	public static byte[] toSmallBytes(char... letters){
		if (letters == null) throw new NullPointerException("Array cannot be null");
		byte[] send = new byte[letters.length];
		
		for (int n=0;n<letters.length;n++){
			send[n] = (byte)letters[n];
		}
		
		return send;
	}
	public static byte[] toBytes(short... nums){
		if (nums == null) throw new NullPointerException("Array cannot be null");
		byte[] send = new byte[nums.length*2];
		
		for (int n=0;n<nums.length;n++){
			short num = nums[n];
			for (int i=0;i<2;i++){
				send[1-i+2*n] = (byte)(num & 0xFF);
				num >>= 8;
			}
		}
		
		return send;
	}
	public static byte[] toBytes(boolean... bools){
		if (bools == null) throw new NullPointerException("Array cannot be null");
		byte[] send = new byte[bools.length/8+1 - (bools.length%8==0?1:0)];
		for (int i=0;i<(bools.length/8+1)*8;i++){
			send[i/8]<<=1;
			send[i/8] |= (i < bools.length ? (bools[i] ?1:0) : 0);
		}
		return send;
	}
	public static byte[] toBytes(double... nums){
		if (nums == null) throw new NullPointerException("Array cannot be null");
		byte[] send = new byte[nums.length * 8];
		byte[] temp;
		
		for (int i=0;i<nums.length;i++){
			temp = toBytes(Double.doubleToLongBits(nums[i]));
			System.arraycopy(temp, 0, send, i*8, 8);
		}
		
		return send;
	}
	public static byte[] toBytes(float... nums){
		if (nums == null) throw new NullPointerException("Array cannot be null");
		byte[] send = new byte[nums.length * 4];
		byte[] temp = new byte[4];
		
		for (int i=0;i<nums.length;i++){
			temp = toBytes(Float.floatToIntBits(nums[i]));
			System.arraycopy(temp, 0, send, i*4, 4);
		}
		
		return send;
	}
	
	public static long[] toLongs(byte[] array){
		if (array == null) throw new NullPointerException("Array cannot be null");
		if (array.length < 8) throw new IllegalArgumentException("Not enough bytes to convert into chars, Array Size:" + array.length + ", Needed 8");
		long[] send = new long[array.length/8];
		for (int n=0;n<send.length;n++){
			long num =0L;
			for (int i=0;i<8;i++){
				num <<= 8;
				num |= (array[n*8+i] & 0xFF);
			}
			send[n] = num;
		}
		return send;
	}
	public static int[] toInts(byte[] array){
		if (array == null) throw new NullPointerException("Array cannot be null");
		if (array.length < 4) throw new IllegalArgumentException("Not enough bytes to convert into ints, Array Size:" + array.length + ", Needed 4");
		int[] send = new int[array.length/4];
		for (int n=0;n<send.length;n++){
			int num =0;
			for (int i=0;i<4;i++){
				num <<= 8;
				num |= (array[n*4+i] & 0xFF);	
			}
			send[n] = num;
		}
		return send;
	}
	public static char[] toChars(byte[] array){
		if (array == null) throw new NullPointerException("Array cannot be null");
		if (array.length < 2) throw new IllegalArgumentException("Not enough bytes to convert into chars, Array Size:" + array.length + ", Needed 2");
		char[] send = new char[array.length/2];
		for (int n=0;n<send.length;n++){
			int num =0;
			for (int i=0;i<2;i++){
				num <<= 8;
				num |= (array[n*2+i] & 0xFF);	
			}
			send[n] = (char)num;
		}
		return send;
	}
	public static char[] toSmallChars(byte[] array){
		if (array == null) throw new NullPointerException("Array cannot be null");
		if (array.length < 1) throw new IllegalArgumentException("Not enough bytes to convert into chars, Array Size:" + array.length + ", Needed 1");
		char[] send = new char[array.length];
		for (int n=0;n<send.length;n++){
			send[n] = (char)array[n];
		}
		return send;
	}
	public static short[] toShorts(byte[] array){
		if (array == null) throw new NullPointerException("Array cannot be null");
		if (array.length < 2) throw new IllegalArgumentException("Not enough bytes to convert into doubles, Array Size:" + array.length + ", Needed 2");
		short[] send = new short[array.length/2];
		for (int n=0;n<send.length;n++){
			int num =0;
			for (int i=0;i<2;i++){
				num <<= 8;
				num |= (array[n*2+i] & 0xFF);	
			}
			send[n] = (short)num;
		}
		return send;
	}
	public static boolean[] toBools(byte[] array){
		if (array == null) throw new NullPointerException("Array cannot be null");
		if (array.length < 1) throw new IllegalArgumentException("Not enough bytes to convert into doubles, Array Size:" + array.length + ", Needed 1");
		boolean[] send = new boolean[array.length*8];
		int count = 0;
		for (byte num : array){
			System.arraycopy(toBools(num), 0, send, count*8, 8);
		}
		return send;
	}
	public static double[] toDoubles(byte[] array){
		if (array == null) throw new NullPointerException("Array cannot be null");
		if (array.length < 8) throw new IllegalArgumentException("Not enough bytes to convert into doubles, Array Size:" + array.length + ", Needed 8");
		double[] send = new double[array.length/8];
		for (int n=0;n<send.length;n++){
			long num =0;
			for (int i=0;i<8;i++){
				num <<= 8;
				num |= (array[n*8+i] & 0xFF);	
			}
			send[n] = Double.longBitsToDouble(num);
		}
		return send;
	}
	public static float[] toFloats(byte[] array){
		if (array == null) throw new NullPointerException("Array cannot be null");
		if (array.length < 4) throw new IllegalArgumentException("Not enough bytes to convert into floats, Array Size:" + array.length + ", Needed 4");
		float[] send = new float[array.length/4];
		for (int n=0;n<send.length;n++){
			int num = 0;
			for (int i=0;i<4;i++){
				num <<= 4;
				num |= (array[n*4+i * 0xFF]);
			}
			send[n] = Float.intBitsToFloat(num);
		}
		return send;
	}
	
	//Returns the long contained in the byte array starting at startIndex
	public static long toLong(byte[] array){
		return toLong(array, 0);
	}
	public static long toLong(byte[] array, int startIndex){
		if (array == null) throw new NullPointerException("Array cannot be null");
		if (array.length < startIndex + 8) throw new IllegalArgumentException("Not enough bytes to convert into long, startIndex:" + startIndex + ", Array Size:" + array.length);
		if (startIndex >= array.length) throw new IndexOutOfBoundsException();
		long send = 0L;
		for (int i = 0;i<8;i++){
			send <<= 8;
			send |= (array[startIndex+i]&0xFF);
		}
		return send;
	}
	
	public static int toInt(byte[] array){
		return toInt(array, 0);
	}
	public static int toInt(byte[] array, int startIndex){
		if (array == null) throw new NullPointerException("Array cannot be null");
		if (array.length < startIndex + 4) throw new IllegalArgumentException("Not enough bytes to convert into integer, startIndex:" + startIndex + ", Array Size:" + array.length);
		if (startIndex >= array.length) throw new IndexOutOfBoundsException();
		int send = 0;
		for (int i = 0;i<4;i++){
			send <<= 8;
			send |= (array[startIndex+i]&0xFF);
		}
		return send;
	}
	
	public static short toShort(byte[] array){
		return toShort(array, 0);
	}
	public static short toShort(byte[] array, int startIndex){
		if (array == null) throw new NullPointerException("Array cannot be null");
		if (array.length < startIndex + 2) throw new IllegalArgumentException("Not enough bytes to convert into short, startIndex:" + startIndex + ", Array Size:" + array.length);
		if (startIndex >= array.length) throw new IndexOutOfBoundsException();
		short send = 0;
		for (int i = 0;i<2;i++){
			send <<= 8;
			send |= (short)(array[startIndex+i]&0xFF);
		}
		return send;
	}
	
	public static char toChar(byte[] array){
		return toChar(array, 0);
	}
	public static char toChar(byte[] array, int startIndex){
		if (array == null) throw new NullPointerException("Array cannot be null");
		if (array.length < startIndex + 2) throw new IllegalArgumentException("Not enough bytes to convert into character, startIndex:" + startIndex + ", Array Size:" + array.length);
		if (startIndex >= array.length) throw new IndexOutOfBoundsException();
		return (char)toShort(array, startIndex);
	}
	
	public static char toSmallChar(byte[] array){
		return toSmallChar(array, 0);
	}
	public static char toSmallChar(byte[] array, int startIndex){
		if (array == null) throw new NullPointerException("Array cannot be null");
		if (array.length < startIndex + 1) throw new IllegalArgumentException("Not enough bytes to convert into character, startIndex:" + startIndex + ", Array Size:" + array.length);
		if (startIndex >= array.length) throw new IndexOutOfBoundsException();
		return (char)array[startIndex];
	}
	
	public static boolean toBool(byte[] array){
		return toBool(array, 0);
	}
	public static boolean toBool(byte[] array, int index){
		if (array == null) throw new NullPointerException("Array cannot be null");
		if (array.length < index + 1) throw new IllegalArgumentException("Not enough bytes to convert into boolean, startIndex:" + index + ", Array Size:" + array.length + " ... Seriously?");
		if (index >= array.length) throw new IndexOutOfBoundsException();
		return (array[index] >> 8) == 1;
	}
	
	public static double toDouble(byte[] array){
		return toDouble(array, 0);
	}
	public static double toDouble(byte[] array, int startIndex){
		if (array == null) throw new NullPointerException("Array cannot be null");
		if (array.length < startIndex + 8) throw new IllegalArgumentException("Not enough bytes to convert into double, startIndex:" + startIndex + ", Array Size:" + array.length);
		if (startIndex >= array.length) throw new IndexOutOfBoundsException();
		return Double.longBitsToDouble(toLong(array, startIndex));
	}
	
	public static float toFloat(byte[] array){
		return toFloat(array, 0);
	}
	public static float toFloat(byte[] array, int startIndex){
		if (array == null) throw new NullPointerException("Array cannot be null");
		if (array.length < startIndex + 4) throw new IllegalArgumentException("Not enough bytes to convert into float, startIndex:" + startIndex + ", Array Size:" + array.length);
		if (startIndex >= array.length) throw new IndexOutOfBoundsException();
		return Float.intBitsToFloat(toInt(array, startIndex));
	}
	
	public static boolean[] toBools(byte val){
		boolean[] send = new boolean[8];
		int count = 0;
		while (count < 8){
			send[7-count++] = (val & 0b1) == 1;
			val >>= 1;
		}
		return send;
	}
	
	//Returns a new byte array based on the input array and the indexes passed in
	public static byte[] specificArray(byte[] array, int... indexes){
		if (array == null) throw new NullPointerException("Array cannot be null");
		byte[] send = new byte[indexes.length];
		for (int i=0;i<send.length;i++){
			if (indexes[i] < 0 || indexes[i] >= array.length) throw new IllegalArgumentException("The indexes array must contian positive integers less than the length of the array");
			send[i] = array[indexes[i]];
		}
		return send;
	}
	
	//Returns a byte array starting from startIndex and containing all the bytes up to and excluding endIndex
	public static byte[] subArray(byte[] array, int startIndex, int endIndex){
		if (array == null) throw new NullPointerException("Array cannot be null");
		if (startIndex >= array.length) throw new IndexOutOfBoundsException("Start index must be below " + array.length + ", not " + startIndex);
		if (startIndex < 0) throw new IndexOutOfBoundsException("Start index must be a positive number greater than or equal to 0");
		if (endIndex > array.length) throw new IndexOutOfBoundsException("End index must be below or equal to " + array.length + ", not " + endIndex);
		if (endIndex <= startIndex) throw new IllegalArgumentException("End index cannot be before or at Start index");
		byte[] send = new byte[endIndex-startIndex];
		System.arraycopy(array, startIndex, send, 0, send.length);
		return send;
	}
	//Returns the exact same as subArray(byte[] array, int startIndex, int array.length-1)
	public static byte[] subArray(byte[] array, int startIndex){
		return subArray(array, startIndex, array.length-1);
	}
}