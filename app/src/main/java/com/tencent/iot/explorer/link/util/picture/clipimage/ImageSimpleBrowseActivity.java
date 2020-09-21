package com.tencent.iot.explorer.link.util.picture.clipimage;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tencent.iot.explorer.link.R;;

public class ImageSimpleBrowseActivity extends Activity {

    public static byte[] bis;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_clip_main);

        Bitmap bitmap = null;
        Bundle bundle = getIntent().getExtras();
//        if (bundle != null) {
//            byte[] bis = bundle.getByteArray("bitmap");
//            bitmap = BitmapFactory.decodeByteArray(bis, 0, bis.length);
//            ((ImageView) findViewById(R.id.image_clip_main_pic_iv)).setImageBitmap(bitmap);
//        }
//
        if (bis != null) {
            bitmap = BitmapFactory.decodeByteArray(bis, 0, bis.length);
            ((ImageView) findViewById(R.id.image_clip_main_pic_iv)).setImageBitmap(bitmap);
        }


        findViewById(R.id.image_clip_main_submit_iv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_OK);
                finish();
            }
        });
        ((TextView) findViewById(R.id.image_clip_main_exit_iv)).setText(R.string.back);
        findViewById(R.id.image_clip_main_exit_iv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });
    }

    public static Bundle getBundle(byte[] bitmap) {
        Bundle bundle = new Bundle();
        bundle.putByteArray("bitmap", bitmap);
        return bundle;
    }

    @Override
    protected void onDestroy() {
        if (bis != null) {
            bis = null;
        }
        super.onDestroy();
    }
}
