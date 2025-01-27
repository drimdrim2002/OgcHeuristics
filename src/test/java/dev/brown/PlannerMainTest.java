package dev.brown;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PlannerMainTest {

    static final Logger log = LoggerFactory.getLogger(PlannerMainTest.class);

    @Test
    public void mainProcessTest() throws Exception {


        String inputFileName = "src/test/resources/input/TEST_K100_1.json";

        PlannerMain.mainProcess(inputFileName, 60);

    }


}