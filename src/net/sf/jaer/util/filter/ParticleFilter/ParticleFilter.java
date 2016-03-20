/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jaer.util.filter.ParticleFilter;

/**
 *
 * @author minliu and hongjie
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

public class ParticleFilter<T extends Particle> {
	private ParticleEvaluator<T, Double> estimateEvaluator; 
        private ParticleEvaluator<T, T> dynamicEvaluator;
	private ArrayList<ParticleWeight<T> > particles = new ArrayList<ParticleWeight<T> >();
	private double[] selectionSum = new double[0];
	private int nextParticleCount=0;
	private boolean useWeightRatio = false;
	private boolean recalculateWeightAfterDrift = false;
	private Comparator<ParticleWeight<T> > strengthComparator = new Comparator<ParticleWeight<T> >() {
		public int compare(ParticleWeight<T> arg0, ParticleWeight<T> arg1) {
			double s0 = getSelectionWeight(arg0);
			double s1 = getSelectionWeight(arg1);
			if( s0 < s1) return -1;
			else if( s0 > s1) return +1;
			else return 0;
		}
	};

	public ParticleFilter(ParticleEvaluator<T, T> dynamic, ParticleEvaluator<T, Double> measurement) {
		this.estimateEvaluator = measurement;
                this.dynamicEvaluator = dynamic;
	}
        
	public void addParticle(T p) {
		this.particles.add(new ParticleWeight<T>(p));
		nextParticleCount++;
	}

	public int getParticleCount() {
		return particles.size();
	}

	public T get(int i) {
		return particles.get(i).data;
	}

	public void evaluateStrength() {
		for(ParticleWeight<T> p : this.particles) {
                        p.data = dynamicEvaluator.evaluate(p.data);
			double weight = estimateEvaluator.evaluate(p.data);
			if( p.lastWeight == 0 ) {
				p.weightRatio = weight;
			} else {
				p.weightRatio = weight / p.lastWeight;
			}
			p.weight = weight;
		}
	}

	@SuppressWarnings("unchecked")
	public void resample(Random r) {
		double sum = prepareResampling();
		int[] selectionDistribution = new int[this.particles.size()];
		ArrayList<ParticleWeight<T> > nextDistribution = new ArrayList<ParticleWeight<T> >();
		for(int i = 0; i < nextParticleCount; i++) {
			double sel = sum*r.nextDouble();
			int index = Arrays.binarySearch(this.selectionSum, sel);
			if( index < 0 ) {
				index = -(index+1);
			}
//			System.out.print(index + " ");
			ParticleWeight<T> p = particles.get(index);
			ParticleWeight<T> particleWeight = new ParticleWeight<T>((T)p.data.clone(), p.weight, selectionDistribution[index]);
			nextDistribution.add(particleWeight);
			selectionDistribution[index]++;
		}
//		System.out.println();
		this.particles = nextDistribution;
	}

	private double prepareResampling() {
		Collections.sort(this.particles, strengthComparator);
		this.selectionSum = new double[getParticleCount()];
		double sum = 0;
		for(int i = 0; i < particles.size(); i++) {
			ParticleWeight<T> p = particles.get(i);
			sum += getSelectionWeight(p);
			this.selectionSum[i] = sum;
		}
		return sum;
	}

	public void disperseDistribution(Random r, double spread) {
		for(ParticleWeight<T> p : this.particles) {
			// do not add error to one copy of the particle
			if( p.copyCount > 0 ) {
				p.data.addNoise(r, spread);
				if( recalculateWeightAfterDrift ) {
					// The weight ratio depends on small changes in strength after noise is added.
					// The filter can be made more accurate by finding the exact strength of the new particle for the previous timestep.
					p.lastWeight = this.estimateEvaluator.evaluate(p.data);
				}
			}
		}
	}
	
	public void setParticleCount(int value) {
		this.nextParticleCount = value;
	}

	public boolean isUsingWeightRatio() { return useWeightRatio; }
	public void useWeightRatio(boolean b) { useWeightRatio = b; }
	
	private double getSelectionWeight(ParticleWeight<T> p) {
		if( useWeightRatio )	return p.weightRatio;
		else return p.weight;
	}
	
	private static class ParticleWeight<T extends Particle> implements Cloneable {
		public ParticleWeight(T p) {
			this(p, 1.0, 0);
		}
		public ParticleWeight(T p, double lastWeight, int copyCount) {
			this.data = p;
			this.lastWeight = lastWeight;
			this.weight = 1.0;
			this.weightRatio = 1.0;
			this.copyCount = copyCount;
		}
		T data;
		double lastWeight;
		double weight;
		double weightRatio;
		int copyCount;
	}

	public void setEvaluator(ParticleEvaluator<T, T>dynamic, ParticleEvaluator<T, Double> measurement) {
		this.estimateEvaluator = measurement;
                this.dynamicEvaluator = dynamic;
        }

	public void setReevaluateAfterNoise(boolean b) {
		this.recalculateWeightAfterDrift = b;
	}
	
}
