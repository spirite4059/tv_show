package com.retrofit.download;

import android.os.Bundle;
import android.os.Message;

import com.retrofit.tools.Tools;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * Created by fq_mbp on 16/7/19.
 */

public class ProgressResponseBody extends ResponseBody {
    private ResponseBody responseBody;
    private ProgressHandler progressHandler;
    private BufferedSource bufferedSource;

    public ProgressResponseBody(ProgressHandler progressHandler) {
        this.progressHandler = progressHandler;
    }

    public void setResponseBody(ResponseBody responseBody) {
        this.responseBody = responseBody;
    }

    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }


    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(responseBody.source()));
        }
        return bufferedSource;
    }

    private Source source(Source source) {
        return new ForwardingSource(source) {
            long contentLength = 0L;
            long oldTime;
            private boolean isHasSpace;

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);
                contentLength += bytesRead != -1 ? bytesRead : 0;
                Message msg = progressHandler.obtainMessage();
                // 检测磁盘空间
                if (checkSdcardSpace(msg))
                    return bytesRead;

                sendMsg(bytesRead, msg);

                return bytesRead;
            }

            private void sendMsg(long bytesRead, Message msg) {
                if (oldTime == 0 || System.currentTimeMillis() - oldTime >= 1000) {
                    oldTime = System.currentTimeMillis();
                    if(bytesRead == -1){  // 下载完成
                        msg.what = 1;
                        oldTime = 0;
                    }else { // 开始下载
                        msg.what = 0;
                    }
                    Bundle bundle = new Bundle();
                    bundle.putLong("contentLength", contentLength);
                    bundle.putLong("fileSize", responseBody.contentLength());
                    bundle.putBoolean("isFinish", bytesRead == -1);
                    msg.setData(bundle);
                    progressHandler.sendMessage(msg);
//                    progressListener.onProgress(contentLength, responseBody.contentLength(), bytesRead == -1);
                } else if(bytesRead == -1){
                    msg.what = 1;
                    oldTime = 0;
                    progressHandler.sendMessage(msg);
                }
            }

            private boolean checkSdcardSpace(Message msg) {
                if(!isHasSpace){
                    if(Tools.isSdCardHasSpace(responseBody.contentLength())){
                        isHasSpace = true;
                    } else {
                        msg.what = 3;
                        progressHandler.sendMessage(msg);
                        return true;
                    }

                }
                return false;
            }
        };
    }
}