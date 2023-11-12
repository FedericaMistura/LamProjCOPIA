package com.example.lamproj.sensors;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.example.lamproj.App;

public class AudioSubSystem {
    public boolean recordAudioEnabled = false;

    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private int bufferSize;

    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    public void stopMetering(){
        isRecording=false;
    }

    /*
    Verifica se la registrazione audio è abilitata
     */
    @SuppressLint("MissingPermission")
    public void startMetering() {
        if (audioRecord == null) {
            if (! App.A.context.isRecordAudioEnabled()){
                return;
            }
            bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, bufferSize);

            final short[] audioData = new short[bufferSize / 2]; // 16-bit audio, quindi dividi per 2

            audioRecord.startRecording();

            isRecording = true;
            //Avvio thread per registrazione audio
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isRecording) {
                        //Continua sempre a leggere i dati audio
                        int bufferReadResult = audioRecord.read(audioData, 0, audioData.length);

                        if (bufferReadResult == AudioRecord.ERROR_INVALID_OPERATION || bufferReadResult == AudioRecord.ERROR_BAD_VALUE) {
                            Log.e("AudioRecord", "Error reading audio data");
                        } else {
                            // Calcola ampliezza
                            int maxAmplitude = 0; //Calcolato il valore massimo nel buffer audio
                            for (short sample : audioData) {
                                if (Math.abs(sample) > maxAmplitude) {
                                    maxAmplitude = Math.abs(sample);
                                }
                            }
                            App.A.sensorHub.level_noise=getDbValue( maxAmplitude); //Calcolo livello in dB
//                          Log.d("Audio Loudness", "Max Amplitude: " + maxAmplitude);
                        }
                    }
                }
            }).start();
        }

    }

    public double getDbValue(double amplitude) {
        double referenceLevel = 32768.0; //Il valore di riferimento per 16 bit di audio
        return 20 * Math.log10(amplitude / referenceLevel);
    }

}
