/*
 * This class ties together the various components
 * to optimize the SVM model via a grid search and
 * store the results.
 */

package financialpredictor.util;

import financialpredictor.Exception.FinancialPredictorException;
import financialpredictor.messagebook.svm.MessageBookSVMDirection;
import financialpredictor.log.LogFactory;
import financialpredictor.log.Logger;
import financialpredictor.messagebook.svm.MessageBookSVM;
import financialpredictor.messagebook.svm.MessageBookSVMFactory;
import java.text.DecimalFormat;
import java.util.ArrayList;
import libsvm.svm;
import libsvm.svm_print_interface;


/**
 *
 * @author enriqueareyan
 */
public class TestGenerator {
    
    
    private int n = 0;
    private int initialTrainNbr     = 100;
    private int initialNbrRows      = 20000;
    private int incrementTrainNbr   = 50;
    private int incrementNbrRows    = 1000;
    private int nbrLoopTrainNbr     = 15;
    private int nbrLoopNbrRows      = 4;
    private int nbrLoopParameters   = 11;
    private int nbrLoopTests        = 5;
    private ArrayList<String> featureSet=new ArrayList<String>();
    private String messageBookCSVFileLocation;
    private String messageBookType;
    private String[] logOptions;
    private String logType;
    
    
    public TestGenerator(int initialTrainNbr, int initialNbrRows , int incrementTrainNbr, int incrementNbrRows,
                        int nbrLoopTrainNbr, int nbrLoopNbrRows, int nbrLoopParameters, int nbrLoopTests,
                        String messageBookCSVFileLocation, String messageBookType, String logType , 
                        String[] logOptions, ArrayList<String> featureSet) throws Exception{
        
        this.initialTrainNbr    = initialTrainNbr;
        this.initialNbrRows     = initialNbrRows;
        this.incrementTrainNbr  = incrementTrainNbr;
        this.incrementNbrRows   = incrementNbrRows;
        this.nbrLoopTrainNbr    = nbrLoopTrainNbr;
        this.nbrLoopNbrRows     = nbrLoopNbrRows;
        this.nbrLoopParameters  = nbrLoopParameters;
        this.nbrLoopTests       = nbrLoopTests;
        this.messageBookCSVFileLocation = messageBookCSVFileLocation;
        this.messageBookType = messageBookType;
        this.logOptions = logOptions;
        this.logType = logType;
        this.featureSet = featureSet;
        
        this.checkParameters();
        
        TestGenerator.setSVMPrintInterface();
        
    }
    public static void setSVMPrintInterface(){
        /*
         * Set Custom print for libsvm
         */
        svm_print_interface your_print_func = new svm_print_interface(){ 
            @Override
            public void print(String s){
                //System.out.print(s);
            }};
	svm.svm_set_print_string_function(your_print_func);           
        
    }
    public final void checkParameters() throws Exception{
        if(this.logType == null){
            throw new FinancialPredictorException("No log type received as a parameter. Add a parameter jdbc.logType to the config file.");
        }
        
    }
    public void printParameters(){
        System.out.println( "/************** Start Parameters ******************/"  +
                            "\nmesageBookCSVFileLocation: " + this.messageBookCSVFileLocation + 
                            "\nmessageBookType: "           + this.messageBookType      +
                            "\ninitialTrainNbr: "           + this.initialTrainNbr      +
                            "\ninitialNbrRows: "            + this.initialNbrRows       +
                            "\nincrementTrainNbr: "         + this.incrementTrainNbr    + 
                            "\nincrementNbrRows: "          + this.incrementNbrRows     +
                            "\nnbrLoopTrainNbr: "           + this.nbrLoopTrainNbr      +
                            "\nnbrLoopNbrRows: "            + this.nbrLoopNbrRows       +
                            "\nnbrLoopParameters: "         + this.nbrLoopParameters    +
                            "\nnbrLoopTests: "              + this.nbrLoopTests         +
                            "\nlogType: "                   + this.logType              + 
                            "\nfeatureSet: "                + this.featureSet);
        for(int i=0;i<this.logOptions.length;i++){
            System.out.println("logOptions"+(i+1)+": "+this.logOptions[i]);
        }
        System.out.println("There will be "+(this.nbrLoopNbrRows*this.nbrLoopParameters*this.nbrLoopTests*this.nbrLoopTrainNbr)+" total test cases");
        System.out.println("/************** End Parameters ******************/");
    }
    public void runTest() throws Exception{
        try{
            double[] res = new double[3];
            /* Specify how are we going to collect results: We have two options: database or text file.*/
            Logger logger = LogFactory.getConcreteLogger(this.logType , this.logOptions);
            /*
            * First loop for the number of Rows to be read from the MessageBook
            */
            for(int h=0;h<this.nbrLoopNbrRows;h++){
                
                System.out.println("Reading "+(this.initialNbrRows + (this.incrementNbrRows*h))+" rows from " + this.messageBookCSVFileLocation);
                /*
                * Second loop is for the number of training rows
                */
                for(int k=0;k<this.nbrLoopTrainNbr;k++){
                    System.out.println("trainNbr = "+(this.initialTrainNbr + (this.incrementTrainNbr*k)));
                    int paramC = -3, paramGamma = -13;
                    /*
                    * Third loop is to change the parameters
                    */
                    for(int x=0;x<this.nbrLoopParameters;x++){
                        System.out.println("Gamma = "+Math.pow(2, paramGamma) + ", C = "+Math.pow(2, paramC));
                        /*
                        * Fourth loop is to test the whole thing a number n of times (usually 10, but the exact value is given as a parameter)
                        */
                        for(int j=0;j<this.nbrLoopTests;j++){
                            System.out.print("\t Run #"+j+": ");
                            
                            /*
                             * Before doing the actual test, let us check if we already 
                             * ran this test and have the result in the database
                             */
                            this.n++;
                            if(!logger.checkIfRowExists(this.n,Math.pow(2, paramGamma),Math.pow(2, paramC),this.initialNbrRows + (this.incrementNbrRows*h), this.initialTrainNbr + (this.incrementTrainNbr*k), j)){
                                /*
                                 * Create a fresh new Message Book each time.
                                 * This way we get truly independent samples.
                                 */
                                MessageBookSVM M = MessageBookSVMFactory.getMessageBookSVM( this.messageBookType,                           /* type of MessageBookSVM */
                                                                                            this.messageBookCSVFileLocation,                 /* location of the CSV file*/
                                                                                            this.initialNbrRows + (this.incrementNbrRows*h) /* maximum nbr of Rows (total rows 400391)*/);
                               /*
                                * Create new SVM model for the message book
                                */
                                MessageBookSVMDirection MessageBookSVMDirection = new MessageBookSVMDirection(
                                                                M,                                                  /* Message Book */
                                                                this.initialTrainNbr + (this.incrementTrainNbr*k),  /* how many training points*/
                                                                Math.pow(2, paramGamma),                            /* gamma parameter*/ 
                                                                Math.pow(2, paramC),                                /* C parameter*/
                                                                this.featureSet                                     /* feature set to include in the model */);
                                MessageBookSVMDirection.createDirectionSVMModel();
                                /* 
                                 *  Test the Book and record the results
                                 */
                                res = MessageBookSVMDirection.predictMessageBook(M);
                                System.out.println("total test:"+(int)res[2] + ", total correct: " + (int)res[0] +", accuracy: "+ new DecimalFormat("#0.0000").format(res[1]) + "%");
                                logger.write(this.n,Math.pow(2, paramGamma),Math.pow(2, paramC),this.initialNbrRows + (this.incrementNbrRows*h), this.initialTrainNbr + (this.incrementTrainNbr*k), j, (int)res[0], res[1]);
                            }else{
                                System.out.println("We already have data for this run");                                
                            }
                        }
                        paramC += 2;
                        paramGamma += 2;
                        System.out.println(new DecimalFormat("#0.00").format((double)n/(double)(this.nbrLoopNbrRows*this.nbrLoopParameters*this.nbrLoopTests*this.nbrLoopTrainNbr)*100) + "% to go ("+this.n+"/"+this.nbrLoopNbrRows*this.nbrLoopParameters*this.nbrLoopTests*this.nbrLoopTrainNbr+")");
                    }
                }
            }
        }catch(FinancialPredictorException e){
            System.out.println("FinancialPredictorException in TestGenerator.runTest: "+e.getMessage());
        }        
    }
    /*
     * Used Mainly for debugging purposes. Here you can test
     * a single set of parameters.
     */
    public static void singleTestOnlyTrades()throws Exception{
        TestGenerator.setSVMPrintInterface();
        System.out.println("***Single test Only Trades***");
        
        double total = 0;
        for(int k=0;k<10;k++){
            /*
            * Create new SVM model for the message book
            */
            //259595 corresponds to 20,000 orders of type 4 or 5
            //369532 corresponds to 30,000 orders of type 4 or 5
            MessageBookSVM OnlyTradesM = MessageBookSVMFactory.getMessageBookSVM( "MessageBookSVMTrades",
                                                                                   "../../Data/LOBSTER_SampleFile_AAPL_2012-06-21_10/AAPL_2012-06-21_34200000_57600000_message_10.csv" /* location of the CSV file*/,
                                                                                        20000);
            //System.out.println("OnlyTrades -Before Training- : \n" + OnlyTradesM);
            ArrayList<String> featureSet = new ArrayList<String>();
            featureSet.add("Time");
            featureSet.add("Type");
            featureSet.add("Order Id");
            MessageBookSVMDirection MessageBookSVMDirection = new MessageBookSVMDirection(
                                            OnlyTradesM     /* Message Book */,
                                            100               /* how many training points*/,
                                            512             /* gamma parameter*/ ,
                                            524288          /* C parameter*/,
                                            featureSet      /* feature set to include in the model */);
            System.out.println("\nTotal sample:"+OnlyTradesM.getNbrEntries() + ", total Training:"+MessageBookSVMDirection.getTrainMessageBook().getNbrEntries());
            //System.out.println("OnlyTrades -After Training- : \n" + OnlyTradesM);
            int[] proportions =  OnlyTradesM.getProportionDirectionPoints();
            System.out.println("Total sell = "+ proportions[0] + ", total buy = "+proportions[1]);
            MessageBookSVMDirection.createDirectionSVMModel();
            int[] proportionsTrain =  MessageBookSVMDirection.getTrainMessageBook().getProportionDirectionPoints();
            System.out.println("Train sell = "+ proportionsTrain[0] + ", Train buy = "+proportionsTrain[1]);
            
            double[] res = new double[2];
            res = MessageBookSVMDirection.predictMessageBook(OnlyTradesM);
            System.out.println("Tested in: "+ (int)res[2] + ", good: " + (int)res[0] +", accuracy: "+ res[1]);
            total += res[1];
        }
        System.out.println("\nAverage for 10 runs:" + new DecimalFormat("#0.0000").format((double)total/(double)10)); 
    }
    
    public static void singleTest() throws Exception{
        TestGenerator.setSVMPrintInterface();
        System.out.println("***Single test All orders***");
        
        double total = 0;
        for(int k=0;k<10;k++){
            
            MessageBookSVM OnlyType1 = MessageBookSVMFactory.getMessageBookSVM( "MessageBookSVMTrades",
                                                                                   "../../Data/LOBSTER_SampleFile_AAPL_2012-06-21_10/AAPL_2012-06-21_34200000_57600000_message_10.csv" /* location of the CSV file*/,
                                                                                        20000);
            //System.out.println(AllOrders);
            //System.out.println("OnlyTrades -Before Training- : \n" + OnlyTradesM);
            ArrayList<String> featureSet = new ArrayList<String>();
            featureSet.add("Time");
            //featureSet.add("Type");
            //featureSet.add("Order Id");
            MessageBookSVMDirection MessageBookSVMDirection = new MessageBookSVMDirection(
                                            OnlyType1   /* Message Book */,
                                            2500        /* how many training points*/,
                                            Math.pow(2,2)         /* gamma parameter*/ ,
                                            Math.pow(2, 13)      /* C parameter*/,
                                            featureSet  /* feature set to include in the model */);
            System.out.println("\nTotal sample:"+OnlyType1.getNbrEntries() + ", total Training:"+MessageBookSVMDirection.getTrainMessageBook().getNbrEntries());
            //System.out.println("OnlyTrades -After Training- : \n" + OnlyTradesM);
            int[] proportions =  OnlyType1.getProportionDirectionPoints();
            System.out.println("Total sell = "+ proportions[0] + ", total buy = "+proportions[1]);
            MessageBookSVMDirection.createDirectionSVMModel();
            int[] proportionsTrain =  MessageBookSVMDirection.getTrainMessageBook().getProportionDirectionPoints();
            System.out.println("Train sell = "+ proportionsTrain[0] + ", Train buy = "+proportionsTrain[1]);
            
            double[] res = new double[2];
            res = MessageBookSVMDirection.predictMessageBook(OnlyType1);
            System.out.println("Tested in: "+ (int)res[2] + ", good: " + (int)res[0] +", accuracy: "+ res[1]);
            total += res[1];
            
            MessageBookSVM allOrders = MessageBookSVMFactory.getMessageBookSVM( "MessageBookSVM",
                                                                                   "../../Data/LOBSTER_SampleFile_AAPL_2012-06-21_10/AAPL_2012-06-21_34200000_57600000_message_10.csv" /* location of the CSV file*/,
                                                                                        20000);
            int totalType4=0,correctType4 = 0;
            int totalType5=0,correctType5 = 0;
            for(int i=0;i<allOrders.getNbrEntries();i++){
                if(allOrders.getEntry(i).getType()==4){
                    if(MessageBookSVMDirection.predictByOrderID(allOrders.getEntry(i).getOrderId()) == allOrders.getEntry(i).getDirection()){
                        correctType4++;
                    }
                    totalType4++;
                }
                if(allOrders.getEntry(i).getType()==5){
                    if(MessageBookSVMDirection.predictByOrderID(allOrders.getEntry(i).getOrderId()) == allOrders.getEntry(i).getDirection()){
                        correctType5++;
                    }
                    totalType5++;
                }
            }
            System.out.println("correctType4 = "+correctType4 + " out of totalType4 = " + totalType4 + ", accuracy:" + new DecimalFormat("#0.0000").format((double)correctType4/(double)totalType4));
            System.out.println("correctType5 = "+correctType5 + " out of totalType5 = " + totalType5 + ", accuracy:" + new DecimalFormat("#0.0000").format((double)correctType5/(double)totalType5));
            
            
        }
        System.out.println("\nAverage for 10 runs:" + new DecimalFormat("#0.0000").format((double)total/(double)10)); 
        
    }
}
