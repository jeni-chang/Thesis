package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AlexNet {
	// fixed data
	private List<Double> pb = new ArrayList<>();
	private List<Double> neuron = new ArrayList<>();
	private List<Double> ratio = new ArrayList<>();
	private List<Double> FLOPS = new ArrayList<>();
	private List<Double> cp_FLOPS = new ArrayList<>();
	private List<Double> params = new ArrayList<>();
	private List<Double> cp_params = new ArrayList<>();
	// random data
	private List<Double> com = new ArrayList<>();
	private List<Double> bw = new ArrayList<>();
	private List<Double> sp = new ArrayList<>();
	
	private List<Double> com_tmp = new ArrayList<>(); // for cloud opt
	
	private double input_width;
	private double input_height;
	
	public AlexNet(double in_width, double in_height) throws IOException {
		
		FileReader fr = new FileReader("AlexNet.txt");
		BufferedReader br = new BufferedReader(fr);
        try {
			while (br.ready()) {
//				    System.out.println(br.readLine());
				this.pb.add(Double.parseDouble(br.readLine())/100.0);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        fr.close();
//	        System.out.println(pb);        
		
		this.input_width = in_width;
		this.input_height = in_height;
	}
		
	public void data_compute() {
		/* compute each layer's neurons */
		this.neuron.add(0.0);
		this.neuron.add(input_height*input_width*3); // input layer
		input_height = ((input_height-11)/4)+1;
		input_width = ((input_width-11)/4)+1;
		this.neuron.add(input_height*input_width*96);
		input_height = ((input_height-3)/2)+1;
		input_width = ((input_width-3)/2)+1;
		this.neuron.add(input_height*input_width*96);
		input_height = ((input_height+2*2-5)/1)+1;
		input_width = ((input_width+2*2-5)/1)+1;
		this.neuron.add(input_height*input_width*256);
		input_height = ((input_height-3)/2)+1;
		input_width = ((input_width-3)/2)+1;
		this.neuron.add(input_height*input_width*256);
		this.neuron.add(input_height*input_width*384);
		this.neuron.add(input_height*input_width*384);
		this.neuron.add(input_height*input_width*256);
		input_height = ((input_height-3)/2)+1;
		input_width = ((input_width-3)/2)+1;
		this.neuron.add(input_height*input_width*256);
		this.neuron.add(4096.0);
		this.neuron.add(4096.0);
		this.neuron.add(1000.0);
//			System.out.println(this.neuron);
		
		/* compute data size change */
		this.ratio.add(1.0);
		this.ratio.add(1.0); // input layer
		for(int i=2; i<=11; i++) this.ratio.add(this.neuron.get(i)/this.neuron.get(i-1));
		this.ratio.add(0.0); // output layer
//			System.out.println(ratio);
		
		/* compute FLOPS */
		this.FLOPS.add(0.0);
		this.FLOPS.add(0.0); // input layer
		this.FLOPS.add(2*3*11*11*96.0);
		this.FLOPS.add(0.0); // pooling
		this.FLOPS.add(2*96*5*5*256.0);
		this.FLOPS.add(0.0); // pooling
		this.FLOPS.add(2*256*3*3*384.0);
		this.FLOPS.add(0.0); // pooling
		this.FLOPS.add(2*384*3*3*384.0);
		this.FLOPS.add(0.0); // pooling
		for(int i=10; i<=12; i++) this.FLOPS.add(2*this.neuron.get(i-1)*this.neuron.get(i)); // fully connected
		for(int i=2; i<=12; i++) this.FLOPS.set(i, this.FLOPS.get(i)/1000000000); // convert to GFLOPS
//			System.out.println(FLOPS);
		
		/* compute parameters (layer size) */
		this.params.add(0.0);
		this.params.add(0.0); // input layer
		this.params.add((3*11*11+1)*96.0);
		this.params.add(0.0); // pooling
		this.params.add((96*5*5+1)*256.0);
		this.params.add(0.0); // pooling
		this.params.add((256*3*3+1)*384.0);
		this.params.add(0.0); // pooling
		this.params.add((384*3*3+1)*384.0);
		this.params.add(0.0); // pooling
		for(int i=10; i<=12; i++) this.params.add(this.neuron.get(i-1)*this.neuron.get(i)); // fully connected
		for(int i=1; i<=12; i++) this.params.set(i, this.params.get(i)*32/1000000); // convert to Mbits 
//			System.out.println(params);
		
		/* compute check point FLOPS */
		this.cp_FLOPS.add(0.0);
		this.cp_FLOPS.add(0.0); // input layer
		for(int i=2; i<=11; i++) this.cp_FLOPS.add(12.0/i);
		this.cp_FLOPS.add(0.0); // output layer
//			for(int i=2; i<=22; i++) this.cp_FLOPS.set(i, this.cp_FLOPS.get(i)/1000000000); // convert to GFLOPS
		
		/* compute check point parameter (check point layer size) */
		this.cp_params.add(0.0);
		this.cp_params.add(0.0);
		for(int i=2; i<=11; i++) this.cp_params.add(this.params.get(12)); // add softmax layer
		this.cp_params.add(0.0); // output layer
		for(int i=1; i<=12; i++) this.cp_params.set(i, this.cp_params.get(i)*32/1000000); // convert to Mbits
		
		
//		String filename = "AlexNet_data.csv";
//		StringBuilder ans = new StringBuilder();
//		for(double d : ratio) {
//			ans.append(d);
//			ans.append(',');
//		}
//		ans.append('\n');
//		for(double d : FLOPS) {
//			ans.append(d);
//			ans.append(',');
//		}
//		ans.append('\n');
//		for(double d : cp_FLOPS) {
//			ans.append(d);
//			ans.append(',');
//		}
//		ans.append('\n');
//		try {
//			FileWriter output = new FileWriter(filename,true);
//			output.write(ans.toString());
//			output.close();
//		}catch (Exception e) {
//			// TODO: handle exception
//		}
	}
	
	public void random_data_compute(int server, int seed) throws IOException {
		Random r = new Random(seed);
//		if(Main.run_choose) r = new Random(seed);
//		else r = new Random();
		
		double tmp = 0.0;
		/* generate bandwidth (Mbits) */
		this.bw.add(0.0);
		for(int i=1; i<server; i++) {
			do{
				tmp = r.nextGaussian()*0.4;
				tmp = Math.abs(tmp);
			}while(tmp>=1.0 || tmp==0.0 || tmp<0.25);
			if(i==1) this.bw.add(1000*tmp); // device
			else this.bw.add(5000*tmp); // MEC server
		}
		this.bw.add(Double.MAX_VALUE); // Cloud server
		if(Main.cloud_opt) {
			for(int i=1; i<=server; i++) {
				this.bw.set(i, Double.MAX_VALUE);
			}
		}
//		System.out.println(bw);
		
		/* generate compute capability (GFLOPS) */
		this.com.add(0.0);
		this.com.add(100.0); // device
		for(int i=2; i<=server; i++) {
			do{
				tmp = r.nextGaussian()*0.4;
				tmp = Math.abs(tmp);
			}while(tmp>=1.0 || tmp==0.0);
//			this.com.add(1000*tmp+100*i); // MEC server and Cloud server
			this.com.add(100.0+200*i);
		}
		for(double d : com) this.com_tmp.add(d); // for cloud opt
		if(Main.cloud_opt) {
			for(int i=1; i<=server; i++) {
				this.com.set(i, 1000*tmp+100*server);
			}
		}
//		System.out.println(com);
		
		/* generate space capacity (Mbits) */
		this.sp.add(0.0);
		if(Main.cloud_opt)this.sp.add(1000000.0);
		else this.sp.add(50000.0);
		for(int i=2; i<=server; i++) this.sp.add(1000000.0);
//			System.out.println(sp);
	}
	
	public List<Double> get_ratio(){
		return this.ratio;
	}
	
	public List<Double> get_FLOPS(){
		return this.FLOPS;
	}
	
	public List<Double> get_params(){
		return this.params;
	}
	
	public List<Double> get_cp_FLOPS(){
		return this.cp_FLOPS;
	}
	
	public List<Double> get_cp_params(){
		return this.cp_params;
	}
	
	public List<Double> get_pb(){
		return this.pb;
	}
	
	public List<Double> get_bw(){
		return this.bw;
	}
	
	public List<Double> get_com(){
		return this.com;
	}
	
	public List<Double> get_com_tmp(){
		return this.com_tmp;
	}
	
	public List<Double> get_sp(){
		return this.sp;
	}
}
