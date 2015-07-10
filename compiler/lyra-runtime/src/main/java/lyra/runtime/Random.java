package lyra.runtime;

/**
 *
 */
public class Random extends Object {

    private java.util.Random instance;

    public Random(){
        instance = new java.util.Random();
    }

    public Number lyra_nextNumber(){
        return new Number(instance.nextDouble());
    }

    public Int lyra_nextInt(Int bound){
        return new Int(instance.nextInt(bound.valueOf()));
    }

}
