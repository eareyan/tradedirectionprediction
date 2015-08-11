/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package financialpredictor.libsvm;

/**
 *
 * @author enriqueareyan
 */
public class svm_node extends libsvm.svm_node{
    
    public svm_node(){
        super();
    }
    
    @Override
    public String toString(){
        return "("+ this.index + "," + this.value + ")";
    }
    
    public static void printNodeArray(libsvm.svm_node[] n){
        System.out.println("***************");
        for(int i=0;i<n.length;i++){
            System.out.println(n[i]);
        }
        System.out.println("***************");        
    }
    
}
