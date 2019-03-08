package main;

import java.util.ArrayList;
import java.util.List;

public class Device {
	private List<Double> pb = new ArrayList<>();
	private List<Double> lc = new ArrayList<>();
	private List<Double> cc = new ArrayList<>();
	private List<Double> com = new ArrayList<>();
	
	public Device(List<Double> pb, List<Double> lc, List<Double> cc, List<Double> com) {
		this.pb =pb;
		this.lc = lc;
		this.cc =cc;
		this.com = com;
	}
	
	public double compute(List<Integer> cp) {
		List<Integer> cp_layer = cp;
		List<Double> remain_ls = new ArrayList<>();
		double cost = 0;
		double ans = 0;
		
		// compute expectation value probability
		for(int i=1; i<cp_layer.size(); i++) {
			double remain = 1;
			for(int j=0; j<i; j++) remain = remain * (1-pb.get(cp_layer.get(j)));
			remain = pb.get(cp_layer.get(i)) * remain;
			remain_ls.add(remain);
		}
		
		// compute expectation value
		for(int i=1; i<cp_layer.size(); i++) {
			cost = 0;
			for(int j=0; j<=cp_layer.get(i); j++) cost = cost + lc.get(j);
			for(int j=0; j<=i; j++) cost = cost + cc.get(cp_layer.get(j));
			ans = ans + remain_ls.get(i-1)*(cost/com.get(1));
		}
		
		return ans;
		
//		System.out.println("Device  ==>" + ans);
	}
	
	public double init_compute() {
		double cost =0;
		// compute time
		for(int i=0; i<lc.size(); i++) cost = cost + lc.get(i);
		cost = cost + cc.get(cc.size()-1);
		cost = cost / com.get(1);
		
		return cost;

//		System.out.println("Init Device  ==>" + cost);
	}
}
