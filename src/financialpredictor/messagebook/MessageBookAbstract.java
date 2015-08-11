/*
 * This class represents a message book.
 * It is composed of MessageBookEntry objects.
 * This is an abstract class. It needs to be extended
 * to be used.
 */
package financialpredictor.messagebook;

import com.opencsv.CSVReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
/**
 *
 * @author enriqueareyan
 */
abstract public class MessageBookAbstract {
        /*
         * Location of the CSV file with the message data
	Columns:
	
	    1.) Time: 		
				Seconds after midnight with decimal 
				precision of at least milliseconds 
				and up to nanoseconds depending on 
				the requested period
	    2.) Type:
				1: Submission of a new limit order
				2: Cancellation (Partial deletion 
				   of a limit order)
				3: Deletion (Total deletion of a limit order)
				4: Execution of a visible limit order			   	 
				5: Execution of a hidden limit order
				7: Trading halt indicator 				   
				   (Detailed information below)
	    3.) Order ID: 	
				Unique order reference number 
				(Assigned in order flow)
	    4.) Size: 		
				Number of shares
	    5.) Price: 		
				Dollar price times 10000 
				(i.e., A stock price of $91.14 is given 
				by 911400)
	    6.) Direction:
				-1: Sell limit order
				1: Buy limit order
				
				Note: 
				Execution of a sell (buy) limit
				order corresponds to a buyer (seller) 
				initiated trade, i.e. Buy (Sell) trade.
										
						
        */
        protected String csvFileLocation;    
        /*
        * An array of objects of type MessageBookEntry which contains all the entries
        * of the MessageBook, up to this.maxNbrRows
        */
        //protected MessageBookEntry[] MessageBook;
        protected ArrayList<MessageBookEntry> MessageBook;
        /*
         * this is a cutoff variable to determine how many rows 
         * to read from the CSV file containing the orderbook
         */
        protected int maxNbrRowsCSVFile;
        /*
         * number of points in the sell and buy direction
         * first position sell, second is buy
         */
        protected int[] proportionDirection;
        /*
         * max min of the price of the message book
         * first position is max, second is min
         */
        protected int[] maxminPrice;
        /*
         * max min of the size of the message book
         * first position is max, second is min
         */
        protected int[] maxminSize;
        /*
         * max min of the time of the message book
         * first position is max, second is min
         */
        protected double[] maxminTime;
        /*
         * max min of the order id of the message book
         * first position is max, second is min
         */
        protected int[] maxminOrderId;
        /*
         * Add an entry to the messagebook
         */
        public void addEntry(MessageBookEntry E, int i){
            this.MessageBook.add(E);
            
        }        
        /*
         * read the CSV file and construct the message book.
         * This construction consists on creating an object of type MessageBookEntry
         * for each entry of the message book.
         */        
        protected void readCSVFile() throws Exception{
            int i = 0;
            //Try to read the CSV file.
            CSVReader reader = new CSVReader(new FileReader(this.csvFileLocation));
            //Get next line
            String [] nextLine;
            //Read as many lines as nbrRows or all of them
            while (((nextLine = reader.readNext()) != null) && i<this.maxNbrRowsCSVFile) {
            //while (((nextLine = reader.readNext()) != null) && this.MessageBook.size()<this.maxNbrRowsCSVFile) {
                /*
                 * Call the function addRawEntry which is suppose to be implemented
                 * by the concrete class.
                 */
                this.addRawEntry(   nextLine[0], 
                                    Byte.parseByte(nextLine[1]), 
                                    Integer.parseInt(nextLine[2]),
                                    Integer.parseInt(nextLine[3]),
                                    Integer.parseInt(nextLine[4]),
                                    Byte.parseByte(nextLine[5]));
                i++;
            }            
            
        }
        /*
         * This function is provided so that the extending class 
         * can create particular entry objects from the raw data.
         * This abstract function is called by readCSVFile().
         */
        abstract protected void addRawEntry(String Time,byte Type, int OrderId,int Size, int Price, byte Direction);
        /*
         * This function first checks if the proportion of points
         * was already computed and returns it if it has.
         * Otherwise, it loops through the message book
         * and computes the proportion of entries for each
         * direction buy (1) and sell (-1), and save it in the object
         * for later use
         */
        public int[] getProportionDirectionPoints(){
            int sell = 0 , buy = 0;
            //Check if the proportions were already calculated.
            if(this.proportionDirection[0]==-1){ 
                for(int i=0;i<this.MessageBook.size();i++){
                    if(this.MessageBook.get(i).getDirection() == -1){
                        sell++;
                    }else{
                        buy++;
                    }
                }
                int[] proportions = new int[2];
                proportions[0] = sell;
                proportions[1] = buy;
                this.proportionDirection = proportions;
            }
            return this.proportionDirection;
        }
        /*
         * Compute the max min price of this message book
         */
        public int[] getMaxMinPrice(){
            //Check if maxmin was already calculated
            if(this.maxminPrice[0] == -1){
                int[] maxmin = new int[2];
                Collections.sort(this.MessageBook , MessageBookComparators.PriceComparator);
                maxmin[0] = this.MessageBook.get(0).getPrice();
                maxmin[1] = this.MessageBook.get(this.MessageBook.size()-1).getPrice();
                this.maxminPrice = maxmin;
            }
            return this.maxminPrice;
            
        }
        /*
         * Compute the max min size of this message book
         */
        public int[] getMaxMinSize(){
            //Check if maxmin was already calculated
            if(this.maxminSize[0] == -1){
                int[] maxmin = new int[2];
                Collections.sort(this.MessageBook, MessageBookComparators.SizeComparator);
                maxmin[0] = this.MessageBook.get(0).getSize();
                maxmin[1] = this.MessageBook.get(this.MessageBook.size()-1).getSize();
                this.maxminSize = maxmin;
            }
            return this.maxminSize;
        }
        /*
         * Compute the max min time of this message book
         */
        public double[] getMaxMinTime(){
            //Check if maxmin was already calculated
            if(this.maxminTime[0] == -1.0){
                double[] maxmin = new double[2];
                Collections.sort(this.MessageBook, MessageBookComparators.TimeComparator);
                maxmin[0] = Double.parseDouble(this.MessageBook.get(0).getTime());
                maxmin[1] = Double.parseDouble(this.MessageBook.get(this.MessageBook.size()-1).getTime());
                this.maxminTime = maxmin;
            }
            return this.maxminTime;
        }
        /*
         * Compute the max min id of this message book
         */
        public int[] getMaxOrderId(){
            //Check if maxmin was already calculated
            if(this.maxminOrderId[0] == -1){
                int[] maxmin = new int[2];
                Collections.sort(this.MessageBook, MessageBookComparators.OrderIdComparator);
                maxmin[0] = this.MessageBook.get(0).getOrderId();
                maxmin[1] = this.MessageBook.get(this.MessageBook.size()-1).getOrderId();
                this.maxminOrderId = maxmin;
            }
            return this.maxminOrderId;
        }        
        /*
         * Get number of entries
         */
        public int getNbrEntries(){
            return this.MessageBook.size();
        }
        /*
         * Get an entry by index
         */
        public MessageBookEntry getEntry(int i){
            return this.MessageBook.get(i);
        }
        /*
         * This function will give you part of the MessageBook.
         * It will give you all the entries that were sell if Direction == -1
         * or it will give you all the entries that were buy if Direction == 1
         */
        public ArrayList<MessageBookEntry> getEntriesByDirection(int Direction) throws Exception{
            if(Direction != 1 && Direction != -1){
                throw new Exception("Direction invalid in getEntriesByDirection");
            }
            ArrayList<MessageBookEntry> MBDirection = new ArrayList<MessageBookEntry>();
            for(int i=0;i<this.MessageBook.size();i++){
                if(this.MessageBook.get(i).getDirection() == Direction){
                    MBDirection.add(this.MessageBook.get(i));
                }
            }
            return MBDirection;
        }
        public int countOrderByType(byte type){
            int counter = 0;
            for(int i=0;i<this.MessageBook.size();i++){
                if(this.MessageBook.get(i).getType() == type){
                    counter++;
                }
            }
            return counter;
        }
    @Override
        public String toString(){
            String ret = "";
            for(int i=0;i<this.MessageBook.size();i++){
                ret += this.MessageBook.get(i) + "\n";
            }
            return ret;
        }    
}