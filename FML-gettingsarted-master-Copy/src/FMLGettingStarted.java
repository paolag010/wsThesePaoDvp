import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

import fr.familiar.FMLTest;
import fr.familiar.parser.FMBuilder;
import fr.familiar.variable.Comparison;
import fr.familiar.variable.FeatureModelVariable;
import fr.familiar.variable.SetVariable;
import fr.familiar.variable.Variable;

/**
 *  
 * Created by macher1 on 28/11/2017.
 */
public class FMLGettingStarted extends FMLTest {   
	
	@Test
    public void testHelloWorld() throws Exception {
        FeatureModelVariable fmv = FM ("fm1", "FM (A : [B] [C] ;)"); // B and C are optional features of A (root)        
        assertEquals(4.0, fmv.counting(), 0.0);

        // alternate way to build a feature model
        FeatureModelVariable fmv2 = new FeatureModelVariable ("fm2", FMBuilder.getInternalFM("A : [B] [C] ;"));
        assertEquals(Comparison.REFACTORING, fmv2.compare(fmv));        
    }
    
}