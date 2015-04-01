/**
 * 
 */
package com.myapp.getlocation.handler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;

import android.widget.Toast;

import com.myapp.getlocation.Constants;
import com.myapp.getlocation.activity.SplashActivity;
import com.myapp.getlocation.http.HttpServiceHandler;
import com.myapp.getlocation.util.FileUtil;

/**
 * @author Jiang
 *
 *
 */
public class DownFileHandler implements HttpServiceHandler {
	/**
	 * 在服务准备阶段，获取文件
	 */
	@Override
	public void onHttpServicePrepare(HttpResponse response) {
		if (response == null) {
			return ;
		}
		
		InputStream inputStream = null;
		try {
			inputStream = response.getEntity().getContent();
			FileUtil fileUtil = new FileUtil();
			File resultFile = fileUtil.write2SDFromInput(Constants.SCENIC_ROUTER_FILE_PATH, 
					Constants.SCENIC + Constants.ALL_SCENIC_ZIP, inputStream);
		} catch (IOException e) {
			
		} catch (Exception e) {
			
		} finally {
			try {
				if (inputStream != null)
					inputStream.close();
			} catch (Exception e2) {
				
			}
		}
	}

	@Override
	public void onHttpServiceFinished(HttpResponse response) {
		
	}

	@Override
	public void onHttpServiceError(Exception e) {
		
	}
}
