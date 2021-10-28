package cc.kafuu.bilidownload.bilibili.video;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cc.kafuu.bilidownload.bilibili.Bili;
import cc.kafuu.bilidownload.bilibili.VideoParsingCallback;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class BiliVideo {

    private static final Headers generalHeaders;
    static {
        generalHeaders = new Headers.Builder()
                .add("Accept", "application/json, text/plain, */*")
                .add("Accept-Language", "zh-CN,zh-Hans;q=0.9")
                .add("Origin", "https://m.bilibili.com")
                .add("Referer", "https://m.bilibili.com/")
                .add("User-Agent", Bili.UA)
                .build();
    }

    public static void fromBv(final String bv, final VideoParsingCallback callback) {
        try {
            //构造访问
            Request request = new Request.Builder()
                    .url("https://api.bilibili.com/x/web-interface/view/detail?aid=&bvid=" + bv.substring(2) + "&recommend_type=&need_rcmd_reason=1")
                    .headers(generalHeaders)
                    .build();
            Bili.httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    callback.onFailure(e.getMessage());
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    analyzingResponse(response, callback);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            callback.onFailure(e.getMessage());
        }
    }

    public static void fromAv(final String av, final VideoParsingCallback callback) {
        try {
            //构造访问
            Request request = new Request.Builder()
                    .url("https://api.bilibili.com/x/web-interface/view/detail?aid=" + av + "&bvid=&recommend_type=&need_rcmd_reason=1")
                    .headers(generalHeaders)
                    .build();
            Bili.httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    callback.onFailure(e.getMessage());
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) {
                    try {
                        analyzingResponse(response, callback);
                    } catch (Exception e) {
                        callback.onFailure(e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            callback.onFailure(e.getMessage());
        }
    }

    private static void analyzingResponse(@NonNull Response response, final VideoParsingCallback callback) throws IOException {
        ResponseBody body = response.body();
        if (body == null) {
            callback.onFailure("Request is returned empty");
            return;
        }

        String json = body.string();

        Log.d("Bili response", json);

        JsonObject res = new Gson().fromJson(json, JsonObject.class);

        if (res.get("code").getAsInt() != 0) {
            callback.onFailure(res.get("message").getAsString());
            return;
        }

        JsonObject data = res.get("data").getAsJsonObject();
        if (data == null) {
            callback.onFailure("Video data is returned empty");
        } else {
            callback.onComplete(new BiliVideo(data));
        }
    }


    private String mBv;
    private long mAid;
    private String mPicUrl;
    private String mTitle;
    private String mDesc;
    private List<BiliPlayInfo> mVideos;

    private BiliVideo(@NonNull JsonObject data) {
        JsonObject view = data.getAsJsonObject("View");

        mBv = view.get("bvid").getAsString();
        mAid = view.get("aid").getAsLong();
        mPicUrl = view.get("pic").getAsString();
        mTitle = view.get("title").getAsString();
        mDesc = view.get("desc").getAsString();

        mVideos = new ArrayList<>();
        for (JsonElement element : view.getAsJsonArray("pages")) {
            JsonObject page = element.getAsJsonObject();

            long cid = page.get("cid").getAsLong();
            String partName = page.get("part").getAsString();
            String partDuration = page.get("duration").getAsString();

            mVideos.add(new BiliPlayInfo(cid, partName, partDuration));
        }
    }

    public String getBv() {
        return mBv;
    }

    public long getAid() {
        return mAid;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDesc() {
        return mDesc;
    }

    public String getPicUrl() {
        return mPicUrl;
    }

    public List<BiliPlayInfo> getVideos() {
        return mVideos;
    }

    @NotNull
    @Override
    public String toString() {
        return "Bv: " + getBv() + ", Aid: " + getAid() + ", Title: " + getTitle() + ", Desc: " + getDesc() + ", Pic: " + getPicUrl();
    }
}
