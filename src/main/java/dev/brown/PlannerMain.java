package dev.brown;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.brown.domain.Solution;
import dev.brown.util.ConstructHeuristics;
import dev.brown.util.InputMaker;
import dev.brown.util.OutputMaker;
import dev.brown.util.ParameterDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlannerMain {

    static final Logger logger = LoggerFactory.getLogger(PlannerMain.class);

    public static void main(String[] args) {

//        logger.debug(args[0]);
        String decodedStr = ParameterDecoder.decode(args[0]);
        JsonObject inputObject = JsonParser.parseString(decodedStr).getAsJsonObject();

        Solution solution = InputMaker.makeSolutionClassByInput(inputObject);
        InputMaker.setMatrixManager(inputObject);

        Solution initialSolution = ConstructHeuristics.solve(solution);
        initialSolution.calculateScore();
        JsonObject output = OutputMaker.convertSolutionToBundles(initialSolution);
        output.addProperty("not assigned order count", initialSolution.notAssignedOrderCount());
        output.addProperty("cost", initialSolution.totalCost() * -1);

        System.out.print(output);
    }
}