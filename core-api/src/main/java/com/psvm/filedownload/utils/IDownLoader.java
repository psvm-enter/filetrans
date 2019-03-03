package com.psvm.filedownload.utils;

/**
 * 文件下载器接口
 * @author psvm
 *
 */
public interface IDownLoader {

	/**
	 * 开始下载
	 */
	void start();
	
	/**
	 * 停止下载
	 */
	void stop();
	
	/**
	 * 取消下载
	 */
	void cancel();
	
	
}
