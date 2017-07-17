# voice
--android 中实现录音与播放的基本操作
在android中，实现录音与语音播放的功能算是比较简单的，但是作为参考，还是很有必要将语音相关的知识做一个简要的记录。
-----
首先，在android中，支持录音支持两种方式。主要包括：字节流模式和文件流模式。用文件流模式进行录音操作比较简单，而且相对来说，因为其封装性比较好，录制下的文件也会比较小。但是相对于文件流模式，就没有字节流模式那么灵活，但是想要用好字节流模式还是需要下一点功夫的。

下面开始介绍文件流模式的语音操作：
# 文件流模式
我们来看录音部分的实现，首先我们实现开始录音的部分：
在正式编码之前，还是需要对其进行一个简要的说明。一般来说，录音功能的实现是在jin层，而在这一层中，是用单线程实现的。如果我们在编码的对录音api进行多线程操作，会导致程序直接闪退，并且我们是无法在java层对其异常进行捕获的。所以，我们必须使用单线程以保证录音的正常运行。
一般来说，开始录音的步骤也就三个，代码如下：
```
releaseRecord();//释放可能没释放的录音相关资源
if (!doStartRecord()) {//真正的开始录音的函数，开始录音成功返回true，否则返回false
   recordFail(); //开始失败，向用户提示开始录音失败
}
```
接下来我们来看看上述三个方法的实现：
实现是释放相关资源的方法releaseRecord：
```
 if (mMediaRecorder != null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
}
```
其中的mMediaRecorder 的MediaRecorder的全局变量。
接下来是真正实现开始录音的实现逻辑doStartRecord()
```
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
```
这一部分代码比较多，但是关键部分我都给出了注释，相信理解起来也不算难吧。这一部分我们实现的主要是在sdcrad根目录下新建一个voice的目录，然后在新建一个以==.m4a==为后缀名的文件。在配置mAudioRecord的相关参数后，将收集到的录音存放到之前的文件中。如果一切都顺利的话，就返回true ，表示开始录音成功。

最后就是提示用户录音实现的逻辑recordFail
```
      mAudioFile = null;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(VioceActivity.this, "录音失败", Toast.LENGTH_SHORT).show();
            }
        }, 100);
```
这里的逻辑比较简单，但是值得注意的是，因为我们开始录音方法是在一个非主线程的线程中执行的，所以我们需要借助hander来实现界面提示的效果。这里的mHander是一个局部变量，其初始化放在OnCreate()方法中。
```  mHandler = new Handler(Looper.getMainLooper());```
综上，开始录音的所有逻辑已经全部实现。接下来实现的是结束录音的实现逻辑：
主题的逻辑如下：
```
 if (!doStopRecord()) {//实现 停止录音的真正逻辑，成功返回true，否则返回false
                    recordFail();//提示用户录音失败
                }
```
这里的doStopRecord实现逻辑如下：
```
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
```
其实我们实现停止录音的逻辑也很简单，首先调用mMediaRecorder.stop();停止录音，然后对录音时间是否大于3s进行判断，若大于3s，则表示录音有效，提示用户，录音成功。
综上，我们文件流的录音的所有代码已经实现完毕。接下来我们实现对其进行播放。如果需要参考全部的代码，请戳[这里](http://www.baidu.com)。
```
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
```

整体来说，基于文件的录音是比较容易实现的。下面介绍如何通过字节流模式实现录音。

# 字节流模式录音
开始录音：主要逻辑startRecord2()的实现
```
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
```
停止录音的doStopRecord()实现：
```
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
```
对其中相关参数的说名：
```
 private boolean mIsRecord = false;

    private final int BUFFER_SIZE = 2048;//缓存区的大小

    private byte[] mBuffer;

    private FileOutputStream mFileOutputStream;

    private AudioRecord mAudioRecord;

    private File mAudioFile2;
```

接下来，实现的是对其字节流模式录制的文件进行播放：doPlay2()主题类的实现：
```
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
```

千言万语肯定不如直接代码来的直接了当，所以的代码实现点[这里](https://github.com/Reoger/voice)
实现的效果如下：

![22-59-38.jpg](http://upload-images.jianshu.io/upload_images/2178834-cd0cdfcc04663f58.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
