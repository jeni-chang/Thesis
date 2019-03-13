package main;

import java.util.ArrayList;
import java.util.List;

public class DP {
	
	private static List<Table> table = new ArrayList<>();
	private static int x;
	private static int y;
	
	public DP(List<Table> t, int layer, int server) {
		DP.table = t;
		DP.x = layer + 1;
		DP.y = server;
	}
	
	public static void recursive(int layer, int server, int cp) {
		
		int loc = cp*x*y + (server-1)*x + (layer+1);
//		int flag = 0;
//		for(Table t: table) {
//			if(t.getID() == loc ) {
//				if(t.getcheck()) flag = 1;
//				t.setcheck(true);
//				break;
//			}
//		}
//		if(loc<0)System.out.println("Resursive Error!!! ==> " + loc);
		if(table.get(loc-1).getcheck()) return;
		table.get(loc-1).setcheck(true);
		
		if(server >= 2 && cp >= 1) {
			for(int i=0; i<=layer; i++) {
				if(check(layer, server, cp, i, server-1, cp-1) ) recursive(i, server-1, cp-1);
				if(check(layer, server, cp, i, server-1, cp)) recursive(i, server-1, cp);
			}
		}
	}
	
	public static boolean check(int layer, int server, int cp, int sub_layer, int sub_server, int sub_cp) {
		/* Example => total 4 layers, 3 servers */
		
		if(server == sub_server+1) {
			// no check point in entire model. R(4,2,0)
			if(sub_layer == x-1 && sub_cp == 0) return false;
			// no check point can use in the last layer. R(4,3,1) do not need to check R(3,2,1)
			else if(layer == DP.x-1 && sub_layer!=DP.x-1 && sub_cp == cp && sub_cp!=0) return false;
			// use check point but there is no layer. R(0,2,1)
			else if(sub_layer == 0 && sub_cp != 0) return false;
			// no enough layer to insert check point. R(4,3,2) do not need to check R(4,2,1)
			else if(sub_layer == layer && sub_cp == (cp-1)) return false;
			// check point more than layer. R(2,2,3)
			else if(sub_layer < sub_cp) return false;
			// check point more than server. R(3,2,3)
			else if(sub_server < sub_cp) return false;
			
			// layer less than
			else if(layer < sub_layer) return false;
			// more check point
			else if(cp < sub_cp) return false;
			
			else return true;
		}
		
		
		return false;
	}
	
	public List<Table> get_table() {
		return DP.table;
	}
}
