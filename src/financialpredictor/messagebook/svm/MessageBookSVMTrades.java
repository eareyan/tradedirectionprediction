/*
 * This is an implementation of a MessageBook
 * to be used for constructing a SVM.
 * This class only consider orders of type 4 or 5.
 */
package financialpredictor.messagebook.svm;

/**
 *
 * @author enriqueareyan
 */
public class MessageBookSVMTrades extends MessageBookSVM{
    
    public MessageBookSVMTrades(String csvFileLocation, int maxNbrTrades) throws Exception{
        //Call parent construtor
        super(csvFileLocation,maxNbrTrades);
    }
    @Override
    /*
     * In this case we want to add a MessageBookEntrySVM.
     * We only add a Trade if it is of Type 4 or 5.
     */
    protected void addRawEntry(String Time,byte Type, int OrderId,int Size, int Price, byte Direction){  
            if( Type== 4 || Type ==5){
                this.MessageBook.add(new MessageBookSVMEntry(Time,Type,OrderId,Size,Price,Direction));
            }
    }    
}
