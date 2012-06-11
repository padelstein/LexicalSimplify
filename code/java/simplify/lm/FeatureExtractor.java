package simplify.lm;

import java.util.ArrayList;

import simplify.ParseTree;

public interface FeatureExtractor {
	public ArrayList<Feature> getFeatures(ParseTree tree);
}
