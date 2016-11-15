/*
Name:Sai Charan Reddy Kamireddy

*/

/*

Loader class involves loading the user program into memory 
It takes in input the filename which consists of user program: the user program consists of starting, data and end records respectively
These data records are loaded in to the memory array 
*/
import java.io.*;
import java.util.*;



/* The loader class takes the user job and puts it in the memory. After the loader finishes its job cpu will be invoked.*/
class Loader
{
   static int traceFlag; 
   static List<ProcessControlBlock> pcb=new ArrayList<ProcessControlBlock>();  
   static int globalCountLines=0;
   public static int pid;
   static String inputfilename;
   ProcessControlBlock ob;
   static int executionTime=0;
  
   
   
   
   //function converts hexa decimal string to decimal value
   int HexToInt(String name) throws Exception
   {
	  int IntegerValue=0;
	  name=name.toUpperCase();
	  for(int i=0;i<name.length();i++)
	  {
		  //if character is a digit then if loop is true 
		  if(Character.isDigit(name.charAt(i)))
		  {
			  IntegerValue=IntegerValue+(Character.getNumericValue(name.charAt(i))*((int)Math.pow(16,name.length()-i-1)));
		  }
		  else  
		  {
			  int temp=name.charAt(i)*((int)Math.pow(16,name.length()-i-1));
			  temp=temp-'A'+10;
			  IntegerValue+=temp;
			  
			  //the inner if loop handles invalid Loader format characters
			  if(temp<10||temp>15)
			  {
				  
				  new ErrorHandler().trap(7,"Invalid loader format character",pid);
			  }
			  
		  }
	  }
	   //the decimal value is returned
	   return IntegerValue;
   }   
  
   void loadFileToMemory() throws Exception
   {
	   BufferedReader br=new BufferedReader(new FileReader(inputfilename));
	   String userFileName="";
		 String buffer=""; 
		 int dataFlag=0;
		 int countLines=0; //count lines is used to measure the length of user job and to throw a warning if it crosses certain threshold
		 ob=new ProcessControlBlock();
		 ob.pid=pid;
		 int freeFrame=-1;
		 int freeDisk=-1;
		 int counter=0;
		 int counter1=0;
		 int iteration=0;
		 int iteration1=0;
		 int forPageReference=0;
		 int initFlag=0;
		 int forDiskReference=-1;
		 userFileName=inputfilename;
		 int errorFlag=-1;
		 int finFlag=-1;
		 
		 for(int i=0;i<globalCountLines;i++)
		 {
			 br.readLine();
		 }
		 

	loop:	 
		 //4 words of loader file is set into memory at a time
			while((buffer=br.readLine())!=null)
			{
				
				countLines++;
	        	globalCountLines++;	
				String splitLine[]=buffer.split(" ");
				
				if(buffer.contains("JOB") && finFlag==0)
				{
					
					String local[]=buffer.split(" ");
					int numberofpages=HexToInt(local[1]);
					countLines=1;
					errorFlag=0;
					
					 Loader.pcb.add(ob.pid,ob);
					 new ErrorHandler().trap(12,"",ob.pid);
					 Memory.DiskOrMemory=2;
					 new Scheduler().addReadyList(ob.pid,1);
					
					
				}
					
				
			
			if(!buffer.contains("FIN"))
			{	
				
				
				//verifying whether frames are available for the job or not
				if(buffer.contains("JOB"))
				{
					String local[]=buffer.split(" ");
					int numberofpages=HexToInt(local[1]);
					
				 
					if(numberofpages<new Memory(pid).numberOfEmptyFrames())
					{
						ob=new ProcessControlBlock();
						ob.pid=pid;
						ob.loadTime=Cpu.globalClock;
						ob.outputLineLimit=HexToInt(local[2].trim());
						pid=pid+1;
						countLines=0;
						forPageReference=0;
						counter=0;
						forDiskReference=0;
						dataFlag=0;
						counter1=0;
						iteration=0;
						iteration1=0;
						initFlag=0;
						finFlag=0;
					}
					else
					{
						globalCountLines--;						
						break loop;
					}
				} 
				
			 
			 
				if(splitLine.length>1 && countLines==2) //the first if loop to get the memory Starting address and length of user job
				{
					
					if(splitLine.length==2)
					{
					
						ob.startingadd=HexToInt(splitLine[0]);
						ob.lengthofprocess=HexToInt(splitLine[1]);
						ob.instructionPages=ob.lengthofprocess/16;
						
						
						
					}
				 
				}
			 
				if(buffer.trim().length()>5 && countLines>1) //loading user job into memory array
				{
					
					if(!buffer.contains("DATA") && dataFlag==0) //if the string doesn't contain data load it into frames
					{
						int j=0; //Used to divide the line into 4 splits
					
						
						for(int i=iteration;i<iteration+4;i++)
						{
							if((splitLine[0].length())>=(j+8))
							{
							if(freeFrame==16)
							{
								
								break;
							}
							
							Memory.MEM[i+freeFrame*16]=splitLine[0].substring(j,j+8);
							
							j=j+8;
							}
					
						}
						iteration=iteration+4;

						forPageReference++;

						
				
					}
					else if(buffer.contains("DATA"))
					{
						dataFlag=1;
						forDiskReference=0;
						
						
					}
					else if(dataFlag==1 && !buffer.contains("DATA"))
					{
						int j=0; //Used to divide the line into 4 splits
					
						
						for(int i=iteration1;i<iteration1+4;i++)
						{
							if((splitLine[0].length())>=(j+8))
								
							{						
							DiskManager.Disk[i+freeDisk*16]=splitLine[0].substring(j,j+8);
							j=j+8;
							}
					
						}
						iteration1=iteration1+4;
						
						forDiskReference++;
						
					}
					
				}
				
				if(buffer.trim().length()==4)
				{
					Memory.startingAdd=HexToInt(splitLine[0]);
					
					
					ob.traceFlag=Character.getNumericValue(splitLine[1].trim().charAt(0)); //sets the traceFlag
					//LOOP used to handle the traceflag invalid character
					if(traceFlag!=0 && traceFlag!=1)
					{
					
						new ErrorHandler().trap(10,"invalid trace flag charcter"+splitLine[1].trim(),ob.pid);				  
						traceFlag=0;
					}
				}
				
				if(countLines==1 && initFlag==0)
				{
					
					freeFrame=new Memory(pid).getFreeFrame();				    
					freeDisk=new DiskManager(pid).getFreePageOnDisk();
					ob.rDataAdd=0;
					
					String frameNo=String.format("%4s",Integer.toBinaryString(freeFrame)).replace(" ","0");					
					frameNo+="000"; //used bit-5
					ob.pagetable.put(counter,frameNo);					
					ob.disktable.put(counter, String.format("%8s",Integer.toBinaryString(freeDisk)).replace(" ","0"));
					counter++;
					counter1++;
					iteration=0;
					iteration1=0;
					initFlag=1;
					
				
				}			 
				else if((forPageReference%4)==0 && countLines>4 && dataFlag==0)
				{
					freeFrame=new Memory(pid).getFreeFrame();					
					String frameNo=String.format("%4s",Integer.toBinaryString(freeFrame)).replace(" ","0");					
					frameNo+="000"; //used bit-5
					
					ob.pagetable.put(counter,frameNo);
					counter++;
					iteration=0;
					iteration1=0;
				}
				else if((forDiskReference%4)==0 && dataFlag==1 && forDiskReference>3)
				{
					freeDisk=new DiskManager(pid).getFreePageOnDisk();
					String pageNo=String.format("%8s",Integer.toBinaryString(freeDisk)).replace(" ","0");
					ob.disktable.put(counter1,pageNo);
					counter1++;
					iteration=0;
					iteration1=0;
					
				}
				
				
			}
			else if(errorFlag==-1)
			{
			
				 //if count lines is greater than 60 memory words then the user program is suspected to overflow warning is generated 
				 if(Memory.lengthOfJob>60)
				 {
					 new ErrorHandler().trap(5,"program size is too large to load into memory:Size of program in hex words ",ob.pid);
				 }
				 
				 
				 int addingOutPutLines=ob.outputLineLimit;
				 
					if(addingOutPutLines>0)
					{
					 ob.wDataAdd=(counter1-1);
					 
					 freeDisk=new DiskManager(pid).getFreePageOnDisk();
					 
					 String pageNo=String.format("%8s",Integer.toBinaryString(freeDisk)).replace(" ","0");
					 
					 ob.disktable.put(counter1,pageNo);
					}
					else
					{
						ob.wDataAdd=-1;
					}

				 Loader.pcb.add(ob.pid,ob);
				 
				 //add the process to the first ready queue 
				 new Scheduler().addReadyList(ob.pid,1);
				 

				 finFlag=1;
				 continue loop;
				 
				 }
				
			}
			 
			
			
			new Scheduler().processReadyQueue();
		 
}
   
   void spoolOut(int ProcessId) throws Exception
   {
	 // System.err.println("the spool out function "+ProcessId); 
	   
	   ProcessControlBlock pb=new ProcessControlBlock();
	   pb=pcb.get(ProcessId);
	   File output_file=new File("/home/skamire/os/phase3/execution_profile.txt");
	   PrintWriter output = new PrintWriter(new BufferedWriter(new FileWriter(output_file,true)));
	   String lineSeperator="\r\n";
	   if(pb.errorflag==1)
	   {
		   pb.termination="abnormal";
		   Memory.normaljobcompletion=Memory.normaljobcompletion+1;
	   }
	   else
	   {
		   pb.termination="normal";
	   }
	  
	   try
	   {
		   if(pb.outputFlag==0 && system.Qiteration==11)
		   {
			output.print(lineSeperator);
     		output.print(lineSeperator);
     		output.print("Process Id(DEC): ");
     		output.print(pb.pid);
     		output.print(lineSeperator);
     		output.print("Current value of clock(HEX):");
     		output.print(Integer.toHexString(Cpu.globalClock));
     		output.print(lineSeperator);
     		output.print("Number of Turns at Cpu (DECIMAL):");
     		output.print(pb.numberofcputurns);
     		output.print(lineSeperator);
     		output.print("load time (HEX):");
     		output.print(Integer.toHexString(pb.loadTime));
     		output.print(lineSeperator);
     		output.print("Termination time(HEX):");
     		output.print(Integer.toHexString(Cpu.globalClock-pb.loadTime));
     		output.print(lineSeperator);
     		output.print("Error messages and warnings: ");
     		output.print(pb.errormsg);
     		output.print(lineSeperator);
     		output.print("Nature of termination: ");
     		output.print(pb.termination);
     		output.print(lineSeperator);
     		output.print("Process Output(HEX): ");
     		output.print(pb.output);
     		output.print(lineSeperator);
     		output.print("run time (HEX) ");
     		output.print(Integer.toHexString(Cpu.globalClock-pb.enteredTime));
     		output.print(lineSeperator);
     		output.println("Execution time(HEX): ");
     		output.println(Integer.toHexString(pb.cumulativeTime));
     		output.print(lineSeperator);
     		output.println("turn around time(hex)");
     		output.print(Integer.toHexString((pb.enteredTime-pb.loadTime)));
     		executionTime=executionTime+(pb.enteredTime-pb.loadTime);
     		output.print(lineSeperator);
     		output.print("Page Fault Handling Time (hex)");
     		output.print(Integer.toHexString(pb.pFT));
     		output.print(lineSeperator);
     		output.print(lineSeperator);
     		output.flush();
     		output.close();
     		
     		
     		
     		pb.outputFlag=1;
     		Loader.pcb.set(pb.pid,pb);
		   }
	   }
	   catch(Exception e)
	   {
		   //System.out.println("Exception caught");
	   }
	   Set<Integer> keys=pb.pagetable.keySet();
	   Set<Integer> diskkey=pb.disktable.keySet();
	   for(Integer key:keys)
	   {
		   new Memory(pb.pid).freeFrame(pb.pagetable.get(key));
	   }
	   
	   for(Integer h:diskkey)
	   {
		   new DiskManager(pb.pid).freeDisk(pb.disktable.get(h)); //change here
	   }
	   if(Scheduler.firstsubqueue.contains(pid))
	   {
		   Scheduler.firstsubqueue.remove(pid);
	   }
	   else if(Scheduler.secondsubqueue.contains(pid))
	   {
		   Scheduler.secondsubqueue.remove(pid);
	   }
	   else if(Scheduler.thirdsubqueue.contains(pid))
	   {
		   Scheduler.thirdsubqueue.remove(pid);
	   }
	   else if(Scheduler.fourthsubqueue.contains(pid))
	   {
		   Scheduler.fourthsubqueue.remove(pid);
	   }
	  
		   
	  Memory.normaljobcompletion=Loader.pcb.size()-Memory.abnormaljobcompletion;
	  if(new Memory(pid).numberOfEmptyFrames()>2)
	   loadFileToMemory();
   }
   
   
   

}


/*The memory class contains three functions read,write and dump these three functions are used to update,read and output the contents of memory respectively*/


class Memory
{
	static int memStartingadd; //memStartingadd starting address of the memory where the user program is to be loaded
	
	static int lengthOfJob; //length of user job
	
	static String[] MEM=new String[256]; //memory array contains the instructions and results of user program
	
	static int startingAdd; //contains the starting address of user job from where execution is to starting
	
	
	private static boolean[] freeFrameList = new boolean[16];
	
	//static boolean[] freePageList=new boolean[256];
	
	private int pid=-1;
	
	static int DiskOrMemory=-1; //if flag=1 instruction address or else if  flag=2 then its page on disk
	
	
	static int normaljobcompletion;
	
	static int abnormaljobcompletion;
	
	
	void resetMemory() throws Exception
	{
		normaljobcompletion=0;
		abnormaljobcompletion=0;
		
		//reset freeFrameslist array
		for(int i=0;i<16;i++)
		{
			freeFrameList[i]=false;
		}
		
		//reset free page list
		for(int i=0;i<256;i++)
		{
			DiskManager.freePageList[i]=false;
		}
		
		//clear memory contents
		for(int i=0;i<256;i++)
		{
			MEM[i]="";
		}
		
		//clear disk contents
		for(int i=0;i<4096;i++)
		{
			DiskManager.Disk[i]="";
		}
	}
	
	
	
	Memory(int ProcessId) throws Exception
	{
		pid=ProcessId;
	}
    

	void unfreeFrame(int freeFrame) throws Exception
	{
		freeFrameList[freeFrame]=false;
		
	}



	void freeFrame(String s) throws Exception
	{
		int framenumber=Integer.parseInt(s.substring(0,4),2);
		freeFrameList[framenumber]=false;
		
	}

	
	//partial search on keys
	String partialKeySearch(Map<String, String> pagetable, String str) throws Exception
	{
		String entry="";
		Set<String> keys=pagetable.keySet();
		
		for(String key:keys)
		{
			if(key.length()==7)
			{
				if(key.substring(0,4).equals(str))
				{
					entry=pagetable.get(key);
					break;
				}
			}
		}
		
		if(entry==null || entry.length()<=0)
		{
			throw new Exception("partial search function has failed");
		}
		
		
		return entry;
	}
	
	
	
	
	
	
	//converts virtual address to real address
	int virtualToReal(int effectiveAddress) throws Exception
	{
		int pageNumber=effectiveAddress/16;
		int offset=effectiveAddress%16;
		String getEntry="";
		String binary="";
		binary=String.format("%4s",Integer.toBinaryString(pageNumber)).replace(" ","0");
		String getFrameNumber="";
		
		int framenumber=-1;

		ProcessControlBlock pb=new ProcessControlBlock();
		
		
			
			try
			{
				pb=Loader.pcb.get(pid);	
				getEntry=pb.pagetable.get(pageNumber);
			    
			
			
				if(getEntry.charAt(6)=='0')
				{
					
					getFrameNumber=getEntry;
					if(getFrameNumber==null)
						getFrameNumber="0000000";
				}
				else
				{
					//service page fault
					getFrameNumber=servicePageFault(getEntry); 
				}
				
			}
			catch(Exception e)
			{
				//System.out.println("Exception caught");
			}
			
			//System.out.println("pid is");
			
			
			try
			{
			framenumber=(Integer.parseInt(getFrameNumber.substring(0,4),2))*16;
			}
			catch(Exception e)
			{
				//return the value -1
			}
			
			//System.out.println(" real adddress is "+(framenumber+offset));
		return ((framenumber)+offset);
	}
	
	
	String servicePageFault(String getEntry) throws Exception
	{
		int diskframenumber=Integer.parseInt(getEntry,2);
		int getfreepage=new DiskManager(pid).getFreePageOnDisk();
		ProcessControlBlock pb=new ProcessControlBlock();
		pb=Loader.pcb.get(pid);
		String str="";
		String str1="";
		Set<Integer> keys=pb.pagetable.keySet();
		pb.pFT=pb.pFT+5;
		//ArrayList<Integer> arr=new ArrayList<Integer>();
		
		for(Integer key:keys)
		{
			if(pb.pagetable.get(key).equals(getEntry))
			{
				str=pb.pagetable.get(key);
				break;
			}
		}
		String getFrameNumber="";
		getFrameNumber=frameToBeReplaced(getEntry);
		
		for(Integer key:keys)
		{
			if(pb.pagetable.get(key).equals(getFrameNumber))
			{
				
				break;
			}
		}
		for(int i=0;i<16;i++)
		{
			DiskManager.Disk[(getfreepage*16)+i]=MEM[(Integer.parseInt(getFrameNumber,2)*16)+i];
		}
		
		
		
		for(int i=0;i<16;i++)
		{
			MEM[(Integer.parseInt(getFrameNumber,2)*16)+i]=DiskManager.Disk[(diskframenumber*16)+i];
		}
		return getFrameNumber;
	}

	String frameToBeReplaced(String entry) throws Exception
	{
		ProcessControlBlock pb=new ProcessControlBlock();
		pb=Loader.pcb.get(pid);
		String str="";
		String tempkey=" ";
		
		
		Set<Integer> keys=pb.pagetable.keySet();
		
		for(Integer key:keys)
		{
			if(pb.pagetable.get(key).equals(entry))
			{
				tempkey=str;
				break;
			}
		}
		return tempkey;
	}

	int virtualToRealDisk(int virtualoffset) throws Exception
	{
		
		String getPageNumber=Loader.pcb.get(pid).pagetable.get(virtualoffset/16);
		return (Integer.parseInt(getPageNumber)+virtualoffset%16);
		
	}
	
	//returns the free frame
	int getFreeFrame() throws Exception
	{
		int i;
		for(i=0;i<16;i++)
		{
			if(freeFrameList[i]==false)	
			{
				freeFrameList[i]=true;
				break;
			}
		}
		return i;
		
	}

	int numberOfEmptyFrames() throws Exception
	{
		int emptyFrames=0;
		for(int i=0;i<freeFrameList.length;i++)
		{
			if(freeFrameList[i]==false)
			{
				emptyFrames++;
			}
		}
		return emptyFrames;
	}
	
	
	//the reset function clears the memory array and deletes the tracefile and outputfile for next job
	void reset() throws Exception
	{
      for(int i=0;i<MEM.length;i++)
	  {
		  MEM[i]=" ";
	  }
      memStartingadd=0;
      lengthOfJob=0;
	}
 
	
    
  
	
	/* to modify a value in the memory contains two registers memory address register and memory buffer register
	  memory address register - stores the memory location to write(memAddress)
	  memory bufffer register- contains the value to be written(memValue)
	*/
	void write(int processId,int memAddr,String memValue) throws Exception
	{
		
		int memAddress=virtualToReal(memAddr);
			for(int i=0;i<4;i++)
			{
				if(i==0 && memAddress!=-1)
				{
				MEM[memAddress+i]=String.format("%8s",memValue).replace(" ","0"); //left padding zeros to hex string 
				
				}
			}

		
	}
	
	//read the contents of memory with a given address
	String read(int processId,int th) throws Exception
	{
		String str="";
		
		int memAddress=virtualToReal(th);
		
		if(memAddress==-1)
		{
			return "00000000";
		}
		for(int i=0;i<1;i++)
		{
    		str=MEM[memAddress+i];		    	
		}
			
			
	
		String.format("%32s",str).replace(" ","0");
		
		return str; //returns the location
	}
	
	
	
}



/*
The cpu class is called after the loader finishes to load the job into memory
The cpu will continue to execute the user job until a halt instruction,io instruction or an error is encounterd.
In such cases the control will be transferred to either error handler(if it is an error) or system(if the job is finished execution)
*/

class Cpu
{
	
	
	
	
	
	static int execFlag=0; //used to instruct the dump that the current job is in execution
	
	static int globalClock=0;
	
	 static int ioclock=0;//couts io clock ticks
	
	
	//converts 8 digit hex string to 32 bit binary string
	String hexToBinary(String s) throws Exception
    {
		String value="";
		int temp=0;
		for(int i=0;i<s.length();i++)
		{
			if(Character.isDigit(s.charAt(i)))
			{
				temp=Character.getNumericValue(s.charAt(i));
			    value+=String.format("%4s", Integer.toBinaryString(temp)).replace(" ", "0");
			} else 
			{
				temp=s.charAt(i)-'A'+10;
				value+=String.format("%4s", Integer.toBinaryString(temp)).replace(" ", "0");
			}
		}
		return value;
	}	
	
	//converts hex string to decimal value
	int hexToDecimal(String s) throws Exception
	{
		int value=0;
		s=s.toUpperCase();
		for(int i=0;i<s.length();i++)
		{
			if(Character.isDigit(s.charAt(i)))
			{
				value+=Character.getNumericValue(s.charAt(i));
			} 
			else 
			{
				 int temp=s.charAt(i)*((int)Math.pow(16,s.length()-i-1));
			     temp=temp-'A'+10;
			     value+=temp;
			}
		}
		return value;
		
	}
	
	
	//fetches and decodes the instruction from memory
	void execute(int pid) throws Exception
	{
		
	
		
		
	 int clock=0; //counts clock ticks
		
	
	 
	 int programCounter=0; //contains the address of next instruction to be read from memory
		
	 
	 ProcessControlBlock process=new ProcessControlBlock();
	  
	  
	  
	 process=Loader.pcb.get(pid);
	 
	 // System.out.println(" \n \n page table for pid is "+process.pid);
	  //System.err.println("The disk table for pid "+process.pid+"disk table "+process.disktable);
	  
	  String[] reg=new String[16]; //temp registers 0-15 which can be used for instruction exeution
	  
	  for(int i=0;i<16;i++)
	  {
		  reg[i]=process.preg[i];
	  }
	  
	  programCounter=process.startingadd; //program counter consists of starting address
	  
	  process.numberofcputurns=process.numberofcputurns+1;
	  
	 // System.out.println("The processId is "+process.pid+" numberofcputurns "+process.numberofcputurns+" time taken for the process "+process.cumulativeTime);
	  
	  execFlag=1;//execution flag is set
	  
	  String instruction=""; //contains the binary string of the instruction
	  
	  String str=""; //temp variable used to store the decimal opcode
	  
	  String lineSeperator="\r\n";//used to write the next line while writing into a file
	  
	  String traceFilePath="/home/skamire/os/phase3/tracefiles/";
	  
	  traceFilePath=traceFilePath+String.valueOf(process.pid);
	  
	  traceFilePath=traceFilePath+".txt";
	  
	  File traceFile=new File(traceFilePath); //contains the trace log of user job
	  
	  //PrintWriter traceLog;
	  
	  int cputurns=0;
	  
	  int timeQuantum=0;
	  
	  if(process.subqueuenumber==1)
	  {
		  cputurns=Scheduler.numberofturns;
		  timeQuantum=Scheduler.quantum;
	  }
	  else if(process.subqueuenumber>1 && process.subqueuenumber<=3)
	  {
		  cputurns=Scheduler.numberofturns+(2*(process.subqueuenumber-1));
		  timeQuantum=Scheduler.quantum;
	  }
	  else if(process.subqueuenumber==4)
	  {
		  timeQuantum=Scheduler.quantum;
	  }
	  else
	  {
		  process.subqueuenumber=1;
		  cputurns=Scheduler.numberofturns;
		  timeQuantum=cputurns*Scheduler.quantum;
		  
	  }
	  
	  int ioFlag=0;
	  
	  int haltFlag=0;
	  
	  clock=process.cumulativeTime;
	  
	
	  int pclock=process.cumulativeTime; //for storing the time of the previous instruction
	  
	  if(traceFile.exists() && process.cumulativeTime==0 && process.traceFlag==1)
	  {
		  
		  traceFile.delete();
		  traceFile.createNewFile();
		  
		 
		  
	  }
	  
	  
	 PrintWriter traceLog = new PrintWriter(new BufferedWriter(new FileWriter(traceFile, true))); //to write user trace log
	  
	  
	  
	  
	  
	
	  
	  if(process.cumulativeTime==0)
	  {
		  process.enteredTime=globalClock;
		  
	  }
	  
	  //if trace flag is set write the user job trace into trace file
	  if(process.traceFlag>=1 && process.cumulativeTime==0)
	  {
		   
		   traceLog.print("  Before Instruction Execution \t   After Instruction Execution "+lineSeperator);
		   traceLog.print("PC\tinstruction\tA\t\tA\t\t	EA"+lineSeperator);
		   traceLog.flush();
		   
	  }
	  
	  
	 try
	 {
	  
      while(true)
	  {
		
		
		str=new Memory(process.pid).read(1,programCounter).substring(0,8);  //read instruction from memory
		
		
		
		instruction=hexToBinary(str); //converting hex instruction to binary
		
	
        
       
	    /**** effective address calculation ****/		
		
		int effectiveAddress=0; //calculating the effective address
		
		effectiveAddress=Integer.parseInt(instruction.substring(16,instruction.length()),2); //if it is direct addressing
		
		//System.out.println("Effective address "+effectiveAddress+"for instruction "+str); 
		
		//indirect addressing
		if(instruction.charAt(0)=='1')
		{
			int th=Integer.parseInt(instruction.substring(16,instruction.length()),2);
			
			try
			{
			effectiveAddress=Integer.parseInt(new Memory(process.pid).read(1,th).substring(0,8),16);
			}
			catch(Exception e)
			{	
				e.printStackTrace();
			}
			
		}
		
		//index addressing
		if(Integer.parseInt(instruction.substring(12,16),2)>0)
		{
			
			effectiveAddress=effectiveAddress+Integer.parseInt(reg[(Integer.parseInt(instruction.substring(12,16),2))],16);
		}
		
		
		
		//traceLog before instruction execution
		if(process.traceFlag==1)
		{
			//System.out.println("enterd loop");
           traceLog.print(programCounter+"	");
		   traceLog.print(str+"		");

		   traceLog.print(String.format("%8s",reg[Integer.parseInt(instruction.substring(8,12),2)]).replace(" ", "")+"		");
		   
		   traceLog.flush();
   		}
		
		
		
		
		//*******opcode decoding******		
		
		switch(Integer.parseInt(instruction.substring(1,8),2))
		{
			//halt instruction: stop execution of user job and write clock info to the output file
			case 0:
				 
				 
				 clock=clock+1;
				 globalClock=globalClock+1;
				 haltFlag=1;
				
			break;
			
			//load instruction load contents of memory to register
			case 1:
	            
				reg[Integer.parseInt(instruction.substring(8,12),2)]=new Memory(process.pid).read(1,effectiveAddress).substring(0,8);
				
				//if trace flag is set write the contents to output file
				if(process.traceFlag>=1)
				{
			        traceLog.print(reg[Integer.parseInt(instruction.substring(8,12),2)]+"			");
					traceLog.print(effectiveAddress+lineSeperator);
				}
				clock=clock+1;
				globalClock=globalClock+1;
				break;
			
            //store instruction: load the contents of register into memory			
			case 2:
			
			    
				new Memory(process.pid).write(1,effectiveAddress,reg[Integer.parseInt(instruction.substring(8,12),2)]);
				//if trace flag is set write the contents to output file
				if(process.traceFlag>=1)
				{
			        traceLog.print(reg[Integer.parseInt(instruction.substring(8,12),2)]+"			");
					traceLog.print(effectiveAddress+lineSeperator);
				}
				clock=clock+1;
				globalClock=globalClock+1;
				break;
			
			
			//add :contents of register are added to a content of memory address
			case 3:
				str=new Memory(process.pid).read(1,effectiveAddress).substring(0,8);
				
				//overflow error is handled
				if((Integer.parseInt(reg[Integer.parseInt(instruction.substring(8,12),2)],16)+Integer.parseInt(str,16))>(Integer.MAX_VALUE))
				{
					new ErrorHandler().trap(6,"Addition instruction caused an over flow",process.pid);
				}
				else
				{
					reg[Integer.parseInt(instruction.substring(8,12),2)]=Integer.toHexString(Integer.parseInt(reg[Integer.parseInt(instruction.substring(8,12),2)],16)+Integer.parseInt(str,16));
				}
				
				//if trace flag is set write the contents to output file				
				if(process.traceFlag>=1)
				{
			        traceLog.print(reg[Integer.parseInt(instruction.substring(8,12),2)]+"			");
					traceLog.print(effectiveAddress+lineSeperator);
				}
				clock=clock+1;
				globalClock=globalClock+1;
				break;
			
			
			//substract:the contents of register are subtracted from content of memory location
			case 4:
				str=new Memory(process.pid).read(1,effectiveAddress).substring(0,8);
				//underflow error is handled
				if((Integer.parseInt(reg[Integer.parseInt(instruction.substring(8,12),2)],16)+Integer.parseInt(str,16))<(Integer.MIN_VALUE))
				{
					new ErrorHandler().trap(11,"Subtraction instruction caused an under flow",process.pid);
				}
				else
				{
					int ty=hexToDecimal(reg[Integer.parseInt(instruction.substring(8,12),2)])-hexToDecimal(str);
					//String binaryString="";
					reg[Integer.parseInt(instruction.substring(8,12),2)]=Integer.toString(ty,16);
					
				}
				
				
				
				//if trace flag is set write the contents to output file
				if(process.traceFlag>=1)
				{
			        traceLog.print(reg[Integer.parseInt(instruction.substring(8,12),2)]+"			");
					traceLog.print(effectiveAddress+lineSeperator);
				}
				clock=clock+1;
				globalClock=globalClock+1;
				
				break;
			
			   
			//multiply:contents of register are multiplied with content of memory address
			case 5:
				str=new Memory(process.pid).read(1,effectiveAddress).substring(0,8);
			    
			reg[Integer.parseInt(instruction.substring(8,12),2)]=Integer.toString(Integer.parseInt(reg[Integer.parseInt(instruction.substring(8,12),2)],16)*Integer.parseInt(str,16),16);
				
				
				//if trace flag is set write the contents to output file
				if(process.traceFlag>=1)
				{
			        traceLog.print(reg[Integer.parseInt(instruction.substring(8,12),2)]+"			");
					traceLog.print(effectiveAddress+lineSeperator);
				}
				clock=clock+2;
				globalClock=globalClock+2;
				break;
			
			//divide:contents of register are divided with content of memory address
			case 6:
			   
			    str=new Memory(process.pid).read(1,effectiveAddress).substring(0,8);
				
				//divide by zero error is handled
				if(Integer.parseInt(str,16)==0)
				{
					new ErrorHandler().trap(8,"Divide by Zero error",process.pid);
				}
				else
				{					
				 reg[Integer.parseInt(instruction.substring(8,12),2)]=Integer.toHexString(Integer.parseInt(reg[Integer.parseInt(instruction.substring(8,12),2)],16)/Integer.parseInt(str,16));
				} 
				//if trace flag is set write the contents to output file
				if(process.traceFlag>=1)
				{
			        traceLog.print(reg[Integer.parseInt(instruction.substring(8,12),2)]+"			");
					traceLog.print(effectiveAddress+lineSeperator);
				}
				clock=clock+2;
				globalClock=globalClock+2;
				break;
			
			//shift left:register contents are shifted left by number of digits
			case 7:
			
				
				int t=(Integer.parseInt(reg[Integer.parseInt(instruction.substring(8,12),2)],16));
				str=new Memory(process.pid).read(1,effectiveAddress).substring(0,8);
				reg[Integer.parseInt(instruction.substring(8,12),2)]=String.valueOf(t<<(Integer.parseInt(str,16)));
				
				//if trace flag is set write the contents to output file
				if(process.traceFlag>=1)
				{
			        traceLog.print(reg[Integer.parseInt(instruction.substring(8,12),2)]+"			");
					traceLog.print(effectiveAddress+lineSeperator);
				}
				clock=clock+1;
				globalClock=globalClock+1;
				break;
			
			
			//shift right:register contents are shifted right
			case 8:
			
				int te=(Integer.parseInt(reg[Integer.parseInt(instruction.substring(8,12),2)]));
				str=new Memory(process.pid).read(1,effectiveAddress).substring(0,8);
				reg[Integer.parseInt(instruction.substring(8,12),2)]=String.valueOf(te>>(Integer.parseInt(str,16)));
				
				if(process.traceFlag>=1)
				{
			        traceLog.print(reg[Integer.parseInt(instruction.substring(8,12),2)]+"			");
					traceLog.print(effectiveAddress+lineSeperator);
				}
				clock=clock+1;
				globalClock=globalClock+1;
				break;
			
			
			//branch on minus:if register contents are less than zero then branching happens
			
			case 9:
			    
				if(Long.parseLong(reg[Integer.parseInt(instruction.substring(8,12),2)],16)<0l)
				{
					programCounter=effectiveAddress-1;
					
				}
				//if trace flag is set write the contents to output file
				if(process.traceFlag>=1)
				{
			        traceLog.print(reg[Integer.parseInt(instruction.substring(8,12),2)]+"			");
					traceLog.print((effectiveAddress+1)+lineSeperator);
				}
				clock=clock+1;
				globalClock=globalClock+1;
				break;
			
			
			//branch on plus:if register contents are greater zero then branching happens
			case 10:
			    
			   if(Integer.parseInt(reg[Integer.parseInt(instruction.substring(8,12),2)],16)>0)
				{
					programCounter=effectiveAddress-1;
					
				}
				//if trace flag is set write the contents to output file
				if(process.traceFlag>=1)
				{
			        traceLog.print(reg[Integer.parseInt(instruction.substring(8,12),2)]+"			");
					traceLog.print((effectiveAddress+1)+lineSeperator);
				}
				clock=clock+1;
				globalClock=globalClock+1;
				break;
			
			
			//branch on zero:if content of register is equal to zero then branching happens
			case 11:
			    
			   if(Integer.parseInt(reg[Integer.parseInt(instruction.substring(8,12),2)],16)==0)
				{
					programCounter=effectiveAddress-1;
					
				}
				//if trace flag is set write the contents to output file
				if(process.traceFlag>=1)
				{
			        traceLog.print(reg[Integer.parseInt(instruction.substring(8,12),2)]+"			");
					traceLog.print((effectiveAddress+1)+lineSeperator);
				}
				clock=clock+1;
				globalClock=globalClock+1;
			   break;	
			
			
			//branch and link:link register is used for branching
			case 12:
			
				reg[Integer.parseInt(instruction.substring(8,12),2)]=Integer.toHexString(programCounter);
				programCounter=effectiveAddress-1;
				
				if(process.traceFlag>=1)
				{
			        traceLog.print(reg[Integer.parseInt(instruction.substring(8,12),2)]+"			");
					traceLog.print((effectiveAddress+1)+lineSeperator);
				}
				clock=clock+2;
				globalClock=globalClock+2;
				break;
			
			
			//and:operand 1:contents of register operand 2:contents of memory address
			case 13:
			
				str=new Memory(process.pid).read(1,effectiveAddress).substring(0,8);
                reg[Integer.parseInt(instruction.substring(8,12),2)]=Integer.toHexString(Integer.parseInt(reg[Integer.parseInt(instruction.substring(8,12),2)],16)&(Integer.parseInt(str,16)));				
				
				if(process.traceFlag>=1)
				{
			        traceLog.print(reg[Integer.parseInt(instruction.substring(8,12),2)]+"			");
					traceLog.print((effectiveAddress)+lineSeperator);
				}
				clock=clock+1;
				globalClock=globalClock+1;
				break;
			
			
			//or operand 1:contents of register operand 2:contents of memory address
			case 14:
			
			   //int input=new system().inputSystem();
			    str=new Memory(process.pid).read(1,effectiveAddress).substring(0,8);
			    reg[Integer.parseInt(instruction.substring(8,12),2)]=Integer.toHexString(Integer.parseInt(reg[Integer.parseInt(instruction.substring(8,12),2)],16)|(Integer.parseInt(str,16)));				
				if(process.traceFlag>=1)
				{
			        traceLog.print(reg[Integer.parseInt(instruction.substring(8,12),2)]+"			");
					traceLog.print((effectiveAddress)+lineSeperator);
				}
				clock=clock+1;
				globalClock=globalClock+1;
				break;
			   
			
			
			//read:from stdin
			case 15:
			
				String st=new system(process.pid).input(process.pid);
				
				//if(process.pid==4)
				//System.err.println("read string is "+st);
				
				int j=0;
				st=String.format("%32s", st).replace(" ", "0");
				
				//memory write ea-ea+3
				for(int i=0;i<4;i++)
				{
				if((j+8)<=st.length())
				{
				 new Memory(process.pid).write(pid,effectiveAddress+i,st.substring(j, j+8));
				}
				 j=j+8;
				}
				//if the record is read
				ioFlag=1;
				if(process.traceFlag>=1)
				{
			        traceLog.print(reg[Integer.parseInt(instruction.substring(8,12),2)]+"			");
					traceLog.print((effectiveAddress)+lineSeperator);
				}
				clock=clock+10;
				ioclock=ioclock+10;
				globalClock=globalClock+10;
				break;
			
			
			//write:write to stdout
			
			case 16:
				
				String q="";
				for(int i=0;i<4;i++)
				{
					q+=new Memory(process.pid).read(pid,effectiveAddress+i);
				}	
					
				new system(process.pid).Output(q);
				
				
				if(process.traceFlag>=1)
				{
			        traceLog.print(reg[Integer.parseInt(instruction.substring(8,12),2)]+"			");
					traceLog.print((effectiveAddress)+lineSeperator);
				}
				clock=clock+10;
				ioclock=ioclock+10;
				globalClock=globalClock+10;
				ioFlag=1;
				break;
				//memory read ea-ea+3
			
			
			//dump;dump the contents of memory array to output file
			case 17:
			
				
				haltFlag=1;
				clock+=1;
				globalClock=globalClock+1;
				break;
			
			default:
			    
				 //call error handler
				 
				 new ErrorHandler().trap(1,"illegal opcode while decoding instruction "+Integer.parseInt(instruction.substring(1,8),2),process.pid);
				 break;
			   
		}
		
		
		
		
        programCounter=programCounter+1;
        //process.cumulativeTime=clock;
        timeQuantum=timeQuantum-(clock-pclock);
	         
                //infinite loop handled
		if(process.cumulativeTime>500)
      		{
      			new ErrorHandler().trap(4, "Infinite loop suspected", process.pid);
      			haltFlag=1;
      		}
       
        
        if(Scheduler.fourthsubqueue.size()>0)
		{
          
		  new Scheduler().trackIoQueue(clock-pclock);	
		}
		
      	pclock=clock;
        
        
      //time quantum has expired
      	if(timeQuantum<=0)
      	{
      		//add the process back to ready queue
      		
      		//System.out.println("The time quantum expired loop");
      		process.startingadd=programCounter;
      		clock=clock-process.cumulativeTime;
      		process.cumulativeTime+=clock;
      		
      		
      		
      		for(int i=0;i<16;i++)
      		{
      			process.preg[i]=reg[i]; //saving the state of the process the registers
      			
      		}
      		
      		Loader.pcb.set(process.pid, process);
      		
      		if(process.numberofcputurns>cputurns)
      		{
      			if(process.subqueuenumber<=3)
      			{
      			
      		     new Scheduler().addReadyList(pid,process.subqueuenumber+1);
      		     process.subqueuenumber=process.subqueuenumber+1;
      		     system.numberofmigrations=system.numberofmigrations+1;
      		  
      			}
      			else if(process.subqueuenumber==4)
      			{
      				new Scheduler().addReadyList(pid,process.subqueuenumber);
      			}
      		}
      		else
      		{
      			new Scheduler().addReadyList(pid, process.subqueuenumber);
      		}
      		Loader.pcb.set(process.pid, process);
      		break;
      	}
      	
      	//if io flag is set then take out the process and add it to the input queue
      	
      	if(ioFlag==1)
      	{
      	//add the process back to block queue
      		process.startingadd=programCounter;
      		clock=clock-process.cumulativeTime;
      		process.cumulativeTime+=clock;
      		for(int i=0;i<16;i++)
      		{
      			process.preg[i]=reg[i]; //saving the state of the process the registers
      			
      		}
      		if(process.subqueuenumber==4)
      		{
      		process.subqueuenumber=1;
      		process.numberofcputurns=0;
      		new Scheduler().addReadyList(process.pid,1);
      		system.numberofmigrations=system.numberofmigrations+1;
      		}
      		else if(process.subqueuenumber<=3)
      		{
      			//process.numberofcputurns=0;
      			if(process.numberofcputurns>cputurns && process.subqueuenumber<=3)
      			{
      				process.subqueuenumber=process.subqueuenumber+1;
      			}
      			
      			new Scheduler().addReadyList(process.pid,process.subqueuenumber);
      			system.numberofmigrations=system.numberofmigrations+1;
      		}
      		Loader.pcb.set(process.pid,process);
      		break;
      		
      	}
      	
      	
      	if(haltFlag==1)
      	{
      		clock=clock-process.cumulativeTime;
      		process.cumulativeTime+=clock;      		
      		new Loader().spoolOut(process.pid);   		     		
      		break;
      	}
      	
      	
		traceLog.flush();
		
		
		
	   }
	 }//try block
     
     catch(Exception e)
     {
		 new ErrorHandler().trap(3,"Over Flow error",process.pid);
	 }
     
     finally
	 {
		
		 
		          execFlag=0;
				  traceLog.flush();
		          traceLog.close();
		          if(traceFile.exists() && process.traceFlag!=1)
		          {
		        	  traceFile.delete();
		          }
		         
		 
	 }	 


		
	}
}



class system
{
  
  
  private static File output_file=new File("/home/skamire/os/phase3/execution_profile.txt"); //output file
  File output_queue=new File("/home/skamire/os/phase3/MLFBQ.txt");
  private int pid=-1;
  static int numberofmigrations;
  static File stat_matrix=new File("/home/skamire/os/phase3/StatisticsMatrix.txt");
  PrintWriter output_matrix=new PrintWriter(new BufferedWriter(new FileWriter(stat_matrix,true)));
  static int Qiteration;
  int[] mat=new int[12];
  
  system(int ProcessId) throws Exception
  {
	  pid=ProcessId;
  }
  
  int getCorrectPcb(int pid) throws Exception
  {
	  int i=0;
	  for(i=0;i<Loader.pcb.size();i++)
	  {
		  if(Loader.pcb.get(i).pid==pid)
		  {			 
			  break;
		  }
	  }
	  return i;
  }
 
  //takes input from stdin
  String input(int pid) throws Exception
  {
	  
	  ProcessControlBlock ob=new ProcessControlBlock();
	  
	  ob= Loader.pcb.get(pid);
	  String str=new DiskManager(ob.pid).accessDisk(pid,ob.rDataAdd);
	 
	  
	  ob.rDataAdd=ob.rDataAdd+4;
	  Loader.pcb.set(ob.pid,ob);

	  return str;
  }
  
  //takes input filename of user job
  void inputJobFile(String args) throws Exception
  {
	
	String filePath=""; 
	  filePath=filePath+args;
	  filePath=filePath;
	  Loader.inputfilename=filePath;
	  int[] cputurns={3,4,5};
	  int[] quantum={35,40,45,50};
	  Scheduler.recursionunwind=2;
	  
	 	  
	  for(int i=0;i<cputurns.length;i++)
	  {
		  
		  for(int j=0;j<quantum.length;j++)
		  {
			  
			
			  
			  
			  Scheduler.numberofturns=cputurns[i];
			  Scheduler.quantum=quantum[j];
			  
			  //clearing loader contents
			   Loader.pcb.clear();  
			   Loader.globalCountLines=0;
			   Loader.pid=0;
			   
			   //reset memory contents
			   new Memory(-1).resetMemory();
			   if(output_file.exists())
			    {
			    	output_file.delete();
			    	output_file.createNewFile();
			    }
			   if(output_queue.exists())
			   {
				   output_queue.delete();
				   output_queue.createNewFile();
				   
			   }
			   
			   //rest cpu clocks
			   Cpu.globalClock=0;
			   Cpu.ioclock=0;
			   
			   //clearing scheduler queues
			   Scheduler.firstsubqueue.clear();
			   Scheduler.secondsubqueue.clear();
			   Scheduler.thirdsubqueue.clear();
			   Scheduler.fourthsubqueue.clear();
			   Scheduler.index=1;
			   Scheduler.queucontents=1;
			   
			   new Scheduler().scheduleProcess(filePath);
			   
				  output_matrix.print(numberofmigrations+"     ");
				  numberofmigrations=0;
				  Qiteration+=1;
		  }
		  
	  }

 	   for(int i=0;i<quantum.length;i++)
	  {
		  output_matrix.print("     "+quantum[i]);
		 
	  }
	  output_matrix.print("\r\n");
	  for(int i=0;i<12;i++)
	  {
		  if((i%4)==0 )
		  {
			  output_matrix.print(cputurns[i/4]+"    ");
			  output_matrix.print("\r\n");
		  }	  
		 
		  output_matrix.print(mat[i]+"		");
		  
		 
	  }
	  output_matrix.print("\r\n");
	  //output_matrix.flush();
	  
	  
	  
	  output_matrix.flush();
	  output_matrix.close();
	

          
	  output_matrix.flush();
	  output_matrix.close();
	
	
	  System.exit(0);
	  
	 
	
  }
  
  //prints output to stdout
  void Output(String str) throws Exception
  {
	  ProcessControlBlock pb=new ProcessControlBlock();
	  pb=Loader.pcb.get(pid);
	  if(str==null)
	  {
		  str="00000000";
		  str+=str;
	  }
	  pb.output=pb.output+str;
	  pb.output+="\n";
	  int j=0;
	  for(int i=0;i<4;i++)
	  {
		 
		 DiskManager.Disk[(pb.wDataAdd*16)+i]=str.substring(j,j+8);
		 j=j+8;
		 
		
	  }
	  pb.wDataAdd=pb.wDataAdd+4;
	  pb.pFT=pb.pFT+10;
	  Loader.pcb.set(pb.pid, pb);
	
  }
  
 
  public static void main(String args[]) throws Exception
  {
	  //File output_file=new File();
    if(output_file.exists())
    {
    	output_file.delete();
    	output_file.createNewFile();
    }
    if(stat_matrix.exists())
    {
    	stat_matrix.delete();
    	stat_matrix.createNewFile();
    }

   new system(-1).inputJobFile(args[0]);
  }
}





/*contains the traps for errors and warnings*/

class ErrorHandler
{
	//private File output_file=new File("C:\\Users\\Saicharan\\Desktop\\execution_profile.txt"); //output file
	
	//trap contains the error handling messages
	
	void trap(int errorCode,String errorMsg,int pid) throws Exception
	{
		
		ProcessControlBlock pb=new ProcessControlBlock();
		pb=Loader.pcb.get(pid);
	
    try
	{	
		
		
		int errorFlag=0;
		switch(errorCode)
		{
			//illegal opcode trap
			case 1:
			pb.errormsg="decoding time error";
			pb.errormsg+=errorMsg;
			
			
			pb.errorflag=1;
			break;
			
			//memory address fault
			case 2:
			//output.print("\r\n");
			pb.errormsg="memory reference error";
			pb.errormsg+=errorMsg;
			
			pb.errorflag=1;
			break;
			
			//invalid input
  			case 3:
			
			pb.errorflag=2;
			break;
			
			//suspected infinite job
			case 4:
				pb.errormsg+=" ";
			    pb.errorflag=1;
			break;
			
			//program size too large
			case 5:
			pb.errormsg="load time warning";
			pb.errormsg+=errorMsg;
			
			break;
			
			//overflow
			case 6:
			
			pb.errormsg="execution time error";
			pb.errormsg+=errorMsg;
			
			pb.errorflag=1;
			break;
			
			//invalid loader format character
			case 7:
				pb.errormsg="load time error";
				pb.errormsg+=errorMsg;
				
				pb.errorflag=1;
			break;
			
			//divide by zero
			case 8:
				pb.errormsg="execution time error";
				pb.errormsg+=errorMsg;
				
				pb.errorflag=1;
			break;
			
			//address out of range
			case 9:
				pb.errormsg="Memory reference";
				pb.errormsg+=errorMsg;
				
				pb.errorflag=1;
			break;
			
			//invalid trace flag
			case 10:
				pb.errormsg="load time warning";
				pb.errormsg+=errorMsg;
				
			
			break;
			
			//under flow
			case 11:
			pb.errormsg="execution time error";
			pb.errormsg+=errorMsg;
			
			pb.errorflag=1;
			break;
			
			//**fin record missing
			case 12:
			pb.errormsg="fin record missing";
			pb.errormsg+=errorMsg;
			break;
			
			//**data record missing
			case 13:
				pb.errormsg="data record missing";
				pb.errormsg+=errorMsg;
				break;
				
			//missing job
			case 14:
				pb.errormsg="Missing job record";
				pb.errormsg+=errorMsg;
				pb.errorflag=1;
				break;
				
			//insufficient ouput space
			case 15:
				pb.errormsg="Insufficient output space";
				pb.errormsg+=errorMsg;
				pb.errorflag=1;
				break;
			//double data
			case 16:
				pb.errormsg="Double data record";
				pb.errormsg+=errorMsg;
				pb.errorflag=1;
				break;
				
						
			default:
			
			break;
			
			  
		}	
		
		Loader.pcb.set(pb.pid, pb);
		if(pb.errorflag>=1)
		{
			new Loader().spoolOut(pb.pid);
		}
		
			   	 
				 
				 if(errorFlag==1)
				{
			     
				errorFlag=0;
				
				//new system().inputJobFile();
				} 
				 
					 
	}
		
	catch(Exception e)
	{
		
	 e.printStackTrace();
	}
	
	
 }
	
}

class ProcessControlBlock
{
	int traceFlag;
	Map<Integer,String> pagetable = new HashMap<Integer,String>();     //page table
	Map<Integer,String> disktable=new HashMap<Integer,String>();       //Disk Table
	int startingadd;
	int lengthofprocess;
	String[] preg=new String[16]; //process register
	int enteredTime=0;
	int cumulativeTime=0;
	int pid;
	int rDataAdd;
	int wDataAdd;
	int ioTime;
	int numberOfPages; //both data and instructions
	int outputLineLimit;
	int instructionPages; //contains only instruction pages
	int ioWaitTime=10;
	int errorflag=0;
	String output="";
	int outputFlag=0;
	int loadTime;
	String termination="";
	String errormsg="";
	int numberofcputurns=0;
	int subqueuenumber=-1;
	int pFT=0;
	
}

class Scheduler
{
	//static LinkedList<Integer> readyQueue=new LinkedList<Integer>();
	//static LinkedList<Integer> ioQueue=new LinkedList<Integer>();
	
	//The quantum and number of cpu turns will change at every iteration
	static int quantum;
	static int numberofturns;
	static int recursionunwind;
	static int index;
	static int queucontents;
	
	//different levels of multi level feed back queues
	static LinkedList<Integer> firstsubqueue=new LinkedList<Integer>();
	static LinkedList<Integer> secondsubqueue=new LinkedList<Integer>();
	static LinkedList<Integer> thirdsubqueue=new LinkedList<Integer>();
	static LinkedList<Integer> fourthsubqueue=new LinkedList<Integer>();
	
	//average multi level queue size
	static List<Integer> firstqSize=new ArrayList<Integer>();
	static List<Integer> secondqSize=new ArrayList<Integer>();
	static List<Integer> thirdqSize=new ArrayList<Integer>();
	static List<Integer> fourthqSize=new ArrayList<Integer>();
	
	
	
	
	
	void scheduleProcess(String jobFileName) throws Exception
	{
		
		new Loader().loadFileToMemory(); //load the files into memory		
		
	}
	
	

	

	void trackIoQueue(int time) throws Exception
	{
	   ProcessControlBlock ob=new ProcessControlBlock();
	  
		for(int i=0;i<fourthsubqueue.size();i++)
	   {
		   if(i<Loader.pcb.size())
		   {
			   
			   for(int j=0;j<Loader.pcb.size();j++)
			   {
				   
				   if(Loader.pcb.get(j).pid==fourthsubqueue.get(i)) 
				   {
					   
					   ob=Loader.pcb.get(j);
					   break;
				   }
			   }
		   
		  
		   ob.ioWaitTime=ob.ioWaitTime-time;
		   
		   //add process to the ready queue
		   if(ob.ioWaitTime<=0)
		   {
			 
			   ob.ioWaitTime=10;
			   fourthsubqueue.poll();
			   addReadyList(ob.pid,1);
			   system.numberofmigrations=system.numberofmigrations+1;
		   }
		   
		    Loader.pcb.set(ob.pid,ob);
		   }
		   
	   }
		
	}

	void processReadyQueue() throws Exception
	{
		 File output_file=new File("/home/skamire/os/phase3/execution_profile.txt");
		   PrintWriter output = new PrintWriter(new BufferedWriter(new FileWriter(output_file,true)));
		   File output_queue=new File("/home/skamire/os/phase3/MLFBQ.txt");
		   PrintWriter outputQueueContents=new PrintWriter(new BufferedWriter(new FileWriter(output_queue,true)));
		   
		
		while((!firstsubqueue.isEmpty() || !secondsubqueue.isEmpty() || !thirdsubqueue.isEmpty() || !fourthsubqueue.isEmpty()))
		{
			
			if(!firstsubqueue.isEmpty())
			{
				new Cpu().execute(firstsubqueue.pollFirst());
			}
			else if(!secondsubqueue.isEmpty())
			{
				new Cpu().execute(secondsubqueue.pollFirst());
			}
			else if(!thirdsubqueue.isEmpty())
			{
				new Cpu().execute(thirdsubqueue.pollFirst());
			}
			else if(!fourthsubqueue.isEmpty())
			{
					new Cpu().execute(fourthsubqueue.pollFirst());
			}
			
			

			if((Cpu.globalClock/500)==index)
			{
				firstqSize.add(new Integer(firstsubqueue.size()));
				secondqSize.add(new Integer(secondsubqueue.size()));
				thirdqSize.add(new Integer(thirdsubqueue.size()));
				fourthqSize.add(new Integer(fourthsubqueue.size()));
				
			}
			
			
			
			
			
						
			if((Cpu.globalClock/500)==index && system.Qiteration==11)
			{
				output.print("Contents of memory (HEX)");
				for(int i=0;i<256;i++)
				{
					if((i%8)==0)
					{
						output.print("\r\n");
						output.print(String.format("%8s", Integer.toHexString(i)).replace(" ", "0")+" ");
					}
					output.print(Memory.MEM[i]+" ");
					
				}
				
				output.print("\r\n");
				
				output.print("The current job in execution is(DECIMAL PID): ");
				if(!firstsubqueue.isEmpty())
					output.print(firstsubqueue.getFirst());
				else if(!secondsubqueue.isEmpty())
					output.print(secondsubqueue.getFirst());
				else if(!thirdsubqueue.isEmpty())
					output.print(thirdsubqueue.getFirst());
				else if(!fourthsubqueue.isEmpty())
					output.print(fourthsubqueue.getFirst());
				output.print("\r\n");
				
				//printing the multi-level queue contents
				output.print("first level queue contents(DECIMAL)"+"\r\n");
				
				for(int g:firstsubqueue)
				output.print(g+"\r\n");
				
				//second level
				output.print("second level queue contents(DECIMAL)"+"\r\n");
				
				for(int g:secondsubqueue)
				output.print(g+"\r\n");
				
				//third level
				output.print("third level queue contents(DECIMAL)"+"\r\n");
				
				for(int g:thirdsubqueue)
				output.print(g+"\r\n");
				
				//fourth level
				output.print("fourth level queue contents(DECIMAL)"+"\r\n");
				
				for(int g:fourthsubqueue)
				output.print(g+"\r\n");
				
				output.print("Degree of multiprogramming(DECIMAL):");
				output.print(firstsubqueue.size()+secondsubqueue.size()+thirdsubqueue.size()+fourthsubqueue.size());
				output.print("\r\n");
				output.print("diskutilization percentage:(DECIMAL)");
				output.print(diskutilization());
				output.print("\r\n");
				output.flush();
				index=index+1;
				
			}
			
			if((Cpu.globalClock/1200)==queucontents)
			{
				
				//first level queue contents
				outputQueueContents.print("First Level Queue contents (DECIMAL)"+"\r\n");
				
				for(int g:firstsubqueue)
					outputQueueContents.print(g+"\r\n");
				
				//second level queue contents
				outputQueueContents.print("Second Level Queue contents (DECIMAL)"+"\r\n");
				
				for(int g:secondsubqueue)
					outputQueueContents.print(g+"\r\n");
				
				//third level queue contents
				outputQueueContents.print("Third Level Queue contents (DECIMAL)"+"\r\n");
				
				for(int g:thirdsubqueue)
					outputQueueContents.print(g+"\r\n");
				
				//fourth level queue contents
				outputQueueContents.print("Fourth Level Queue contents (DECIMAL)"+"\r\n");
				
				for(int g:fourthsubqueue)
					outputQueueContents.print(g+"\r\n");
				
				outputQueueContents.print("\r\n");
				outputQueueContents.print("\r\n");
				outputQueueContents.flush();
				queucontents=queucontents+1;
				
			}

		}
		

		if(system.Qiteration==11 && recursionunwind==2)
		{	
		//termination statistics
		output.print("TERMINATION STATISTICS ");
		output.print("\r\n");
		output.println("---------------------");
		output.print("\r\n");
		
		output.print("Clock value:(HEX)"+Integer.toHexString(Cpu.globalClock));
		output.print("\r\n");		
		output.print("mean turn around time:(HEX)");
		output.print(meanturnaroundtime());
		output.print("\r\n");
		output.print("mean run time:(HEX)");
		output.print(meanexecutiontime());
		output.print("\r\n");
		output.print("Mean Page Fault Time:(HEX)");
		output.print(meanpagefaulttime());
		output.print("\r\n");
		output.print("Mean Io time :(HEX)");
		output.print(Integer.toHexString(Cpu.ioclock/Loader.pcb.size()));
		output.print("\r\n");
		output.print("Mean Execution Time:(HEX)");
		output.print(meanruntime());
		output.print("\r\n");
		output.print("Cpu idle time:(Hex)");
		output.print("0");
		output.println("\r\n");
		output.print("total time lost in abnormal jobs:DECIMAL");
		output.print(timeLost());
		output.println("\r\n");
		output.print("Total time taken to suspect infinite jobs:(HEX)");
		output.print(timeLostInfiniteJobs());
		output.print("\r\n");
		output.print("total number of jobs terminated normally: (DECIMAL)");
		output.print(Memory.normaljobcompletion-Memory.abnormaljobcompletion);
		output.println("\r\n");
		output.print("total number of jobs terminated abnormally:(DECIMAL)");
		output.print(Memory.abnormaljobcompletion);
		output.println("\r\n");
		output.print("disk utilization percentage: (DECIMAL) ");
		output.print(diskutilization());
		output.print("\r\n");
		output.print("Total Page faults handled: (HEX)");
		output.print(pagefaultstotal());
		output.print("\r\n");
		output.print("Maximum number of jobs in first sub queue:(DECIMAL) ");
		output.print(Collections.max(firstqSize));
		output.print("\r\n");
		output.print("average number of jobs in first sub queue:(DECIMAL) ");
		output.print(averageQueueSize(firstqSize).substring(0,4));
		output.print("\r\n");
		output.print("Maximum number of jobs in second sub queue:(DECIMAL) ");
		output.print(Collections.max(secondqSize));
		output.print("\r\n");
		output.print("average number of jobs in second sub queue:(DECIMAL) ");
		output.print(averageQueueSize(secondqSize).substring(0,4));
		output.print("\r\n");
		output.print("Maximum number of jobs in third sub queue:(DECIMAL) ");
		output.print(Collections.max(thirdqSize));
		output.print("\r\n");
		output.print("average number of jobs in third sub queue:(DECIMAL) ");
		output.print(averageQueueSize(thirdqSize).substring(0,4));
		output.print("\r\n");
		output.print("Maximum number of jobs in fourth sub queue:(DECIMAL) ");
		output.print(Collections.max(fourthqSize));
		output.print("\r\n");
		output.print("average number of jobs in fourth sub queue:(DECIMAL) ");
		output.print(averageQueueSize(fourthqSize).substring(0,4));
		output.print("\r\n");
		
		
		output.flush();
		output.close();
		recursionunwind=1;
		}
		
		//System.exit(0);
	}
	
		
	private String averageQueueSize(List <Integer> queue)
	{
		  Integer sum = 0;
		  if(!queue.isEmpty())
		  {
		    for (Integer mark : queue) {
		        sum += mark;
		    }
		    return Double.toString(sum.doubleValue() / queue.size());
		  }
		  return Integer.toString(sum);
	}
	






	String pagefaultstotal() throws Exception
	{
		int meanpagefaulttime=0;
		for(int i=0;i<Loader.pcb.size();i++)
		{
			meanpagefaulttime=meanpagefaulttime+Loader.pcb.get(i).pFT;
		}
		
		return Integer.toHexString(meanpagefaulttime/10);
	}





	private String meanpagefaulttime()
	{
		int meanpagefaulttime=0;
		for(int i=0;i<Loader.pcb.size();i++)
		{
			meanpagefaulttime=meanpagefaulttime+Loader.pcb.get(i).pFT;
		}
		
		return Integer.toHexString(meanpagefaulttime/Loader.pcb.size());
		
		
	}

     String timeLostInfiniteJobs()
     {
    	 int timelost=0;
 		for(int i=0;i<Loader.pcb.size();i++)
 		{
 			if(Loader.pcb.get(i).errorflag==1 && Memory.DiskOrMemory==2)
 			{
 		    timelost=timelost+Loader.pcb.get(i).cumulativeTime;
 			Memory.abnormaljobcompletion++;
 			
 			}
 		}
 		
 		return Integer.toHexString((timelost/3)/Loader.pcb.size());
     }



	String timeLost() throws Exception 
	{
		int meantime=0;
		for(int i=0;i<Loader.pcb.size();i++)
		{
			if(Loader.pcb.get(i).errorflag==1 && Memory.DiskOrMemory==2)
			{
			meantime=meantime+Loader.pcb.get(i).cumulativeTime;
			Memory.abnormaljobcompletion++;
			
			}
		}
		
		return Integer.toHexString(meantime/Loader.pcb.size());
	
		
	}

	int diskutilization()
	{
		int number=0;
		for(int i=0;i<256;i++)
		{
			if(DiskManager.freePageList[i]==true)
			{
				number++;
			}
		}
		return (number%256);
	}
	
	String meanexecutiontime()
	{
		return Integer.toHexString((Loader.executionTime/2)/Loader.pcb.size());
	}

	String meanruntime() throws Exception 
	{
		int meantime=0;
		for(int i=0;i<Loader.pcb.size();i++)
		{
			meantime=meantime+Loader.pcb.get(i).cumulativeTime;
		}
		
		return Integer.toHexString(meantime/Loader.pcb.size());
	
		
	}

	String meanturnaroundtime() throws Exception
	{
		int meantime=0;
		for(int i=0;i<Loader.pcb.size();i++)
		{
			meantime=meantime+Loader.pcb.get(i).enteredTime-Loader.pcb.get(i).loadTime;
		}
		
		return Integer.toHexString(meantime/Loader.pcb.size());
	}

	void addReadyList(int pid,int queueNumber) throws Exception
	{
		
		ProcessControlBlock ob=new ProcessControlBlock();
		ob=Loader.pcb.get(pid);
		
		switch(queueNumber)
		{
		
		case 1:
			ob.subqueuenumber=1;
			Loader.pcb.set(pid, ob);
			firstsubqueue.add(new Integer(pid));
			break;
			
		case 2:
			ob.subqueuenumber=2;
			Loader.pcb.set(pid, ob);
			secondsubqueue.add(new Integer(pid));
			system.numberofmigrations++;			
			break;
			
		case 3:
			ob.subqueuenumber=3;
			Loader.pcb.set(pid, ob);
			thirdsubqueue.add(new Integer(pid));
			system.numberofmigrations++;
			
			break;
		case 4:
			ob.subqueuenumber=4;
			ob.ioWaitTime=9*numberofturns*quantum;
			Loader.pcb.set(pid, ob);
			fourthsubqueue.add(new Integer(pid));
			system.numberofmigrations++;
			break;
			
		default:
			System.out.println("invalid queue number");
			break;
		}
		
		
	}
	
	
	
	
}





