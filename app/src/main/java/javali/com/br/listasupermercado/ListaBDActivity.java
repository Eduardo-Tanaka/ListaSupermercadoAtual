package javali.com.br.listasupermercado;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.http.HttpResponseCache;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javali.com.br.listasupermercado.Entity.ProdutoBD;


public class ListaBDActivity extends ActionBarActivity {

    private ListView lista;
    private List<ProdutoBD> produtos;
    private LinearLayout baseProgressBar;
    private Bundle bundle;

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_bd);

        baseProgressBar = (LinearLayout) findViewById(R.id.baseProgressBar2);

        try {
            File httpCacheDir = new File(getApplicationContext().getCacheDir(), "http");
            long httpCacheSize = 10 * 1024 * 1024; // 10 MiB
            HttpResponseCache.install(httpCacheDir, httpCacheSize);
        } catch (IOException e) {
            Log.i("TAG", "HTTP response cache installation failed:" + e);
        }

        lista = (ListView) findViewById(R.id.listView2);
        lista.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        bundle = getIntent().getExtras();

        new GetListaBDWS().execute();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    protected void onStop() {
        super.onStop();
        HttpResponseCache cache = HttpResponseCache.getInstalled();
        if (cache != null) {
            cache.flush();
        }
    }

    public void enviar(View v) {
        SparseBooleanArray b = lista.getCheckedItemPositions();
        for(int i = 0; i < produtos.size(); i++) {
            if(b.get(i) == true) {
                new ListaWS((ProdutoBD) lista.getItemAtPosition(i)).execute();
            }
        }
        finish();
    }
    private class ListaWS extends AsyncTask<String, Integer, String> {

        ProdutoBD produto;

        public ListaWS(ProdutoBD produtoBD) {
            this.produto = produtoBD;
        }

        @Override
        protected String doInBackground(String... params) {
            String result = "";

            try {
                String url = "http://listasupermercadows.apphb.com/api/listaws/";

                HttpClient httpclient = new DefaultHttpClient();

                HttpPost httpPost = new HttpPost(url);

                String json = "";

                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("Nome", produto.getNome());
                jsonObject.accumulate("Quantidade", 1);
                jsonObject.accumulate("ContaId", bundle.getInt("ContaId"));

                json = jsonObject.toString();

                StringEntity se = new StringEntity(json, "UTF-8");

                httpPost.setEntity(se);

                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");

                HttpResponse httpResponse = httpclient.execute(httpPost);

            } catch (Exception e) {
                Log.d("Erro", e.getLocalizedMessage());
            }
            return result;
        }
    }

    private void listar(){
        ArrayAdapter adapter = new ArrayAdapter<ProdutoBD>(this, android.R.layout.simple_list_item_multiple_choice, produtos);
        adapter.hasStableIds();
        lista.setAdapter(adapter);
    }

    private class GetListaBDWS extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            baseProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {

            String result = "";
            String url = "http://listasupermercadows.apphb.com/api/produtows/";
            HttpClient httpClient = new DefaultHttpClient();
            try {
                HttpResponse response = httpClient.execute(new HttpGet(url));
                Stream stream = new Stream();
                result += stream.getStringFromInputStream(response.getEntity().getContent());
            } catch (IOException e) {
                Log.e("TAG_ASYNC_TASK", e.getMessage());
                baseProgressBar.setVisibility(View.INVISIBLE);
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            baseProgressBar.setVisibility(View.INVISIBLE);
            super.onPostExecute(s);
            Gson gson = new Gson();
            ProdutoBD[] p = gson.fromJson(s, ProdutoBD[].class);
            List<ProdutoBD> listaBanco = new ArrayList<ProdutoBD>();
            for(int i = 0; i < p.length; i++ ) {
                listaBanco.add(p[i]);
            }
            produtos = listaBanco;
            listar();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_lista_bd, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.ingles) {
            Configuration config = new Configuration();
            config.locale = new Locale("en");
            getApplicationContext().getResources().updateConfiguration(config, getApplicationContext().getResources().getDisplayMetrics());
            Intent intent = new Intent(getApplicationContext(), SplashScreenActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else if(id == R.id.portugues) {
            Configuration config = new Configuration();
            config.locale = new Locale("pt");
            getApplicationContext().getResources().updateConfiguration(config, getApplicationContext().getResources().getDisplayMetrics());
            Intent intent = new Intent(getApplicationContext(), SplashScreenActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else if(id == R.id.menuLista) {
            Intent intent = new Intent(this, ListaActivity.class);
            intent.putExtra("ContaId", bundle.getInt("ContaId"));
            startActivity(intent);
        } else if(id == R.id.menuBanco) {
            onResume();
        } else if(id == R.id.sair) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
