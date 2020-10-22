package grey.techno.test;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;

import static android.view.Gravity.BOTTOM;


public class MainActivity extends AppCompatActivity {

    final static String URL_LIST = "url list";
    final static String PENDING_INTENT = "pending intent";
    final static String CHAIN_LIST = "chain list";
    final static int CODE_LOADER_RESULT_OK = 0;
    final static int CODE_LOADER_RESULT_FAIL = 1;
    final static int CODE_LOADER_REQUEST = 0;
    private MainVM mainVM;
    private boolean isFirstValue = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainVM = new ViewModelProvider(this).get(MainVM.class);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MainFragment(), "main").commit();
        setSupportActionBar(findViewById(R.id.app_bar));
        invalidateOptionsMenu();
        mainVM.event.observe(this, value -> {
            if (isFirstValue) {
                isFirstValue = false;
                return;
            }
            if (value.equals(MainVM.NO_URL_EVENT)) {
                makeToast(getResources().getString(R.string.no_url));
            } else if (value.equals(MainVM.SCAN_EVENT)) {
//                startScan();
            } else if (value.equals(MainVM.VALIDATE_EVENT)) {
//                startValidationService();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (BuildConfig.FLAVOR.equals("device")) {
            menu.getItem(1).setVisible(mainVM.validateBtnState.getValue());
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.scan) {
            mainVM.buttonScanListener();
            if (BuildConfig.FLAVOR.equals("device")) {
                startScan();
            }
            return true;
        } else if(id == R.id.validate){
            mainVM.buttonValidateListener();
            startValidationService();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (requestCode == CODE_LOADER_REQUEST) {
                if (resultCode == CODE_LOADER_RESULT_OK) {
                    mainVM.finishValidation(data.getParcelableArrayListExtra(CHAIN_LIST));
                } else if (resultCode == CODE_LOADER_RESULT_FAIL) {
                    makeToast(getResources().getString(R.string.network_fail));
                    mainVM.finishValidation();
                }
                return;
            }
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null && result.getContents() != null) {
                if (BuildConfig.FLAVOR.equals("device")) {
                    mainVM.parseURLs(result.getContents());
                }
            } else {
                makeToast(getResources().getString(R.string.scan_fail));
            }
        }
    }

    @Override
    protected void onResume() {
        invalidateOptionsMenu();
        super.onResume();
    }

    void makeToast(String str) {
        Toast toast = Toast.makeText(this, str, Toast.LENGTH_SHORT);
        toast.setGravity(BOTTOM, 0, 300);
        toast.show();
    }

    void startValidationService() {
        PendingIntent pi = createPendingResult(CODE_LOADER_REQUEST, new Intent(), 0);
        Intent intent = new Intent(this, LoadCertService.class);
        intent.putStringArrayListExtra(URL_LIST, (ArrayList<String>) mainVM.urls);
        intent.putExtra(PENDING_INTENT, pi);
        startService(intent);
    }

    void startScan(){
        IntentIntegrator in = new IntentIntegrator(this);
        in.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        in.setBeepEnabled(false);
        in.setPrompt("");
        in.setBarcodeImageEnabled(true);
        in.setOrientationLocked(false);
        in.initiateScan();
    }
}
