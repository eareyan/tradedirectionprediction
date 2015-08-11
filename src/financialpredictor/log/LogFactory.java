/*
 * This class implemnets the factory design pattern.
 * It contains a single static method to produce objects
 * of type Logger.
 */
package financialpredictor.log;

import financialpredictor.Exception.FinancialPredictorException;

/**
 *
 * @author enriqueareyan
 */
public class LogFactory {
    
    public static Logger getConcreteLogger(String logType, String[] Options) throws FinancialPredictorException{
        if(logType.equalsIgnoreCase("MySQLDatabase")){
            return new MySQLDatabase(Options);
        }else if(logType.equalsIgnoreCase("TextFile")){
            return new TextFile(Options);
        }else{
            throw new FinancialPredictorException("logType: "+logType+ ", not supported");
        }
    }
}
