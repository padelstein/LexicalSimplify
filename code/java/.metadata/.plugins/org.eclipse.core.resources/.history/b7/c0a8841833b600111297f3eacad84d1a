package simplify.lm;

import java.io.FileWriter;

import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import simplify.Data;
import simplify.DataReader;
import simplify.ExamplePairReader;
import simplify.ExamplePair;
import simplify.ParseTree;

public class SimpleNormalClassifier {
	private HashMap<String, Integer> featureToIndex = new HashMap<String, Integer>();
	private FeatureComparatorByIndex comparator = new FeatureComparatorByIndex();
	private int featureCount = 1;

	public static void main(String[] args){
		//trainRun();
		generateData();
	}
	
	public static void generateData(){
		SimpleNormalClassifier classifier = new SimpleNormalClassifier();

		ArrayList<FeatureExtractor> extractors = new ArrayList<FeatureExtractor>();
		extractors.add(new SurfaceFeatureExtractor());
		extractors.add(new POSFeatureExtractor());
		extractors.add(new ParseTreeFeatureExtractor());
				
		InputStream in = null;
		OutputStream out = null;
		
		try {
			// normal training data
			in = new GZIPInputStream(new FileInputStream("/Volumes/Data/drk04747/research/simplify/lm/data/classify/normal.classify.train.gz"));
			out = new GZIPOutputStream(
					new FileOutputStream("/Volumes/Data/drk04747/research/simplify/lm/classifier/new_data/normal.train.all.svm.gz"));

			
			classifier.generateSVM(in, false, extractors, true, out);
			
			// simple training data
			in = new GZIPInputStream(new FileInputStream("/Volumes/Data/drk04747/research/simplify/lm/data/classify/simple.classify.train.gz"));
			out = new GZIPOutputStream(
					new FileOutputStream("/Volumes/Data/drk04747/research/simplify/lm/classifier/new_data/simple.train.all.svm.gz"));

			
			classifier.generateSVM(in, true, extractors, true, out);
			
			// normal testing data
			in = new GZIPInputStream(new FileInputStream("/Volumes/Data/drk04747/research/simplify/lm/data/classify/normal.classify.test.gz"));
			out = new GZIPOutputStream(
					new FileOutputStream("/Volumes/Data/drk04747/research/simplify/lm/classifier/new_data/normal.test.all.svm.gz"));

			
			classifier.generateSVM(in, false, extractors, false, out);

			// simple training data
			in = new GZIPInputStream(new FileInputStream("/Volumes/Data/drk04747/research/simplify/lm/data/classify/simple.classify.test.gz"));
			out = new GZIPOutputStream(
					new FileOutputStream("/Volumes/Data/drk04747/research/simplify/lm/classifier/new_data/simple.test.all.svm.gz"));
			
			classifier.generateSVM(in, true, extractors, false, out);			
		} catch (IOException e) {
			throw new RuntimeException("Problem opening file\n" + e);
		}		
	}
	
	public static void trainRun(){
		SimpleNormalClassifier classifier = new SimpleNormalClassifier();

		ArrayList<FeatureExtractor> extractors = new ArrayList<FeatureExtractor>();
		//extractors.add(new SurfaceFeatureExtractor());
		extractors.add(new POSFeatureExtractor());
		//extractors.add(new ParseTreeFeatureExtractor());
		
		//classifier.generateSVMTrain(Data.PARSE_FILE, Data.ALIGN_FILE, extractors,
		//		"/Volumes/Data/drk04747/research/simplify/lm/classifier/temp.pos.svm" );
		
		InputStream in = null;
		OutputStream out = null;
		
		try {
			in = new GZIPInputStream(new FileInputStream("/Volumes/Data/drk04747/research/simplify/lm/data/classify/normal.classify.train.gz"));
			out = new GZIPOutputStream(
					new FileOutputStream("/Volumes/Data/drk04747/research/simplify/lm/classifier/new_data/normal.train.pos.svm.gz"));

		} catch (IOException e) {
			throw new RuntimeException("Problem opening file\n" + e);
		}		
		
		//classifier.generateSVMTrain(in, false, extractors, out);
	}
	
	// just grabs the text from the files
	public static void temp(){
		InputStream in = null;
		PrintWriter out = null;
		
		try {
			in = new GZIPInputStream(new FileInputStream(Data.NORMAL_GZ));
			out = new PrintWriter(new GZIPOutputStream(
					new FileOutputStream("/Volumes/Data/drk04747/research/simplify/lm/classifier/normal.txt.gz")));

			DataReader reader = new DataReader(in);
			
			while( reader.hasNext() ){
				ParseTree next = reader.next();
				
				if( next == null ){
					out.println("");
				}else{
					out.println(next.textString());
				}
			}
			
			out.close();
			
		} catch (IOException e) {
			throw new RuntimeException("Problem opening file\n" + e);
		}		

	}

	public void generateSVM(InputStream stream, boolean simple, ArrayList<FeatureExtractor> extractors, boolean train, OutputStream outStream){
		DataReader reader = new DataReader(stream);
		
		PrintWriter out = new PrintWriter(outStream);
	
		while( reader.hasNext() ){
			ParseTree next = reader.next();
			
			if( next != null ){		
				if(simple){
					out.println("1 " + getFeatureLine(extractors, next, train));
				}else{
					out.println("-1 " + getFeatureLine(extractors, next, train));
				}
			}else{
				out.println("");
			}
		}
		
		out.close();
	}
	
	public void generateSVM(String parseFile, String alignFile, ArrayList<FeatureExtractor> extractors, boolean train, String output){
		ExamplePairReader reader = new ExamplePairReader(parseFile, alignFile);

		PrintWriter out = null;

		try {
			out = new PrintWriter(new FileWriter(output));
		} catch (IOException e) {
			throw new RuntimeException("Problems opening file: " + output);
		}

		while( reader.hasNext() ){
			ExamplePair pair = reader.next();
						
			// for now, don't output examples where the normal and simple are the same
			if( !pair.getSimple().textString().equalsIgnoreCase(pair.getNormal().textString())){

				// positive (simple) examples
				//out.println(pair.getSimple().textString());
				out.println("1 " + getFeatureLine(extractors, pair.getSimple(), train));
				//out.println(pair.getNormal().textString());
				out.println("-1 " + getFeatureLine(extractors, pair.getNormal(), train));
			}
		}

		out.close();
	}

	private String getFeatureLine(ArrayList<FeatureExtractor> extractors, ParseTree tree, boolean addNew){
		ArrayList<Feature> features = new ArrayList<Feature>();

		for( FeatureExtractor ex: extractors ){
			for( Feature f: ex.getFeatures(tree) ){
				// if we're adding features, add all of the indices, otherwise, remove them
				if( !featureToIndex.containsKey(f.getLabel()) && !addNew ){
					// don't do anything
				}else{
					if( !featureToIndex.containsKey(f.getLabel()) ){
						// add it (addNew must be true)
						featureToIndex.put(f.getLabel(), featureCount);
						featureCount++;
					}
					
					features.add(f);
				}
			}
		}


		// sort the features by index
		Collections.sort(features, comparator);

		return getFeatureLine(features, addNew);
	}

	private String getFeatureLine(ArrayList<Feature> features, boolean addNew ){
		StringBuffer buffer = new StringBuffer();

		for( Feature f: features ){
			buffer.append(featureToIndex.get(f.getLabel()) + ":" + f.getValue() + " ");
		}

		if( buffer.length() == 0 ){
			return "";
		}else{
			// remove the trailing whitespace and return
			return buffer.substring(0, buffer.length()-1);
		}
	}

	private class FeatureComparatorByIndex implements Comparator<Feature>{
		//private HashMap<String, Integer> featureToIndex;

		//public FeatureComparatorByIndex(HashMap<String, Integer> featureToIndex){
		//	this.featureToIndex = featureToIndex;
		//}
		public int compare(Feature f1, Feature f2) {
			int index1 = featureToIndex.get(f1.getLabel());
			int index2 = featureToIndex.get(f2.getLabel());

			if( index1 == index2 ){
				return 0;
			}else if( index1 < index2 ){
				return -1;
			}else{
				return 1;
			}
		}
	}
}