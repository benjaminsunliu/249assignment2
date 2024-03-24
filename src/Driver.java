import java.io.*;
import java.util.Scanner;

public class Driver {
    public static void main(String[] args) throws FileNotFoundException {
        Scanner scanner = new Scanner(System.in);
        String[] inputFiles = {"errorFile.csv","Movies1990.csv","Movies1991.csv","Movies1992.csv","Movies1993.csv","Movies1994.csv","Movies1995.csv","Movies1996.csv","Movies1997.csv","Movies1998.csv","Movies1999.csv"};
        String[] genres = {"musical", "comedy", "animation", "adventure", "drama", "crime", "biography", "horror", "action", "documentary", "fantasy", "mystery", "sci-fi", "family", "western", "romance", "thriller"};
        PrintWriter p1Manifest = new PrintWriter(new FileOutputStream("part1_manifest.txt"), true);
        for (String inputFile : inputFiles) {
            p1Manifest.println(inputFile);
            p1Manifest.flush();
        }
        String part1_manifest = "part1_manifest.txt";
        String part2_manifest = do_part1(part1_manifest, genres);
        String part3_manifest = do_part2(part2_manifest);
        Movie[][] moviesArr = do_part3(part3_manifest, genres);
        displayMainMenu(scanner, moviesArr, genres, 0);
    }

    public static String do_part1(String part1, String[] genres) {
        String part2 = "part2_manifest.txt";
        try {
            PrintWriter pw = new PrintWriter(new FileOutputStream("good_movie_records.txt"), true);
            Scanner fileScanner = new Scanner(new FileInputStream(part1));
            PrintWriter badpw = new PrintWriter(new FileOutputStream("bad_movie_records.txt"), true);
            while (fileScanner.hasNextLine()){ // Read each line from the manifest file
                try{ // Try to open the file in them manifest and read each line
                    String csv = fileScanner.nextLine();
                    System.out.println("Reading File: " + csv);

                    Scanner csvScanner = new Scanner(new FileInputStream(csv));
                    while (csvScanner.hasNextLine()) { // Read each line from the csv file
                        String line = csvScanner.nextLine();
                        try{ // Try to process the line
                            processMovieRecord(line, genres);
                            pw.println(line);
                            pw.flush();
                        }
                        catch (Exception e) { // Catch any exceptions and write the line to the bad_movie_records.txt file
                            badpw.println(line);
                            badpw.flush();
                            System.out.println("\t" + e.getMessage());
                        }
                    }
                }
                catch(Exception e){ // Catch any invalid files in the manifest
                    System.out.println("\t" + e.getMessage());
                }
            }
            sortGenre(part2 ,genres);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return part2;
    }

    public static String[] splitLine(String line) throws ExcessFieldsException, MissingQuotesException, MissingFieldsException, MissingFieldsException {
        // Assume there's a reasonable upper limit to detect excess fields
        String[] tempFields = new String[11];
        int fieldCount = 0;

        String currentField = "";
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes; // Toggle the inQuotes flag
                continue; // Optionally, skip adding quotes to the field
            }

            if (c == ',' && !inQuotes) {
                tempFields[fieldCount] = currentField;
                fieldCount++;
                if (fieldCount > 10) {
                    throw new ExcessFieldsException("Error: More than 10 fields found.");
                }
                currentField = "";
            } else {
                currentField += c;
            }
        }


        // Check for unclosed quotes at the end of the line
        if (inQuotes) {
            throw new MissingQuotesException("Error: Missing closing quote in line.");
        }

        // Add the last field since it won't be followed by a comma
        tempFields[fieldCount] = currentField;
        fieldCount++;

        // Check for excess fields
        if (fieldCount > 10) {
            if (line.endsWith(",")){ // If the line ends with a comma, then there is an empty field at the end
                fieldCount--; // Decrement fieldCount to remove the empty field
            }
            else{ // If the line does not end with a comma, then there is an excess field
                throw new ExcessFieldsException("Error: More than 10 fields found.");
            }
        }

        // Check for the correct number of fields
        if (fieldCount < 10) {
            throw new MissingFieldsException("Error: Missing fields. Expected 10, but found " + fieldCount + ".");
        }

        // Since fieldCount is exactly 10, we can directly use tempFields
        String[] fields = new String[fieldCount];
        System.arraycopy(tempFields, 0, fields, 0, fieldCount);

        return fields;
    }


    public static void processMovieRecord(String line, String[] genres) throws BadYearException, BadTitleException, BadDurationException, BadGenreException, BadRatingException, BadScoreException, BadNameException, ExcessFieldsException, MissingFieldsException, MissingQuotesException {
        String[] fields = splitLine(line); // Split the line into fields using the splitLine method.

        // Handle Whitespaces
        for (int i = 0; i < fields.length; i++) { // trim all fields of leading/trailing whitespace
            fields[i] = fields[i].trim();
        }

        // Handle Quotes
        for (int i = 0; i < fields.length; i++) { // check for missing quotes and remove leading/trailing quotes if none are missing
            if(fields[i].startsWith("\"") && !(fields[i].endsWith("\"")) || !(fields[i].startsWith("\"")) && fields[i].endsWith("\"")) {
                throw new MissingQuotesException();
            }

            // Continuously remove leading and trailing quotes while they exist.
            while (fields[i].startsWith("\"") && fields[i].endsWith("\"") && fields[i].length() > 1) {
                fields[i] = fields[i].substring(1, fields[i].length() - 1);
            }

        }

        // Check for bad fields
        if (fields[0] == null || fields[0].isEmpty() || !isInteger(fields[0]) || Integer.parseInt(fields[0]) > 1999 || Integer.parseInt(fields[0]) < 1990) {
            throw new BadYearException();
        }
        if (fields[1] == null || fields[1].isEmpty()) {
            throw new BadTitleException();
        }
        if (!isInteger(fields[2]) || fields[2] == null || fields[2].isEmpty() || Integer.parseInt(fields[2]) < 30 || Integer.parseInt(fields[2]) > 300) {
            throw new BadDurationException();
        }

        for (int i = 0; i < genres.length; i++) {
            if (fields[3] != null && !fields[3].isEmpty() && fields[3].toLowerCase().equals(genres[i])) {
                break;
            }
            else if (i == genres.length - 1) {
                throw new BadGenreException();
            }
        }

        /*if (fields[3] != null && !fields[3].isEmpty() && !(fields[3].toLowerCase().equals("musical") || fields[3].toLowerCase().equals("comedy") || fields[3].toLowerCase().equals("animation") || fields[3].toLowerCase().equals("adventure") || fields[3].toLowerCase().equals("drama") || fields[3].toLowerCase().equals("crime") || fields[3].toLowerCase().equals("biography") || fields[3].toLowerCase().equals("horror") || fields[3].toLowerCase().equals("action") || fields[3].toLowerCase().equals("documentary") || fields[3].toLowerCase().equals("fantasy") || fields[3].toLowerCase().equals("mystery") || fields[3].toLowerCase().equals("sci-fi") || fields[3].toLowerCase().equals("family") || fields[3].toLowerCase().equals("romance") || fields[3].toLowerCase().equals("thriller") || fields[3].toLowerCase().equals("western"))){
            throw new BadGenreException();
        }*/

        if (!(fields[4].toLowerCase().equals("pg") || fields[4].toLowerCase().equals("unrated") || fields[4].toLowerCase().equals("g") || fields[4].toLowerCase().equals("r") || fields[4].toLowerCase().equals("pg-13") || fields[4].toLowerCase().equals("nc-17"))) {
            throw new BadRatingException();
        }
        if (!isDouble(fields[5]) || fields[5] == null || fields[5].isEmpty() || fields[4] == null || fields[4].isEmpty() || Double.parseDouble(fields[5]) < 0 || Double.parseDouble(fields[5]) > 10) {
            throw new BadScoreException();
        }
        if (fields[6] == null || fields[6].isEmpty()) {
            throw new BadNameException();
        }
        if (fields[7] == null || fields[7].isEmpty()) {
            throw new BadNameException();
        }
        if (fields[8] == null || fields[8].isEmpty()) {
            throw new BadNameException();
        }
        if (fields[9] == null || fields[9].isEmpty()) {
            throw new BadNameException();
        }
    }

    public static void sortGenre(String part2,String[] genres) throws FileNotFoundException, MissingFieldsException, MissingQuotesException, ExcessFieldsException {
        Scanner fileScanner = new Scanner(new FileInputStream("good_movie_records.txt"));
        PrintWriter[] genreWriters = new PrintWriter[genres.length];

        for (int i = 0; i < genres.length; i++) {
            genreWriters[i] = new PrintWriter(new FileOutputStream(genres[i] + ".csv"), true);
        }

        PrintWriter p2Manifest = new PrintWriter(new FileOutputStream(part2), true);
        for (String genre : genres) {
            p2Manifest.println(genre + ".csv");
            p2Manifest.flush();
        }

        while (fileScanner.hasNextLine()) {
            String line = fileScanner.nextLine();
            String[] fields = splitLine(line);
            for (int i = 0; i < genres.length; i++) {
                if (fields[3].toLowerCase().equals(genres[i])) {
                    genreWriters[i].println(line);
                    genreWriters[i].flush();
                }
            }
        }
    }

    // Helper methods
    // isInteger method
    public static boolean isInteger(String str) {
        if (str == null) {
            return false;
        }
        try {
            Integer.parseInt(str.trim());
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    // Check if a string is a double
    public static boolean isDouble(String str) {
        if (str == null) {
            return false;
        }
        try {
            Double.parseDouble(str.trim());
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
    public static String do_part2(String part2) {
        String part3 = "part3_manifest.txt";
        try {
            Scanner fileScanner = new Scanner(new FileInputStream(part2));
            PrintWriter p3Manifest = new PrintWriter(new FileOutputStream(part3), true);

            while (fileScanner.hasNextLine()) {
                String csvFileName = fileScanner.nextLine();
                String serFileName = csvFileName.replace(".csv", ".ser");

                // First Pass: Count lines to determine array size
                int count = 0;
                try (Scanner countScanner = new Scanner(new FileInputStream(csvFileName))) {
                    while (countScanner.hasNextLine()) {
                        countScanner.nextLine();
                        count++;
                    }
                } catch (Exception e) {
                    System.out.println("Error counting lines: " + e.getMessage());
                }

                // Initialize array with determined size
                Movie[] movies = new Movie[count];

                // Second Pass: Read CSV and populate array
                int index = 0;
                try (Scanner csvScanner = new Scanner(new FileInputStream(csvFileName))) {
                    while (csvScanner.hasNextLine()) {
                        String line = csvScanner.nextLine();
                        String[] fields = splitLine(line);
                        movies[index++] = new Movie(Integer.parseInt(fields[0]), fields[1], Integer.parseInt(fields[2]), fields[3], fields[4], Double.parseDouble(fields[5]), fields[6], fields[7], fields[8], fields[9]);
                    }
                } catch (Exception e) {
                    System.out.println("Error reading CSV: " + e.getMessage());
                }

                // Serialize the array of Movies
                try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(serFileName))) {
                    oos.writeObject(movies);
                    p3Manifest.println(serFileName);
                } catch (Exception e) {
                    System.out.println("Error serializing: " + e.getMessage());
                }
            }
            p3Manifest.close();
        } catch (Exception e) {
            System.out.println("Error in do_part2: " + e.getMessage());
        }
        return part3;
    }

    public static Movie[][] do_part3(String part3, String[] genres) {
        Movie[][] movies = null;
        try {
            Scanner fileScanner = new Scanner(new FileInputStream(part3));
            Scanner countScanner = new Scanner(new FileInputStream(part3));
            while (fileScanner.hasNextLine()) {
                int arrsize = 0;
                for (String genre : genres) {
                    if (countScanner.nextLine().contains(genre)) {
                        arrsize++;
                    }
                }
                movies = new Movie[arrsize][];
                for (int i = 0; i < genres.length; i++) {
                    if (fileScanner.nextLine().contains(genres[i])) {
                        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(genres[i] + ".ser"))) {
                            movies[i] = (Movie[]) ois.readObject();
                        } catch (Exception e) {
                            System.out.println("Error reading serialized file: " + e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error in do_part3: " + e.getMessage());
        }
        return movies;
    }

    public static void displayMainMenu(Scanner scanner, Movie[][] moviesArr, String[] genres, int choice) {
        String genre = genres[choice];
        System.out.println("\n\nWelcome to the Movie Database by Benjamin Liu!");
        System.out.println("-----------------------------\n\t\tMain Menu\n-----------------------------");
        System.out.println("s. Select a movie array to navigate");
        System.out.println("n. Navigate " + genre + " movies" + " (" + moviesArr[choice].length + " records)");
        System.out.println("x. Exit\n-----------------------------\n");
        System.out.print("Enter your choice: ");
        String input = scanner.nextLine();
        switch (input.toLowerCase()){
            case "s":
                displaySubMenu(scanner, moviesArr, genres);
                break;
            case "n":
                movieNavigation(scanner, moviesArr, genres, choice);
                break;
            case "x":
                System.out.println("Goodbye!");
                System.exit(0);
                break;
            default:
                System.out.println("Invalid input. Please try again.");
                displayMainMenu(scanner, moviesArr, genres, choice);
        }

    }

    public static void displaySubMenu(Scanner scanner, Movie[][] moviesArr, String[] genres){
        System.out.println("-----------------------------\n\t\tGenre Sub-Menu\n-----------------------------");
        for (int i = 0; i < genres.length; i++) {
            System.out.println((i + 1) + ". " + genres[i] + " (" + moviesArr[i].length + " movies)");
        }
        System.out.println(genres.length+1 + ". Exit\n-----------------------------\n");
        System.out.print("Enter your choice: ");
        String input = scanner.nextLine();
        try{
            int choice = Integer.parseInt(input);
            if (choice == genres.length + 1) {
                System.out.println("Goodbye!");
                System.exit(0);
            } else if (choice > 0 && choice <= genres.length) {
                displayMainMenu(scanner, moviesArr, genres, choice - 1);
            }
            else {
                System.out.println("Invalid input. Please try again.");
                displaySubMenu(scanner, moviesArr, genres);
            }
        }
        catch (Exception e){
            System.out.println("Invalid input. Please try again.");
            displaySubMenu(scanner, moviesArr, genres);
        }
    }
    public static void movieNavigation(Scanner scanner, Movie[][] moviesarr, String[] genres, int choice){
        System.out.println("-----------------------------\n\t\tMovie Navigation\n-----------------------------");
        System.out.println("Navigating " + genres[choice] + " movies" + " (" + moviesarr[choice].length + ")");
        System.out.println("Enter Your Choice: ");
        try{
            String input = scanner.nextLine();
            int n = Integer.parseInt(input);
            /*if (n > 0 && n <= moviesarr[choice].length){
                System.out.println(moviesarr[choice][n-1].toString());
                System.out.println("Press Enter to continue...");
                scanner.nextLine();
                movieNavigation(scanner, moviesarr, genres, choice);
            }
            else{
                System.out.println("Invalid input. Please try again.");
                movieNavigation(scanner, moviesarr, genres, choice);
            }*/
        }
        catch (Exception e){
            System.out.println("Invalid input. Please try again.");
            movieNavigation(scanner, moviesarr, genres, choice);
        }
    }

}

// Exceptions
class BadYearException extends Exception {
    public BadYearException(String message) {
        super(message);
    }
    public BadYearException() {
        super("Year must be between 1990 and 1999");
    }
}
class BadTitleException extends Exception {
    public BadTitleException(String message) {
        super(message);
    }
    public BadTitleException() {
        super("Title must be at least 1 character long");
    }
}
class BadGenreException extends Exception {
    public BadGenreException(String message) {
        super(message);
    }
    public BadGenreException() {
        super("Genre not recognized");
    }
}
class BadScoreException extends Exception {
    public BadScoreException(String message) {
        super(message);
    }
    public BadScoreException() {
        super("Score must be between 0 and 10");
    }
}
class BadDurationException extends Exception {
    public BadDurationException(String message) {
        super(message);
    }
    public BadDurationException() {
        super("Duration must be between 30 and 300 minutes");
    }
}
class BadRatingException extends Exception {
    public BadRatingException(String message) {
        super(message);
    }
    public BadRatingException() {
        super("Rating not recognized");
    }
}
class BadNameException extends Exception {
    public BadNameException(String message) {
        super(message);
    }
    public BadNameException() {
        super("Name must be at least 1 character long");
    }
}
class MissingQuotesException extends Exception {
    public MissingQuotesException(String message) {
        super(message);
    }
    public MissingQuotesException() {
        super("Missing quotes");
    }
}
class ExcessFieldsException extends Exception {
    public ExcessFieldsException(String message) {
        super(message);
    }
    public ExcessFieldsException() {
        super("Excess fields");
    }
}
class MissingFieldsException extends Exception {
    public MissingFieldsException(String message) {
        super(message);
    }
    public MissingFieldsException() {
        super("Missing fields");
    }
}