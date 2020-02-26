package android.hardware;

/** {@hide} */
interface IConsumerIrService{

    boolean hasIrEmitter();
    void transmit(String packageName, int carrierFrequency, in int[] pattern);
    int[] getCarrierFrequencies();

}