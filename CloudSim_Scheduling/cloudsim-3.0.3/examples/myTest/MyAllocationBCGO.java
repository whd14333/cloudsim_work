/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */


package myTest;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.text.DecimalFormat;
import java.util.*;

/**
 * An example showing how to create
 * scalable simulations.
 */
public class MyAllocationBCGO {

	/** The cloudlet list. */
	private static List<Cloudlet> cloudletList;
	private static int cloudletNum=40;

	private static List<Vm> vmlist;
	private static int vmNum=5;

	private static void createVM(int userId) {

		//Creates a container to store VMs. This list is passed to the broker later
		vmlist = new ArrayList<Vm>();

		// VM description（虚拟机参数设置）
		int vmid = 0;
		int[] mips = new int[] {278, 289, 132, 209, 286};//虚拟机CPU频率
		long size = 10000; // image size (MB)
		int ram = 2048; // vm memory (MB)
		long[] bw = new long[] {1000, 1200, 1100, 1300, 900};//虚拟机带宽
		int pesNumber = 1; // number of cpus
		String vmm = "Xen"; // VMM name

		// create VM
		// add the VM to the vmList
		for(int i=0; i<vmNum; i++) {
			vmlist.add(new Vm(vmid, userId, mips[i], pesNumber, ram, bw[i], size, vmm, new CloudletSchedulerTimeShared()));
			vmid++;
		}
	}


	private static void createCloudlet(int userId){
		int pesNumber = 1;
		int id=0;
		long[] length = new long[] {
				19365, 49809, 30218, 44157, 16754, 26785,12348, 28894, 33889, 58967,
				35045, 12236, 20085, 31123, 32227, 41727, 51017, 44787, 65854, 39836,
				18336, 20047, 31493, 30727, 31017, 30218, 44157, 16754, 26785, 12348,
				49809, 30218, 44157, 16754, 26785, 44157, 16754, 26785, 12348, 28894};//云任务指令数
		long[] fileSize = new long[] {
				30000, 50000, 10000, 40000, 20000, 41000, 27000, 43000, 36000, 33000,
				23000, 22000, 41000, 42000, 24000, 23000, 36000, 42000, 46000, 33000,
				23000, 22000, 41000, 42000, 50000, 10000, 40000, 20000, 41000, 10000,
				40000, 20000, 41000, 27000, 30000, 50000, 10000, 40000, 20000, 17000};//云任务文件大小
		// 使用随机的方法生成指令长度和文件数据长度。
//		long[] length = new long[cloudletNum];
//		long[] fileSize = new long[cloudletNum];
//		Random random = new Random();
//		random.setSeed(10000L);//设置种子，让每次运行产生的随机数相同
//		for(int i = 0 ; i < cloudletNum ; i ++) {
//			length[i] = random.nextInt(4000) + 1000;
//		}
//		random.setSeed(5000L);//设置种子，让每次运行产生的随机数相同
//		for(int i = 0 ; i < cloudletNum ; i ++) {
//			fileSize[i] = random.nextInt(20000) + 10000;
//		}
		long outputSize=300;
		UtilizationModel model=new UtilizationModelFull();

		cloudletList=new ArrayList<Cloudlet>();
		for (int i = 0; i < cloudletNum; i++) {
			Cloudlet cloudlet=new Cloudlet(id, length[i], pesNumber, fileSize[i], outputSize, model, model, model);
			cloudlet.setUserId(userId);
			cloudletList.add(cloudlet);
			id++;
		}
	}


	////////////////////////// STATIC METHODS ///////////////////////

	/**
	 * Creates main() to run this example
	 */
	public static void main(String[] args) {
		Log.printLine("Starting MyAllocationBCGO...");

		try {
			// First step: Initialize the CloudSim package. It should be called
			// before creating any entities.
			int num_user = 1;   // number of grid users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false;  // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			// Second step: Create Datacenters
			//Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation
			@SuppressWarnings("unused")
			Datacenter datacenter0 = createDatacenter("Datacenter_0");
			@SuppressWarnings("unused")
			Datacenter datacenter1 = createDatacenter("Datacenter_1");

			//Third step: Create Broker
			MyDatacenterBroker broker = createBroker();
			int brokerId = broker.getId();

			//Fourth step: Create VMs and Cloudlets and send them to broker
			createVM(brokerId); //creating 20 vms
			createCloudlet(brokerId); // creating 40 cloudlets

			broker.submitVmList(vmlist);
			broker.submitCloudletList(cloudletList);

			broker.bindCloudletToVmBCGO(cloudletList,vmlist);

			// Fifth step: Starts the simulation
			CloudSim.startSimulation();

			// Final step: Print results when simulation is over
			List<Cloudlet> newList = broker.getCloudletReceivedList();

			CloudSim.stopSimulation();

			printCloudletList(newList);

			Log.printLine("MyAllocationBCGO finished!");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Log.printLine("The simulation has been terminated due to an unexpected error");
		}
	}

	private static Datacenter createDatacenter(String name) {

		// Here are the steps needed to create a PowerDatacenter:
		// 1. We need to create a list to store
		// our machine
		List<Host> hostList = new ArrayList<Host>();

		// 2. A Machine contains one or more PEs or CPUs/Cores.
		// In this example, it will have only one core.
		List<Pe> peList = new ArrayList<Pe>();

		int mips = 1000;

		// 4. Create Host with its id and list of PEs and add them to the list
		// of machines
		int hostId = 0;
		int ram = 2048; // host memory (MB)
		long storage = 1000000; // host storage
		int bw = 10000; //带宽

		for(int i=0; i<vmNum; i++) {
			// 3. Create PEs and add these into a list.
			// 为每个虚拟机创建一个CPU，CPU能力大于虚拟机能力。
			peList.add(new Pe(i, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating

			hostList.add(
					new Host(
							hostId,
							new RamProvisionerSimple(ram),
							new BwProvisionerSimple(bw),
							storage,
							peList,
							new VmSchedulerTimeShared(peList)
					)
			); // This is our machine
			hostId++;
		}

		// 5. Create a DatacenterCharacteristics object that stores the
		// properties of a data center: architecture, OS, list of
		// Machines, allocation policy: time- or space-shared, time zone
		// and its price (G$/Pe time unit).
		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located
		double cost = 3.0; // the cost of using processing in this resource
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.001; // the cost of using storage in this
		// resource
		double costPerBw = 0.0; // the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN
		// devices by now

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
				arch, os, vmm, hostList, time_zone, cost, costPerMem,
				costPerStorage, costPerBw);

		// 6. Finally, we need to create a PowerDatacenter object.
		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}

	//We strongly encourage users to develop their own broker policies, to submit vms and cloudlets according
	//to the specific rules of the simulated scenario
	private static MyDatacenterBroker createBroker(){

		MyDatacenterBroker broker = null;
		try {
			broker = new MyDatacenterBroker("Broker");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

	/**
	 * Prints the Cloudlet objects
	 * @param list  list of Cloudlets
	 */
	private static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();

		double[] executeTimeOfVM = new double[vmNum];//记录每个虚拟机VM的最后一个任务完成时间
		double meanOfExecuteTimeOfVM = 0;//虚拟机平均运行时间
		for(int i=0;i<vmNum;i++) {//初始化数组
			executeTimeOfVM[i] = 0;
		}
		double LB=0;//负载平衡因子

		Cloudlet cloudlet;

		String indent = "    ";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent
				+ "Data center ID" + indent + "VM ID" + indent + "Time" + indent
				+ "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
				Log.print("SUCCESS");

				Log.printLine(indent + indent + cloudlet.getResourceId()
						+ indent + indent + indent + cloudlet.getVmId()
						+ indent + indent
						+ dft.format(cloudlet.getActualCPUTime()) + indent
						+ indent + dft.format(cloudlet.getExecStartTime())
						+ indent + indent
						+ dft.format(cloudlet.getFinishTime()));
				//计算每个虚拟机最后完成的时间
				if(cloudlet.getFinishTime() > executeTimeOfVM[cloudlet.getVmId()]) {
					executeTimeOfVM[cloudlet.getVmId()] = cloudlet.getFinishTime();
				}
			}
		}

		//求所有虚拟机平均运行时间
		for(int i=0;i<vmNum;i++) {
			meanOfExecuteTimeOfVM += executeTimeOfVM[i];
			Log.printLine("VM" + i +" executeTime:" + executeTimeOfVM[i] + "\n");
		}
		meanOfExecuteTimeOfVM /= vmNum;
		Log.printLine("meanOfExecuteTimeOfVM:" + meanOfExecuteTimeOfVM + "\n");

		//计算负载平衡因子
		for(int i=0; i<vmNum; i++) {
			LB += Math.pow(executeTimeOfVM[i]-meanOfExecuteTimeOfVM, 2);
		}
		LB = Math.sqrt(meanOfExecuteTimeOfVM/vmNum);
		Log.printLine("LB:" + LB + "\n");
	}
}
