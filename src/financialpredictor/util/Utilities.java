/*
 * This is a utility class to run the tests.
 * This class contains only static methods.
 */
package financialpredictor.util;

import com.mysql.jdbc.PreparedStatement;
import financialpredictor.Exception.FinancialPredictorException;
import financialpredictor.messagebook.MessageBook;
import financialpredictor.messagebook.MessageBookRules;
import financialpredictor.messagebook.svm.MessageBookSVM;
import financialpredictor.messagebook.svm.MessageBookSVMDirection;
import financialpredictor.messagebook.svm.MessageBookSVMFactory;
import financialpredictor.orderbook.OrderBook;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

/**
 *
 * @author enriqueareyan
 */
public class Utilities {
    public static void analyzeMessageBook(MessageBook M) throws FinancialPredictorException, Exception{
        /*
         * Loop through all the entries to find out if
         * any order of type 1 share ID 
         */
        for(int i=0;i<M.getNbrEntries();i++){
            System.out.println(i);
            /*
             * Only if the entry is of type 1, loop from beginning
             */
            if(M.getEntry(i).getType() == 1){
                for(int j=0;j<M.getNbrEntries();j++){
                    /*
                     * Only if the entry is of type 1, compare if they have the same ID
                     */
                    if(M.getEntry(j).getType() == 1){
                        /*
                         * Obsiously a given entry shara its same id. Check also that these 
                         * are orders in differnet places (i not equal to j)
                         */
                        if((M.getEntry(i).getOrderId() == M.getEntry(j).getOrderId()) && i!=j){
                            System.out.println("This repeat:"+i+"-"+j);
                        }
                    }
                }
            }
        }
        
    }
    /*
     * This function test all the rules in increment number of orders
     * and stores the results in the local database results_rules
     */
    public static void bulkTestRules() throws Exception{
        int deltaT = 5;
        int[] ret = new int[2];
        
        String[] database_options = new String[6];
        database_options[0]= "localhost";
        database_options[1]= "8889";
        database_options[2]= "financialpred";
        database_options[3]= "results_rules";
        database_options[4]= "root";
        database_options[5]= "root";
        financialpredictor.log.MySQLDatabase Database = new financialpredictor.log.MySQLDatabase(database_options);
        
        for(int i=1000;i<21000;i=i+1000){
            String csvOrderBookFileLocation = "../../Data/LOBSTER_SampleFile_AAPL_2012-06-21_10/AAPL_2012-06-21_34200000_57600000_orderbook_10.csv";
            String csvMessageBookFileLocation = "../../Data/LOBSTER_SampleFile_AAPL_2012-06-21_10/AAPL_2012-06-21_34200000_57600000_message_10.csv";

            OrderBook O = new OrderBook(csvOrderBookFileLocation, 0, i);
            MessageBookSVM M = MessageBookSVMFactory.getMessageBookSVM( "MessageBookSVM",       /* type of MessageBookSVM */
                                                                        csvMessageBookFileLocation, /* location of the CSV file*/
                                                                        i                           /* maximum nbr of Rows (total rows 400391)*/);
            try {            
                String sql = "INSERT INTO results_rules (total_orders,total_trades,tick,quote,LR,EMO,Decile) VALUES (?,?,?,?,?,?,?)";
                PreparedStatement preparedStatement = (PreparedStatement) Database.getConnection().prepareStatement(sql);
                ret = MessageBookRules.TickRuleToEntireMessageBook(M);
                preparedStatement.setInt(1, i);
                preparedStatement.setInt(2, ret[0]);
                preparedStatement.setInt(3, ret[1]);
                ret = MessageBookRules.QuoteRuleToEntireMessageBook(M, O, deltaT);
                preparedStatement.setInt(4, ret[1]);
                ret = MessageBookRules.LR_Rule(M, O, deltaT);
                preparedStatement.setInt(5, ret[1]);
                ret = MessageBookRules.EMO_Rule(M, O, deltaT);
                preparedStatement.setInt(6, ret[1]);
                ret = MessageBookRules.Decile_Rule(M, O, deltaT);
                preparedStatement.setInt(7, ret[1]);
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
    public static void testRules() throws Exception{
        String csvOrderBookFileLocation = "../../Data/LOBSTER_SampleFile_AAPL_2012-06-21_10/AAPL_2012-06-21_34200000_57600000_orderbook_10.csv";
        int maxNbrRows = 5000;
        int deltaT = 5;
        OrderBook O = new OrderBook(csvOrderBookFileLocation, 0, maxNbrRows);
        /*
         * Next line only for debugging purposes
         */
        //System.out.println(O);
        String csvMessageBookFileLocation = "../../Data/LOBSTER_SampleFile_AAPL_2012-06-21_10/AAPL_2012-06-21_34200000_57600000_message_10.csv";
        MessageBookSVM M = MessageBookSVMFactory.getMessageBookSVM( "MessageBookSVM",   /* type of MessageBookSVM */
                                                                                            csvMessageBookFileLocation, /* location of the CSV file*/
                                                                                            maxNbrRows /* maximum nbr of Rows (total rows 400391)*/);        
        
        System.out.println("Sample Size: "+maxNbrRows);
        int[] ret = new int[2];
        ret = MessageBookRules.TickRuleToEntireMessageBook(M);
        System.out.println("***************** Tick Rule *******************");        
        System.out.println("Total = "+ ret[0]);
        System.out.println("Correct = " + ret[1]);
        System.out.printf("Correct prc = %.2f\n", ((float)ret[1]/ret[0])*100);
        
        ret = MessageBookRules.QuoteRuleToEntireMessageBook(M, O, deltaT);
        System.out.println("***************** Quote Rule *******************");        
        System.out.println("Total = "+ ret[0]);
        System.out.println("Correct = " + ret[1]);
        System.out.printf("Correct prc = %.2f\n", ((float)ret[1]/ret[0])*100);
        
        System.out.println("***************** LR_Rule *******************");        
        ret = MessageBookRules.LR_Rule(M, O, deltaT);
        System.out.println("Total = "+ ret[0]);
        System.out.println("Correct = " + ret[1]);
        System.out.printf("Correct prc = %.2f\n", ((float)ret[1]/ret[0])*100);
        System.out.println("***************** EMO_Rule *******************");        
        ret = MessageBookRules.EMO_Rule(M, O, deltaT);
        System.out.println("Total = "+ ret[0]);
        System.out.println("Correct = " + ret[1]);
        System.out.printf("Correct prc = %.2f\n", ((float)ret[1]/ret[0])*100);
        System.out.println("****************** Decile_Rule ******************");        
        ret= MessageBookRules.Decile_Rule(M, O, deltaT);        
        System.out.println("Total = "+ ret[0]);
        System.out.println("Correct = " + ret[1]);
        System.out.printf("Correct prc = %.2f\n", ((float)ret[1]/ret[0])*100);
    }
    public static void bulkTests(String[] args)throws FileNotFoundException, IOException, Exception {
        /* First, we need to receive the location of the configuration file by command line */
        if(args.length != 1){
            throw new FinancialPredictorException("Need the configuration file as the first parameter on the command line");
        }
        String configurationFileLocation = args[0];
        /* Read the parameters from the configuracion file */   
        Properties props = new Properties();
        FileInputStream fis = new FileInputStream(configurationFileLocation);
        props.load(fis);
        /* Prepare Feature Set */
        ArrayList<String> featureSet=new ArrayList<String>();
        for(int i=1;i<4;i++){
            if(props.getProperty("jdbc.feature"+i) != null){
                featureSet.add(props.getProperty("jdbc.feature"+i));
            }
        }
        /* Prepare log Options*/
        int i=1;
        while(props.getProperty("jdbc.logOptions"+i) != null){
            i++;
        }
        String[] logOptions = new String[(i-1)];
        for(int j=0;j<(i-1);j++){
            logOptions[j] = props.getProperty("jdbc.logOptions"+(j+1));
        }
        try{
            /*Prepare Test*/
            TestGenerator G = new TestGenerator(Integer.parseInt(props.getProperty("jdbc.initialTrainNbr")), 
                                                Integer.parseInt(props.getProperty("jdbc.initialNbrRows")), 
                                                Integer.parseInt(props.getProperty("jdbc.incrementTrainNbr")), 
                                                Integer.parseInt(props.getProperty("jdbc.incrementNbrRows")),
                                                Integer.parseInt(props.getProperty("jdbc.nbrLoopTrainNbr")),
                                                Integer.parseInt(props.getProperty("jdbc.nbrLoopNbrRows")),
                                                Integer.parseInt(props.getProperty("jdbc.nbrLoopParameters")),
                                                Integer.parseInt(props.getProperty("jdbc.nbrLoopTests")),
                                                props.getProperty("jdbc.messageBookCSVFileLocation"), 
                                                props.getProperty("jdbc.messageBookType"), 
                                                props.getProperty("jdbc.logType") , 
                                                logOptions,
                                                featureSet
                                                );        
            G.printParameters();
            /*Run the Test! */
            G.runTest();
        }catch(FinancialPredictorException e){
            System.out.println("FinancialPredictorException in main: "+e.getMessage());
        }
    }    
    public static void miscTest() throws Exception{
        /*MessageBookSVM M = new MessageBookSVM("../../Data/LOBSTER_SampleFile_AAPL_2012-06-21_10/AAPL_2012-06-21_34200000_57600000_message_10.csv",10);
        System.out.println(M);        
        MessageBookSVMDirection MSVM = new MessageBookSVMDirection(M,9,2.0,2.0,new ArrayList<String>());
        System.out.println(M);
        System.out.println("There are "+MSVM.getTrainMessageBook().getNbrEntries()+ " training rows \n"+MSVM.getTrainMessageBook());
        int[] xxx = MSVM.getTrainMessageBook().getProportionDirectionPoints();
        int[] yyy = M.getProportionDirectionPoints();
        System.out.println("Original Buy:"+yyy[1]+", Sell:"+yyy[0]);
        System.out.println("Buy: "+xxx[1]+", Sell:"+xxx[0]);
        */
        
        String csvMessageBookFileLocation = "../../Data/LOBSTER_SampleFile_AAPL_2012-06-21_10/AAPL_2012-06-21_34200000_57600000_message_10.csv";
        
        MessageBookSVM M2 = MessageBookSVMFactory.getMessageBookSVM( "MessageBookSVM",   /* type of MessageBookSVM */
                                                 csvMessageBookFileLocation, /* location of the CSV file*/
                                                 400391 /* maximum nbr of Rows (total rows 400391)*/);
        printOneFamilyOrder(M2, 16675969);
        //analyzeMessageBook(M2);
        //System.out.println(MessageBookRules.getPreviousIndex(M2, 530, 15));        
    }
    /*
     * This helper function prints all the order from the messagebook
     * with given orderID
     */
    public static void printOneFamilyOrder(MessageBookSVM M,int orderID){
        for(int i=0;i<M.getNbrEntries();i++){
            if(M.getEntry(i).getOrderId() == orderID){
                System.out.println(M.getEntry(i));
            }
        }
    }
    
}
