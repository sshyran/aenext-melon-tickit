package tickit.cinema;

import java.net.URL;
import java.net.URLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import android.content.Context;
import android.os.Handler;
//import android.util.Log;

public class AenextHTTPLib {
	public final static String ROOT_URI="http://aenext.dyndns.info:2081";
	//public final static String ROOT_URI="http://aenext.dyndns.info:3000";
	//public final static String ROOT_URI="http://tickit-cinema-api.aenext.com";
	//public final static String ROOT_URI="http://tickit-cinema-api-1-0-1.aenext.com";
	//public final static String ROOT_URI="http://10.35.54.2";
	//public final static String ROOT_URI="http://192.168.1.8:3000";
	//################################################################################################### http_request
	public static Document http_request(Handler handler, Context ctx,String uri){
		try {
			//Log.d("AenextHTTP","START REQUEST HTTP");
			//Log.d("AenextHTTP",uri);
			
			URL 					url		= new URL(uri);
			URLConnection 			urlC  	= url.openConnection();		
								
			//Log.d("test",Integer.toString(urlC.getContentLength()));
			
			DocumentBuilderFactory 	dcf = DocumentBuilderFactory.newInstance();
			DocumentBuilder 		db  = dcf.newDocumentBuilder();									
			Document 				doc = db.parse(urlC.getInputStream());			
			
			//Log.d("AenextHTTP","FINISH REQUEST HTTP");
			return doc;
		} 
		catch (Exception e){
			AenextSQALib.report_error(handler, ctx, AenextSQALib.AERR_AenextHTTPLib_http_request,uri, e);
			return null;
		}
		
	}
	//################################################################################################### END
}

