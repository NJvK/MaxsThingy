import java.util.Iterator;

public class ShiftRegister<E> implements Iterable<E>{
	private Object[] list;
	private int pointer, size;
	public ShiftRegister(int capacity){
		list = new Object[capacity];
		pointer = 0;
		size = 0;
	}
	public ShiftRegister(){
		this(10);
	}
	public void add(E[] items){
		for (E item : items){
			add(item);
		}
	}
	public void add(E item){
		list[pointer] = item;
		pointer = wrap(pointer+1, list.length, 0);
		size = Math.min(size+1, list.length);
		
	}
	@SuppressWarnings("unchecked")
	public E pop(){
		pointer = wrap(pointer-1, list.length, 0);
		E send = (E)list[pointer];
		list[pointer] = null;
		if (send != null) size--;
		return send;
	}
	public E remove(){
		return pop();
	}
	@SuppressWarnings("unchecked")
	public E removeLast(){
		int counter = list.length, cursor = pointer;
		do{
			cursor = wrap(cursor-1, list.length, 0);
			counter--;
		}while(counter > 0 && list[wrap(cursor-1, list.length, 0)] != null);
		//cursor points to the last item in the list
		E send = (E)list[cursor];
		list[cursor] = null;
		if (send != null) size--;
		return send;
	}
	@SuppressWarnings("unchecked")
	public E get(int index){
		return (E)list[wrap(pointer-index-1, list.length, 0)];
	}
	public int size(){
		return size;
	}
	public int capacity(){
		return list.length;
	}
	public String toString(){
		String send = "";
		for (Object item : this){
			send += (item == null ? "X" : item.toString()) + ", ";
		}
		return send;
	}
	public String fullString(){
		String send = "";
		for (int i=0;i<list.length;i++){
			send += (list[i]==null ? "X" : list[i].toString()) + ", ";
		}
		return send;
	}
	public int getTop(){
		return pointer;
	}
	public void clear(){
		for (int i=0;i<list.length;i++){
			list[i] = null;
		}
		size = 0;
		pointer = 0;
	}
	public static int wrap(int num, int top, int bottom){ //wraps a number num between top-1 and bottom inclusive
		if (top == bottom) return num; //stack over flow protection and div by 0 protection
		//includes bottom, excludes top
		if (num < top && num >= bottom){
			return num;
		}
		if (num < bottom){
			return wrap(num+top-bottom, top, bottom);
		}
		if (num >= top){
			return ((num-top)%(top-bottom))+bottom;
		}
		return num;
	}
	public Iterator<E> iterator(){
		return new ShiftRegisterIterator<E>();
	}
	private class ShiftRegisterIterator<E> implements Iterator<E>{
		private int cursor = pointer, counter = list.length;
		public ShiftRegisterIterator(){}
		@SuppressWarnings("unchecked")
		public E next(){
			cursor = wrap(cursor-1, list.length, 0);
			counter--;
			return (E)list[cursor];
		}
		public boolean hasNext(){
			return list[wrap(cursor-1, list.length, 0)] != null && counter > 0;
		}
	}
}