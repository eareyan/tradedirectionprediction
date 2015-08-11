/*
 * This class represents an entry (or a row) in the order book.
 */
package financialpredictor.orderbook;

/**
 *
 * @author enriqueareyan
 */
public class OrderBookEntry {
    /*
     * Best Ask
     */
    private int BestAsk;
    /*
     * Best Ask Volume
     */
    private int BestAskVolume;
    /*
     * Best Ask Bid
     */
    private int BestBid;
    /*
     * Best Bid Volume
     */
    private int BestBidVolume;
    /*
     * Best Midpoint
     */
    private float MidPoint;
    /*
     * 1 :   means upward midpoint crossing
     * -1:   means downward midpoint crossing
     * 0 :   means stationary midpoint crossing
     */    
    private byte LabelMidPoint;
    
    /*
     * Constructor.
     */
    public OrderBookEntry(int BestAsk, int BestAskVolume, int BestBid, int BestBidVolume){
        this.BestAsk = BestAsk;
        this.BestAskVolume = BestAskVolume;
        this.BestBid = BestBid;
        this.BestBidVolume = BestBidVolume;
        this.MidPoint = (BestAsk + BestBid) / 2;
    }
    public void setLabelMidPoint(byte LabelMidPoint){
        this.LabelMidPoint = LabelMidPoint;
    }
    /*
     * Getters
     */
    public int getBestAsk(){
        return this.BestAsk;
    }
    public int getBestAskVolume(){
        return this.BestAskVolume;
    }
    public int getBestBid(){
        return this.BestBid;
    }
    public int getBestBidVolume(){
        return this.BestBidVolume;
    }
    public float getMidPoint(){
        return this.MidPoint;
    }
    public byte getLabelMidPoint(){
        return this.LabelMidPoint;
    }
}
