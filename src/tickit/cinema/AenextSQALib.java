package tickit.cinema;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class AenextSQALib 
{
    public static final int AERR_AenextHTTPLib_http_request 					= 1;
    
    public static final int AERR_AenextUtilityLib_BuildDayList 					= 2;
    public static final int AERR_AenextUtilityLib_getHTTPCachedImage			= 3;
    public static final int AERR_AenextUtilityLib_requestHTTPImageWithCached	= 4;
    public static final int AERR_AenextUtilityLib_getStringFromDoc				= 5;
    public static final int AERR_AenextUtilityLib_format_double					= 6;
    	
    public static final int AERR_Application									= 7;
    public static final int AERR_Handler										= 8;
    
    //private static String uri="http://tickit-cinema-sqa-1-0-1.aenext.com/tickit_cinema_requests/report_error/0";
    //private static String uri="http://10.35.54.2/tickit_cinema_requests/report_error/0";
    //private static String uri="http://192.168.1.8:3000/tickit_cinema_requests/report_error/0";
    private static String uri="http://aenext.dyndns.info:2081/tickit_cinema_requests/report_error/0";
    //################################################################################################################# report_error
    //Usage Examples: AenextSQALib.report_error(handler, ctx, AenextSQALib.AERR_AenextHTTPLib_http_request,uri, e);
    
    public static void report_error(Handler handler, Context ctx,int aenext_error_code,String extra_data,Exception e){
    	try {
    		if (aenext_error_code!=AERR_Handler){
    			Message m = new Message();		
    			m.what = aenext_error_code;    	
    			handler.sendMessage(m);
    		}
    		transmit_error(aenext_error_code,extra_data,e);
    	} catch(Exception exception){
    		exception.printStackTrace();
    	}
	}	
  //################################################################################################################# transmit_error
    public static void transmit_error(int aenext_error_code,String extra_data,Exception e){
		try {
			String source_phone_number="source_phone_number=";
	    	String error_code		="error_code="+Integer.toString(aenext_error_code);
	    	String error_message	="error_message=";
	    	String error_detail		="error_detail=";
	    	String error_extra		="error_extra=";
	    	
	    	//Error_message
	    	error_message=error_message+URLEncoder.encode(e.toString(),"UTF-8");
	    	//Log.d("AenextSQALib",e.toString());
	    	
	    	//Error_detail
	        for(int i=0;i<e.getStackTrace().length;i++){
	        	//Log.d("AenextSQALib",e.getStackTrace()[i].toString());
	        	error_detail=error_detail+URLEncoder.encode(e.getStackTrace()[i].toString()+"|","UTF-8");
	        }
	        
	        //Error_extra
	        error_extra=error_extra+URLEncoder.encode(extra_data);
	        //Log.d("AenextSQALib",extra_data);
	        
	        //Assemble URI
	        uri=uri + "?" + source_phone_number + "&" + error_code + "&" + error_extra  + "&" + error_message + "&" + error_detail;
	        
			URL url;
			url = new URL(uri);
			URLConnection 			urlC  	= url.openConnection();			
			Log.d("AenextSQALib",uri);
			Log.d("AenextSQALib",Integer.toString(urlC.getContentLength()));
			
		} catch (Exception exception) { 
			exception.printStackTrace();
		}
				        
    }
    //################################################################################################################# END
}
