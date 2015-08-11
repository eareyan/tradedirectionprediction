/*
 * This class represents an entry (or a row) in the messabe book.
 * 
 */
package financialpredictor.messagebook;

/**
 *
 * @author enriqueareyan
 */
public class MessageBookEntry {
    private int index = -1;
    private String Time;
    private byte Type;
    private int OrderId;
    private int Size;
    private int Price;
    private byte Direction;
    
    public MessageBookEntry(String Time,byte Type, int OrderId,int Size, int Price, byte Direction){
        this.Time       = Time;
        this.Type       = Type;
        this.OrderId    = OrderId;
        this.Size       = Size;
        this.Price      = Price;
        this.Direction  = Direction;
    }
    /*
     * Setters
     */
    public void setIndex(int index){
        this.index = index;
    }
    /*
     * Getters
     */
    public int getIndex(){
        return this.index;
    }
    public String getTime(){
        return this.Time;
    }
    public byte getType(){
        return this.Type;
    }
    public int getOrderId(){
        return this.OrderId;
    }
    public int getSize(){
        return this.Size;
    }
    public int getPrice(){
        return this.Price;
    }
    public byte getDirection(){
        return this.Direction;
    }
    
    @Override
    public String toString(){
        String ret = "";
        if(this.index!=-1){
            ret = ret + "\t"+ this.index + "\t";
        }
        ret = ret + this.Direction + "\t" +this.Time + "\t\t" + this.Type + "\t\t" + this.Size + "\t" + this.Price +"\t\t" + this.OrderId;
        return ret;
    }
}
