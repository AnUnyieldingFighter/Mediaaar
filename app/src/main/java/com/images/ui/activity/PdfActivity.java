package com.images.ui.activity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.pdf.PdfRenderer;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.media.option.R;
import media.library.images.ui.thing.photoview.PhotoView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//查看pdf
public class PdfActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill);

        File f = new File("/storage/emulated/0/tk/card/00MZXL10870000014809.pdf");
        if (!f.exists()) {
            Log.e("---", "文件不存在");
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ArrayList<Bitmap> bitmaps = onPdfToBitmap(f);
            ArrayList<View> views = new ArrayList();
            for (int i = 0; i < bitmaps.size(); i++) {
                View view = getView(bitmaps.get(i));
                views.add(view);
            }
            MyPagerAdapter adapter = new MyPagerAdapter(views);
            ViewPager viewPager = (ViewPager) findViewById(R.id.vp);
            viewPager.setAdapter(adapter);
        }
    }

    @androidx.annotation.RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private ArrayList<Bitmap> onPdfToBitmap(File pdfFile) {
        ArrayList<Bitmap> bitmaps = new ArrayList();
        try {
            ParcelFileDescriptor f = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY);
            if (f == null) {
                return bitmaps;
            }
            //限定一张图最大5M
            int fixedSize = 10 * 1024 * 1024;
            PdfRenderer pdfRenderer = new PdfRenderer(f);
            int pageCount = pdfRenderer.getPageCount();
            Log.e("---", "pdf图片张数： " + pageCount + "_" + getSize(fixedSize));
            for (int i = 0; i < pageCount; i++) {
                PdfRenderer.Page page = pdfRenderer.openPage(i);
                int width = getResources().getDisplayMetrics().densityDpi / 72 * page.getWidth();
                int height = getResources().getDisplayMetrics().densityDpi / 72 * page.getHeight();
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                canvas.drawColor(Color.WHITE);
                canvas.drawBitmap(bitmap, 0, 0, null);
                Rect r = new Rect(0, 0, width, height);
                page.render(bitmap, r, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                //
                int bitByte = bitmap.getByteCount();
                String size = bitmap.getByteCount() + "byte";
                Log.e("---", "大小_1：" + size + "_" + getSize(bitmap.getByteCount()) + "_Widt:" + bitmap.getWidth() + "_Height:" + bitmap.getHeight());
                float bitF = (float) fixedSize / (float) bitByte;
                if (bitF < 0.4) {
                    bitF = 0.4f;
                }
                if (bitF < 1) {
                    Matrix matrix = new Matrix();
                    matrix.setScale(bitF, bitF);
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    size = bitmap.getByteCount() + "byte";
                    Log.e("---", bitF + "_大小_2：" + size + "_" + getSize(bitmap.getByteCount()) + "_Widt:" + bitmap.getWidth() + "_Height:" + bitmap.getHeight());
                }
                //
                bitmaps.add(bitmap);
                page.close();
            }
            f.close();
            pdfRenderer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            Log.e("---", "读取bit失败:" + ex.getMessage());
        }
        return bitmaps;

    }

    /**
     * @param sizeI 字节
     * @return
     */
    private String getSize(long sizeI) {

        String value = "B";
        double size = 0;
        if (sizeI > 1024) {
            //kb
            size = sizeI / 1024;
            value = "kb";
        }
        if (size > 1024) {
            //M
            size = size / 1024;
            value = "M";
        }
        if (size > 1024) {
            //G
            size = size / 1024;
            value = "G";
        }
        double temp = sizeI;
        if (size > 0) {
            double size1 = (int) (size * 100);
            size = size1 / 100;
            temp = size;
        }
        return temp + value;
    }

    private View getView(Bitmap bit) {
        View rootView = LayoutInflater.from(this).inflate(R.layout.hos_pager_bill, null);
        PhotoView billIv = (PhotoView) rootView.findViewById(R.id.bill_iv);
        billIv.setImageBitmap(bit);
        return rootView;
    }

    public class MyPagerAdapter extends PagerAdapter {
        private List<View> mViewList = new ArrayList();

        private MyPagerAdapter(List<View> mViewList) {
            this.mViewList = mViewList;
        }

        @Override
        public int getCount() {
            //返回有效的View的个数
            return mViewList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        //instantiateItem该方法的功能是创建指定位置的页面视图。finishUpdate(ViewGroup)返回前，页面应该保证被构造好
        //返回值：返回一个对应该页面的object，这个不一定必须是View，但是应该是对应页面的一些其他容器
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mViewList.get(position));
            return mViewList.get(position);
        }

        //该方法的功能是移除一个给定位置的页面。
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
