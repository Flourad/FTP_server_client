
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
	
	 // 构造函数
	 public FtpClientFrame() 
	 {
		  // 调用系统外观/java外观
		 try {
		     UIManager.setLookAndFeel(//"com.sun.java.swing.plaf.motif.MotifLookAndFeel");
		   "javax.swing.plaf.metal.MetalLookAndFeel");
		  } catch (Exception e) { }
		
		  //关闭窗口	
		  addWindowListener(new WindowAdapter() {
		   	public void windowClosing(WindowEvent e) {
		    	System.exit(0);
		   }
		  });
		  
		 
		  		  
		  //以下是外观组件
		  B_StartWebServer = new JButton("启动Web服务器");
		  //B_StartWebServer.setPreferredSize(new Dimension(10, 10));
		  B_EndWebServer = new JButton("关闭Web服务器");
		  //B_EndWebServer.setPreferredSize(new Dimension(10, 10));
		  B_StartFtpServer = new  JButton("启动Ftp服务器");
		 // B_StartFtpServer .setPreferredSize(new Dimension(10, 10));
		  B_EndFtpServer = new JButton("关闭Ftp服务器");
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
	public static String initDir;	//保存服务器线程运行时所在的工作目录
	public static ArrayList users = new ArrayList();
	public static ArrayList usersInfo = new ArrayList();
	
	public FtpServer()
	{
		FtpConsole fc = new FtpConsole();
		fc.start();
		loadUsersInfo();		//加载
		counter = 1;		
		int i = 0;
		try
		{

			//监听21号端口,21口用于控制,20口用于传数据
			ServerSocket s = new ServerSocket(21);
			for(;;)
			{
				//接受客户端请求
				Socket incoming = s.accept();
				BufferedReader in = new BufferedReader(new InputStreamReader(incoming.getInputStream()));
			    PrintWriter out = new PrintWriter(incoming.getOutputStream(),true);//文本文本输出流
				out.println("220 准备为您服务"+",你是当前第  "+counter+" 个登陆者!");//命令正确的提示

				//创建服务线程
				FtpHandler h = new FtpHandler(incoming,i);
				h.start();
				users.add(h);   //将此用户线程加入到这个 ArrayList 中
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
		s = s.substring(6,s.length());//子串开始6，扩展到 s.length()的位置。 
		int p1 = 0;		//放 | 的索引
		int p2 = 0;		//放 | 后一位的索引
		if(new File(s).exists())//测试当前 File 是否存在
		{
			try
			{
				BufferedReader fin = new BufferedReader(new InputStreamReader(new FileInputStream(s)));
				String line;  //从文件中取一行存于此
				String field; //放 | 前 line 的子串
				int i = 0;
				//第一个while 作用为读所有行
				while((line = fin.readLine())!=null)//到达流尾则为 null
				{
					UserInfo tempUserInfo = new UserInfo();
					p1 = 0;
					p2 = 0;
					i = 0;
					if(line.startsWith("#"))//如以#开始,返回ture
						continue;
					//第二个while 作用为load 文件中一行的信息
					while((p2 = line.indexOf("|",p1))!=-1)//从p1开始查,返回 | 第一次出现的索引,没有返回-1
					{
						field = line.substring(p1,p2);//从p1 ~ p2-1
						p2 = p2 +1; 
						p1 = p2;   //新p2
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
	Socket ctrlSocket;		//用于控制的套接字
	Socket dataSocket;		//用于传输的套接字
	int id;
	String cmd = "";		//存放指令(空格前)
	String param = "";		//放当前指令之后的参数(空格后)
	String user;
	String remoteHost = " ";	   //客户IP
	int remotePort = 0;			   //客户TCP 端口号
	String dir = FtpServer.initDir;//当前目录
	String rootdir = "c:/";	       //默认根目录,在checkPASS中设置
	int state = 0 ;				   //用户状态标识符,在checkPASS中设置
	String reply;				   //返回报告
	PrintWriter ctrlOutput; 
	int type = 0;				   //文件类型(ascII 或 bin)
	String requestfile = "";
	boolean isrest = false;
	
	//FtpHandler方法
	
	public FtpHandler(Socket s,int i)
	{
		ctrlSocket = s;
		id = i;	
	}

	//run 方法
	public void run()
	{
		String str = "";
		int parseResult;							//与cmd对应的号
		
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
				if(str == null) finished = true;	//跳出while
				else
				{
					parseResult = parseInput(str);  //指令转化为指令号
					System.out.println("指令:"+cmd+" 参数:"+param);
					System.out.print("->");
					switch(state)					//用户状态开关
					{
						case FtpState.FS_WAIT_LOGIN:
								finished = commandUSER();
								break;
						case FtpState.FS_WAIT_PASS:
								finished = commandPASS();
								break;
						case FtpState.FS_LOGIN:
						{
							switch(parseResult)//指令号开关,决定程序是否继续运行的关键
							{
								case -1:
									errCMD();					//语法错误
									break;
								case 4:
									finished = commandCDUP();   //到上一层目录
									break;
								case 6:
									finished = commandCWD();	//到指定的目录
									break;
								case 7:
									finished = commandQUIT();	//退出
									break;
								case 9:
									finished = commandPORT();	//客户端IP:地址+TCP 端口号
									break;
								case 11:
									finished = commandTYPE();	//文件类型设置(ascII 或 bin)
									break;
								case 14:
									finished = commandRETR();	//从服务器中获得文件
									break;
								case 15:
									finished = commandSTOR();	//向服务器中发送文件
									break;
								case 22:
									finished = commandABOR();	//关闭传输用连接dataSocket
									break;
								case 23:
									finished = commandDELE();	//删除服务器上的指定文件
									break;
								case 25:
									finished = commandMKD();	//建立目录
									break;
								case 27:
									finished = commandLIST();	//文件和目录的列表
									break;
								case 26:
								case 33:
									finished = commandPWD();	//"当前目录" 信息
									break;
								case 32:
									finished = commandNOOP();	//"命令正确" 信息
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

	//parseInput方法	
	int parseInput(String s)
	{
		int p = 0;
		int i = -1;
		p = s.indexOf(" ");
		if(p == -1) 				 //如果是无参数命令(无空格)
			cmd = s;
		else 
			cmd = s.substring(0,p);  //有参数命令,过滤参数
		
		if(p >= s.length() || p ==-1)//如果无空格,或空格在读入的s串最后或之外
			param = "";
		else
			param = s.substring(p+1,s.length());
		cmd = cmd.toUpperCase();     //转换该 String 为大写
		
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
	
	//validatePath方法
	//判断路径的属性,返回 int 
	int validatePath(String s)
	{
		File f = new File(s);		//相对路径
		if(f.exists() && !f.isDirectory())
		{
			String s1 = s.toLowerCase();
			String s2 = rootdir.toLowerCase();
			if(s1.startsWith(s2))	
				return 1;			//文件存在且不是路径,且以rootdir 开始
			else
				return 0;			//文件存在且不是路径,不以rootdir 开始
		}
		f = new File(addTail(dir)+s);//绝对路径
		if(f.exists() && !f.isDirectory())
		{
			String s1 = (addTail(dir)+s).toLowerCase();
			String s2 = rootdir.toLowerCase();
			if(s1.startsWith(s2))
				return 2;			//文件存在且不是路径,且以rootdir 开始
			else 
				return 0;			//文件存在且不是路径,不以rootdir 开始
		}
		return 0;					//其他情况
	}
	
	boolean checkPASS(String s) //检查密码是否正确,从文件中找
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

	//commandUSER方法
	//用户名是否正确
	boolean commandUSER()
	{
		if(cmd.equals("USER"))
		{
			reply = "331 用户名正确,需要口令";
			user = param;
		  	state = FtpState.FS_WAIT_PASS;
			return false;
		}
		else
		{
			reply = "501 参数语法错误,用户名不匹配";
			return true;
		}

	}

	//commandPASS 方法
	//密码是否正确
	boolean commandPASS()
	{
		if(cmd.equals("PASS"))
		{
			if(checkPASS(param))
			{
				reply = "230 用户登录了";
				state = FtpState.FS_LOGIN;
				System.out.println("新消息: 用户: "+user+" 来自于: "+ remoteHost +"登录了");
				System.out.print("->");
				return false;
			}
			else
			{
				reply = "530 没有登录";
				return true;
			}
		}
		else
		{
			reply = "501 参数语法错误,密码不匹配";
			return true;
		}

	}

	void errCMD()
	{
		reply = "500 语法错误";
	}	
	
	boolean commandCDUP()//到上一层目录
	{					 
		dir = FtpServer.initDir;	
		File f = new File(dir);
		if(f.getParent()!=null &&(!dir.equals(rootdir)))//有父路径 && 不是根路径
		{
			dir = f.getParent();
			reply = "200 命令正确";
		}
		else
		{
			reply = "550 当前目录无父路径";
		}
		
		return false;
	}// commandCDUP() end

	boolean commandCWD()// CWD (CHANGE WORKING DIRECTORY)
	{					//该命令改变工作目录到用户指定的目录
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
					reply = "550 此路径不存在";
					//return false;
				}
				else
				{
					s1 = new File(dir).getParent();
					if(s1!=null)
					{
						dir = s1;
						reply = "250 请求的文件处理结束, 当前目录变为: "+dir;
					}
					else
						reply = "550 此路径不存在";
				}
			}
			else if(param.equals(".") || param.equals(".\\"))
			{}
			else 
			{
				dir = param;
				reply = "250 请求的文件处理结束, 工作路径变为 "+dir;
			}		
		}
		else if(f1.isDirectory() && f1.exists())
		{
			dir = s+param;
			reply = "250 请求的文件处理结束, 工作路径变为 "+dir;
		}
		else
			reply = "501 参数语法错误";
		
		return false;
	} // commandCDW() end

	boolean commandQUIT()
	{
		reply = "221 服务关闭连接";
		return true;
	}// commandQuit() end
	

	boolean commandPORT()
	{
		int p1 = 0;
		int p2 = 0;
		int[] a = new int[6];//存放ip+tcp
		int i = 0;			 
		try
		{
			while((p2 = param.indexOf(",",p1))!=-1)//前5位
			{
				 a[i] = Integer.parseInt(param.substring(p1,p2));
				 p2 = p2+1;
				 p1 = p2;
				 i++;
			}
			a[i] = Integer.parseInt(param.substring(p1,param.length()));//最后一位
		}
		catch(NumberFormatException e)
		{
			reply = "501 参数语法错误";
			return false;
		}
		
		remoteHost = a[0]+"."+a[1]+"."+a[2]+"."+a[3];
		remotePort = a[4] * 256+a[5];
		reply = "200 命令正确";
		return false;
	}//commandPort() end
		
	
	boolean commandLIST()//文件和目录的列表
	{
		try
		{
			dataSocket = new Socket(remoteHost,remotePort,InetAddress.getLocalHost(),20);
			PrintWriter dout = new PrintWriter(dataSocket.getOutputStream(),true);
			if(param.equals("") || param.equals("LIST"))
			{
				ctrlOutput.println("150 文件状态正常,ls以 ASCII 方式操作");
				File f = new File(dir);

				/*String thepath=f.getPath();
				System.out.println("path is"+dir);*/

				String[] dirStructure = f.list();//指定路径中的文件名数组,不包括当前路径或父路径
				String fileType;
				//String[] filedir=new String [dirStructure.length];
				for(int i =0; i<dirStructure.length;i++)
				{   
					
					
					if(dirStructure[i].indexOf(".")!=-1)
					{
						fileType = "f";		//这是一个文件
					}
					else
					{
						fileType = "d";		//这是一个目录
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
			reply = "226 传输数据连接结束";
		}
		catch(Exception e)
		{
			e.printStackTrace();
			reply = "451 Requested action aborted: local error in processing";
			return false;
		}
		
		return false;
	}// commandLIST() end

	boolean commandTYPE()	//TYPE 命令用来完成类型设置
	{
		if(param.equals("A"))
		{
			type = FtpState.FTYPE_ASCII;//0
			reply = "200 命令正确 ,转 ASCII 模式";
		}
		else if(param.equals("I"))
		{
			type = FtpState.FTYPE_IMAGE;//1
			reply = "200 命令正确 转 BINARY 模式";
		}
		else
			reply = "504 命令不能执行这种参数";
			
		return false;
	}
	
	//connamdRETR 方法
	//从服务器中获得文件
	boolean commandRETR()
	{
		requestfile = param;
		File f =  new File(requestfile);
  		if(!f.exists())
		{
	  		f = new File(addTail(dir)+param);
			if(!f.exists())
			{
	   			reply = "550 文件不存在";
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
					ctrlOutput.println("150 文件状态正常,以二进治方式打开文件:  "+ requestfile);
					dataSocket = new Socket(remoteHost,remotePort,InetAddress.getLocalHost(),20);
    				BufferedInputStream  fin = new BufferedInputStream(new FileInputStream(requestfile));
	  				PrintStream dataOutput = new PrintStream(dataSocket.getOutputStream(),true);
					byte[] buf = new byte[1024]; 		//目标缓冲区
					int l = 0;
					while((l=fin.read(buf,0,1024))!=-1)	//缓冲区未读满
					{
			  			dataOutput.write(buf,0,l);		//写入套接字
					}
		 			fin.close();
     				dataOutput.close();
		 			dataSocket.close();
		 			reply ="226 传输数据连接结束";

				}
				catch(Exception e)
				{
					e.printStackTrace();
					reply = "451 请求失败: 传输出故障";
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
		 			reply ="226 传输数据连接结束";
				}
				catch(Exception e)
				{
					e.printStackTrace();
					reply = "451 请求失败: 传输出故障";
					return false;
				}
			}
		}
  		return false;

	}
	
	//commandSTOR 方法
	//向服务器中发送文件STOR
	boolean commandSTOR()
	{
		if(param.equals(""))
		{
			reply = "501 参数语法错误";
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
				reply = "226 传输数据连接结束";
			}
			catch(Exception e)
			{
				e.printStackTrace();
				reply = "451 请求失败: 传输出故障";
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
				reply = "226 传输数据连接结束";
			}
			catch(Exception e)
			{
				e.printStackTrace();
				reply = "451 请求失败: 传输出故障";
				return false;
			}
		}
		return false;
	}
	
	boolean commandPWD()
	{
		reply = "257 " + dir + " 是当前目录.";
		return false;
	}
	
	boolean commandNOOP()
	{
		reply = "200 命令正确.";
		return false;
	}
	
	//强关dataSocket 流
	boolean commandABOR()
	{
		try
		{
			dataSocket.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			reply = "451 请求失败: 传输出故障";
			return false; 
		}
		reply = "421 服务不可用, 关闭数据传送连接";
		return false;
	}
	
	//删除服务器上的指定文件
	boolean commandDELE()
	{
		int i = validatePath(param);
		if(i == 0)
		{
			reply = "550 请求的动作未执行,文件不存在,或目录不对,或其他";
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
		
		reply = "250 请求的文件处理结束,成功删除服务器上文件";
		return false;

	}

	//建立目录,要绝对路径
	boolean commandMKD()
	{
		String s1 = param.toLowerCase();
		String s2 = rootdir.toLowerCase();
		if(s1.startsWith(s2))
		{
			File f = new File(param);
			if(f.exists())
			{
				reply = "550 请求的动作未执行,目录已存在";
				return false;
			}
			else 
			{
				f.mkdirs();
				reply = "250 请求的文件处理结束, 目录建立";
			}
		}
		else 
		{
			File f = new File(addTail(dir)+param);
			if(f.exists())
			{
				reply = "550 请求的动作未执行,目录已存在";
				return false;
			}
			else 
			{
				f.mkdirs();
				reply = "250 请求的文件处理结束, 目录建立";
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
	String conCmd;		//命令
	String conParam;	//参数
///
	public FtpConsole()
	{
		System.out.println("ftp 服务器启动!");
		cin = new BufferedReader(new InputStreamReader(System.in));
	}
///
	public void stop_server() throws IOException{
		System.out.println("ftp 服务器关闭!");
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
			switch(parseInput(input))		//命令号
			{
				case 1:
					consoleQUIT();			//退出
					break;
				case 8:
					ok = consoleLISTUSER(); //列出所有注册用户及其工作目录
					break;
				case 0:
					ok = consoleLIST();		//列出活动,及其IP
					break;
				case 2:
					ok = consoleADDUSER();	//增加一个注册用户
					break;
				case 3:
				 	ok = consoleDELUSER();	//删除一个注册用户
				 	break;
				 case 7:
				 	ok = consoleHELP();		//显示帮助信息
				 	break;
				 case -1:
				 	ok = consoleERR();		//错误命令
				 	break;
			}
		}
	}

	//退出
	int consoleQUIT()						
	{
		System.exit(0);
		return 0;
	}
	
	//列出所有注册用户及其工作目录
	boolean consoleLISTUSER()				
	{
		System.out.println("用户名   \t\t 工作目录");
		for(int i = 0 ; i<FtpServer.usersInfo.size();i++)
		{
			System.out.println(((UserInfo)FtpServer.usersInfo.get(i)).user+" \t\t\t "+((UserInfo)FtpServer.usersInfo.get(i)).workDir);
		}
		return false;
	}

	//列出活动用户,及其IP
	boolean consoleLIST()					
	{
		int i = 0;
  		for(i=0;i<FtpServer.users.size();i++)
		{
			System.out.println((i+1)+":"+((FtpHandler)(FtpServer.users.get(i))).user 
			+ " 来自于 " 
			+((FtpHandler)(FtpServer.users.get(i))).ctrlSocket.getInetAddress().toString());
		}

  	    return false;
	}
	
	//判断是否已经注册了
	boolean validateUserName(String s)		
	{
		for(int i = 0 ; i<FtpServer.usersInfo.size();i++)
		{
			if(((UserInfo)FtpServer.usersInfo.get(i)).user.equals(s))
				return false;	
		}
		return true;
	}

	//增加一个注册用户
	boolean consoleADDUSER()				
	{
		System.out.print("请键入用户 :");
		try
		{
			cin = new BufferedReader(new InputStreamReader(System.in));
			UserInfo tempUserInfo = new UserInfo();
			String line = cin.readLine();
			if(line != "")
			{
				if(!validateUserName(line))//已存在这个用户
				{
					System.out.println("用户名 "+line+" 已注册!");
					return false;
				}
			}
			else
			{
				System.out.println("用户名不能为空 !");
				return false;
			}
			
			tempUserInfo.user = line;
			System.out.print("请键入密码 :");
			line= cin.readLine();
			if(line != "")
				tempUserInfo.password = line;
			else
			{
				System.out.println("密码不能为空 !");
				return false;
			}
			System.out.print("输入用户主目录 : ");
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
				System.out.println("主目录不能为空 !");
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

	//删除一个注册用户
	boolean consoleDELUSER()				
	{
		String s = "";
		if(conParam.equals(""))
		{
			System.out.println("用法:deluser 用户名");
			return false;
		}
		for(int i=0;i<FtpServer.usersInfo.size();i++)
		{
			s = ((UserInfo)FtpServer.usersInfo.get(i)).user;
			if(s.equals(conParam))
			{
				System.out.println("用户注册信息 "+conParam+" 已删除");
                FtpServer.usersInfo.remove(i);
				saveUserInfo();
				return false;
			}
		}
		System.out.println("用户 "+conParam+" 不存在");					
		return false;

	}

	///
	boolean consoleHELP()
	{
		if(conParam.equals(""))
		{
			System.out.println("adduser :增加一个注册用户");
			System.out.println("deluser <username> :删除一个注册用户");
			System.out.println("quit  :退出");
			System.out.println("list  :列出活动用户,及其IP");
			System.out.println("listuser : 列出所有注册用户及其工作目录");
			System.out.println("help :显示 帮助信息");
		}
		else if(conParam.equals("adduser"))
			System.out.println("adduser :增加一个注册用户");
		else if(conParam.equals("deluser"))
			System.out.println("deluser <username> :删除一个注册用户");
		else if(conParam.equals("quit"))
			System.out.println("quit  :退出");
		else if(conParam.equals("list"))
			System.out.println("list  :列出活动用户,及其IP");
		else if(conParam.equals("listuser"))
			System.out.println("listuser : 列出所有注册用户及其工作目录");
		else if(conParam.equals("help"))
			System.out.println("help :显示 帮助信息");
		else
			return false;
		return false;
		
	} 
	boolean consoleERR()
	{
		System.out.println("错误命令!");
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
			conCmd = s.substring(0,p);//返回子串,0 ~ p-1 */
		
		if(p >= s.length() || p ==-1)
			conParam = "";
		else
			conParam = s.substring(p+1,s.length());
			
		upperCmd = conCmd.toUpperCase();//小写->大写
		
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

class FtpState//储存用户状态信息
{
	final static int FS_WAIT_LOGIN = 0;	//等待输入用户名状态
	final static int FS_WAIT_PASS = 1;	//等待输入密码状态
	final static int FS_LOGIN = 2;		//已经登陆状态
	
	final static int FTYPE_ASCII = 0;
	final static int FTYPE_IMAGE  = 1;
	final static int FMODE_STREAM = 0;
	final static int FMODE_COMPRESSED = 1;
	final static int FSTRU_FILE = 0;
	final static int FSTRU_PAGE = 1;
}
/****************************************************************************/

class UserInfo//储存用户配置信息
{
	String user;
	String password;
	String workDir;
}
