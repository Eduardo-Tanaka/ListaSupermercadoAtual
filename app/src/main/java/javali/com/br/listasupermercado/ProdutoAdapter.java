package javali.com.br.listasupermercado;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import javali.com.br.listasupermercado.Entity.Produto;


/**
 * Created by rm71676 on 20/03/2015.
 */
public class ProdutoAdapter extends ArrayAdapter<Produto> {

    private int resource;
    private LayoutInflater inflater;
    private Context context;
    private static List<Produto> listaP;
    private int id;

    public ProdutoAdapter(Context ctx, int resourceId, List<Produto> objects) {
        super( ctx, resourceId, objects );
        resource = resourceId;
        inflater = LayoutInflater.from( ctx );
        context = ctx;
    }

    @Override
    public View getView ( final int position, View convertView, ViewGroup parent ) {

        Resources resources = getContext().getResources();
        convertView = (RelativeLayout) inflater.inflate( resource, null );
        final Produto produto = getItem( position );
        final TextView name = (TextView) convertView.findViewById(R.id.txtProduto);
        name.setText(produto.toString());

        // Alerta
        final AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
        builder1.setMessage(resources.getString(R.string.msg));
        builder1.setCancelable(true);
        builder1.setPositiveButton(resources.getString(R.string.sim),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        id = produto.getCodigo();
                        new DeleteListaWS().execute(id+"");
                        Intent intent = new Intent(context.getApplicationContext(), ListaActivity.class);
                        int c = produto.getContaId();
                        intent.putExtra("ContaId", c);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(intent);
                    }
                });
        builder1.setNegativeButton(resources.getString(R.string.nao),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(produto.getQuantidade() <= 0){
                            produto.setQuantidade(1);
                            name.setText(produto.toString());
                            id = produto.getCodigo();
                            new UpdateListaWS(produto).execute();
                        }
                        dialog.cancel();
                    }
                });


        ImageButton btnMais = (ImageButton) convertView.findViewById(R.id.btnMais);

        btnMais.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                id = produto.getCodigo();
                new UpdateListaWS(produto).execute();
                produto.setQuantidade(produto.getQuantidade()+1);
                name.setText(produto.toString());
            }
        });

        ImageButton btnMenos = (ImageButton)convertView.findViewById(R.id.btnMenos);

        btnMenos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                produto.setQuantidade(produto.getQuantidade()-1);
                name.setText(produto.toString());
                if(produto.getQuantidade() <= 0) {
                    AlertDialog alert2 = builder1.create();
                    alert2.show();
                }
                id = produto.getCodigo();
                new UpdateListaWS(produto).execute();
            }
        });

        Button btnExcluir = (Button)convertView.findViewById(R.id.btnExcluir);

        btnExcluir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alert11 = builder1.create();
                alert11.show();
            }
        });

        return convertView;
    }

    private class DeleteListaWS extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            String result = "";
            String url = "http://listasupermercadows.apphb.com/api/listaws/" + params[0];
            HttpClient httpClient = new DefaultHttpClient();
            try {
                HttpResponse response = httpClient.execute(new HttpDelete(url));
            } catch (IOException e) {
                Log.e("TAG_ASYNC_TASK", e.getMessage());
            }
            return result;
        }
    }

    private class UpdateListaWS extends AsyncTask<String, Integer, String> {

        private Produto produto;

        public UpdateListaWS(Produto produto) {
            this.produto = produto;
        }

        @Override
        protected String doInBackground(String... params) {
            String result = "";
            String url = "http://listasupermercadows.apphb.com/api/listaws/" + id;
            HttpClient httpClient = new DefaultHttpClient();
            try {
                HttpPut httpPut = new HttpPut(url);

                String json = "";

                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("codigo", produto.getCodigo());
                jsonObject.accumulate("quantidade", produto.getQuantidade());
                jsonObject.accumulate("nome", produto.getNome());
                jsonObject.accumulate("contaId", produto.getContaId());

                json = jsonObject.toString();

                StringEntity se = new StringEntity(json);

                httpPut.setEntity(se);

                httpPut.setHeader("Accept", "application/json");
                httpPut.setHeader("Content-type", "application/json");

                HttpResponse response = httpClient.execute(httpPut);
            } catch (IOException e) {
                Log.e("TAG_ASYNC_TASK", e.getMessage());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return result;
        }
    }
}
