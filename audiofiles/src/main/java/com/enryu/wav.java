package com.enryu;
    // making a wav file
    // https://en.wikipedia.org/wiki/WAV
    // https://www.youtube.com/watch?v=JqJPBu7GXvw
    // https://www.youtube.com/watch?v=rHqkeLxAsTc&t
    // http://soundfile.sapp.org/doc/WaveFormat/
    // https://docs.oracle.com/javase/8/docs/technotes/guides/sound/programmer_guide/contents.html

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class wav {

    // RIFF Chunk
    static final String FILE_TYPE_BLOC_ID = "RIFF";
    // static final String FILE_SIZE = "----";  unneeded 
    static final String FILE_FORMAT_ID = "WAVE";

    // fmt sub-chunk
    static final String FORMAT_BLOC_ID = "fmt "; // four bytes so we add a space
    static final int FORMAT_DATA_LENGTH = 16; // 4 bytes  
    static final int FORMAT_TYPE = 1; // 2 bytes  PCM = 1
    static final int NUMBER_OF_CHANNELS = 2; // 2 bytes  Mono = 1, Stereo = 2  
    static final int SAMPLE_RATE = 44100; // 4 bytes, our frequency
    static final int BITS_PER_SAMPLE = 16; // 2 bytes
    static final int BYTE_RATE = SAMPLE_RATE * NUMBER_OF_CHANNELS * (BITS_PER_SAMPLE / 8);
    static final int BLOCK_ALIGN = NUMBER_OF_CHANNELS * (BITS_PER_SAMPLE / 8);
    

    // data sub-chunk
    static final String DATA_BLOC_ID = "data";
    // static final String DATA_SIZE = "----"; unneeded 

    // sampled data
    static final int DURATION_IN_SECONDS = 5; // 5 seconds
    static final int MAX_AMPLITUDE = 32760; // https://youtu.be/rHqkeLxAsTc?t=1959 explains why this number
    static final double FREQ = 440.0; // A4 note

    // Sending file to my folder for it
    String folderPath = "D:\\Code stuff\\audiofiles\\AudioFilesCreated"; 
    String fileName = "testingediting.wav";
    File outputFile = new File(folderPath, fileName);
    
    // helper to write bytes, modified from https://www.youtube.com/watch?v=rHqkeLxAsTc&t, since the video is in C++
    public static void writeAsBytes(DataOutputStream dos, int value, int byteSize) throws IOException {
       for (int i = 0; i < byteSize; i++) {
           dos.writeByte(value & 0xFF);
           value >>= 8;
       }
   }

    @SuppressWarnings("CallToPrintStackTrace")
    int main(){
        try {
            outputFile.getParentFile().mkdirs();
            
            try (FileOutputStream fos = new FileOutputStream(outputFile)
            ; DataOutputStream dos = new DataOutputStream(fos)) {

                // RIFF Chunk
                dos.writeBytes(FILE_TYPE_BLOC_ID);
                writeAsBytes(dos, 36 + 0, 4); 
                dos.writeBytes(FILE_FORMAT_ID);

                // fmt sub-chunk
                dos.writeBytes(FORMAT_BLOC_ID);
                writeAsBytes(dos, FORMAT_DATA_LENGTH, 4); // instead of ----
                writeAsBytes(dos, FORMAT_TYPE, 2);
                writeAsBytes(dos, NUMBER_OF_CHANNELS, 2);
                writeAsBytes(dos, SAMPLE_RATE, 4);
                writeAsBytes(dos, BYTE_RATE, 4);
                writeAsBytes(dos, BLOCK_ALIGN, 2);
                writeAsBytes(dos, BITS_PER_SAMPLE, 2);
                
                // data sub-chunk
                dos.writeBytes(DATA_BLOC_ID);
                writeAsBytes(dos, 0, 4); // instead of ----

                int startAudio= (int)fos.getChannel().position();

                 // Write actual audio data 
                // This was mine that I created, it hurts the ears
                for(int i = 0; i < SAMPLE_RATE * DURATION_IN_SECONDS; i++) {
                    double amplitude = (double)i / SAMPLE_RATE * MAX_AMPLITUDE; // ensures amplitude never gets bigger than SAMPLE_RATE * DURATION_IN_SECONDS

                    double value = Math.sin((2 * 3.14159265359 * i * FREQ) / SAMPLE_RATE); // sin wave

                    double channel1 = amplitude * value / 2; // low to high volume
                    double channel2 = MAX_AMPLITUDE - amplitude * value; // high to low volume

                    writeAsBytes(dos, (int)channel1, 2); // needs to be 16 bits
                    writeAsBytes(dos, (int)channel2, 2); 
                }
    
                /* // This is code made by Claude since mine hurts ears
                for(int i = 0; i < SAMPLE_RATE * DURATION_IN_SECONDS; i++) {
                    // Envelope: fade in first half, fade out second half
                    double envelope;
                    double progress = (double)i / (SAMPLE_RATE * DURATION_IN_SECONDS);

                    if (progress < 0.5) {
                        envelope = progress * 2; // fade in
                    } else {
                        envelope = (1 - progress) * 2; // fade out
                    }
                    envelope *= MAX_AMPLITUDE;
                    
                    // C Major chord: C4(261.63) + E4(329.63) + G4(392.00)
                    double c = Math.sin((2 * Math.PI * i * 261.63) / SAMPLE_RATE);
                    double e = Math.sin((2 * Math.PI * i * 329.63) / SAMPLE_RATE);
                    double g = Math.sin((2 * Math.PI * i * 392.00) / SAMPLE_RATE);
                    
                    double mixedValue = (c + e + g) / 3.0; // average the three notes
                    
                    int sample = (int)(envelope * mixedValue);
                    
                    writeAsBytes(dos, sample, 2); // left channel
                    writeAsBytes(dos, sample, 2); // right channel (same for mono effect)
                } */

                int endAudio = (int)fos.getChannel().position(); // in c++ its tellp
                fos.getChannel().position(startAudio - 4); // in c++ its seekp

                writeAsBytes(dos, endAudio - startAudio, 4); // how much time has elapsed 

                fos.getChannel().position(4); // in c++ its seekp
                writeAsBytes(dos, endAudio - 8, 4);
            }
            // close file
            System.out.println("WAV file created at: " + outputFile.getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();}
        return 0;
    }

}
