package dev.brown;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.brown.domain.Solution;
import dev.brown.util.ConstructHeuristics;
import dev.brown.util.InputMaker;
import dev.brown.util.OutputMaker;
import dev.brown.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlannerMain {

    static final Logger logger = LoggerFactory.getLogger(PlannerMain.class);

    public static void main(String[] args) {

        String decodedStr = getDecodedStr(args);

//
//
//        logger.info("encodedStr-->" + encodedStr);
//        String decodedStr = ParameterDecoder.decode(encodedStr);
//        logger.info("decodedStr-->" + decodedStr);
        mainProcess(decodedStr);
    }

    public static void mainProcess(String decodedStr) {
        JsonObject inputObject = JsonParser.parseString(decodedStr).getAsJsonObject();

        Solution solution = InputMaker.makeSolutionClassByInput(inputObject);
        InputMaker.setMatrixManager(inputObject);

        Solution initialSolution = ConstructHeuristics.solve(solution);
        Constants.RIDER_TYPE_SIZE = initialSolution.riderMap().size();
        initialSolution.calculateScore();
        JsonObject output = OutputMaker.convertSolutionToBundles(initialSolution);

//        logger.debug(String.valueOf(initialSolution.totalCost() * -1));

//        output.addProperty("not assigned order count", initialSolution.notAssignedOrderCount());
        output.addProperty("cost", initialSolution.totalCost() * -1);

        System.out.print(output);
    }

    private static String getDecodedStr(String[] args) {
        StringBuilder sb = new StringBuilder();
        for (String arg : args) {
            String encodedStr = Properties.getEnvValue(arg);
            sb.append(encodedStr);
        }
        return sb.toString();
    }
}