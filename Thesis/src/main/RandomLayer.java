package main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RandomLayer {
	private int layer;
	private List<Double> pb = new ArrayList<>();
	private List<Double> lc = new ArrayList<>();
	private List<Double> cc = new ArrayList<>();
	private List<Double> bw = new ArrayList<>();
	private List<Double> com = new ArrayList<>();
	private List<Double> r = new ArrayList<>();
	private double f; 
	
	public RandomLayer(int layer, List<Double> r, List<Double> pb, List<Double> lc, List<Double> cc, List<Double> bw, List<Double> com, Double f) {
		this.layer = layer;
		this.r = r;
		this.pb =pb;
		this.lc = lc;
		this.cc =cc;
		this.bw = bw;
		this.com = com;
		this.f = f;
	}
	
	public double compute(int cp_num, int server, List<Integer> cp_layer) {
		double ans = 0.0;
		List<Integer> random_tmp = new ArrayList<>();
		List<Double> remain_ls = new ArrayList<>();
//		List<Integer> cp_layer = new ArrayList<>();
		List<Integer> cp_server = new ArrayList<>();
		
//		// generate check point layer
//		for(int i=2; i<layer; i++) random_tmp.add(i);
//		Collections.shuffle(random_tmp);
//		for(int i=1; i<cp_num; i++) cp_layer.add(random_tmp.get(i-1));
//		cp_layer.add(layer);
//		Collections.sort(cp_layer);
		
		// generate random server to compute
//		random_tmp.clear();
		for(int i=1; i<=server; i++) random_tmp.add(i);
		Collections.shuffle(random_tmp);
		for(int i=1; i<=cp_layer.size(); i++) cp_server.add(random_tmp.get(i-1));
		Collections.sort(cp_server);
//		System.out.println(cp_server);

		// compute expectation value probability
		double remain = 1;
		for(int i=1; i<=cp_layer.size(); i++) {			
			remain_ls.add(remain*pb.get(cp_layer.get(i-1)));
			remain *=(1- pb.get(cp_layer.get(i-1)));
		}
		
//		System.out.println("Random layer server ==> " + cp_server);
//		System.out.println("Random layer layer ==> " + cp_layer);
//		System.out.println("Random layer remain data ==> " + remain_ls);
		
		List<Double> tcost = new ArrayList<>();
		List<Double> ccost = new ArrayList<>();
		for(int i=1; i<=cp_server.size(); i++) {
			double ttime = 0.0;
			double ctime = 0.0;
			double ratio = 1.0;
			double tmp = 0.0;
			// data size change ratio
			if(i == 1) ratio = 1.0;
			else 
				for(int j=1; j<=cp_layer.get(i-2); j++) ratio = ratio * this.r.get(j);
			// transmission delay
			if(i == 1) 
				for(int j=1; j<cp_server.get(i-1); j++) ttime = ttime + f*ratio/this.bw.get(j);
			else 
				for(int j=cp_server.get(i-2); j<cp_server.get(i-1); j++) ttime = ttime + f*ratio/this.bw.get(j);
			
			tcost.add(ttime);
			
			// compute delay
			if(i == 1)
				for(int j=1; j<=cp_layer.get(i-1); j++) ctime = ctime + lc.get(j);
			else
				for(int j=cp_layer.get(i-2)+1; j<=cp_layer.get(i-1); j++) ctime = ctime + lc.get(j);
			
			ctime = ctime + cc.get(cp_layer.get(i-1));
			ctime = ctime / com.get(cp_server.get(i-1));
			
			ccost.add(ctime);
			
			for(double d: tcost) tmp = tmp + d;
			for(double d : ccost) tmp = tmp + d;
			
			ans = ans + tmp*remain_ls.get(i-1);
		}
//		System.out.println(ccost);
//		System.out.println("Random layer server ==> " + com);
//		System.out.println("Random choose layer ans ==> " + ans);
		
		return ans;
	}
}
