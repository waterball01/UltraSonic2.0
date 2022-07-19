import org.apache.commons.math3.optim.PointValuePair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Main {
    public static void main(final String[] args){
        final Sonic sonic = new Sonic(new Vector3[]{new Vector3(1,0,0), new Vector3(0,0,Math.PI/2)}, new double[]{5.0, 7.0}, new Vector3(4.0,3.0,0.0));
        final Vector3 position = sonic.calcpos();
        System.out.println("Result");
        System.out.println(position);
    }
}
