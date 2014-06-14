package vynguyen.ntpclockapp;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	private static final String NTP_SERVER_URL = "0.asia.pool.ntp.org";
	private static final int TIME_RESYNC = 1;
	private static final int REQUEST_TIMEOUT = 30000;
	
	private AsyncTask<String, Void, Long> ntpTask;
	private final int timeForResync = TIME_RESYNC * 60 * 1000; 
	private TextView countdownTimer;
	private ProgressDialog progress;
	private CountDownTimer timer = new CountDownTimer(timeForResync, 1000) {

		public void onTick(long millisUntilFinished) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(millisUntilFinished);

			countdownTimer.setText("remaining "+String.format("%d min, %d sec", 
		                    TimeUnit.MILLISECONDS.toMinutes( millisUntilFinished),
		                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - 
		                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
			
			
		}

		public void onFinish() {
			if(!progress.isShowing())
				progress = ProgressDialog.show(MainActivity.this, "Synchronizing","Please wait...", true);
			countdownTimer.setText("");
			ntpTask = new NTPOperation().execute("");
		}
		
		
	};
	
	private CustomDigitalClock customClock;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);		
		progress = ProgressDialog.show(this, "Synchronizing","Please wait...", true);
		initView();		
		ntpTask = new NTPOperation().execute("");

	}
	
	
	
	@Override
    public void onDestroy() {
        super.onDestroy();
        if(progress.isShowing())
        	progress.dismiss();
        progress = null;
        ntpTask.cancel(true);
        ntpTask = null;
        timer.cancel();
        timer = null;
    }



	private void initView(){
		countdownTimer = (TextView) findViewById(R.id.countdown);
		customClock = (CustomDigitalClock) findViewById(R.id.digital_clock);
		Button syncBtn = (Button) findViewById(R.id.syncBtn);
		syncBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(!progress.isShowing())
					progress = ProgressDialog.show(MainActivity.this, "Synchronizing","Please wait...", true);
				ntpTask.cancel(true);
				timer.cancel();
				ntpTask = new NTPOperation().execute("");				
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private class NTPOperation extends AsyncTask<String, Void, Long> {

		@Override
		protected Long doInBackground(String... params) {
			SntpClient client = new SntpClient();
			long time = 0;
			if(isCancelled())
				return time;
			if (client.requestTime(NTP_SERVER_URL, REQUEST_TIMEOUT)) {
				time = client.getNtpTime();				
			}			
			
			return time;
		}

		@Override
		protected void onPostExecute(Long result) {	
			if(isCancelled()){ return;}
			customClock.setCurrentTime(result);
			timer.start();
			progress.dismiss();
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}
	}

}
