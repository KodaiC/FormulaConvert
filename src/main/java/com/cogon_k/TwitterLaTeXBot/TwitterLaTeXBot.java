package com.cogon_k.TwitterLaTeXBot;

import java.util.Scanner;

public class TwitterLaTeXBot {
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        while (true) {
            System.out.println(Generater.generatePDF(Parser.parse(scan.nextLine()), true));
        }
    }
}
