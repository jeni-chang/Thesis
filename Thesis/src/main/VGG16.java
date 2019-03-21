package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VGG16 {
	// fixed data
	private List<Double> pb = new ArrayList<>();
	private List<Double> neuron = new ArrayList<>();
	private List<Double> channel = new ArrayList<>();
	private List<Double> ratio = new ArrayList<>();
	private List<Double> FLOPS = new ArrayList<>();
	private List<Double> cp_FLOPS = new ArrayList<>();
	private List<Double> params = new ArrayList<>();
	private List<Double> cp_params = new ArrayList<>();
	// random data
	private List<Double> com = new ArrayList<>();
	private List<Double> bw = new ArrayList<>();
	private List<Double> sp = new ArrayList<>();
	
	private double input_width;
	private double input_height;
	private double filter_width;
	private double filter_height;
	
	public VGG16(double in_width, double in_height, double f_width, double f_height) throws IOException {
		this.channel.add(0.0);
		this.channel.add(3.0);
		this.channel.add(64.0);
		this.channel.add(64.0);
		this.channel.add(64.0);
		this.channel.add(128.0);
		this.channel.add(128.0);
		this.channel.add(128.0);
		this.channel.add(256.0);
		this.channel.add(256.0);
		this.channel.add(256.0);
		this.channel.add(256.0);
		this.channel.add(512.0);
		this.channel.add(512.0);
		this.channel.add(512.0);
		this.channel.add(512.0);
		this.channel.add(512.0);
		this.channel.add(512.0);
		this.channel.add(512.0);
		this.channel.add(512.0);
		this.channel.add(1.0);
		this.channel.add(1.0);
		this.channel.add(1.0);
		
		FileReader fr = new FileReader("VGG.txt");
		BufferedReader br = new BufferedReader(fr);
        try {
			while (br.ready()) {
//			    System.out.println(br.readLine());
				this.pb.add(Double.parseDouble(br.readLine())/100.0);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        fr.close();
//        System.out.println(pb);        
		
		this.input_width = in_width;
		this.input_height = in_height;
		this.filter_width = f_width;
		this.filter_height = f_height;
		
//		System.out.println(this.pb);
	}
	
	// compute fixed data
	public void data_compute() {
		/* compute each layer's neurons */
		this.neuron.add(0.0);
		for(int i=1; i<=22; i++) {
			if(i>=1 && i<=3) this.neuron.add(input_width * input_height * this.channel.get(i));
			else if(i>=4 && i<=6) this.neuron.add((input_width/2) * (input_height/2) * this.channel.get(i));
			else if(i>=7 && i<=10) this.neuron.add((input_width/4) * (input_height/4) * this.channel.get(i));
			else if(i>=11 && i<=14) this.neuron.add((input_width/8) * (input_height/8) * this.channel.get(i));
			else if(i>=15 && i<=18) this.neuron.add((input_width/16) * (input_height/16) * this.channel.get(i));
			else if(i==19) this.neuron.add((input_width/32) * (input_height/32) * this.channel.get(i));
			else if(i==20) this.neuron.add(4096.0);
			else if(i==21) this.neuron.add(4096.0);
			else if(i==22) this.neuron.add(1000.0);
		}
		
		/* compute data size change */
		this.ratio.add(1.0);
		this.ratio.add(1.0); // input layer
		for(int i=2; i<=21; i++) this.ratio.add(this.neuron.get(i)/this.neuron.get(i-1));
		this.ratio.add(0.0); // output layer
//		System.out.println(ratio);
		
		/* compute FLOPS */
		this.FLOPS.add(0.0);
		this.FLOPS.add(0.0); // input layer
		for(int i=2; i<=19; i++) { // convolution layer
			if(i!=4 && i!=7 && i!=11 && i!=15 && i!=19) {
				double flops = (2*this.channel.get(i-1)*filter_width*filter_height*this.channel.get(i))+1;
				this.FLOPS.add(flops);
			}
			else this.FLOPS.add(0.0); // pooling layer
		}
		for(int i=20; i<=22; i++) this.FLOPS.add(2*this.neuron.get(i-1)*this.neuron.get(i)); // fully connected
		for(int i=2; i<=22; i++) this.FLOPS.set(i, this.FLOPS.get(i)/1000000000); // convert to GFLOPS
//		this.FLOPS.set(9, 20.0);
//		System.out.println(FLOPS);
		
		/* compute parameters (layer size) */
		this.params.add(0.0);
		this.params.add(0.0); // input layer
		for(int i=2; i<=19; i++) {
			if(i!=4 && i!=7 && i!=11 && i!=15 && i!=19) {
				double flops = (this.channel.get(i-1)*filter_width*filter_height+1) * this.channel.get(i);
				this.params.add(flops);
			}
			else this.params.add(0.0); // pooling layer
		}
		for(int i=20; i<=22; i++) this.params.add(this.neuron.get(i-1)*this.neuron.get(i)); // fully connected
		for(int i=1; i<=22; i++) this.params.set(i, this.params.get(i)*32/1000000); // convert to Mbits 
//		System.out.println(params);
		
		/* compute check point FLOPS */
		this.cp_FLOPS.add(0.0);
		this.cp_FLOPS.add(0.0); // input layer
//		for(int i=2; i<=21; i++) this.cp_FLOPS.add(this.FLOPS.get(22));
//		for(int i=2; i<=21; i++) this.cp_FLOPS.add(22.0-i*i/25);
		for(int i=2; i<=21; i++) this.cp_FLOPS.add(22.0/i);
		this.cp_FLOPS.add(0.0); // output layer
//		for(int i=2; i<=22; i++) this.cp_FLOPS.set(i, this.cp_FLOPS.get(i)/1000000000); // convert to GFLOPS
		
		/* compute check point parameter (check point layer size) */
		this.cp_params.add(0.0);
		this.cp_params.add(0.0);
		for(int i=2; i<=21; i++) this.cp_params.add(this.params.get(22)); // add softmax layer
		this.cp_params.add(0.0); // output layer
		for(int i=1; i<=22; i++) this.cp_params.set(i, this.cp_params.get(i)*32/1000000); // convert to Mbits
	}
	
	public void random_data_compute(int server, int seed) throws IOException {
		
//		if(Main.run_choose) {
//			// read bandwidth data
//			FileReader fr = new FileReader("BW.txt");
//			BufferedReader br = new BufferedReader(fr);
//	        try {
//				while (br.ready()) {
//					this.bw.add(Double.parseDouble(br.readLine()));
//				}
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//	        this.bw.add(Double.MAX_VALUE);
//	        fr.close();
//	        
//	        // read compute capability data
//	        fr = new FileReader("COM.txt");
//	        br = new BufferedReader(fr);
//	        try {
//				while (br.ready()) {
//					this.com.add(Double.parseDouble(br.readLine()));
//				}
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//	        fr.close();
//		}
//		else {
//			Random r = new Random();
//			double tmp = 0.0;
//			/* generate bandwidth (Mbits) */
//			this.bw.add(0.0);
//			for(int i=1; i<server; i++) {
//				do{
//					tmp = r.nextGaussian()*0.4;
//					tmp = Math.abs(tmp);
//				}while(tmp>=1.0 || tmp==0.0 || tmp<0.25);
//				if(i==1) this.bw.add(1000*tmp); // device
//				else this.bw.add(8000*tmp); // MEC server
//			}
//			this.bw.add(Double.MAX_VALUE); // Cloud server
////			System.out.println(bw);
//			
//			/* generate compute capability (GFLOPS) */
//			this.com.add(0.0);
//			this.com.add(50.0); // device
//			for(int i=2; i<=server; i++) {
//				do{
//					tmp = r.nextGaussian()*0.4;
//					tmp = Math.abs(tmp);
//				}while(tmp>=1.0 || tmp==0.0);
//				this.com.add(1000*tmp+100*i); // MEC server and Cloud server
//			}
////			System.out.println(com);
//		}
		
		Random r;
		if(Main.run_choose) r = new Random(seed);
		else r = new Random();
		
		double tmp = 0.0;
		/* generate bandwidth (Mbits) */
		this.bw.add(0.0);
		for(int i=1; i<server; i++) {
			do{
				tmp = r.nextGaussian()*0.4;
				tmp = Math.abs(tmp);
			}while(tmp>=1.0 || tmp==0.0 || tmp<0.25);
			if(i==1) this.bw.add(1000*tmp); // device
			else this.bw.add(8000*tmp); // MEC server
		}
		this.bw.add(Double.MAX_VALUE); // Cloud server
//		System.out.println(bw);
		
		/* generate compute capability (GFLOPS) */
		this.com.add(0.0);
		this.com.add(50.0); // device
		for(int i=2; i<=server; i++) {
			do{
				tmp = r.nextGaussian()*0.4;
				tmp = Math.abs(tmp);
			}while(tmp>=1.0 || tmp==0.0);
			this.com.add(1000*tmp+100*i); // MEC server and Cloud server
		}
		
		
		/* generate space capacity (Mbits) */
		this.sp.add(0.0);
		this.sp.add(50000.0);
		for(int i=2; i<=server; i++) this.sp.add(1000000.0);
//		System.out.println(sp);
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
	
	public List<Double> get_sp(){
		return this.sp;
	}
	

}
