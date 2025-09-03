package helpful.connection;
import java.net.*;
import java.io.*;
import java.lang.NullPointerException;
import java.lang.IndexOutOfBoundsException;
import helpful.datastructures.DLList;
import helpful.datastructures.ShiftRegister;
import helpful.datastructures.Graph;

public final class ConnectionHandler implements Closeable{
	private boolean isServer, isConnected, isNetwork, voidBuffer, notifyNetwork; //void buffer only works when a message listener is set up - it wont add to the buffer when true
	private Client client;
	private Client[] clients;
	private ServerSocket server;
	private Thread listener;
	private DLList<byte[]> buffer;
	private DLList<Thread> threads;
	private Receiver runner;
	private Connecter connecter;
	private String errorMessage; //errorMessage = any errors that are raised
	private int maxClientCount, clientCount; //number of clients that need to be connected for a full connection
	private Graph<Client> conns;
	private MessageListener ML; //event listener to be notified on every message we're sent
	
	
	/* TODO:
	 * comment the hell out of this
	 */
	
	public ConnectionHandler(String ipAddr, boolean isServer, int maxClientCount, String name){
		//isServer = false;
		try{
			if (ipAddr.equals("127.0.0.1") || ipAddr.equals("localhost")){
				ipAddr = InetAddress.getLocalHost().getHostAddress();
			}
			
			this.isServer = isServer;
			this.errorMessage = "";
			this.threads = new DLList<Thread>();
			this.clientCount = 1; //clients[0] aka us counts as a client
			
			if (name.length() > 28){ //cut the name down to proper size
				name = name.substring(0, 28);
			}
			
			this.conns = new Graph<Client>();
			
			if (isServer){
				try{
					server = new ServerSocket(2049);
					this.connecter = new Connecter();
				}
				catch (BindException e){
					//there is already a server being hosted on that port - really should never happen but idk
					e.printStackTrace();
					return;
				}
				buffer = new DLList<byte[]>();
				connecter = new Connecter();
				listener = new Thread(connecter);
				
				clients = new Client[maxClientCount+1];
				this.maxClientCount = maxClientCount;
				isConnected = false;
				
				byte[] ip = new byte[6];
				System.arraycopy(InetAddress.getLocalHost().getAddress(), 0, ip, 0, 4);
				System.arraycopy(ByteTranslater.toBytes((short)server.getLocalPort()), 0, ip, 4, 2);
				clients[0] = new Client(ip, name); //client 0 is us
				conns.add(clients[0]);
				
				notifyNetwork = true; //default value, when a new client joins this server, all local clients will be told, but servers connected to this one will only be told if this value is true
			}
			else{
				Socket temp = new Socket(ipAddr, 2049);
				buffer = new DLList<byte[]>();
				runner = new Receiver(temp);
				listener = new Thread(runner);
				client = new Client(temp, "Temp username", runner, listener, true);
				clients = new Client[2];
				
				byte[] ip = new byte[6];
				System.arraycopy(InetAddress.getLocalHost().getAddress(), 0, ip, 0, 4);
				System.arraycopy(ByteTranslater.toBytes((short)temp.getLocalPort()), 0, ip, 4, 2);
				clients[0] = new Client(ip, name); //clients 0 is us
				//System.out.println(temp.getLocalPort());
				clients[1] = client; //this is the server we connected to
				clients[1].isServer = true;
				
				//connect it into the graph of our known network
				conns.add(clients[0]);
				
				notifyNetwork = false; //don't cause an infinite feedback loop for clients telling the server that it joined
				
				//tell the server we connected to about ourselves
				//temp.getOutputStream().write(onConnection().send(temp.getInetAddress(), (byte)0, (byte)0, true, false, true, false, false)); //isComplete, isClosing, isNew, isPing, isEncrypted
			}
			//clients[0] = new Client(InetAddress.getLocalHost().getAddress(), name);
			clients[0].isServer = isServer;
			
		
			//server = null;
			listener.start();
			
		}
		catch (Exception e){
			e.printStackTrace();
			return;
		}
	}
	//returns true if there are any error messages to report
	public boolean hasError(){
		return !errorMessage.equals("");
	}
	//Returns the error message saved in memory and then wipes it from memory
	public String getError(){
		String send = errorMessage;
		errorMessage = "";
		return send;
	}
	
	//Receiving information functions / methods
	//Reads the next byte array while leaving it in the buffer
	public byte[] read(){
		return buffer.get(0);
	}
	//Reads and removes the next byte array from the buffer
	public byte[] pop(){
		return buffer.remove(0);
	}
	//Only removes the byte array from the buffer
	public void remove(){
		buffer.remove(0);
	}
	//Returns true if there are bytes in the buffer
	public boolean hasBuffer(){
		return buffer.size()>0;
	}
	//Exact same function as hasBuffer()
	public boolean hasReads(){
		return hasBuffer();
	}
	
	//Send information functions / methods
	//Base Functions:
	//Parses the array into the appropriate size
	public int send(byte[] array, String name){
		if (array.length > Packet.MAXDATASIZE){
			byte[] subArray;
			int pos=0;
			boolean res = true, temp = false;
			for (int i=0;i<10 && pos < array.length;i++){
				subArray = new byte[Math.min(Packet.MAXDATASIZE, array.length - pos)];
				System.arraycopy(array, pos, subArray, 0, subArray.length);
				pos += subArray.length;
				temp = send(subArray, name, subArray.length < Packet.MAXDATASIZE) == 0;
				if (!temp) res = false;
			}
			return res ? 0 : 1;
		}
		//Array is of proper size
		return send(array, name, true);
	}
	//Prepares the packet and client info for sending:
	public int send(byte[] array, String name, boolean isComplete){
		if (array.length > Packet.MAXDATASIZE){
			return send(array, name);
		}
		return send(new Packet(array), clientLookUp(name), isComplete);
	}
	//Sends the packet to the client
	private int send(Packet pack, Client client, boolean... boolData){
		if (client == null) return 2;
		if (client.client == null){
			//no direct connection to the client, find the next step to take and make the step
			byte[] ip = new byte[4];
			System.arraycopy(client.IP, 0, ip, 0, 4);
			short port = ByteTranslater.toShort(client.IP, 4);
			
			Client next = conns.shortestPath(clients[0], client).get(1); //get the next step to take, not us
			
			if (next == null) return 3; //no next step found
			if (next.client == null) return 4; //turns out our next step is not connected to us
			try{
				next.client.getOutputStream().write(pack.send(InetAddress.getByAddress(ip), port, (short)next.client.getLocalPort(), (byte)0, boolData));
				return 0;
			}
			catch (Exception e){
				e.printStackTrace();
			}
			
			return 5;
		}
		if (client.client.isClosed()) return 6; //our connection to the place it wants to end is closed, no messages can be sent
		
		try{
			client.client.getOutputStream().write(pack.send(client.client.getInetAddress(), client.client.getPort(), (short)client.client.getLocalPort(), (byte)0, boolData));
			return 0;
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return 7;
	}
	
	//Gets the byte array of this computer's ip address
	public byte[] getLocalAddress(){
		try{
			return InetAddress.getLocalHost().getAddress();
		}
		catch (IOException ex){
			ex.printStackTrace();
		}
		return new byte[4];
	}
	//gets the string representation of this computer's ip address
	public String getLocalAddressString(){
		try{
			return InetAddress.getLocalHost().getHostAddress();
		}
		catch (IOException ex){
			ex.printStackTrace();
		}
		return "0.0.0.0";
	}
	//Returns the name associated with this connectionHandler
	public String getLocalName(){
		return clients[0].username;
	}
	//Returns a String representation of all the clinets connected to this connection handler
	public String getConnections(){
		String send = "";
		for (Client client : clients){
			if (client == null) continue;
			send += client + "\n";
		}
		return send;
	}
	//Gets the names of all the clients connected to this node, effectively returns the names of the client list
	public String[] getClientNames(){
		String[] send = new String[clientCount+1];
		int ind = 0;
		for (Client cli : clients){
			if (cli == null) continue;
			send[ind++] = cli.username;
		}
		return send;
	}
	
	//Closes all connections this computer has through connectionHandler
	public void close(){
		if (!isServer){ //Client with only a server to notify
			if (client == null) throw new NullPointerException("Server connected to is null");
			if(!client.client.isClosed()) send(new Packet(0), client, true, true, false, false); //isComplete, isClosing, isNew, isPing
			client.die();
		}
		else{
			//This is an entire server to close
			for (Client client : clients){
				if (client == null) continue;
				send(new Packet(0), client, true, true); //isComplete, isClosing
				client.die();
			}
			
			for (Thread thread : threads){
				thread.interrupt();
				try{
					thread.join(5000);
				}
				catch(InterruptedException e){
					errorMessage = e.getMessage();
				}
			}
			try{
				server.close();
			}
			catch(IOException ex){
				ex.printStackTrace();
			}
		}
	}
	//Returns true if the connection handler's socket has closed
	public boolean isClosed(){
		if (!isServer) return client.client.isClosed();
		else return server.isClosed();
	}
	
	//On creating a new connection: - Override if you wish to change info sent at the beginning
	public Packet onConnection(){
		int clientCount =0;
		for (Client cli : clients){
			if (cli != null) clientCount++;
		}
		
		Packet send = new Packet((Math.max(clientCount, 1))*32 + 2);
		//Extra name length protection
		if (clients[0].username.length() > 26){
			clients[0].username = clients[0].username.substring(0, 26);
		}
		if (isServer){
			//Server either connecting into server network or adding a client
			send.add(ByteTranslater.toBytes(true, clientCount>1));
			
			send.add((byte)(clientCount-1));
			
			
			byte[] temp = new byte[32];
			for (int i=1;i<clients.length;i++){
				if (clients[i] == null) continue;
				System.arraycopy(clients[i].client.getInetAddress().getAddress(), 0, temp, 0, 4);
				System.arraycopy(ByteTranslater.toBytes((short)clients[i].client.getPort()), 0, temp, 4, 2);
				System.arraycopy(ByteTranslater.toSmallBytes(clients[i].username.toCharArray()), 0, temp, 6, Math.min(26, clients[i].username.length()));
				send.add(temp);
			}
			
			//add the name at the end of all the other connections 
			send.add(ByteTranslater.toSmallBytes(clients[0].username.toCharArray()));
			send.add(new byte[26-clients[0].username.length()]); //make sure the packet will insert the next pack of bytes at the appropriate place
		}
		else{
			//Client connecting into the network via server
			
			//data[0] is bool data, both false, byte = 0
			send.add(ByteTranslater.toBytes(false, false));
			
			//data[1] is number of other conntions, client, 0 - Client only learns of new connection through server which already knows
			send.add((byte)0);
			
			//add the name at the end of all the other connections 
			//send.add(clients[i].client.getInetAddress().getAddress());
			send.add(ByteTranslater.toSmallBytes(clients[0].username.toCharArray()));
			
		}
		return send;
	}
	
	//Turn a client name into a client object with thread, receiver and IP address associated with it
	public Client clientLookUp(String name){
		if (clients == null) return null;
		for (Client client : clients){
			if (client != null ? client.equals(name) : false) return client;
		}
		Client cli = conns.slowSearchComplete(new Client(new byte[6], name));
		return cli;
		/*if (cli != null){
			//return conns.shortestPath(clients[0], cli).get(1);
		}
		return null;*/
	}
	//Turn a client ipAddress into a client object with socket, thread, and receiver
	public Client clientLookUp(byte[] ip){
		if (clients == null) return null;
		for (Client client : clients){
			if (client != null ? client.equals(ip) : false) return client;
		}
		if (conns.contains(new Client(ip))){
			//try catch with null reference exception?
			//return conns.shortestPath(clients[0], new Client(ip)).get(1);
			return conns.complete(new Client(ip));
		}
		return null;
	}
	
	//Sets the current message listener to be notified whenever a message is recieved by this handler - returns true if this message listener wasn't yet initalized
	public boolean setMessageListener(MessageListener ml, boolean voidBuffer){
		//keep in mind, the internal buffer for all messages recieved will still accumulate 
		boolean send = this.ML == null; 
		this.ML = ml;
		this.voidBuffer = voidBuffer;
		return send;
	}
	
	//Sets our policy on notifing the entire network of a new client joining this server
	public void setNotifyNetworkPolicy(boolean notifyNetwork){
		this.notifyNetwork = notifyNetwork;
	}
	public boolean getNotifyNetworkPolicy(){
		return notifyNetwork;
	} 
	
	//Returns true if both byte arrays contain the same values
	public static boolean byteEquals(byte[] arr1, byte[] arr2){
		if (arr1.length != arr2.length) return false;
		for (int i=0;i<arr1.length;i++){
			if (arr1[i] != arr2[i]) return false;
		}
		return true;
	}
	
	//returns the average delay being experieneced from the first client connecting to this
	public long getAvgDelay(){
		return clients[1].receiver.getAvgPing();
	}
	
	//returns if this connection handler is a server / router
	public boolean isServer(){
		return this.isServer;
	}
	
	//Debug method to figure out what information is exchanged
	public String getConnString(){
		return conns.toString();
	}
	public void print(byte[] array){
		String send = "[";
		for (byte num : array){
			send += num + ", ";
		}
		send += "\b\b]";
		System.out.println(send);
	}
	protected DLList<Client> getPathTo(String name){
		Client complete = clientLookUp(name);
		//System.out.println("\nDebug complete client from path viewer: " + complete + "\n\n");
		if (complete == null) return null;
		return conns.shortestPath(clients[0], complete);
	}
	
	//classes that aid in the use of connection handler's operation
	protected class Client{
		Socket client;
		String username;
		Receiver receiver;
		Thread thread;
		boolean isServer;
		byte[] IP; //this should have 6 values, first 4 for the ip, last 2 for the 2 bytes representing the short value of the port
		public Client(byte[] ip){
			this.IP = ip;
			checkIP();
		}
		public Client(byte[] ip, String name){
			this.IP = ip;
			this.username = name;
			checkIP();
		}
		public Client(Socket socket, String username, Receiver receiver, Thread thread, boolean isServer){
			this.client = socket;
			this.username = username;
			this.receiver = receiver;
			this.thread = thread;
			this.isServer = isServer;
			
			this.IP = new byte[6];
			System.arraycopy(socket.getInetAddress().getAddress(), 0, this.IP, 0, 4);
			System.arraycopy(ByteTranslater.toBytes((short)socket.getPort()), 0, this.IP, 4, 2);
		}
		public void die(){
			try{
				if (client != null) client.close();
			}
			catch (IOException ex){
				ex.printStackTrace();
			}
			if (thread != null) thread.interrupt();
			if (receiver != null) receiver.end();
		}
		public boolean equals(Object other){
			if (other instanceof Client){
				Client c = (Client)other;
				if (byteEquals(c.IP, new byte[] {0,0,0,0,0,0})){
					//using general equals for name lookup
					//System.out.println("Client equals check: " + c.username + "=?=" + this.username + "=>" + c.username.equals(this.username) + ", " + c.username.length() + ", " + this.username.length());
					return c.username.equals(this.username);
				}
				return byteEquals(c.IP, this.IP);
			}
			if (other instanceof byte[]){
				return byteEquals((byte[])other, IP);
			}
			if (other instanceof String){
				return this.username.equals((String)other);
			}
			return false;
		}
		public int hashCode(){
			return ByteTranslater.toShort(ByteTranslater.specificArray(IP, 3, 5));
		}
		public String toString(){
			return (client != null ? client.toString() + ", " : "") + (username!=null?username:"nullName") + ", " + receiver + ", " + (thread != null ? thread.getName() +", " : "" )+ "server?" + isServer + ", IP:" + ByteTranslater.toInt(IP) + " Port:" + ByteTranslater.toShort(IP,4);
		}
		private void checkIP(){
			if (IP.length == 6) return;
			byte[] temp = new byte[6];
			System.arraycopy(IP, 0, temp, 0, Math.min(IP.length, temp.length));
			if (client != null){
				System.arraycopy(ByteTranslater.toBytes((short)client.getPort()), 0, temp, 4, 2);
			}
			this.IP = temp;
		}
	}
	protected class Connecter implements Runnable{
		boolean running = true;
		public void run(){
			while (running){
				try{
					Thread.sleep(5);
				}
				catch(InterruptedException ex){
					Thread.currentThread().interrupt();
					running=false;
					return;
				}
				//if (maxClientCount <= clientCount) continue;
				try{
					Socket listen = server.accept(); //accept the incomming socket connection
					Receiver rec = new Receiver(listen); //create a new receiver based on the socket - listens for all packets being sent
					
					int index = findOpenIndex(); //puts the client into the list at some arbitrary index
					
					if (index == -1){ //no open spaces
						listen.getOutputStream().write((new Packet(100, new Packet(0))).send(listen.getInetAddress(), listen.getPort(), listen.getLocalPort(), (byte)0, true));
						continue;
					}
					Thread temp = new Thread(rec, "Server reciever - " + index);
					//send the message of existance of this network
					listen.getOutputStream().write(onConnection().send(listen.getInetAddress(), listen.getPort(), listen.getLocalPort(), (byte)0, true, false, true, false, false)); //isComplete, isClosing, isNew, isPing, isEncrypted
					//then save its data for ease of access, otherwise we'll be telling it about its own existance
					clients[index] = new Client(listen, "Temp username", rec, temp, false);
					
					conns.add(clients[index]); //add it to the graph
					conns.addEdge(clients[0], clients[index], 1); //connect the two on the network graph
					
					//System.out.println("Sending onConnection packet");
					
					temp.start();
					
					threads.add(temp);
				}
				catch (SocketException e){
					close();
					running = false;
					return;
				}
				catch (IOException e) {
					e.printStackTrace();
					System.out.println("Exception caught when listening for a connection in the ServerConnecter");
					System.out.println(e.getMessage());
					running = false;
				}
			}
		}
		protected int findOpenIndex(){
			if (clients == null) return -1;
			for (int i=0;i<clients.length;i++){
				if (clients[i] == null) return i;
			}
			return -1;
		}
	}
	protected class Receiver implements Runnable{
		boolean running = true;
		DLList<Packet> packetBuffer;
		ShiftRegister<Long> millisDiff;
		Socket listen;
		public Receiver(Socket socket){
			packetBuffer = new DLList<Packet>();
			millisDiff = new ShiftRegister<Long>(25); //stores the millisecond difference found in the packets
			this.listen = socket;
		}
		public void run(){
			Packet temp = null;
			while (running){
				try{
					Thread.sleep(5);
				}
				catch(InterruptedException ex){
					Thread.currentThread().interrupt();
					running=false;
					return;
				}
				try{
					temp = Packet.read(listen.getInputStream());
					if (temp != null ? !temp.getAlive() : true){
						end();
						return;
					}
					
					//if the destination address is not us
					if (!byteEquals(temp.getReceiverIP(), clients[0].IP)){
						//The packet is not meant to stay here, we need to find its next stop
						DLList<Client> tempList = conns.shortestPath(clients[0], conns.complete(new Client(temp.getReceiverIP())));
						
						if (tempList == null ? true : tempList.size() == 0){
							send(new Packet(404, temp), clientLookUp(temp.getSenderIP()), true); //isComplete
							continue;
						}
						Client next = tempList.get(1);
						if (next == null){
							send(new Packet(404, temp), clientLookUp(temp.getSenderIP()), true); //isComplete
							continue;
						}
						//Send the packet on its path
						next.client.getOutputStream().write(temp.sendComplete());
						continue;
					}
					//If a new connection is created
					if(temp.getBoolData()[2]){
						//Find the client object recently created:
						Client cli = null;
						for (int i=1;i<clients.length;i++){
							if (clients[i].equals(temp.getSenderIP())){
								cli = clients[i];
								break;
							}
						}
						
						char[] nameChars = new char[26];
						
						if (cli == null){
							if (isServer) throw new NullPointerException("Server issue found, newly connected client doesn't actually exist");
							//If we're being notified of a new connection on the network, add it solely to the graph of our known network
							for (int n=0;n<26;n++){ //grab the name
								if (temp.getData()[2+n] == 0) break;
								nameChars[n] = (char)temp.getData()[2+n];
							}
							cli = new Client(temp.getSenderIP(), (new String(nameChars)).trim());
							conns.add(cli); //add the new one to the graph
							conns.addEdge(clients[1], cli, 1); //its guarenteed connected to the server, so show their connection
							continue;
						}
						
						
						if (!cli.username.equals("Temp username")) continue; //Already saw this new user, can safely ignore it
						
						byte[] data = temp.getData();
						boolean[] bools = ByteTranslater.toBools(data[0]);
						cli.isServer = bools[0];
						conns.add(cli);
						
						
						if (bools[1]){
							//Add its neighbors to our graph
							int count = data[1] & 0xff;
							for (int i=0;i<count;i++){
								byte[] tempIP = new byte[6];
								
								System.arraycopy(data, 2 + i*32, tempIP, 0, 6); //grab the ip
								
								for (int n=6;n<32;n++){ //grab the name
									if (data[2+i*32+n] == 0) break;
									nameChars[n-6] = (char)data[2+i*32+n];
								}
								//find the complete version if it exists
								Client tempClient = new Client(tempIP, (new String(nameChars)).trim());
								
								nameChars = new char[nameChars.length]; //void the previous name after our use
								
								if (conns.contains(tempClient))
									tempClient = conns.complete(tempClient);
								else conns.add(tempClient);
								
								//connect the two on the graph
								conns.addEdge(cli, tempClient, 1);
							}
							
							//then add the name of the new thread
							for (int n=0;n<26;n++){//Grabing the name
								if (data[2+count*32+n] == 0) break;
								nameChars[n] = (char)data[2+count*32+n];
							}
						}
						else{
							//only add the name of the new thread
							for (int n=0;n<26;n++){ //grab the name
								if (data[2+n] == 0) break;
								nameChars[n] = (char)data[2+n];
							}
							
						}
						
						conns.addEdge(cli, clients[0], 1);
						
						//set the name to the one given
						cli.username = (new String(nameChars)).trim();
						
						//tell it about our existance
						boolean sent = send(onConnection(), cli, true, false, true, false, false) == 0; //isComplete, notClosing, isNew, notPing, notEncrypted
						
						//tell our neighbors about its existance - pass its message along to our neighbors
						for (int i=1;i<clients.length;i++){
							if (clients[i] == null) continue; //this space hasn't been filled yet
							if (clients[i] == cli) continue; //it already knows of its own existance
							if (clients[i].isServer && !notifyNetwork) continue; //we're not going to notify the entire network of a new client, only the other clients nearby
							temp.setRecieverHeader(clients[i].client.getInetAddress(), ByteTranslater.toShort(clients[i].IP,4)); //set the reciever of the packet to each individual client
							clients[i].client.getOutputStream().write(temp.sendComplete()); //send the packet on its way
						}
						millisDiff.add(temp.getDelayMillis());
					}
					else{
						packetBuffer.add(temp);
						millisDiff.add(packetBuffer.get(packetBuffer.size()-1).getDelayMillis());
						if (packetBuffer.get(packetBuffer.size()-1).getBoolData()[0]){
							flushBuffer();
						}
					}
				}
				catch (IOException e){
					errorMessage = e.toString();
				}
			}
		}
		//To be called after a complete packet train has gone through, possibly containing multiple packets
		public void flushBuffer(){
			//Combines all the byte data into one GIANT array and clears the packet buffer
			byte[][] arrays = new byte[packetBuffer.size()][(int)Packet.maxSize()];
			int i=0;
			for (Packet nums : packetBuffer){
				arrays[i++] = nums.getData();
			}
			buffer.add(Packet.combine(arrays));
			if (ML != null){ //if we have a message listener
				ML.onMessage(buffer.get(0));
				if (voidBuffer) buffer.remove(0); //removes the message we just recieved, while leaving any other messages from before still hanging around
			}
			packetBuffer.clear();
		}
		public void end(){
			try{
				listen.close();
			}
			catch(IOException e){
				e.printStackTrace();
			}
			running = false;
		}
		public long getAvgPing(){
			long send = 0L;
			for (int i=0;i<millisDiff.size();i++){
				send += millisDiff.get(i);
			}
			return send / millisDiff.capacity();
		}
	}
}