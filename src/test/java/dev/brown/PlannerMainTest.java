package dev.brown;

import java.io.BufferedReader;
import java.io.FileReader;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PlannerMainTest {
    static final Logger log = LoggerFactory.getLogger(PlannerMainTest.class);

    @Test
    public void mainProcessTest() {
        String fileName = "src/test/java/dev/brown/util/decodingTest.txt";

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))) {

            StringBuilder stringBuilder = new StringBuilder();

            String tmp;
            while ((tmp = bufferedReader.readLine()) != null) {
                stringBuilder.append(tmp);
            }

//            log.info(decodedStr);
            String[] args = new String[1];
            args[0] = stringBuilder.toString();
            PlannerMain.main(args);
//            InputMaker.makeSolutionClassByInput(decodedStr);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }



}