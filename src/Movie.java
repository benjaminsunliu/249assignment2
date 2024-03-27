// -----------------------------------------------------
// Assignment 2
// Question: Movie class
// Written by: Vincent de Serres-40272920 and Benjamin Liu-
// -----------------------------------------------------

import java.io.Serializable;

public class Movie implements Serializable {
    private int year;
    private String title;
    private int duration;
    private String genre;
    private String rating;
    private double score;
    private String director;
    private String actor1;
    private String actor2;
    private String actor3;

    public Movie() {
        this.year = 0;
        this.title = "";
        this.duration = 0;
        this.genre = "";
        this.rating = "";
        this.score = 0.0;
        this.director = "";
        this.actor1 = "";
        this.actor2 = "";
        this.actor3 = "";
    }
    public Movie(int year, String title, int duration, String genre, String rating, double score, String director, String actor1, String actor2, String actor3) {
        this.year = year;
        this.title = title;
        this.duration = duration;
        this.genre = genre;
        this.rating = rating;
        this.score = score;
        this.director = director;
        this.actor1 = actor1;
        this.actor2 = actor2;
        this.actor3 = actor3;
    }
    public Movie(Movie movie) {
        this.year = movie.year;
        this.title = movie.title;
        this.duration = movie.duration;
        this.genre = movie.genre;
        this.rating = movie.rating;
        this.score = movie.score;
        this.director = movie.director;
        this.actor1 = movie.actor1;
        this.actor2 = movie.actor2;
        this.actor3 = movie.actor3;
    }
    public int getYear() {
        return year;
    }
    public String getTitle() {
        return title;
    }
    public int getDuration() {
        return duration;
    }
    public String getGenre() {
        return genre;
    }
    public String getRating() {
        return rating;
    }
    public double getScore() {
        return score;
    }
    public String getDirector() {
        return director;
    }
    public String getActor1() {
        return actor1;
    }
    public String getActor2() {
        return actor2;
    }
    public String getActor3() {
        return actor3;
    }
    public void setYear(int year) {
        this.year = year;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setDuration(int duration) {
        this.duration = duration;
    }
    public void setGenre(String genre) {
        this.genre = genre;
    }
    public void setRating(String rating) {
        this.rating = rating;
    }
    public void setScore(double score) {
        this.score = score;
    }
    public void setDirector(String director) {
        this.director = director;
    }
    public void setActor1(String actor1) {
        this.actor1 = actor1;
    }
    public void setActor2(String actor2) {
        this.actor2 = actor2;
    }
    public void setActor3(String actor3) {
        this.actor3 = actor3;
    }

    @Override
    public String toString() {
        return "Year: " + year + "\tTitle: " + title + "\tDuration: " + duration + "\tGenre: " + genre + "\tRating: " + rating + "\tScore: " + score + "\tDirector: " + director + "\tActor1: " + actor1 + "\tActor2: " + actor2 + "\tActor3: " + actor3;
    }
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Movie) {
            Movie movie = (Movie) obj;
            return year == movie.year && title.equals(movie.title) && duration == movie.duration && genre.equals(movie.genre) && rating.equals(movie.rating) && score == movie.score && director.equals(movie.director) && actor1.equals(movie.actor1) && actor2.equals(movie.actor2) && actor3.equals(movie.actor3);
        }
        return false;
    }
}
