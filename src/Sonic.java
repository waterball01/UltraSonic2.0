import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;

import java.util.*;

import static java.lang.Math.*;

public class Sonic {

    public Vector3[] sens_poses;
    public double[] distances;
    public double x;
    public double y;
    public double alpha;
    public double h = 10.0;
    public double w = 10.0;
    public PointValuePair result;
    HashMap<String,PointValuePair> pos = new HashMap<String,PointValuePair>();
    Vector3 position;

    public Sonic(final Vector3[] sens_poses, final double[] distances, final Vector3 position){
        this.sens_poses = sens_poses;
        this.distances = distances;
        this.position = position;
    }
    public MultivariateFunction calcy(Vector3 sens_pos, double h, double distance){
        MultivariateFunction d_y = new MultivariateFunction() {
            @Override
            public double value(double[] doubles) {
                y = doubles[0];
                alpha = doubles[1];
                double g = pow(((h - y - sens_pos.y()*sin(alpha) - sens_pos.x()*cos(alpha))/sin(sens_pos.theta()+alpha) - distance),2.0);
                return g;
            }
        };
        return d_y;
    }
    public MultivariateFunction calcx(Vector3 sens_pos, double w, double distance){
        MultivariateFunction d_x = new MultivariateFunction() {
            @Override
            public double value(double[] doubles) {
                x = doubles[0];
                alpha = doubles[1];
                double g = pow(((w - x + sens_pos.y()*sin(alpha) - sens_pos.x()*cos(alpha))/cos(sens_pos.theta()+alpha) - distance),2.0);
                return g;
            }
        };
        return d_x;
    }

    public Vector3 calcpos(){
        for (int i = 0; i < sens_poses.length; i++) {
            List<MultivariateFunction> d = new ArrayList<MultivariateFunction>();
            List<PointValuePair> results = new ArrayList<PointValuePair>();
            Vector3 sens_pos = sens_poses[i];
            double distance = distances[i];
            double u = 0.0;
            double e = 0.0;
            MultivariateFunction d_y1 = calcy(sens_pos, h, distance);
            MultivariateFunction d_y2 = calcy(sens_pos, 0, distance);
            MultivariateFunction d_x1 = calcx(sens_pos, w, distance);
            MultivariateFunction d_x2 = calcx(sens_pos, 0, distance);
            d.add(d_y1);
            d.add(d_y2);
            d.add(d_x1);
            d.add(d_x2);
            int l = 0;
            e = position.theta();
            for (MultivariateFunction dist : d) {
                NelderMeadSimplex nelder = new NelderMeadSimplex(new double[][]{
                        {0.1, 0.1},
                        {-0.1, -0.1},
                        {-0.1, 0.1}

                });
                Comparator c = new Comparator() {
                    @Override
                    public int compare(Object o1, Object o2) {
                        PointValuePair d1 = (PointValuePair) o1;
                        PointValuePair d2 = (PointValuePair) o2;
                        if (d1.getValue() == d2.getValue()) {
                            return 0;
                        } else if (d1.getValue() > d2.getValue()) {
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                };
                switch(l){
                    case 0:
                    case 1:
                        u = position.y();
                        break;
                    case 2:
                    case 3:
                        u = position.x();
                        break;
                }
                nelder.build(new double[]{u, e});
                nelder.evaluate(dist, c);
                double val = 0.0;
                int j = 0;
                PointValuePair ans = null;
                for (PointValuePair p : nelder.getPoints()) {
                    if (j == 0) {
                        val = p.getValue();
                        ans = p;
                    } else {
                        if (val > p.getValue()) {
                            val = p.getValue();
                            ans = p;
                        }

                    }
//                    System.out.println("Values");
//                    System.out.println(val);
//                    System.out.println("Points");
//                    System.out.println(Arrays.toString(p.getPoint()));
                    j++;
                }
                results.add(ans);
                l++;
//                System.out.println("Least value for each equation");
//                System.out.println(Arrays.toString(ans.getPoint()));
//                System.out.println(ans.getValue());

            }
            int q = 0;
            int a = 0;
            for (PointValuePair result : results) {
                if (a == 0){
                    this.result = result;
                }
                else {
                    if (result.getValue() < this.result.getValue()){
                        this.result = result;
                        q = a;
                    }
                }
                a++;
            }
            switch(q){
                case 0:
//                    System.out.println("y_1");
                    pos.put("y_1",result);
                    break;
                case 1:
//                    System.out.println("y_2");
                    pos.put("y_2",result);
                    break;
                case 2:
//                    System.out.println("x_1");
                    pos.put("x_1",result);
                    break;
                case 3:
//                    System.out.println("x_2");
                    pos.put("x_2",result);
                    break;
            }
//            System.out.println("Best ans for sensor should equal final ans");
//            System.out.println(Arrays.toString(result.getPoint()));
        }
        Vector3 position = new Vector3();
        for (String str : pos.keySet()){
            PointValuePair res = pos.get(str);
            if (str.contains("x")){
                position.setX(res.getPoint()[0]);
            }
            else if (str.contains("y")){
                position.setY(res.getPoint()[0]);
            }
            position.setTheta(res.getPoint()[1]);
            System.out.println(res.getValue());
        }
        return position;
    }
}
