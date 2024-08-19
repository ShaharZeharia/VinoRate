package com.example.vinorate.Utilities;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.vinorate.R;

public class ImageLoader {
    private static volatile ImageLoader instance;
    private static Context context;

    private ImageLoader(Context context) {
        this.context = context.getApplicationContext();
    }


    public static ImageLoader getInstance() {
        return instance;
    }

    public static ImageLoader initImageLoader(Context context) {
        if (instance == null) {
            synchronized (ImageLoader.class) {
                if (instance == null) {
                    instance = new ImageLoader(context);
                }
            }
        }
        return getInstance();
    }

    public void load(String source, ImageView imageView) {
        if (context != null) {
            Glide.with(context)
                    .load(source)
                    .placeholder(R.drawable.ic_unavailable_photo)
                    .centerInside()
                    .into(imageView);
        }
    }

    public void load(Drawable source, ImageView imageView) {
        Glide.with(context)
                .load(source)
                .placeholder(R.drawable.ic_unavailable_photo)
                .centerInside()
                .into(imageView);
    }
}
