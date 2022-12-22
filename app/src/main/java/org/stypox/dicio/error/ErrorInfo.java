package org.stypox.dicio.error;

import static org.stypox.dicio.error.ExceptionUtils.getStackTraceString;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @implNote Taken with some modifications from NewPipe, file error/ErrorInfo.kt
 */
public class ErrorInfo implements Parcelable {

    public static final Creator<ErrorInfo> CREATOR = new Creator<>() {
        @Override
        public ErrorInfo createFromParcel(final Parcel in) {
            return new ErrorInfo(in);
        }

        @Override
        public ErrorInfo[] newArray(final int size) {
            return new ErrorInfo[size];
        }
    };


    @NonNull private final String stackTrace;
    @NonNull private final UserAction userAction;

    public ErrorInfo(@Nullable final Throwable throwable, @NonNull final UserAction userAction) {
        this.stackTrace = throwable == null ? "" : getStackTraceString(throwable);
        this.userAction = userAction;
    }

    private ErrorInfo(final Parcel parcel) {
        this.stackTrace = parcel.readString();
        this.userAction = parcel.readParcelable(UserAction.class.getClassLoader());
    }


    @NonNull
    public String getStackTrace() {
        return stackTrace;
    }

    @NonNull
    public UserAction getUserAction() {
        return userAction;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(stackTrace);
        dest.writeParcelable(userAction, flags);
    }
}
