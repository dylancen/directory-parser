import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class directoryParser {
    private List<File> order;
    private String originPath;

    private static final Pattern p = Pattern.compile("(require ‘)(.*)(‘)");

    private void parseAndOrderAllFiles(String path){
        File originDir = new File(path);
        for(File file : originDir.listFiles()){
            if(file.isFile()){
                List<String> dependencies = new ArrayList<String>();
                addToOrder(file, dependencies);
            }else{
                parseAndOrderAllFiles(file.getAbsolutePath());
            }
        }
    }

    private void addToOrder(File f, List<String> dependencies){
        try {
            Scanner s = new Scanner(f);
            dependencies.add(f.getAbsolutePath());
            while(s.hasNextLine()){
                String line = s.nextLine();
                String required = reqRegex(line);
                if(required != null){
                    File depFile = new File(this.originPath + "\\" + required + ".txt");
                    if(dependencies.contains(depFile.getAbsolutePath())){
                        throw new Exception("Infinite loop: " + dependencies.toString());
                    }else{
                        addToOrder(depFile, dependencies);
                    }
                }
            }
            dependencies.remove(f.getAbsolutePath());
            s.close();
            if(!order.contains(f)){
                order.add(f);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void concatFileContents(){
        File newFile = new File("output_file.txt");
        List<File> orderedFiles = new ArrayList<>();
        orderedFiles.addAll(this.order);
        try{
            if(!newFile.createNewFile()){
                PrintWriter clearer = new PrintWriter(newFile);
                clearer.print("");
                clearer.close();
            }

            FileWriter writer = new FileWriter(newFile);
            String buffer = "";
            while(!orderedFiles.isEmpty()){
                File f = orderedFiles.remove(orderedFiles.size()-1);
                buffer = updateBuffer(f, orderedFiles) + buffer;
            }
            writer.write(buffer);
            writer.close();
        }catch (Exception e){
            System.out.println("Error occured");
            e.printStackTrace();
        }
    }

    private String updateBuffer(File f, List<File> orderedFiles){
        Scanner reader;
        try {
            reader = new Scanner(f);
            orderedFiles.remove(f);
            String localBuffer = "";
            while(reader.hasNextLine()){
                String line = reader.nextLine();
                String required = reqRegex(line);
                if (required != null){
                    File depFile = new File(this.originPath + "\\" + required + ".txt");
                    localBuffer = localBuffer + updateBuffer(depFile, orderedFiles);
                }else {
                    localBuffer = localBuffer + line + "\n";
                }
            }
            reader.close();
            return localBuffer;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private String reqRegex(String line){
        Matcher m = p.matcher(line);
        if(m.find()){
            return m.group(2);
        }
        return null;
    }

    public directoryParser(String path){
        this.originPath = path;
        this.order = new ArrayList<>();

        parseAndOrderAllFiles(this.originPath);
        concatFileContents();
    }

    public List<File> getOrderedFiles(){
        return this.order;
    }
}
