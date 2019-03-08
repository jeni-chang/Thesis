package main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import main.PbCombin;

public class Main {
	
	static Table opt = null;
	static Table heu_opt = null;
	static Table heu_opt_2 = null;
	
	static int layer = 22;
	static int server = 4;
	static boolean compute_opt = false;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		List<Table> table = new ArrayList<>();
		
		double choose = 5;
		double pipeline_threshold = 0.5; // sec
		
		
		List<Double> pb = new ArrayList<>();
		List<Double> heu_pb_1 = new ArrayList<>();
		List<Double> heu_pb_2 = new ArrayList<>();
		List<Double> lc = new ArrayList<>();
		List<Double> cc = new ArrayList<>();
		List<Double> r = new ArrayList<>();
		List<Double> bw = new ArrayList<>();
		List<Double> com = new ArrayList<>();
		List<Double> ls = new ArrayList<>();
		List<Double> cs = new ArrayList<>();
		List<Double> sp = new ArrayList<>();
		
		double width = 2240;
		double height = 2240;
		double f = width*height*3*24; // width * height * RGB * bits 
		f = f / 1000000; // convert to Mbits
		
		VGG16 vgg = new VGG16(width, height, 3, 3);
		vgg.data_compute();
		lc = vgg.get_FLOPS(); // FLOPS
		cc = vgg.get_cp_FLOPS(); // FLOPS
		r = vgg.get_ratio();
		ls = vgg.get_params(); // bits
		cs = vgg.get_cp_params(); //bits
		
		pb.add(0.0);
		pb.add(0.0); // input layer
		pb.add(0.4196);
		pb.add(0.4006);
		pb.add(0.3787);
		pb.add(0.3545);
		pb.add(0.3288);
		pb.add(0.3024);
		pb.add(0.276);
		pb.add(0.2504);
		pb.add(0.2262);
		pb.add(0.2043);
		pb.add(0.1853);
		pb.add(0.17);
		pb.add(0.159);
		pb.add(0.159);
		pb.add(0.1473);
		pb.add(0.1452);
		pb.add(0.1445);
		pb.add(0.1446);
		pb.add(0.1447);
		pb.add(0.1452);
		pb.add(1.0);
		
		bw.add(0.0);
		bw.add(100000.0);
		bw.add(500000.0); // Mbits
		bw.add(500000.0);
		bw.add(Double.MAX_VALUE);
		
		com.add(0.0);
		com.add(5.0); // GFLOPS
		com.add(100.0);
		com.add(100.0);
		com.add(150.0);
		
		sp.add(0.0);
		sp.add(500.0); // Mbits
		sp.add(5000000.0);
		sp.add(5000000.0);
		sp.add(5000000.0);
		
		/* create table */
		for (int c=0; c<=server; c++) {  // layer more than server
			for (int s=1; s<=server; s++) {
				for(int l=0; l<=layer; l++) {
					Table t = new Table();
					t.setL(l);
					t.setS(s);
					t.setC(c);
					table.add(t);
				}
			}
		}
		
		/*---------------------------------*/
		/* randomly choose heuristic probability */
		heu_pb_1.add(0.0);
		List<Integer> tmp = new ArrayList<>();
		for(int i=1; i<layer; i++) tmp.add(i);
		Collections.shuffle(tmp);
		for(int i=1; i<=choose-1; i++) heu_pb_1.add(pb.get(tmp.get(i)));
		heu_pb_1.add(1.0);
//		System.out.println("heu_pb_1 ==> " + heu_pb_1);

		/* average choose heuristic probability */
		List<Double> heu_tmp = new ArrayList<>();
		for(double d: pb) heu_tmp.add(d);
		Collections.sort(heu_tmp);
		int ceiling = (int) Math.ceil(layer/choose);
		heu_pb_2.add(0.0);
		for(int i=1; i<=choose-1; i++) heu_pb_2.add(heu_tmp.get(i*ceiling));
		heu_pb_2.add(1.0);
//		System.out.println("heu_pb_2 ==> " + heu_pb_2);
		
		/*---------------------------------------------------------------*/
		/* compute opt probability combination */
		if(compute_opt) pb_combin(PbCombin.pb_combin, pb, PbCombin.pb, layer);
		else PbCombin.pb.add(1.0);
		Collections.sort(PbCombin.pb);
//		System.out.println("PbCombin.pb ==> " + PbCombin.pb);
		
		/* compute heuristic probability combination version 1 (random) */
		pb_combin(PbCombin.heu_pb_combin, heu_pb_1, PbCombin.heu_pb_1, choose);
		Collections.sort(PbCombin.heu_pb_1);
//		PbCombin.heu_pb_1.add(1.0); // for test
//		System.out.println("PbCombin.heu_pb_1 ==> " + PbCombin.heu_pb_1);
		
		/* compute heuristic probability combination version 2 (average) */
		pb_combin(PbCombin.heu_pb_combin_2, heu_pb_2, PbCombin.heu_pb_2, choose);
		Collections.sort(PbCombin.heu_pb_2);
//		System.out.println("PbCombin.heu_pb_2 ==> " + PbCombin.heu_pb_2);
		
		/*---------------------------------*/
		/* compute Brute DP and heuristic DP */ 
		new DP(table, layer, server);
		for(int i=1; i<= server; i++) DP.recursive(layer, server, i);
		
		NewBottomUp btmup = new NewBottomUp(table, pb, lc, cc , r, bw, com, f, layer, server, 3, ls , cs, sp, pipeline_threshold);
		btmup.init_pb(server);
		btmup.compute();
		
		List<Double> opt_id_ls = new ArrayList<>(); // in order to find the opt R
		List<Double> heu_opt_id_ls = new ArrayList<>(); // in order to find the opt R
		List<Double> heu_opt_id_ls_2 = new ArrayList<>(); // in order to find the opt R
		
		List<Double> opt_ls = new ArrayList<>();
		List<Double> heu_opt_ls = new ArrayList<>();
		List<Double> heu_opt_ls_2 = new ArrayList<>();
		for(int j=1; j<=server; j++) {
			for(Table t: table) {
				if(t.getL() == layer && t.getS() == server && t.getC() == j) {
//					System.out.print(t.toString() + "===>");
//					System.out.println(t.getPb(0));
////					
//					System.out.print(t.toString() + "===>");
//					System.out.println(t.getPb(1));
////					
//					System.out.print(t.toString() + "===>");
//					System.out.println(t.get_ans_tmp(1));
//
////					System.out.println(t.getPb(0));
//					System.out.println(t.getPb(0).get(1.0));
					
					opt_ls = find_opt_ls(opt_id_ls, t, opt_ls, 0);
					heu_opt_ls = find_opt_ls(heu_opt_id_ls, t, heu_opt_ls, 1);
					heu_opt_ls_2 = find_opt_ls(heu_opt_id_ls_2, t, heu_opt_ls, 2);
					
					break;
				}
			}
			System.out.println(j + " check point Optimal Solution ==> " + opt_ls.get(0));
			System.out.println(j + " check point Heuristic Optimal Solution Version 1 ==> " + heu_opt_ls.get(0));
			System.out.println(j + " check point Heuristic Optimal Solution Version 2 ==> " + heu_opt_ls_2.get(0));
		}
		find_opt(opt_id_ls, table, 0);
//		System.out.println("opt id ls ==> " + opt_id_ls);
		find_opt(heu_opt_id_ls, table, 1);
//		System.out.println("heu opt id ls ==> " + heu_opt_id_ls);
		find_opt(heu_opt_id_ls_2,  table, 2);
//		System.out.println("heu opt id ls 2 ==> " + heu_opt_id_ls_2);
		System.out.println("opt R ==> " + opt + " ==> " + opt.getAns(0));
		System.out.println("opt heu_opt_1_R ==> " + heu_opt + " ==> " + heu_opt.getAns(1));
		System.out.println("opt heu_opt_2_R ==> " + heu_opt_2 + " ==> " + heu_opt_2.getAns(2));
		
		List<Table> cp_loc = new ArrayList<>();
		List<Table> heu_cp_loc = new ArrayList<>();
		List<Table> heu_cp_loc_2 = new ArrayList<>();
		List<Integer> cp_layer = new ArrayList<>();
		List<Integer> heu_cp_layer = new ArrayList<>();
		List<Integer> heu_cp_layer_2 = new ArrayList<>();
		
		if(opt.getAns(0) != 0) {
			get_cp_layer(cp_loc, cp_layer, Main.opt, table, 0);
			System.out.println("opt cp loc ==> " + cp_loc);
		}
		
		if(heu_opt.getAns(1) != 0) {
			get_cp_layer(heu_cp_loc, heu_cp_layer, Main.heu_opt, table, 1);
			System.out.println("heu opt cp loc ==> " + heu_cp_loc);
		}
		
		if(heu_opt_2.getAns(2) != 0) {
			get_cp_layer(heu_cp_loc_2, heu_cp_layer_2, Main.heu_opt_2, table, 2);
			System.out.println("heu opt cp loc 2 ==> " + heu_cp_loc_2);
		}
		
		
		cp_layer.add(0);
		heu_cp_layer.add(0);
		heu_cp_layer_2.add(0);
		Collections.reverse(cp_layer);
		Collections.reverse(heu_cp_layer);
		Collections.reverse(heu_cp_layer_2);
		System.out.println("Opt Ans ==> " + cp_layer);
		System.out.println("Heuristic version 1 Ans ==> " + heu_cp_layer);
		System.out.println("Heuristic version 2 Ans ==> " + heu_cp_layer_2);
		
		/* Cloud only */
		Cloud cloud = new Cloud(pb, lc, cc, bw , com, f);
		System.out.println("Opt cloud ==> " + cloud.compute(cp_layer));
		System.out.println("Heuristic version 1 cloud ==> " + cloud.compute(heu_cp_layer));
		System.out.println("Heuristic version 2 cloud ==> " + cloud.compute(heu_cp_layer_2));
		System.out.println("No check point cloud ==> " + cloud.init_compute());
		
		/* Device only */
		Device device = new Device(pb, lc, cc, com);
		System.out.println("Opt device ==> " + device.compute(cp_layer));
		System.out.println("Heuristic version 1 device ==> " + device.compute(heu_cp_layer));
		System.out.println("Heuristic version 2 device ==> " + device.compute(heu_cp_layer_2));
		System.out.println("No check point device ==> " + device.init_compute());
		
	}
	
	public static void pb_combin(Map<Integer, Set<Double>> map, List<Double> ls, List<Double> result,double choose){
		PbCombin cbin = new PbCombin(ls);
		String s = null;
		char[] from = new char[(int)choose];
		for(int i=0; i<choose; i++) {
			char c = (char)(i+1+'@');
			from[i] = c;
		}
	    char to[] = new char[from.length];
	    
	    Set<Double> init = new HashSet<>();
	    init.add(1.0);
	    map.put(0, init);
	    for (int i = 1; i <= from.length; i++) {
	      s = cbin.comb(from, to, i, from.length, i);
	      cbin.compute_pb(s, i, map);
	    }
	    
	    cbin.map_to_list(map, result);
	}
	
	public static void find_opt(List<Double> tmp_ls, List<Table> table, int version){
		double min = Double.MAX_VALUE;
		int tmp = 0;
		for(int i=0; i<tmp_ls.size(); i++) {
			if(tmp_ls.get(i) <= min) {
				tmp = i;
				min = tmp_ls.get(i);
			}
		}
		for(Table t: table) {
			if(t.getL() == layer && t.getS() == server && t.getC() == tmp+1) {
				if(version == 0) {
					Main.opt = t;
					Main.opt.setAns(tmp_ls.get(tmp), version);
				}
				else if(version == 1) {
					Main.heu_opt = t;
					Main.heu_opt.setAns(tmp_ls.get(tmp), version);
				}
				else if(version == 2) {
					Main.heu_opt_2 = t;
					Main.heu_opt_2.setAns(tmp_ls.get(tmp), version);
				}
				break;
			}
		}
		
	}
	
	public static List<Double> find_opt_ls(List<Double> tmp_ls, Table opt_tmp, List<Double> ls, int version){
		double min = Double.MAX_VALUE;
		ls = new ArrayList<>();
		ls.add(0.0);
		ls.add(0.0);
		ls.add(0.0);
		for(List<Double> l: opt_tmp.get_ans_tmp(version)) {
			if(l.get(0) <= min) {
				min = l.get(0); 
				ls.set(0, l.get(0)); // answer
				ls.set(1, l.get(3)); // id number
				ls.set(2, l.get(4)); // remain data
			}
		}
		tmp_ls.add(ls.get(0)); // for tracking opt R
		
		return ls;
	}
	
	public static void get_cp_layer(List<Table> cp_loc, List<Integer> cp_layer, Table opt, List<Table> table, int version){
		
		double min = Double.MAX_VALUE; 
		cp_loc.add(opt);
		List<Double> opt_ls = new ArrayList<>();
		opt_ls.add(0.0);
		opt_ls.add(0.0);
		opt_ls.add(0.0);
		for(List<Double> l: opt.get_ans_tmp(version)) {
			if(l.get(0) <= min) {
				min = l.get(0); 
				opt_ls.set(0, l.get(0)); // answer
				opt_ls.set(1, l.get(3)); // id number
				opt_ls.set(2, l.get(4)); // remain data
			}
		}
		while(opt_ls.get(1) != -1.0){
			for(Table t: table) {
				if(t.getID() == opt_ls.get(1).intValue()) {
					min = Double.MAX_VALUE;
//					System.out.println("get cp layer ==> " + t.toString());
					cp_loc.add(t);
					for(List<Double> l: t.get_ans_tmp(version)) {
						if(l.get(0) <= min && l.get(2) == opt_ls.get(2)) {
							min = l.get(0);
							opt_ls.set(0, l.get(0));
							opt_ls.set(1, l.get(3));
							if(l.get(3) != -1.0)opt_ls.set(2, l.get(4));
						}
					}
					break;
				}
			}
		}
		// the check point's layer
		
		for(int i=0; i<cp_loc.size(); i++) {
			if(i != cp_loc.size()-1) {
				if(cp_loc.get(i).getC() - cp_loc.get(i+1).getC() == 1) {
					cp_layer.add(cp_loc.get(i).getL());
				}
			}
			else {
				if(cp_loc.get(i).getC() ==1) {
					cp_layer.add(cp_loc.get(i).getL());
				}
			}
			
		}
		
//		return cp_layer;
	}

}
