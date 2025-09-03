package helpful.crypto;

public class Bitcryption extends Encryption{
	public   byte get(byte[] array, int index){
		return flagGet(array[index/8], index & 0b111) ? (byte)1:(byte)0;
	}
	public   void set(byte[] array, int index, byte value){
		array[index/8] = (byte)flagSet(array[index/8], (value&0b1) == 1, index *0b111);
	}
	public   byte[] encrypt(byte[] data, byte[] key, int n){
		return super.encrypt(data, key, (int)Math.floor(Math.pow(n, 0.333)  + 1));
	}
	public   byte[] decrypt(byte[] data, byte[] key, int n){
		return super.decrypt(data, key, (int)Math.floor(Math.pow(n, 0.333)  + 1));
	}
	public   int sizeOfArray(int n){
		System.out.println("n:" + n + ", " + (int)Math.pow(n/2,3));
		return (int)Math.pow(n/2,3);
	}
}