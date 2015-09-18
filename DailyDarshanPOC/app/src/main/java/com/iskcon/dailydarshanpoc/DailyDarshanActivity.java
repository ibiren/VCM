package com.iskcon.dailydarshanpoc;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class DailyDarshanActivity extends AppCompatActivity {

    private ArrayList<String> imageUrlsList = new ArrayList<>();
    private ViewPager imagePager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_darshan);

        imagePager = (ViewPager) findViewById(R.id.imagePager);

        new DownloadImgUrlsTask().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_daily_darshan, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class DownloadImgUrlsTask extends AsyncTask<Void, Void, String>
    {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                // Connect to the web site
                Document doc = Jsoup.connect("https://www.dropbox.com/sh/vwtc1f83c0r8038/AABZEBnwx6QYysPG6ZxhHjnxa?dl=0").get();
                // Using Elements to get the Meta data
                Elements contents = doc.getElementsByAttributeValue("class", "filename");
                String url = null;
                for(Element element: contents)
                {
                    url = element.getElementsByTag("a").attr("href");
                    if(url.contains("http"))
                    {
                        if(!imageUrlsList.contains(url))
                            imageUrlsList.add(url);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String aVoid) {
            super.onPostExecute(aVoid);

            imagePager.setAdapter(new FullScreenImageAdapter(imageUrlsList));
        }
    }

    class FullScreenImageAdapter extends PagerAdapter {

        private ArrayList<String> _imagePaths;
        private LayoutInflater inflater;
        private DisplayImageOptions options;
        private ImageLoader imageLoader;

        // constructor
        public FullScreenImageAdapter(ArrayList<String> imagePaths) {
            this._imagePaths = imagePaths;
            options = new DisplayImageOptions.Builder()
                    .cacheInMemory(true) // default
                    .cacheOnDisk(true) // default
                    .considerExifParams(true) // default
                    .bitmapConfig(Bitmap.Config.RGB_565) // default
                    .build();
            imageLoader = ImageLoader.getInstance();
        }

        @Override
        public int getCount() {
            return this._imagePaths.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((FrameLayout) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            final ImageView imgDisplay;
            final ProgressBar progressBar;

            inflater = (LayoutInflater) DailyDarshanActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View viewLayout = inflater.inflate(R.layout.fragment_fullscreen_image, container,
                    false);

            imgDisplay = (ImageView) viewLayout.findViewById(R.id.ivImage);
            progressBar = (ProgressBar) viewLayout.findViewById(R.id.progressBar);

            if (progressBar != null)
                progressBar.setVisibility(View.VISIBLE);
//            ImageSize targetSize = new ImageSize(720, 1280);

            //Load image.
            imageLoader.displayImage(_imagePaths.get(position).replace("dl=0", "dl=1"), imgDisplay, options, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    if (progressBar != null)
                        progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    if (progressBar != null)
                        progressBar.setVisibility(View.GONE);

//                    if (imgDisplay != null)
//                        imgDisplay.setImageBitmap(loadedImage);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                    if (progressBar != null)
                        progressBar.setVisibility(View.GONE);
                }
            });

            ((ViewPager) container).addView(viewLayout);
            return viewLayout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView((FrameLayout) object);

        }

    }
}
