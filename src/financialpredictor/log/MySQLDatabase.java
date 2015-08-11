/*
 * Implements all the logic to store and check results
 * on a MySQL database
 */
package financialpredictor.log;

import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.ResultSet;
import financialpredictor.Exception.FinancialPredictorException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author enriqueareyan
 */
public class MySQLDatabase implements Logger{
    private Connection conn = null;
    private String servername;
    private int port;
    private String dbname;
    private String tablename;
    private String user;
    private String password;
    /*
     * The constructor received a string of length 2
     * with the connect string in the first position
     * and the name of the table in the second
     */
    public MySQLDatabase(String[] options) throws FinancialPredictorException{
        if(options.length!=6){
            throw new FinancialPredictorException("The logger of type MySQLDatabase must receive an array of lenght 6 as a parameter");
        }
        this.servername = options[0];
        this.port       = Integer.parseInt(options[1]);
        this.dbname     = options[2];
        this.tablename  = options[3];
        this.user       = options[4];
        this.password   = options[5];
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            try {
                conn = DriverManager.getConnection("jdbc:mysql://"+this.servername+":"+this.port+"/"+this.dbname+"?user="+this.user+"&password="+this.password);
            } catch (SQLException ex) {
                // handle any errors
                System.out.println("SQLException: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("VendorError: " + ex.getErrorCode());
                System.exit(1);
            }                
        } catch (Exception ex) {
            // handle the error
            System.out.println(ex);
            System.exit(1);
        }
        /*
         * Check if table this.tableName exists.
         * If it does not exists, create it.
         */
        try{
            String sql = "SELECT * FROM information_schema.tables WHERE table_schema =  ? AND table_name =  ? LIMIT 1;";
            PreparedStatement preparedStatement = (PreparedStatement) conn.prepareStatement(sql);
            preparedStatement.setString(1, this.dbname);
            preparedStatement.setString(2, this.tablename);
            ResultSet rs = (ResultSet) preparedStatement.executeQuery();
            if(!rs.next()){
                System.out.println("flag");
                /*Table does not exists, create it */
                sql = "CREATE TABLE "+this.tablename+" (`id` int(11) NOT NULL AUTO_INCREMENT, `gamma` float NOT NULL, `C` float NOT NULL, `total_rows` int(11) NOT NULL, `training_rows` int(11) NOT NULL, `run` int(11) NOT NULL, `correct_nbr` int(11) NOT NULL, `correct_prc` float NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB  DEFAULT CHARSET=latin1";
                preparedStatement = (PreparedStatement) conn.prepareStatement(sql);
                preparedStatement.executeUpdate();
            }
        }catch(SQLException ex){
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());  
            System.exit(1);
        }
        
    }
    public Connection getConnection(){
        return this.conn;
    }
    @Override
    public boolean checkIfRowExists(int n, double gamma, double C,int total_rows,int training_rows,int run){
        try{
            String sql = "SELECT id FROM "+this.tablename+" WHERE gamma = ? AND C = ? AND total_rows = ? AND training_rows = ? AND run = ?";
            PreparedStatement preparedStatement = (PreparedStatement) conn.prepareStatement(sql);
            preparedStatement.setDouble(1, gamma);
            preparedStatement.setDouble(2, C);
            preparedStatement.setInt(3, total_rows);
            preparedStatement.setInt(4, training_rows);
            preparedStatement.setInt(5, run);
            ResultSet rs = (ResultSet) preparedStatement.executeQuery();
            return rs.next();
        }catch(SQLException ex){
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());  
            System.exit(1);
        }
        return false;
    }

    @Override
    public void write(int n,double gamma, double C,int total_rows,int training_rows,int run,int correct_nbr,double correct_prc) {
        try {
            String sql = "INSERT INTO "+this.tablename+" (gamma,C,total_rows,training_rows,run,correct_nbr,correct_prc) VALUES (?,?,?,?,?,?,?)";
            PreparedStatement preparedStatement = (PreparedStatement) conn.prepareStatement(sql);
            preparedStatement.setDouble(1, gamma);
            preparedStatement.setDouble(2, C);
            preparedStatement.setInt(3, total_rows);
            preparedStatement.setInt(4, training_rows);
            preparedStatement.setInt(5, run);
            preparedStatement.setInt(6, correct_nbr);
            preparedStatement.setDouble(7, correct_prc);
            preparedStatement.execute();
        } catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            System.exit(1);
        }            
    }        
}    
