package javali.com.br.listasupermercado.Entity;

import java.io.Serializable;

/**
 * Created by rm71676 on 20/03/2015.
 */
public class ProdutoBD implements Serializable{

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    private int codigo;
    private String nome;

    @Override
    public String toString() {
        return nome.toUpperCase();
    }
}
