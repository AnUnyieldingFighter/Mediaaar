package media.library.manager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import media.library.able.MediaResIbl;
import media.library.images.config.entity.MediaEntity;
import media.library.images.manager.MediaManager;
import media.library.images.unmix.ImageLog;

public class GoogleImgVideo {
    private static GoogleImgVideo manager;

    public static GoogleImgVideo getInstance() {
        if (manager == null) {
            manager = new GoogleImgVideo();
        }
        return manager;
    }

    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    public void initPickMedia(AppCompatActivity act) {
        //checkForValidRequestCode(11);
        boolean isAvailable = ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(act);
        Log.d("google图片选择器", "是否可用：" + isAvailable);
        //1.7.0 报错 Can only use lower 16 bits for requestCode
        // 报错原因：androidx.fragment:fragment:1.1.0 : FragmentActivity.checkForValidRequestCode()验证失败
        // ActivityResultRegistry:register->registerKey->generateRandomNumber 产生的reqCode 过大
        // 升级到
        // androidx.activity:activity:1.10.1  任然报错
        // 改其它方案  启动到另外一个页面 会先调用startActivityForResult 另外一个页面传值会本页面 本页面会调用 onActivityResult
        pickMedia = act.registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                ImageLog.d("google图片选择器", "结果: " + uri);
                if (mediaResIbl != null) {
                    List<Uri> uris = new ArrayList<>();
                    uris.add(uri);
                    ArrayList<MediaEntity> res = redUris(uris, act);
                    mediaResIbl.onMediaRes(res);
                }
            } else {
                ImageLog.d("google图片选择器", "No media selected");
                if (mediaResIbl != null) {
                    mediaResIbl.onDismiss();
                }
            }
        });

    }

    private ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia;

    //多张图片与视频
    public void initPickMultipleMedia(AppCompatActivity act, int maxItems) {
        pickMultipleMedia = act.registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(maxItems), uris -> {
            if (!uris.isEmpty()) {
                ImageLog.d("google图片选择器", "结果:: " + uris.size());
                if (mediaResIbl != null) {
                    ArrayList<MediaEntity> res = redUris(uris, act);
                    mediaResIbl.onMediaRes(res);
                }
            } else {
                ImageLog.d("google图片选择器", "结果:No media selected");
                if (mediaResIbl != null) {
                    mediaResIbl.onDismiss();
                }
            }
        });
    }

    //读取数据
    private ArrayList<MediaEntity> redUris(List<Uri> uris, Context context) {
        ArrayList<MediaEntity> res = new ArrayList();
        for (int i = 0; i < uris.size(); i++) {
            Uri uri = uris.get(i);
            int flag = Intent.FLAG_GRANT_READ_URI_PERMISSION;
            context.getContentResolver().takePersistableUriPermission(uri, flag);
            //
            MediaEntity video = MediaManager.getInstance().getImg(uri, context);
            if (video == null) {
                continue;
            }
            String mediaType = video.mediaType;
            if (mediaType == null) {
                mediaType = "";
            }
            if (mediaType.contains("image") || mediaType.contains("gif")) {
                video.type = 1;
            } else {
                video.type = 2;
            }
            res.add(video);
        }
        return res;
    }

    //设置 单选
    public void setSingleChoice(int type) {
        if (pickMedia == null) {
            return;
        }
        selectOneImgVideo(true, type);
    }

    public void setSingleChoice(String mediaTypeStr) {
        if (pickMedia == null) {
            return;
        }
        ActivityResultContracts.PickVisualMedia.SingleMimeType mediaType =
                new ActivityResultContracts.PickVisualMedia.SingleMimeType(mediaTypeStr);
        onRadio(mediaType);
    }

    //设置 多选
    public void setMultipleChoice(int type) {
        if (pickMultipleMedia == null) {
            return;
        }
        selectOneImgVideo(false, type);
    }

    public void setMultipleChoice(String mediaTypeStr) {
        if (pickMultipleMedia == null) {
            return;
        }
        ActivityResultContracts.PickVisualMedia.SingleMimeType mediaType =
                new ActivityResultContracts.PickVisualMedia.SingleMimeType(mediaTypeStr);
        onMultiple(mediaType);

    }

    private void selectOneImgVideo(boolean isOne, int type) {
        ActivityResultContracts.PickVisualMedia.VisualMediaType mediaType = null;
        switch (type) {
            case 1:
                //根据类型，只包括以下对launch()的调用之一  您想让用户从中选择的媒体。
                // 启动照片选择器，让用户选择图像和视频。
                mediaType = ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE;
                break;
            case 2:
                //启动照片选择器，让用户只选择图片和视频
                mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE;
                break;
            case 3:
                //启动照片选择器，让用户仅选择视频。
                mediaType = ActivityResultContracts.PickVisualMedia.VideoOnly.INSTANCE;
                break;
            case 4:
                //启动照片选择器，让用户只选择
                //特定的MIME类型，如gif。
                String mimeType = "image/gif";
                mediaType = new ActivityResultContracts.PickVisualMedia.SingleMimeType(mimeType);
                break;
        }
        if (mediaType == null) {
            return;
        }
        if (isOne) {
            onRadio(mediaType);
        } else {
            onMultiple(mediaType);
        }

    }

    //单选
    private void onRadio(ActivityResultContracts.PickVisualMedia.VisualMediaType mediaType) {
        PickVisualMediaRequest.Builder request = new PickVisualMediaRequest.Builder();
        request.setMediaType(mediaType);
        PickVisualMediaRequest mediaRequest = request.build();
        //
        //mediaRequest = new PickVisualMediaRequest();
        //request.setMediaType(mediaType);
        pickMedia.launch(mediaRequest);
    }

    //多选
    private void onMultiple(ActivityResultContracts.PickVisualMedia.VisualMediaType mediaType) {
        PickVisualMediaRequest.Builder request = new PickVisualMediaRequest.Builder();
        request.setMediaType(mediaType);
        PickVisualMediaRequest mediaRequest = request.build();
        //
        //mediaRequest = new PickVisualMediaRequest();
        //request.setMediaType(mediaType);
        pickMultipleMedia.launch(mediaRequest);
    }

    public void onDestroy() {
        if (pickMedia != null) {
            pickMedia.unregister();
        }
        if (pickMultipleMedia != null) {
            pickMultipleMedia.unregister();
        }
    }

    private MediaResIbl mediaResIbl;

    public void setMediaResIbl(MediaResIbl mediaResIbl) {
        this.mediaResIbl = mediaResIbl;
    }
}
