package com.samsung.sample.jspeex;

import java.io.IOException;

import android.media.MediaRecorder;

public class SoundMeter
{
  private static final double EMA_FILTER = 0.6D;
  private double mEMA = 0.0D;
  private MediaRecorder mRecorder = null;
  
  public double getAmplitude()
  {
    if (this.mRecorder != null) {
      return this.mRecorder.getMaxAmplitude() / 2700.0D;
    }
    return 0.0D;
  }
  
  public double getAmplitudeEMA()
  {
    this.mEMA = (0.6D * getAmplitude() + 0.4D * this.mEMA);
    return this.mEMA;
  }
  
  public void start()
  {
    if (this.mRecorder == null)
    {
      this.mRecorder = new MediaRecorder();
      this.mRecorder.setAudioSource(1);
      this.mRecorder.setOutputFormat(1);
      this.mRecorder.setAudioEncoder(1);
      this.mRecorder.setOutputFile("/dev/null");
      //mRecorder.setaudio
      try {
		this.mRecorder.prepare();
	} catch (IllegalStateException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
      this.mRecorder.start();
      this.mEMA = 0.0D;
    }
  }
  
  public void stop()
  {
    if (this.mRecorder != null)
    {
      this.mRecorder.stop();
      this.mRecorder.release();
      this.mRecorder = null;
    }
  }
}