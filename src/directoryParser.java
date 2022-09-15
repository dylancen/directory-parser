import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class directoryParser {

    //переменная для хранения отсортированного списка файлов
    private List<File> order;

    //переменная для храниения пути корневой папки
    private String originPath;

    //regex паттерн для нахождения пути из директивы формата require ‘*‘
    private static final Pattern p = Pattern.compile("(require ‘)(.*)(‘)");

    //функция для рекурсивного парсинга всех файлов и папок в корневой папки
    private void parseAndOrderAllFiles(String path){
        //создаем объект входного файла (это всегда папка)
        File originDir = new File(path);
        File[] sortedFiles = originDir.listFiles();
        Arrays.sort(sortedFiles, Comparator.comparing(File::getName));
        //перебираем все объекты в папке
        for(File file : sortedFiles){
            if(file.isFile()){ //если объект - файл, отправляем его в функцию сортировки файлов
                //dependencies - объект используемый для сохранения пути зависимости файлов, используемая чтобы находить циклической зависимости
                List<String> dependencies = new ArrayList<String>();
                //отправляем объект файла в функцию сортирвки
                addToOrder(file, dependencies);
            }else{
                //если же это папка, рекурсивно продолжаем исследование ее объектов
                parseAndOrderAllFiles(file.getAbsolutePath());
            }
        }
    }

    //функция addToOrder отвечает за сортировку найденных файлов внутри классовой переменной directoryParser.order
    //эта функция также является рекурсивной, т.к нужно тоже исследовать зависимости обозначенные внутри файлов
    private void addToOrder(File f, List<String> dependencies){
        try {
            //открываем объект сканнер позволяющий нам считывать построчно входной файл
            Scanner s = new Scanner(f);
            //добавляем путь к цепочке зависимостей
            dependencies.add(f.getAbsolutePath());
            //исследуем файл построчно
            while(s.hasNextLine()){
                String line = s.nextLine();
                //проверяем строку на наличие директивы, если такова имеется, функция вернет относительный путь к файлу, если же нет, то null
                String required = reqRegex(line);
                if(required != null){
                    //в случае наличия директивы, открываем файл от которого текущий файл зависит
                    File depFile = new File(this.originPath + "\\" + required + ".txt");
                    //если в цепочке уже есть найденный файл, значит мы нашли цикличность, выводим ошибку с обозначением элементов цикла
                    if(dependencies.contains(depFile.getAbsolutePath())){
                        throw new Exception("Infinite loop: " + dependencies.toString());
                    }else if(!order.contains(depFile)){
                        //если же нет цикличности, и файл еще не был отсортирован, исследуем этот файл
                        addToOrder(depFile, dependencies);
                    }
                }
            }
            //в конечном этапе рекурсии убираем путь из цепочки зависимости
            dependencies.remove(f.getAbsolutePath());
            //закрываем сканнер файла
            s.close();
            //добавляем файл, если его не добавляли ранее
            if(!order.contains(f)){
                order.add(f);
            }
        } catch (Exception e) {
            //в случае возникновения ошибки она выводится (либо неверно указан относительный путь файла в директиве, либо циклическая зависимость
            throw new RuntimeException(e);
        }
    }

    //функция concatFileContents отвечает за конкатенацию файлов в соответствии со списком и зависимостями
    private void concatFileContents(){
        //открываем файл вывода
        File newFile = new File("output_file.txt");
        //создаем список последовательности структуры каталогов и копируем туда значения из directoryParser.order
        List<File> orderedFiles = new ArrayList<>();
        orderedFiles.addAll(this.order);
        try{
            //если файл уже существует, удаляем содержимое, если нет, создаем его
            if(!newFile.createNewFile()){
                PrintWriter clearer = new PrintWriter(newFile);
                clearer.print("");
                clearer.close();
            }
            //создаем объект FileWriter котрый позволит нам вводить строки в выводной файл
            FileWriter writer = new FileWriter(newFile);
            //создаем буффер в который мы будем поэтапно добавлять данные из файлов а затем разом запишем в файл
            String buffer = "";
            //создаем цикл который работает пока не выведены все файлы
            while(!orderedFiles.isEmpty()){
                //в каждом этапе цикла вытаскиваем файл с конца сортировки (тк у них больше зависимостей)
                File f = orderedFiles.remove(orderedFiles.size()-1);
                //получаем скоипанованный текст из рекурсивной функции getCombinedText и добавляем его в буффер, вписываем его в начала, тк мы идем с конца списка
                buffer = getCombinedText(f, orderedFiles) + buffer;
            }
            //вводим все строки в файл
            writer.write(buffer);
            //закрываем объект writer
            writer.close();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    //рекурсивная функция getCombinedText выводит строки вводного файла, учитвая внутренние зависимости
    private String getCombinedText(File f, List<File> orderedFiles){
        Scanner reader;
        try {
            //создаем объект считывания файла
            reader = new Scanner(f);
            //создаем локальный буфер который будет записывать поэтапный текст фалов
            String localBuffer = "";
            //построчно считываем файл и проверяем на наличие директивы
            while(reader.hasNextLine()){
                String line = reader.nextLine();
                String required = reqRegex(line);
                //если директива найдена, создаем объект файла и продолжаем рекурсивную цепочку
                if (required != null){
                    File depFile = new File(this.originPath + "\\" + required + ".txt");
                    //записываем вывод рекурсии (цепочки зависимости файлов) в локальный буфер
                    localBuffer = localBuffer + getCombinedText(depFile, orderedFiles);
                }else {
                    //если директивы нет то просто записываем построчно данные файла
                    localBuffer = localBuffer + line + "\n";
                }
            }
            //закрываем объект считывания в конце рекурсии
            reader.close();
            //возвращаем буффер рекурсивного этапа
            return localBuffer;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    //функция для нахождения директивы в строке, с исполбзыванием regex
    private String reqRegex(String line){
        Matcher m = p.matcher(line);
        if(m.find()){
            return m.group(2);
        }
        return null;
    }

    //инициализатор класса directoryParser
    //автоматически считывает, сортирует и конкатенирует файлы при иницализации
    public directoryParser(String path){
        this.originPath = path;
        this.order = new ArrayList<>();

        parseAndOrderAllFiles(this.originPath);
        concatFileContents();
    }

    //функция для вывода отсортированного списка структуры файлов
    public List<File> getOrderedFiles(){
        return this.order;
    }
}
