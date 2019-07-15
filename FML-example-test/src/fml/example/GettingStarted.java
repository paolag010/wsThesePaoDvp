package fml.example;

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


public class GettingStarted  extends FMLTest {
	
	public static void main(String[] args) {
		
		try {
			
			GettingStarted gs = new GettingStarted();			
			gs.setUp();
			
			
			//gs.testHelloFMLJUnit();		// original + FeatureConfiguration.java 
			//gs.testHelloFML();
			
			
			gs.runAlgSchemTest();
			//gs.runAlgSchemFMConfs();
			
			
			gs.tearDown();
			
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
	}	
	
	
	public void testHelloFMLJUnit() throws Exception {
        FeatureModelVariable fmv = FM ("fm1", "FM (A : [B] [C] ;)"); // B and C are optional features of A (root)        
        assertEquals(4.0, fmv.counting(), 0.0);

        // alternate way to build a feature model
        FeatureModelVariable fmv2 = new FeatureModelVariable ("fm2", FMBuilder.getInternalFM("A : [B] [C] ;"));
        assertEquals(Comparison.REFACTORING, fmv2.compare(fmv));
        
        // void or unsatisfiable feature model
        FeatureModelVariable fmv3 = FM ("fm1", "FM (A : B C ; B -> !C; )");   // B and C are mandatory features and B -> !C introduces a logical contradiction     
        assertEquals(0.0, fmv3.counting(), 0.0);
        assertFalse(fmv3.isValid());
        
        // false optional, dead, core, all configs
        FeatureModelVariable fmv4 = FM ("fm1", "FM (A : B [C] [D] ; B -> !C; B -> D; )"); // C is a dead feature and D is a core feature      
        assertEquals(1.0, fmv4.counting(), 0.0);
        assertTrue(fmv4.isValid());        
        assertEquals(1, fmv4.falseOptionalFeatures().size());
        assertEquals(3, fmv4.cores().size());
        assertEquals(1, fmv4.deads().size());
                 
        Set<Variable> allConfigs = fmv.configs();	 
        for (Variable cf : allConfigs) {
            Set<String> confFts = ((SetVariable) cf).names();
            FeatureConfiguration ftConf = new FeatureConfiguration(confFts, fmv);	  
            System.err.println("" + confFts + " " + ftConf.getConfMap());
        }
        
        // slice, merge, etc       
        
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
        FeatureModelVariable fmv5 = null; 
        fmv5 = extractSubFM("A", "B", "R1");
        showFM(fmv5);
        
        
/*
        System.out.println("\n\n*** fmv7 - FMBase ***\n");
        FeatureModelVariable fmv7 = null; 
        fmv7 = generateFMBase2("A", "B", "R1");
        showFM(fmv7);
*/		

    }
	
	public void runAlgSchemTest() throws Exception{
		
		FeatureModelVariable fmvFull = null;         
        FeatureModelVariable fmvToWeave = null;
        FeatureModelVariable fmvSub = null;
        Set<Expression<String>> fmvFullCnst = null;
        Set<Expression<String>> fmvToWeaveCnst = null;
        String fmvSubStringCnst = null;
        
        String source,target,assoc = null;
        
        
        source = "A"; target = "B"; assoc = "R1"; 
        fmvFull = generateFMBase(source, target, assoc);
        fmvFullCnst = fmvFull.getAllConstraints();
        
        System.out.println("\n\n*** fmvFull - AB ***\n");
        showFM(fmvFull); 
        showFMInfo(fmvFull);
        
        source = "B"; target = "C"; assoc = "R2";
        fmvToWeave 	= generateFMBase(source, target, assoc);
        fmvToWeaveCnst = fmvToWeave.getAllConstraints();  
        
        System.out.println("\n\n*** fmvBC ***\n");
        showFM(fmvToWeave);
        
        
        String fRootfmvSub 	= assoc+"_s"+source;									// R2_sB;        
        String constLeft 	=  source+"ref"+target; 								// BrefC
        fmvSub = extractSubFM(fmvToWeave, fRootfmvSub);
        fmvSubStringCnst = getConstraintsFeatureImplies(fmvToWeaveCnst, constLeft);
        
        System.out.println("\n\n*** fmvSub ***\n");
        showFM(fmvSub);
        System.out.println("\n Constraints: \n" + fmvSubStringCnst);
       
        String featureToInsert = "rel" + source;									//"relB";
        fmvFull = insertFMXorMode(fmvFull, fmvSub, featureToInsert,fmvSubStringCnst);
        
        System.out.println("\n\n*** fmvFull - new ***\n");
        showFM(fmvFull);
        
	}
	
	
	public void runAlgSchemFMConfs() throws Exception{
		
		FeatureModelVariable fmvFull = null;         
        Set<Expression<String>> fmvFullCnst = null;
        String source,target,assoc = null;
        
        source = "A"; target = "B"; assoc = "R1"; 
        fmvFull = generateFMBase(source, target, assoc);
        fmvFullCnst = fmvFull.getAllConstraints();
        
        System.out.println("\n\n*** fmvFull - AB ***\n");
        showFM(fmvFull); 
        showFMInfo(fmvFull);
        showConfigurationsTest(fmvFull);
	}

	
	
	//**********   	SHEMALG   *********

		
	public FeatureModelVariable generateFMBase(String Eo, String Ed, String r) throws Exception {
		FeatureModelVariable fmBase = null;
		
		String s1 = "refsPairs";
		
		String f = "FM ( ";
		f = f + " SM"     + ": " + "SEnt [SView]"           					+ ";";
		
		f = f + " SEnt"   + ": " + "("+ "s"+Eo +"|"+ "s"+Ed +")+"    	  		+ ";";
		
		f = f + " s"+Eo   + ": " + "("+ "rel"+Eo +"|"+ Eo + ")"    	    		+ ";";
		f = f + " rel"+Eo + ": " + r +"_s" +Eo               					+ ";";
		f = f + r+"_s"+Eo + ": " + "("+ Eo+"emb"+Ed +"|"+ Eo+"ref"+Ed + ")"  	+";";
		
		f = f + " s"+Ed   + ": " + "("+ "rel"+Ed +"|"+ Ed + ")"      	  		+ ";";
		f = f + " rel"+Ed + ": " + r +"_s" +Ed               					+ ";";
		f = f + r+"_s"+Ed + ": " + "("+ Ed+"emb"+Eo +"|"+ Ed+"ref"+Eo + ")"  	+ ";";
		
		f = f + " SView"  + ": " + "refs2"           						    + ";"; 			//No se pueden comenzar los nombres de los features por numero
		f = f + " refs2"  + ": " + Eo + Ed + "refs"     						+ ";";
		
		//f = f + Eo + "ref" + Ed + " -> " + "s" + Ed   						+ ";";
		f = f + Eo+Ed+"refs" + " -> (" + "s"+Eo+" & "+"!"+r+"_s"+Eo+" & "+"s"+Ed+" & "+"!"+r+"_s"+Ed	+ ");";
		
		f = f + ")";
		
				
		fmBase = FM ("fmBase" , f);
		
		//System.out.println(f);
		//System.out.println("\n--- fmBase \n" + fmBase.toString());
		
		Expression<String> exp = null;
		Expression<String> expComplexLeft = null;
		Expression<String> expComplexRight = null;
		
		
		//Eo -> EoEdrefs OR r_sEd;
		expComplexLeft = new Expression<String>(Eo);
		expComplexRight = new Expression<String>(ExpressionType.OR, Eo+Ed+"refs", r +"_s" +Ed);
		exp = new Expression<String>(ExpressionType.IMPLIES, expComplexLeft, expComplexRight);
		fmBase.addConstraint(exp);

		//Ed -> EoEdrefs OR r_sEo;
		expComplexLeft = new Expression<String>(Ed);
		expComplexRight = new Expression<String>(ExpressionType.OR, Eo+Ed+"refs", r +"_s" +Eo);
		exp = new Expression<String>(ExpressionType.IMPLIES, expComplexLeft, expComplexRight);
		fmBase.addConstraint(exp);		
		
		//EorefEd -> sEd;
		exp = new Expression<String>(ExpressionType.IMPLIES, Eo + "ref" + Ed, "s" + Ed);
		fmBase.addConstraint(exp);

		//EdrefEo -> sEo;
		exp = new Expression<String>(ExpressionType.IMPLIES, Ed + "ref" + Eo, "s" + Eo);
		fmBase.addConstraint(exp);

		
		//EorefEd -> !EdrefEo;
		expComplexLeft = new Expression<String>(Eo + "ref" + Ed);
		expComplexRight = new Expression<String>(ExpressionType.NOT, Ed + "ref" + Eo,null);
		exp = new Expression<String>(ExpressionType.IMPLIES, expComplexLeft, expComplexRight);
		fmBase.addConstraint(exp);

		//EorefEd -> !EdembEo;
		expComplexLeft = new Expression<String>(Eo + "ref" + Ed);
		expComplexRight = new Expression<String>(ExpressionType.NOT, Ed + "emb" + Eo,null);
		exp = new Expression<String>(ExpressionType.IMPLIES, expComplexLeft, expComplexRight);
		fmBase.addConstraint(exp);
		
		//EdrefEo -> !EdembEo;
		expComplexLeft = new Expression<String>(Ed + "ref" + Eo);
		expComplexRight = new Expression<String>(ExpressionType.NOT, Eo + "emb" + Ed,null);
		exp = new Expression<String>(ExpressionType.IMPLIES, expComplexLeft, expComplexRight);
		fmBase.addConstraint(exp);

		//EdrefEo -> !EdembEo;
		//f = f + Eo+Ed+"refs" + " -> " + "s"+Eo+" & "+"!"+r+"_s"+Eo+" & "+"s"+Ed+" & "+"!"+r+"_s"+Ed;
		//f = f + Eo + "ref" + Ed + " -> " + "s" + Ed   						+ ";";
		
		Expression<String> expTemp1 = null;
		Expression<String> expTemp2 = null;
		/*
		expComplexLeft = new Expression<String>("s" + Eo);
		expComplexRight = new Expression<String>(ExpressionType.NOT, r +"_s" +Eo,null);
		expTemp1 = new Expression<String>(ExpressionType.AND, expComplexLeft, expComplexRight);
		expComplexLeft = new Expression<String>("s" + Ed);
		expComplexRight = new Expression<String>(ExpressionType.NOT, r +"_s" +Ed,null);
		expTemp2 = new Expression<String>(ExpressionType.AND, expComplexLeft, expComplexRight);
		expComplexLeft = new Expression<String>(Eo+Ed+"refs");
		expComplexRight = new Expression<String>(ExpressionType.AND, expTemp1,expTemp2);
		*/
		/*
		expComplexLeft = new Expression<String>("s" + Eo);
		expComplexRight = new Expression<String>(ExpressionType.NOT, r +"_s"+Eo,null);
		expTemp1 = new Expression<String>(ExpressionType.AND, expComplexLeft, expComplexRight);
		expComplexLeft = expTemp1;
		expComplexRight = new Expression<String>("s" + Ed);
		expTemp1 = new Expression<String>(ExpressionType.AND, expComplexLeft, expComplexRight);
		expComplexLeft = expTemp1;
		expComplexRight = new Expression<String>(ExpressionType.NOT, r +"_s" +Ed,null);
		expTemp1 = new Expression<String>(ExpressionType.AND, expComplexLeft,expComplexRight);
				
		expComplexLeft = new Expression<String>(Eo+Ed+"refs");				
		exp = new Expression<String>(ExpressionType.IMPLIES, expComplexLeft, expTemp1);
		fmBase.addConstraint(exp);
		*/
		//System.out.println("\n*** after adding constraints ***\n" + fmBase.toString());
		//System.out.println("\n--- after adding constraints ...");
		
		return fmBase;
	}
		
	public FeatureModelVariable extractSubFM(String Eo, String Ed, String r) throws Exception {
		FeatureModelVariable fmBase = null;
		FeatureModelVariable fmExtracted = null;
		
		String f = "FM ( ";
		f = f + " SM"     + ": " + "SEnt [SView]"           					+ ";";
		f = f + " SEnt"   + ": " + "("+ "s"+Eo +"|"+ "s"+Ed +")+"      		+ ";";
		
		f = f + " s"+Eo   + ": " + "("+ "rel"+Eo +"|"+ Eo + ")"        		+ ";";
		f = f + " rel"+Eo + ": " + r +"_s" +Eo               					+ ";";
		f = f + r+"_s"+Eo + ": " + "("+ Eo+"emb"+Ed +"|"+ Eo+"ref"+Ed + ")"  	+";";
		
		f = f + " s"+Ed   + ": " + "("+ "rel"+Ed +"|"+ Ed + ")"        		+ ";";
		f = f + " rel"+Ed + ": " + r +"_s" +Ed               					+ ";";
		f = f + r+"_s"+Ed + ": " + "("+ Ed+"emb"+Eo +"|"+ Ed+"ref"+Eo + ")"  	+";";
		
		f = f + ")";
							
		fmBase = FM ("fmBase" , f);       									//	System.out.println(f);
		System.out.println("\n--- fmBase \n" + fmBase.toString());
		
		fmExtracted = new FeatureModelVariable ("fmExtracted", fmBase.extract("sB"));
		System.out.println("\n--- fmExtracted:\n" + fmExtracted.toString());
		
		System.out.println("\n--- removint branch sB ..." + fmExtracted.toString());
		fmBase.removeFeature("sB"); 												// Remove the entire branch
		
		return fmBase;
	}
	
	public FeatureModelVariable generateFMBase2(String Eo, String Ed, String r) throws Exception {
		FeatureModelVariable fmBase = null;
		
		String f = "FM ( ";
		f = f + " SM"     + ": " + "SEnt [SView]"           					+ ";";
		//f = f + " SEnt"   + ": " + "("+ "s"+Eo +"|"+ "s"+Ed +")+"      		+ ";";		
		//f = f + " SEnt"   + ": " + "s"+Eo + " s"+Ed							     		+ ";";
		f = f + " SEnt"   + ": " + "s"+Eo 							     		+ ";";
		
		f = f + " s"+Eo   + ": " + "("+ "rel"+Eo +"|"+ Eo + ")"     	   		+ ";";
		f = f + " rel"+Eo + ": " + r +"_s" +Eo               					+ ";";
		f = f + r+"_s"+Eo + ": " + "("+ Eo+"emb"+Ed +"|"+ Eo+"ref"+Ed + ")"  	+";";
		
		//f = f + " s"+Ed   + ": " + "("+ "rel"+Ed +"|"+ Ed + ")"        		+ ";";
		//f = f + " rel"+Ed + ": " + r +"_s" +Ed               					+ ";";
		//f = f + r+"_s"+Ed + ": " + "("+ Ed+"emb"+Eo +"|"+ Ed+"ref"+Eo + ")"  	+";";
		
		f = f + ")";
		
		fmBase = FM ("fmBase" , f);	
		System.out.println(f);
		System.out.println("\n--- fmBase \n" + fmBase.toString());		

		
		System.out.println("\n--- fmSub");
		/*String fSub = "FM ( ";
		fSub = fSub + " s"+Ed   + ": " + "("+ "rel"+Ed +"|"+ Ed + ")"        		+ ";";
		fSub = fSub + " rel"+Ed + ": " + r +"_s" +Ed               					+ ";";
		fSub = fSub + r+"_s"+Ed + ": " + "("+ Ed+"emb"+Eo +"|"+ Ed+"ref"+Eo + ")"  	+";";
		fSub = fSub + ")";
		*/
		String fSub = "FM ( ";
		//fSub = fSub + " SEnt"   + ": " + "s"+Ed							     		+ ";";
		fSub = fSub + " s"+Ed   + ": " + "("+ "rel"+Ed +"|"+ Ed + ")"        		+ ";";
		fSub = fSub + " rel"+Ed + ": " + r +"_s" +Ed               					+ ";";
		fSub = fSub + r+"_s"+Ed + ": " + "("+ Ed+"emb"+Eo +"|"+ Ed+"ref"+Eo + ")"  	+";";
		fSub = fSub + ")";		
		FeatureModelVariable fmSub = null;
		fmSub = FM ("fmSub" , fSub);						
		//System.out.println("\n \n" + fSub);
		System.out.println(fmSub.toString());
		

		//fmBase.insert(fmSub, fmBase.getFeature("SEnt"), FeatureEdgeKind.MANDATORY);
		fmBase.insert(fmSub, "SEnt", FeatureEdgeKind.MANDATORY);
		
		
		
		
		
		String fmSa = "SEnt:(sA|sB);";
		FeatureModelVariable va = new FeatureModelVariable("SEnt", FMBuilder.getInternalFM(fmSa));
		FeatureVariable fmTest = new FeatureVariable("SEnt", va);
		fmBase.setOr(fmTest);		
		System.out.println("\n*** fmBase after setOr ***\n" + fmBase.toString());	
		
		Variable fmV = new FeatureVariable("SEnt", va);
		fmBase.setFeatureAttribute(fmBase.getFeature("SEnt"), fmBase.getFeature("SEnt").getIdentifier(), fmV);
		
		
		//EorefEd -> sEd;
		/*Expression<String> exp2left = new Expression<String>("SEnt");
		Expression<String> exp2right = new Expression<String>(ExpressionType.OR, "sA", "sB");
		Expression<String> exp1 = new Expression<String>(ExpressionType.FEATURE, exp2left, exp2right);
		 */

		
		String fmS = "SEnt:(sA|sB);";
		FeatureModelVariable v = new FeatureModelVariable("SEnt", FMBuilder.getInternalFM(fmS));
		fmBase.getFeature("SEnt").setFmw(v);
		
		
		
		
		
		System.out.println("\n--- fmBase ");
		//System.out.println("\n*** fmBase ***\n" + fmBase.toString());		
		return fmBase;
	}

	
	//**********   	OPERATIONS WITH FM   *********
	
	
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
        
        //System.out.println("\n\n CONSTRAINS ... \n\n" + cnst);
              
        return cnst;
	}

	public String getConstraints(Set<Expression<String>> constraints){        
		
		String cnst = "";
		
        for (Expression<String> cf : constraints) {
            cnst += cf.toString() + "; \n";
        }        
        
        //System.out.println("\n\n CONSTRAINS ... \n\n" + cnst);
                
        return cnst;
	}
		
	public String getConstraintsFeatureImplies(Set<Expression<String>> constraints, String featureSource){        
		
		String cnst = "";
		
		for (Expression<String> cf : constraints) {
        	if (cf.getLeft().toString().equals(featureSource))
        		cnst += cf.toString() + "; \n";
        		//constraints.remove(cf);
        }        
        
        //System.out.println("\n\n CONSTRAINTS ... \n\n" + cnst );
                
        return cnst;
	}	
	
	//If fRoot is the root, the subfeature will include the constraints
	//If fRoot is not the root, the subfeature discard the constraints
	public FeatureModelVariable extractSubFM(FeatureModelVariable fmv, String fRoot) throws Exception {
		
		FeatureModelVariable fmExtracted = null;
		
		fmExtracted = new FeatureModelVariable ("fmExtracted", fmv.extract(fRoot));
		//fmv.removeFeature("sB"); 												     // Remove the entire branch
		
		return fmExtracted;
	}

	
	//all child of featureRoot will be now an Xor group 
	public FeatureModelVariable insertFMXorMode(FeatureModelVariable fmvBase, 
												FeatureModelVariable fmvSub, 
												String ftIntoInsert,
												String fmvSubStringCnst) throws Exception {
		
		FeatureModelVariable fmvNew = null;
		
		String fmvBaseStringCnst = this.getConstraints(fmvBase);
		fmvBase.removeAllConstraints();
		
		String fmvBaseString 	= fmvBase.toString();
		String fmvSubString 	= fmvSub.toString();
		String ftRootfmvSub 	= fmvSub.root().getFtName();
		
		/*System.out.println("\n\n-*- string of fmvBase:\n" 	+ fmvBaseString);
		System.out.println("\n\n-*- string of fmvSub:\n" 	+ fmvSubString);
		System.out.println("\n\n");
		System.out.println("--- featureToInsert:\t" 	+ ftIntoInsert);
		System.out.println("--- featureRootfmvSub:\t" 		+ ftRootfmvSub);
			*/	
		
		//String ftInToOldExpression = ftIntoInsert+": \"R1_sB\"" ;
		String ftInToOldExpression = null;		
		String[] conds = fmvBaseString.split(";");        
        boolean exp = false;   
        int i = 0;        
        while(!exp && i < conds.length ){
        	ftInToOldExpression = conds[i];        	
        	String ft = ftInToOldExpression.substring(2, conds[i].indexOf(":") ).replace("\"", "") ;
        	if(ft.equals(ftIntoInsert)){ exp = true; }
        	i++;
        }
		
		String ftInToNewExpression = ftIntoInsert+": (";
		Set<String> children = fmvBase.getFeature(ftIntoInsert).children().names();
	    Iterator<String> it = children.iterator();
	    int compteur = 0;
        while ( it.hasNext() ){
        	String child = it.next();
        	if(compteur == 0) ftInToNewExpression += child;
        	else ftInToNewExpression += "|" + child;
        }	    
        ftInToNewExpression += "|" + ftRootfmvSub + ")"; 
        
                
        /*System.out.println("\n\n--- children of ftIntoInsert:\n");		
        System.out.println("  oldFeature:\t" + ftInToOldExpression);
        System.out.println("  newFeature:\t" + ftInToNewExpression);
		*/
        
        
        fmvBaseString = fmvBaseString.replaceFirst(ftInToOldExpression, ftInToNewExpression);        
		//System.out.println("\n\n--- string of fmvBase - feature updated :\n" + fmvBaseString);
		
		fmvBaseString = fmvBaseString.concat("\n" + fmvSubString);
		//System.out.println("\n\n--- string of fmvBase - fmvSub inserted :\n" + fmvBaseString);
		
		fmvNew = this.createFMStrategy01("fmNew",fmvBaseString+fmvBaseStringCnst+fmvSubStringCnst);		
		//System.out.println("\n\n-*- string of fmvNew:\n" + fmvNew);	
		
		
		return fmvNew;
	}
	
	
	//**********   	EXPORT FM   *********
	
	public void exportFMtoXMI(FeatureModelVariable fmv) throws Exception{
		System.out.println("Exporting fm to xmi ...");		
		FeatureModel fm = (FeatureModel) new FMLFeatureModelReader().parseString( fmv.toString() );
		XMIResource xmi = new FMLFeatureModelWriter((org.xtext.example.mydsl.fml.FeatureModel) fm).toXMI("examples/testing/FMs/fm2ecore");
		
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
	

	//**********   	SHOW FM INFORMATION   *********
	
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
	
	public void showConfigurationsTest(FeatureModelVariable fmv) throws Exception{
        System.out.println("\nValid configurations:\n");
        
        //FeatureModelVariable fmvTest = fmv;
        FeatureModelVariable fmvTest = this.createFMStrategy01("fmTest",fmv.toString());
        Set<String> ftsDeselected = null;
        
        Set<Variable> allConfigs = fmv.configs();	 
        for (Variable cf : allConfigs) {
            Set<String> confFts = ((SetVariable) cf).names();
            FeatureConfiguration ftConf = new FeatureConfiguration(confFts, fmv);
            ftsDeselected = ftConf.getDeselectFeatures();
            System.out.println("\t" + confFts + " " + ftConf.getConfMap());            
        }
        
        System.out.println("\nLast configuration:\n");
        System.out.println(" --- fts deselected:");
        System.out.println("\t" + ftsDeselected);        
        
        
        Iterator<String> it = ftsDeselected.iterator();	    
        while ( it.hasNext() ){
        	String ft = it.next();        	
        	fmvTest.removeFeature(ft);
        }
        
        fmvTest.getFeature("sB").setMandatoryStatus();
        fmvTest.getFeature("sA").setMandatoryStatus();
        fmvTest.removeAllConstraints();
        
        System.out.println("\n --- fm pruned - info:");
        //showFMInfo(fmvTest);
		System.out.println();
        System.out.println("Valid model? : \t" + fmvTest.isValid());
        System.out.println("Number of features: \t" + fmvTest.features().size() );
        System.out.println("Number of config: \t" + fmvTest.counting() );  
        
        System.out.println("\n --- fm pruned:\n");
        showFM(fmvTest);        
        //System.out.println();	
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