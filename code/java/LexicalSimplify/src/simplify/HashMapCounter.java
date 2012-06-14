package simplify;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HashMapCounter<K>{  // implements Map<K, Integer>{
	private HashMap<K, ChangeableInteger> map = new HashMap<K, ChangeableInteger>();
	
	public void clear() {
		map.clear();
	}

	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}
	
	public ArrayList<Map.Entry<K, Integer>> sortedEntrySet(){
		ArrayList<Map.Entry<K, Integer>> list = new ArrayList<Map.Entry<K, Integer>>();
		
		for( Map.Entry<K, ChangeableInteger> e: map.entrySet()){
			list.add(new AbstractMap.SimpleEntry<K, Integer>(e.getKey(), e.getValue().getInt()));
		}
		
		Collections.sort(list, new Comparator<Map.Entry<K, Integer>>(){
			public int compare(Map.Entry<K, Integer> e1, Map.Entry<K, Integer> e2){
				return -e1.getValue().compareTo(e2.getValue());
			}
		});
		
		return list;
	}

	public int get(Object key) {
		if( !map.containsKey(key) ){
			return 0;
		}else{
			return map.get(key).getInt();
		}
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public Set<K> keySet() {
		return map.keySet();
	}

	public void put(K key, int value) {
		map.put(key, new ChangeableInteger(value));
	}
	
	public void increment(K key){
		increment(key, 1);
	}
	
	public void increment(K key, int value){
		if( map.containsKey(key) ){
			map.get(key).increment(value);
		}else{
			map.put(key, new ChangeableInteger(value));
		}
	}

	public int remove(Object key) {
		return map.remove(key).getInt();
	}

	public int size() {
		return map.size();
	}
	
	private class ChangeableInteger{
		private int num;
		
		public ChangeableInteger(){
			num = 0;
		}
		
		public ChangeableInteger(int num){
			this.num = num;
		}
		
		public void increment( int val ){
			num += val;
		}
		
		public int getInt(){
			return num;
		}
		
		public void setInt(int num){
			this.num = num;
		}
		
		public int compareTo(ChangeableInteger o){
			if( num < o.num ){
				return -1;
			}else if( num > o.num ){
				return 1;
			}else{
				return 0;
			}
		}
		
		public boolean equals(Object o){
			return num == ((ChangeableInteger)o).num;
		}
	}
}
