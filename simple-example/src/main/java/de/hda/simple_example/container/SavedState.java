package de.hda.simple_example.container;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.view.View;

public class SavedState extends View.BaseSavedState {

    private boolean focused;
    private boolean expanded;
    private String query;

    public SavedState(Parcel source) {
        super(source);
        focused = source.readInt() == 1;
        expanded = source.readInt() == 1;
        query = source.readString();
    }

    public SavedState(Parcelable superState) {
        super(superState);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(focused ? 1 : 0);
        dest.writeInt(expanded ? 1 : 0);
        dest.writeString(query);
    }

    public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
        @Override
        public SavedState createFromParcel(Parcel in) {
            return new SavedState(in);
        }

        @Override
        public SavedState[] newArray(int size) {
            return new SavedState[size];
        }
    };

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public boolean isFocused() {
        return focused;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
