import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class directoryParser {
    private Set<File> allFiles;
    private String originPath;

    private void parseAllFiles(String path){
        File originDir = new File(path);
        for(File file : originDir.listFiles()){
            if(file.isFile()){
                allFiles.add(file);
            }else{
                parseAllFiles(file.getAbsolutePath());
            }
        }
    }

    private void concatFileContents(){
        File newFile = new File("output_file.txt");
        try{
            if(!newFile.createNewFile()){
                PrintWriter clearer = new PrintWriter(newFile);
                clearer.print("");
                clearer.close();
            }

            FileWriter writer = new FileWriter(newFile);
            Scanner reader;
            for(File file : allFiles){
                reader = new Scanner(file);
                while(reader.hasNextLine()){
                    writer.write(reader.nextLine());
                    writer.write("\n");
                }
                reader.close();
            }
            writer.close();
        }catch (Exception e){
            System.out.println("Error occured");
            e.printStackTrace();
        }
    }

    public directoryParser(String path){
        this.originPath = path;
        this.allFiles = new TreeSet<>(Comparator.comparing(File::getName));
        parseAllFiles(this.originPath);
        concatFileContents();
    }



    public Set<File> getAllFiles(){
        return this.allFiles;
    }
}
