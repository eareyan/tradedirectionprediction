/*
 * Implements all the logic to store and check results
 * on a text file
 */
package financialpredictor.log;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;


/**
 *
 * @author enriqueareyan
 */
public class TextFile implements Logger{
    
    public File file;
    public String fileLocation;
    
    public TextFile(String[] fileLocation){
        this.fileLocation = fileLocation[0];
         try{
            //Specify the file name and path here
            this.file =new File(this.fileLocation);

            /* This logic is to create the file if the
            * file is not already present
            */
            if(!this.file.exists()){
                this.file.createNewFile();
            }    
         }catch(IOException ioe){
            System.out.println("Exception occurred:");
            ioe.printStackTrace();             
         }
    }
    @Override
    public void write(int n, double gamma, double C,int total_rows,int training_rows,int run,int correct_nbr,double correct_prc){
        try{
            String sql =  "INSERT INTO test "
                                  + "VALUES ("+n+","+gamma+","+C+","+total_rows+","+training_rows+","+run+","+correct_nbr+","+correct_prc+");"+ '\n';
            //Here true is to append the content to file
            FileWriter fw = new FileWriter(this.file,true);
            //BufferedWriter writer give better performance
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(sql);
            //Closing BufferedWriter Stream
            bw.close();
        }catch(IOException ioe){
            System.out.println("Exception occurred:");
            ioe.printStackTrace();
        }
   }    
    @Override
    public boolean checkIfRowExists(int n, double gamma, double C,int total_rows,int training_rows,int run){
        
        try{
            int m = TextFile.countLines(this.fileLocation);
            if(n<=m){
               return true;    
            }else{
                return false;
            }
        }catch(Exception e){
            System.out.println(e);
        }
        return false;
    }
    
    
public static int countLines(String filename) throws IOException {
    InputStream is = new BufferedInputStream(new FileInputStream(filename));
    try {
        byte[] c = new byte[1024];
        int count = 0;
        int readChars = 0;
        boolean empty = true;
        while ((readChars = is.read(c)) != -1) {
            empty = false;
            for (int i = 0; i < readChars; ++i) {
                if (c[i] == '\n') {
                    ++count;
                }
            }
        }
        return (count == 0 && !empty) ? 1 : count;
    } finally {
        is.close();
    }
}    
}
