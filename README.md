双重 密码验证：github-recovery-codes.txt



PlayerView  


TextureView SurfaceView SphericalGLSurfaceView VideoDecoderGLSurfaceView 优缺点

选型决策树：
1.需要360°/VR播放 → SphericalGLSurfaceView
2.需要添加GL特效/专业编辑 → VideoDecoderGLSurfaceView
3.需要视图变换(旋转/缩放) → TextureView
4.常规视频播放 → PlayerView(默认SurfaceView)


1.功耗：SurfaceView < TextureView(≈+30%) < SphericalGLSurfaceView(≈+300%)
2.内存：SurfaceView(≈15MB) < TextureView(≈25MB) < GLSurfaceView(≈50MB)
3.帧率稳定性：SurfaceView(99.9%) > TextureView(98%) > GLSurfaceView(95%)

兼容性说明：
1.SurfaceView：全API支持，但API24以下动画受限
2.TextureView：API14+完全支持
3.GLSurfaceView：需要OpenGL ES 2.0+/3.0+




关于权限问题：
https://developer.aliyun.com/article/633305
https://blog.csdn.net/c6E5UlI1N/article/details/123515886
https://cloud.tencent.com/developer/article/2412719

android 6  (api  23) 开始远行时需要授权

android 7  (api  24) 提出使用ileProvider来获取文件（同时也要相应的权限）

android 10 (api 29)  引入了分区存储，但不是强制的
                     通过清单配置android:requestLegacyExternalStorage="true"关闭分区存储

android 11 (api 30） 定义 MANAGE_EXTERNAL_STORAGE 权限(包括读取和删除外部存储中的文件，无法直接写入外部存储),强制开启分区存储，应用以 Android 11 为目标版本
                     系统会忽略requestLegacyExternalStorage标记， 访问共享存储空间都需要使用MediaStore进行访问。
                      WRITE_EXTERNAL_STORAGE权限被废弃，这意味着应用仍然无法直接向外部存储写入数据，需要用 MediaStore API
https://blog.csdn.net/weixin_30336075/article/details/140239527


android 13 (api 33)  开始 权限细分 READ_MEDIA_IMAGES，READ_MEDIA_VIDEO ,READ_MEDIA_AUDIO但是使用系统或者谷歌的照片选择器 不需要权限
                     废弃 READ_EXTERNAL_STORAGE,WRITE_EXTERNAL_STORAGE 权限
                      

android 14 (api 34)  新增了READ_MEDIA_VISUAL_USER_SELECTED权限，用于对照片和视频进行选择性授权，
                     如果要全部 仍然要 READ_MEDIA_IMAGES，READ_MEDIA_VIDEO ,READ_MEDIA_AUDIO 权限

获取照片/视频
> android 14  (api 34)  不使用google的图片选择器，就要申请权限（READ_MEDIA_IMAGES，READ_MEDIA_VIDEO，READ_MEDIA_AUDIO,READ_MEDIA_VISUAL_USER_SELECTED）
> android 13  (api 33)  不使用google的图片选择器，就要申请权限（READ_MEDIA_IMAGES，READ_MEDIA_VIDEO，READ_MEDIA_AUDIO）
<= android 12 (api 32)  不使用google的图片选择器，就要申请权限（READ_EXTERNAL_STORAGE，WRITE_EXTERNAL_STORAGE）
拍照 
 >= android 13 （api 33） 拍照/写入读取照片 使用MediaStore 写入图片不需要权限
 >= android 11 （api 30） 拍照/写入读取照片 使用MediaStore 写入图片不需要权限，不使用MediaStorede，仍然要申请权限 READ_EXTERNAL_STORAGE，WRITE_EXTERNAL_STORAGE 权限
<=android 10   （api 29） 拍照/写入读取图片 无MediaStore 写入图片不需要权限，仍然要 READ_EXTERNAL_STORAGE，WRITE_EXTERNAL_STORAGE 权限




视频播放 缓存
 
移动设备（标清）	3MB-5MB	内存受限设备，优先保障流畅性
移动设备（高清）	5MB-8MB	平衡内存占用与缓冲效率
平板/电视（4K）	8MB-12MB	高分辨率视频需要更大缓冲空间
直播流	3MB-6MB	需降低延迟，缓冲量较点播少


关键影响因素
视频分辨率：  
480p视频：3-4MB足够
1080p视频：建议6-8MB
4K视频：需要10MB以上
帧率：
30fps视频：基础缓冲量
60fps视频：建议增加20%-30%缓冲
编码格式：
H.264：标准缓冲量
H.265/HEVC：可适当减少10%-15%
AV1：需要更大缓冲空间


最佳推荐 推荐初始配置 （适用于大多数高清场景）
setTargetBufferBytes(6 * 1024 * 1024) // 6MB