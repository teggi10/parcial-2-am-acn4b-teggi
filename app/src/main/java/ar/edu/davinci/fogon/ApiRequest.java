package ar.edu.davinci.fogon;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ApiRequest extends AsyncTask<String, Integer, RecipesResponse> {
    public interface Listener {
        void onRecipesLoaded(RecipesResponse response);

        void onRecipesError();
    }

    private static final OkHttpClient client = new OkHttpClient();
    private final Listener listener;

    public ApiRequest(Listener listener) {
        this.listener = listener;
    }

    private String run(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("HTTP " + response.code());
            return response.body().string();
        }
    }

    @Override
    protected RecipesResponse doInBackground(String... strings) {
        try {
            String body = run(strings[0]);
            return new Gson().fromJson(body, RecipesResponse.class);
        } catch (IOException | JsonParseException e) {
            Log.e("api-request", "Could not load recipes", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(RecipesResponse response) {
        super.onPostExecute(response);
        if (response == null || response.getRecipes().isEmpty()) {
            listener.onRecipesError();
        } else {
            listener.onRecipesLoaded(response);
        }
    }
}
