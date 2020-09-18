package grey.techno.test;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class ChainData implements Parcelable {
    private String baseURL;
    private List<String> subjects = new ArrayList<>();
    private List<Boolean> validations = new ArrayList<>();

    ChainData(String url) {
        baseURL = url;
    }

    void add(String issuer, Boolean validation) {
        subjects.add(issuer);
        validations.add(validation);
    }

    public String getBaseURL() {
        return baseURL;
    }

    public List<String> getSubjects() {
        return subjects;
    }

    public List<Boolean> getValidations() {
        return validations;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.baseURL);
        dest.writeStringList(this.subjects);
        dest.writeList(this.validations);
    }

    protected ChainData(Parcel in) {
        this.baseURL = in.readString();
        this.subjects = in.createStringArrayList();
        this.validations = new ArrayList<Boolean>();
        in.readList(this.validations, Boolean.class.getClassLoader());
    }

    public static final Parcelable.Creator<ChainData> CREATOR = new Parcelable.Creator<ChainData>() {
        @Override
        public ChainData createFromParcel(Parcel source) {
            return new ChainData(source);
        }

        @Override
        public ChainData[] newArray(int size) {
            return new ChainData[size];
        }
    };
}
