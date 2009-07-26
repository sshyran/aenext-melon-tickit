package tickit.cinema;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class TickitCinemaEnterZipcode extends Activity {
	////////////////////////////////////////////////////////////////////// Global Variables
	private Editable data;
	////////////////////////////////////////////////////////////////////// Handler
    Handler handler=new Handler() { 
        @Override 
        public void handleMessage(Message msg) { 
        	switch(msg.what){
        	case AenextSQALib.AERR_Application:
        		Toast.makeText(TickitCinemaEnterZipcode.this, 
        				"Sorry, Tickit Cinema service is temporarily unavailable this moment. Please retry later!",
        				Toast.LENGTH_LONG).show();
            	finish();        		
        		break;
        	}        	
        } 
      };	
	////////////////////////////////////////////////////////////////////// onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {    	
    	try {    		
    		super.onCreate(savedInstanceState);
    		setContentView(R.layout.enter_zipcode);
    		init_screen();
    	}catch (Exception e){    		
    		AenextSQALib.report_error(handler, this, AenextSQALib.AERR_Application,"", e);
    	}
    }
    ////////////////////////////////////////////////////////////////////// init_screen
    protected void init_screen(){        
        Button btn0 = (Button) findViewById(R.id.btn0);
        Button btn1 = (Button) findViewById(R.id.btn1);
        Button btn2 = (Button) findViewById(R.id.btn2);
        Button btn3 = (Button) findViewById(R.id.btn3);
        Button btn4 = (Button) findViewById(R.id.btn4);
        Button btn5 = (Button) findViewById(R.id.btn5);
        Button btn6 = (Button) findViewById(R.id.btn6);
        Button btn7 = (Button) findViewById(R.id.btn7);
        Button btn8 = (Button) findViewById(R.id.btn8);
        Button btn9 = (Button) findViewById(R.id.btn9);
        Button btnok 		= (Button) findViewById(R.id.btnok);
        Button btncancel 	= (Button) findViewById(R.id.btncancel);
        Button btnclear 	= (Button) findViewById(R.id.btnclear);
        final EditText txtzipcode	= (EditText) findViewById(R.id.txtzipcode);
        
        btn0.setOnClickListener(new Button.OnClickListener() { public void onClick(View v) { data=txtzipcode.getText();data.append('0');}});
        btn1.setOnClickListener(new Button.OnClickListener() { public void onClick(View v) { data=txtzipcode.getText();data.append('1');}});
        btn2.setOnClickListener(new Button.OnClickListener() { public void onClick(View v) { data=txtzipcode.getText();data.append('2');}});
        btn3.setOnClickListener(new Button.OnClickListener() { public void onClick(View v) { data=txtzipcode.getText();data.append('3');}});
        btn4.setOnClickListener(new Button.OnClickListener() { public void onClick(View v) { data=txtzipcode.getText();data.append('4');}});
        btn5.setOnClickListener(new Button.OnClickListener() { public void onClick(View v) { data=txtzipcode.getText();data.append('5');}});
        btn6.setOnClickListener(new Button.OnClickListener() { public void onClick(View v) { data=txtzipcode.getText();data.append('6');}});
        btn7.setOnClickListener(new Button.OnClickListener() { public void onClick(View v) { data=txtzipcode.getText();data.append('7');}});
        btn8.setOnClickListener(new Button.OnClickListener() { public void onClick(View v) { data=txtzipcode.getText();data.append('8');}});
        btn9.setOnClickListener(new Button.OnClickListener() { public void onClick(View v) { data=txtzipcode.getText();data.append('9');}});        
        btnclear.setOnClickListener(new Button.OnClickListener() 
        { 
        	public void onClick(View v) 
        	{ 
        		
        		data=txtzipcode.getText();
        		if (data.length()>0)
        		{
        			data.delete(txtzipcode.getSelectionStart()-1,txtzipcode.getSelectionStart());        			
        		}
        	}
        }); 
        btncancel.setOnClickListener(new Button.OnClickListener() 
        { 
        	public void onClick(View v) 
        	{
        		//User cancel
                setResult(TickitCinemaStarter.TICKIT_RETURN_TO_PREV_ACTIVITY);
                finish();
        	}
        });
        btnok.setOnClickListener(new Button.OnClickListener() 
        { 
        	public void onClick(View v) 
        	{   //User OK      		        		
                setResult(TickitCinemaStarter.TICKIT_RESTART,new Intent().putExtra("default_location", txtzipcode.getText().toString()));
                finish();        		
        	}
        });
        
    }
    ////////////////////////////////////////////////////////////////////// END
}