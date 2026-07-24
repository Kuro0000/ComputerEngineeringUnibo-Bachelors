public class Prenotazione {
    private String id;
    private String matricola;
    public Prenotazione(){
        id = "L";
        matricola = "";
    }
    public Prenotazione(String id, String matricola){
        this.id = id;
        this.matricola = matricola;
    }
    public String getId(){
        return id;
    }

    public String getMatricola(){
        return matricola;
    }
    public void prenota(String id, String matricola){
        this.id = id;
        this.matricola = matricola;
    }
}
