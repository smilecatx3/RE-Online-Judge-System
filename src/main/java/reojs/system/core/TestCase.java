package reojs.system.core;

import java.util.List;


public class TestCase {
    private List<String> inputs;
    private String output;


    TestCase(List<String> inputs, String output) {
        this.inputs = inputs;
        this.output = output;
    }

    public List<String> getInputs() {
        return inputs;
    }

    public String getOutput() {
        return output;
    }

    @Override
    public String toString() {
        return String.format("TestCase{inputs: %s; output: %s}", inputs, output);
    }
}
