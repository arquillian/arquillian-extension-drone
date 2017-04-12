package org.jboss.arquillian.drone.webdriver.binary.process;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Pattern;
import org.arquillian.spacelift.process.OutputTransformer;
import org.arquillian.spacelift.process.ProcessInteraction;
import org.jboss.arquillian.drone.webdriver.utils.Validate;

/**
 * Creates a Spacelift interaction for process that are usually tight with some specific output and for which you can
 * define which output should be printed to stdout
 */
public class BinaryInteraction {

    private List<Pattern> allowedOutput;
    private OutputTransformer transformer;
    private Pattern expectedPattern;
    private CountDownLatch countDownLatch;

    /**
     * Creates empty interaction builder
     */
    public BinaryInteraction() {
        allowedOutput = new ArrayList<Pattern>();
        transformer = output -> {
            checkCountDownLatch(output.toString());
            return output;
        };
    }

    /**
     * Defines an interaction when {@code pattern} is matched
     *
     * @param pattern
     *     the line
     *
     * @return current instance to allow chaining
     */
    public BinaryInteraction.MatchedOutputProcessInteractionBuilder when(String pattern) {
        this.expectedPattern = Pattern.compile(pattern);
        return new BinaryInteraction.MatchedOutputProcessInteractionBuilder();
    }

    public BinaryInteraction printToOut(String pattern) {
        allowedOutput.add(Pattern.compile(pattern));
        return BinaryInteraction.this;
    }

    /**
     * Defines a prefix for standard output and standard error output. Might be {@code null} or empty string,
     * in such case no prefix is added and process outputs cannot be distinguished
     *
     * @param prefix
     *     the prefix
     *
     * @return current instance to allow chaining
     */
    public BinaryInteraction outputPrefix(final String prefix) {
        if (!Validate.empty(prefix)) {
            transformer = output -> {
                checkCountDownLatch(output.toString());
                return output.prepend(prefix);
            };
        }
        return this;
    }

    private void checkCountDownLatch(String string) {
        if (countDownLatch != null && countDownLatch.getCount() > 0 && !Validate.empty(string)) {
            if (string.matches(expectedPattern.toString())) {
                countDownLatch.countDown();
            }
        }
    }

    /**
     * Builds {@link ProcessInteraction} object from defined data
     *
     * @return {@link ProcessInteraction}
     */
    public ProcessInteraction build() {
        return new BinaryInteraction.ProcessInteractionImpl(transformer, allowedOutput);
    }

    private static class ProcessInteractionImpl implements ProcessInteraction {

        private final List<Pattern> allowedOutput;
        private final OutputTransformer transformer;

        ProcessInteractionImpl(OutputTransformer outputTransformer, List<Pattern> allowedOutput) {
            this.transformer = outputTransformer;
            this.allowedOutput = allowedOutput;
        }

        @Override
        public List<Pattern> allowedOutput() {
            return allowedOutput;
        }

        @Override
        public List<Pattern> errorOutput() {
            return new ArrayList<>();
        }

        @Override
        public Map<Pattern, String> replyMap() {
            return new LinkedHashMap<>();
        }

        @Override
        public List<Pattern> terminatingOutput() {
            return new ArrayList<>();
        }

        @Override
        public String textTypedIn() {
            return "";
        }

        @Override
        public OutputTransformer transformer() {
            return transformer;
        }
    }

    /**
     * Definition of allowed actions when output is matched
     */
    public class MatchedOutputProcessInteractionBuilder {

        /**
         * Counts down the latch (if not null and is bigger than zero)
         *
         * @param countDownLatch
         *     A {@link CountDownLatch} to be counted down
         *
         * @return current instance to allow chaining
         */
        public BinaryInteraction thenCountDown(CountDownLatch countDownLatch) {
            BinaryInteraction.this.countDownLatch = countDownLatch;
            return BinaryInteraction.this;
        }
    }
}
