public class Fibonacci {
	// This is a class containing the methods to compute
	// Fibonacci of N
	public Fibonacci() {
		super();
		
	}

	public long computeFibonacciIterative(long n) {
		/************************************************
		 * This method uses iterative solution to compute
		 * the Fibonacci number of n.
		 * 
		 * Given: <int> n - The input integer number >= 0.
		 * Returns: <int> - The Fibonacci number of n
		 * 
		 * This piece of code comes from:
		 * "http://www.codeproject.com/Articles/21194/
		 * Iterative-vs-Recursive-Approaches"
		 ************************************************/
		if (n == 0) return 0;
	    if (n == 1) return 1;
	        
	    long prevPrev = 0;
	    long prev = 1;
	    long result = 0;
	        
	    for (long i = 2; i <= n; i++)
	    {
	        result = prev + prevPrev;
	        prevPrev = prev;
	        prev = result;
	    }
	    return result;
	}
}