package helloWorldFamiliarJava;

import fr.familiar.interpreter.VariableNotExistingException;
import fr.familiar.parser.VariableAmbigousConflictException;
import fr.familiar.variable.FeatureModelVariable;

public class HelloWorldFMmain {

	public static void main(String[] args) {
		
		HelloWorldFM hw = new HelloWorldFM();
		
		HelloWorldFM fam_pilot = HelloWorldFM.getInstance(); 
        System.out.println("Instantiation of HelloWorldFM"); 
        System.out.println("%%%");
        String FMA = "fma = FM(A: B C [D]; C: (X|Y); D: (Q|T|R)+;)"; 
        try {
			fam_pilot.eval(FMA);
		} catch (FMEngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
        System.out.println("Eval de FMA"); 
        try {
			FeatureModelVariable fmav = fam_pilot.getFMVariable("fma");
			System.out.println(hw.fmHelloWorld(fmav));	        
		} catch (VariableNotExistingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (VariableAmbigousConflictException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
        
        
	}

}
