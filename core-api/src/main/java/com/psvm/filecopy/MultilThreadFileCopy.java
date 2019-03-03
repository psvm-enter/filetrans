package com.psvm.filecopy;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 多线程 大文件复制文件复制
 * @author psvm
 *
 */
public class MultilThreadFileCopy {
	
	public static void main(String[] args) throws Exception {
		CopyBigFileByMultiThread("D:\\CentOS-6.5-x86_64-bin-DVD1.iso","D:\\123.iso",1);
	}
	
	private static AtomicLong atomicLong= new AtomicLong();
	
	public static void CopyBigFileByMultiThread(String srcFilePath,String targetFilePath,int threadNum) throws Exception {
		File file = new File(srcFilePath);
		long fileLength = file.length();
		long blockSize = (fileLength/threadNum ==0) ?(fileLength/threadNum) : (fileLength/threadNum+1);
		long startIndex = 0;
		long endIndex = 0;
		CountDownLatch countDownLatch = new CountDownLatch(threadNum);
		long currentTimeMillis = System.currentTimeMillis();
		for (int i = 0; i < threadNum; i++) {
			if(i == threadNum-1) {
				startIndex = i*blockSize;
				endIndex = fileLength-1;
				new FileCopyThread("threadName-"+i,targetFilePath,srcFilePath,startIndex,endIndex,countDownLatch).start();
			}else {
				startIndex = i*blockSize;
				endIndex = blockSize+startIndex;
				new FileCopyThread("threadName-"+i,targetFilePath,srcFilePath,startIndex,endIndex,countDownLatch).start();
			}
		}
		getProgress(fileLength);
		countDownLatch.await();
		System.out.println("共计时："+(System.currentTimeMillis()-currentTimeMillis)/1000+"秒");
	}
	
	public static void getProgress(long total) throws Exception {
		while(atomicLong.get() * 1.0 /total < 1) {
			 Thread.sleep(1000);
			 if(atomicLong.get() > 1000) {
				 System.out.println("当前复制百分比："+atomicLong.get() * 1.0 /total);
				 
			 }
		}
		 System.out.println("当前复制百分比："+atomicLong.get() * 1.0 /total);
	}
	
	public static class FileCopyThread extends Thread{
		private String targetFilePath;
		private String srcFilePath;
		private long startIndex;	//开始位置
		private long endIndex;		//结束位置
		CountDownLatch countDownLatch;
		private String threadName;
		public FileCopyThread(String threadName,String targetFile, String srcFile, long startIndex, long endIndex,CountDownLatch countDownLatch) {
			super();
			this.threadName = threadName;
			this.targetFilePath = targetFile;
			this.srcFilePath = srcFile;
			this.startIndex = startIndex;
			this.endIndex = endIndex;
			this.countDownLatch = countDownLatch;
		}
		@Override
		public void run() {
			try {
				System.out.println("当前线程-"+threadName+"复制数据范围："+startIndex+"------"+endIndex);
				RandomAccessFile targetFile = new RandomAccessFile(targetFilePath, "rw");
				RandomAccessFile srcFile = new RandomAccessFile(srcFilePath, "rw");
				targetFile.seek(startIndex);
				srcFile.seek(startIndex);
				System.out.println("");
				byte[] buffer = new byte[1024*8];
				int len = 0;
				while((len = srcFile.read(buffer))!= -1) {
					targetFile.write(buffer, 0, len);
					atomicLong.addAndGet(len);
					if(targetFile.getFilePointer() > endIndex) {
						System.out.println(threadName+"---线程读取完毕");
						break;
					}
				}
				targetFile.close();
				srcFile.close();
				countDownLatch.countDown();
			} catch (Exception e) {
				e.printStackTrace();
				countDownLatch.countDown();
			}
		}
	}
}
