import java.util.*;
import java.io.*;
import java.net.*;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;
import java.text.SimpleDateFormat;

public class WebServer {
	
	static ServerSocket server=null;
	static Socket client=null;

	public static void web_main() {
		int i=1, PORT=80;	
		try {
			//创建一个与80端口绑定的服务器
			server=new ServerSocket(PORT); 
			System.out.println("Web Server is listening on port "+server.getLocalPort());
			for (;;) {
				client=server.accept(); //接受客户机的连接请求
				new ConnectionThread(client,i).start(); 
				i++;
			}
		} catch (Exception e) {System.out.println(e);}
		}
	}

/* ConnnectionThread类完成与一个Web浏览器的通信 */
class ConnectionThread extends Thread {
	Socket client; //连接Web浏览器的socket字
	int counter; //计数器
	public ConnectionThread(Socket cl,int c) {
		client=cl;
		counter=c;
	}
	public void run() //线程体
	{
		try {
			//获取客户机的地址：IP+端口号
			String destIP=client.getInetAddress().toString(); //客户机IP地址
			int destport=client.getPort(); //客户机端口号
			System.out.println("Connection "+counter+":connected to "+destIP+" on port "+destport+".");
			String content="Connection "+counter+":connected to "+destIP+" on port "+destport+",";
			//写logString字符串到./log目录下的文件中
			writelog(content);
			
			PrintStream outstream=new PrintStream(client.getOutputStream());
			DataInputStream instream=new DataInputStream(client.getInputStream());
			String inline=instream.readLine(); //读取Web浏览器提交的请求信息
			System.out.println("Received:"+inline);
			
			if (getrequest(inline)) { //如果是GET请求
				String filename=getfilename(inline);
				File file=new File(filename);
				if (file.exists()) { //若文件存在，则将文件送给Web浏览器
					String sid=getsessionid(inline);//获取web浏览器提交的sessionid
					sid=session(sid);//处理sessionid
					System.out.println(filename+" requested.");
	
					outstream.println("HTTP/1.0 200 OK");
					outstream.println("MIME_version:1.0");
					outstream.println("Content_Type:text/html");
					int len=(int)file.length();
					outstream.println("Content_Length:"+len);
					outstream.println("");
					sendfile(outstream,file); //发送文件
					outstream.flush();
				} else { //文件不存在时
					String notfound="<html><head><title>Not Found</title></head><body><h1>Error 404-file not found</h1></body></html>";
					outstream.println("HTTP/1.0 404 no found");
					outstream.println("Content_Type:text/html");
					outstream.println("Content_Length:"+notfound.length()+2);
					outstream.println("");
					outstream.println(notfound);
					outstream.flush();
				}
			}
			long m1=1; 
			while (m1<11100000) {m1++;} //延时
			client.close();
		} catch (IOException e) {
			System.out.println("Exception:"+e);
		}
	}

	/* 获取请求类型是否为“GET” */
	boolean getrequest(String s) { 
		if (s.length()>0){
			if (s.substring(0,3).equalsIgnoreCase("GET")) 
				return true;
		}
		return false;
	}

	/* 获取要访问的文件名 (可以判断三种请求方式)*/
	String getfilename(String s) {
		String f=s.substring(s.indexOf(' ')+1);
		int i=f.indexOf(";");
		if(i!=-1){
			f=f.substring(0,i);/*GET 语句为：GET /index.html;jssession=57433735734 HTTP/1.1 */
			if (f.charAt(0)=='/')
				f=f.substring(1);
		}
		else {
			f=f.substring(0,f.indexOf(' ')); /*GET 语句为：GET /index.html HTTP/1.1 */
			if (f.charAt(0)=='/')
				f=f.substring(1);
		} 
		if (f.equals("")) 
			f="index.html";/*GET 语句为：GET / HTTP/1.1 */
		return f;
	}
	
	static void writelog(String content) {
		String fileName = "./log.txt";
		Date date=new Date();
		SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String logcontent = content+df.format(date).toString();
		//按方法A追加文件
		appendMethodA(fileName, logcontent);
		appendMethodA(fileName, "\n\r");
		//显示文件内容
	}
	
	/*使用RandomAccessFile方法追加文件*/
	public static void appendMethodA(String fileName, String content){
		try {
			// 打开一个随机访问文件流，按读写方式
			RandomAccessFile randomFile = new RandomAccessFile(fileName, "rw");
			// 文件长度，字节数
			long fileLength = randomFile.length();
			//将写文件指针移到文件尾。
			randomFile.seek(fileLength);
			randomFile.writeBytes(content);
			randomFile.close();
		} catch (IOException e){
			e.printStackTrace();
		}
	}
	
	/* 获取sessionid */
	String getsessionid(String s)
	{
		String sid=s;
		int i=sid.indexOf("=");
		try{
			if(i!=-1){
				sid=sid.substring(i+1);
				sid=sid.substring(0,sid.indexOf(' '));
	        }else 
	        sid=null;
		}catch(Exception e){
			System.out.println("e.toString()");
		} 
		System.out.println("sessionid="+sid);
		return sid;
	}


	/*把指定文件发送给Web浏览器 */ 
	void sendfile(PrintStream outs,File file) {
		try {
			DataInputStream in=new DataInputStream(new FileInputStream(file));
			int len=(int)file.length();
			byte buf[]=new byte[len];
			in.readFully(buf);//将文件数据读入buf中
			outs.write(buf,0,len);
			outs.flush();
			in.close();
		}catch (Exception e) {
			System.out.println("Error retrieving file.");
			System.exit(1);
		}
	}
	// session处理
	String session(String sessionid)
	{   
		Date date=new Date();
	    SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    String timenow =df.format(date).toString();
	    ghashtable HT=new ghashtable();
	    String sid=sessionid;
	    if(sid==null){
	    	sid= createsession( timenow, HT.HT);
	    	System.out.println("create a a new session and the sessionid is "+sid);
	    }else{
	    	try{
	    		if(searchsession(sid,HT.HT)){
	    			System.out.println("yes! this session cell exsit");}
	    		else{
	    			sid= createsession( timenow,HT.HT);
	    			System.out.println("cannot find the session information,now create a new!"); 
	    			System.out.println("create a a new session and the sessionid is "+sid);
	    		}
	    	}catch(Exception e){System.out.println(e.toString());}
	    }
	    return sid;
	}
	
	//创建session(用hashtable实现)
	String createsession(String timenow,Hashtable ht)
	{
		String s=timenow;
		//Hashtable cht= ht;
		ssvalue  va=new ssvalue();
		sskey ke= new sskey();
		va.s1=s;
		String temp=(String)ke.sskey();
		ht.put(temp,va.ssvalue());
		return temp;
	}
	//搜索session信息 用get()实现
	boolean searchsession(String sessionid,Hashtable ht)
	{ 
		String sid= sessionid;
	   // Hashtable sht=ht;
		String temp=(String)ht.get(sid);
		if (temp==null){
			return false;
		}else{
			return true;
		}  
	}
}
//定义加入hashtable（必须以类的形式）中的值
class ssvalue{
	String s1;
	String ssvalue(){
          return s1;
	}
}
//定义加入hashtable（必须以类的形式）中的检索码（这就是sessionid）
//产生8位随机字符串
class sskey
{
	String sskey()
	{
		char[] cc = new char[10]; 
		for (int i = 0; i<cc.length; i++){
			while(cc[i]<'A'||cc[i]>'Z')
                	cc[i]=(char)(Math.random()*(int)'Z');
		}//end for
		String str = new String("");
		for (int i = 0; i<cc.length; i++)
			str += cc[i];
		return str;
	}
}

//用类来解决多线程公用hashtable的问题
class  ghashtable{
	static final  Hashtable  HT=new Hashtable();
}
