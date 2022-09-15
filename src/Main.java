import java.io.File;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args){
        directoryParser parser = new directoryParser("C:\\Users\\dylan\\Documents\\test_folder");
        for(File f : parser.getOrderedFiles()){
            System.out.println(f.getAbsolutePath());
        }
    }
}
