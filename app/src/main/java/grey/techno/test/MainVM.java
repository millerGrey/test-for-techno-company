package grey.techno.test;

import android.util.Patterns;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class MainVM extends ViewModel {

    final static String SCAN_EVENT = "scan";
    final static String VALIDATE_EVENT = "validate";
    final static String NO_URL_EVENT = "no_url";

    List<String> urls = new ArrayList<>();
    private MutableLiveData<List<ChainData>> chains = new MutableLiveData<>(new ArrayList<>());
    MutableLiveData<String> event = new MutableLiveData<>("");
    MutableLiveData<Boolean> progress = new MutableLiveData<>(false);
    MutableLiveData<Boolean> validateBtnState = new MutableLiveData<>(false);
    private boolean isValidationOver = false;

    void finishValidation(List<ChainData> list) {
        chains.setValue(list);
        finishValidation();
    }

    void finishValidation() {
        progress.setValue(false);
        isValidationOver = true;
    }

    List<ChainData> getChains() {
        return chains.getValue();
    }

    boolean isValidationOver() {
        return isValidationOver;
    }


    void buttonScanListener() {
        event.setValue(SCAN_EVENT);
        chains = new MutableLiveData<>(new ArrayList<>());
        urls = new ArrayList<>();
        if (BuildConfig.FLAVOR.equals("emulator")) {
            parseURLs("https://yandex.ru\n" +
                    "Visit dubai.com! We'are here!\n" +
                    "https://vbankcenter.ru/ https://mail.yahoo.com/d/folders/1/messages/AKZ5BooBZVroX2CYhwBpeHfIKRI?reason=invalid_cred https://track.toggl.com/projects/3996957/list выадлыашозуцз а94шкльа https://ya.ru/ QR sample payloads:\n" +
                    "Our sites: http://site1.ru, http://site2.ru/cgi/users. Also check this https://one.site3.org?id=23");
        }
        isValidationOver = false;
    }

    void buttonValidateListener() {
        event.setValue(VALIDATE_EVENT);
        progress.setValue(true);
    }

    void parseURLs(String str) {
        Matcher m = Patterns.WEB_URL.matcher(str);
        while (m.find()) {
            urls.add(m.group());
            chains.getValue().add(new ChainData(m.group()));
        }
        if (urls.size() > 0) {
            validateBtnState.setValue(true);
        } else {
            event.setValue(NO_URL_EVENT);
        }
    }


}
