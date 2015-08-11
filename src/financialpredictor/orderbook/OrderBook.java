/*
 * This class represents an object of type orderbook.
 * It is composed of OrderBookEntry objects.
 */
package financialpredictor.orderbook;

import com.opencsv.CSVReader;
import java.io.FileReader;
import libsvm.svm_model;

/**
 *
 * @author enriqueareyan
 */
public class OrderBook {
        /*
         * Location of the CSV file with the orderbook data
         * This file has the following format: 
         *  1.) Ask Price 1: 	Level 1 Ask Price 	(Best Ask)
	 *   2.) Ask Size 1: 	Level 1 Ask Volume 	(Best Ask Volume)
	 *   3.) Bid Price 1: 	Level 1 Bid Price 	(Best Bid)
	 *   4.) Bid Size 1: 	Level 1 Bid Volume 	(Best Bid Volume)
	 *   5.) Ask Price 2: 	Level 2 Ask Price 	(2nd Best Ask)
	 *   ...
        */
        private String csvFileLocation;
        /*
        * An array of objects of type OrderBookEntry which contains all the entries
        * of the OrderBook, up to this.maxNbrRows
        */
        private OrderBookEntry[] OrderBook;
        /*
         * deltaT: how many events, defined as rows in the orderbook,
         * to look into the future to determine any metric.
         */
        private int deltaT;
        /*
         * this is a cutoff variable to determine how many rows 
         * to read from the orderbook
         */
        private int maxNbrRows;
        
        /*
         * SVM Model to predict midpoints. This is yet to be used.
         */
        private svm_model model;
        
        
        /*
         * Constructor
         */
        public OrderBook(String csvFileLocation, int deltaT, int maxNbrRows) throws Exception{
            /*
             * Initialize the Order book.
             * Receives the location of the csv File, 
             * deltaT which is the numbers of events to look into the future,
             * nbrRows wich is the maximum number of rows to take from the CSV file, starting from the first row
             */
            this.csvFileLocation = csvFileLocation;
            this.deltaT     = deltaT;
            this.maxNbrRows    = maxNbrRows; 
            this.OrderBook = new OrderBookEntry[maxNbrRows];
            /*
             * Reads the data from the CSV File
             */
            this.readCSVFile();
            
        }

        /*
         * read the CSV file and construct the order book.
         * This construction consists on creating an object of type OrderBookEntry
         * for each entry of the order book.
         */
        private void readCSVFile() throws Exception{
            int i = 0;
            //Try to read the CSV file.
            CSVReader reader = new CSVReader(new FileReader(this.csvFileLocation));
            //Get next line
            String [] nextLine;
            //Placeholder for object of type OrderBookEntry
            OrderBookEntry E;
            //Read as many lines as nbrRows or all of them
            while (((nextLine = reader.readNext()) != null) && i<this.maxNbrRows) {
                /*
                 * Create Object of type OrderBookEntry and added to this.orderbook
                 */
                E = new OrderBookEntry(Integer.parseInt(nextLine[0]),Integer.parseInt(nextLine[1]),Integer.parseInt(nextLine[2]),Integer.parseInt(nextLine[3]));
                this.OrderBook[i] = E;
                i++;
            }            
            
        }
        
        public void labelOrderBookMidPointsBla(){
            /*
             * This function looks at deltaT events (rows) into the future
             * and does two things: 1) computes the midpoint (stored in this.midpoints) and
             * 2) determines the direction of the midpoint (stored in this.labelsMidpoint) 
             */
            int j=0;            
            //Go through orderbook
            for (int i=0;i<this.OrderBook.length; i++) {
                /*
                 * Calculates the Mid-price movement according to deltaT
                 */
                if(i+1>deltaT){ 
                    if(this.OrderBook[i].getMidPoint() > this.OrderBook[j].getMidPoint()){
                        this.OrderBook[j].setLabelMidPoint((byte) 1);
                    }else if(this.OrderBook[i].getMidPoint() < this.OrderBook[j].getMidPoint()){
                        this.OrderBook[j].setLabelMidPoint((byte) -1);
                    }else{
                        this.OrderBook[j].setLabelMidPoint((byte) 0);
                    }
                    j++;
                }
            }            
        }

        public void proportionMidpointLabels(){
            /*
             * This functions loops through the previously computed
             * midpoint labels and counts how many belong to each class
             */
            int upward = 0, downward = 0 , stationary = 0;
            for(int i=0;i<this.OrderBook.length;i++){
                if(this.OrderBook[i].getLabelMidPoint() == 1){
                    upward++;
                }else if(this.OrderBook[i].getLabelMidPoint() == -1){
                    downward++;
                }else{
                    stationary++;
                }
            }
            System.out.println("Data point going upward: \t\t" + upward + " ("+(float)upward /(upward+downward+stationary) +")");
            System.out.println("Data point going downward: \t\t" + downward+ " ("+(float)downward /(upward+downward+stationary) +")");
            System.out.println("Data point going stationary:\t\t" + stationary+ " ("+(float)stationary /(upward+downward+stationary) +")");
            System.out.println("Total: " +(upward+downward+stationary));
        }        
        /*
         * Get an entry by index
         */
        public OrderBookEntry getEntry(int i){
            return this.OrderBook[i];
        }
        
    @Override
        public String toString(){
        System.out.println("ORDERBOOK:\n"
                + "Parameters: deltaT = "+this.deltaT + ", fileLocation:"+this.csvFileLocation
                +"\nLevel k \tBest Ask \tBest Bid \tMidpoint \tMidPoint Label\n");
        for(int i=0;i<this.OrderBook.length;i++){
            System.out.print(i+"\t\t"+  this.OrderBook[i].getBestAsk()          + "\t\t"+ 
                                        this.OrderBook[i].getBestBid()    + "\t\t"+ 
                                        this.OrderBook[i].getMidPoint()         + "\t");
            if(this.OrderBook[i].getLabelMidPoint()==1){
                System.out.println("upward");
            }else if(this.OrderBook[i].getLabelMidPoint() == -1){
                System.out.println("downward");
            }else{
                System.out.println("stationary");
            }
        }
            return "";
        }
}
