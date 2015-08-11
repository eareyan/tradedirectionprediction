/*
 * This class takes care of preparing a MessageBookSVM
 * to be SVM trained and tested.
 */
package financialpredictor.messagebook.svm;

import financialpredictor.messagebook.MessageBook;
import financialpredictor.messagebook.MessageBookEntry;
import java.util.ArrayList;
import java.util.Random;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_parameter;
import libsvm.svm_problem;

/**
 *
 * @author enriqueareyan
 */
public class MessageBookSVMDirection {
    
    private MessageBookSVM OriginalBook;
    private MessageBookSVM Train;
    private svm_model model;
    private svm_parameter param;
    private ArrayList<String> featureSet;
    private int nbrFeature;
    
    /*
     * Main Constructor to perform basic operations
     */
    public MessageBookSVMDirection(){
        /*
         * Set parameters for the SVM
         */
        this.param=new svm_parameter();
        this.param.svm_type=svm_parameter.C_SVC;
        this.param.kernel_type=svm_parameter.RBF;
        this.param.gamma=0.5;
        this.param.nu=0.5;
        this.param.cache_size=20000;
        this.param.C=1;
        this.param.eps=0.001;
        this.param.p=0.1;
        
    }
    /*
     * Constructor. Receives a MessageBookSVM and how many entries to use as training. 
     */
    public MessageBookSVMDirection(MessageBookSVM M, int trainNbr,double gamma,double C,ArrayList<String> featureSet)throws Exception{
        this();
        if(M.getNbrEntries() < trainNbr){
            throw new Exception("Not enough entries in the MessageBook. You asked to train with "+trainNbr+ " entries, but the MessageBook received only has "+ M.getNbrEntries());
        }        
        /*
         * Override parameters gamma and C
         */
        this.OriginalBook = M;
        this.param.gamma = gamma;
        this.param.C = C;
        this.featureSet = featureSet;
        if(this.featureSet.size()>3){
            throw new Exception("Feature set is bigger than allowed. Possible features are Time, Type, Order Id.");
        }
        /*
         * First we need to determine the number of features we are using.
         * If we use the feature Type, then we need to include room for 6 features
         * since these will be represented as categorical data.
         * If Type is not included, then the number is just the number of parameters in this.featureSet
         */
        this.nbrFeature = 2 + this.featureSet.size(); //At least 2 features always: size and price.
        if(this.featureSet.indexOf("Type")!=-1){
            this.nbrFeature += 5; //5 because the feature Type itself gives one
        }
        /*
         * Initialize MessageBook to be used as training
         */
        this.Train = new MessageBookSVM();
        /*
         * Get the proportion of points for each direction
         * then choose trainNbr random points, train the SVM
         */
        //First partition the book by direction:
        ArrayList<MessageBookEntry> SellBook = M.getEntriesByDirection(-1); //contains all sell entries
        ArrayList<MessageBookEntry> BuyBook = M.getEntriesByDirection(1);   //contains all buy entries
        //Obtain the proportion of buy to sell.
        int[] proportionDirection = M.getProportionDirectionPoints();
        /*
         * Here we select the sell and buy training data according to the
         * proportion in the original book. 
         */
        float cutoff = (float)proportionDirection[0] / (proportionDirection[0]+proportionDirection[1]);
        Random R = new Random();
        int currentRandNumber = 0;
        boolean pickSellOrder = true,SellOrBuyBookEmpty = false;
        for(int i=0;i<trainNbr;i++){//Loop to construct the Training Book
            if(!SellOrBuyBookEmpty){//Check if one of sell or buy book is empty
                if(R.nextFloat()<=cutoff){//Should pick a Sell Order
                    if(SellBook.isEmpty()){//We ran out of sell orders
                        pickSellOrder = false;
                        SellOrBuyBookEmpty = true;
                    }else{
                        pickSellOrder = true;
                    }
                }else{//Should pick a buy order
                    if(BuyBook.isEmpty()){//We ran out of buy orders
                        pickSellOrder = true;
                        SellOrBuyBookEmpty = true;                        
                    }else{
                        pickSellOrder = false;
                    }
                }
            }
            if(pickSellOrder){
                //Get a random sell order from the available ones
                currentRandNumber = R.nextInt(SellBook.size());
                this.Train.addEntry(SellBook.get(currentRandNumber),i);
                ((MessageBookSVMEntry)SellBook.get(currentRandNumber)).setTrain(true);
                SellBook.remove(currentRandNumber);                
            }else{
                //Get a random buy order from the available ones
                currentRandNumber = R.nextInt(BuyBook.size());
                this.Train.addEntry(BuyBook.get(currentRandNumber),i);
                ((MessageBookSVMEntry)BuyBook.get(currentRandNumber)).setTrain(true);
                BuyBook.remove(currentRandNumber);
                
            }
        }
    }
    /*
     * Getters
     */
    public MessageBook getTrainMessageBook(){
        return this.Train;
    }
    
    /*
     * This function creates an array of type svm_node to train a svm model using libsvm.
     */
    private financialpredictor.libsvm.svm_node[] prepareSVMNode(MessageBookEntry E){        
        financialpredictor.libsvm.svm_node[] nodes = new financialpredictor.libsvm.svm_node[this.nbrFeature];
        financialpredictor.libsvm.svm_node node;
        int iFeature = 0;
        /*
         * RESCALING data from the current messagebook, i.e.,
         * using the max and min from the current number of total rows: x - min / (max - min).
         * However, this strategy seems to be worst than using some other heuristics,
         * so rescaling might be a future research direction.
         */
        if(this.featureSet.indexOf("Time")!=-1){
            /* Rescale the Time. */        
            node = new financialpredictor.libsvm.svm_node();
            node.index = iFeature + 1;
            node.value = (Double.parseDouble(E.getTime()) - this.OriginalBook.getMaxMinTime()[1]) / (this.OriginalBook.getMaxMinTime()[0] - this.OriginalBook.getMaxMinTime()[1]);//Time        
            nodes[iFeature] = node;
            iFeature++;
        }
        if(this.featureSet.indexOf("Type")!=-1){
            /*
             * The type data is categorical. We will represent it with a 6 dimensional vector
             * where a 1 in the ith position indicate a trade of type ith 
             *                      1: Submission of a new limit order
                                    2: Cancellation (Partial deletion 
                                       of a limit order)
                                    3: Deletion (Total deletion of a limit order)
                                    4: Execution of a visible limit order			   	 
                                    5: Execution of a hidden limit order
                                    7: Trading halt indicator 	
             */
            for(int i=0;i<6;i++){
                node = new financialpredictor.libsvm.svm_node();
                node.index = iFeature + 1;
                if(i == E.getType()){
                    node.value = 1;       //Type
                }else{
                    node.value = 0;
                }
                nodes[iFeature] = node;
                iFeature++;
            }
        }
        if(this.featureSet.indexOf("Order Id")!=-1){
            /* Rescale the order id. */
            node = new financialpredictor.libsvm.svm_node();
            node.index = iFeature + 1;
            node.value = (double)(E.getOrderId() - this.OriginalBook.getMaxOrderId()[1]) / (double)(this.OriginalBook.getMaxOrderId()[0] - this.OriginalBook.getMaxOrderId()[1]);    //Order ID
            nodes[iFeature] = node;
            iFeature++;
        }
        /* Rescale size. This is part of the basic set and it is always included. */
        node = new financialpredictor.libsvm.svm_node();
        node.index = iFeature + 1;
        node.value = (double) ((E.getSize() - this.OriginalBook.getMaxMinSize()[1] ) / (double)(this.OriginalBook.getMaxMinSize()[0] - this.OriginalBook.getMaxMinSize()[1]));       //Size
        nodes[iFeature] = node;        
        iFeature++;
        /* We need to rescale price. This is part of the basic set and it is always included. */
        node = new financialpredictor.libsvm.svm_node();
        node.index = iFeature + 1;
        node.value = (double) ((E.getPrice() - this.OriginalBook.getMaxMinPrice()[1]) / (double)(this.OriginalBook.getMaxMinPrice()[0] - this.OriginalBook.getMaxMinPrice()[1]));      //Price
        nodes[iFeature] = node;   
        return nodes;
    }    
    
    /*
     * This functions creates a model to be used in the libsvm library
     * to classify the direction of an entry in the message book.
     */
    public void createDirectionSVMModel(){
        svm_problem problem = new svm_problem();
        problem.l = this.Train.getNbrEntries();          // Number of Training vectors
        problem.y = new double[this.Train.getNbrEntries()];    // Labels of the vectors
        problem.x = new financialpredictor.libsvm.svm_node[this.Train.getNbrEntries()][];// Vectors
        /*
         * Loop through each Entry and get the SVMNode to create the model
         */
        for(int i=0;i<this.Train.getNbrEntries();i++){
            problem.y[i] = (double) this.Train.getEntry(i).getDirection();   //Set label
            problem.x[i] = this.prepareSVMNode(this.Train.getEntry(i));      // Let the MessageBookEntry prepare its node
            //financialpredictor.libsvm.svm_node.printNodeArray(problem.x[i]); //only for debugging purposes
        }

        /*
         * Set the SVM model
         */
        this.model = svm.svm_train(problem,this.param);
    }
    /*
     * Function to predict data
     */
    public double predictData(MessageBookEntry E){
        return svm.svm_predict(this.model, this.prepareSVMNode(E));
    }
    /*
     * Function to predict a whole mesage book.
     * The function returns the number of correctly predicted entries
     * and the proportion of the correctly predicted entries
     */
    public double[] predictMessageBook(MessageBook M){
        int good = 0,total=0;
        int[] totalByType = new int[8];
        int[] goodByType = new int[8];
        for(int j=0;j<8;j++){
            goodByType[j] = 0;
            totalByType[j] = 0;
        }
        for(int i=0;i<M.getNbrEntries();i++){
            /* Only get a prediction on those orders that were not used for training. 
             * In other words, our prediction exclude the orders that were used for training.
             */
            MessageBookSVMEntry E = (MessageBookSVMEntry)M.getEntry(i);
            if(!E.isTrain()){
                total++;
                totalByType[E.getType()]++;
                if(this.predictData(E) == M.getEntry(i).getDirection()){
                    good++;
                    goodByType[E.getType()]++;
                }
            }
        }
       /*
        * Next 3 lines for debugging purposes only:
        */
       /* 
        for(int j=0;j<8;j++){
            System.out.println(j+":"+totalByType[j]+"-"+goodByType[j]);
        }
        System.out.println("Accuracy 4 or 5: " +((double)(goodByType[4]+goodByType[5]) / (double)(totalByType[4]+totalByType[5])));
       */
        double[] ret = new double[3];
        ret[0] = good;
        ret[1] = (float)good/total;
        ret[2] = total;
        return ret;
    }
    /*
     * Function that returns the prediction of an entry
     * given its orderId
     */
    public double predictByOrderID(int orderID){
        for(int i=0;i<this.OriginalBook.getNbrEntries();i++){
            if(this.OriginalBook.getEntry(i).getOrderId() == orderID){
                return this.predictData(this.OriginalBook.getEntry(i));
            }
        }
        return 0;
    }
}
