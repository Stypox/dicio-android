package org.dicio.dicio_android.error;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * The user actions that can cause an error.
 * @implNote Taken with some modifications from NewPipe, file error/UserAction.java
 */
public enum UserAction implements Parcelable {
    SKILL_EVALUATION("Skill evaluation");


    public static final Creator<UserAction> CREATOR = new Creator<>() {
        @Override
        public UserAction createFromParcel(final Parcel in) {
            return UserAction.values()[in.readInt()];
        }

        @Override
        public UserAction[] newArray(final int size) {
            return new UserAction[size];
        }
    };


    private final String message;

    UserAction(final String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(ordinal());
    }
}
