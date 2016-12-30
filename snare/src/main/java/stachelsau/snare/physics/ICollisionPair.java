package stachelsau.snare.physics;

public interface ICollisionPair {

    int getObjIdA();
    int getObjIdB();
    float[] getCollisionPoint();

}