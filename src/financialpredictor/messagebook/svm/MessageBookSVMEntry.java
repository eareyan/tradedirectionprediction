/*
 * This is a particular type of MessageBook entry
 * to be used in conjunction with MessageBookSVM.
 * In particular, this class records whether or not
 * the entry was used to train the model.
 */
package financialpredictor.messagebook.svm;
import financialpredictor.messagebook.MessageBookEntry;

/**
 *
 * @author enriqueareyan
 */
public class MessageBookSVMEntry extends MessageBookEntry{
    
        /*
        *   This boolean keeps track of wheter or not 
        *   this entry was used to train the SVM model
        */
        private boolean train = false;
    
        public MessageBookSVMEntry(String Time,byte Type, int OrderId,int Size, int Price, byte Direction){
            
            super(Time,Type,OrderId,Size, Price, Direction);
        }
        /*
         * Returns a boolean to indicate whether or not this entry was
         * used for training the SVM model
         */
        public boolean isTrain(){
            return this.train;
        }
        /*
         * Sets true/false this entry was used for training the SVM model
         */
        public void setTrain(boolean train){
            this.train = train;
        }
    @Override
    public String toString(){
        String ret = super.toString();
        if(this.isTrain()){
            ret += "\t\t Used for training";
        }else{
            ret += "\t\t Not used for training";
        }        
        return ret;
    }        
}