
public class DiskManager 
{
	static String[] Disk=new String[4096]; //disk array
	private int pid=-1;
	static boolean[] freePageList=new boolean[256]; //free page list on disk
	
	
	//constructor to initialize process id
	DiskManager(int ProcessId) throws Exception
	{
		pid=ProcessId;
	}
	
	//frees the disk if occupied
	void freeDisk(String s) throws Exception
	{
	    int disknumber=Integer.parseInt(s,2);
	    freePageList[disknumber]=false;
		
	}
	
	//returns the free page number on disk
	int getFreePageOnDisk() throws Exception
	{
		int i;
		for(i=0;i<256;i++)
		{
			if(freePageList[i]==false)	
			{
				freePageList[i]=true;
				break;
			}
		}
		return i;
		
	}
    
	//reading the disk
	String accessDisk(int pid,int diskaddress) throws Exception
	{
		String str="";
		int diskNumber=diskaddress/16;
		int diskOffset=diskaddress%16;
		ProcessControlBlock pb=new ProcessControlBlock();		
		pb=Loader.pcb.get(pid);
		
		String diskEntry=pb.disktable.get(diskNumber);
		int pageNumber=Integer.parseInt(diskEntry,2);
		
		
		if(pageNumber>256)
		{
			System.out.println("The pageNumber error"+pageNumber);
		}
		int dAddress=(pageNumber*16)+diskOffset;
		
		
		for(int i=0;i<4;i++)
		{
			if(dAddress<4092)
			str=str+Disk[dAddress+i];
		}
		return str;
	}
         	
        //write on disk
	void writeDisk(int diskaddress,String str) throws Exception
	{
		for(int i=0;i<4;i++)
		{
			Disk[i]=str.substring(i*4, (i*4)+4);
			
		}
	}
	
        //virtual to real disk address conversion
	int virtualToRealDisk(int virtualoffset) throws Exception
	{
		
		String getPageNumber=Loader.pcb.get(pid).pagetable.get(virtualoffset/16);
		return (Integer.parseInt(getPageNumber)+virtualoffset%16);
		
	}
	
	
	
	

}
