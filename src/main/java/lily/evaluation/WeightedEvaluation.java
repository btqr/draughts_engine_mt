package lily.evaluation;

public interface WeightedEvaluation extends Evaluation {
    void setWeights(double[] weights);

    double[] getWeights();
}
