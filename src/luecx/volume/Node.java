package luecx.volume;

public class Node{

    int id;
    double x,y,z;
    double force_x,force_y,force_z;

    public Node(double x, double y, double z, int id) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.id = id;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getForce_x() {
        return force_x;
    }

    public void setForce_x(double force_x) {
        this.force_x = force_x;
    }

    public double getForce_y() {
        return force_y;
    }

    public void setForce_y(double force_y) {
        this.force_y = force_y;
    }

    public double getForce_z() {
        return force_z;
    }

    public void setForce_z(double force_z) {
        this.force_z = force_z;
    }
}
