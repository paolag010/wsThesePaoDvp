package fml.helloWord;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.XMLResource;
//import org.eclipse.core.expressions.Expression;
import org.junit.Test;
import org.xtext.example.mydsl.fml.FeatureEdgeKind;
import org.xtext.example.mydsl.fml.FeatureModel;
import org.xtext.example.mydsl.fml.SliceMode;

import splar.core.fm.XMLFeatureModel;
//import de.ovgu.featureide.fm.core.FeatureModel;
import fr.familiar.FMLTest;
import fr.familiar.experimental.FGroup;
import fr.familiar.fm.basic.FMLFeatureModelReader;
import fr.familiar.fm.basic.FMLFeatureModelWriter;
import fr.familiar.fm.featureide.FMLtoFeatureIDE;
import fr.familiar.gui.featureide.FM_Familiar_FeatureIDE;
import fr.familiar.operations.Mode;
import fr.familiar.parser.FMBuilder;
import fr.familiar.parser.FMConverter;
import fr.familiar.parser.FMConverterFormat;
import fr.familiar.variable.Comparison;
import fr.familiar.variable.ConstraintVariable;
import fr.familiar.variable.FeatureModelVariable;
import fr.familiar.variable.FeatureVariable;
import fr.familiar.variable.SetVariable;
import fr.familiar.variable.Variable;
import gsd.synthesis.Expression;
import gsd.synthesis.ExpressionType;

import org.eclipse.core.resources.IFile;

import de.ovgu.featureide.fm.core.io.xml.XmlFeatureModelReader;
import de.ovgu.featureide.fm.core.io.xml.XmlFeatureModelWriter;


public class mainHelloWorldFML  extends FMLTest {
	
	public static void main(String[] args) {
		
		try {
			
			mainHelloWorldFML gs = new mainHelloWorldFML();			
			gs.setUp();
			
			gs.testHelloFML();
			
			gs.tearDown();
			
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
	}	

	
	public void testHelloFML() throws Exception {
		
		
		// Valid feature model. B and C are optional features of A (root)
		FeatureModelVariable fmv1 = this.createFMStrategy01("fm1","A : [B] [C] ;");                 
		System.out.println("\n*** fmv1 ***\n");
		showFM(fmv1);
        showFMInfo(fmv1);
        showConfigurations(fmv1);   

        
        // alternate way to build a feature model
        FeatureModelVariable fmv2 = new FeatureModelVariable ("fm2", FMBuilder.getInternalFM("A : [B] [C] ;"));
        System.out.println("\n*** fmv2 ***\n"); 
        System.out.print("Compare fmv1 and fmv2: ");
        showComparation(fmv2, fmv1);
        
        
        // void or unsatisfiable feature model
        // B and C are mandatory features and B -> !C introduces a logical contradiction
        FeatureModelVariable fmv3 = this.createFMStrategy01("fm3","A : B C ; B -> !C;");
		System.out.println("\n*** fmv1 ***\n");
		showFM(fmv3);
        showFMInfo(fmv3);
        
        
        // false optional, dead, core, all configs
        // FM where C is a dead feature and D is a core feature  
        FeatureModelVariable fmv4 = this.createFMStrategy01("fm4","A : B [C] [D] ; B -> !C; B -> D;");
        System.out.println("\n*** fmv4 ***\n");
        showFM(fmv4);
        showFMInfo(fmv4);
        showConstraints(fmv4);        
        
                
        System.out.println("\n*** fmv5 - FMBase ***\n");
        FeatureModelVariable fmv5 = this.createFMStrategy01("fm4","A : B [C] [D] ; B -> !C; B -> D;"); 
        fmv5.removeFeature("B");
        showFM(fmv5);

    }

	
	// ********************************************************
	// **********   	OPERATIONS WITH FM		***************
	// ********************************************************		
	
	
	//  -----   	OPERATIONS WITH FM  - USING FAMILIAR OPTIONS		-----
	
	public FeatureModelVariable  createFMStrategy01(String fmName, String fm) throws Exception{		        
		// Example: FeatureModelVariable fmv = FM ("fm1", "FM (A : [B] [C] ;)");
		
		FeatureModelVariable fmv = FM (fmName, "FM (" + fm + ")");      
        return fmv;        
	}

	// alternate way to build a feature model
	public FeatureModelVariable createFMStrategy02(String fmName, String fm) throws Exception{
		// Example FeatureModelVariable fmv2 = new FeatureModelVariable ("fm2", FMBuilder.getInternalFM("A : [B] [C] ;"));
		
		FeatureModelVariable fmv = new FeatureModelVariable (fmName, FMBuilder.getInternalFM(fm));
	    return fmv;
	}	
	
	public String getConstraints(FeatureModelVariable fmv){        
		
		String cnst = "";
        Set<Expression<String>> constraints = fmv.getAllConstraints();	 
        
        for (Expression<String> cf : constraints) {
            cnst += cf.toString() + "; \n";
        }        
        
        return cnst;
	}

	public String getConstraints(Set<Expression<String>> constraints){        
		
		String cnst = "";
		
        for (Expression<String> cf : constraints) {
            cnst += cf.toString() + "; \n";
        }        
                
        return cnst;
	}
		
	public String getConstraintsFeatureImplies(Set<Expression<String>> constraints, String featureSource){        
		
		String cnst = "";
		
		for (Expression<String> cf : constraints) {
        	if (cf.getLeft().toString().equals(featureSource))
        		cnst += cf.toString() + "; \n";
        		//constraints.remove(cf);
        }        
        
        return cnst;
	}	
	
	
	//If fRoot is the root, the subfeature will include the constraints
	//If fRoot is not the root, the subfeature discard the constraints
	//Extract but preserving the original fm
	public FeatureModelVariable extractSubFM(FeatureModelVariable fmv, String fRoot) throws Exception {
		
		FeatureModelVariable fmExtracted = null;		
		fmExtracted = new FeatureModelVariable ("fmExtracted", fmv.extract(fRoot));
		//fmv.removeFeature("sB"); 												     // Remove the entire branch
		
		return fmExtracted;
	}

	
	// ********************************************
	// **********   	EXPORT FM		***********
	// ********************************************	
	
	public void exportFMtoXMI(FeatureModelVariable fmv) throws Exception{
		
		System.out.println("\n\nExporting fm to xmi ...");		
		FeatureModel fm = (FeatureModel) new FMLFeatureModelReader().parseString( fmv.toString() );
		XMIResource xmi = new FMLFeatureModelWriter((org.xtext.example.mydsl.fml.FeatureModel) fm).toXMI("examples/testing/FMs/"+fmv.getIdentifier());
		
		//https://nyx.unice.fr/projects/familiar/wiki/fmload/
		//FeatureModel fm = (FeatureModel) new FMLFeatureModelReader().parseString( fmv.toString() );
		//FeatureModel fm = new FMLFeatureModelReader().parseFile(new File("examples/testing/FMs/fm2.fml"));
		//FeatureModel fm = (FeatureModel) new FMLFeatureModelReader().parseString( "FM (A : [B] [C] ;)" );
		//XMIResource xmi = new FMLFeatureModelWriter(fm).toXMI("examples/testing/FMs/fm2ecore");		// serializing fm to XMI
		//new FMLFeatureModelWriter(fm2).toString() ;													// or to text (String representation) 
		//FeatureModel fm3 = new FMLFeatureModelReader().parseXMIFile(xmi);	
	}
	
	public void exportFMtoSplot(FeatureModelVariable fmv) throws Exception{
		
		System.out.println("Exporting fm to Splot ...");
		FeatureModel fm = (FeatureModel) new FMLFeatureModelReader().parseString( fmv.toString() );		
		fmv.toSPLOT();
		
		//FM_Familiar_FeatureIDE.get(fmv);
		//String s = new FMLtoFeatureIDE(fmv).transformToText();
	}
	

	// ****************************************************
	// **********   	SHOW FM INFORMATION		***********
	// ****************************************************	
	
	public void showFM(FeatureModelVariable fmv){
		System.out.println(fmv.toString());
	}
	
	public void showFMInfo(FeatureModelVariable fmv){		
		System.out.println();
        System.out.println("Valid model? : \t" + fmv.isValid());
        System.out.println("Number of features: \t" + fmv.features().size() );
        System.out.println("Number of config: \t" + fmv.counting() );        
        System.out.println("Number of cores : \t" + fmv.cores().size() );
        System.out.println("Number of deads : \t" + fmv.deads().size() );
	}
	
	public void showConstraints(FeatureModelVariable fmv){
        System.out.println("Constraints :");
        Set<Expression<String>> constraints = fmv.getAllConstraints();	 
        for (Expression<String> cf : constraints) {
            String cnst = cf.toString();            	  
            System.out.println("\t" + cnst);
        }        
        System.out.println();
	}
	
	public void showComparation(FeatureModelVariable fmv1, FeatureModelVariable fmv2){
		
		String result = fmv2.compare(fmv1).toString();
		
		if(result.equals(Comparison.REFACTORING.toString())){
			System.out.print("equals true" );		
		}
		
        System.out.println();
	}
	
	public void showConfigurations(FeatureModelVariable fmv){
        System.out.println("\nValid configurations:\n");
        
        Set<Variable> allConfigs = fmv.configs();	 
        for (Variable cf : allConfigs) {
            Set<String> confFts = ((SetVariable) cf).names();
            FeatureConfiguration ftConf = new FeatureConfiguration(confFts, fmv);	  
            System.out.println("\t" + confFts + " " + ftConf.getConfMap());
        }        
        System.out.println();	
    }
	
	public void showGroups(FeatureModelVariable fmv){
        System.out.println("\nGroups:\n");
        
        Set<FGroup> allGroups = fmv.getGroups();     
        
        for (FGroup cf : allGroups) {
            String confFts = ((FGroup) cf).toString();
            System.out.println("\t" + confFts);
        }        
        System.out.println();	
    }
	


	
}