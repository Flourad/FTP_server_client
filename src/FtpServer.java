
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

class FtpClientFrame extends JFrame
{
	
	 protected JButton B_StartWebServer;
	 protected JButton B_EndWebServer;
	 protected JButton B_StartFtpServer;
	 protected JButton B_EndFtpServer;
	 public FtpServer ftpServer;
	
	 // ���캯��
	 public FtpClientFrame() 
	 {
		  // ����ϵͳ���/java���
		 try {
		     UIManager.setLookAndFeel(//"com.sun.java.swing.plaf.motif.MotifLookAndFeel");
		   "javax.swing.plaf.metal.MetalLookAndFeel");
		  } catch (Exception e) { }
		
		  //�رմ���	
		  addWindowListener(new WindowAdapter() {
		   	public void windowClosing(WindowEvent e) {
		    	System.exit(0);
		   }
		  });
		  
		 
		  		  
		  //������������
		  B_StartWebServer = new JButton("����Web������");
		  //B_StartWebServer.setPreferredSize(new Dimension(10, 10));
		  B_EndWebServer = new JButton("�ر�Web������");
		  //B_EndWebServer.setPreferredSize(new Dimension(10, 10));
		  B_StartFtpServer = new  JButton("����Ftp������");
		 // B_StartFtpServer .setPreferredSize(new Dimension(10, 10));
		  B_EndFtpServer = new JButton("�ر�Ftp������");
		 //  B_EndFtpServer.setPreferredSize(new Dimension(10, 10));
		  
		  
		  Thread thread1 = new Thread();
		  Thread thread2 = new Thread();
		  Thread thread3 = new Thread();
		  Thread thread4 = new Thread();
		  		  		  
          setTitle("Web & FTP Server");
          setSize(new Dimension(600, 364));	 

	
		  Container cp = getContentPane();
		  JPanel panel = new JPanel();
		  panel.setLayout(new FlowLayout());
		  cp.setLayout(new BorderLayout());
		  panel.add(B_StartWebServer);
		  panel.add(B_EndWebServer);
		  panel.add(B_StartFtpServer);
		  panel.add(B_EndFtpServer);
		  
		  cp.add(panel, BorderLayout.CENTER);	
		  
		  B_StartWebServer.addActionListener(
				  new ActionListener() {
		                public void actionPerformed(ActionEvent e) {
		                	B_EndWebServer.setEnabled(true);
		                	WebServer.web_main();
		                	B_StartWebServer.setEnabled(false);
		                }
		            }
				  );
		  B_EndWebServer.addActionListener(
				  new ActionListener() {
		                public void actionPerformed(ActionEvent e){
		                	B_StartWebServer.setEnabled(true);
		                	B_EndWebServer.setEnabled(false);
		                }
		            }
				  );
		  B_StartFtpServer.addActionListener(
				  new ActionListener() {
		         			public void actionPerformed(ActionEvent e) {
		                	B_StartFtpServer.setEnabled(false);
		                	B_EndFtpServer.setEnabled(true);
		                	ftpServer = new FtpServer();
		                }
		            }
				  );
		  B_EndFtpServer.addActionListener(
				  new ActionListener() {
		                public void actionPerformed(ActionEvent e) {
		                	B_StartFtpServer.setEnabled(true);
		                	B_EndFtpServer.setEnabled(false);
		                	return;
		                	
		                
		                
		                }
		            }
				  );
		  

	 }
		 
	 }


/*****************************************************************************************/
public class FtpServer extends Thread
{
	private static Socket socketClient;
	private int counter;
	public static String initDir;	//����������߳�����ʱ���ڵĹ���Ŀ¼
	public static ArrayList users = new ArrayList();
	public static ArrayList usersInfo = new ArrayList();
	
	public FtpServer()
	{
		FtpConsole fc = new FtpConsole();
		fc.start();
		loadUsersInfo();		//����
		counter = 1;		
		int i = 0;
		try
		{

			//����21�Ŷ˿�,21�����ڿ���,20�����ڴ�����
			ServerSocket s = new ServerSocket(21);
			for(;;)
			{
				//���ܿͻ�������
				Socket incoming = s.accept();
				BufferedReader in = new BufferedReader(new InputStreamReader(incoming.getInputStream()));
			    PrintWriter out = new PrintWriter(incoming.getOutputStream(),true);//�ı��ı������
				out.println("220 ׼��Ϊ������"+",���ǵ�ǰ��  "+counter+" ����½��!");//������ȷ����ʾ

				//���������߳�
				FtpHandler h = new FtpHandler(incoming,i);
				h.start();
				users.add(h);   //�����û��̼߳��뵽��� ArrayList ��
				counter++;
				i++;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		
	}
	
	
	public void loadUsersInfo()
	{
		String s = getClass().getResource("user.cfg").toString();
		s = s.substring(6,s.length());//�Ӵ���ʼ6����չ�� s.length()��λ�á� 
		int p1 = 0;		//�� | ������
		int p2 = 0;		//�� | ��һλ������
		if(new File(s).exists())//���Ե�ǰ File �Ƿ����
		{
			try
			{
				BufferedReader fin = new BufferedReader(new InputStreamReader(new FileInputStream(s)));
				String line;  //���ļ���ȡһ�д��ڴ�
				String field; //�� | ǰ line ���Ӵ�
				int i = 0;
				//��һ��while ����Ϊ��������
				while((line = fin.readLine())!=null)//������β��Ϊ null
				{
					UserInfo tempUserInfo = new UserInfo();
					p1 = 0;
					p2 = 0;
					i = 0;
					if(line.startsWith("#"))//����#��ʼ,����ture
						continue;
					//�ڶ���while ����Ϊload �ļ���һ�е���Ϣ
					while((p2 = line.indexOf("|",p1))!=-1)//��p1��ʼ��,���� | ��һ�γ��ֵ�����,û�з���-1
					{
						field = line.substring(p1,p2);//��p1 ~ p2-1
						p2 = p2 +1; 
						p1 = p2;   //��p2
						switch(i)
						{
							case 0:
								tempUserInfo.user = field;
								break;
							case 1:
								tempUserInfo.password = field;
								break;
							case 2:
								tempUserInfo.workDir = field;
								break;
						}
						i++;
					} 
					usersInfo.add(tempUserInfo);
				}
				fin.close();
			}
			catch(Exception e)
			{
				//e.printStackTrace();
			}
		}
	}
	
	/*****************************************************************************/
	public static void main(String args[])
	{
		
		if(args.length != 0) 
		{
			initDir = args[0];
		}
		else
		{ 
			initDir = "c:/";	
		}
		FtpClientFrame clientframe = new FtpClientFrame();
	    clientframe.show();
		
		
	} 

}

/******************************************************************************/
class FtpHandler extends Thread
{
	Socket ctrlSocket;		//���ڿ��Ƶ��׽���
	Socket dataSocket;		//���ڴ�����׽���
	int id;
	String cmd = "";		//���ָ��(�ո�ǰ)
	String param = "";		//�ŵ�ǰָ��֮��Ĳ���(�ո��)
	String user;
	String remoteHost = " ";	   //�ͻ�IP
	int remotePort = 0;			   //�ͻ�TCP �˿ں�
	String dir = FtpServer.initDir;//��ǰĿ¼
	String rootdir = "c:/";	       //Ĭ�ϸ�Ŀ¼,��checkPASS������
	int state = 0 ;				   //�û�״̬��ʶ��,��checkPASS������
	String reply;				   //���ر���
	PrintWriter ctrlOutput; 
	int type = 0;				   //�ļ�����(ascII �� bin)
	String requestfile = "";
	boolean isrest = false;
	
	//FtpHandler����
	
	public FtpHandler(Socket s,int i)
	{
		ctrlSocket = s;
		id = i;	
	}

	//run ����
	public void run()
	{
		String str = "";
		int parseResult;							//��cmd��Ӧ�ĺ�
		
		try
		{
			BufferedReader ctrlInput = new BufferedReader
								(new InputStreamReader(ctrlSocket.getInputStream()));
			ctrlOutput = new PrintWriter(ctrlSocket.getOutputStream(),true);
			state  = FtpState.FS_WAIT_LOGIN;  		//0
			boolean finished = false;
			while(!finished)	
			{
				str = ctrlInput.readLine();			
				if(str == null) finished = true;	//����while
				else
				{
					parseResult = parseInput(str);  //ָ��ת��Ϊָ���
					System.out.println("ָ��:"+cmd+" ����:"+param);
					System.out.print("->");
					switch(state)					//�û�״̬����
					{
						case FtpState.FS_WAIT_LOGIN:
								finished = commandUSER();
								break;
						case FtpState.FS_WAIT_PASS:
								finished = commandPASS();
								break;
						case FtpState.FS_LOGIN:
						{
							switch(parseResult)//ָ��ſ���,���������Ƿ�������еĹؼ�
							{
								case -1:
									errCMD();					//�﷨����
									break;
								case 4:
									finished = commandCDUP();   //����һ��Ŀ¼
									break;
								case 6:
									finished = commandCWD();	//��ָ����Ŀ¼
									break;
								case 7:
									finished = commandQUIT();	//�˳�
									break;
								case 9:
									finished = commandPORT();	//�ͻ���IP:��ַ+TCP �˿ں�
									break;
								case 11:
									finished = commandTYPE();	//�ļ���������(ascII �� bin)
									break;
								case 14:
									finished = commandRETR();	//�ӷ������л���ļ�
									break;
								case 15:
									finished = commandSTOR();	//��������з����ļ�
									break;
								case 22:
									finished = commandABOR();	//�رմ���������dataSocket
									break;
								case 23:
									finished = commandDELE();	//ɾ���������ϵ�ָ���ļ�
									break;
								case 25:
									finished = commandMKD();	//����Ŀ¼
									break;
								case 27:
									finished = commandLIST();	//�ļ���Ŀ¼���б�
									break;
								case 26:
								case 33:
									finished = commandPWD();	//"��ǰĿ¼" ��Ϣ
									break;
								case 32:
									finished = commandNOOP();	//"������ȷ" ��Ϣ
									break;
								
							}
						}
							break;
						

					}
				} 
				ctrlOutput.println(reply);
				ctrlOutput.flush();
				
			} 
			ctrlSocket.close();
		} 
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	//parseInput����	
	int parseInput(String s)
	{
		int p = 0;
		int i = -1;
		p = s.indexOf(" ");
		if(p == -1) 				 //������޲�������(�޿ո�)
			cmd = s;
		else 
			cmd = s.substring(0,p);  //�в�������,���˲���
		
		if(p >= s.length() || p ==-1)//����޿ո�,��ո��ڶ����s������֮��
			param = "";
		else
			param = s.substring(p+1,s.length());
		cmd = cmd.toUpperCase();     //ת���� String Ϊ��д
		
	  	if(cmd.equals("CDUP"))
				i = 4;
		if(cmd.equals("CWD"))
				i = 6;
		if(cmd.equals("QUIT"))
				i = 7;
		if(cmd.equals("PORT"))
				i = 9;
		if(cmd.equals("TYPE"))
				i = 11;
		if(cmd.equals("RETR"))
				i = 14;
		if(cmd.equals("STOR"))
				i = 15;
		if(cmd.equals("ABOR"))
				i = 22;
		if(cmd.equals("DELE"))
				i = 23;
		if(cmd.equals("MKD"))
				i = 25;
		if(cmd.equals("PWD"))
				i = 26;
		if(cmd.equals("LIST"))
				i = 27;
	  	if(cmd.equals("NOOP"))
				i = 32;
		if(cmd.equals("XPWD"))
				i = 33;
	 return i;
	}
	
	//validatePath����
	//�ж�·��������,���� int 
	int validatePath(String s)
	{
		File f = new File(s);		//���·��
		if(f.exists() && !f.isDirectory())
		{
			String s1 = s.toLowerCase();
			String s2 = rootdir.toLowerCase();
			if(s1.startsWith(s2))	
				return 1;			//�ļ������Ҳ���·��,����rootdir ��ʼ
			else
				return 0;			//�ļ������Ҳ���·��,����rootdir ��ʼ
		}
		f = new File(addTail(dir)+s);//����·��
		if(f.exists() && !f.isDirectory())
		{
			String s1 = (addTail(dir)+s).toLowerCase();
			String s2 = rootdir.toLowerCase();
			if(s1.startsWith(s2))
				return 2;			//�ļ������Ҳ���·��,����rootdir ��ʼ
			else 
				return 0;			//�ļ������Ҳ���·��,����rootdir ��ʼ
		}
		return 0;					//�������
	}
	
	boolean checkPASS(String s) //��������Ƿ���ȷ,���ļ�����
	{
		for(int i = 0; i<FtpServer.usersInfo.size();i++)
		{
			if(((UserInfo)FtpServer.usersInfo.get(i)).user.equals(user) && 
				((UserInfo)FtpServer.usersInfo.get(i)).password.equals(s))
			{
				rootdir = ((UserInfo)FtpServer.usersInfo.get(i)).workDir;
				dir = ((UserInfo)FtpServer.usersInfo.get(i)).workDir;
				return true;
			}
		}
		return false;
	}

	//commandUSER����
	//�û����Ƿ���ȷ
	boolean commandUSER()
	{
		if(cmd.equals("USER"))
		{
			reply = "331 �û�����ȷ,��Ҫ����";
			user = param;
		  	state = FtpState.FS_WAIT_PASS;
			return false;
		}
		else
		{
			reply = "501 �����﷨����,�û�����ƥ��";
			return true;
		}

	}

	//commandPASS ����
	//�����Ƿ���ȷ
	boolean commandPASS()
	{
		if(cmd.equals("PASS"))
		{
			if(checkPASS(param))
			{
				reply = "230 �û���¼��";
				state = FtpState.FS_LOGIN;
				System.out.println("����Ϣ: �û�: "+user+" ������: "+ remoteHost +"��¼��");
				System.out.print("->");
				return false;
			}
			else
			{
				reply = "530 û�е�¼";
				return true;
			}
		}
		else
		{
			reply = "501 �����﷨����,���벻ƥ��";
			return true;
		}

	}

	void errCMD()
	{
		reply = "500 �﷨����";
	}	
	
	boolean commandCDUP()//����һ��Ŀ¼
	{					 
		dir = FtpServer.initDir;	
		File f = new File(dir);
		if(f.getParent()!=null &&(!dir.equals(rootdir)))//�и�·�� && ���Ǹ�·��
		{
			dir = f.getParent();
			reply = "200 ������ȷ";
		}
		else
		{
			reply = "550 ��ǰĿ¼�޸�·��";
		}
		
		return false;
	}// commandCDUP() end

	boolean commandCWD()// CWD (CHANGE WORKING DIRECTORY)
	{					//������ı乤��Ŀ¼���û�ָ����Ŀ¼
		File f = new File(param);
		String s = "";
		String s1 = "";
		if(dir.endsWith("/"))
			s = dir;
		else
			s = dir + "/";
		File f1 = new File(s+param);
		
		if(f.isDirectory() && f.exists())
		{
			if(param.equals("..") || param.equals("..\\"))
			{
				if(dir.compareToIgnoreCase(rootdir)==0)
				{
					reply = "550 ��·��������";
					//return false;
				}
				else
				{
					s1 = new File(dir).getParent();
					if(s1!=null)
					{
						dir = s1;
						reply = "250 ������ļ��������, ��ǰĿ¼��Ϊ: "+dir;
					}
					else
						reply = "550 ��·��������";
				}
			}
			else if(param.equals(".") || param.equals(".\\"))
			{}
			else 
			{
				dir = param;
				reply = "250 ������ļ��������, ����·����Ϊ "+dir;
			}		
		}
		else if(f1.isDirectory() && f1.exists())
		{
			dir = s+param;
			reply = "250 ������ļ��������, ����·����Ϊ "+dir;
		}
		else
			reply = "501 �����﷨����";
		
		return false;
	} // commandCDW() end

	boolean commandQUIT()
	{
		reply = "221 ����ر�����";
		return true;
	}// commandQuit() end
	

	boolean commandPORT()
	{
		int p1 = 0;
		int p2 = 0;
		int[] a = new int[6];//���ip+tcp
		int i = 0;			 
		try
		{
			while((p2 = param.indexOf(",",p1))!=-1)//ǰ5λ
			{
				 a[i] = Integer.parseInt(param.substring(p1,p2));
				 p2 = p2+1;
				 p1 = p2;
				 i++;
			}
			a[i] = Integer.parseInt(param.substring(p1,param.length()));//���һλ
		}
		catch(NumberFormatException e)
		{
			reply = "501 �����﷨����";
			return false;
		}
		
		remoteHost = a[0]+"."+a[1]+"."+a[2]+"."+a[3];
		remotePort = a[4] * 256+a[5];
		reply = "200 ������ȷ";
		return false;
	}//commandPort() end
		
	
	boolean commandLIST()//�ļ���Ŀ¼���б�
	{
		try
		{
			dataSocket = new Socket(remoteHost,remotePort,InetAddress.getLocalHost(),20);
			PrintWriter dout = new PrintWriter(dataSocket.getOutputStream(),true);
			if(param.equals("") || param.equals("LIST"))
			{
				ctrlOutput.println("150 �ļ�״̬����,ls�� ASCII ��ʽ����");
				File f = new File(dir);

				/*String thepath=f.getPath();
				System.out.println("path is"+dir);*/

				String[] dirStructure = f.list();//ָ��·���е��ļ�������,��������ǰ·����·��
				String fileType;
				//String[] filedir=new String [dirStructure.length];
				for(int i =0; i<dirStructure.length;i++)
				{   
					
					
					if(dirStructure[i].indexOf(".")!=-1)
					{
						fileType = "f";		//����һ���ļ�
					}
					else
					{
						fileType = "d";		//����һ��Ŀ¼
					}

					//System.out.println(fileType+dirStructure[i]);
					//filedir[i]=dir+dirStructure[i];
					//System.out.println("dir"+filedir[i]);
					//File nf=new File(filedir[i]);
					//long lm=nf.lastModified();
					//long year=lm/1000/3600/24/365+70-100;
					//Float ffff=new Float(year);
					//String time=new String(ffff.toString()+"-"+"01-"+"01"+" 00:00:00PM");

					//if(fileType=="d"){
					  // dout.println(time+"\t<dir>\t"+dirStructure[i]);
					  // System.out.println(time+"\t<dir>\t"+dirStructure[i]);
					//}
					//if(fileType=="f"){
					//long len=nf.length();
					//dout.println(time+"\t"+len+"\t"+dirStructure[i]);
					//System.out.println(time+"\t"+len+"\t"+dirStructure[i]);
					//}
					
					dout.println(dirStructure[i].trim());

				}
			} 
			dout.close();
			dataSocket.close();
			reply = "226 �����������ӽ���";
		}
		catch(Exception e)
		{
			e.printStackTrace();
			reply = "451 Requested action aborted: local error in processing";
			return false;
		}
		
		return false;
	}// commandLIST() end

	boolean commandTYPE()	//TYPE �������������������
	{
		if(param.equals("A"))
		{
			type = FtpState.FTYPE_ASCII;//0
			reply = "200 ������ȷ ,ת ASCII ģʽ";
		}
		else if(param.equals("I"))
		{
			type = FtpState.FTYPE_IMAGE;//1
			reply = "200 ������ȷ ת BINARY ģʽ";
		}
		else
			reply = "504 �����ִ�����ֲ���";
			
		return false;
	}
	
	//connamdRETR ����
	//�ӷ������л���ļ�
	boolean commandRETR()
	{
		requestfile = param;
		File f =  new File(requestfile);
  		if(!f.exists())
		{
	  		f = new File(addTail(dir)+param);
			if(!f.exists())
			{
	   			reply = "550 �ļ�������";
	   			return  false;
			}
			requestfile = addTail(dir)+param;
		}
  
  		if(isrest)
		{
     
		}
		else
		{
	 		if(type==FtpState.FTYPE_IMAGE)				//bin
			{
				try
				{
					ctrlOutput.println("150 �ļ�״̬����,�Զ����η�ʽ���ļ�:  "+ requestfile);
					dataSocket = new Socket(remoteHost,remotePort,InetAddress.getLocalHost(),20);
    				BufferedInputStream  fin = new BufferedInputStream(new FileInputStream(requestfile));
	  				PrintStream dataOutput = new PrintStream(dataSocket.getOutputStream(),true);
					byte[] buf = new byte[1024]; 		//Ŀ�껺����
					int l = 0;
					while((l=fin.read(buf,0,1024))!=-1)	//������δ����
					{
			  			dataOutput.write(buf,0,l);		//д���׽���
					}
		 			fin.close();
     				dataOutput.close();
		 			dataSocket.close();
		 			reply ="226 �����������ӽ���";

				}
				catch(Exception e)
				{
					e.printStackTrace();
					reply = "451 ����ʧ��: ���������";
					return false;
				}

			}
			if(type==FtpState.FTYPE_ASCII)//ascII
			{
	  			try
				{
					ctrlOutput.println("150 Opening ASCII mode data connection for "+ requestfile);
					dataSocket = new Socket(remoteHost,remotePort,InetAddress.getLocalHost(),20);
    				BufferedReader  fin = new BufferedReader(new FileReader(requestfile));
	  				PrintWriter dataOutput = new PrintWriter(dataSocket.getOutputStream(),true);
					String s;
					while((s=fin.readLine())!=null)
					{
		   				dataOutput.println(s);	///???
					}
		 			fin.close();
     				dataOutput.close();
		 			dataSocket.close();
		 			reply ="226 �����������ӽ���";
				}
				catch(Exception e)
				{
					e.printStackTrace();
					reply = "451 ����ʧ��: ���������";
					return false;
				}
			}
		}
  		return false;

	}
	
	//commandSTOR ����
	//��������з����ļ�STOR
	boolean commandSTOR()
	{
		if(param.equals(""))
		{
			reply = "501 �����﷨����";
			return false;
		}
		requestfile = addTail(dir)+param;
		if(type == FtpState.FTYPE_IMAGE)//bin
		{
			try
			{
				ctrlOutput.println("150 Opening Binary mode data connection for "+ requestfile);
				dataSocket = new Socket(remoteHost,remotePort,InetAddress.getLocalHost(),20);
				BufferedOutputStream fout = new BufferedOutputStream(new FileOutputStream(requestfile));
				BufferedInputStream dataInput = new BufferedInputStream(dataSocket.getInputStream());
				byte[] buf = new byte[1024];
				int l = 0;
				while((l = dataInput.read(buf,0,1024))!=-1)
				{
					fout.write(buf,0,l);
				}
				dataInput.close();
				fout.close();
				dataSocket.close();
				reply = "226 �����������ӽ���";
			}
			catch(Exception e)
			{
				e.printStackTrace();
				reply = "451 ����ʧ��: ���������";
				return false;
			}
		}
		if(type == FtpState.FTYPE_ASCII)//ascII
		{
			try
			{
				ctrlOutput.println("150 Opening ASCII mode data connection for "+ requestfile);
				dataSocket = new Socket(remoteHost,remotePort,InetAddress.getLocalHost(),20);
				PrintWriter fout = new PrintWriter(new FileOutputStream(requestfile));
				BufferedReader dataInput = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
				String line;
				while((line = dataInput.readLine())!=null)
				{
					fout.println(line);					
				}
				dataInput.close();
				fout.close();
				dataSocket.close();
				reply = "226 �����������ӽ���";
			}
			catch(Exception e)
			{
				e.printStackTrace();
				reply = "451 ����ʧ��: ���������";
				return false;
			}
		}
		return false;
	}
	
	boolean commandPWD()
	{
		reply = "257 " + dir + " �ǵ�ǰĿ¼.";
		return false;
	}
	
	boolean commandNOOP()
	{
		reply = "200 ������ȷ.";
		return false;
	}
	
	//ǿ��dataSocket ��
	boolean commandABOR()
	{
		try
		{
			dataSocket.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			reply = "451 ����ʧ��: ���������";
			return false; 
		}
		reply = "421 ���񲻿���, �ر����ݴ�������";
		return false;
	}
	
	//ɾ���������ϵ�ָ���ļ�
	boolean commandDELE()
	{
		int i = validatePath(param);
		if(i == 0)
		{
			reply = "550 ����Ķ���δִ��,�ļ�������,��Ŀ¼����,������";
	    	return false;
		}
		if(i == 1)
    	{
	    	File f = new File(param);
			f.delete();
    	}
		if(i == 2)
		{
			File f= new File(addTail(dir)+param);
			f.delete();
		} 
		
		reply = "250 ������ļ��������,�ɹ�ɾ�����������ļ�";
		return false;

	}

	//����Ŀ¼,Ҫ����·��
	boolean commandMKD()
	{
		String s1 = param.toLowerCase();
		String s2 = rootdir.toLowerCase();
		if(s1.startsWith(s2))
		{
			File f = new File(param);
			if(f.exists())
			{
				reply = "550 ����Ķ���δִ��,Ŀ¼�Ѵ���";
				return false;
			}
			else 
			{
				f.mkdirs();
				reply = "250 ������ļ��������, Ŀ¼����";
			}
		}
		else 
		{
			File f = new File(addTail(dir)+param);
			if(f.exists())
			{
				reply = "550 ����Ķ���δִ��,Ŀ¼�Ѵ���";
				return false;
			}
			else 
			{
				f.mkdirs();
				reply = "250 ������ļ��������, Ŀ¼����";
			}
		}
		
		return false;
	}

	String addTail(String s)
	{
		if(!s.endsWith("/"))
			s = s + "/";
		return s;
	}
		
}

/********************************************************************************/
class FtpConsole extends Thread
{
	BufferedReader cin;	//
	String conCmd;		//����
	String conParam;	//����
///
	public FtpConsole()
	{
		System.out.println("ftp ����������!");
		cin = new BufferedReader(new InputStreamReader(System.in));
	}
///
	public void stop_server() throws IOException{
		System.out.println("ftp �������ر�!");
		if(cin!=null)
			cin.close();
		
		
	}
	public void run()
	{
		boolean ok = false;
		String input = "";
		while(!ok)
		{
			System.out.print("->");
			try
			{
				input = cin.readLine(); 
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			switch(parseInput(input))		//�����
			{
				case 1:
					consoleQUIT();			//�˳�
					break;
				case 8:
					ok = consoleLISTUSER(); //�г�����ע���û����乤��Ŀ¼
					break;
				case 0:
					ok = consoleLIST();		//�г��,����IP
					break;
				case 2:
					ok = consoleADDUSER();	//����һ��ע���û�
					break;
				case 3:
				 	ok = consoleDELUSER();	//ɾ��һ��ע���û�
				 	break;
				 case 7:
				 	ok = consoleHELP();		//��ʾ������Ϣ
				 	break;
				 case -1:
				 	ok = consoleERR();		//��������
				 	break;
			}
		}
	}

	//�˳�
	int consoleQUIT()						
	{
		System.exit(0);
		return 0;
	}
	
	//�г�����ע���û����乤��Ŀ¼
	boolean consoleLISTUSER()				
	{
		System.out.println("�û���   \t\t ����Ŀ¼");
		for(int i = 0 ; i<FtpServer.usersInfo.size();i++)
		{
			System.out.println(((UserInfo)FtpServer.usersInfo.get(i)).user+" \t\t\t "+((UserInfo)FtpServer.usersInfo.get(i)).workDir);
		}
		return false;
	}

	//�г���û�,����IP
	boolean consoleLIST()					
	{
		int i = 0;
  		for(i=0;i<FtpServer.users.size();i++)
		{
			System.out.println((i+1)+":"+((FtpHandler)(FtpServer.users.get(i))).user 
			+ " ������ " 
			+((FtpHandler)(FtpServer.users.get(i))).ctrlSocket.getInetAddress().toString());
		}

  	    return false;
	}
	
	//�ж��Ƿ��Ѿ�ע����
	boolean validateUserName(String s)		
	{
		for(int i = 0 ; i<FtpServer.usersInfo.size();i++)
		{
			if(((UserInfo)FtpServer.usersInfo.get(i)).user.equals(s))
				return false;	
		}
		return true;
	}

	//����һ��ע���û�
	boolean consoleADDUSER()				
	{
		System.out.print("������û� :");
		try
		{
			cin = new BufferedReader(new InputStreamReader(System.in));
			UserInfo tempUserInfo = new UserInfo();
			String line = cin.readLine();
			if(line != "")
			{
				if(!validateUserName(line))//�Ѵ�������û�
				{
					System.out.println("�û��� "+line+" ��ע��!");
					return false;
				}
			}
			else
			{
				System.out.println("�û�������Ϊ�� !");
				return false;
			}
			
			tempUserInfo.user = line;
			System.out.print("��������� :");
			line= cin.readLine();
			if(line != "")
				tempUserInfo.password = line;
			else
			{
				System.out.println("���벻��Ϊ�� !");
				return false;
			}
			System.out.print("�����û���Ŀ¼ : ");
			line = cin.readLine();
			if(line != "")
			{
				File f = new File(line);
				if(!f.exists())
					f.mkdirs();
				tempUserInfo.workDir = line;
			}
			else
			{
				System.out.println("��Ŀ¼����Ϊ�� !");
				return false;
			}
			FtpServer.usersInfo.add(tempUserInfo);
			saveUserInfo();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
		return false;
	}

	//
	void saveUserInfo()					
	{
		String s = "";
		try
		{
			BufferedWriter fout = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("user.cfg")));
			for(int i = 0; i < FtpServer.usersInfo.size();i++)
			{
				s = ((UserInfo)FtpServer.usersInfo.get(i)).user+"|"+((UserInfo)FtpServer.usersInfo.get(i)).password+"|"+((UserInfo)FtpServer.usersInfo.get(i)).workDir+"|";
				fout.write(s);
				fout.newLine();
			}
			fout.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}  

	//ɾ��һ��ע���û�
	boolean consoleDELUSER()				
	{
		String s = "";
		if(conParam.equals(""))
		{
			System.out.println("�÷�:deluser �û���");
			return false;
		}
		for(int i=0;i<FtpServer.usersInfo.size();i++)
		{
			s = ((UserInfo)FtpServer.usersInfo.get(i)).user;
			if(s.equals(conParam))
			{
				System.out.println("�û�ע����Ϣ "+conParam+" ��ɾ��");
                FtpServer.usersInfo.remove(i);
				saveUserInfo();
				return false;
			}
		}
		System.out.println("�û� "+conParam+" ������");					
		return false;

	}

	///
	boolean consoleHELP()
	{
		if(conParam.equals(""))
		{
			System.out.println("adduser :����һ��ע���û�");
			System.out.println("deluser <username> :ɾ��һ��ע���û�");
			System.out.println("quit  :�˳�");
			System.out.println("list  :�г���û�,����IP");
			System.out.println("listuser : �г�����ע���û����乤��Ŀ¼");
			System.out.println("help :��ʾ ������Ϣ");
		}
		else if(conParam.equals("adduser"))
			System.out.println("adduser :����һ��ע���û�");
		else if(conParam.equals("deluser"))
			System.out.println("deluser <username> :ɾ��һ��ע���û�");
		else if(conParam.equals("quit"))
			System.out.println("quit  :�˳�");
		else if(conParam.equals("list"))
			System.out.println("list  :�г���û�,����IP");
		else if(conParam.equals("listuser"))
			System.out.println("listuser : �г�����ע���û����乤��Ŀ¼");
		else if(conParam.equals("help"))
			System.out.println("help :��ʾ ������Ϣ");
		else
			return false;
		return false;
		
	} 
	boolean consoleERR()
	{
		System.out.println("��������!");
		return false;
	} 
	int parseInput(String s)
	{
		String upperCmd;
		int p = 0;
		conCmd = "";
		conParam = "";
		p = s.indexOf(" ");			  
		if(p == -1)
			conCmd = s;
		else 
			conCmd = s.substring(0,p);//�����Ӵ�,0 ~ p-1 */
		
		if(p >= s.length() || p ==-1)
			conParam = "";
		else
			conParam = s.substring(p+1,s.length());
			
		upperCmd = conCmd.toUpperCase();//Сд->��д
		
		if(upperCmd.equals("LIST"))
			return 0;
		else if(upperCmd.equals("QUIT")||upperCmd.equals("EXIT"))
			return 1;
		else if(upperCmd.equals("ADDUSER"))
			return 2;
		else if(upperCmd.equals("DELUSER"))
			return 3;
		else if(upperCmd.equals("EDITUSER"))
			return 4;
		else if(upperCmd.equals("ADDDIR"))
			return 5;
		else if(upperCmd.equals("REMOVEDIR"))
			return 6;
		else if(upperCmd.equals("HELP") ||upperCmd.equals("?"))
			return 7;
		else if(upperCmd.equals("LISTUSER"))
			return 8;						
		return -1;
	}
}
/***************************************************************************/

class FtpState//�����û�״̬��Ϣ
{
	final static int FS_WAIT_LOGIN = 0;	//�ȴ������û���״̬
	final static int FS_WAIT_PASS = 1;	//�ȴ���������״̬
	final static int FS_LOGIN = 2;		//�Ѿ���½״̬
	
	final static int FTYPE_ASCII = 0;
	final static int FTYPE_IMAGE  = 1;
	final static int FMODE_STREAM = 0;
	final static int FMODE_COMPRESSED = 1;
	final static int FSTRU_FILE = 0;
	final static int FSTRU_PAGE = 1;
}
/****************************************************************************/

class UserInfo//�����û�������Ϣ
{
	String user;
	String password;
	String workDir;
}
