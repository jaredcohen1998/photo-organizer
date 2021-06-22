package com.drew;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardCopyOption.*;

public class Main {
    public static void main(String[] args) throws IOException {
        try {
            String basePath = "D:\\temp\\Family Pictures - Copy\\";
            String dateUnknownPath = basePath + "Date Unknown";
            List<File> files = new ArrayList<>();
            Metadata metadata = null;

            BufferedWriter writer = new BufferedWriter(new FileWriter("master_log.txt"));

            System.out.println("Scanning files...this can take a while...");
            listf(basePath, files);

            int filesSize = files.size();
            System.out.println(filesSize + " files scanned!");
            System.out.println("Organizing by date...");
            System.out.println("-------------------------------------------");

            for (int i = 0; i < filesSize; i++) {
                File file = files.get(i);
                String fileName = file.getName();
                String filePath = file.getPath();

                writer.write("\n---------- " + (i + 1) + "/" + filesSize + " ---------- \n");

                boolean metaDataFound = true;
                try {
                    metadata = ImageMetadataReader.readMetadata(file);
                } catch (Exception e) {
                    System.err.println("EXCEPTION METADATA ON FILE " + fileName + ": " + e.getMessage());
                    writer.write("ERROR: Could not read metadata of " + fileName + "\n");
                    metaDataFound = false;
                }

                if (!metaDataFound) {
                    continue;
                }

                writer.write(fileName + "\n");
                boolean dateTakenFound = false;

                for (Directory directory : metadata.getDirectories()) {
                    for (Tag tag : directory.getTags()) {
                        if (tag.getTagName().startsWith("Date/Time Digitized")) {
                            dateTakenFound = true;

                            String[] dateAndTime = tag.toString().split("-");
                            String[] date = dateAndTime[1].trim().split(":");
                            String year = date[0];
                            String month = date[1];
                            String day = date[2].split(" ")[0];

                            String folderToMove = file.getParent() + "\\" + year + "-" + month + "-" + day + "\\";

                            writer.write("  Date taken: " + year + "-" + month + "-" + day + "\n");
                            writer.write("  Source folder: " + filePath + "\n");
                            writer.write("  Target folder: " + folderToMove + "\\" + fileName + "\n");

                            new File(folderToMove).mkdir();
                            Files.move(Paths.get(filePath), Paths.get(folderToMove + "\\" + fileName), REPLACE_EXISTING);

                            writer.write("  SUCCESS!\n");
                            break;
                        }
                    }

                    if (dateTakenFound) {
                        break;
                    }
                }

                if (!dateTakenFound) {
                    new File(dateUnknownPath).mkdir();

                    Files.move(Paths.get(filePath), Paths.get(dateUnknownPath + "\\" + fileName), REPLACE_EXISTING);
                    writer.write("  ERROR: Date taken unknown\n");
                    writer.write("  Moving to " + dateUnknownPath + "\\" + fileName + "\n");
                }

                System.out.println(fileName + " done..." + (i + 1) + "/" + filesSize);
            }

            System.out.println("Done!");
            writer.close();
        } catch (Exception e) {
            System.err.println("EXCEPTION: " + e.getMessage());
        }
    }

    public static void listf(String directoryName, List<File> files) {
        File directory = new File(directoryName);

        // Get all files from a directory.
        File[] fList = directory.listFiles();
        if (fList != null) {
            for (File file : fList) {
                if (file.isFile()) {
                    files.add(file);
                } else if (file.isDirectory()) {
                    listf(file.getAbsolutePath(), files);
                }
            }
        }
    }
}
