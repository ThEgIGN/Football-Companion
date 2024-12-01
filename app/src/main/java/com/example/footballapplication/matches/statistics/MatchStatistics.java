package com.example.footballapplication.matches.statistics;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class MatchStatistics implements Parcelable {

    private final String statisticTitle;
    private final int homeStatistic;
    private final int awayStatistic;
    private final int homeColor;
    private final int awayColor;
    private final int order;
    private final boolean percentage;

    public MatchStatistics(String statisticTitle, int homeStatistic, int awayStatistic, boolean percentage, int homeColor, int awayColor, int order) {
        this.statisticTitle = statisticTitle;
        this.homeStatistic = homeStatistic;
        this.awayStatistic = awayStatistic;
        this.percentage = percentage;
        this.homeColor = homeColor;
        this.awayColor = awayColor;
        this.order = order;
    }

    protected MatchStatistics(Parcel in) {
        statisticTitle = in.readString();
        homeStatistic = in.readInt();
        awayStatistic = in.readInt();
        homeColor = in.readInt();
        awayColor = in.readInt();
        order = in.readInt();
        percentage = in.readByte() != 0;
    }

    public static final Creator<MatchStatistics> CREATOR = new Creator<MatchStatistics>() {
        @Override
        public MatchStatistics createFromParcel(Parcel in) {
            return new MatchStatistics(in);
        }

        @Override
        public MatchStatistics[] newArray(int size) {
            return new MatchStatistics[size];
        }
    };

    public String getStatisticTitle() {
        return statisticTitle;
    }

    public int getHomeStatistic() {
        return homeStatistic;
    }

    public int getAwayStatistic() {
        return awayStatistic;
    }

    public boolean isPercentage() {
        return percentage;
    }

    public int getAwayColor() {
        return awayColor;
    }

    public int getHomeColor() {
        return homeColor;
    }

    public int getOrder() {
        return order;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(statisticTitle);
        parcel.writeInt(homeStatistic);
        parcel.writeInt(awayStatistic);
        parcel.writeInt(homeColor);
        parcel.writeInt(awayColor);
        parcel.writeInt(order);
        parcel.writeByte((byte) (percentage ? 1 : 0));
    }
}
