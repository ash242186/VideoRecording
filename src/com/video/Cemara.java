package com.video;




import java.io.File;
import java.io.IOException;
import java.util.Date;




import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioEncoder;
import android.media.MediaRecorder.VideoEncoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore.Video.Media;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class Cemara extends Activity implements SurfaceHolder.Callback{
	
	private Camera prCamera;
	private SurfaceHolder prSurfaceHolder;
	private SurfaceView prSurfaceView;
	private int cMaxRecordDurationInMs = 120000;
	private int rotated_angle =90;
	private long cMaxFileSizeInBytes = 20971520;//20 Megabyte
	private ToggleButton recordtogglebutton;
	private TextView timeElapsed1;
	private boolean prRecordInProcess;
	private MediaRecorder prMediaRecorder;
	private File prRecordedFile;
	private String cVideoFilePath;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cemara);
		
		prRecordInProcess = false;
		cVideoFilePath = android.os.Environment.getExternalStorageDirectory()+"/"+getString(R.string.app_name)+"/";
	
		prSurfaceView = (SurfaceView) findViewById(R.id.surfaceView1);
		timeElapsed1 = (TextView) findViewById(R.id.timeElapsed);
		recordtogglebutton = (ToggleButton) findViewById(R.id.recordtogglebutton);
		
		prSurfaceHolder = prSurfaceView.getHolder();
		prSurfaceHolder.addCallback(this);
		prSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		prMediaRecorder = new MediaRecorder();
		recordtogglebutton.setOnCheckedChangeListener(checkedchangelistener);
	}

	
	private OnCheckedChangeListener checkedchangelistener =  new OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
			// TODO Auto-generated method stub
			/*pause player*/
			if(isChecked){
				System.out.println("start recording....");
				if(startRecording())
				// update current playback time every 500ms until stop
					timer.start();
				else
					Toast.makeText(Cemara.this, "Some error in Camera !!", Toast.LENGTH_SHORT).show();
			}
			/*play player*/
			else{
				System.out.println("stop recording......");
				stopRecording();
				timer.cancel();
				forwardscreen();
			}
		}};


	@Override
	public void surfaceChanged(SurfaceHolder _holder, int _format, int _width, int _height) {
		// TODO Auto-generated method stub
		try {
			
			/*Camera.Parameters lParam = prCamera.getParameters();
			lParam.setSceneMode(Camera.Parameters.SCENE_MODE_PORTRAIT);
			prCamera.setParameters(lParam);*/
			
			//prMediaRecorder.setOrientationHint(90);
			//prMediaRecorder.setVideoSize(240, 320);
			
			prCamera.setDisplayOrientation(rotated_angle);
			prCamera.setPreviewDisplay(_holder);
			prCamera.startPreview();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
		/*open front camera*/
		CameraInfo cameraInfo = new CameraInfo(); 
		int cameraCount = Camera.getNumberOfCameras();
		if(cameraCount >1)
		for ( int camIdx = 0; camIdx < Camera.getNumberOfCameras(); camIdx++ ) { 
	        Camera.getCameraInfo( camIdx, cameraInfo ); 
	        if ( cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT  ) { 
	            try { 
	            	prCamera = Camera.open( camIdx ); 
	            } catch (RuntimeException e) { 
	                Log.i("Camera failed to open: ",e.getLocalizedMessage()); 
	            } 
	        } 
	    }else		
	    	prCamera = Camera.open();
		
		if (prCamera == null) {
			Toast.makeText(this, "Camera is not available!", Toast.LENGTH_SHORT).show();
			finish();
		}
	}


	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		if (prRecordInProcess) {
			stopRecording();
		}/* else {
			prCamera.stopPreview();
		}
		prMediaRecorder.release();
		prMediaRecorder = null;
		prCamera.release();
		prCamera = null;*/
	}
	
	
	private boolean startRecording() {
		prCamera.stopPreview();
		try {
			prCamera.unlock();
			prMediaRecorder.setCamera(prCamera);
			
			prMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
			prMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
			prMediaRecorder.setOrientationHint(rotated_angle);
			
			
			prMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
			prMediaRecorder.setAudioEncoder(AudioEncoder.AMR_NB);
			prMediaRecorder.setVideoEncoder(VideoEncoder.MPEG_4_SP);
			
			prMediaRecorder.setMaxDuration(cMaxRecordDurationInMs);
			prMediaRecorder.setMaxFileSize(cMaxFileSizeInBytes);
			
			prRecordedFile = new File(cVideoFilePath + String.valueOf(System.currentTimeMillis()) +".mp4");
			prMediaRecorder.setOutputFile(prRecordedFile.getPath());
			
			
			prMediaRecorder.setPreviewDisplay(prSurfaceHolder.getSurface());
			
			prMediaRecorder.prepare();
			
			prMediaRecorder.start();
			prRecordInProcess = true;
			return true;
		} catch (IOException _le) {
			_le.printStackTrace();
			return false;
		}
	}

	private void stopRecording() {
		prMediaRecorder.stop();
		prMediaRecorder.reset();
		prMediaRecorder.release();
		timeElapsed1.setText("--:--");
		prRecordInProcess = false;
		prCamera.lock();
		prCamera.stopPreview();
		prCamera.release();
		prCamera =null;
		
		/*insert video information into database*/
		Date taken = new Date();
        ContentValues values = new ContentValues(10);
        values.put(Media.TITLE, prRecordedFile.getName());
        values.put(Media.DISPLAY_NAME, prRecordedFile.getName());
        values.put(Media.DATE_TAKEN, taken.getTime());
        values.put(Media.DATE_ADDED, taken.getTime());
        values.put(Media.DATE_MODIFIED, taken.getTime());
        values.put(Media.MIME_TYPE, "video/mp4");
        
        values.put(Media.BUCKET_ID, prRecordedFile.getParentFile().toString().toLowerCase());
        values.put(Media.BUCKET_DISPLAY_NAME, prRecordedFile.getParentFile().getName().toLowerCase());
        values.put(Media.SIZE, prRecordedFile.length());
        values.put(Media.DATA, prRecordedFile.getAbsolutePath());
        
        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        Uri uri = contentResolver.insert(Media.EXTERNAL_CONTENT_URI, values);
    	Log.i("ContentResolver---cemara", uri.toString());
		
    	
	}
	
	/**
	 * CountDownTimer using for counting elapsed time of media file
	 */
	private CountDownTimer timer = new CountDownTimer(cMaxRecordDurationInMs, 1000) {
		
		@Override
		public void onTick(long millisUntilFinished) {
			// TODO Auto-generated method stub
			timeElapsed1.setText(countTime(millisUntilFinished));
		}
		
		@Override
		public void onFinish() {
			// TODO Auto-generated method stub
			stopRecording();
			forwardscreen();
		}
	};
	/**
     * Convert time from milliseconds into minutes and seconds, proper to media player
     * 
     * @param miliseconds	media content time in milliseconds
     * @return	time in format minutes:seconds
     */
    private String countTime(long miliseconds) {
    	String timeInMinutes = new String();
    	long minutes = miliseconds / 60000;
    	long seconds = (miliseconds % 60000)/1000;
    	timeInMinutes = minutes + ":" + (seconds<10?"0" + seconds:seconds);
		return timeInMinutes;
    }

	
	
	private void forwardscreen(){
		Intent intent = new Intent(Cemara.this, PlayVideo.class);
		intent.putExtra("path", prRecordedFile.getAbsolutePath());
		startActivity(intent);
		finish();
	}
	
	
}
