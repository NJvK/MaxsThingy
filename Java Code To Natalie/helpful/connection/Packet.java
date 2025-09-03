package helpful.connection;
import java.io.*;
import java.net.*;
public class Packet{
	protected byte[] data; //the array holding all data, header and random
	protected int count=0; //Stores the index of where to add more data
	
	//Static variables that should only change by extensions of this class
	protected static int HEADERSIZE = 32;   //The size of all the information about the packet, recipient and sender
	
	protected static int IPRECEIVERSTART=0; 					//0:The index of the header where the recipient ip:port is going to start, taking 4 bytes
	protected static int IPSENDERSTART = IPRECEIVERSTART+6; 	//4:The index of the header where the sender's ip:port is going to start, taking 4 bytes
	protected static int TIMELOGSTART = IPSENDERSTART+6; 		//8:The index of the header where the time this packet is send is stored, taking 8 bytes
	protected static int BOOLDATASTART= TIMELOGSTART+8; 		//16:The index of the header where misc boolean values are stored (1 bit per val), taking 1 byte
	protected static int PRIMDATASTART= BOOLDATASTART+1; 		//17:The index of the header where the type of data being stored in the packet is stored, 4/8 bits are used, taking 1 byte
	protected static int SIZESTART = PRIMDATASTART+1;   		//18:The index of the header where the size info is stored, taking SIZEBYTESIZE bytes
	protected static int SIZEBYTESIZE = 4;  					//The number of bytes used in the header to transmit the size of the packet
	protected static int ERRORSTART = SIZESTART+SIZEBYTESIZE;	//22:The index of the header where the error code is stored 
	protected static int BUGFIXINDEX = 31;						//31:The index to prevent stupidity on client side when server causes disconnect
	
	protected static int MAXDATASIZE = (int)Math.pow(2, SIZEBYTESIZE*8); //Used to determine if the data being added or the size being set is valid
	
	protected final static byte BYTE=0, SHORT=1, INT=2, LONG=3, FLOAT=4, DOUBLE=5, CHAR=6, BOOLEAN=7, STRING=8;
	protected static Class<?>[] dataType;
	//Constructs a packet of correct size
	public Packet(int size){
		if (size > MAXDATASIZE){
			//too big for packet standardization
			throw new IllegalArgumentException("Size of " + size + " too big for Max Size of " + MAXDATASIZE);
		}
		
		this.data = new byte[size+HEADERSIZE];
		this.count = HEADERSIZE;
		
		setDataList();
	}
	//Constructs a new packet with the preset data already inside
	public Packet(byte[] tempData){
		if (tempData == null)
			throw new NullPointerException("Initialized data array cannot be null");
		if (tempData.length > MAXDATASIZE){
			//too big for packet stanardization
			throw new IllegalArgumentException("Data size of " + tempData.length + " too big for Max Size of " + MAXDATASIZE);
		}
		
		byte[] temp = new byte[tempData.length+HEADERSIZE];
		System.arraycopy(tempData, 0, temp, HEADERSIZE, tempData.length);
		
		this.data = temp;
		this.count = HEADERSIZE + tempData.length;
		
		setDataList();
	}
	//Constructs a new packet with the header of the failed packet in the data of this packet, with the error value as shown
	public Packet(int errorCode, Packet failedPacket){
		this(failedPacket == null ? 0 : HEADERSIZE);
		if (errorCode > Short.MAX_VALUE){
			//Error code is too big
			throw new IllegalArgumentException("Error code of " + errorCode + " is too big");
		}
		//copies the header into the data section of this packet
		if (failedPacket != null){
			System.arraycopy(failedPacket.sendComplete(), 0, data, HEADERSIZE, HEADERSIZE);
			this.count = HEADERSIZE*2;
		}
		
		setErrorVal((short)errorCode);
	}
	//Used exclusivly internally to create a packet from the data recieved over the stream
	private Packet(byte[] header, byte[] tempData){
		this.data = new byte[header.length + tempData.length];
		System.arraycopy(header, 0, data, 0, header.length);
		System.arraycopy(tempData, 0, data, header.length, tempData.length);
		
		this.count = header.length + tempData.length;
		
		setDataList();
	}
	//Loads the array "dataType" with the classes it should hold and the maximum size of the array - Thread safe
	protected synchronized void setDataList(){
		if (dataType == null){
			dataType = new Class<?>[256];
			dataType[0] = byte.class;
			dataType[1] = short.class;
			dataType[2] = int.class;
			dataType[3] = long.class;
			dataType[4] = float.class;
			dataType[5] = double.class;
			dataType[6] = char.class;
			dataType[7] = boolean.class;
			dataType[8] = (new String()).getClass();
		}
	}
	//adds the array of bytes to the data array, returns true if all the bytes fit, false if any bytes were not inputted
	public void add(byte[] addition){
		if (addition == null)
			throw new NullPointerException("addition cannot be null");
		if (count + addition.length > data.length)
			throw new IllegalArgumentException("Cannot add data of length " + addition.length + " to data storage with " + (data.length - count - HEADERSIZE) + " bytes left");
		
		System.arraycopy(addition, 0, data, count, Math.max(0, Math.min(data.length-count, addition.length)));
		count += addition.length;
	}
	//Just adds a single byte to the data
	public void add(byte addition){
		if (count + 1 > data.length)
			throw new IllegalArgumentException("Cannot add data of length " + 1 + " to data storage with " + (data.length - count - HEADERSIZE) + " bytes left");
		
		data[count++] = addition;
	}
	//Used internally to set the size parameter in the header
	protected void setSizeHeader(int size){
		byte[] sizeBytes = new byte[SIZEBYTESIZE];
		
		for (int i=0;i<Math.min(SIZEBYTESIZE, HEADERSIZE - SIZESTART) && size > 0;i++){
			sizeBytes[SIZEBYTESIZE-1-i] = (byte)(size & 0xFF);
			size >>= 8;
		}
		
		System.arraycopy(sizeBytes, 0, data, SIZESTART, Math.min(SIZEBYTESIZE, HEADERSIZE - SIZESTART));
	}
	//Used internally to set the ip / port of the recieving end of this packet, used before returning data for sending
	protected void setRecieverHeader(InetAddress addr, int port){
		System.arraycopy(addr.getAddress(), 0, data, IPRECEIVERSTART, 4);
		System.arraycopy(ByteTranslater.toBytes(port), 2, data, IPRECEIVERSTART+4, 2);
	}
	//Used internally to set the ip of this computer for this packet, used before returning data for sending
	protected void setSenderHeader(int sendersPort){
		try{
			System.arraycopy(InetAddress.getLocalHost().getAddress(), 0, data, IPSENDERSTART, 4);
			System.arraycopy(ByteTranslater.toBytes(sendersPort), 2, data, IPSENDERSTART+4, 2);
		}
		catch(UnknownHostException e){
			e.printStackTrace();
		}
	}
	//Used internally to set the time this packet was sent, used before returning data for sending
	protected void setTimeHeader(){
		System.arraycopy(ByteTranslater.toBytes(System.currentTimeMillis()), 0, data, TIMELOGSTART, 8);
	}
	//Used internally to set the boolean values of this packet, declared for sending
	protected void setBoolHeader(boolean... vals){
		data[BOOLDATASTART] = ByteTranslater.toBytes(vals)[0];
	}
	//Used internally to set the data type for this packet, declared for sending
	protected void setDataHeader(int val){
		data[PRIMDATASTART] = ByteTranslater.toBytes(val)[0];
	}
	//Used internally to set the error code of this packet, declared on initalization
	protected void setErrorVal(short val){
		System.arraycopy(ByteTranslater.toBytes(val), 0, data, ERRORSTART, 2);
	}
	//sets the specified byte to 1, ensures during disconnects we are notified of it and can safely remove the data
	public void setServerProt(){
		data[BUGFIXINDEX] = 1;
	}
	//Used to set the header to proper values and returns the byte array representing all the data
	public byte[] send(InetAddress reciever, int port, int sendersPort, byte dataClass, boolean... booleanValues){
		setSizeHeader(data.length-HEADERSIZE);
		setRecieverHeader(reciever, port);
		setSenderHeader(sendersPort);
		setTimeHeader();
		setBoolHeader(booleanValues);
		setDataHeader(dataClass);
		setServerProt();
		return this.data;
	}
	//Returns the entire data array without making any changes, used when the packet's data is completely known like in a server relay stop
	public byte[] sendComplete(){
		return this.data;
	}
	//Returns a class object representing the type of data in the packet
	public Class<?> getDataType(){
		byte type = data[PRIMDATASTART];
		return dataType[type];
	}
	//Returns the boolean data contained in the header
	public boolean[] getBoolData(){
		//[0] - is this the last packet in a string of packets (true if singular packet with no extras)
		//[1] - is there more info coming? possibly of a different data type
		return ByteTranslater.toBools(data[BOOLDATASTART]);
	}
	//Returns the difference in milliseconds between now and when the packet was sent
	public long getDelayMillis(){
		//Returns the difference between when the packet was sent vs now
		byte[] timeSent = new byte[8];
		System.arraycopy(data, TIMELOGSTART, timeSent, 0, 8);
		return System.currentTimeMillis() - ByteTranslater.toLong(timeSent);
	}
	//Returns a byte array representing who this packet came from
	public byte[] getSenderIP(){
		byte[] send = new byte[6];
		System.arraycopy(data, IPSENDERSTART, send, 0, 6);
		return send;
	}
	//Returns a byte array representing who this packet is being sent to
	public byte[] getReceiverIP(){
		byte[] send = new byte[6];
		System.arraycopy(data, IPRECEIVERSTART, send, 0, 6);
		return send;
	}
	//returns an array containing the pure data
	public byte[] getData(){
		int size = ByteTranslater.toInt(data, SIZESTART);
		
		byte[] send = new byte[size];
		System.arraycopy(data, 32, send, 0, Math.min(size, data.length-32));
		return send;
	}
	//Returns the errorCode associated with this packet
	public short getErrorCode(){
		return ByteTranslater.toShort(data, ERRORSTART);
	}
	public boolean getAlive(){
		return data[BUGFIXINDEX] == 1;
	}
	//Returns a String representaion of this packet's data
	public String toString(){
		String send = "";
		for (int i=0;i<data.length;i++){
			send += data[i] + ", ";
			if (i == HEADERSIZE-1) send += "//";
		}
		return "Packet[" + data.length + ", {" + send + "}]";
	}
	//Returns the maximum size of the packets
	public static long maxSize(){
		return (long)(Math.pow(2, 8*SIZEBYTESIZE));
	}
	//Reads the Socket inputStream and returns a Packet created on the data passed through
	public static Packet read(InputStream stream){
		byte[] tempHeader = new byte[HEADERSIZE];
		try{
			stream.read(tempHeader);
		}
		catch(SocketException e){
			return null; //the socket is closed
		}
		catch(IOException e){
			e.printStackTrace();
		}
		
		byte[] tempData = new byte[SIZEBYTESIZE];
		System.arraycopy(tempHeader, SIZESTART, tempData, 0, SIZEBYTESIZE);
		
		tempData = new byte[ByteTranslater.toInt(tempData)];
		try{
			stream.read(tempData);
		}
		catch(SocketException e){
			return null; //the socket is closed
		}
		catch(IOException e){
			e.printStackTrace();
		}
		return (new Packet(tempHeader, tempData));
	}
	//Combines the byte arrays into 1 massive array containging all the arrays one after another
	public static byte[] combine(byte[]... arrays){
		int size = 0;
		for (byte[] array:arrays){
			size += array.length;
		}
		byte[] send = new byte[size];
		size=0;
		for (byte[] array:arrays){
			System.arraycopy(array, 0, send, size, array.length);
			size += array.length;
		}
		return send;
	}
}