package reoger.hut.voice;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * android 录音、播放  两种模式
 * 文件模式
 * 字节流模式 所有以2结尾的方法都是此模式的方法
 */
public class VioceActivity extends AppCompatActivity {


    private Button mBut; //文件流 模式录音
    private Button mBut1; //字节流 模式录音

    private Button mBut6;//播放按钮 文件流
    private Button mBut7;//播放按钮 字节流

    private TextView mText;

    private ExecutorService mExecutorService;

    private MediaRecorder mMediaRecorder;
    private File mAudioFile;

    private long mStartRecordTime, mEndRecordTime;

    private Handler mHandler;


//////////////////////////////////////////////////////

    private boolean mIsRecord = false;

    private final int BUFFER_SIZE = 2048;//缓存区的大小

    private byte[] mBuffer;

    private FileOutputStream mFileOutputStream;

    private AudioRecord mAudioRecord;

    private File mAudioFile2;

    /////////////////////////////////////////////////////////////

    private volatile boolean mIsPlaying ;
    private MediaPlayer mMediaPlayer;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vioce);

        mHandler = new Handler(Looper.getMainLooper());
        mExecutorService = Executors.newSingleThreadExecutor();//单线程

        mBuffer = new byte[BUFFER_SIZE];


        initView();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mExecutorService.shutdownNow();
        stopPlay();
        releaseRecord();

    }

    /**
     * 停止播放的逻辑
     */
    private void stopPlay() {
        mIsPlaying = false;

        if(mMediaPlayer != null){
            mMediaPlayer.setOnCompletionListener(null);
            mMediaPlayer.setOnErrorListener(null);

            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private void initView() {
        mBut = (Button) findViewById(R.id.button4);
        mBut1 = (Button) findViewById(R.id.button5);
        mBut6 = (Button) findViewById(R.id.button6);
        mBut7 = (Button) findViewById(R.id.button7);

        mText = (TextView) findViewById(R.id.textView);

        //文件模式录音
        mBut.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startRecord();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        stopRecord();
                        break;
                }
                return true;
            }
        });

        //字节流模式录音
        mBut1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mIsRecord){
                    //正在录音
                    mIsRecord = true;
                    mBut1.setText("停止");

                    mExecutorService.submit(new Runnable() {
                        @Override
                        public void run() {
                              if(!startRecord2()){
                                recordFail2();
                              }
                        }
                    });
                }else {
                    mIsRecord = false;
                    mBut1.setText("开始");
                   if(!stopRecord2()){
                       recordFail2();
                   }
                }
            }
        });

        //文件流模式的播放按钮
        mBut6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mAudioFile != null && !mIsPlaying){
                    //设置当前播放状态
                    mIsPlaying = true;

                    mExecutorService.submit(new Runnable() {
                        @Override
                        public void run() {
                             doPlay(mAudioFile);

                        }
                    });
                }
            }
        });

        //字节流录音的播放
        mBut7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mAudioFile2 != null && !mIsPlaying){
                    //设置当前播放状态
                    mIsPlaying = true;

                    mExecutorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            doPlay2(mAudioFile2);

                        }
                    });
                }
            }
        });
    }

    /**
     * 字节流模式的播放 实现
     * @param mAudioFile
     */
    private void doPlay2(File mAudioFile) {

        //声音类型，扬声器播放
        int steamType = AudioManager.STREAM_MUSIC;

        //采样频率
        int sampleRate = 44100;

        //MONO 表示单声道 录音输入单声道 播放也使用单声道
        int channelConfig = AudioFormat.CHANNEL_OUT_MONO;

        //录音使用16bit 所以播放也使用同样的格式
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

        //流模式
        int mode = AudioTrack.MODE_STREAM;

        //计算需要最小buffer的大小
        int minBufferSize =AudioTrack.getMinBufferSize(sampleRate,channelConfig,audioFormat);

        AudioTrack audioTrack = new AudioTrack(steamType,sampleRate,channelConfig,audioFormat,
                Math.max(minBufferSize,BUFFER_SIZE),mode);

        //从文件流中读取数据
        FileInputStream inputStream = null;

        try{
            inputStream = new FileInputStream(mAudioFile2);
            int read;
            //循环读取数据，写到播放器去播放
            audioTrack.play();
            while((read = inputStream.read(mBuffer)) > 0){
                int ret = audioTrack.write(mBuffer,0,read);
            switch (ret){
                case AudioTrack.ERROR:
                case AudioTrack.ERROR_BAD_VALUE:
                case AudioTrack.ERROR_INVALID_OPERATION:
                case AudioTrack.ERROR_DEAD_OBJECT:
                        playFail();
                        return ;
                default:
                    break;
            }
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            mIsPlaying = false;

           if(inputStream != null)
               closeQuietly(inputStream);
            resetQuietly(audioTrack);
        }

    }

    /**
     * 释放audioTrack
     * @param audioTrack
     */
    private void resetQuietly(AudioTrack audioTrack) {
        try {
            audioTrack.stop();
            audioTrack.release();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    /**
     * 静默关闭文件输入流
     * @param inputStream
     */
    private void closeQuietly(FileInputStream inputStream) {
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 文件流
     * 实际播放按钮
     * @param mAudioFile
     */
    private void doPlay(File mAudioFile) {
        //配置播放器 MediaPlayer
        mMediaPlayer = new MediaPlayer();

        try{

            //设置声音文件
            mMediaPlayer.setDataSource(mAudioFile.getAbsolutePath());

            //设置监听回调
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopPlay();
                }
            });

            //设置出错的监听器
            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    playFail();
                    //提示用户
                    stopPlay();
                    //释放播放器
                    return true;
                }
            });

            //配置音量，是否循环
            mMediaPlayer.setVolume(1,1);
            mMediaPlayer.setLooping(false);

            mMediaPlayer.prepare();
            mMediaPlayer.start();
        }catch (Exception  e){
            e.printStackTrace();
            playFail();
            stopPlay();
        }
    }

    /**
     * 提示用户播放失败
     */
    private void playFail() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(VioceActivity.this,"播放失败",Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 录音错误处理
     */
    private void recordFail2() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(VioceActivity.this, "录音失败", Toast.LENGTH_SHORT).show();

                mIsRecord = false;
                mBut1 .setText("开始");
            }
        });
    }

    /**
     * 停止录音（流模式）
     */
    private boolean stopRecord2() {
        try {
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;

            mFileOutputStream.close();

            mEndRecordTime = System.currentTimeMillis();

            final int second = (int) ((mEndRecordTime - mStartRecordTime)/1000);
            if(second >3){
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mText.setText("录音成功"+second+" 秒");
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * 启动录音逻辑(流模式)
     * @return
     */
    private boolean startRecord2() {
        try {
            mAudioFile2  = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/voice/"
                    + System.currentTimeMillis() + ".pcm");
            mAudioFile2.getParentFile().mkdirs();
            mAudioFile2.createNewFile();

            mFileOutputStream = new FileOutputStream(mAudioFile2);
            //配置AudioRecord

            //从麦克风采集数据
            int audioSource = MediaRecorder.AudioSource.MIC;

            //采集频率
            int sampleRate = 44100;

            //单声道输入
            int channelConfig = AudioFormat.CHANNEL_IN_MONO;

            //设置pcm（脉冲编码调制 Pulse Code Modulation）编码格式
            int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

            //计算AudioRecord 内存buffer最小的大小
            int minBufferSize = AudioRecord.getMinBufferSize(sampleRate,channelConfig,audioFormat);
            //创建AudioRecord对象
            mAudioRecord = new AudioRecord(audioSource,sampleRate,channelConfig,audioFormat,Math.max(BUFFER_SIZE,minBufferSize));

            mAudioRecord.startRecording();
            mStartRecordTime = System.currentTimeMillis();

            //循环读取数据，写到输出流中
            while(mIsRecord){
                int read = mAudioRecord.read(mBuffer,0,BUFFER_SIZE);
                if(read >0 ){
                    //读取文件写到文件中
                    mFileOutputStream.write(mBuffer,0,read);
                }else{
                    return false;
                }
            }

        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            return false;
        }

        //停止录音
        return true;
    }

    /**
     * 文件流模式
     * 停止录音
     */
    private void stopRecord() {
        mBut.setText(R.string.press_say);
        mBut.setBackgroundResource(android.R.drawable.dark_header);

        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                if (!doStopRecord()) {
                    recordFail();
                }
            }
        });

    }

    /**
     * 文件流模式
     * 停止录音 真正的逻辑
     * @return
     */
    private boolean doStopRecord() {
        try {

            mMediaRecorder.stop();
            mEndRecordTime = System.currentTimeMillis();

            final int seond = (int) ((mEndRecordTime - mStartRecordTime) / 1000);

            if (seond < 3) {
                recordFail();
                return false;
            } else {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mText.setText("录音" + seond + "成功！");
                    }
                });
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            return  false;
        }
        return true;
    }

    /**
     * 文件流模式
     * 开始录音
     */
    private void startRecord() {
        mBut.setText(R.string.spack);
        mBut.setBackgroundResource(R.mipmap.ic_launcher_round);

        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                //释放之前的record
                releaseRecord();

                if (!doStartRecord()) {
                    recordFail();
                }
            }
        });
    }

    /**
     * 提醒用户录音失败
     */
    private void recordFail() {
        mAudioFile = null;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(VioceActivity.this, "录音失败", Toast.LENGTH_SHORT).show();
            }
        }, 100);
    }

    /**
     * 真正的录音逻辑
     *
     * @return
     */
    private boolean doStartRecord() {
        try {
            mMediaRecorder = new MediaRecorder();

            mAudioFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/voice/"
                    + System.currentTimeMillis() + ".m4a");
            mAudioFile.getParentFile().mkdirs();
            mAudioFile.createNewFile();

            //设置从麦克风采集声音
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

            //保存文件为mp4的格式
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

            //设置所有android系统都支持的采样频率
            mMediaRecorder.setAudioSamplingRate(44100);

            //设置acc的编码方式
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

            //设置比较好的音质
            mMediaRecorder.setAudioEncodingBitRate(96000);

            mMediaRecorder.setOutputFile(mAudioFile.getAbsolutePath());

            mMediaRecorder.prepare();
            mMediaRecorder.start();

            mStartRecordTime = System.currentTimeMillis();

        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            return false;
        }finally {
            if(mAudioRecord != null){
                mAudioRecord.release();
            }
        }

        return true;
    }

    /**
     * 释放录音资源
     */
    private void releaseRecord() {
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }
}
