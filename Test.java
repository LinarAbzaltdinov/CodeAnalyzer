import java.io.*;
import java.util.*;

public class MainClass {

    Integer a;

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        PrintWriter out = null;
        solveD(in, out);
        Integer a;
        Integer b = null;
        Object c = b;
        new Scanner(System.in).close();
        out.close();
        in.close();
        c.toString();
    }

    public static void solveD(Scanner in, PrintWriter out) {
        int n = in.nextInt();
        int first = in.nextInt();
        double min = Double.MIN_VALUE;
        double max = Double.MAX_VALUE;
        for (int i = 1; i < n; ++i) {
            int t = in.nextInt();
            double currentMin = (t - first - 1) * 1.0 / i;
            double currentMax = (t - first + 1) * 1.0 / i;
            if (currentMax < min || currentMin > max) {
                out.println("kukuha");
                return;
            }
            if (currentMin > min) {
                min = currentMin;
            }
            if (currentMax < max) {
                max = currentMax;
            }
        }
        out.printf("%.3f %.3f", min, max);
    }

    public static class NullFactory {

        public static Object getNull() {
            return null;
        }
    }
}