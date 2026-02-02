package com.enryu;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;

// Making a wav file header editor myself!!!

public class edit {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main() {
        System.out.println("Enter the path of the WAV file to read(or \"\" for cancel):");
            String inputPath = scanner.nextLine();

            if (inputPath.equals("no") || inputPath.equals("null") || inputPath.isEmpty()) {
                System.out.println("Operation cancelled.");
                return;
            }

            System.out.println("You entered: " + inputPath);
            File fileIn = new File(inputPath);

            if (!fileIn.exists() || !fileIn.isFile()) {
                System.out.println("The specified file does not exist or is not a valid file.");
                return;
            }

            System.out.println("Which header field do you want to edit? (e.g. Sample Rate(sr)/Frequency(fr), Bits Per Sample(bps), Num Channels(nc)):");
            String field = scanner.nextLine().trim().toLowerCase();

            switch (field) {
                case "sample rate", "sr", "frequency", "fr" -> {
                    System.out.println("Editing Sample Rate...");
                    System.out.println("Enter the new sample rate:");
                    int newSampleRate = Integer.parseInt(scanner.nextLine().trim());
                    editSampleRate(fileIn, newSampleRate);
                }
                case "bits per sample", "bps" -> {
                    System.out.println("Editing Bits Per Sample...");
                    System.out.println("Enter the new bits per sample:");
                    int newBitsPerSample = Integer.parseInt(scanner.nextLine().trim());
                    editBitsPerSample(fileIn, newBitsPerSample);
                }
                case "num channels", "nc" -> {
                    System.out.println("Editing Number of Channels...");
                    System.out.println("Enter the new number of channels:");
                    int newNumChannels = Integer.parseInt(scanner.nextLine().trim());
                    editNumChannels(fileIn, newNumChannels);
                }
                case "audio format", "af" -> {
                    System.out.println("Editing Audio Format...");
                    System.out.println("Enter the new audio format:");
                    int newAudioFormat = Integer.parseInt(scanner.nextLine().trim());
                    editAudioFormat(fileIn, newAudioFormat);
                }
                case "wave", "wav" -> {
                    System.out.println("Editing WAVE Chunk...");
                    editWAVE(fileIn);
                }
                case "fmt" -> {
                    System.out.println("Editing FMT Chunk...");
                    editFMT(fileIn);
                }
                default -> {
                    System.out.println("Invalid field name.");
                    System.out.println("Exiting....");
                    return; // exit so we don't update unnecessarily
                }
            }
            update(fileIn); // call update to recalculate other header fields
            System.out.println("Editing operation completed.");
    }

    public static void editSampleRate(File file, int newSampleRate) {
            // skip byes to sample rate position and write new value
            try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
                raf.seek(24); // Sample Rate is at byte offset 24
                // https://en.wikipedia.org/wiki/WAV#WAV_file_header
                // You add up the bytes of the previous fields to get to the sample rate position
                raf.writeInt(Integer.reverseBytes(newSampleRate)); // Write new sample rate in little-endian format
            } catch (IOException e) {
                System.out.println("Error editing sample rate: " + e.getMessage());
            }

            areWeDone();
    }

    public static void editBitsPerSample(File file, int newBitsPerSample) {
            try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
                raf.seek(34); // Bits Per Sample is at byte offset 34
                raf.writeShort(Short.reverseBytes((short) newBitsPerSample)); // Write new bits per sample in little-endian format
            } catch (IOException e) {
                System.out.println("Error editing bits per sample: " + e.getMessage());
            }

            areWeDone();
    }

    public static void editNumChannels(File file, int newNumChannels) {
            try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
                raf.seek(22); // Num Channels is at byte offset 22
                raf.writeShort(Short.reverseBytes((short) newNumChannels)); // Write new number of channels in little-endian format
            } catch (IOException e) {
                System.out.println("Error editing number of channels: " + e.getMessage());
            }

            areWeDone();
    }

    public static void editAudioFormat(File file, int newAudioFormat) {
            try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
                raf.seek(20); // Audio Format is at byte offset 20
                raf.writeShort(Short.reverseBytes((short) newAudioFormat)); // Write new audio format in little-endian format
            } catch (IOException e) {
                System.out.println("Error editing audio format: " + e.getMessage());
            }

            areWeDone();
    }

    public static void editWAVE(File file){
        // if a .wav file is broken and need to fix the WAVE chunk
        try(RandomAccessFile raf = new RandomAccessFile(file, "rw")){
            raf.seek(8); // WAVE chunk starts at byte offset 8
            raf.writeBytes("WAVE");
        } catch (IOException e){
            System.out.println("Error editing WAVE chunk: " + e.getMessage());
        }

        areWeDone();
    }

    public static void editFMT(File file){
        try(RandomAccessFile raf = new RandomAccessFile(file, "rw")){
            raf.seek(12); // FMT chunk starts at byte offset 12
            raf.writeBytes("fmt ");
        } catch (IOException e){
            System.out.println("Error editing FMT chunk: " + e.getMessage());
        }

        areWeDone();
    }

    public static void update(int newSampleRate, int bitsPerSample, int newNumChannels, File file){
        System.out.println("Update function called.");
        // need to recalculate these when sample rate, bits per sample, or num channels change

        // BlockAlign BytePerBloc NbrChannels * BitsPerSample / 8
        int blockAlign = newNumChannels * bitsPerSample / 8;

        // ByteRate BytePerSec Frequency * BytePerBloc   
        int byteRate = newSampleRate * blockAlign;

        // then write the new values to the file
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            // ByteRate is at byte offset 28
            raf.seek(28); 
            raf.writeInt(Integer.reverseBytes(byteRate)); 

            // block align is at byte offset 32
            raf.seek(32); 
            raf.writeShort(Short.reverseBytes((short) blockAlign)); 
        } catch (IOException e) {
            System.out.println("Error updating header fields: " + e.getMessage());
        }
    }

    // Overloaded update that reads current header fields from the file and delegates to the parameterized update
    public static void update(File file){
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            raf.seek(24); // Sample Rate
            int sampleRate = Integer.reverseBytes(raf.readInt());

            raf.seek(34); // Bits Per Sample
            int bitsPerSample = Short.toUnsignedInt(Short.reverseBytes(raf.readShort()));

            raf.seek(22); // Num Channels
            int numChannels = Short.toUnsignedInt(Short.reverseBytes(raf.readShort()));

            update(sampleRate, bitsPerSample, numChannels, file);
        } catch (IOException e) {
            System.out.println("Error reading header fields for update: " + e.getMessage());
        }
    }

   public static void areWeDone(){
        System.out.println("Editing complete.");
        System.out.println("Do you wish to edit another chunk?.");
        String answer;
        answer = scanner.nextLine().trim().toLowerCase();

        if(answer.equals("yes") || answer.equals("y")){
            main();
        } else {
            System.out.println("Exiting editor.");
        }
   }
}



// TODO

// Add more header fields to edit when file may be corrupted
// for instance
// No RIFF
