package media.library.player.view;

import android.content.Context;
import android.os.Handler;

import java.util.ArrayList;

import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.Renderer;
import androidx.media3.exoplayer.audio.AudioRendererEventListener;
import androidx.media3.exoplayer.audio.AudioSink;
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector;
import androidx.media3.exoplayer.video.VideoRendererEventListener;

@UnstableApi
public class FFmpegRenderersFactory extends DefaultRenderersFactory {
    //1.EXTENSION_RENDERER_MODE_ON:
    // 仅启用，不优先,该模式下，Media3 会启用扩展渲染器，但扩展渲染器的优先级低于默认渲染器
    // （系统原生渲染器，如 MediaCodecVideoRenderer、MediaCodecAudioRenderer）。
    //播放器会先尝试使用默认渲染器处理媒体流（如用 MediaCodec 硬解码处理音频 / 视频），只有当默认渲染器不支持当前媒体格式
    // （如默认解码器无法解析 FLAC 音频）或初始化 / 解码失败时，才会降级使用扩展渲染器。
    //2.EXTENSION_RENDERER_MODE_PREFER
    //该模式下，Media3 不仅会启用扩展渲染器，还会让扩展渲染器的优先级高于默认渲染器。
    //播放器会优先尝试使用扩展渲染器处理媒体流，只有当扩展渲染器不支持当前媒体格式或执行失败时，才会回退到默认渲染器。
    @OptIn(markerClass = UnstableApi.class)
    public FFmpegRenderersFactory(Context context) {
        super(context);
        setExtensionRendererMode(EXTENSION_RENDERER_MODE_PREFER); // 优先使用FFmpeg
        setEnableDecoderFallback(true);
    }

    @Override
    protected void buildVideoRenderers(Context context, int extensionRendererMode,
                                       MediaCodecSelector
                                               mediaCodecSelector, boolean enableDecoderFallback, Handler eventHandler,
                                       VideoRendererEventListener eventListener,
                                       long allowedVideoJoiningTimeMs, ArrayList<Renderer> out) {
        // 优先使用 FFmpeg 视频解码器
        super.buildVideoRenderers(context, DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER, mediaCodecSelector,
                enableDecoderFallback, eventHandler, eventListener, allowedVideoJoiningTimeMs, out);
    }

    @Override
    protected void buildAudioRenderers(
            Context context,
            int extensionRendererMode,
            MediaCodecSelector mediaCodecSelector,
            boolean enableDecoderFallback,
            AudioSink audioSink,
            Handler eventHandler,
            AudioRendererEventListener eventListener,
            ArrayList<Renderer> out) {
        // 音频使用系统硬件解码，关闭 FFmpeg 音频扩展
        //out.add(new FfmpegAudioRenderer());//FfmpegAudioRenderer
        super.buildAudioRenderers(
                context,
                DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF,
                mediaCodecSelector,
                enableDecoderFallback,
                audioSink,
                eventHandler,
                eventListener,
                out);
    }
}