package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class YOLO {
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
	
	public YOLO(double in_width, double in_height) throws IOException {
		
		FileReader fr = new FileReader("YOLO.txt");
		BufferedReader br = new BufferedReader(fr);
        try {
			while (br.ready()) {
//					    System.out.println(br.readLine());
				this.pb.add(Double.parseDouble(br.readLine())/100.0);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        fr.close();
//		        System.out.println(pb);        
		
		this.input_width = in_width;
		this.input_height = in_height;
	}
	
	public void data_compute() {
		/* compute each layer's neurons */
		this.neuron.add(0.0);
		this.neuron.add(input_height*input_width*3); // input layer
		input_height = input_height/2;
		input_width = input_width/2;
		this.neuron.add(input_height*input_width*64); // convolution 1
		input_height = input_height/2;
		input_width = input_width/2;
		this.neuron.add(input_height*input_width*64); // pooling 1
		this.neuron.add(input_height*input_width*192); // convolution 2
		input_height = input_height/2;
		input_width = input_width/2;
		this.neuron.add(input_height*input_width*192); // pooling 2
		this.neuron.add(input_height*input_width*128);
		this.neuron.add(input_height*input_width*256);
		this.neuron.add(input_height*input_width*256);
		this.neuron.add(input_height*input_width*512);
		input_height = input_height/2;
		input_width = input_width/2;
		this.neuron.add(input_height*input_width*512); // pooling 3
		this.neuron.add(input_height*input_width*256);
		this.neuron.add(input_height*input_width*512);
		this.neuron.add(input_height*input_width*256);
		this.neuron.add(input_height*input_width*512);
		this.neuron.add(input_height*input_width*256);
		this.neuron.add(input_height*input_width*512);
		this.neuron.add(input_height*input_width*256);
		this.neuron.add(input_height*input_width*512);
		this.neuron.add(input_height*input_width*512);
		this.neuron.add(input_height*input_width*1024);
		input_height = input_height/2;
		input_width = input_width/2;
		this.neuron.add(input_height*input_width*1024); // pooling 4
		this.neuron.add(input_height*input_width*512);
		this.neuron.add(input_height*input_width*1024);
		this.neuron.add(input_height*input_width*512);
		this.neuron.add(input_height*input_width*1024);
		this.neuron.add(input_height*input_width*1024);
		input_height = input_height/2;
		input_width = input_width/2;
		this.neuron.add(input_height*input_width*1024);
		this.neuron.add(input_height*input_width*1024);
		this.neuron.add(input_height*input_width*1024);
		this.neuron.add(4096.0);
		this.neuron.add(4096.0);
		this.neuron.add(1000.0);
//			System.out.println(this.neuron);
		
		/* compute data size change */
		this.ratio.add(1.0);
		this.ratio.add(1.0); // input layer
		for(int i=2; i<=31; i++) this.ratio.add(this.neuron.get(i)/this.neuron.get(i-1));
		this.ratio.add(0.0); // output layer
//			System.out.println(ratio);
		
		/* compute FLOPS */
		this.FLOPS.add(0.0);
		this.FLOPS.add(0.0); // input layer
		this.FLOPS.add(2*3*7*7*64.0);
		this.FLOPS.add(0.0); // pooling 1
		this.FLOPS.add(2*64*3*3*192.0);
		this.FLOPS.add(0.0); // pooling 2
		this.FLOPS.add(2*192*1*1*128.0);
		this.FLOPS.add(2*128*3*3*256.0);
		this.FLOPS.add(2*256*3*3*256.0);
		this.FLOPS.add(2*256*3*3*512.0);
		this.FLOPS.add(0.0); // pooling 3
		this.FLOPS.add(2*512*1*1*256.0);
		this.FLOPS.add(2*256*3*3*512.0);
		this.FLOPS.add(2*512*1*1*256.0);
		this.FLOPS.add(2*256*3*3*512.0);
		this.FLOPS.add(2*512*1*1*256.0);
		this.FLOPS.add(2*256*3*3*512.0);
		this.FLOPS.add(2*512*1*1*256.0);
		this.FLOPS.add(2*256*3*3*512.0);
		this.FLOPS.add(2*512*1*1*512.0);
		this.FLOPS.add(2*512*3*3*1024.0);
		this.FLOPS.add(0.0); // pooling 4
		this.FLOPS.add(2*1024*1*1*512.0);
		this.FLOPS.add(2*512*3*3*1024.0);
		this.FLOPS.add(2*1024*1*1*512.0);
		this.FLOPS.add(2*512*3*3*1024.0);
		this.FLOPS.add(2*1024*3*3*1024.0);
		this.FLOPS.add(2*1024*3*3*1024.0);
		
		this.FLOPS.add(2*1024*3*3*1024.0);
		this.FLOPS.add(2*1024*3*3*1024.0);
		for(int i=30; i<=32; i++) this.FLOPS.add(2*this.neuron.get(i-1)*this.neuron.get(i)); // fully connected
		for(int i=2; i<=32; i++) this.FLOPS.set(i, this.FLOPS.get(i)/1000000000); // convert to GFLOPS
//		System.out.println(FLOPS);
		
		/* compute parameters (layer size) */
		this.params.add(0.0);
		this.params.add(0.0); // input layer
		this.params.add((3*7*7+1)*64.0);
		this.params.add(0.0); // pooling 1
		this.params.add((64*3*3+1)*192.0);
		this.params.add(0.0); // pooling 2
		this.params.add((192*1*1+1)*128.0);
		this.params.add((128*3*3+1)*256.0);
		this.params.add((256*3*3+1)*256.0);
		this.params.add((256*3*3+1)*512.0);
		this.params.add(0.0); // pooling 3
		this.params.add((512*1*1+1)*256.0);
		this.params.add((256*3*3+1)*512.0);
		this.params.add((512*1*1+1)*256.0);
		this.params.add((256*3*3+1)*512.0);
		this.params.add((512*1*1+1)*256.0);
		this.params.add((256*3*3+1)*512.0);
		this.params.add((512*1*1+1)*256.0);
		this.params.add((256*3*3+1)*512.0);
		this.params.add((512*1*1+1)*512.0);
		this.params.add((512*3*3+1)*1024.0);
		this.params.add(0.0); // pooling 4
		this.params.add((1024*1*1+1)*512.0);
		this.params.add((512*3*3+1)*1024.0);
		this.params.add((1024*1*1+1)*512.0);
		this.params.add((512*3*3+1)*1024.0);
		this.params.add((1024*3*3+1)*1024.0);
		this.params.add((1024*3*3+1)*1024.0);
		
		this.params.add((1024*3*3+1)*1024.0);
		this.params.add((1024*3*3+1)*1024.0);
		for(int i=30; i<=32; i++) this.params.add(this.neuron.get(i-1)*this.neuron.get(i)); // fully connected
		for(int i=1; i<=32; i++) this.params.set(i, this.params.get(i)*32/1000000); // convert to Mbits 
//			System.out.println(params);
		
		/* compute check point FLOPS */
		this.cp_FLOPS.add(0.0);
		this.cp_FLOPS.add(0.0); // input layer
		for(int i=2; i<=31; i++) this.cp_FLOPS.add(16.0/i);
		this.cp_FLOPS.add(0.0); // output layer
//			for(int i=2; i<=22; i++) this.cp_FLOPS.set(i, this.cp_FLOPS.get(i)/1000000000); // convert to GFLOPS
		
		/* compute check point parameter (check point layer size) */
		this.cp_params.add(0.0);
		this.cp_params.add(0.0);
		for(int i=2; i<=31; i++) this.cp_params.add(this.params.get(32)); // add softmax layer
		this.cp_params.add(0.0); // output layer
		for(int i=1; i<=32; i++) this.cp_params.set(i, this.cp_params.get(i)*32/1000000); // convert to Mbits
		
//		String filename = "YOLO_data.csv";
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
		Random r= new Random(seed);
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
			else this.bw.add(10000*tmp); // MEC server
//			else this.bw.add(2300.0+150*i); // MEC server
//			else this.bw.add(2500.0); // MEC server
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
