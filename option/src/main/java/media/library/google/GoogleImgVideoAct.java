package media.library.google;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.images.imageselect.R;


//google 图片选择器
public class GoogleImgVideoAct extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_google_img_video);
        Intent intent = getIntent();
        String type = intent.getStringExtra("arg0");
        selectOneImg2(type);

    }


    //选择一张图片
    private void selectOneImg(int type) {
        onRadio(type);
    }

    private void onRadio(int type) {
        //在单选模式下注册照片拾取器活动启动器。
        ActivityResultLauncher<PickVisualMediaRequest> pickMedia = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(), uri -> {
                    // Callback is invoked after the user selects a media item or closes the
                    // photo picker.
                    if (uri != null) {
                        Log.d("PhotoPicker", "Selected URI: " + uri);

                    } else {
                        Log.d("PhotoPicker", "No media selected");
                    }
                });
        switch (type) {
            case 1:
                //根据类型，只包括以下对launch()的调用之一  您想让用户从中选择的媒体。
                // 启动照片选择器，让用户选择图像和视频。
                pickMedia.launch(
                        new PickVisualMediaRequest.Builder()
                                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE)
                                .build());
                break;
            case 2:
                // 启动照片选择器，让用户只选择图片和视频
                pickMedia.launch(
                        new PickVisualMediaRequest.Builder()
                                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                                .build()
                );
                break;
            case 3:
                //启动照片选择器，让用户仅选择视频。
                pickMedia.launch(
                        new PickVisualMediaRequest.Builder()
                                .setMediaType(ActivityResultContracts.PickVisualMedia.VideoOnly.INSTANCE)
                                .build()
                );
                break;
            case 4:
                //启动照片选择器，让用户只选择
                //特定的MIME类型，如gif。
                String mimeType = "image/gif";
                pickMedia.launch(
                        new PickVisualMediaRequest.Builder()
                                .setMediaType(
                                        new ActivityResultContracts.PickVisualMedia
                                                .SingleMimeType(mimeType))
                                .build()
                );
                break;
        }


    }

    private void selectOneImg2(int type) {
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
                mediaType = new ActivityResultContracts.PickVisualMedia
                        .SingleMimeType(mimeType);
                break;
        }
        if (mediaType == null) {
            return;
        }
        onRadio2(mediaType);
    }

    private void selectOneImg2(String type) {
        ActivityResultContracts.PickVisualMedia.VisualMediaType mediaType = null;
        switch (type) {
            case "1":
                //根据类型，只包括以下对launch()的调用之一  您想让用户从中选择的媒体。
                // 启动照片选择器，让用户选择图像和视频。
                mediaType = ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE;
                break;
            case "2":
                //启动照片选择器，让用户只选择图片和视频
                mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE;
                break;
            case "3":
                //启动照片选择器，让用户仅选择视频。
                mediaType = ActivityResultContracts.PickVisualMedia.VideoOnly.INSTANCE;
                break;
            case "4":
                //启动照片选择器，让用户只选择
                //特定的MIME类型，如gif。
                String mimeType = "image/gif";
                mediaType = new ActivityResultContracts.PickVisualMedia
                        .SingleMimeType(mimeType);
                break;
        }
        if (mediaType == null) {
            return;
        }
        onRadio2(mediaType);
    }

    private void onRadio2(ActivityResultContracts.PickVisualMedia.VisualMediaType mediaType) {
        ActivityResultLauncher<PickVisualMediaRequest> pickMedia = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(), uri -> {
                    // Callback is invoked after the user selects a media item or closes the
                    // photo picker.
                    if (uri != null) {
                        Log.d("PhotoPicker", "Selected URI: " + uri);

                    } else {
                        Log.d("PhotoPicker", "No media selected");
                    }
                });
        pickMedia.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(mediaType)
                        .build()
        );
    }

}
