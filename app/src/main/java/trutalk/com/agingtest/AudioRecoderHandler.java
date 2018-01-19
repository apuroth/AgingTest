package trutalk.com.agingtest;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;

import java.io.File;

/**
 * 实时音频录制处理类<br/>
 * 记得申明系统权限：MODIFY_AUDIO_SETTINGS、RECORD_AUDIO<br/>
 * 使用实例代码：<br/>
 *
 * <pre>
 * audioRecoderHandler = new AudioRecoderHandler(this);
 * audioRecoderHandler.startRecord(new AudioRecordingCallback() {
 *  &#064;Override
 *  public void onStopRecord(String savedPath) {
 *
 *  }
 *
 *  &#064;Override
 *  public void onRecording(byte[] data, int startIndex, int length) {
 *      // TODO 录制监听。处理data即可。立即播放or发送出去，随你。
 *  }
 * });
 * </pre>
 *
 *
 */
public class AudioRecoderHandler {
    /**
     * 录音数据单次回调数组最大为多少
     */
    private static int MAX_DATA_LENGTH = 160;

    private AudioRecord audioRecord;// 录音对象
    private boolean isRecording = false;// 标记是否正在录音中
    private int frequence = 8000;// 采样率 8000
    private int channelInConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;// 定义采样通道（过时，但是使用其他的又不行
    private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;// 定义音频编码（16位）
    private byte[] buffer = null;// 录制的缓冲数组
    private File lastCacheFile = null;// 记录上次录制的文件名

    //public CacheCleanManager cacheManager;

    public AudioRecoderHandler(Context context) {
        if (context == null) {
            throw new RuntimeException("Context could not be null!");
        }
    }

    /**
     * 开始录制音频
     *
     *            录制过程中的回调函数
     */
    public void startRecord(AudioRecordingCallback audioRecordingCallback) {
        RecordTask task = new RecordTask(audioRecordingCallback);
        task.execute();// 开始执行
    }

    /**
     * 停止录制
     */
    public void stoppRecord() {
        isRecording = false;
    }

    /**
     * 删除上次录制的文件（一般是用户取消发送导致删除上次录制的内容）
     *
     * @return true表示删除成功，false表示删除失败，一般是没有上次录制的文件，或者文件已经被删除了
     */
    public boolean deleteLastRecordFile() {
        boolean success = false;
        if (lastCacheFile != null && lastCacheFile.exists()) {
            success = lastCacheFile.delete();
        }
        return success;
    }

    /**
     * 录制音频的任务类
     *
     * @author 李长军
     *
     */
    private class RecordTask extends AsyncTask<String, Integer, String> {

        private AudioRecordingCallback audioRecordingCallback = null;

        public RecordTask(AudioRecordingCallback audioRecordingCallback) {
            this.audioRecordingCallback = audioRecordingCallback;
        }

        @Override
        protected void onPreExecute() {
            // 根据定义好的几个配置，来获取合适的缓冲大小
            // int bufferSize = 800;
            int bufferSize = AudioRecord.getMinBufferSize(frequence,
                    channelInConfig, audioEncoding);
            // 实例化AudioRecord
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    frequence, channelInConfig, audioEncoding, bufferSize);
            // 定义缓冲数组
            buffer = new byte[bufferSize];
            MAX_DATA_LENGTH = bufferSize / 2;
            audioRecord.startRecording();// 开始录制
            isRecording = true;// 设置录制标记为true
        }

        @Override
        protected void onPostExecute(String result) {
            audioRecord = null;
            if (result == null) {
                lastCacheFile = null;
            } else {
                lastCacheFile = new File(result);
            }
            if (audioRecordingCallback != null) {
                audioRecordingCallback.onStopRecord(result);
            }
        }

        @Override
        protected String doInBackground(String... params) {
            String tempFileName = null;
            // 开始录制
            while (isRecording) {
                // 录制的内容放置到了buffer中，result代表存储长度
                int result = audioRecord.read(buffer, 0, buffer.length);
                // 将数据回调出去
                if (audioRecordingCallback != null) {
                    int offset = result % MAX_DATA_LENGTH > 0 ? 1 : 0;
                    for (int i = 0; i < result / MAX_DATA_LENGTH + offset; i++) {
                        int length = MAX_DATA_LENGTH;
                        if ((i + 1) * MAX_DATA_LENGTH > result) {
                            length = result - i * MAX_DATA_LENGTH;
                        }
                        audioRecordingCallback.onRecording(buffer, i
                                * MAX_DATA_LENGTH, length);
                    }
                }
            }
            if (audioRecord != null) {
                audioRecord.stop();
            }
            return tempFileName;
        }
    }

    /**
     * 监听录制过程，用于实时获取录音数据
     *
     * @author 李长军
     *
     */
    public static interface AudioRecordingCallback {
        /**
         * 录音数据获取回调
         *
         * @param data
         *            数据数组对象
         * @param startIndex
         *            数据其开始
         * @param length
         *            数据的结尾
         */
        public void onRecording(byte[] data, int startIndex, int length);

        /**
         * 录音结束后的回调
         *
         * @param savedPath
         *            录音文件存储的路径
         */
        public void onStopRecord(String savedPath);
    }

    /**
     * 释放资源
     */
    public void release() {
        if (audioRecord != null) {
            audioRecord.release();
            audioRecord = null;
        }

    }
}
