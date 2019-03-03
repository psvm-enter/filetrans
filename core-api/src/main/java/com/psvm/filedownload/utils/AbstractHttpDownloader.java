
package com.psvm.filedownload.utils;

/**
 * @author psvm
 *
 */
public abstract class AbstractHttpDownloader implements IDownLoader {

	
	
	protected abstract void setReqUrl(String req);
	
	
	protected abstract void setDestPath(String destPath);
	
	protected abstract String getDownloaderName() ;
	
	
	
}
