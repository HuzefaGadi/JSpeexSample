package com.samsung.sample.jspeex;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.songu.recordvoice.doc.Globals;
import com.songu.recordvoice.model.VoiceModel;
import com.songu.recordvoice.service.ServiceManager;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class JSpeexSampleActivity extends Activity implements LocationListener {

    private static final String TAG = JSpeexSampleActivity.class.getSimpleName();
    private static final int RECORDER_BPP = 16;
    private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
    private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";

    FileOutputStream os = null;

    int bufferSize ;
    int frequency = 44100; //8000;
    int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    boolean started = false;
    RecordAudio recordTask;

    short threshold=5000;

    boolean debug=false;

    private volatile MediaRecorder mRecorder;
    private final String startRecordingLabel = "Start recording";
    private final String resumeRecordingLabel = "Resumed recording";
    private final String pauseRecordingLabel = "Paused recording";
    private final String stopRecordingLabel = "Stop recording";
    private volatile boolean mIsRecording = false;
    private volatile File mOutputFile;
    public ProgressDialog mDlg;
    private ProgressBar mProgressBar;

    SharedPreferences sp;
    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    // flag for GPS status
    boolean canGetLocation = false;
    int mCounter = 0;

    Location location; // location
    double latitude; // latitude
    double longitude; // longitude

    protected LocationManager locationManager;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute
    private Button btnLogout;
    private SeekBar seekBar;

    //private SoundMeter mSensor;
    Button button;
    int mHitCount = 0;
    int limitDecibelValue = 0;
    volatile boolean isStopPressed;
    private Handler mHandler = new Handler();

    List<File> mFileList = new ArrayList<>();
    volatile int pauseCounter = 0;
    private Runnable mPollTask = new Runnable() {
        public void run() {
            //double d = mSensor.getAmplitude();
            int decibelValue;
            decibelValue = getDecibelValue();
            MediaPlayer loMediaPlayer = MediaPlayer.create(JSpeexSampleActivity.this, Uri.fromFile
                    (mOutputFile));

            if (loMediaPlayer != null) {
                Log.e("loMediaPlayer", "" + loMediaPlayer.getDuration());
                Log.e("mOutputFile", "" + mOutputFile.length());
                int duration = loMediaPlayer.getDuration();
                if (duration >= TimeUnit.SECONDS.toMillis(55)) {
                    if (mIsRecording) {
                        stopRecording();
                        mOutputFile = getFile("amr");
                        resumeRecording(mOutputFile);
                        startBufferedWrite(mOutputFile);
                    }
                }
                loMediaPlayer.release();
            }

            mProgressBar.setProgress(decibelValue);
            mHandler.postDelayed(mPollTask, 1000L);
        }

    };


    public synchronized void resumeRecording(File file) {
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setOutputFile(file.getAbsolutePath());
        try {
            mRecorder.prepare();
            mRecorder.start();
        } catch (IllegalStateException e) {
            System.out.println();
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile(file.getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println(e);
        }
//        Toast.makeText(this, "resuming....", Toast.LENGTH_SHORT).show();
        button.setText(resumeRecordingLabel);
    }

    public synchronized void pauseRecording() {
        try {
            mRecorder.stop();
        } catch (IllegalStateException e) {
            Log.e("pauseRecording", "" + e);
        }
        Toast.makeText(this, "pausing....", Toast.LENGTH_SHORT).show();
        button.setText(pauseRecordingLabel);
    }

    TextView tvCounter;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        tvCounter = (TextView) findViewById(R.id.tvCounter);
        sp = this.getSharedPreferences("login", MODE_PRIVATE);
        limitDecibelValue = sp.getInt("limit", 0);
        mDlg = new ProgressDialog(this);
        //mSensor = new SoundMeter();
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        seekBar = (SeekBar) findViewById(R.id.seekBar1);
        seekBar.setMax(140);
        seekBar.setProgress(limitDecibelValue);

        mRecorder = new MediaRecorder();
//        mRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
//            @Override
//            public void onError(MediaRecorder mr, int what, int extra) {
//                switch (what) {
//                    case MediaRecorder.MEDIA_ERROR_SERVER_DIED:
//                        mRecorder.release();
//                        mRecorder = null;
//                        mRecorder = new MediaRecorder();
//                        break;
//                }
//            }
//        });

//		initEncoder();


        findViewById(R.id.btnStop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsRecording) {
                    mIsRecording = false;
//                doneWithThisRecording(false);
                   // stopRecording();

                }
                stopAquisition();
            }
        });

        button = (Button) findViewById(R.id.button);
        btnLogout = (Button) findViewById(R.id.btnLogout);
        button.setText(startRecordingLabel);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                startAquisition();
              /*  if (!mIsRecording) {
                    button.setText(stopRecordingLabel);
                    mIsRecording = true;
//                    mDecibelSensor.start();
                    mOutputFile = getFile("amr");
                    resumeRecording(mOutputFile);
                    startBufferedWrite(mOutputFile);
                    //mSensor.start();
                    mHandler.postDelayed(mPollTask, 1000L);


                }*/

//                else {
//                    mIsRecording = false;
//                    stopRecording();
////                    mDecibelSensor.stop();
//                }

            }
        });
        final SharedPreferences sp = this.getSharedPreferences("login", MODE_PRIVATE);
        btnLogout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                sp.edit().putBoolean("login1", false).apply();
                String uid = sp.getString("uid", "");
                JSpeexSampleActivity.this.finish();
                Intent m = new Intent(JSpeexSampleActivity.this, LoginActivity.class);
                JSpeexSampleActivity.this.startActivity(m);
                ServiceManager.onLogoutUser(uid);
            }
        });
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                // TODO Auto-generated method stub
                sp.edit().putInt("limit", arg1).apply();
                limitDecibelValue = arg1;
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
                // TODO Auto-generated method stub

            }

        });

    }


    public synchronized void stopRecording() {
        this.mHitCount = 0;
//        Toast.makeText(this, "Stopping....", Toast.LENGTH_SHORT).show();
        mDlg.setTitle("Uploading " + ++mCounter + " audio");
        mDlg.setMessage("Please wait....");

        mDlg.show();
        button.setText(startRecordingLabel);
        try {
            mRecorder.stop();
        } catch (IllegalStateException e) {
            Log.e("stopRecording", "" + e);
        }
        VoiceModel model = new VoiceModel();
        model.mPath = mOutputFile.getPath();
        Location m = getLocation();
        if (m != null) {
            model.mLat = String.valueOf(m.getLatitude());
            model.mLong = String.valueOf(m.getLongitude());
        }
        Time time = new Time();
        time.setToNow();
        model.mFileName = time.format("%Y%m%d%H%M%S");
        // this part so it would not log you out
//        sp.edit().putBoolean("login1", false).apply();
        String uid = sp.getString("uid", "");
        ServiceManager.onVoiceUpload(model, uid, JSpeexSampleActivity.this);
        MediaPlayer mp = MediaPlayer.create(this, Uri.fromFile(mOutputFile));
        if (mp != null)
            Log.e("LOG_TAG", "Uploaded successfully! Length of outputFile: " + mOutputFile
                    .length() + "Duration " + MediaPlayer.create(this, Uri.fromFile(mOutputFile))
                    .getDuration());

    }

//    synchronized void mergeAudio() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    File tempFile = new File(mTempFile.getAbsolutePath());
//                    // Second parameter to indicate appending of data
//                    FileOutputStream fos = new FileOutputStream(mOutputFile, true);
//                    FileInputStream fis = new FileInputStream(tempFile);
//                    Log.d("LOG_TAG", "Length of outputFile: " + mOutputFile
//                            .length() + " and Length of inputFile: " + tempFile.length());
//
//                    byte fileContent[] = new byte[(int) tempFile.length()];
//                    fis.read(fileContent);// Reads the file content as byte from the list.
//
//                    /* copy the entire file, but not the first 6 bytes */
//                    byte[] headerlessFileContent = new byte[fileContent.length - 6];
//                    for (int j = 6; j < fileContent.length; j++) {
//                        headerlessFileContent[j - 6] = fileContent[j];
//                    }
//                    fileContent = headerlessFileContent;
//
//                /* Write the byte into the combine file. */
//                    fos.write(fileContent);
//
//                /* Delete the new recording as it is no longer required (Save memory!!!) :-) */
//                    boolean deleted = tempFile.delete();
//
//                    Log.e("LOG_TAG", "New recording deleted after merging: " + deleted);
//                    Log.e("LOG_TAG", "Successfully merged the two Voice Message Recordings");
//                    Log.e("LOG_TAG", "Length of outputFile after merging: " + mOutputFile.length());
//
//
//                } catch (Exception ex) {
//                    Log.e("LOG_TAG", "Error while merging audio file: " + ex.getMessage());
//                }
//
//            }
//        }).start();
//
//    }

//    synchronized void doneWithThisRecording(final boolean restart) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    File tempFile = new File(mTempFile.getAbsolutePath());
//                    // Second parameter to indicate appending of data
//                    FileOutputStream fos = new FileOutputStream(mOutputFile, true);
//                    FileInputStream fis = new FileInputStream(tempFile);
//                    Log.d("LOG_TAG", "Length of outputFile: " + mOutputFile
//                            .length() + " and Length of inputFile: " + tempFile.length());
//
//                    byte fileContent[] = new byte[(int) tempFile.length()];
//                    fis.read(fileContent);// Reads the file content as byte from the list.
//
//                    /* copy the entire file, but not the first 6 bytes */
//                    byte[] headerlessFileContent = new byte[fileContent.length - 6];
//                    for (int j = 6; j < fileContent.length; j++) {
//                        headerlessFileContent[j - 6] = fileContent[j];
//                    }
//                    fileContent = headerlessFileContent;
//
//                /* Write the byte into the combine file. */
//                    fos.write(fileContent);
//
//                /* Delete the new recording as it is no longer required (Save memory!!!) :-) */
//                    boolean deleted = tempFile.delete();
//
//                    Log.e("LOG_TAG", "New recording deleted after merging: " + deleted);
//                    Log.e("LOG_TAG", "Successfully merged the two Voice Message Recordings");
//                    Log.e("LOG_TAG", "Length of outputFile after merging: " + mOutputFile.length());
//
//                } catch (Exception ex) {
//                    Log.e("LOG_TAG", "Error while merging audio file: " + ex.getMessage());
//
//                } finally {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            stopRecording();
//                            mOutputFile = getFile("amr");
//                            if (restart) {
//                                resumeRecording(mOutputFile);
//                            }
//                            mIsRecording = restart;
//                        }
//                    });
//
//                }
//            }
//        }).start();
//
//
//    }


    void mergeAudiowithlist() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File mainVoiceRecordingFile;
                List<File> loUploadFileList = new ArrayList<>(mFileList);
                try {
                    if (loUploadFileList.size() == 1) {
                        mainVoiceRecordingFile = loUploadFileList.get(0);
                    } else if (loUploadFileList.size() > 1) {

                        mainVoiceRecordingFile = loUploadFileList.get(0);
                        for (int i = 1; i < loUploadFileList.size(); i++) {
                            File newVoiceRecordingFile = loUploadFileList.get(i);
                            // Second parameter to indicate appending of data
                            FileOutputStream fos = new FileOutputStream(mainVoiceRecordingFile, true);
                            FileInputStream fis = new FileInputStream(newVoiceRecordingFile);
                            Log.d("LOG_TAG", "Length of outputFile: " + mainVoiceRecordingFile
                                    .length() + " and Length of inputFile: " + newVoiceRecordingFile.length());

                            byte fileContent[] = new byte[(int) newVoiceRecordingFile.length()];
                            fis.read(fileContent);// Reads the file content as byte from the list.

     /* copy the entire file, but not the first 6 bytes */
                            byte[] headerlessFileContent = new byte[fileContent.length - 6];
                            for (int j = 6; j < fileContent.length; j++) {
                                headerlessFileContent[j - 6] = fileContent[j];
                            }
                            fileContent = headerlessFileContent;

                /* Write the byte into the combine file. */
                            fos.write(fileContent);

                /* Delete the new recording as it is no longer required (Save memory!!!) :-) */
                            boolean deleted = newVoiceRecordingFile.delete();

                            Log.d("LOG_TAG", "New recording deleted after merging: " + deleted);
                            Log.d("LOG_TAG", "Successfully merged the two Voice Message Recordings");
                            Log.d("LOG_TAG", "Length of outputFile after merging: " + mainVoiceRecordingFile.length());
                        }
                    }

                } catch (Exception ex) {
                    Log.e("LOG_TAG", "Error while merging audio file: " + ex.getMessage());
                }

            }
        }).start();

    }


    public Location getLocation() {
        try {
            locationManager = (LocationManager) this
                    .getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, JSpeexSampleActivity.this);
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, JSpeexSampleActivity.this);
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    @Override
    public void onDestroy() {
        //mMedia.release();
        mRecorder.release();
        mRecorder = null;
//        MusicUtils.unbindFromService(mToken);
        super.onDestroy();
    }

    public void playAnswer(String path) {

		/*try {
            long [] list = new long[1];
			String answerPath = Globals.m_baseUrl + "output/" + path;
			//list[0] = MusicUtils.insert(MainFragment.this.getActivity(), songPath);
			list[0] = MusicUtils.insert(this, answerPath);
			
			mService.open(list, 0);
		} catch (RemoteException e) {
			e.printStackTrace();
		}*/
        tvCounter.setText("Uploaded " + mCounter + " times");

        MediaPlayer mp = new MediaPlayer();
        try {
            mp.setDataSource(Globals.m_baseUrl + "output/" + path);
            mp.prepare();
            mp.start();
        } catch (IOException e) {
            //Log.e(LOG_TAG, "prepare() failed");
        }


    }

    private synchronized void startBufferedWrite(final File file) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (mIsRecording) {
                        mProgressBar.setProgress(getDecibelValue());
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } finally {
                    mProgressBar.setProgress(0);

                }
            }
        }).start();
    }


    private File getFile(final String suffix) {
        Time time = new Time();
        time.setToNow();
        return new File(Environment.getExternalStorageDirectory(), time.format("%Y%m%d%H%M%S") + "." + suffix);
    }

    @Override
    public void onLocationChanged(Location arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderDisabled(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
        // TODO Auto-generated method stub

    }


    static final private double REFERENCE = 0.6;

    public double getAmplitude() {
        if (mRecorder != null) {
            return (mRecorder.getMaxAmplitude());

        } else
            return 0;

    }

    int getDecibelValue() {

        double amplitude = getAmplitude();
        int decibelValue = (int) (20 * Math.log10(amplitude / REFERENCE));
//        Log.e("amplitud", amplitude + "");
//        Log.e("decibelValue", decibelValue + "");

        return decibelValue;
    }

    public void stopAquisition() {
        Log.w(TAG, "stopAquisition");
        if (started) {
            started = false;
            recordTask.cancel(true);
        }
    }
    public void startAquisition(){
       // Log.w(TAG, "startAquisition");
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {

                //elapsedTime=0;
                started = true;
                recordTask = new RecordAudio();
                recordTask.execute();
                //startButton.setText("RESET");
            }
        }, 500);
    }

    public class RecordAudio extends AsyncTask<Void, Double, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            Log.w(TAG, "doInBackground");
            try {

                String filename = getTempFilename();

                try {
                    os = new FileOutputStream(filename);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }


                bufferSize = AudioRecord.getMinBufferSize(frequency,
                        channelConfiguration, audioEncoding);

                AudioRecord audioRecord = new AudioRecord( MediaRecorder.AudioSource.MIC, frequency,
                        channelConfiguration, audioEncoding, bufferSize);

                short[] buffer = new short[bufferSize];

                audioRecord.startRecording();

                while (started) {
                    int bufferReadResult = audioRecord.read(buffer, 0,bufferSize);
                    if(AudioRecord.ERROR_INVALID_OPERATION != bufferReadResult){
                        //check signal
                        //put a threshold
                        int foundPeak=searchThreshold(buffer,threshold);
                        if (foundPeak>-1){ //found signal
                            //record signal
                            byte[] byteBuffer =ShortToByte(buffer,bufferReadResult);
                            try {
                                os.write(byteBuffer);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }else{//count the time
                            //don't save signal
                        }


                        //show results
                        //here, with publichProgress function, if you calculate the total saved samples,
                        //you can optionally show the recorded file length in seconds:      publishProgress(elsapsedTime,0);


                    }
                }

                audioRecord.stop();


                //close file
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                copyWaveFile(getTempFilename(),getFilename());
                deleteTempFile();


            } catch (Throwable t) {
                t.printStackTrace();
                Log.e("AudioRecord", "Recording Failed");
            }
            return null;

        } //fine di doInBackground

        byte [] ShortToByte(short [] input, int elements) {
            int short_index, byte_index;
            int iterations = elements; //input.length;
            byte [] buffer = new byte[iterations * 2];

            short_index = byte_index = 0;

            for(/*NOP*/; short_index != iterations; /*NOP*/)
            {
                buffer[byte_index]     = (byte) (input[short_index] & 0x00FF);
                buffer[byte_index + 1] = (byte) ((input[short_index] & 0xFF00) >> 8);

                ++short_index; byte_index += 2;
            }

            return buffer;
        }


        int searchThreshold(short[]arr,short thr){
            int peakIndex;
            int arrLen=arr.length;
            for (peakIndex=0;peakIndex<arrLen;peakIndex++){
                if ((arr[peakIndex]>=thr) || (arr[peakIndex]<=-thr)){
                    //se supera la soglia, esci e ritorna peakindex-mezzo kernel.

                    return peakIndex;
                }
            }
            return -1; //not found
        }

    /*
    @Override
    protected void onProgressUpdate(Double... values) {
        DecimalFormat sf = new DecimalFormat("000.0000");
        elapsedTimeTxt.setText(sf.format(values[0]));

    }
    */

        private String getFilename(){
            String filepath = Environment.getExternalStorageDirectory().getPath();
            File file = new File(filepath,AUDIO_RECORDER_FOLDER);

            if(!file.exists()){
                file.mkdir();
            }

            return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + AUDIO_RECORDER_FILE_EXT_WAV);
        }


        private String getTempFilename(){
            File mainFile = Environment.getExternalStorageDirectory();
            File file = new File(mainFile,AUDIO_RECORDER_FOLDER);

            if(!file.exists()){
                boolean directoryCreated = file.mkdir();

            }

            File tempFile = new File(file,AUDIO_RECORDER_TEMP_FILE);

            // if(tempFile.exists())
            // tempFile.delete();

            return (tempFile.getAbsolutePath());
        }





        private void deleteTempFile() {
            File file = new File(getTempFilename());

            file.delete();
        }

        private void copyWaveFile(String inFilename,String outFilename){
            FileInputStream in = null;
            FileOutputStream out = null;
            long totalAudioLen = 0;
            long totalDataLen = totalAudioLen + 36;
            long longSampleRate = frequency;
            int channels = 1;
            long byteRate = RECORDER_BPP * frequency * channels/8;

            byte[] data = new byte[bufferSize];

            try {
                in = new FileInputStream(inFilename);
                out = new FileOutputStream(outFilename);
                totalAudioLen = in.getChannel().size();
                totalDataLen = totalAudioLen + 36;


                WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                        longSampleRate, channels, byteRate);

                while(in.read(data) != -1){
                    out.write(data);
                }

                in.close();
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void WriteWaveFileHeader(
                FileOutputStream out, long totalAudioLen,
                long totalDataLen, long longSampleRate, int channels,
                long byteRate) throws IOException {

            byte[] header = new byte[44];

            header[0] = 'R';  // RIFF/WAVE header
            header[1] = 'I';
            header[2] = 'F';
            header[3] = 'F';
            header[4] = (byte) (totalDataLen & 0xff);
            header[5] = (byte) ((totalDataLen >> 8) & 0xff);
            header[6] = (byte) ((totalDataLen >> 16) & 0xff);
            header[7] = (byte) ((totalDataLen >> 24) & 0xff);
            header[8] = 'W';
            header[9] = 'A';
            header[10] = 'V';
            header[11] = 'E';
            header[12] = 'f';  // 'fmt ' chunk
            header[13] = 'm';
            header[14] = 't';
            header[15] = ' ';
            header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
            header[17] = 0;
            header[18] = 0;
            header[19] = 0;
            header[20] = 1;  // format = 1
            header[21] = 0;
            header[22] = (byte) channels;
            header[23] = 0;
            header[24] = (byte) (longSampleRate & 0xff);
            header[25] = (byte) ((longSampleRate >> 8) & 0xff);
            header[26] = (byte) ((longSampleRate >> 16) & 0xff);
            header[27] = (byte) ((longSampleRate >> 24) & 0xff);
            header[28] = (byte) (byteRate & 0xff);
            header[29] = (byte) ((byteRate >> 8) & 0xff);
            header[30] = (byte) ((byteRate >> 16) & 0xff);
            header[31] = (byte) ((byteRate >> 24) & 0xff);
            header[32] = (byte) (channels * 16 / 8);  // block align
            header[33] = 0;
            header[34] = RECORDER_BPP;  // bits per sample
            header[35] = 0;
            header[36] = 'd';
            header[37] = 'a';
            header[38] = 't';
            header[39] = 'a';
            header[40] = (byte) (totalAudioLen & 0xff);
            header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
            header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
            header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

            out.write(header, 0, 44);
        }

    }

}