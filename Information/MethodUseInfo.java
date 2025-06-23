package oo.com.iseu.Information;

import java.io.Serializable;
import java.util.HashSet;

public class MethodUseInfo implements Serializable{
	private HashSet<Integer> useIndics;

	public MethodUseInfo() {
		super();
		this.useIndics = new HashSet<>();
	}

	public HashSet<Integer> getUseIndics() {
		return useIndics;
	}

	public void setUseIndics(HashSet<Integer> useIndics) {
		this.useIndics = useIndics;
	}
	@Override
	public String toString() {
		return useIndics.toString()+"\n";
	}
}
