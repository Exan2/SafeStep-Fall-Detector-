package com.falldetector.safestep;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class UserInfoActivity extends AppCompatActivity {
    
    private static final String PREFS_NAME = "UserInfoPrefs";
    
    private EditText etName;
    private EditText etDateOfBirth;
    private EditText etIdentityNumber;
    private AutoCompleteTextView etBloodType;
    private AutoCompleteTextView etWilaya;
    private AutoCompleteTextView etDaira;
    private EditText etPhoneNumber;
    private Button btnSave;
    
    private ArrayAdapter<String> bloodTypeAdapter;
    private ArrayAdapter<String> wilayaAdapter;
    private ArrayAdapter<String> dairaAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        
        initViews();
        loadUserData();
    }
    
    private void initViews() {
        etName = findViewById(R.id.et_name);
        etDateOfBirth = findViewById(R.id.et_dob);
        etIdentityNumber = findViewById(R.id.et_id);
        etBloodType = findViewById(R.id.et_blood_type);
        etWilaya = findViewById(R.id.et_wilaya);
        etDaira = findViewById(R.id.et_daira);
        etPhoneNumber = findViewById(R.id.et_phone);
        btnSave = findViewById(R.id.btn_save);
        
        setupDropdowns();
        setupDatePicker();
        setupClickListeners();
    }
    
    private void setupDropdowns() {
        // Blood Type Dropdown
        bloodTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, 
                getResources().getStringArray(R.array.blood_types));
        etBloodType.setAdapter(bloodTypeAdapter);
        etBloodType.setThreshold(0);
        etBloodType.setOnClickListener(v -> etBloodType.showDropDown());
        
        // Wilaya Dropdown
        wilayaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, 
                getResources().getStringArray(R.array.wilayas));
        etWilaya.setAdapter(wilayaAdapter);
        etWilaya.setThreshold(0);
        etWilaya.setOnClickListener(v -> etWilaya.showDropDown());
        
        // Daira Dropdown (will be populated based on wilaya selection)
        dairaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        etDaira.setAdapter(dairaAdapter);
        etDaira.setThreshold(0);
        etDaira.setOnClickListener(v -> {
            if (dairaAdapter.getCount() > 0) {
                etDaira.showDropDown();
            } else {
                Toast.makeText(this, "Please select a wilaya first", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void setupDatePicker() {
        etDateOfBirth.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String date = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                        etDateOfBirth.setText(date);
                    }, year, month, day);
            
            // Set max date to today
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePickerDialog.show();
        });
    }
    
    private void setupClickListeners() {
        btnSave.setOnClickListener(v -> saveUserData());
        
        // Wilaya selection listener to update daira options
        etWilaya.setOnItemClickListener((parent, view, position, id) -> {
            String selectedWilaya = etWilaya.getText().toString();
            updateDairaOptions(selectedWilaya);
            etDaira.setText(""); // Clear daira selection
        });
    }
    
    private void updateDairaOptions(String wilaya) {
        String[] dairas = getDairasForWilaya(wilaya);
        dairaAdapter.clear();
        dairaAdapter.addAll(dairas);
        dairaAdapter.notifyDataSetChanged();
    }
    
    private String[] getDairasForWilaya(String wilaya) {
        // Comprehensive daira mapping for major wilayas
        switch (wilaya) {
            case "Alger":
                return new String[]{"Alger Centre", "Bab El Oued", "Hussein Dey", "Kouba", "El Harrach", 
                                  "Baraki", "Dar El Beïda", "Bourouba", "Oued Smar", "Reghaïa"};
            case "Oran":
                return new String[]{"Oran Centre", "Aïn El Turk", "Arzew", "Bethioua", "Boufatis", 
                                  "El Ançor", "Es Senia", "Gdyel", "Hassi Ben Okba", "Mers El Kébir"};
            case "Constantine":
                return new String[]{"Constantine Centre", "Aïn Abid", "Aïn Smara", "Beni Hamidane", 
                                  "El Khroub", "Hamma Bouziane", "Ibn Ziad", "Ouled Rahmoune", "Zighoud Youcef"};
            case "Annaba":
                return new String[]{"Annaba Centre", "Aïn Barbar", "Berrahal", "Chetaibi", "Echatt", 
                                  "El Bouni", "El Hadjar", "Seraïdi"};
            case "Blida":
                return new String[]{"Blida Centre", "Aïn Romana", "Beni Kreddane", "Boufarik", "Bouïnoura", 
                                  "Bouïra", "Chiffa", "El Affroun", "Hammam Melouane", "Larbaa"};
            case "Sétif":
                return new String[]{"Sétif Centre", "Aïn Arnat", "Aïn Azel", "Aïn El Kebira", "Aïn Lahdjar", 
                                  "Aïn Oulmene", "Amoucha", "Babor", "Béni Aziz", "Béni Ourtilane"};
            case "Tlemcen":
                return new String[]{"Tlemcen Centre", "Aïn Tallout", "Aïn Youcef", "Bab El Assa", 
                                  "Bensekrane", "Bouhlou", "Chetouane", "Fellaoucene", "Ghazaouet", "Hennaya"};
            case "Batna":
                return new String[]{"Batna Centre", "Aïn Djasser", "Aïn Touta", "Arris", "Barika", 
                                  "Bouzina", "Chemora", "Djezzar", "El Madher", "Fesdis"};
            case "Djelfa":
                return new String[]{"Djelfa Centre", "Aïn El Ibel", "Aïn Oussara", "Birine", "Charef", 
                                  "Dar Chioukh", "El Idrissia", "Hassi Bahbah", "Messaad", "Zaccar"};
            case "Béjaïa":
                return new String[]{"Béjaïa Centre", "Adekar", "Aït Rizine", "Amizour", "Aokas", 
                                  "Barbacha", "Darguina", "El Kseur", "Feraoun", "Kherrata"};
            default:
                // Common dairas for other wilayas
                return new String[]{"Centre", "Nord", "Sud", "Est", "Ouest", "Capitale", "Principale", 
                                  "Ville", "Commune", "District"};
        }
    }
    
    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        etName.setText(prefs.getString("name", ""));
        etDateOfBirth.setText(prefs.getString("dob", ""));
        etIdentityNumber.setText(prefs.getString("id", ""));
        etBloodType.setText(prefs.getString("blood_type", ""));
        etWilaya.setText(prefs.getString("wilaya", ""));
        etDaira.setText(prefs.getString("daira", ""));
        etPhoneNumber.setText(prefs.getString("phone", ""));
    }
    
    private void saveUserData() {
        String name = etName.getText().toString().trim();
        String dob = etDateOfBirth.getText().toString().trim();
        String id = etIdentityNumber.getText().toString().trim();
        String bloodType = etBloodType.getText().toString().trim();
        String wilaya = etWilaya.getText().toString().trim();
        String daira = etDaira.getText().toString().trim();
        String phone = etPhoneNumber.getText().toString().trim();
        
        // Basic validation
        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Name and phone number are required", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Save to SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        editor.putString("name", name);
        editor.putString("dob", dob);
        editor.putString("id", id);
        editor.putString("blood_type", bloodType);
        editor.putString("wilaya", wilaya);
        editor.putString("daira", daira);
        editor.putString("phone", phone);
        
        editor.apply();
        
        Toast.makeText(this, "Information saved successfully", Toast.LENGTH_SHORT).show();
        
        // Go to Emergency Demo screen
        startActivity(new Intent(this, EmergencyDemoActivity.class));
        finish();
    }
}
