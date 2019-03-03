package com.psvm.filedownload.utils;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

/**
 * 多线程下载第二版本  使用http协议特性-range
 * @author psvm
 *
 */
public class MultiThreadDownLoaderV2 {

	public static void main(String[] args) throws Exception {
//		downLoader("http://mirror.bit.edu.cn/apache/zookeeper/zookeeper-3.4.13/zookeeper-3.4.13.tar.gz",
//				"D:\\",3);
		singleThreadDownLoader("http://mirror.bit.edu.cn/apache/zookeeper/zookeeper-3.4.13/zookeeper-3.4.13.tar.gz",
				"D:\\");
	}
	
	
	
	public static void singleThreadDownLoader(String url,String filePath) throws Exception{
		URL reqUrl = URI.create(url).toURL();
		String destFullPath = filePath+url.substring(url.lastIndexOf("/"));
		HttpURLConnection conn = (HttpURLConnection) reqUrl.openConnection();
		conn.setConnectTimeout(5000);
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Accept-Language", "zh-CN");
		conn.setRequestProperty("Charset", "UTF-8");
		conn.setRequestProperty("Connection", "Keep-Alive");
		long currentTimeMillis = System.currentTimeMillis();
		
		FileOutputStream fos = new FileOutputStream((destFullPath));
		
		InputStream ins = conn.getInputStream();
		
		byte[] buffer = new byte[1024];
		int len = 0;
		while((len = ins.read(buffer))!=-1) {
			fos.write(buffer, 0, len);
		}
		
		conn.disconnect();
		fos.close();
		
		System.out.println("下载总耗时："+(System.currentTimeMillis()-currentTimeMillis)*1.0/1000);
		
	}
	/**
	 * http 
	 * 		1、请求get下载文件，若是支持范围下载，则返回头部含有Accept-Ranges: bytes,则说明服务器端支持范围下载
	 * 		2、或者直接在请求的时候设置请求头部 Range：bytes=0-1024，若返回状态码位206说明其支持范围下载
	 * @param url						httpUrl请求路径
	 * @param filePath					下载文件的存放路径
	 * @param threadNum					下载的线程数目
	 * @throws Exception
	 */
	public static void downLoader(String url,String filePath,int threadNum) throws Exception {
		CountDownLatch countDownLatch = new CountDownLatch(threadNum);
		URL reqUrl = URI.create(url).toURL();
		String destFullPath = filePath+url.substring(url.lastIndexOf("/"));
		HttpURLConnection conn = (HttpURLConnection) reqUrl.openConnection();
		conn.setConnectTimeout(5000);
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Range",
				"bytes=0-1024");
		conn.setRequestProperty("Accept-Language", "zh-CN");
		conn.setRequestProperty("Charset", "UTF-8");
		conn.setRequestProperty("Connection", "Keep-Alive");
		int responseCode = conn.getResponseCode();
		
		long currentTimeMillis = System.currentTimeMillis();
		if(responseCode == 206) {			//支持范围请求
			
			//支持范围请求
			String headerField = conn.getHeaderField("Content-Range");
			long contentlength = Long.parseLong(headerField.substring(headerField.indexOf("/")+1));
			conn.disconnect();
			if(contentlength > 0) {
				RandomAccessFile randomAccessFile = new RandomAccessFile(destFullPath, "rw");
				randomAccessFile.setLength(contentlength);
				randomAccessFile.close();
				long blockSize = (contentlength/threadNum == 0) ? (contentlength/threadNum):((contentlength/threadNum) +1);
				for (int i = 0; i < threadNum; i++) {
					long startIndex = blockSize*i;
					long endIndex = (startIndex + blockSize >contentlength)? (startIndex+blockSize-1): contentlength;
					new PartDownLoader(url,startIndex,endIndex,new RandomAccessFile(destFullPath, "rw"),countDownLatch).start();
				}
			}else {
				System.out.println("文件大小为空");
			}
			
		}else {			//不支持范围请求
			//若是不支持范围请求，则使用单线程下载
		}
		
		countDownLatch.await();
		System.out.println("下载总耗时："+(System.currentTimeMillis()-currentTimeMillis)*1.0/1000);
		
	}
	
	static class PartDownLoader extends Thread{
		
		private String url;
		private long startIndex;
		private long endIndex;
		private CountDownLatch countDownLatch;
		private RandomAccessFile targetFile;
		
		public PartDownLoader(String url, long startIndex, long endIndex, RandomAccessFile targetFile,CountDownLatch countDownLatch) {
			this.url = url;
			this.startIndex = startIndex;
			this.endIndex = endIndex;
			this.targetFile = targetFile;
			this.countDownLatch = countDownLatch;
		}

		@Override
		public void run() {
			try {
				URL reqUrl = URI.create(url).toURL();
				HttpURLConnection conn = (HttpURLConnection) reqUrl.openConnection();
				String range = "bytes="+startIndex+"-"+endIndex;
				conn.setConnectTimeout(5000);
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Range",
						range);
				conn.setRequestProperty("Accept-Language", "zh-CN");
				conn.setRequestProperty("Charset", "UTF-8");
				conn.setRequestProperty("Connection", "Keep-Alive");
				int responseCode = conn.getResponseCode();
				if(responseCode == 206) {//返回正常状态码
					InputStream inputStream = conn.getInputStream();
					targetFile.seek(startIndex);
					int len = 0;
					byte[] buff = new byte[1024];
					while((len = inputStream.read(buff))!= -1) {
						targetFile.write(buff, 0, len);
					}
				}else {
					System.out.println("下载失败");
				}
				System.out.println("下载完成");
				conn.disconnect();
				countDownLatch.countDown();
				targetFile.close();
			} catch (Exception e) {
				e.printStackTrace();
				countDownLatch.countDown();
			}
		}
	}
	
}
