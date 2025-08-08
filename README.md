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