package com.myapp.getlocation;

import android.os.Environment;

/**
 */
public class Constants {
    public static final String SHAREDPREFERENCES_NAME = "Travel";
    public static final String API_MESSAGE_KEY = "API_MESSAGE_KEY";
    public static final String SCIENCE_ID_KEY = "SCIENCE_ID_KEY";
    public static final String INDEX_FLAG = "INDEX_FLAG";
    public static final String SCIENCE_LINE_ID_KEY = "SCIENCE_LINE_ID_KEY";
    public final static String DATABASE_NAME = "imyuu";
    //单个景区保存路径
    public static final String SCENIC_SINGLE_FILE_PATH = Environment.getExternalStorageDirectory() + "/imyuu/scenic";

    public static final String SCENIC_ROUTER_FILE_PATH = "imyuu/";
    public static final String SCENIC_IMAGE_FILE_PATH = Environment.getExternalStorageDirectory() + "/imyuu/scenic/";

    public static final String API_ALL_SCENIC_DOWNLOAD = "trip/allScenicScenicAreaAction.action";
    public static final String API_SINGLE_SCENIC_DOWNLOAD = "trip/oneScenicScenicAreaAction.action?scenicId=";
//    public static final String API_SPOT_SUBMIT = "http://192.168.1.114:8000/WebTest/jieshou";
//    public static final String API_SECTION_SUBMIT = "http://www.imyuu.com/trip/loginInfoFromAPPRegRegisterAccountAction.action";
    public static final String API_SPOT_SUBMIT = "IUURestful/restful/collection/uploadScenicSpots";
    public static final String API_SECTION_SUBMIT = "IUURestful/restful/collection/uploadScenicLines";
    
    public static final String SCENIC = "scenic";
    public static final String ALL_SCENIC_ZIP = ".zip";
    public static final String ALL_SCENIC_JSON = ".json";
    public static final String LIST_KEY_SCIENINFO_LINE_NAME = "line_name";

    /* 请求码*/
    public static final int IMAGE_REQUEST_CODE = 0;
    public static final int CAMERA_REQUEST_CODE = 1;
    public static final int DEFAULT_REQUEST_CODE = 2;
}
