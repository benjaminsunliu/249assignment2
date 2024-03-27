// -----------------------------------------------------
// Assignment 2
// Question: Driver class
// Written by: Vincent de Serres-40272920 and Benjamin Liu-
// -----------------------------------------------------

import java.io.*;
import java.util.Scanner;

public class Driver {
	public static void main(String[] args) throws FileNotFoundException {
		// Initialize scanner for user input
		Scanner scanner = new Scanner(System.in);

		// Array of input file names
		String[] inputFiles = { "errorFile.csv", "Movies1990.csv", "Movies1991.csv", "Movies1992.csv", "Movies1993.csv",
				"Movies1994.csv", "Movies1995.csv", "Movies1996.csv", "Movies1997.csv", "Movies1998.csv",
				"Movies1999.csv" };

		// Array of genres
		String[] genres = { "musical", "comedy", "animation", "adventure", "drama", "crime", "biography", "horror",
				"action", "documentary", "fantasy", "mystery", "sci-fi", "family", "western", "romance", "thriller" };

		// Create a PrintWriter to write the file names to part1_manifest.txt
		PrintWriter p1Manifest = new PrintWriter(new FileOutputStream("part1_manifest.txt"), true);

		// Write input file names to part1_manifest.txt
		for (int i = 0; i < inputFiles.length; i++) {
			p1Manifest.println(inputFiles[i]);
			p1Manifest.flush();
		}

		// Close the PrintWriter for part1_manifest.txt
		p1Manifest.close();

		// Path to part1_manifest.txt
		String part1_manifest = "part1_manifest.txt";

		// Call do_part1 method to process input files and generate part2_manifest.txt
		String part2_manifest = do_part1(part1_manifest, genres);

		// Call do_part2 method to generate part3_manifest.txt
		String part3_manifest = do_part2(part2_manifest);

		// Call do_part3 method to deserialize movies from part3_manifest.txt
		Movie[][] moviesArr = do_part3(part3_manifest, genres);

		// Display main menu
		displayMainMenu(scanner, moviesArr, genres, 0);

		// Close the scanner
		scanner.close();
	}

	// Method to process part1
	public static String do_part1(String part1, String[] genres) {
		// Initialize part2_manifest file name
		String part2 = "part2_manifest.txt";
		try {
			// Create a PrintWriter to write good movie records to good_movie_records.txt
			PrintWriter pw = new PrintWriter(new FileOutputStream("good_movie_records.txt"), true);

			// Create a Scanner to read input files from part1_manifest.txt
			Scanner fileScanner = new Scanner(new FileInputStream(part1));

			// Create a PrintWriter to write bad movie records to bad_movie_records.txt
			PrintWriter badpw = new PrintWriter(new FileOutputStream("bad_movie_records.txt"), true);

			// Initialize line number counter
			int lineNumber = 0;

			// Read each line from the manifest file
			while (fileScanner.hasNextLine()) {
				try {
					// Read the file name from part1_manifest.txt
					String csv = fileScanner.nextLine();

					// Create a Scanner to read lines from the CSV file
					Scanner csvScanner = new Scanner(new FileInputStream(csv));

					// Initialize line number within the CSV file
					int lineInCSV = 0;

					// Read each line from the csv file
					while (csvScanner.hasNextLine()) {
						String line = csvScanner.nextLine();
						lineNumber++;
						lineInCSV++;

						// Try to process the line
						try {
							processMovieRecord(line, genres, csv, lineInCSV);
							pw.println(line);
							pw.flush();
						} catch (Exception e) {
							// Catch any exceptions and write the line to the bad_movie_records.txt file
							badpw.println("Error: " + e.getMessage() + " in file " + csv + " at line " + lineInCSV);
							badpw.println("Movie Record: " + line);
							badpw.println("Input File: " + csv);
							badpw.println("Line Number: " + lineInCSV);
							badpw.println();
						}
					}
				} catch (Exception e) {
					// Catch any invalid files in the manifest
				}
			}
			// Sort genre and update part2_manifest file name
			sortGenre(part2, genres);

			// Close PrintWriter objects
			pw.close();
			badpw.close();
		} catch (Exception e) {
			// Catch any exceptions
		}
		return part2;
	}

	public static String[] splitLine(String line)
			throws ExcessFieldsException, MissingQuotesException, MissingFieldsException, MissingFieldsException {
		// Assume there's a reasonable upper limit to detect excess fields
		String[] tempFields = new String[11]; // Temporary array to hold fields, allowing one extra for potential excess
		int fieldCount = 0; // Counter to keep track of the number of fields processed

		String currentField = ""; // Variable to accumulate characters for the current field
		boolean inQuotes = false; // Flag to indicate if currently inside quotes

		// Loop through each character in the input line
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);

			// Check if the character is a quote
			if (c == '"') {
				inQuotes = !inQuotes; // Toggle the inQuotes flag
				continue; // Optionally, skip adding quotes to the field
			}

			// Check if the character is a comma and not inside quotes
			if (c == ',' && !inQuotes) {
				tempFields[fieldCount] = currentField; // Store the accumulated field
				fieldCount++; // Increment the field count
				if (fieldCount > 10) { // Check for excess fields
					throw new ExcessFieldsException("Error: More than 10 fields found.");
				}
				currentField = ""; // Reset the current field for the next iteration
			} else {
				currentField += c; // Append the character to the current field
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
			if (line.endsWith(",")) { // If the line ends with a comma, then there is an empty field at the end
				fieldCount--; // Decrement fieldCount to remove the empty field
			} else { // If the line does not end with a comma, then there is an excess field
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

		return fields; // Return the array of fields
	}

	public static void processMovieRecord(String line, String[] genres, String inputFile, int lineNumber)
			throws BadYearException, BadTitleException, BadDurationException, BadGenreException, BadRatingException,
			BadScoreException, BadNameException, ExcessFieldsException, MissingFieldsException, MissingQuotesException {
		// Split the line into fields using the splitLine method.
		String[] fields = splitLine(line);

		// Handle Whitespaces
		for (int i = 0; i < fields.length; i++) { // trim all fields of leading/trailing whitespace
			fields[i] = fields[i].trim();
		}

		// Handle Quotes
		for (int i = 0; i < fields.length; i++) { // check for missing quotes and remove leading/trailing quotes if none
													// are missing
			if (fields[i].startsWith("\"") && !(fields[i].endsWith("\""))
					|| !(fields[i].startsWith("\"")) && fields[i].endsWith("\"")) {
				throw new MissingQuotesException();
			}

			// Continuously remove leading and trailing quotes while they exist.
			while (fields[i].startsWith("\"") && fields[i].endsWith("\"") && fields[i].length() > 1) {
				fields[i] = fields[i].substring(1, fields[i].length() - 1);
			}

		}

		// Check for bad fields
		try {
			// Check year
			if (fields[0] == null || fields[0].isEmpty() || !isInteger(fields[0]) || Integer.parseInt(fields[0]) > 1999
					|| Integer.parseInt(fields[0]) < 1990) {
				throw new BadYearException();
			}
			// Check title
			if (fields[1] == null || fields[1].isEmpty()) {
				throw new BadTitleException();
			}
			// Check duration
			if (!isInteger(fields[2]) || fields[2] == null || fields[2].isEmpty() || Integer.parseInt(fields[2]) < 30
					|| Integer.parseInt(fields[2]) > 300) {
				throw new BadDurationException();
			}

			// Check genre
			boolean validGenre = false;
			for (String genre : genres) {
				if (fields[3] != null && !fields[3].isEmpty() && fields[3].equalsIgnoreCase(genre)) {
					validGenre = true;
					break;
				}
			}
			if (!validGenre) {
				throw new BadGenreException();
			}

			// Check rating
			if (!(fields[4].toLowerCase().equals("pg") || fields[4].toLowerCase().equals("unrated")
					|| fields[4].toLowerCase().equals("g") || fields[4].toLowerCase().equals("r")
					|| fields[4].toLowerCase().equals("pg-13") || fields[4].toLowerCase().equals("nc-17"))) {
				throw new BadRatingException();
			}
			// Check score
			if (!isDouble(fields[5]) || fields[5] == null || fields[5].isEmpty() || fields[4] == null
					|| fields[4].isEmpty() || Double.parseDouble(fields[5]) < 0 || Double.parseDouble(fields[5]) > 10) {
				throw new BadScoreException();
			}
			// Check names
			for (int i = 6; i <= 9; i++) {
				if (fields[i] == null || fields[i].isEmpty()) {
					throw new BadNameException();
				}
			}
		} catch (Exception e) {
			// Write the invalid movie record to bad_movie_records.txt
			try (PrintWriter badpw = new PrintWriter(new FileWriter("bad_movie_records.txt", true))) {
				badpw.println("Error: " + e.getMessage() + " at line " + lineNumber + " of file " + inputFile);
				badpw.println("Movie Record: " + line);
				badpw.println("Input File: " + inputFile);
				badpw.println("Line Number: " + lineNumber);
				badpw.println();
			} catch (IOException ioException) {
				System.out.println("Error writing to bad_movie_records.txt: " + ioException.getMessage());
			}
			throw e; // Re-throw the exception to indicate failure in processing this record
		}

		// If the record is valid, write it to the corresponding genre file
		try (PrintWriter genreWriter = new PrintWriter(new FileWriter(fields[3].toLowerCase() + ".csv", true))) {
			genreWriter.println(line);
		} catch (IOException ioException) {
			System.out.println("Error writing to genre file: " + ioException.getMessage());
		}
	}

	public static void sortGenre(String part2, String[] genres)
			throws FileNotFoundException, MissingFieldsException, MissingQuotesException, ExcessFieldsException {
		// Open the file containing good movie records for reading
		Scanner fileScanner = new Scanner(new FileInputStream("good_movie_records.txt"));

		// Create PrintWriter array to hold writers for each genre file
		PrintWriter[] genreWriters = new PrintWriter[genres.length];

		// Initialize genre writers for each genre file
		for (int i = 0; i < genres.length; i++) {
			genreWriters[i] = new PrintWriter(new FileOutputStream(genres[i] + ".csv"), true);
		}

		// Create a PrintWriter to write the updated manifest file
		PrintWriter p2Manifest = new PrintWriter(new FileOutputStream(part2), true);

		// Write the genre filenames to the manifest file
		for (String genre : genres) {
			p2Manifest.println(genre + ".csv");
			p2Manifest.flush();
		}

		// Process each line in the good movie records file
		while (fileScanner.hasNextLine()) {
			String line = fileScanner.nextLine();
			String[] fields = splitLine(line);
			// Write the line to the corresponding genre file
			for (int i = 0; i < genres.length; i++) {
				if (fields[3].toLowerCase().equals(genres[i])) {
					genreWriters[i].println(line);
					genreWriters[i].flush();
				}
			}
		}
	}

	// Helper methods

	// isInteger method: Checks if a string represents an integer
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

	// isDouble method: Checks if a string represents a double
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
		// Initialize the output manifest file for part 3
		String part3 = "part3_manifest.txt";
		try {
			// Open the input manifest file for part 2
			Scanner fileScanner = new Scanner(new FileInputStream(part2));
			// Create a PrintWriter to write the manifest for part 3
			PrintWriter p3Manifest = new PrintWriter(new FileOutputStream(part3), true);

			// Iterate through each line in the part 2 manifest file
			while (fileScanner.hasNextLine()) {
				// Get the CSV file name from the manifest
				String csvFileName = fileScanner.nextLine();
				// Generate the corresponding serialized file name
				String serFileName = csvFileName.replace(".csv", ".ser");

				// First Pass: Count lines to determine array size
				int count = 0;
				try (Scanner countScanner = new Scanner(new FileInputStream(csvFileName))) {
					while (countScanner.hasNextLine()) {
						countScanner.nextLine();
						count++;
					}
				} catch (Exception e) {
					// Handle errors in counting lines
					System.out.println("Error counting lines: " + e.getMessage());
				}

				// Initialize array with determined size to store Movie objects
				Movie[] movies = new Movie[count];

				// Second Pass: Read CSV and populate array with Movie objects
				int index = 0;
				try (Scanner csvScanner = new Scanner(new FileInputStream(csvFileName))) {
					while (csvScanner.hasNextLine()) {
						String line = csvScanner.nextLine();
						String[] fields = splitLine(line);
						// Create Movie object and add it to the array
						movies[index++] = new Movie(Integer.parseInt(fields[0]), fields[1], Integer.parseInt(fields[2]),
								fields[3], fields[4], Double.parseDouble(fields[5]), fields[6], fields[7], fields[8],
								fields[9]);
					}
				} catch (Exception e) {
					// Handle errors in reading CSV
					System.out.println("Error reading CSV: " + e.getMessage());
				}

				// Serialize the array of Movie objects
				try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(serFileName))) {
					oos.writeObject(movies);
					// Write the serialized file name to the part 3 manifest
					p3Manifest.println(serFileName);
				} catch (Exception e) {
					// Handle errors in serializing
					System.out.println("Error serializing: " + e.getMessage());
				}
			}
			// Close the PrintWriter for the part 3 manifest
			p3Manifest.close();
		} catch (Exception e) {
			// Handle errors in do_part2 method
			System.out.println("Error in do_part2: " + e.getMessage());
		}
		// Return the path of the part 3 manifest file
		return part3;
	}

	public static Movie[][] do_part3(String part3, String[] genres) {
		Movie[][] movies = null;
		try {
			Scanner fileScanner = new Scanner(new FileInputStream(part3));
			Scanner countScanner = new Scanner(new FileInputStream(part3));

			// Iterate through each line in the part 3 manifest file
			while (fileScanner.hasNextLine()) {
				int arrsize = 0;
				// Count the number of movies for each genre
				for (int i = 0; i < genres.length; i++) {
					String genre = genres[i];
					if (countScanner.nextLine().contains(genre)) {
						arrsize++;
					}

				}
				// Initialize the 2D array to store movies
				movies = new Movie[arrsize][];

				// Populate the 2D array with Movie objects
				for (int i = 0; i < genres.length; i++) {
					if (fileScanner.nextLine().contains(genres[i])) {
						try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(genres[i] + ".ser"))) {
							movies[i] = (Movie[]) ois.readObject();
						} catch (Exception e) {
							// Handle errors in reading serialized file
							System.out.println("Error reading serialized file: " + e.getMessage());
						}
					}
				}
			}
		} catch (Exception e) {
			// Handle errors in do_part3 method
			System.out.println("Error in do_part3: " + e.getMessage());
		}
		// Return the 2D array containing Movie objects
		return movies;
	}

	public static void displayMainMenu(Scanner scanner, Movie[][] moviesArr, String[] genres, int choice) {
		// Display the main menu options
		System.out.println("\n\nWelcome to the Movie Database by Benjamin Liu and Vincent de Serres!");
		System.out.println("-----------------------------\n\t\tMain Menu\n-----------------------------");
		System.out.println("s. Select a movie array to navigate");
		System.out.println("n. Navigate " + genres[choice] + " movies" + " (" + moviesArr[choice].length + " records)");
		System.out.println("x. Exit\n-----------------------------\n");
		System.out.print("Enter your choice: ");
		String input = scanner.nextLine();
		switch (input.toLowerCase()) {
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

	public static void displaySubMenu(Scanner scanner, Movie[][] moviesArr, String[] genres) {
		// Display the genre sub-menu options
		System.out.println("-----------------------------\n\t\tGenre Sub-Menu\n-----------------------------");
		for (int i = 0; i < genres.length; i++) {
			System.out.println((i + 1) + ". " + genres[i] + " (" + moviesArr[i].length + " movies)");
		}
		System.out.println(genres.length + 1 + ". Exit\n-----------------------------\n");
		System.out.print("Enter your choice: ");
		String input = scanner.nextLine();
		try {
			int choice = Integer.parseInt(input);
			if (choice == genres.length + 1) {
				System.out.println("Goodbye!");
				System.exit(0);
			} else if (choice > 0 && choice <= genres.length) {
				displayMainMenu(scanner, moviesArr, genres, choice - 1);
			} else {
				System.out.println("Invalid input. Please try again.");
				displaySubMenu(scanner, moviesArr, genres);
			}
		} catch (Exception e) {
			System.out.println("Invalid input. Please try again.");
			displaySubMenu(scanner, moviesArr, genres);
		}
	}

	public static void movieNavigation(Scanner scanner, Movie[][] moviesArr, String[] genres, int choice) {
		// Display the movie navigation menu
		System.out.println("-----------------------------\n\t\tMovie Navigation\n-----------------------------");
		String genre = genres[choice];
		System.out.println("Navigating " + genre + " movies (" + moviesArr[choice].length + ")");
		System.out.print("Enter Your Choice: ");

		try {
			int input = Integer.parseInt(scanner.nextLine());
			// Call navigateMovies method based on user input
			navigateMovies(input, moviesArr[choice], choice, scanner, moviesArr, genres);
		} catch (NumberFormatException e) {
			System.out.println("Invalid input. Please enter an integer.");
			movieNavigation(scanner, moviesArr, genres, choice);
		}
	}

	public static void navigateMovies(int n, Movie[] movies, int choice, Scanner scanner, Movie[][] moviesArr,
			String[] genres) {
		int currentPosition = 0; // Current position within the movie array

		// Check if the user wants to end the viewing session
		if (n == 0) {
			// End the viewing session and display the main menu again
			displayMainMenu(scanner, moviesArr, genres, choice);
			return; // Exit the method
		}

		// Display the current movie record
		displayMovieRecord(currentPosition, movies, true); // Highlight current movie

		// Adjust the navigation based on the value of n
		if (n < 0) {
			// Moving backward
			int moves = Math.min(-n, currentPosition); // Determine how many records to display
			if (moves < Math.abs(n) - 1) {
				System.out.println("BOF has been reached."); // Notify if BOF is reached
				currentPosition = 0; // Reset current position to the beginning
			} else {
				// Display records above the current position
				for (int i = currentPosition - 1; i >= currentPosition - moves; i--) {
					displayMovieRecord(i, movies, false);
				}
				currentPosition -= moves; // Update current position
			}
		} else {
			// Moving forward
			int moves = Math.min(n, movies.length - currentPosition - 1); // Determine how many records to display
			if (moves < n) {
				// Display records below the current position
				for (int i = currentPosition + 1; i <= currentPosition + moves; i++) {
					displayMovieRecord(i, movies, false);
				}
				System.out.println("EOF has been reached."); // Notify if EOF is reached
				currentPosition += moves; // Update current position
			} else {
				// Display records below the current position
				for (int i = currentPosition + 1; i <= currentPosition + n - 1; i++) {
					displayMovieRecord(i, movies, false);
				}
				currentPosition += n - 1; // Update current position
			}
		}

		// Continue movie navigation
		movieNavigation(scanner, moviesArr, genres, choice);
	}

	public static void displayMovieRecord(int index, Movie[] movies, boolean isCurrentPosition) {
		// Display movie record
		System.out.println(movies[index].toString());
	}
}

//Exception class for indicating that the year in the movie record is invalid
class BadYearException extends Exception {
	// Parameterized constructor with a custom error message
	public BadYearException(String message) {
		super(message);
	}

	// Default constructor with a predefined error message
	public BadYearException() {
		super("Year must be between 1990 and 1999");
	}
}

//Exception class for indicating that the title in the movie record is invalid
class BadTitleException extends Exception {
	// Parameterized constructor with a custom error message
	public BadTitleException(String message) {
		super(message);
	}

	// Default constructor with a predefined error message
	public BadTitleException() {
		super("Title must be at least 1 character long");
	}
}

//Exception class for indicating that the genre in the movie record is invalid
class BadGenreException extends Exception {
	// Parameterized constructor with a custom error message
	public BadGenreException(String message) {
		super(message);
	}

	// Default constructor with a predefined error message
	public BadGenreException() {
		super("Genre not recognized");
	}
}

//Exception class for indicating that the score in the movie record is invalid
class BadScoreException extends Exception {
	// Parameterized constructor with a custom error message
	public BadScoreException(String message) {
		super(message);
	}

	// Default constructor with a predefined error message
	public BadScoreException() {
		super("Score must be between 0 and 10");
	}
}

//Exception class for indicating that the duration in the movie record is invalid
class BadDurationException extends Exception {
	// Parameterized constructor with a custom error message
	public BadDurationException(String message) {
		super(message);
	}

	// Default constructor with a predefined error message
	public BadDurationException() {
		super("Duration must be between 30 and 300 minutes");
	}
}

//Exception class for indicating that the rating in the movie record is invalid
class BadRatingException extends Exception {
	// Parameterized constructor with a custom error message
	public BadRatingException(String message) {
		super(message);
	}

	// Default constructor with a predefined error message
	public BadRatingException() {
		super("Rating not recognized");
	}
}

//Exception class for indicating that a name in the movie record is invalid
class BadNameException extends Exception {
	// Parameterized constructor with a custom error message
	public BadNameException(String message) {
		super(message);
	}

	// Default constructor with a predefined error message
	public BadNameException() {
		super("Name must be at least 1 character long");
	}
}

//Exception class for indicating that quotes are missing in a movie record
class MissingQuotesException extends Exception {
	// Parameterized constructor with a custom error message
	public MissingQuotesException(String message) {
		super(message);
	}

	// Default constructor with a predefined error message
	public MissingQuotesException() {
		super("Missing quotes");
	}
}

//Exception class for indicating that there are excess fields in a movie record
class ExcessFieldsException extends Exception {
	// Parameterized constructor with a custom error message
	public ExcessFieldsException(String message) {
		super(message);
	}

	// Default constructor with a predefined error message
	public ExcessFieldsException() {
		super("Excess fields");
	}
}

//Exception class for indicating that there are missing fields in a movie record
class MissingFieldsException extends Exception {
	// Parameterized constructor with a custom error message
	public MissingFieldsException(String message) {
		super(message);
	}

	// Default constructor with a predefined error message
	public MissingFieldsException() {
		super("Missing fields");
	}
}
