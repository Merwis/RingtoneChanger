package cz.uhk.fim.ringtonechanger;

import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.math.BigDecimal;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Petr on 14. 1. 2016.
 */
public class NoiseCheckingService extends Service {

    protected static final String TAG = "noise-checking-service";
    private static final int shortTimePeriod = 5000;
    private static final int longTimePeriod = 15000;
    Timer t = new Timer();
    private boolean loudMode = false;
    private int timePeriod;
    private int oldRingtoneVolume;
    private int calibratedVolume;

    AudioManager audioManager = null;

    private static final int FREQUENCY = 44100;
    private static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private volatile int BUFFSIZE = 0;
    private static final double P0 = 0.000002;

    private static final int CALIB_DEFAULT = -120;
    private int mCaliberationValue = CALIB_DEFAULT;

    AudioRecord mRecordInstance = null;

    private SharedPreferences mSharedPreferences;


    private int counter;


    public NoiseCheckingService() {
        super();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        counter = 0;
        timePeriod = longTimePeriod;
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        mSharedPreferences = getSharedPreferences(Strings.SHARED_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(Strings.NOISE_CHECKING_RUNNING_ADDED_KEY, true);
        editor.commit();
        calibratedVolume = mSharedPreferences.getInt(Strings.CALIBRATION_ADDED_KEY, 38);

        t.scheduleAtFixedRate(new TimerTask() {
                                  @Override
                                  public void run() {
                                      counter += 1; // pocitani probehlich mereni za ucelem sledovani statistik
                                      //if (!silentMode() && locked()) { // vypnuta kontrola zamknute obrazovky pro ucely testovani
                                      if (!silentMode()) {
                                          Log.d(TAG, timePeriod + " perioda");
                                          Log.d(TAG, "merime");
                                          BUFFSIZE = AudioRecord.getMinBufferSize(
                                                  FREQUENCY, CHANNEL, ENCODING) * 10;

                                          mRecordInstance = new AudioRecord(
                                                  MediaRecorder.AudioSource.MIC,
                                                  FREQUENCY, CHANNEL, ENCODING, BUFFSIZE
                                          );

                                          measure();
                                      } else { // pokud je telefon odemcen nebo vzpnuto vyzvaneni
                                          Log.d(TAG, "nemerime");
                                          // debug notification
                                          sendNotification("c " + counter + " ticho " + silentMode() + " locked " + locked());
                                          if (loudMode) {
                                              setDefaultRingtoneVolume();
                                              timePeriod = longTimePeriod;
                                              loudMode = false;
                                              Log.d(TAG, "vynuceny navrat k defaultu");
                                          }
                                      }
                                  }
                              },
                2000,
                timePeriod);

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(Strings.NOISE_CHECKING_RUNNING_ADDED_KEY, false);
        editor.commit();
        t.cancel();
        stopRecordInstance();



    }

    public void measure() {
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

            int breakingPoint = (int) ((double) calibratedVolume * 1.17);

            //sendNotification(splValue + "normal" + calibratedVolume + " " + breakingPoint);


            if (splValue > breakingPoint && !loudMode) {
                setMaxRingtoneVolume();
                timePeriod = shortTimePeriod;
                sendNotification(splValue + getApplicationContext().getString(R.string.volumeUp) + breakingPoint);
                loudMode = true;
                Log.d(TAG, "zesilujeme " + splValue );
            } else if (splValue <=breakingPoint && loudMode){
                setDefaultRingtoneVolume();
                timePeriod = longTimePeriod;
                sendNotification(splValue + getApplicationContext().getString(R.string.volumeDown));
                loudMode = false;
                Log.d(TAG, "zeslabujeme" + splValue);
            }

            //writeLog(splValue);


        } catch (Exception e) {
            e.printStackTrace();
        }
        stopRecordInstance();
    }


    public double round(double d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Double.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();
    }

    private boolean silentMode() {
        if (audioManager.getStreamVolume(AudioManager.STREAM_RING) == 0) {
            return true;
        } else {
            return false;
        }
    }

    private boolean locked() {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (keyguardManager.inKeyguardRestrictedInputMode()) {
            return true;
        } else {
            return false;
        }
    }

    private int getOldRingtoneVolume() {
        int vol = audioManager.getStreamVolume(AudioManager.STREAM_RING);
        return vol;
    }

    private void setMaxRingtoneVolume() {
        oldRingtoneVolume = getOldRingtoneVolume();
        audioManager.setStreamVolume(AudioManager.STREAM_RING, audioManager.getStreamMaxVolume(AudioManager.STREAM_RING),0);
    }

    private void setDefaultRingtoneVolume() {
        audioManager.setStreamVolume(AudioManager.STREAM_RING, oldRingtoneVolume,0);
    }

    private void stopRecordInstance() {
        if(mRecordInstance != null){
            mRecordInstance.stop();
            mRecordInstance.release();
            mRecordInstance = null;
        }
    }


    private void sendNotification(String splValue) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_launcher))
                .setColor(Color.RED)
                .setContentTitle(splValue)
                .setContentText("");

        builder.setAutoCancel(true);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(0, builder.build());
    }

}
