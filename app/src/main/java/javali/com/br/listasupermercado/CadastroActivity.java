package javali.com.br.listasupermercado;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javali.com.br.listasupermercado.Entity.Produto;
import javali.com.br.listasupermercado.Entity.Usuario;

public class CadastroActivity extends ActionBarActivity {

    private EditText usuario;
    private EditText senha;
    private EditText senha2;
    private String user;
    private String password;
    private LinearLayout baseProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        usuario = (EditText) findViewById(R.id.txtUsuario1);
        senha = (EditText) findViewById(R.id.txtSenha1);
        senha2 = (EditText) findViewById(R.id.txtSenha2);
        baseProgressBar = (LinearLayout) findViewById(R.id.baseProgressBar4);
    }

    public void logar(View v) {
        finish();
    }

    public void cadastrar(View v) {
        if(senha.getText().toString().equals(senha2.getText().toString()) && usuario.getText().length() > 0 && senha.getText().length() > 0) {
            user = usuario.getText().toString();
            password = senha.getText().toString();

            new CadastroWSPost().execute();
        } else {
            Resources resources = getResources();
            Toast.makeText(this, resources.getString(R.string.senhas), Toast.LENGTH_SHORT).show();
        }
    }

    private class CadastroWSPost extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            baseProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            String result = "";

            try {
                String urlWS = "http://listasupermercadows.apphb.com/api/contaws/";

                HttpClient httpclient = new DefaultHttpClient();

                HttpPost httpPost = new HttpPost(urlWS);

                String json = "";
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("Usuario", user);
                jsonObject.accumulate("Senha", password);

                json = jsonObject.toString();

                StringEntity se = new StringEntity(json);

                httpPost.setEntity(se);

                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");

                HttpResponse httpResponse = httpclient.execute(httpPost);
                Stream stream = new Stream();
                result += stream.getStringFromInputStream(httpResponse.getEntity().getContent());
            } catch (Exception e) {
                Log.d("Erro", e.getLocalizedMessage());
                baseProgressBar.setVisibility(View.INVISIBLE);
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            baseProgressBar.setVisibility(View.INVISIBLE);
            super.onPostExecute(s);
            String msg = "{  \"message\": \"Usuário já existe\"}"; // mensagem vindo do ws caso já exista um usuario cadastrado com esse nome
            if(s.equals(msg)) {
                Resources resources = getResources();
                Toast.makeText(getApplicationContext(), resources.getString(R.string.usuarioJaExiste), Toast.LENGTH_SHORT).show();
            } else {
                Resources resources = getResources();
                Toast.makeText(getApplicationContext(), resources.getString(R.string.usuarioCadastrado), Toast.LENGTH_SHORT).show();
                startActivity(new Intent(CadastroActivity.this, MainActivity.class));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_cadastro, menu);
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
        }

        return super.onOptionsItemSelected(item);
    }
}
