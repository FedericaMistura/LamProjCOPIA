package com.example.lamproj.data;

import java.util.List;
/*
Per rendere asincrono
 */

public interface SampleDbListSampleResultInterface {
    /*
    Chiamato quando l'operazione di recupero dei samples è completata
     */
    public void onGetListSampleComplete(List<Sample> samples);

}