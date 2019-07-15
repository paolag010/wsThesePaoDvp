

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

import fr.familiar.FMLTest;
import fr.familiar.parser.FMBuilder;
import fr.familiar.variable.FeatureModelVariable;


import fr.familiar.gui.FamiliarTool;
import fr.familiar.interpreter.FMLShell;
import fr.familiar.variable.Comparison;
import fr.familiar.variable.SetVariable;
import fr.familiar.variable.Variable;
import gsd.synthesis.BDDBuilder;



public class HelloWorldFM{


	public String fmHelloWorld() throws Exception {
        FeatureModelVariable fmv = FM ("fm1", "FM (A : [B] [C] ;)"); // B and C are optional features of A (root)    
		FeatureModelVariable fmv2 = new FeatureModelVariable ("fm2", FMBuilder.getInternalFM("A : [B] [C] ;"));      
        return "coucou! " + fmv2.counting();        
    }

}
