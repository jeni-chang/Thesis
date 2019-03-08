package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Table {
	private int L; // layer
	private int S; // server
	private int C; // check point
	private boolean check;
	private boolean capacity_check;
	private double ans;
	private double heu_ans;
	private double heu_ans_2;
	private Map<Double, List<Double>> pb; // <probability, [ans, cost, remain data]>
	private Map<Double, List<Double>> heu_pb; // <probability, [ans, cost, remain data]>
	private Map<Double, List<Double>> heu_pb_2; // <probability, [ans, cost, remain data]>
	private List<List<Double>> ans_tmp; // { [E, cost, remain data, base on which ID, base ID remain data], [], ...} 
	private List<List<Double>> heu_ans_tmp; // { [E, cost, remain data], [], ...} 
	private List<List<Double>> heu_ans_tmp_2; // { [E, cost, remain data], [], ...} 
	private int id;
	private static int cnt = 0;
	
	public Table() {
		this.id = ++cnt;
		this.check = false;
		this.capacity_check = true;
		this.pb = new HashMap<>();
		this.heu_pb = new HashMap<>();
		this.heu_pb_2 = new HashMap<>();
		this.ans_tmp = new ArrayList<>();
		this.heu_ans_tmp = new ArrayList<>();
		this.heu_ans_tmp_2 = new ArrayList<>();
		this.ans = Double.MAX_VALUE;
		this.heu_ans = Double.MAX_VALUE;
		this.heu_ans_2 = Double.MAX_VALUE;
	}
	
	public void setL(int layer) {
		this.L = layer;
	}
	
	public void setS(int server) {
		this.S = server;
	}
	
	public void setC(int cp) {
		this.C = cp;
	}
	
	public void setPb(double pb, List<Double> cost, int version) {
		if(version == 0) this.pb.put(pb, cost);
		else if(version == 1) this.heu_pb.put(pb, cost);
		else if(version == 2) this.heu_pb_2.put(pb, cost);
		
	}
	
	public void setAns(double ans, int version) {
		if(version == 0) this.ans = ans;
		else if(version == 1) this.heu_ans = ans;
		else if(version == 2) this.heu_ans_2 = ans;
		
	}
	
	public void setcheck(boolean b) {
		this.check = b;
	}
	
	// set capacity check
	public void set_capa_check(boolean b) { 
		this.capacity_check = b;
	}
	
	public void set_ans_tmp(List<Double> ls, int version) {
		if(version == 0) this.ans_tmp.add(ls);
		else if(version == 1) this.heu_ans_tmp.add(ls);
		else if(version == 2) this.heu_ans_tmp_2.add(ls);
	}
	
	public int getID() {
		return this.id;
	}
	
	public int getL() {
		return this.L;
	}
	
	public int getS() {
		return this.S;
	}
	
	public int getC() {
		return this.C;
	}
	
	public double getAns(int version) {
		if(version == 0) return this.ans;
		else if(version == 1) return this.heu_ans;
		else return this.heu_ans_2;
	}
	
	public Map<Double, List<Double>> getPb(int version) {
		if(version == 0) return this.pb;
		else if(version == 1) return this.heu_pb;
		else return this.heu_pb_2;
	}
	
	
	public boolean getcheck() {
		return this.check;
	}
	public boolean get_capa_check() {
		return this.capacity_check;
	}
	
	public List<List<Double>> get_ans_tmp(int version){
		if(version == 0) return this.ans_tmp;
		else if(version == 1) return this.heu_ans_tmp;
		else  return this.heu_ans_tmp_2;
		
	}
	
	@Override
	public String toString() {
		return String.format("R(%d, %d, %d)", L, S, C);
	}
	
}
