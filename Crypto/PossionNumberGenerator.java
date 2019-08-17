package Crypto;

/*
 * This is code is used to sample a number from a Poisson distribution
 */

public class PossionNumberGenerator {
	public static int poissonSample(double lambda) {
		double L = Math.exp(-lambda);
		double p = 1.0;
		int k = 0;
		do {
			k++;
			p *= Math.random();
		} while (p > L);
		return k - 1;
	}
}
