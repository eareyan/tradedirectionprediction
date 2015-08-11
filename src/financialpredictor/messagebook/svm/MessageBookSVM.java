/*
 * This is an implementation of a MessageBook
 * to be used for constructing a SVM.
 */
package financialpredictor.messagebook.svm;
import financialpredictor.messagebook.MessageBook;

/**
 *
 * @author enriqueareyan
 */
public class MessageBookSVM extends MessageBook{
        /*
        * Blank Constructor
        */
        public MessageBookSVM(){
            super();
        }
        /*
         * Construct a MessageBook from a csvFileLocation
         */
        public MessageBookSVM(String csvFileLocation, int maxNbrRows) throws Exception{
            //Call parent construtor
            super(csvFileLocation,maxNbrRows);
        }     
        @Override
        /*
         * In this case we want to add a MessageBookEntrySVM.
         * We will include any order in this book.
         */
        protected void addRawEntry(String Time,byte Type, int OrderId,int Size, int Price, byte Direction){            
                this.MessageBook.add(new MessageBookSVMEntry(Time,Type,OrderId,Size,Price,Direction));
        }
        
}
