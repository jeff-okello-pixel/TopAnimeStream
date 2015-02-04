package com.topanimestream.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Arrays;

public class Review implements Parcelable {
    private int ReviewId;
    private int AccountId;
    private int AnimeId;
    private String Value;
    private int LanguageId;
    private String AddedDate;
    private int StoryRating;
    private int ArtRating;
    private int CharacterRating;
    private int EnjoymentRating;
    private int HelpfulCount;
    private int NotHelpfulCount;
    private int SoundRating;
    private int OverallRating;
    private Account Account;
    private String SeparatorTitle;//only used in listviews
    public Review() {
    }
    public static Review CreateReviewSeparator(String SeparatorTitle)
    {
        Review review = new Review();
        review.setSeparatorTitle(SeparatorTitle);
        return review;

    }
    public Review(Parcel in) {

        ReviewId = in.readInt();
        AccountId = in.readInt();
        AnimeId = in.readInt();
        Value = in.readString();
        LanguageId = in.readInt();
        AddedDate = in.readString();
        StoryRating = in.readInt();
        ArtRating = in.readInt();
        CharacterRating = in.readInt();
        EnjoymentRating = in.readInt();
        HelpfulCount = in.readInt();
        NotHelpfulCount = in.readInt();
        SoundRating = in.readInt();
        OverallRating = in.readInt();
        Account = (Account)in.readParcelable(Account.getClass().getClassLoader());
        SeparatorTitle = in.readString();
    }
    public String getSeparatorTitle() {
        return SeparatorTitle;
    }

    public void setSeparatorTitle(String separatorTitle) {
        SeparatorTitle = separatorTitle;
    }

    public Account getAccount() {
        return Account;
    }

    public void setAccount(Account account) {
        Account = account;
    }

    public int getReviewId() {
        return ReviewId;
    }

    public void setReviewId(int reviewId) {
        ReviewId = reviewId;
    }

    public int getAccountId() {
        return AccountId;
    }

    public void setAccountId(int accountId) {
        AccountId = accountId;
    }

    public int getAnimeId() {
        return AnimeId;
    }

    public void setAnimeId(int animeId) {
        AnimeId = animeId;
    }

    public String getValue() {
        return Value;
    }

    public void setValue(String value) {
        Value = value;
    }

    public int getLanguageId() {
        return LanguageId;
    }

    public void setLanguageId(int languageId) {
        LanguageId = languageId;
    }

    public String getAddedDate() {
        return AddedDate;
    }

    public void setAddedDate(String addedDate) {
        AddedDate = addedDate;
    }

    public int getStoryRating() {
        return StoryRating;
    }

    public void setStoryRating(int storyRating) {
        StoryRating = storyRating;
    }

    public int getArtRating() {
        return ArtRating;
    }

    public void setArtRating(int artRating) {
        ArtRating = artRating;
    }

    public int getCharacterRating() {
        return CharacterRating;
    }

    public void setCharacterRating(int characterRating) {
        CharacterRating = characterRating;
    }

    public int getEnjoymentRating() {
        return EnjoymentRating;
    }

    public void setEnjoymentRating(int enjoymentRating) {
        EnjoymentRating = enjoymentRating;
    }

    public int getHelpfulCount() {
        return HelpfulCount;
    }

    public void setHelpfulCount(int helpfulCount) {
        HelpfulCount = helpfulCount;
    }

    public int getNotHelpfulCount() {
        return NotHelpfulCount;
    }

    public void setNotHelpfulCount(int notHelpfulCount) {
        NotHelpfulCount = notHelpfulCount;
    }

    public int getSoundRating() {
        return SoundRating;
    }

    public void setSoundRating(int soundRating) {
        SoundRating = soundRating;
    }

    public int getOverallRating() {
        return OverallRating;
    }

    public void setOverallRating(int overallRating) {
        OverallRating = overallRating;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ReviewId);
        dest.writeInt(AccountId);
        dest.writeInt(AnimeId);
        dest.writeString(Value);
        dest.writeInt(LanguageId);
        dest.writeString(AddedDate);
        dest.writeInt(StoryRating);
        dest.writeInt(ArtRating);
        dest.writeInt(CharacterRating);
        dest.writeInt(EnjoymentRating);
        dest.writeInt(HelpfulCount);
        dest.writeInt(NotHelpfulCount);
        dest.writeInt(SoundRating);
        dest.writeInt(OverallRating);
        dest.writeParcelable(Account, flags);
        dest.writeString(SeparatorTitle);
    }
}
