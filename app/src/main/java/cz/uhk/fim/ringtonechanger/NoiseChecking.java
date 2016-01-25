package cz.uhk.fim.ringtonechanger;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Petr on 14. 1. 2016.
 * Based on https://code.google.com/p/splmeter/
 */
public class NoiseChecking {

    AudioManager audioManager = null;
    AudioRecord mRecordInstance = null;

    Timer t = new Timer();

    private Handler mCalibrationHandler = new Handler();

    private boolean finished;

    Context context;

    private static final int FREQUENCY = 44100;
    private static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private volatile int BUFFSIZE = 0;
    private static final double P0 = 0.000002;

    private static final int CALIB_DEFAULT = -120;
    private int mCaliberationValue = CALIB_DEFAULT;

    double[] calibrationArray = new double[3];
    int calCounter;

    private SharedPreferences mSharedPreferences;



    public NoiseChecking(Context context) {
        this.context = context;

        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        mSharedPreferences = context.getSharedPreferences(Strings.SHARED_PREFERENCES, Context.MODE_PRIVATE);
    }

    public void calibrate() {
        calCounter = 0;
        finished = false;

        t.scheduleAtFixedRate(new TimerTask() {
                                  @Override
                                  public void run() {
                                      BUFFSIZE = AudioRecord.getMinBufferSize(
                                              FREQUENCY, CHANNEL, ENCODING) * 10;

                                      mRecordInstance = new AudioRecord(
                                              MediaRecorder.AudioSource.MIC,
                                              FREQUENCY, CHANNEL, ENCODING, BUFFSIZE
                                      );
                                      if (calCounter < 3) {
                                          go();
                                          calCounter += 1;
                                      } else {
                                          saveCalibration();

                                          mCalibrationHandler.postDelayed(new Runnable() {
                                              @Override
                                              public void run() {
                                                  if (finished == true) {
                                                      Toast.makeText(context, context.getString(R.string.calibrationCompleted), Toast.LENGTH_SHORT).show();
                                                  } else {
                                                      Toast.makeText(context, context.getString(R.string.calibrationFailed), Toast.LENGTH_SHORT).show();
                                                  }
                                              }
                                          }, 100);


                                          t.cancel();
                                          if (mRecordInstance != null) {
                                              mRecordInstance.stop();
                                              mRecordInstance.release();
                                              mRecordInstance = null;
                                          }

                                      }

                                  }
                              },
                500,
                500);
    }

    public void go() {
        try {
            mRecordInstance.startRecording();

            double splValue = 0.0;
            double rmsValue = 0.0;

            int SIZE = BUFFSIZE;
            short[] tempBuffer = new short[SIZE];

            mRecordInstance.read(tempBuffer, 0, SIZE);

            for (int i = 0; i < SIZE - 1; i++) {
                rmsValue += tempBuffer[i] * tempBuffer[i];
            }



            rmsValue = rmsValue / SIZE;
            rmsValue = Math.sqrt(rmsValue);

            splValue = 20 * Math.log10(rmsValue / P0); //0.0001
            splValue = splValue + mCaliberationValue;
            splValue = round(splValue, 2);

            calibrationArray[calCounter] = splValue;


        } catch (Exception e) {
            e.printStackTrace();
        }
        if(mRecordInstance != null){
            mRecordInstance.stop();
            mRecordInstance.release();
            mRecordInstance = null;
        }
    }

    public double round(double d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Double.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();
    }

    private void saveCalibration() {
        int i = 0;
        double sum = 0;
        int calibratedValue;
        while (i<calCounter) {
            sum += calibrationArray[i];
            i++;
        }
        calibratedValue = (int) (sum / calCounter);

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(Strings.CALIBRATION_ADDED_KEY, calibratedValue);
        editor.commit();
        Log.d("Noise check", "Calibration :" + calibratedValue);
        finished = true;
    }

    public boolean getFinished() {
        return finished;
    }
}
