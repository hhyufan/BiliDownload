package cc.kafuu.bilidownload.bilibili;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class Bili {
    //下载视频保存目录
    public static File saveDir = null; //= new File(Environment.getExternalStorageDirectory().getPath() + "/Download/BiliVideo");

    public static void initApplication(Context context) {
        saveDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
    }

    public static final OkHttpClient httpClient = new OkHttpClient();//OkHttpUtils.getUnsafeOkHttpClient()

    public static final String UA = "Mozilla/5.0 (iPhone; CPU iPhone OS 15_0_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.0 EdgiOS/46.3.30 Mobile/15E148 Safari/605.1.15";
    public static Headers generalHeaders;
    public static Headers downloadHeaders;
    public static String biliCookie = null;
    public static void updateHeaders(String cookie) {
        biliCookie = cookie;
        Headers.Builder generalHeadersBuilder = new Headers.Builder()
                .add("Accept", "application/json, text/plain, */*")
                .add("Accept-Language", "zh-CN,zh-Hans;q=0.9")
                .add("Origin", "https://m.bilibili.com")
                .add("Referer", "https://m.bilibili.com/")
                .add("User-Agent", Bili.UA);

        Headers.Builder downloadHeadersBuilder = new Headers.Builder()
                .add("accept", "*/*")
                .add("accept-encoding", "gzip, deflate, br")
                .add("Accept-Language", "zh-CN,zh-Hans;q=0.9")
                .add("Connection", "keep-alive")
                .add("Origin", "https://m.bilibili.com")
                .add("Referer", "https://m.bilibili.com/")
                .add("user-agent", Bili.UA);

        if (cookie != null) {
            generalHeadersBuilder.add("Cookie", cookie);
            downloadHeadersBuilder.add("Cookie", cookie);
        }

        generalHeaders = generalHeadersBuilder.build();
        downloadHeaders = downloadHeadersBuilder.build();
    }
    static {
        updateHeaders(null);
    }

    public static BiliAccount biliAccount = null;

    public static void requestExitLogin(final okhttp3.Callback callback) {
        if (biliCookie == null) {
            return;
        }

        int begin = Bili.biliCookie.indexOf("bili_jct=");
        if (begin == -1) {
            return;
        }
        int end = Bili.biliCookie.indexOf(";", begin);
        if (end == -1) {
            return;
        }

        String bili_jct = Bili.biliCookie.substring(begin + 9, end);
        Log.d("bili_jct", bili_jct);


        httpClient.newCall(new Request.Builder()
                .url("https://passport.bilibili.com/login/exit/v2")
                .post(new FormBody.Builder().add("biliCSRF", bili_jct).build())
                .headers(generalHeaders)
                .build()
        ).enqueue(callback);
    }

    /**
     * Api: https://api.bilibili.com/x/player/playurl
     * */
    public static Request playUrlRequest(long cid, long avid, int quality) {
        String url = "https://api.bilibili.com/x/player/playurl?cid=" + cid + "&avid=" + avid + "&otype=json&fourk=1";
        if (quality != 0) {
            url += "&qn=" + quality;
        }
        return new Request.Builder().url(url).headers(Bili.generalHeaders).build();
    }
}
