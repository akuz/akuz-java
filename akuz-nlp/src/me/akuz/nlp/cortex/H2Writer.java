package me.akuz.nlp.cortex;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import me.akuz.core.Index;
import me.akuz.core.Pair;
import me.akuz.core.PairComparator;
import me.akuz.core.SortOrder;
import me.akuz.core.sort.SelectK;

public final class H2Writer {
	
	public final int generate(double[] probs) {
		
		double value = ThreadLocalRandom.current().nextDouble();
		double sum = 0.0;
		for (int i=0; i<probs.length; i++) {
			sum += probs[i];
			if (sum >= value) {
				return i;
			}
		}
		return probs.length - 1;
	}
	
	public void write(final List<H2Layer> layers, final Index<Character> charIndex) {
		
		for (int layerIndex=0; layerIndex<layers.size(); layerIndex++) {
			
			final H2Layer layer = layers.get(layerIndex);
			
			System.out.println("=========================== LAYER " + layerIndex);
			
			final List<Pair<Integer, Double>> sortedFeatures = new ArrayList<>();
			for (int f=0; f<layer.getFeatureDim(); f++) {
				sortedFeatures.add(new Pair<Integer, Double>(f, layer.getFeatureProbs()[f]));
			}
			Collections.sort(sortedFeatures, new PairComparator<Integer, Double>(SortOrder.Desc));
			
			DecimalFormat fmt = new DecimalFormat("0.000");
			
			for (int i=0; i<sortedFeatures.size(); i++) {
				
				Pair<Integer, Double> pair = sortedFeatures.get(i);
				Integer featureIndex = pair.v1();
				Double featureProb = pair.v2();
				
				System.out.println("---------------------------");
				System.out.println("#" + (i+1) + ", f: " + featureIndex + ", p: " + fmt.format(featureProb));
				
				PChar[] featureChars = new PChar[] { new PChar(layer.getFeatureDim(), featureIndex)};
				PWord featureWord = new PWord(layer.getFeatureDim(), featureChars);
				for (int nextLayerIndex=layerIndex; nextLayerIndex>=0; nextLayerIndex--) {
					featureWord = layers.get(nextLayerIndex).fromFeaturesToData(featureWord);
				}
				write(featureWord, charIndex);
			}
		}
	}

	
	public void write(final PWord word, final Index<Character> charIndex) {
		
		if (word.getCharDim() != charIndex.size()) {
			throw new IllegalArgumentException(
					"PWord char dimensionality " +
					"doesn't match charIndex dim");
		}
		
		DecimalFormat fmt = new DecimalFormat("0.000");
		
		List<List<Pair<Character, Double>>> top = new ArrayList<>();
		int maxTopSize = 0;
		for (int i=0; i<word.size(); i++) {
			
			final PChar ch = word.getChar(i);
			final SelectK<Character, Double> select = new SelectK<>(SortOrder.Desc, 10);
			for (int d=0; d<ch.getDim(); d++) {
				final double prob = ch.getProb(d);
				if (prob >= 0.001) {
					select.add(new Pair<>(charIndex.getValue(d), ch.getProb(d)));
				}
			}
			final List<Pair<Character, Double>> selected = select.get();
			if (maxTopSize < selected.size()) {
				maxTopSize = selected.size();
			}
			top.add(selected);
		}
		
		for (int r=0; r<maxTopSize; r++) {
			for (int c=0; c<top.size(); c++) {
				
				final List<Pair<Character, Double>> selected = top.get(c);
				
				if (r < selected.size()) {
					System.out.print("  | ");
					System.out.print(selected.get(r).v1());
					System.out.print(" ");
					System.out.print(fmt.format(selected.get(r).v2()));
				} else {
					System.out.print("  | ");
					System.out.print(" ");
					System.out.print(" ");
					System.out.print("    ");
				}
			}
			System.out.println();
		}
	}


}
