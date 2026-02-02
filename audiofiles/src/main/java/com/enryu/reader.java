package com.enryu;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Scanner;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

/**
 * Simple WAV reader that prompts for a file path, reads audio format and bytes,
 * and writes metadata and raw bytes to a *_data.txt file.
 * 
 * Made with a lot of AI, since making the wav file was what I wanted to do
 * This is just an extra thing
 * 
 * edit and wav though are made by me
 */
public class reader {

    @SuppressWarnings(value = {"CallToPrintStackTrace"})
    public reader() {
        // Prompt for WAV file paths
        System.out.println("Enter the path of the WAV file to read(or \"\" for cancel):");
        try (Scanner scanner = new Scanner(System.in)) {
            String inputPath = scanner.nextLine();

            if (inputPath.equals("no") || inputPath.equals("null") || inputPath.isEmpty()) {
                System.out.println("Operation cancelled.");
                return;
            }

            System.out.println("You entered: " + inputPath);
            File fileIn = new File(inputPath);

            // Read WAV file chunk details
            String riffHeader = "";
            int fileSize = 0;
            String waveHeader = "";
            String fmtHeader = "";
            int fmtSize = 0;
            short audioFormat = 0;
            short channels = 0;
            int sampleRate = 0;
            int bytePerSec = 0;
            short bytePerBloc = 0;
            short bitsPerSample = 0;
            String dataHeader = "";
            int dataSize = 0;

            try (RandomAccessFile raf = new RandomAccessFile(fileIn, "r")) {
                // Read RIFF header
                byte[] riffBytes = new byte[4];
                raf.read(riffBytes);
                riffHeader = new String(riffBytes);
                
                // Read file size
                fileSize = readLittleEndianInt(raf) + 8;
                
                // Read WAVE header
                byte[] waveBytes = new byte[4];
                raf.read(waveBytes);
                waveHeader = new String(waveBytes);
                
                // Read fmt header
                byte[] fmtBytes = new byte[4];
                raf.read(fmtBytes);
                fmtHeader = new String(fmtBytes);
                
                // Read fmt chunk size
                fmtSize = readLittleEndianInt(raf);
                
                // Read audio format
                audioFormat = readLittleEndianShort(raf);
                
                // Read channels
                channels = readLittleEndianShort(raf);
                
                // Read sample rate
                sampleRate = readLittleEndianInt(raf);
                
                // Read byte per sec
                bytePerSec = readLittleEndianInt(raf);
                
                // Read byte per block
                bytePerBloc = readLittleEndianShort(raf);
                
                // Read bits per sample
                bitsPerSample = readLittleEndianShort(raf);

                // Find data chunk
                while (raf.getFilePointer() < raf.length() - 8) {
                    byte[] chunkId = new byte[4];
                    raf.read(chunkId);
                    String chunk = new String(chunkId);
                    int chunkSize = readLittleEndianInt(raf);

                    if (chunk.equals("data")) {
                        dataHeader = chunk;
                        dataSize = chunkSize;
                        break;
                    } else {
                        raf.skipBytes(chunkSize);
                    }
                }
            }

            // Read audio stream
            try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(fileIn)) {
                AudioFormat format = audioInputStream.getFormat();

                // Capture format details
                boolean bigEndian = format.isBigEndian();
                String encoding = format.getEncoding().toString();
                float frameRate = format.getFrameRate();
                int frameSize = format.getFrameSize();
                long totalFrames = audioInputStream.getFrameLength();

                // Read audio data into buffer
                int numBytes = audioInputStream.available();
                byte[] buffer = new byte[numBytes];
                int bytesRead = 0;
                while (bytesRead < numBytes) {
                    int n = audioInputStream.read(buffer, bytesRead, numBytes - bytesRead);
                    if (n == -1) {
                        break;
                    }
                    bytesRead += n;
                }

                System.out.println("Successfully read the WAV file and its audio data.");

                // Prepare output file
                String outputTxtPath = inputPath + "_data.txt";
                File outputTxtFile = new File(outputTxtPath);

                try (java.io.FileWriter fw = new java.io.FileWriter(outputTxtFile);
                     java.io.PrintWriter writer = new java.io.PrintWriter(fw)) {

                    writer.println(".wav File Data");
                    writer.println();

                    // Write WAV header information
                    writer.println("WAV Header Information:");
                    writer.println("RIFF Header: " + riffHeader);
                    writer.println("File Size: " + fileSize + " bytes");
                    writer.println("WAVE Header: " + waveHeader);
                    writer.println();
                    
                    writer.println("Format Chunk:");
                    writer.println("fmt Header: " + fmtHeader);
                    writer.println("fmt Chunk Size: " + fmtSize);
                    writer.println("Audio Format: " + audioFormat + " (1 = PCM)");
                    writer.println("Channels: " + channels);
                    writer.println("Sample Rate: " + sampleRate + " Hz");
                    writer.println("Byte Per Sec: " + bytePerSec);
                    writer.println("Byte Per Block: " + bytePerBloc);
                    writer.println("Bits Per Sample: " + bitsPerSample);
                    writer.println();
                    
                    writer.println("Data Chunk:");
                    writer.println("data Header: " + dataHeader);
                    writer.println("Data Size: " + dataSize + " bytes");
                    writer.println();

                    // Write AudioFormat details
                    writer.println("AudioFormat Details:");
                    writer.println("Big Endian: " + bigEndian);
                    writer.println("Encoding: " + encoding);
                    writer.println("Frame Rate: " + frameRate);
                    writer.println("Frame Size (bytes): " + frameSize);
                    writer.println("Total Frames: " + totalFrames);
                    writer.println();

                    // Write audio bytes as hex
                    writer.println("WAV File Audio Data (in bytes):");
                    writer.println("Total Bytes: " + buffer.length);
                    int byteRead;
                    int count = 0;
                    try (java.io.FileInputStream fis = new java.io.FileInputStream(fileIn)) {
                        while ((byteRead = fis.read()) != -1) {
                            writer.print(String.format("%02X ", byteRead));
                            // New line every 16 bytes for readability
                            if (++count % 16 == 0) {
                                writer.println();
                            }
                        }
                    }

                    writer.println();
                    writer.println();
                    writer.println("Audio Data:");
                    writer.println();
                    for (byte b : buffer) {
                        writer.println(b);
                    }

                    System.out.println("Audio data written to: " + outputTxtPath);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int readLittleEndianInt(RandomAccessFile raf) throws IOException {
        byte[] bytes = new byte[4];
        raf.read(bytes);
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    private short readLittleEndianShort(RandomAccessFile raf) throws IOException {
        byte[] bytes = new byte[2];
        raf.read(bytes);
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
    }
}