package com.video;


import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.VideoView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class PlayVideo extends Activity implements OnPreparedListener{

	private String TAG = getClass().getSimpleName();
	private Button choose;
	private ToggleButton playtogglebutton;
	private TextView timeEnd, timeElapsed;
	private ProgressBar progressBar;
	private VideoView videoviewer;
	private CountDownTimer timer;
	private Bundle extra;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.play_video);
		
		extra = getIntent().getExtras();
		System.out.println(extra.size()+""+extra.getString("path"));
		
		
		choose = (Button) findViewById(R.id.choose);
		playtogglebutton = (ToggleButton) findViewById(R.id.playtogglebutton);
		timeElapsed = (TextView) findViewById(R.id.timeElapsed);
		timeEnd = (TextView) findViewById(R.id.timeEnd);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		
		playtogglebutton.setOnCheckedChangeListener(checkedchangelistener);
		
	
		
		/*write a code for upload file to server ......start uploading to server*/
		choose.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Toast.makeText(PlayVideo.this, "Start uplaoding........... ", Toast.LENGTH_SHORT).show();
			}
		});
		
		videoviewer = (VideoView) findViewById(R.id.videoviewer);
		videoviewer.setVideoURI(Uri.parse(extra.getString("path")));
		videoviewer.requestFocus();
		videoviewer.setOnPreparedListener(PlayVideo.this);
	}

	private OnCheckedChangeListener checkedchangelistener =  new OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
			// TODO Auto-generated method stub
			/*pause player*/
			if(isChecked){
				System.out.println("player pause");
				playMedia(false);
			}
			/*play player*/
			else{
				System.out.println("player play");
				playMedia(true);
			}
		}};
		
	@Override
	public void onPrepared(MediaPlayer mp) {
		// TODO Auto-generated method stub
		Log.d(TAG, "media player preparing.......");
		mp.setLooping(true);


		// onSeekCompletionListener declaration
		mp.setOnSeekCompleteListener(new OnSeekCompleteListener() {
			// show current frame after changing the playback position
			@Override
			public void onSeekComplete(MediaPlayer mp) {
				if (!mp.isPlaying()) {
					//playMedia(true);
					System.out.println("inside the setOnSeekCompleteListener");
					playMedia(false);
				}
				System.out.println("inside------ the setOnSeekCompleteListener");
				timeElapsed.setText(countTime(videoviewer.getCurrentPosition()));
			}
		});

		mp.setOnCompletionListener(null);
		// onBufferingUpdateListener declaration
		mp.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {
			// show updated information about the buffering progress
			@Override
			public void onBufferingUpdate(MediaPlayer mp, int percent) {
				Log.d(this.getClass().getName(), "percent: " + percent);
			}
		});
		
		int time = videoviewer.getDuration();
		int time_elapsed = videoviewer.getCurrentPosition();
		progressBar.setProgress(time_elapsed);

		// update current playback time every 500ms until stop
		timer = new CountDownTimer(time, 500) {

			@Override
			public void onTick(long millisUntilFinished) {
				timeElapsed.setText(countTime(videoviewer.getCurrentPosition()));
				float a = videoviewer.getCurrentPosition();
				float b = videoviewer.getDuration();
				progressBar.setProgress((int) (a / b * 100));
			}

			@Override
			public void onFinish() {
				stopMedia();
			}
		};


		timeEnd.setText(countTime(time));
		timeElapsed.setText(countTime(time_elapsed));
		playMedia(true);
	}

	/**
     * Convert time from milliseconds into minutes and seconds, proper to media player
     * 
     * @param miliseconds	media content time in milliseconds
     * @return	time in format minutes:seconds
     */
    public String countTime(int miliseconds) {
    	String timeInMinutes = new String();
    	int minutes = miliseconds / 60000;
    	int seconds = (miliseconds % 60000)/1000;
    	timeInMinutes = minutes + ":" + (seconds<10?"0" + seconds:seconds);
		return timeInMinutes;
    }
    
    /**
     * Start or Pause playback of media content
     * 
     * @param v	View the touch event has been dispatched to
     */
	public void playMedia(boolean isplay) {
		
			if (isplay) {
				videoviewer.start();
				timer.start();
			} else {
				videoviewer.pause();
				timer.cancel();
			}
		
    }
    
    /**
     * Pause and rewind to beginning of the media content
     * 
     * @param v	View the touch event has been dispatched to
     */
	public void stopMedia() {
		if (videoviewer.getCurrentPosition() != 0) {
			
			playtogglebutton.setChecked(true);
			
			videoviewer.pause();
			videoviewer.seekTo(0);
			timer.cancel();

			timeElapsed.setText(countTime(videoviewer.getCurrentPosition()));
			progressBar.setProgress(0);
		}
	}


	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		if(videoviewer != null)
			videoviewer.stopPlayback();
		if (timer != null) {
			timer.cancel();
		}
		super.onStop();
	}
}
