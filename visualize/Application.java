package visualize;

import java.awt.EventQueue;

import javax.swing.JFrame;

import utils.UtilityMethods;
import csguided.BDDWrapper;
import csguided.GridCell2D;
import csguided.Variable;
import jdd.bdd.BDD;
import jdd.bdd.Permutation;


public class Application extends JFrame {
    
    public Application() {

        initUI();
    }

    private void initUI() {
    	
    	BDD bdd = new BDD(10000, 1000);
    	int dim = 10;
    	int numOfBits = UtilityMethods.numOfBits(dim);
    	
    	Variable[] x = Variable.createVariables(bdd, numOfBits, "x");
    	Variable[] y = Variable.createVariables(bdd, numOfBits, "y");
    	Variable[] vars = Variable.unionVariables(x, y);
    	Variable[] xPrime = Variable.createPrimeVariables(bdd, x);
    	Variable[] yPrime = Variable.createPrimeVariables(bdd, y);
    	Variable[] varsPrime = Variable.unionVariables(xPrime, yPrime);
    	
    	int cube=bdd.ref(bdd.getOne());
		for(int i=0;i<vars.length;i++){
			cube=bdd.andTo(cube, vars[i].getBDDVar());
		}
		
    	Permutation vPrimeToV = bdd.createPermutation( Variable.getBDDVars(varsPrime), Variable.getBDDVars(vars));
    	
    	//init
    	int initCellX = BDDWrapper.assign(bdd, 0, x);
    	int initCellY = BDDWrapper.assign(bdd, 0, y);
    	int init = BDDWrapper.and(bdd, initCellX, initCellY);
    	BDDWrapper.free(bdd, initCellX);
    	BDDWrapper.free(bdd, initCellY);
    	
    	//trans
    	int currentCell1 = assignCell(bdd, x, y, new GridCell2D(0, 0));
    	int nextCell1 = assignCell(bdd, xPrime, yPrime, new GridCell2D(0,1));
    	int trans1 = bdd.ref(bdd.and(currentCell1, nextCell1));
    	
    	int currentCell2 = assignCell(bdd, x, y, new GridCell2D(0, 1));
    	int nextCell2 = assignCell(bdd, xPrime, yPrime, new GridCell2D(0,0));
    	int trans2 = bdd.ref(bdd.and(currentCell2, nextCell2));
    	
    	int trans= bdd.ref(bdd.or(trans1, trans2));
    	
    	bdd.printSet(trans);
    	
        add(new GameBoard(400,400,dim,dim, bdd, init, trans, x, y, cube, vPrimeToV ));
        setResizable(false);
        pack();
        

        setTitle("draw grids");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }    
    
    public static void main(String[] args) {
        
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                Application ex = new Application();
                ex.setVisible(true);
            }
        });
    }
    
    public int assignCell(BDD bdd, Variable[] x, Variable[] y , GridCell2D cell){
		int xAssign=BDDWrapper.assign(bdd, cell.getX(), x);
		int yAssing=BDDWrapper.assign(bdd, cell.getY(), y);
		int result=bdd.ref(bdd.and(xAssign, yAssing));
		bdd.deref(xAssign);
		bdd.deref(yAssing);
		return result;
	}
}