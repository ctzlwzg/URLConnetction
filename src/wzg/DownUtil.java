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
		conn.disconnect();//�ر�Http����
		int currentPartSize=fileSize/threadNum+1;
		RandomAccessFile file=new RandomAccessFile(targeFile, "rw");
		//���ñ����ļ���С
		file.setLength(fileSize);
		file.close();
		for (int i = 0; i <threadNum; i++) {
			//����ÿ���߳����صĿ�ʼλ��
			int startPos=i*currentPartSize;
			//ÿ���߳�ʹ��һ��RandomAccessFile��������
			RandomAccessFile currentPart=new RandomAccessFile(targeFile, "rw");
			//��λ���̵߳�����λ��
			currentPart.seek(startPos);
			//���������߳�
			threads[i]=new DownThread(startPos,currentPartSize,currentPart);
			threads[i].start();
		}
	}
	public double getCompletRate(){
		//ͳ�ƶ����߳��Ѿ����ص��ܴ�С
		int sumSize=0;
		for (int i = 0; i < threadNum; i++) {
			sumSize+=threads[i].length;
		}
		//�����Ѿ���ɵİٷֱ�
		return sumSize*1.0/fileSize;
	}
	
	private class DownThread extends Thread{
		//��ǰ�̵߳�����λ��
		private int startPos;
		//���嵱ǰ�̸߳������ص��ļ���С
		private int currentPartSize;
		//��ǰ�߳���Ҫ���ص��ļ���
		private RandomAccessFile currentPart;
		//������߳��Ѿ����ص��ֽ���
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
				//����startPos���ڵ㣬�������߳�ֻ�����Լ�������ǲ����ļ�
				InputStream inStream=conn.getInputStream();
				inStream.skip(this.startPos);
				byte[]buffer=new byte[1024];
				int hasRead=0;
				//��ȡ�������ݣ���д�뱾���ļ�
				while(length<currentPartSize&&(hasRead=inStream.read(buffer))!=-1){
					currentPart.write(buffer, 0, hasRead);
					//�ۻ����߳����ص��ܴ�С
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
