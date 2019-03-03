package com.psvm.filedownload.utils;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 简单的多线程下载器
 * 
 * @author psvm
 *
 */
public class MultiThreadDownLoader {

	public static void main(String[] args) throws Exception {
		downLoadFromUrl("http://mirror.bit.edu.cn/apache/zookeeper/zookeeper-3.4.13/zookeeper-3.4.13.tar.gz",
				"D:\\",3);
	}

	/**
	 * 多线程下载文件
	 * 
	 * @param url
	 *            下载文件对应的Url路径
	 * @param destFileFullPath
	 *            下载的文件目标存储的绝对路径
	 * @param threadNum
	 *            启动下载线程数量
	 * @throws Exception
	 */
	public static void downLoadFromUrl(String url, String destFileFullPath, int threadNum) throws Exception {
		
		URL targetUrl = URI.create(url).toURL();
		CountDownLatch countDownLatch = new CountDownLatch(threadNum);
		// 获取连接实例，并且设置连接的头部
		HttpURLConnection conn = (HttpURLConnection) targetUrl.openConnection();
		conn.setConnectTimeout(5000);
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Accept",
				"image/gif, image/jpeg, image/pjpeg, image/pjpeg, "
						+ "application/x-shockwave-flash, application/xaml+xml, "
						+ "application/vnd.ms-xpsdocument, application/x-ms-xbap, "
						+ "application/x-ms-application, application/vnd.ms-excel, "
						+ "application/vnd.ms-powerpoint, application/msword, */*");
		conn.setRequestProperty("Accept-Language", "zh-CN");
		conn.setRequestProperty("Charset", "UTF-8");
		conn.setRequestProperty("Connection", "Keep-Alive");
		conn.connect();
		// 获取下载文件的大小
		int contentLength = conn.getContentLength();
		System.out.println("文件大小："+contentLength);
		long currentTimeMillis = System.currentTimeMillis();
		conn.disconnect();
		String fileExtName = url.substring(url.lastIndexOf("/"));
		// 下载块大小
		int currentPartSize = contentLength / threadNum;
		File targetFile = new File(destFileFullPath + fileExtName);
		AtomicInteger atomicInteger = new AtomicInteger();
		
		for (int i = 0; i < threadNum; i++) {
			int startIndex = i * currentPartSize;
			if(i == threadNum -1) {
				new DownLoaderThread(url,startIndex,contentLength - startIndex ,new RandomAccessFile(targetFile, "rw"),countDownLatch,
						contentLength,atomicInteger).start();
			}else {
				new DownLoaderThread(url,startIndex,currentPartSize,new RandomAccessFile(targetFile, "rw"),countDownLatch,
						contentLength,atomicInteger).start();
			}
		}
		countDownLatch.await();
		System.out.println("下载总耗时："+(System.currentTimeMillis()-currentTimeMillis)*1.0/1000);
		System.out.println("下载完成");
	}

	public static class DownLoaderThread extends Thread {

		private String targetUrl;
		private int startWirteIndex;
		private int writeLength;
		private RandomAccessFile targetFile;
		private CountDownLatch countDownLatch;
		private int totalLength;
		private AtomicInteger atomicInteger;

		public DownLoaderThread(String targetUrl, int startWirteIndex, int writeLength, RandomAccessFile targetFile,
				CountDownLatch countDownLatch,int totalLength, AtomicInteger atomicInteger) {
			super();
			this.targetUrl = targetUrl;
			this.startWirteIndex = startWirteIndex;
			this.writeLength = writeLength;
			this.targetFile = targetFile;
			this.countDownLatch = countDownLatch;
			this.totalLength = totalLength;
			this.atomicInteger = atomicInteger;
		}

		@Override
		public String toString() {
			return "DownLoaderThread [targetUrl=" + targetUrl + ", startWirteIndex=" + startWirteIndex
					+ ", writeLength=" + writeLength + ", targetFile=" + targetFile + "]";
		}

		@Override
		public void run() {
			// 获取连接实例，并且设置连接的头部
			System.out.println(this.toString());
			HttpURLConnection conn;
			try {
				URL url = URI.create(targetUrl).toURL();
				conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(5000);
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Accept",
						"image/gif, image/jpeg, image/pjpeg, image/pjpeg, "
								+ "application/x-shockwave-flash, application/xaml+xml, "
								+ "application/vnd.ms-xpsdocument, application/x-ms-xbap, "
								+ "application/x-ms-application, application/vnd.ms-excel, "
								+ "application/vnd.ms-powerpoint, application/msword, */*");
				conn.setRequestProperty("Accept-Language", "zh-CN");
				conn.setRequestProperty("Charset", "UTF-8");
				conn.setRequestProperty("Connection", "Keep-Alive");
				conn.connect();
				InputStream inputStream = conn.getInputStream();
				// 方法阻塞 跳过流，skip过程是阻塞的
				inputStream.skip(startWirteIndex);
				targetFile.seek(startWirteIndex);
				byte[] buffer = new byte[1024];
				int readLength = 0;
				int hasWriteLength = 0;
				while (hasWriteLength < writeLength && (readLength = inputStream.read(buffer))!= -1) {
					targetFile.write(buffer, 0, readLength);
					hasWriteLength += readLength;
				}
				inputStream.close();
				targetFile.close();
				atomicInteger.addAndGet(writeLength);
				System.out.println("下载完成百分比："+atomicInteger.get()*1.0/totalLength);
			} catch (Exception e) {
				e.printStackTrace();
			}finally {
				countDownLatch.countDown();
			}

		}

	}
}
