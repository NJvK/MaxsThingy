package helpful.datastructures;
public class Trie{
	private TrieNode root;
	private int wordCount; //for funsies
	private static final boolean excludeSpace = false; //for your use to change in code
	public Trie(String[] inputs){
		this();
		add(inputs);
	}
	public Trie(){
		root = new TrieNode(false);
	}
	public void add(String[] inputs){ //add an array of strings
		for (String item : inputs){
			boolean res = add(item);
			wordCount += res ? 1:0; //adding was successful, add to our word count (will count duplicates)
		}
	}
	public int getWordCount(){
		return this.wordCount;
	}
	public boolean add(String item){
		if (item == null) return false;
		char[] chars = item.toLowerCase().toCharArray(); //makes sure there aren't any weird edge cases with speical characters
		//can remove if wanted from here 
		for (char c : chars) {
			if(!Character.isLetter(c) && (c!=' ' || excludeSpace)) {
				return false;
			}
		} //to this line, all of it can go if you trust your source
		return add(chars, root);
	}
	private boolean add(char[] remaining, TrieNode current){ //private recursive method
		if (remaining == null) return false; //remaining is wrong - one type of exit condition
		if (current == null) return false; //the current node is wrong - null protection
		
		if (remaining.length == 0){ //this is the last item to add 
			current.setWord(true); //this sequence is a word
			return true; //no more letters to add
		}
		
		if (!current.hasNext(remaining[0])){ //first time adding this char to this node
			current.add(remaining[0]); //add the trie node
		}
		
		current = current.next(remaining[0]); //update where we are supposed to be
		
		char[] next = new char[remaining.length-1]; //set up the array for the next recursion step
		System.arraycopy(remaining, 1, next, 0, next.length); //copys the remaining characters bar the first item into the array going deeper into recursion
		return add(next, current); //recursion
	}
	private TrieNode get(String item){
		return get(item.toLowerCase().toCharArray(), root);
	}
	private TrieNode get(char[] remaining, TrieNode current){
		if (remaining == null) return null; //remaining is wrong - one type of exit condition
		if (current == null) return null; //the current node is wrong - null protection
		
		if (remaining.length == 1){ //this is the last item
			return current.next(remaining[0]); //send the connection with that last item back
		}
		//go to the next node
		current = current.next(remaining[0]); //update where we are supposed to be
		char[] next = new char[remaining.length-1]; //set up the array for the next recursion step
		System.arraycopy(remaining, 1, next, 0, next.length); //copys the remaining characters bar the first item into the array going deeper into recursion
		return get(next, current); //recursion
	}
	public boolean wordExists(String item){
		TrieNode temp = get(item);
		if (temp == null) return false; //doesn't exist
		return temp.isWord();
	}
	public boolean branchesExist(String item){
		TrieNode temp = get(item);
		if (temp == null) return false; //doesn't exist
		return temp.hasAConnection();
	}
	public boolean[] existance(String item){
		TrieNode temp = get(item);
		if (temp == null) return new boolean[2]; //auto fills with two falses
		return new boolean[]{temp.isWord(), temp.hasAConnection()};
	}
	private class TrieNode{ //the nodes traversed by the Trie
		private TrieNode[] connections; //connections only go one way
		private boolean isWord;
		
		public TrieNode(boolean isWord){
			this.isWord = isWord;
			//the entire alphabet and space if included
			connections = new TrieNode[26 + (excludeSpace ? 0:1)]; // ? is boolean operator, returns the following "ifTrue : isFalse"
		}
		public void add(char connection){
			if (connection == ' ' && !excludeSpace){
				connections[26] = new TrieNode(false);
				return;
			}
			//can't connect to existsing TrieNode, will always create a new one
			connections[(int)(connection)-97] = new TrieNode(false); //char - 'a'
		}
		public void setWord(boolean isWord){
			this.isWord = isWord;
		}
		public boolean hasNext(char item){
			if (item == ' ' && !excludeSpace) return connections[26]!=null;
			return connections[(int)(item)-97] != null;
		}
		public TrieNode next(char item){ //the TrieNode connected by this character
			if (item == ' ' && !excludeSpace) return connections[26];
			return connections[(int)(item)-97];
		}
		public boolean isWord(){
			return this.isWord;
		}
		public String toString(){
			String send = "";
			for (int i=0;i<26;i++){
				send+=((char)(i+97) + ":" + (connections[i]!=null) + ", ");
			}
			if (!excludeSpace) send += " :"+ (connections[26]!=null);
			return send;
		}
		public boolean hasAConnection(){
			for (TrieNode node : connections){
				if (node != null) return true; //found a connection
			}
			return false; //all connections are false;
		}
	}
}