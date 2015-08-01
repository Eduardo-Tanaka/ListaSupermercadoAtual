package javali.com.br.listasupermercado.Entity;

/**
 * Created by rm71676 on 20/03/2015.
 */
public class Produto {

    private int listaId;
    private int quantidade;
    private String nome;
    private int contaId;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public int getCodigo() {
        return listaId;
    }

    public void setCodigo(int codigo) {
        this.listaId = codigo;
    }

    @Override
    public String toString() {
        return (nome + "    -    ").toUpperCase() + quantidade;
    }

    public int getContaId() {
        return contaId;
    }

    public void setContaId(int contaId) {
        this.contaId = contaId;
    }
}
