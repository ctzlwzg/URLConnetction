package wzg;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownUtil {
	private String path;
	private String targeFile;
	private int threadNum;
	private DownThread[] threads;
	private int fileSize;
	public DownUtil(String path, String targeFile, int threadNum) {
		this.path = path;
		this.threadNum = threadNum;
		threads=new DownThread[threadNum];
		this.targeFile = targeFile;
	}
	
	public void download() throws Exception{
		URL url=new URL(path);
		HttpURLConnection conn=(HttpURLConnection)url.openConnection();
		conn.setConnectTimeout(5*1000);
		conn.setRequestMethod("GET");
		conn.setRequestProperty(
		"Accept",
		"image/gif,image/jpeg,image/pjpeg,image/pjpeg"
		+"application/x-shockwave-flash,application/xaml+xml,"
		+"application/vnd.ms-xpsdocument,application/x-ms-xbap,"
		+"application/x-ms-application,application/vnd.ms-excel,"
		+"application/vnd.ms-powerpoint,application/msword,*/*");
		
		conn.setRequestProperty("Accept-Language", "zh-CN");
		conn.setRequestProperty("Charset", "UTF-8");
		conn.setRequestProperty("Connection", "Keep-Alive");
		
		fileSize=conn.getContentLength();
		conn.disconnect();//关闭Http连接
		int currentPartSize=fileSize/threadNum+1;
		RandomAccessFile file=new RandomAccessFile(targeFile, "rw");
		//设置本地文件大小
		file.setLength(fileSize);
		file.close();
		for (int i = 0; i <threadNum; i++) {
			//计算每个线程下载的开始位置
			int startPos=i*currentPartSize;
			//每个线程使用一个RandomAccessFile进行下载
			RandomAccessFile currentPart=new RandomAccessFile(targeFile, "rw");
			//定位该线程的下载位置
			currentPart.seek(startPos);
			//创建下载线程
			threads[i]=new DownThread(startPos,currentPartSize,currentPart);
			threads[i].start();
		}
	}
	public double getCompletRate(){
		//统计多少线程已经下载的总大小
		int sumSize=0;
		for (int i = 0; i < threadNum; i++) {
			sumSize+=threads[i].length;
		}
		//返回已经完成的百分比
		return sumSize*1.0/fileSize;
	}
	
	private class DownThread extends Thread{
		//当前线程的下载位置
		private int startPos;
		//定义当前线程负责下载的文件大小
		private int currentPartSize;
		//当前线程需要下载的文件快
		private RandomAccessFile currentPart;
		//定义该线程已经下载的字节数
		public int length;
		public DownThread(int startPos, int currentPartSize, RandomAccessFile currentPart) {
			this.startPos = startPos;
			this.currentPartSize = currentPartSize;
			this.currentPart = currentPart;
		}
		@Override
		public void run() {
			try {
				URL url=new URL(path);
				HttpURLConnection conn=(HttpURLConnection)url.openConnection();
				conn.setConnectTimeout(5*1000);
				conn.setRequestMethod("GET");
				conn.setRequestProperty(
				"Accept",
				"image/gif,image/jpeg,image/pjpeg,image/pjpeg"
				+"application/x-shockwave-flash,application/xaml+xml,"
				+"application/vnd.ms-xpsdocument,application/x-ms-xbap,"
				+"application/x-ms-application,application/vnd.ms-excel,"
				+"application/vnd.ms-powerpoint,application/msword,*/*");
				
				conn.setRequestProperty("Accept-Language", "zh-CN");
				conn.setRequestProperty("Charset", "UTF-8");
				//跳过startPos个节点，表明该线程只下载自己负责的那部分文件
				InputStream inStream=conn.getInputStream();
				inStream.skip(this.startPos);
				byte[]buffer=new byte[1024];
				int hasRead=0;
				//读取网络数据，并写入本地文件
				while(length<currentPartSize&&(hasRead=inStream.read(buffer))!=-1){
					currentPart.write(buffer, 0, hasRead);
					//累积该线程下载的总大小
					length+=hasRead;
				}
				currentPart.close();
				inStream.close();
						
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
	}
}
