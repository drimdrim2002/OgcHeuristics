package dev.brown.util;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class ParameterDecoderTest {
    static final Logger log = LoggerFactory.getLogger(ParameterDecoderTest.class);

    @Test
    void decode() throws FileNotFoundException {

        String fileName = "src/test/java/dev/brown/util/decodingTest.txt";

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))) {

            StringBuilder stringBuilder = new StringBuilder();

            String tmp;
            while ((tmp = bufferedReader.readLine()) != null) {
                stringBuilder.append(tmp);
            }

            String decodedStr = ParameterDecoder.decode(stringBuilder.toString());
            log.info(decodedStr);
            InputMaker.makeSolutionClassByInput(decodedStr);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}