import java.io.File;
public class Main {
    public static void main(String[] args){
        directoryParser parser = new directoryParser("C:\\Users\\dylan\\Documents\\test_folder");
        for(File file : parser.getAllFiles()){
            System.out.println(file.getAbsoluteFile());
        }
    }
}
