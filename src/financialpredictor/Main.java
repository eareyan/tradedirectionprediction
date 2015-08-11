/*
 * Main class to test the Financial Predictor
 * 
 */
package financialpredictor;

import java.io.FileNotFoundException;
import java.io.IOException;
import financialpredictor.util.Utilities;


/**
 *
 * @author enriqueareyan
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException, Exception {
        //Utilities.testRules();
        //Utilities.bulkTestRules();
        //Utilities.miscTest();
        try{
            Utilities.bulkTests(args);
        }catch(FileNotFoundException e){
            System.out.println("FileNotFoundException:" + e.getLocalizedMessage());
        }
    }
}
