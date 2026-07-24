public class Studente {
    private String matricola;
    private String nome;
    private String cognome;
    private int voto;
    public Studente(String matricola, String nome, String cognome){
        this.matricola = matricola;
        this.nome = nome;
        this.cognome = cognome;
        voto = -1;
    }
    public String getNome(){
        return nome;
    }
    public String getCognome(){
        return cognome;
    }
    public String getMatricola(){
        return matricola;
    }
    public int voto(){
        return voto;
    }
    public void setVoto(int voto){
        this.voto = voto;
    }
}
