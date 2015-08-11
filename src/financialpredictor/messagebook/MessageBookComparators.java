/*
 * This class contains comparators for different
 * features of the message book.
 */
package financialpredictor.messagebook;

import java.util.Comparator;

/**
 *
 * @author enriqueareyan
 */
public enum MessageBookComparators implements Comparator<MessageBookEntry>{
    PriceComparator{
        @Override
        public int compare(MessageBookEntry E1, MessageBookEntry E2){
            if(E1.getPrice() < E2.getPrice()){
                return 1;
            }else if(E1.getPrice() > E2.getPrice()){
                return -1;
            }
            return 0;
        }
    },
    SizeComparator{
        @Override
        public int compare(MessageBookEntry E1, MessageBookEntry E2){
            if(E1.getSize() < E2.getSize()){
                return 1;
            }else if(E1.getSize() > E2.getSize()){
                return -1;
            }
            return 0;
        }
    },
    TimeComparator{
        @Override
        public int compare(MessageBookEntry E1, MessageBookEntry E2){
            if(Double.parseDouble(E1.getTime()) < Double.parseDouble(E2.getTime())){
                return 1;
            }else if(Double.parseDouble(E1.getTime()) > Double.parseDouble(E2.getTime())){
                return -1;
            }
            return 0;
        }
    },
    OrderIdComparator{
        @Override
        public int compare(MessageBookEntry E1, MessageBookEntry E2){
            if(E1.getOrderId() < E2.getOrderId()){
                return 1;
            }else if(E1.getOrderId() > E2.getOrderId()){
                return -1;
            }
            return 0;
        }
    }    
}