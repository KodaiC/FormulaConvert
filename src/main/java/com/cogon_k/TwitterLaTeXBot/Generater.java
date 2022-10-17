package com.cogon_k.TwitterLaTeXBot;

import java.io.*;
import java.util.UUID;

public class Generater {
    public static String base =
            """
                    \\documentclass[uplatex]{jsarticle}
                    \\usepackage{amsmath}
                    \\usepackage{amssymb}
                    \\usepackage{amsfonts}
                    \\pagestyle{empty}
                                
                    \\begin{document}
                    \\begin{equation*}
                    \\begin{split}
                        %s
                    \\end{split}
                    \\end{equation*}
                    \\end{document}
                    """;
    public static boolean DO_PRINT_STDOUT = false;

    public static String generatePDF(String tex, boolean doRemove) {
        String uuid = UUID.randomUUID().toString();
        write(uuid + ".tex", base.formatted(tex));
        int result = 0;

        try {
            Process p1 = new ProcessBuilder(new String[]{"sh", "-c",
                    "timeout 3 latexmk " + uuid + ".tex" +
                            " && pdfcrop --margins \"4 4 4 4\" " + uuid + ".pdf" +
                            " && pdftoppm -r 1000 -png " + uuid + "-crop.pdf " + uuid + "-image"
            }).start();
            p1.waitFor();
            result = p1.exitValue();

            if (DO_PRINT_STDOUT) {
                try (BufferedReader r = new BufferedReader(new InputStreamReader(p1.getInputStream()))) {
                    String line;
                    while ((line = r.readLine()) != null) {
                        System.out.println(line);
                    }
                }
                try (BufferedReader r = new BufferedReader(new InputStreamReader(p1.getErrorStream()))) {
                    String line;
                    while ((line = r.readLine()) != null) {
                        System.out.println(line);
                    }
                }
            }
            p1.destroy();

            if (doRemove) {
                Process p2 = new ProcessBuilder(new String[]{"sh", "-c", "rm " + uuid + ".* " + uuid + "-crop.pdf"}).start();
                p2.waitFor();
                p2.destroy();
            }
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return result == 0 ? uuid : "";
    }

    public static void write(String fileName, Object... objs) {
        try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(fileName, false)))) {
            for (Object o : objs) {
                pw.println(o);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
