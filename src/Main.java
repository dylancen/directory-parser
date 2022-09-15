import java.io.File;

public class Main {
    public static void main(String[] args){
        directoryParser parser = new directoryParser(args[0]);
        for(File f : parser.getOrderedFiles()){
            System.out.println(f.getAbsolutePath());
        }
    }
}
