public class Registro {
    private static int N = 10;
    private Studente[] studenti ;
    private int count;
    public Registro(){
        studenti = new Studente[N];
        count = 0;
    }
    //synchronized per via di una risorsa condivisa in scrittura
    public synchronized boolean iscriviStudente(String nome, String cognome, String matricola){
        if(nome==null || nome.trim().isEmpty() || cognome == null || cognome.trim().isEmpty() || matricola==null || matricola.trim().isEmpty() || count==N){
            return false;
        }
        studenti[count] = new Studente(matricola, nome, cognome);
        count++;
        return true;
    }
    public synchronized boolean caricaVoto(String matricola, int voto){
        if(voto<=0 || voto>33 || matricola == null|| matricola.isEmpty()){
            return false;
        }
       for(int i = 0;i<N && studenti[i]!=null;i++){
            if(studenti[i].getMatricola().equals(matricola)){
                studenti[i].setVoto(voto);
                return true;
            }
       }
       return false;
    }

}
