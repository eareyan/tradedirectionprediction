/*
 * This class implemnets the factory design pattern.
 * It contains a single static method to produce objects
 * of type MessageBookSVM.
 */
package financialpredictor.messagebook.svm;

import financialpredictor.Exception.FinancialPredictorException;

/**
 *
 * @author enriqueareyan
 */
public class MessageBookSVMFactory {
    
    public static MessageBookSVM getMessageBookSVM(String Type, String csvFileLocation,int nbrRows) throws FinancialPredictorException, Exception{    
        if(Type.equalsIgnoreCase("MessageBookSVM")){
            return new MessageBookSVM(csvFileLocation,nbrRows);
        }else if(Type.equalsIgnoreCase("MessageBookSVMTrades")){
            return new MessageBookSVMTrades(csvFileLocation,nbrRows);
        }else{
            throw new FinancialPredictorException("MessageBookSVM of type: "+Type+ ", not supported");
        }
    }
    
}
