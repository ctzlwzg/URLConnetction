package wzg;

public class Test {
	public static void main(String[] args) throws Exception {
		DownUtil downUtil=new DownUtil("http://www.crazyit.org/"
			+"attachments/month_1403/1403202355ff6cc9a4fbf6f14a.png",
			"ios.png", 4);
		downUtil.download();
		new Thread(() ->{
			while(downUtil.getCompletRate()<1){
				System.out.println("ÒÔÍê³É£º"+downUtil.getCompletRate());
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
		
	}
}
