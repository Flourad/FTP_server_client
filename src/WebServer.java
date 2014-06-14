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
			//����һ����80�˿ڰ󶨵ķ�����
			server=new ServerSocket(PORT); 
			System.out.println("Web Server is listening on port "+server.getLocalPort());
			for (;;) {
				client=server.accept(); //���ܿͻ�������������
				new ConnectionThread(client,i).start(); 
				i++;
			}
		} catch (Exception e) {System.out.println(e);}
		}
	}

/* ConnnectionThread�������һ��Web�������ͨ�� */
class ConnectionThread extends Thread {
	Socket client; //����Web�������socket��
	int counter; //������
	public ConnectionThread(Socket cl,int c) {
		client=cl;
		counter=c;
	}
	public void run() //�߳���
	{
		try {
			//��ȡ�ͻ����ĵ�ַ��IP+�˿ں�
			String destIP=client.getInetAddress().toString(); //�ͻ���IP��ַ
			int destport=client.getPort(); //�ͻ����˿ں�
			System.out.println("Connection "+counter+":connected to "+destIP+" on port "+destport+".");
			String content="Connection "+counter+":connected to "+destIP+" on port "+destport+",";
			//дlogString�ַ�����./logĿ¼�µ��ļ���
			writelog(content);
			
			PrintStream outstream=new PrintStream(client.getOutputStream());
			DataInputStream instream=new DataInputStream(client.getInputStream());
			String inline=instream.readLine(); //��ȡWeb������ύ��������Ϣ
			System.out.println("Received:"+inline);
			
			if (getrequest(inline)) { //�����GET����
				String filename=getfilename(inline);
				File file=new File(filename);
				if (file.exists()) { //���ļ����ڣ����ļ��͸�Web�����
					String sid=getsessionid(inline);//��ȡweb������ύ��sessionid
					sid=session(sid);//����sessionid
					System.out.println(filename+" requested.");
	
					outstream.println("HTTP/1.0 200 OK");
					outstream.println("MIME_version:1.0");
					outstream.println("Content_Type:text/html");
					int len=(int)file.length();
					outstream.println("Content_Length:"+len);
					outstream.println("");
					sendfile(outstream,file); //�����ļ�
					outstream.flush();
				} else { //�ļ�������ʱ
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
			while (m1<11100000) {m1++;} //��ʱ
			client.close();
		} catch (IOException e) {
			System.out.println("Exception:"+e);
		}
	}

	/* ��ȡ���������Ƿ�Ϊ��GET�� */
	boolean getrequest(String s) { 
		if (s.length()>0){
			if (s.substring(0,3).equalsIgnoreCase("GET")) 
				return true;
		}
		return false;
	}

	/* ��ȡҪ���ʵ��ļ��� (�����ж���������ʽ)*/
	String getfilename(String s) {
		String f=s.substring(s.indexOf(' ')+1);
		int i=f.indexOf(";");
		if(i!=-1){
			f=f.substring(0,i);/*GET ���Ϊ��GET /index.html;jssession=57433735734 HTTP/1.1 */
			if (f.charAt(0)=='/')
				f=f.substring(1);
		}
		else {
			f=f.substring(0,f.indexOf(' ')); /*GET ���Ϊ��GET /index.html HTTP/1.1 */
			if (f.charAt(0)=='/')
				f=f.substring(1);
		} 
		if (f.equals("")) 
			f="index.html";/*GET ���Ϊ��GET / HTTP/1.1 */
		return f;
	}
	
	static void writelog(String content) {
		String fileName = "./log.txt";
		Date date=new Date();
		SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String logcontent = content+df.format(date).toString();
		//������A׷���ļ�
		appendMethodA(fileName, logcontent);
		appendMethodA(fileName, "\n\r");
		//��ʾ�ļ�����
	}
	
	/*ʹ��RandomAccessFile����׷���ļ�*/
	public static void appendMethodA(String fileName, String content){
		try {
			// ��һ����������ļ���������д��ʽ
			RandomAccessFile randomFile = new RandomAccessFile(fileName, "rw");
			// �ļ����ȣ��ֽ���
			long fileLength = randomFile.length();
			//��д�ļ�ָ���Ƶ��ļ�β��
			randomFile.seek(fileLength);
			randomFile.writeBytes(content);
			randomFile.close();
		} catch (IOException e){
			e.printStackTrace();
		}
	}
	
	/* ��ȡsessionid */
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


	/*��ָ���ļ����͸�Web����� */ 
	void sendfile(PrintStream outs,File file) {
		try {
			DataInputStream in=new DataInputStream(new FileInputStream(file));
			int len=(int)file.length();
			byte buf[]=new byte[len];
			in.readFully(buf);//���ļ����ݶ���buf��
			outs.write(buf,0,len);
			outs.flush();
			in.close();
		}catch (Exception e) {
			System.out.println("Error retrieving file.");
			System.exit(1);
		}
	}
	// session����
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
	
	//����session(��hashtableʵ��)
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
	//����session��Ϣ ��get()ʵ��
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
//�������hashtable�������������ʽ���е�ֵ
class ssvalue{
	String s1;
	String ssvalue(){
          return s1;
	}
}
//�������hashtable�������������ʽ���еļ����루�����sessionid��
//����8λ����ַ���
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

//������������̹߳���hashtable������
class  ghashtable{
	static final  Hashtable  HT=new Hashtable();
}
