package dev.brown;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.brown.domain.Solution;
import dev.brown.util.ConstructHeuristics;
import dev.brown.util.InputMaker;
import dev.brown.util.JsonFileReader;
import dev.brown.util.OutputMaker;
import dev.brown.util.Properties;
import java.io.FileNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlannerMain {

    static final Logger logger = LoggerFactory.getLogger(PlannerMain.class);

    public static void main(String[] args) throws Exception {

        String inputFileName = args[0];
        Integer runningTime = Integer.parseInt(args[1]);


        mainProcess(inputFileName, runningTime);

        
    }

    public static void mainProcess(String inputFileName, Integer runningTime) throws Exception {
        JsonObject inputObject = JsonFileReader.readJsonFile(inputFileName);

        String errorMsg = "";
        if (inputObject == null) {
            errorMsg = String.format("Input File {%s} is not found", inputFileName);
            throw new FileNotFoundException(errorMsg);
        }


        Solution solution = InputMaker.makeSolutionClassByInput(inputObject);
        InputMaker.setMatrixManager(inputObject, solution);

        Solution initialSolution = ConstructHeuristics.solve(solution);
        initialSolution.calculateScore();
        JsonObject output = OutputMaker.convertSolutionToBundles(initialSolution);


        output.addProperty("cost", initialSolution.totalCost() * -1);

        logger.info("output: {}", output);
    }

}