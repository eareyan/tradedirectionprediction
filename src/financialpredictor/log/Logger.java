/*
 * Interface. Any way to store results
 * should implement this interface.
 */
package financialpredictor.log;
/**
 *
 * @author enriqueareyan
 */
public interface Logger {
    
    public void write(int n, double gamma, double C,int total_rows,int training_rows,int run,int correct_nbr,double correct_prc);
    
    public boolean checkIfRowExists(int n,double gamma, double C,int total_rows,int training_rows,int run);
    
}
