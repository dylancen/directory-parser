# directory-parser
Link to challenge: https://doczilla-hr.notion.site/4dc4a09b1d6944d78a7f7d77317cbb63

The solution is divided into 2 parts:
- reading the structure of the filesystem and sorting them based on their dependencies (and name)
- concatentaion of the files (based on their dependencies) into a single file

The solution is completed in the **directoryParser** class

The function for reading and sorting is **parseAndOrderAllFiles()**, that reads and sorts in parallel using the recursive function **addToOrder**

After reading the files are concatenated using **concatFileContents()**, that also uses a recursive function that reads all dependencies **getCombinedText(..)**

The program outputs the correctly sorted file structure list and also creates a file in the root folder with the name **output_file.txt** in which all the file contents are concatenated.

You can launch the program using the following command:

`java -classpath ".\out\production\combineFiles\" Main *absolute file path*`

Make sure that you are in the root folder of the project repository and that you have java 18 installed
