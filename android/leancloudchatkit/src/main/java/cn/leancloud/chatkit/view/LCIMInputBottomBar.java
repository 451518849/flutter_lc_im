package cn.leancloud.chatkit.view;

import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import cn.leancloud.chatkit.R;
import cn.leancloud.chatkit.event.LCIMInputBottomBarEvent;
import cn.leancloud.chatkit.event.LCIMInputBottomBarLocationClickEvent;
import cn.leancloud.chatkit.event.LCIMInputBottomBarRecordEvent;
import cn.leancloud.chatkit.event.LCIMInputBottomBarTextEvent;
import cn.leancloud.chatkit.utils.LCIMPathUtils;
import cn.leancloud.chatkit.utils.LCIMSoftInputUtils;
import de.greenrobot.event.EventBus;


/**
 * Created by wli on 15/7/24.
 * 专门负责输入的底部操作栏，与 activity 解耦
 * 当点击相关按钮时发送 InputBottomBarEvent，需要的 View 可以自己去订阅相关消息
 */
public class LCIMInputBottomBar extends LinearLayout {

  /**
   * 加号 Button
   */
  private View actionBtn;

  /**
   * 文本输入框
   */
  private EditText contentEditText;

  /**
   * 发送文本的Button
   */
  private View sendTextBtn;

  /**
   * 切换到语音输入的 Button
   */
  private View voiceBtn;

  /**
   * 切换到文本输入的 Button
   */
  private View keyboardBtn;

  /**
   * 底部的layout，包含 emotionLayout 与 actionLayout
   */
  private View moreLayout;

  /**
   * 录音按钮
   */
  private LCIMRecordButton recordBtn;

  /**
   * action layout
   */
  private LinearLayout actionLayout;
  private View cameraBtn;
  private View pictureBtn;

  /**
   * 最小间隔时间为 1 秒，避免多次点击
   */
  private final int MIN_INTERVAL_SEND_MESSAGE = 1000;

  public LCIMInputBottomBar(Context context) {
    super(context);
    initView(context);
  }

  public LCIMInputBottomBar(Context context, AttributeSet attrs) {
    super(context, attrs);
    initView(context);
  }


  /**
   * 隐藏底部的图片、emtion 等 layout
   */
  public void hideMoreLayout() {
    moreLayout.setVisibility(View.GONE);
  }


  private void initView(Context context) {
    View.inflate(context, R.layout.lcim_chat_input_bottom_bar_layout, this);
    actionBtn = findViewById(R.id.input_bar_btn_action);
    contentEditText = (EditText) findViewById(R.id.input_bar_et_content);
    sendTextBtn = findViewById(R.id.input_bar_btn_send_text);
    voiceBtn = findViewById(R.id.input_bar_btn_voice);
    keyboardBtn = findViewById(R.id.input_bar_btn_keyboard);
    moreLayout = findViewById(R.id.input_bar_layout_more);
    recordBtn = (LCIMRecordButton) findViewById(R.id.input_bar_btn_record);

    actionLayout = (LinearLayout) findViewById(R.id.input_bar_layout_action);
    cameraBtn = findViewById(R.id.input_bar_btn_camera);
    pictureBtn = findViewById(R.id.input_bar_btn_picture);

    setEditTextChangeListener();
    initRecordBtn();

    actionBtn.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        boolean showActionView =
          (GONE == moreLayout.getVisibility() || GONE == actionLayout.getVisibility());
        moreLayout.setVisibility(showActionView ? VISIBLE : GONE);
        actionLayout.setVisibility(showActionView ? VISIBLE : GONE);
        LCIMSoftInputUtils.hideSoftInput(getContext(), contentEditText);
      }
    });

    contentEditText.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        moreLayout.setVisibility(View.GONE);
        LCIMSoftInputUtils.showSoftInput(getContext(), contentEditText);
      }
    });

    keyboardBtn.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        showTextLayout();
      }
    });

    voiceBtn.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        showAudioLayout();
      }
    });

    sendTextBtn.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        String content = contentEditText.getText().toString();
        if (TextUtils.isEmpty(content)) {
          Toast.makeText(getContext(), R.string.lcim_message_is_null, Toast.LENGTH_SHORT).show();
          return;
        }

        contentEditText.setText("");
        new Handler().postDelayed(new Runnable() {
          @Override
          public void run() {
            sendTextBtn.setEnabled(true);
          }
        }, MIN_INTERVAL_SEND_MESSAGE);

        EventBus.getDefault().post(
          new LCIMInputBottomBarTextEvent(LCIMInputBottomBarEvent.INPUTBOTTOMBAR_SEND_TEXT_ACTION,
            content, getTag()));
      }
    });

    pictureBtn.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        EventBus.getDefault().post(new LCIMInputBottomBarEvent(
          LCIMInputBottomBarEvent.INPUTBOTTOMBAR_IMAGE_ACTION, getTag()));
      }
    });

    cameraBtn.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        EventBus.getDefault().post(new LCIMInputBottomBarEvent(
          LCIMInputBottomBarEvent.INPUTBOTTOMBAR_CAMERA_ACTION, getTag()));
      }
    });
  }

  public void addActionView(View view) {
    actionLayout.addView(view);
  }

  /**
   * 初始化录音按钮
   */
  private void initRecordBtn() {
    recordBtn.setSavePath(LCIMPathUtils.getRecordPathByCurrentTime(getContext()));
    recordBtn.setRecordEventListener(new LCIMRecordButton.RecordEventListener() {
      @Override
      public void onFinishedRecord(final String audioPath, int secs) {
        if (secs > 0)
        EventBus.getDefault().post(
          new LCIMInputBottomBarRecordEvent(
            LCIMInputBottomBarEvent.INPUTBOTTOMBAR_SEND_AUDIO_ACTION, audioPath, secs, getTag()));
        recordBtn.setSavePath(LCIMPathUtils.getRecordPathByCurrentTime(getContext()));
      }

      @Override
      public void onStartRecord() {
      }
    });
  }

  /**
   * 展示文本输入框及相关按钮，隐藏不需要的按钮及 layout
   */
  private void showTextLayout() {
    contentEditText.setVisibility(View.VISIBLE);
    recordBtn.setVisibility(View.GONE);
    voiceBtn.setVisibility(contentEditText.getText().length() > 0 ? GONE : VISIBLE);
    sendTextBtn.setVisibility(contentEditText.getText().length() > 0 ? VISIBLE : GONE);
    keyboardBtn.setVisibility(View.GONE);
    moreLayout.setVisibility(View.GONE);
    contentEditText.requestFocus();
    LCIMSoftInputUtils.showSoftInput(getContext(), contentEditText);
  }

  /**
   * 展示录音相关按钮，隐藏不需要的按钮及 layout
   */
  private void showAudioLayout() {
    contentEditText.setVisibility(View.GONE);
    recordBtn.setVisibility(View.VISIBLE);
    voiceBtn.setVisibility(GONE);
    keyboardBtn.setVisibility(VISIBLE);
    moreLayout.setVisibility(View.GONE);
    LCIMSoftInputUtils.hideSoftInput(getContext(), contentEditText);
  }

  /**
   * 设置 text change 事件，有文本时展示发送按钮，没有文本时展示切换语音的按钮
   */
  private void setEditTextChangeListener() {
    contentEditText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        boolean showSend = charSequence.length() > 0;
        keyboardBtn.setVisibility(!showSend ? View.VISIBLE : GONE);
        sendTextBtn.setVisibility(showSend ? View.VISIBLE : GONE);
        voiceBtn.setVisibility(View.GONE);
      }

      @Override
      public void afterTextChanged(Editable editable) {
      }
    });
  }
}
