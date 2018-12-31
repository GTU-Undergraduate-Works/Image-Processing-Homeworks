public class Complex {

    private final double real;
    private final double imaginary;


    public Complex(double real, double imag) {
        this.real = real;
        imaginary = imag;
    }


    public String toString() {
        if (imaginary == 0) return real + "";
        if (real == 0) return imaginary + "j";
        if (imaginary < 0) return real + " - " + (-imaginary) + "j";
        return real + " + " + imaginary + "j";
    }

    public Complex plus(Complex b) {
        Complex a = this;
        double real = a.real + b.real;
        double imag = a.imaginary + b.imaginary;
        return new Complex(real, imag);
    }

    public Complex minus(Complex b) {
        Complex a = this;
        double real = a.real - b.real;
        double imag = a.imaginary - b.imaginary;
        return new Complex(real, imag);
    }

    public Complex times(Complex b) {
        Complex a = this;
        double r = a.real * b.real - a.imaginary * b.imaginary;
        double i = a.real * b.imaginary + a.imaginary * b.real;
        return new Complex(r, i);
    }

    public Complex scale(double alpha) {
        return new Complex(alpha * real, alpha * imaginary);
    }

    public Complex conjugate() {
        return new Complex(real, -imaginary);
    }

    public double getReal() {
        return real;
    }

    public double getImaginary() {
        return imaginary;
    }

    public boolean equals(Object x) {
        if (x == null) return false;
        if (this.getClass() != x.getClass()) return false;
        Complex that = (Complex) x;
        return (this.real == that.real) && (this.imaginary == that.imaginary);
    }
}

