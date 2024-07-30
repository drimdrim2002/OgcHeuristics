package dev.brown;

import dev.brown.util.Properties;
import java.io.BufferedReader;
import java.io.FileReader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatcher.*;
import org.mockito.ArgumentMatchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mockito.Mockito;
class PlannerMainTest {
    static final Logger log = LoggerFactory.getLogger(PlannerMainTest.class);

//    @BeforeAll
//    public static void setup() {
//        Mockito.mockStatic(Properties.class);
//        String fileName = "src/test/java/dev/brown/util/decodingTest.txt";
//        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))) {
//
//            StringBuilder stringBuilder = new StringBuilder();
//
//            String tmp;
//            while ((tmp = bufferedReader.readLine()) != null) {
//                stringBuilder.append(tmp);
//            }
//
//            Mockito.when(Properties.getEnvValue(ArgumentMatchers.anyString()))
//                .thenReturn(stringBuilder.toString());
//
//
//
//
////            InputMaker.makeSolutionClassByInput(decodedStr);
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//        }
//    }
    @Test
    public void mainProcessTest() {

//        String[] args = new String[1];
//        args[0] = "alpha_input";
//        PlannerMain.main(args);
    }



}