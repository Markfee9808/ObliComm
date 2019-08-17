package Crypto;

/*
 * This is code is used to sample a number from an exponential distribution
 */

public class ExponentialNumberGenerator {
	
	public static int exponentialSample(double lambda) {
		return (int) ((int) -(1 / lambda) * Math.log(Math.random()));
	}

	public static void main(String[] args) {
		for(int i = 0; i < 10000; i++) {
			System.out.println(exponentialSample(0.33333333));
		}
	}
}
