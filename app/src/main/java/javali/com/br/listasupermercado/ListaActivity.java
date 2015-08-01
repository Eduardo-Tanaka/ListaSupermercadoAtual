package javali.com.br.listasupermercado;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.http.HttpResponseCache;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

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

import javali.com.br.listasupermercado.Entity.Produto;
import javali.com.br.listasupermercado.Entity.ProdutoBD;


public class ListaActivity extends ActionBarActivity {

    private ListView lista;
    private AutoCompleteTextView autoComplete;
    private List<ProdutoBD> produtosBD;
    private List<Produto> minhaLista;
    private Context ctx;
    private ProdutoAdapter produtoAdapter;
    private LinearLayout baseProgressBar;
    private Bundle bundle;

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);

        try {
            File httpCacheDir = new File(getApplicationContext().getCacheDir(), "http");
            long httpCacheSize = 10 * 1024 * 1024; // 10 MiB
            HttpResponseCache.install(httpCacheDir, httpCacheSize);
        } catch (IOException e) {
            Log.i("TAG", "HTTP response cache installation failed:" + e);
        }

        produtosBD = new ArrayList<>();

        lista = (ListView)findViewById(R.id.listView);
        autoComplete = (AutoCompleteTextView)findViewById(R.id.autoComplete);

        baseProgressBar = (LinearLayout) findViewById(R.id.baseProgressBar);

        ctx = this;

        minhaLista = new ArrayList<>();

        bundle = getIntent().getExtras();

        new GetListaWS().execute();
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

    @Override
    protected void onStart() {
        super.onStart();
        new GetListaBDWS().execute();
    }

    private class GetListaBDWS extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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
            }
            return result;

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Gson gson = new Gson();
            ProdutoBD[] p = gson.fromJson(s, ProdutoBD[].class);
            List<ProdutoBD> listaBanco = new ArrayList<>();
            for(int i = 0; i < p.length; i++ ) {
                listaBanco.add(p[i]);
            }
            produtosBD = listaBanco;
            listarBD();
        }
    }

    private void listarBD(){
        String[] list = new String[produtosBD.size()];
        for (int i = 0; i < list.length; i++) {
            list[i] = produtosBD.get(i).getNome();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, list);
        autoComplete.setAdapter(adapter);
    }


    public void adicionar(View v) {
        if(autoComplete.getText().toString().length() > 0) {
            new ListaPostWS().execute();
            ((InputMethodManager) getSystemService(ListaActivity.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
            onResume();
        } else {
            Resources resources = getResources();
            Toast.makeText(this, resources.getString(R.string.adicioneUmProdutoValido), Toast.LENGTH_SHORT).show();
        }
    }

    private class GetListaWS extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            baseProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {

            String result = "";
            String url = "http://listasupermercadows.apphb.com/api/logarws/" + bundle.getInt("ContaId");
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
            Produto[] p = gson.fromJson(s, Produto[].class);
            List<Produto> listaProduto = new ArrayList<>();
            for(int i = 0; i < p.length; i++ ) {
                listaProduto.add(p[i]);
            }
            minhaLista = listaProduto;
            listar();
        }
    }

    public void listar() {
        produtoAdapter = new ProdutoAdapter(ctx, R.layout.produto, minhaLista);
        produtoAdapter.notifyDataSetChanged();

        lista.setAdapter(produtoAdapter);
    }

    private class ListaPostWS extends AsyncTask<String, Integer, String> {

        Produto produto;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            produto = new Produto();
            produto.setNome(autoComplete.getText().toString());
            produto.setQuantidade(1);
            produto.setContaId(bundle.getInt("ContaId"));
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
                jsonObject.accumulate("Quantidade", produto.getQuantidade());
                jsonObject.accumulate("ContaId", produto.getContaId());

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

    @Override
    protected void onResume() {
        super.onResume();
        new GetListaWS().execute();
        lista.setAdapter(produtoAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_lista, menu);
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
            onResume();
        } else if(id == R.id.menuBanco) {
            Intent intent = new Intent(this, ListaBDActivity.class);
            intent.putExtra("ContaId", bundle.getInt("ContaId"));
            startActivity(intent);
        } else if(id == R.id.sair) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}
