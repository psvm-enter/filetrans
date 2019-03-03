/**
 * 
 */
package com.psvm.filedownload.utils;

/**
 * @author lelsie
 *
 */
public interface DownloaderListener {

	
	void onDownloadFinish();
	
	
	void onDownloadStop();
	
	
	void onDownLoadCancel();
}
