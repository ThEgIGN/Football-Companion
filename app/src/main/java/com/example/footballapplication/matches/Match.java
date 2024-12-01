package com.example.footballapplication.matches;

public class Match {

    private final String tournamentName, homeTeam, awayTeam, dateTime, status;
    private MatchType type;
    private final long unixTime;
    private int id;
    private final int homeScore;
    private final int awayScore;

    public Match(String tournamentName, String homeTeam, String awayTeam, String dateTime, MatchType type, int id, int homeScore, int awayScore, long unixTime, String status) {
        this.tournamentName = tournamentName;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.dateTime = dateTime;
        this.type = type;
        this.id = id;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.unixTime = unixTime;
        this.status = status;
    }

    public String getTournamentName() {
        return tournamentName;
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public String getAwayTeam() {
        return awayTeam;
    }

    public String getDateTime() {
        return dateTime;
    }

    public MatchType getType() {
        return type;
    }

    public void setType(MatchType type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHomeScore() {
        return homeScore;
    }

    public int getAwayScore() {
        return awayScore;
    }

    public long getUnixTime() {
        return unixTime;
    }

    public String getStatus() {
        return status;
    }
}
