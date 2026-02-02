package com.enryu;

import java.util.Scanner;

public class Main {
    @SuppressWarnings("unused") 
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Do you want to create a WAV file or read a WAV file? (c/r/e):");
            String choice = scanner.nextLine().trim().toLowerCase();
            switch (choice) {
                case "c" -> {
                    System.out.println("Creating a WAV file...");
                    wav w = new wav();
                    w.main();
                }
                case "r" -> {
                    System.out.println("Reading a WAV file...");
                    reader r = new reader();
                }
                case "e" -> {
                    System.out.println("Editing .wav file...");
                    edit ed = new edit();
                    edit.main();
                }
                default -> System.out.println("Invalid choice. Please enter 'c' to create or 'r' to read or 'e' to edit.");  
        }
        
        System.out.println("Program ended.");
        }
    }
}