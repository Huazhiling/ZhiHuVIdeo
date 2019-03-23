package com.sd.mvc.intercept_video_link.utils

import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.*

class DownloadBody(responseBody: ResponseBody) : ResponseBody() {
    private var responseBody: ResponseBody? = null
    // BufferedSource 是okio库中的输入流，这里就当作inputStream来使用。
    private var bufferedSource: BufferedSource? = null

    init {
        this.responseBody = responseBody
    }

    override fun contentLength(): Long {
        return responseBody?.contentLength()!!
    }

    override fun contentType(): MediaType? {
        return responseBody?.contentType()!!

    }

    override fun source(): BufferedSource? {
        if (bufferedSource === null) {
            bufferedSource = Okio.buffer(source(responseBody!!.source()))
        }
        return bufferedSource
    }

    fun source(source: Source): Source {
        return object : ForwardingSource(source) {
            var totalBytesRead = 0L
            override fun read(sink: Buffer, byteCount: Long): Long {
                var bytesRead = super.read(sink, byteCount)
                // read() returns the number of bytes read, or -1 if this source is exhausted.
                totalBytesRead += if (bytesRead != -1L) bytesRead else 0
//                if (null != progressListener) {
//                    progressListener.update(totalBytesRead, responseBody.contentLength(), bytesRead == -1);
//                }
                return bytesRead
            }
        }

    }
}