/*
 * This class implements all the extant trade classification rules
 * at the time of its creation. The main purpose is to compare these
 * rules to the SVM. The rules are: Tick, Quote, Lee and Ready (LR), EMO 
 * and Decile.
 */
package financialpredictor.messagebook;

import financialpredictor.Exception.FinancialPredictorException;
import financialpredictor.messagebook.svm.MessageBookSVM;
import financialpredictor.orderbook.OrderBook;

/**
 *
 * @author enriqueareyan
 */
public class MessageBookRules {
    private static int buyDirectionBuyerInitiatedTrade = -1;
    private static int sellDirectionSellerInitiatedTrade = 1;
    
    public static int[] TickRuleToEntireMessageBook(MessageBookSVM MessageBook) throws Exception{
        int correct = 0, total = 0;
        for(int i=1; i<MessageBook.getNbrEntries();i++){
            if(MessageBook.getEntry(i).getType() == 4 || MessageBook.getEntry(i).getType() == 5){
                    total++;
                    if(TickRuleToOrder(MessageBook,i) == MessageBook.getEntry(i).getDirection()){
                        correct++;
                    }
            }
        }
        int[] ret = new int[2];
        ret[0] = total;
        ret[1] = correct;
        return ret;
    }
    public static int TickRuleToOrder(MessageBookAbstract M, int i) throws Exception{
        /*
         * According to the tick rule, a trade is classified as buy (sell) if it is executed
         * at a price higher (lower) than that of the previous trade.
         * A trade executed at the same price as the previous trade is a zero tick trade.
         */
            if(M.getEntry(i-1).getPrice() < M.getEntry(i).getPrice()){
                return buyDirectionBuyerInitiatedTrade; // buy
            }else if(M.getEntry(i-1).getPrice() > M.getEntry(i).getPrice()) {
                return sellDirectionSellerInitiatedTrade;// sell
            }else{                
                return zeroTick(M,i);
            }
    }
    public static int zeroTick(MessageBookAbstract M, int i) throws Exception{
        /* In this case, the closest previous price that differs from the current trade pice is used 
         * to identify whether the trade is a zero-uptick or zero-downtick trade.
         * A zero-uptick (downtick) trade is classified as a buy (sell).
         */
        for(int j=i-2;j>=0;j--){
            if(M.getEntry(j).getPrice() < M.getEntry(i).getPrice()){
            /* zero-uptick */
                return buyDirectionBuyerInitiatedTrade; //buy
            }else if(M.getEntry(j).getPrice() > M.getEntry(i).getPrice()){
            /* zero-downtick */
                return sellDirectionSellerInitiatedTrade;//sell
            }
        }
        throw new FinancialPredictorException("Tick test failed");
    }
    
    public static int[] QuoteRuleToEntireMessageBook(MessageBookAbstract M,OrderBook O,int deltaT){
        int correct = 0,total = 0;
        for(int i =0;i<M.getNbrEntries();i++){
            /*
             * Quote rule applies only to trades of type 4 (Execution of a visible limit order)
             * or type 5 (Execution of a hidden limit order)
             */
            if(M.getEntry(i).getType()==4 || M.getEntry(i).getType()==5){
                total++;
                if(M.getEntry(i).getDirection() == QuoteRuleToOrder(M,O,i,deltaT)){
                    correct++;
                }
            }
        }
        int[] ret = new int[2];
        ret[0] = total;
        ret[1] = correct;
        return ret;
    }
    public static int QuoteRuleToOrder(MessageBookAbstract M,OrderBook O,int i,int deltaT){
        /*
         * The quote rule determines trade direction by examining the location of the transaction
         * price relative to the quote midpoints. If the transaction price is higher (lower) than the
         * midpoint, a trade will be classified as a buy (sell). The disadvantage of the quote rule is
         * that it cannot classify trades at the midpoints.
         */
        if(M.getEntry(i).getPrice() > O.getEntry(getPreviousIndex(M,i,deltaT)).getMidPoint()){
            return buyDirectionBuyerInitiatedTrade;
        }else if(M.getEntry(i).getPrice() < O.getEntry(getPreviousIndex(M,i,deltaT)).getMidPoint()){
            return sellDirectionSellerInitiatedTrade;
        }else{
            return 0;
        }        
    }
    /*
     * This function receives a MessageBook and index of an entry in the message book
     * and looks for the index of closest entry in the same MessageBook that
     * is deltaT seconds in the past. It returns the index of such entry.
     */
    public static int getPreviousIndex(MessageBookAbstract M,int index, int deltaT){
        double pastTime = Double.parseDouble(M.getEntry(index).getTime()) - deltaT;
        for(int i=0;i<M.getNbrEntries();i++){
            if(Double.parseDouble(M.getEntry(i).getTime()) >= pastTime){
                return i;
            }
        }
        return 0;
        
    }
    
    public static int[] LR_Rule(MessageBookAbstract M,OrderBook O,int deltaT) throws Exception{
        /*
         * The LR rule uses the tick rule to classifiy trades at the midpoint and uses the quote
         * rule elsewhere. 
         */
        int correct = 0,total = 0;
        for(int i =0;i<M.getNbrEntries();i++){
            if(M.getEntry(i).getType() == 4 || M.getEntry(i).getType() == 5){
                total++;
                if(M.getEntry(i).getPrice() == O.getEntry(i).getMidPoint()){
                    if(M.getEntry(i).getDirection() == TickRuleToOrder(M,i)){
                        correct++;
                    }
                }else{
                    if(M.getEntry(i).getDirection() == QuoteRuleToOrder(M,O,i,deltaT)){
                        correct++;
                    }
                }
            }
        }
        int[] ret = new int[2];
        ret[0] = total;
        ret[1] = correct;
        return ret;                
    }
    
    public static int[] EMO_Rule(MessageBookAbstract M, OrderBook O,int deltaT) throws Exception{
        /*
         * The EMO rule usus the tick rule to classify all trades except trades at the ask and
         * bid at which points the quote rule is applied.
         */
        int correct = 0,total=0;
        for(int i=0; i< M.getNbrEntries();i++){
            if(M.getEntry(i).getType() == 4 || M.getEntry(i).getType() == 5){
                total++;            
                if(M.getEntry(i).getPrice() == O.getEntry(i).getBestAsk() || M.getEntry(i).getPrice() == O.getEntry(i).getBestBid()){
                    if(M.getEntry(i).getDirection() == QuoteRuleToOrder(M,O,i,deltaT)){
                        correct++;
                    }
                }else{
                    if(M.getEntry(i).getDirection() == TickRuleToOrder(M,i)){
                        correct++;
                    }                
                }
            }
        }
        int[] ret = new int[2];
        ret[0] = total;
        ret[1] = correct;
        return ret;        
    }
    public static int[] Decile_Rule(MessageBookAbstract M, OrderBook O,int deltaT) throws Exception{
        /*
         * The DecilAlgorithm by Chakrabarty, Li, Nguyen and Van Ness, consists
         * of dividing the spread into deciles and applying etiehr the Tick or Quote
         * rule in each decile.
         */
        int[] deciles = new int[11];
        int decileLocation;
        int correct = 0,total = 0;
        for(int i =0;i<M.getNbrEntries();i++){
            if(M.getEntry(i).getType() == 4 || M.getEntry(i).getType() == 5){
                total++;            
            
                deciles = MessageBookRules.getDeciles(O.getEntry(i).getBestBid(), O.getEntry(i).getBestAsk());
                decileLocation = MessageBookRules.determineDecile(M.getEntry(i).getPrice(), deciles);
                switch(decileLocation){
                    case -1: case 3: case 4: case 5: case 6:                    
                        if(MessageBookRules.TickRuleToOrder(M, i)  == M.getEntry(i).getDirection()){
                            correct++;
                        }
                        break;
                    case 0: case 1: case 2: case 7: case 8: case 9: case 10:
                        if(MessageBookRules.QuoteRuleToOrder(M, O, i,deltaT) == M.getEntry(i).getDirection()){
                            correct++;
                        }
                        break;
                }
            }
        }
        int[] ret = new int[2];
        ret[0] = total;
        ret[1] = correct;
        return ret;        
    }
    public static int[] getDeciles(int BestBid, int BestAsk){
        /*
         * Given a BestBid and Best Ask, this funciton returns an array with
         * 11 values where position 0 represents the BestBid and position 10 
         * represents the best ask. Each of the remaining position is a decile
         * of the spread.
         */
        int spread = BestAsk - BestBid;
        int decileValue = spread/10;
        int[] deciles = new int[11];
        for(int j=0;j<11;j++){
            deciles[j] = BestBid + decileValue*j;
        }
        return deciles;
    }
    public static int determineDecile(int Price, int[] deciles){
        /*
         * Given a price and an array of decile, this function returns
         * the decile where this price is located. Note that decile 
         * 10 and 9 are the same. 
         */
        for(int i=1;i<11;i++){
            if(Price>=deciles[(i-1)] && Price <= deciles[i]){
                return i;
            }
        }
        return -1;
    }
    
    public static void printDeciles(int[] deciles){
        /*
         * Helper function to print the deciles of a given spread.
         * Prints 11 values from best bid all the way to bes ask
         * and should pass through mid price.
         */
        for(int i=0;i<11;i++){
            System.out.println("\t"+deciles[i]);
        }
    }
    
}
