package dev.brown;

import com.google.gson.JsonObject;
import dev.brown.alns.ALNSOptimizer;
import dev.brown.alns.parameter.HyperParameter;
import dev.brown.domain.Rider;
import dev.brown.domain.Solution;
import dev.brown.util.ConstructHeuristics;
import dev.brown.util.InputMaker;
import dev.brown.util.JsonFileReader;
import dev.brown.util.OutputMaker;
import dev.brown.util.Validator;
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

        String errorMsg;
        if (inputObject == null) {
            errorMsg = String.format("Input File {%s} is not found", inputFileName);
            throw new FileNotFoundException(errorMsg);
        }


        Solution solution = InputMaker.makeSolutionClassByInput(inputObject);
        InputMaker.setMatrixManager(inputObject, solution);

        Solution initialSolution = ConstructHeuristics.solve(solution);
        initialSolution.calculateScore();

        logger.info("initialSolution cost: {}", initialSolution.totalCost());

        // 1. 하이퍼파라미터 설정
//        HyperParameter params = createHyperParameters();
//        runOptimization(params, initialSolution);




        JsonObject output = OutputMaker.convertSolutionToBundles(initialSolution);




        output.addProperty("cost", initialSolution.totalCost() * -1);

        Validator.validateAll(solution);

        logger.info("output: {}", output);
    }

    private static HyperParameter createHyperParameters() {
        return new HyperParameter.Builder()
            .timeMargin(0.5)
            .rho(0.5f)
            .smoothingRatio(0.01f)
            .minProb(0.05f)
            .maxProb(0.15f)
            .maxDestroy(1000)
            .routeRemovalRatio(0.2f)
            .updatePeriod(100)
            .score1(20)
            .score2(10)
            .score3(2)
            .shawNoise(6.0f)
            .shawD(9.0f)
            .shawT(3.0f)
            .shawL(2.0f)
            .worstNoise(3.0f)
            .alpha1(1.0f)
            .alpha2(0.0f)
            .considerSize(12)
            .wtWeight(0.2f)
            .twWeight(1.0f)
            .useWorst(true)
            .useOld(false)
            .build();
    }

    private static void runOptimization(HyperParameter params, Solution initialSolution) {
        // 최적화 실행 설정
        long seed = System.currentTimeMillis();
        double timeLimit = 300.0; // 5분

        // ALNS 최적화기 생성 및 실행
        ALNSOptimizer optimizer = new ALNSOptimizer(params, seed, initialSolution);
        Solution bestSolution = optimizer.optimize(timeLimit);

        // 결과 출력
        printResults(bestSolution);
    }

    private static void printResults(Solution solution) {
        logger.info("Optimization completed!");
        logger.info("Final cost: " + solution.totalCost());
        logger.info("Number of unassigned orders: " + solution.notAssignedOrderCount());

        // 라이더별 결과 출력
        for (Rider rider : solution.riderMap().values()) {
            logger.info("Rider " + rider.id() + " results:");
            logger.info("  Number of orders: " + rider.orderList().size());
            logger.info("  Route: " + rider.shopIndexList() + " -> " + rider.deliveryIndexList());
            logger.info("  Cost: " + rider.cost());
        }
    }
}