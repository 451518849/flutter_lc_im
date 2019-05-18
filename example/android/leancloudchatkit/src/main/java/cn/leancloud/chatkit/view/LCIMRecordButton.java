package cn.leancloud.chatkit.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Color;
//import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import cn.leancloud.chatkit.R;
import cn.leancloud.chatkit.utils.LCIMAudioHelper;
import cn.leancloud.chatkit.utils.LCIMLogUtils;
import cn.leancloud.chatkit.utils.LCIMPathUtils;
import com.avos.avoscloud.im.v2.audio.*;

/**
 * 录音的按钮
 */
public class LCIMRecordButton extends Button {
  public static final int BACK_RECORDING = R.drawable.lcim_chat_voice_bg_pressed;
  public static final int BACK_IDLE = R.drawable.lcim_chat_voice_bg;
  public static final int SLIDE_UP_TO_CANCEL = 0;
  public static final int RELEASE_TO_CANCEL = 1;
  private static final int MIN_INTERVAL_TIME = 1000;
  private static int[] recordImageIds = {R.drawable.lcim_record_icon_voice0,
    R.drawable.lcim_record_icon_voice1, R.drawable.lcim_record_icon_voice2,
    R.drawable.lcim_record_icon_voice3, R.drawable.lcim_record_icon_voice4,
    R.drawable.lcim_record_icon_voice5};
  private TextView textView;
  private String outputPath = null;
  private RecordEventListener recordEventListener;
  private long startTime;
  private Dialog recordIndicator;
  private View view;
  private AVIMAudioRecorder audioRecorder;
  private ObtainDecibelThread thread;
  private Handler volumeHandler;
  private ImageView imageView;
  private int status;
  private OnDismissListener onDismiss = new OnDismissListener() {

    @Override
    public void onDismiss(DialogInterface dialog) {
      stopRecording();
    }
  };

  public LCIMRecordButton(Context context) {
    super(context);
    init();
  }

  public LCIMRecordButton(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init();
  }

  public LCIMRecordButton(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public void setSavePath(String path) {
    outputPath = path;
  }

  public void setRecordEventListener(RecordEventListener listener) {
    recordEventListener = listener;
  }

  private void init() {
    volumeHandler = new ShowVolumeHandler();
    setBackgroundResource(BACK_IDLE);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (outputPath == null)
      return false;
    int action = event.getAction();
    switch (action) {
      case MotionEvent.ACTION_DOWN:
        startRecord();
        break;
      case MotionEvent.ACTION_UP:
        if (status == RELEASE_TO_CANCEL) {
          cancelRecord();
        } else {
          finishRecord();
        }
        break;
      case MotionEvent.ACTION_MOVE:
        if (event.getY() < 0) {
          status = RELEASE_TO_CANCEL;
        } else {
          status = SLIDE_UP_TO_CANCEL;
        }
        setTextViewByStatus();
        break;
      case MotionEvent.ACTION_CANCEL:
        cancelRecord();
        break;
      default:
        break;
    }
    return true;
  }

  public int getColor(int id) {
    return getContext().getResources().getColor(id);
  }

  private void setTextViewByStatus() {
    if (status == RELEASE_TO_CANCEL) {
      textView.setTextColor(getColor(R.color.lcim_commom_read));
      textView.setText(R.string.lcim_chat_record_button_releaseToCancel);
    } else if (status == SLIDE_UP_TO_CANCEL) {
      textView.setTextColor(Color.WHITE);
      textView.setText(R.string.lcim_chat_record_button_slideUpToCancel);
    }
  }

  private void startRecord() {
    LCIMAudioHelper.getInstance().stopPlayer();
    initRecordDialog();
    startTime = System.currentTimeMillis();
    setBackgroundResource(BACK_RECORDING);
    startRecording();
    recordIndicator.show();
  }

  private void initRecordDialog() {
    if (null == recordIndicator) {
      recordIndicator = new Dialog(getContext(), R.style.lcim_record_dialog_style);
      view = inflate(getContext(), R.layout.lcim_chat_record_layout, null);
      imageView = (ImageView) view.findViewById(R.id.imageView);
      textView = (TextView) view.findViewById(R.id.textView);
      recordIndicator.setContentView(view, new LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT));
      recordIndicator.setOnDismissListener(onDismiss);

      LayoutParams lp = recordIndicator.getWindow().getAttributes();
      lp.gravity = Gravity.CENTER;
    }
  }

  private void removeFile() {
    File file = new File(outputPath);
    if (file.exists()) {
      file.delete();
    }
  }

  private void finishRecord() {
    stopRecording();
    recordIndicator.dismiss();
    setBackgroundResource(BACK_IDLE);
  }

  private void cancelRecord() {
    stopRecording();
    setBackgroundResource(BACK_IDLE);
    recordIndicator.dismiss();
    Toast.makeText(getContext(), getContext().getString(R.string.lcim_chat_cancelRecord),
      Toast.LENGTH_SHORT).show();
    removeFile();
  }

  private void startRecording() {
    outputPath = LCIMPathUtils.getRecordPathByCurrentTime(getContext());
    try {
      if (null == audioRecorder) {
        final String localFilePath = outputPath;
        audioRecorder = new AVIMAudioRecorder(localFilePath, new AVIMAudioRecorder.RecordEventListener(){
        @Override
        public void onFinishedRecord(long milliSeconds, String reason){
          if (status == RELEASE_TO_CANCEL) {
            removeFile();
          } else if (null != recordEventListener) {
            if (milliSeconds < MIN_INTERVAL_TIME) {
              Toast.makeText(getContext(), getContext().getString(R.string.lcim_chat_record_button_pleaseSayMore), Toast.LENGTH_SHORT).show();
              removeFile();
            } else {
              recordEventListener.onFinishedRecord(localFilePath, Math.round(milliSeconds/1000));
            }
          }
        }
        @Override
        public void onStartRecord() {
          if (null != recordEventListener) {
            recordEventListener.onStartRecord();
          }
        }
        });
      }
      audioRecorder.start();
      thread = new ObtainDecibelThread();
      thread.start();
      recordEventListener.onStartRecord();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private void stopRecording() {
    if (thread != null) {
      thread.exit();
      thread = null;
    }
    if (audioRecorder != null) {
      audioRecorder.stop();
      audioRecorder = null;
    }
  }

  public interface RecordEventListener {
    public void onFinishedRecord(String audioPath, int secs);

    void onStartRecord();
  }

  private class ObtainDecibelThread extends Thread {
    private volatile boolean running = true;

    public void exit() {
      running = false;
    }

    @Override
    public void run() {
      while (running) {
        try {
          Thread.sleep(200);
        } catch (InterruptedException e) {
          LCIMLogUtils.logException(e);
        }
        if (audioRecorder == null || !running) {
          break;
        }
        int x = audioRecorder.getMaxAmplitude();
        if (x != 0) {
          int f = (int) (10 * Math.log(x) / Math.log(10));
          int index = (f - 18) / 5;
          if (index < 0) index = 0;
          if (index > 5) index = 5;
          volumeHandler.sendEmptyMessage(index);
        }
      }
    }

  }

  class ShowVolumeHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
      imageView.setImageResource(recordImageIds[msg.what]);
    }
  }
}
