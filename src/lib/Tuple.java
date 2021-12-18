package lib;

public class Tuple<T, T1> {
    private T x;
    private T1 y;

    public Tuple(T x, T1 y) {
        this.x = x;
        this.y = y;
    }

    public T getX() {
        return x;
    }

    public T1 getY() {
        return y;
    }

    public void setX(T x) {
        this.x = x;
    }

    public void setY(T1 y) {
        this.y =y;
    }

    @Override
    public String toString() {
        return "Tuple{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}

