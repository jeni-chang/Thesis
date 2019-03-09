package main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VGG16 {
	private List<Double> pb = new ArrayList<>();
	private List<Double> neuron = new ArrayList<>();
	private List<Double> channel = new ArrayList<>();
	private List<Double> ratio = new ArrayList<>();
	private List<Double> FLOPS = new ArrayList<>();
	private List<Double> cp_FLOPS = new ArrayList<>();
	private List<Double> params = new ArrayList<>();
	private List<Double> cp_params = new ArrayList<>();
	
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
		
//		this.pb.add(0.0);
//		this.pb.add(0.0); // input layer
//		this.pb.add(0.669);
//		this.pb.add(0.681);
//		this.pb.add(0.688);
//		this.pb.add(0.689);
//		this.pb.add(0.3288);
//		this.pb.add(0.3024);
//		this.pb.add(0.276);
//		this.pb.add(0.2504);
//		this.pb.add(0.2262);
//		this.pb.add(0.2043);
//		this.pb.add(0.1853);
//		this.pb.add(0.17);
//		this.pb.add(0.159);
//		this.pb.add(0.159);
//		this.pb.add(0.1473);
//		this.pb.add(0.1452);
//		this.pb.add(0.1445);
//		this.pb.add(0.1446);
//		this.pb.add(0.1447);
//		this.pb.add(0.1452);
//		this.pb.add(1.0);
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
        
		
		this.input_width = in_width;
		this.input_height = in_height;
		this.filter_width = f_width;
		this.filter_height = f_height;
		
//		System.out.println(this.pb);
		

	}
	
	public void data_compute() {
		// compute each layer's neurons
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
		
		// compute data size change
		this.ratio.add(1.0);
		this.ratio.add(1.0); // input layer
		for(int i=2; i<=21; i++) this.ratio.add(this.neuron.get(i)/this.neuron.get(i-1));
		this.ratio.add(0.0); // output layer
//		System.out.println(ratio);
		
		// compute FLOPS
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
//		System.out.println(FLOPS);
		
		// compute parameters (layer size)
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
		System.out.println(params);
		
		// compute check point FLOPS
		this.cp_FLOPS.add(0.0);
		this.cp_FLOPS.add(0.0); // input layer
		for(int i=2; i<=21; i++) this.cp_FLOPS.add(this.FLOPS.get(22));
		this.cp_FLOPS.add(0.0); // output layer
		for(int i=2; i<=22; i++) this.cp_FLOPS.set(i, this.cp_FLOPS.get(i)/1000000000); // convert to GFLOPS
		
		//compute check point parameter (check point layer size)
		this.cp_params.add(0.0);
		this.cp_params.add(0.0);
		for(int i=2; i<=21; i++) this.cp_params.add(this.params.get(22)); // add softmax layer
		this.cp_params.add(0.0); // output layer
		for(int i=1; i<=22; i++) this.cp_params.set(i, this.cp_params.get(i)*32/1000000); // convert to Mbits
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
	
	

}
