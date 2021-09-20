package luecx.volume;

public class Result {

    private double dx,dy,dz;
    private double s_xx, s_yy, s_zz, t_23, t_13, t_12;

    public Result() {
    }

    public void setDisplacement(double dx, double dy, double dz){
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
    }

    public void setStress(double s_xx,double s_yy,double s_zz,double t_23,double t_13,double t_12){
        this.s_xx = s_xx;
        this.s_yy = s_yy;
        this.s_zz = s_zz;
        this.t_23 = t_23;
        this.t_13 = t_13;
        this.t_12 = t_12;
    }

    public void setFromString(String string){
        String[] split = string.replace(" ", "").split(",");
        setDisplacement(
                Float.parseFloat(split[1]),
                Float.parseFloat(split[2]),
                Float.parseFloat(split[3]));
        setStress(
                Float.parseFloat(split[4]),
                Float.parseFloat(split[5]),
                Float.parseFloat(split[6]),
                Float.parseFloat(split[7]),
                Float.parseFloat(split[8]),
                Float.parseFloat(split[9]));
    }

    public double getDx() {
        return dx;
    }

    public double getDy() {
        return dy;
    }

    public double getDz() {
        return dz;
    }

    public double getS_xx() {
        return s_xx;
    }

    public double getS_yy() {
        return s_yy;
    }

    public double getS_zz() {
        return s_zz;
    }

    public double getT_23() {
        return t_23;
    }

    public double getT_13() {
        return t_13;
    }

    public double getT_12() {
        return t_12;
    }
}
