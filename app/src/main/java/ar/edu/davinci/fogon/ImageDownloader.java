package ar.edu.davinci.fogon;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageDownloader extends AsyncTask<String, Integer, Bitmap> {
    private static final String USER_AGENT = "FogonRecipesApp/2.0 (Android)";
    private static final LruCache<String, Bitmap> cache = new LruCache<String, Bitmap>(16 * 1024 * 1024) {
        @Override
        protected int sizeOf(String key, Bitmap bitmap) {
            return bitmap.getByteCount();
        }
    };

    private final ImageView image;
    private String requestedUrl;

    public ImageDownloader(ImageView image) {
        this.image = image;
    }

    public static void load(ImageView image, String url) {
        image.setTag(url);
        Bitmap cached = cache.get(url);
        if (cached != null) {
            image.setImageBitmap(cached);
            return;
        }
        // Download several images at the same time instead of one after the other.
        new ImageDownloader(image).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
    }

    @Override
    protected Bitmap doInBackground(String... strings) {
        String urlDownload = strings[0];
        requestedUrl = urlDownload;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(urlDownload).openConnection();
            connection.setRequestProperty("User-Agent", USER_AGENT);
            try (InputStream content = connection.getInputStream()) {
                Bitmap bitmap = BitmapFactory.decodeStream(content);
                if (bitmap != null) cache.put(urlDownload, bitmap);
                return bitmap;
            }
        } catch (IOException e) {
            Log.e("image-downloader", "Could not download " + urlDownload, e);
            return null;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        if (bitmap == null) return;
        // The view may have been asked for another image while this one was downloading.
        if (!requestedUrl.equals(image.getTag())) return;
        this.image.setImageBitmap(bitmap);
    }
}
