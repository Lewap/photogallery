package org.lewap.photogallery.llm;

public class GenerateOptions {
    private double temperature = 0.2;
    private int maxTokens = 100;

    public GenerateOptions(double temperature, int maxTokens) {
        this.temperature = temperature;
        this.maxTokens = maxTokens;
    }

    public GenerateOptions() {

    }

    public double getTemperature() { return temperature; }
    public int getMaxTokens() { return maxTokens; }

}