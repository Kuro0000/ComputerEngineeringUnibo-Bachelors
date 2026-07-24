import java.io.Serializable;

public class Veicolo implements Serializable{
    private String targa;
    private String patente;
    private String tipo;
    private String img;
    public Veicolo(String targa, String patente, String tipo, String img){
        if(patente.length() != 5 || targa==null || (!tipo.equals("camper") && !tipo.equals("auto")) || !img.endsWith("_img"))
            throw new IllegalArgumentException("errore degli argomenti");
        try{
            Integer.parseInt(patente);
        }catch(NumberFormatException e){
             throw new IllegalArgumentException("errore degli argomenti"); 
        }
        this.targa = targa;
        this.patente = patente;
        this.tipo = tipo;
        this.img = tipo;
    }
    public String getTarga(){
        return targa;
    }
    public String getPatente(){
        return patente;
    }
        public String getTipo(){
        return tipo;
    }
        public String getImg(){
        return img;
    }
}
