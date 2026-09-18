package media.library.images.ui.provider;


import androidx.core.content.FileProvider;

public class OptionFileProvider extends FileProvider {
    //android:exported="false" 表示这个 Provider 不对外公开暴露。
    //也就是说，其他 App 不能随便直接访问你的 FileProvider。这是安全设置，通常 FileProvider 都应该写 false
    //android:grantUriPermissions="true"
    //以后，系统可以临时把这个 photoUri 的读写权限授权给相机 App。
    //简单说：允许你主动临时授权别人访问某个具体文件 Uri。
    //组合起来就是：
    //平时不公开，但当你主动分享某个 Uri 时，可以临时给对方权限。


    //1.<external-path
    //    name="options"
    //    path="/" />
    //意思是允许 FileProvider 访问外部存储根目录下面的文件。
    //external-path 对应的大概是：
    //storage/emulated/0/
    //path="/" 表示这个目录下所有路径都可能被 FileProvider 暴露，比如：
    //storage/emulated/0/DCIM/a.jpg
    //storage/emulated/0/Download/b.pdf
    //storage/emulated/0/cs/video/test.mp4
    //name="options" 是生成 Uri 时路径里的别名，不是真实目录名。
    //比如真实文件：
    //storage/emulated/0/DCIM/a.jpg
    //生成的 Uri 可能类似：
    //content://你的包名.fileprovider/options/DCIM/a.jpg

    //2.<root-path name="root_path" path="." />
    //表示设备根路径 /，范围更大，通常更不安全。

    //3.<external-path name="ty" path="cs/video" />
    //表示只允许外部存储下的：
    //storage/emulated/0/cs/video/
    //这个范围更小、更安全


    //4.<external-files-path
    //    name="pictures"
    //    path="Pictures/" />
    //5.<external-path
    //    name="camera"
    //    path="DCIM/Camera/" />
}
