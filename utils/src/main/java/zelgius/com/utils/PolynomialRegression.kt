package zelgius.com.utils

/******************************************************************************
 * Compilation:  javac -cp .:jama.jar PolynomialRegression.java
 * Execution:    java  -cp .:jama.jar PolynomialRegression
 * Dependencies: jama.jar
 *
 * % java -cp .:jama.jar PolynomialRegression
 * 0.01 n^3 + -1.64 n^2 + 168.92 n + -2113.73 (R^2 = 0.997)
 *
 */

import Jama.Matrix
import Jama.QRDecomposition

/**
 * The `PolynomialRegression` class performs a polynomial regression
 * on an set of *N* data points (*y<sub>i</sub>*, *x<sub>i</sub>*).
 * That is, it fits a polynomial
 * *y* = <sub>0</sub> +  <sub>1</sub> *x* +
 * <sub>2</sub> *x*<sup>2</sup> + ... +
 * <sub>*d*</sub> *x*<sup>*d*</sup>
 * (where *y* is the response variable, *x* is the predictor variable,
 * and the <sub>*i*</sub> are the regression coefficients)
 * that minimizes the sum of squared residuals of the multiple regression model.
 * It also computes associated the coefficient of determination *R*<sup>2</sup>.
 *
 *
 * This implementation performs a QR-decomposition of the underlying
 * Vandermonde matrix, so it is neither the fastest nor the most numerically
 * stable way to perform the polynomial regression.
 *
 * @author Robert Sedgewick
 * @author Kevin Wayne
 */
class PolynomialRegression
/**
 * Performs a polynomial reggression on the data points `(y[i], x[i])`.
 *
 * @param x            the values of the predictor variable
 * @param y            the corresponding values of the response variable
 * @param degree       the degree of the polynomial to fit
 * @param variableName the name of the predictor variable
 */
@JvmOverloads constructor(
    x: DoubleArray,
    y: DoubleArray,
    degree: Int,
    private val variableName: String = "n"  // name of the predictor variable
) : Comparable<PolynomialRegression> {
    private var degree: Int = 0                 // degree of the polynomial regression
    private val beta: Matrix                // the polynomial regression coefficients
    private val sse: Double                 // sum of squares due to error
    private var sst: Double = 0.toDouble()                 // total sum of squares

    init {
        this.degree = degree

        val n = x.size
        var qr: QRDecomposition? = null
        var matrixX: Matrix? = null

        // in case Vandermonde matrix does not have full rank, reduce degree until it does
        while (true) {

            // build Vandermonde matrix
            val vandermonde = Array(n) { DoubleArray(this.degree + 1) }
            for (i in 0 until n) {
                for (j in 0..this.degree) {
                    vandermonde[i][j] = Math.pow(x[i], j.toDouble())
                }
            }
            matrixX = Matrix(vandermonde)

            // find least squares solution
            qr = QRDecomposition(matrixX)
            if (qr.isFullRank) break

            // decrease degree and try again
            this.degree--
        }

        // create matrix from vector
        val matrixY = Matrix(y, n)

        // linear regression coefficients
        beta = qr!!.solve(matrixY)

        // mean of y[] values
        var sum = 0.0
        for (i in 0 until n)
            sum += y[i]
        val mean = sum / n

        // total variation to be accounted for
        for (i in 0 until n) {
            val dev = y[i] - mean
            sst += dev * dev
        }

        // variation not accounted for
        val residuals = matrixX!!.times(beta).minus(matrixY)
        sse = residuals.norm2() * residuals.norm2()
    }

    /**
     * Returns the `j`th regression coefficient.
     *
     * @param j the index
     * @return the `j`th regression coefficient
     */
    fun beta(j: Int): Double {
        // to make -0.0 print as 0.0
        return if (Math.abs(beta.get(j, 0)) < 1E-4) 0.0 else beta.get(j, 0)
    }

    /**
     * Returns the degree of the polynomial to fit.
     *
     * @return the degree of the polynomial to fit
     */
    fun degree(): Int {
        return degree
    }

    /**
     * Returns the coefficient of determination *R*<sup>2</sup>.
     *
     * @return the coefficient of determination *R*<sup>2</sup>,
     * which is a real number between 0 and 1
     */
    fun R2(): Double {
        return if (sst == 0.0) 1.0 else 1.0 - sse / sst   // constant function
    }

    /**
     * Returns the expected response `y` given the value of the predictor
     * variable `x`.
     *
     * @param x the value of the predictor variable
     * @return the expected response `y` given the value of the predictor
     * variable `x`
     */
    fun predict(x: Double): Double {
        // horner's method
        var y = 0.0
        for (j in degree downTo 0)
            y = beta(j) + x * y
        return y
    }

    /**
     * Returns a string representation of the polynomial regression model.
     *
     * @return a string representation of the polynomial regression model,
     * including the best-fit polynomial and the coefficient of
     * determination *R*<sup>2</sup>
     */
    override fun toString(): String {
        var s = StringBuilder()
        var j = degree

        // ignoring leading zero coefficients
        while (j >= 0 && Math.abs(beta(j)) < 1E-5)
            j--

        // create remaining terms
        while (j >= 0) {
            if (j == 0)
                s.append(String.format("%.2f ", beta(j)))
            else if (j == 1)
                s.append(String.format("%.2f %s + ", beta(j), variableName))
            else
                s.append(String.format("%.2f %s^%d + ", beta(j), variableName, j))
            j--
        }
        s = s.append("  (R^2 = " + String.format("%.3f", R2()) + ")")

        // replace "+ -2n" with "- 2n"
        return s.toString().replace("+ -", "- ")
    }

    /**
     * Compare lexicographically.
     */
    override fun compareTo(that: PolynomialRegression): Int {
        val EPSILON = 1E-5
        val maxDegree = Math.max(this.degree(), that.degree())
        for (j in maxDegree downTo 0) {
            var term1 = 0.0
            var term2 = 0.0
            if (this.degree() >= j) term1 = this.beta(j)
            if (that.degree() >= j) term2 = that.beta(j)
            if (Math.abs(term1) < EPSILON) term1 = 0.0
            if (Math.abs(term2) < EPSILON) term2 = 0.0
            if (term1 < term2)
                return -1
            else if (term1 > term2) return +1
        }
        return 0
    }

    /**
     * Unit tests the `PolynomialRegression` data type.
     *
     * @param args the command-line arguments
     */
    /*public static void main(String[] args) {
        double[] x = {10, 20, 40, 80, 160, 200};
        double[] y = {100, 350, 1500, 6700, 20160, 40000};
        PolynomialRegression regression = new PolynomialRegression(x, y, 3);

        // Use System.out.println() so that it works with either stdlib.jar or algs4.jar version
        System.out.println(regression);
    }*/
}
/**
 * Performs a polynomial reggression on the data points `(y[i], x[i])`.
 * Uses n as the name of the predictor variable.
 *
 * @param x      the values of the predictor variable
 * @param y      the corresponding values of the response variable
 * @param degree the degree of the polynomial to fit
 */


/*

Copyright © 2000–2017, Robert Sedgewick and Kevin Wayne.
        Last updated: Fri Oct 20 12:50:46 EDT 2017.
        */
