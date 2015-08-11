/*
 * This is the simplest implementation of a MessageBook.
 * This class directly extends the abstract class MessageBookAbstract.
 */
package financialpredictor.messagebook;

import java.util.ArrayList;

/**
 *
 * @author enriqueareyan
 */
public class MessageBook extends MessageBookAbstract{
        /*
         * Base Constructor
         */
        public MessageBook(){
            //This constructor needs to be called by every other constructor.
            /*
             * Initialize the proportion as -1
             */
            this.proportionDirection = new int[2];
            this.proportionDirection[0] = -1;
            this.proportionDirection[1] = -1;
            /*
             * Initialize max min price as -1
             */
            this.maxminPrice = new int[2];
            this.maxminPrice[0] = -1;
            this.maxminPrice[1] = -1;
            /*
             * Initialize max min size as -1
             */
            this.maxminSize = new int[2];
            this.maxminSize[0] = -1;
            this.maxminSize[1] = -1;
            /*
             * Initialize max min time as -1
             */
            this.maxminTime = new double[2];
            this.maxminTime[0] = -1.0;
            this.maxminTime[1] = -1.0;
            /*
             * Initialize max min order id as -1
             */
            this.maxminOrderId = new int[2];
            this.maxminOrderId[0] = -1;
            this.maxminOrderId[1] = -1;
            /*
             * Initialize Message Book
             */
            this.MessageBook = new ArrayList<MessageBookEntry>();
        }
        /*
         * Construct a MessageBook from a csvFileLocation
         */
        public MessageBook(String csvFileLocation, int maxNbrRows) throws Exception{
            //Call main construtor
            this();
            /*
             * Initialize the message book.
             */
            this.csvFileLocation = csvFileLocation;
            this.maxNbrRowsCSVFile = maxNbrRows;
            //Initialize MessageBook from CSV file
            this.readCSVFile();
        }
    @Override
    /*
     * Adds a plain flavored MessageBookEntry
     */
    protected void addRawEntry(String Time,byte Type, int OrderId,int Size, int Price, byte Direction){
        this.MessageBook.add(new MessageBookEntry(Time,Type,OrderId,Size,Price,Direction));
    }
}
