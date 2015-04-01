package com.myapp.getlocation.util;

import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.myapp.getlocation.entity.SectionPointsModel;
import com.myapp.getlocation.entity.ServiceState;
import com.myapp.getlocation.entity.SpotPointsModel;


/**
 */
public class HttpUtil {
    private static final int SET_CONNECTION_TIMEOUT = 5 * 1000;

    public static int doPost(String stringUrl, String json) { 
		String result = null; 
		HttpPost post = new HttpPost(stringUrl); 
		HttpResponse httpResponse = null; 
		try { 
			Log.d("before POST", stringUrl+"---"+json);
			StringEntity entity=new StringEntity(json,HTTP.UTF_8); 
			entity.setContentType("application/json"); 
			post.setEntity(entity); 
			
			httpResponse = new DefaultHttpClient().execute(post); 
			return httpResponse.getStatusLine().getStatusCode() ; 
		} catch (Exception e) { 
			e.printStackTrace(); 
			return 0; 
		} 
	}
    
	public static int postByRestTemplate(String stringUrl, ArrayList<SectionPointsModel> pointList) {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
		restTemplate.getMessageConverters().add(
				new MappingJackson2HttpMessageConverter());
		// restTemplate.getMessageConverters().add(new
		// StringHttpMessageConverter());
		restTemplate.getMessageConverters().add(new FormHttpMessageConverter());

		ServiceState rr = restTemplate.postForObject(stringUrl, pointList,
				ServiceState.class);

		// HttpHeaders requestHeaders = new HttpHeaders();
		// requestHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		//
		// HttpEntity requestEntity = new HttpEntity(pointList,requestHeaders);
		// ResponseEntity<ServiceState> response =
		// restTemplate.exchange(stringUrl, HttpMethod.POST, requestEntity,
		// ServiceState.class);
		// ServiceState result = response.getBody();
		//
		// return result.getStateCode();
		return rr.getStateCode();
	}

	public static int postListByRestTemplate(String stringUrl, ArrayList<SpotPointsModel> pointList) {

		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
		restTemplate.getMessageConverters().add(
				new MappingJackson2HttpMessageConverter());
		restTemplate.getMessageConverters().add(
				new StringHttpMessageConverter());
		restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
		// ArrayList<SpotPointsModel> list = new ArrayList<SpotPointsModel>();
		// SpotPointsModel ss = new SpotPointsModel();
		// ss.setSpotId("188");
		// ss.setId(188);
		// ss.setSpotType("1");
		// Points pt = new Points(122.33,11.021,112.00);
		// pt.setAbsoluteAltitude(1222.001);
		// ArrayList lp = new ArrayList();
		// lp.add(pt);
		// ss.setSpotPoints(lp);
		// list.add(ss);

		ServiceState rr = restTemplate.postForObject(stringUrl, pointList,
				ServiceState.class);

		// HttpHeaders requestHeaders = new HttpHeaders();
		// requestHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		//
		// HttpEntity requestEntity = new HttpEntity(list,requestHeaders);
		// ResponseEntity<String> response = restTemplate.exchange(stringUrl,
		// HttpMethod.POST, requestEntity, String.class);
		// String result = response.getBody();

		return rr.getStateCode();
	}
	
	public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null)
            return false;
        NetworkInfo netinfo = cm.getActiveNetworkInfo();
        return netinfo != null && netinfo.isConnected();
    }
   
}
