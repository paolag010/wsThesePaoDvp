package helloWorldFamiliarJava;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

import static org.junit.Assert.*;
import fr.familiar.FMLTest;
import fr.familiar.FeatureModelLoader;
import fr.familiar.interpreter.FMLShell;
import fr.familiar.parser.FMBuilder;
import fr.familiar.variable.FeatureModelVariable;
import fr.familiar.variable.Comparison;
import fr.familiar.variable.SetVariable;
import fr.familiar.variable.Variable;
import gsd.synthesis.BDDBuilder;
import fr.familiar.parser.FMLCommandInterpreter;
import fr.familiar.interpreter.VariableNotExistingException;
import fr.familiar.parser.VariableAmbigousConflictException;

import org.apache.log4j.Logger;


public class HelloWorldFM{	
	
    private final FMLShell _shell;
    private final FMLCommandInterpreter _environment;
    private boolean hasBeenParsed;
    private static final Logger log = Logger.getLogger(HelloWorldFM.class);
    private static HelloWorldFM instance;
	
    public HelloWorldFM(){
        _shell = FMLShell.instantiateStandalone(System.in);
         _environment = _shell.getCurrentEnv();
        _shell.setVerbose(false);
        log.debug("Environment du shell :"+_environment);
        hasBeenParsed = false;
    }

    public static  HelloWorldFM getInstance() {
        if(instance==null)
            instance = new HelloWorldFM();
        instance.getShell().printFMLHeader();
        return instance;
    }

    /**
     * Reset the environment
     */
    public  void clearInterpreter() {
        this._shell.reset();
    }	

    public  void eval(String instr) throws FMEngineException {
        log.debug(instr);
        _shell.parse(instr);

        if (_shell.hasFatalErrors()) {
            //throw new FMLFatalError(_shell.getFatalErrors());
            throw new FMEngineException("Fatal error :"+_shell.getFatalErrors().toString());
        }

        if (_shell.hasAssertionErrors()) {
            hasBeenParsed = true; // considering that assertion does not break
            // the parsing process
            //throw new FMLAssertionError(_shell.getAssertionErrors());
            throw new FMEngineException("Assertion error :"+_shell.getFatalErrors().toString());
        }
    }
    
    
	public String fmHelloWorld()
	{
       // FeatureModelVariable fm = new v =  FM ("fm1", "FM (A : [B] [C] ;)"); // B and C are optional features of A (root)    
       // FeatureModelVariable fmv =  new FeatureModelVariable("fm1", A: B C [D]; C: (X|Y); D: (Q|T|R)+;);
        //FeatureModelVariable fmv2 = new FeatureModelVariable ("fm2", FMBuilder.getInternalFM("A : [B] [C] ;"));   
		 		String FMA = "fma = FM(A: B C [D]; C: (X|Y); D: (Q|T|R)+;)";
		//FeatureModel<String> FMID = "FM (A : [B] [C] ;)";
        //FeatureModelVariable fmv = new FeatureModelVariable("ff", (A : [B] [C] ;));
        
        
		 return "coucou! " ;        
	 }
	
	public String fmHelloWorld(FeatureModelVariable fmv)
	{
		System.out.println("Export de FMA");
		
			Iterator it1 = fmv.getGroups().iterator();
			it1 = fmv .getAllConstraints().iterator();
			while(it1.hasNext()) {
				Variable f = (Variable)it1.next();
				System.out.println(f.toString());
			}
		
			
			System.out.println("Export de FMA");
			
		    Iterator it = fmv.configs().iterator();
		    while(it.hasNext()) {
		    	//System.out.println(it);		    	
		    	//it.next();
		    	
		    	Variable f = (Variable)it.next();	    	
		    	System.out.println(f.toString());
		    	
		      }
	
		 return "coucou! " + fmv.counting();       
		 
	 }	
	
	  /**
     * @param id identifier of a FeatureModel variable
     * @return a variable of type feature model whose identifier is id in the
     *         current environment
     * @throws VariableAmbigousConflictException
     * @throws VariableNotExistingException
     */
    public  FeatureModelVariable getFMVariable(String id) throws VariableNotExistingException, VariableAmbigousConflictException {
        Variable v = _environment.getVariable(id);
        assertNotNull(v);
        assertTrue(v instanceof FeatureModelVariable);
        return (FeatureModelVariable) v;

    }
    
    FMLShell getShell() { return _shell; }

    public  void setVerbose(boolean b) { _shell.setVerbose(b); }

    	
}
