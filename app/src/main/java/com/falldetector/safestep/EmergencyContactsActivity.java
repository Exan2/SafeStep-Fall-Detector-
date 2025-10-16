package com.falldetector.safestep;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import android.os.Looper;
import android.provider.Settings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class EmergencyContactsActivity extends AppCompatActivity {
    
    private static final String PREFS_NAME = "EmergencyContactsPrefs";
    private static final String CONTACTS_KEY = "emergency_contacts";
    
    private RecyclerView recyclerViewContacts;
    private Button btnAddContact;
    private LinearLayout layoutNoContacts;
    private EmergencyContactsAdapter adapter;
    private List<EmergencyContact> contactsList;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int SMS_PERMISSION_REQUEST = 1001;
    private boolean emergencyMessageSent = false;
    private android.os.Handler locationTimeoutHandler = new android.os.Handler();
    private Runnable locationTimeoutRunnable;
    private static final String TAG = "EmergencyContactsActivity";
    
    private interface LocationResultCallback {
        void onResult(String locationText);
    }

    private boolean isInternetAvailable() {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
                return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            }
        } catch (Exception e) {
            android.util.Log.d(TAG, "Error checking internet: " + e.getMessage());
        }
        return false;
    }

    private void fetchBestLocation(LocationResultCallback callback) {
        try {
            boolean hasFine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            boolean hasCoarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            if (!hasFine && !hasCoarse) {
                callback.onResult("Location: Permission denied");
                return;
            }

            // Check internet connectivity for location services
            if (!isInternetAvailable()) {
                android.util.Log.d(TAG, "No internet connection - location services may need internet");
                callback.onResult("Location: No internet connection");
                return;
            }

            android.util.Log.d(TAG, "Starting location fetch with internet...");

            // 1) Try getCurrentLocation (fresh, high accuracy) with shorter timeout
            fusedLocationClient
                    .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            String loc = String.format("Location: %.6f, %.6f", location.getLatitude(), location.getLongitude());
                            String copyableCoords = String.format("%.6f, %.6f", location.getLatitude(), location.getLongitude());
                            android.util.Log.d(TAG, "Current location found: " + loc);
                            callback.onResult(copyableCoords);
                        } else {
                            android.util.Log.d(TAG, "Current location is null, trying last known");
                            // 2) Fallback to last known location
                            fusedLocationClient.getLastLocation()
                                    .addOnSuccessListener(last -> {
                                        if (last != null) {
                                            String loc = String.format("Location: %.6f, %.6f", last.getLatitude(), last.getLongitude());
                                            String copyableCoords = String.format("%.6f, %.6f", last.getLatitude(), last.getLongitude());
                                            android.util.Log.d(TAG, "Last known location: " + loc);
                                            callback.onResult(copyableCoords);
                                        } else {
                                            android.util.Log.d(TAG, "No last known location, requesting fresh");
                                            // 3) Request a single update with timeout
                                            requestSingleLocationUpdateWithTimeout(callback);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        android.util.Log.d(TAG, "Last location failed: " + e.getMessage());
                                        requestSingleLocationUpdateWithTimeout(callback);
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.d(TAG, "Current location failed: " + e.getMessage());
                        // If current location fails, try last known then single update
                        fusedLocationClient.getLastLocation()
                                .addOnSuccessListener(last -> {
                                    if (last != null) {
                                        String loc = String.format("Location: %.6f, %.6f", last.getLatitude(), last.getLongitude());
                                        String copyableCoords = String.format("%.6f, %.6f", last.getLatitude(), last.getLongitude());
                                        android.util.Log.d(TAG, "Fallback last location: " + loc);
                                        callback.onResult(copyableCoords);
                                    } else {
                                        android.util.Log.d(TAG, "No fallback location, requesting fresh");
                                        requestSingleLocationUpdateWithTimeout(callback);
                                    }
                                })
                                .addOnFailureListener(err -> {
                                    android.util.Log.d(TAG, "Fallback failed: " + err.getMessage());
                                    requestSingleLocationUpdateWithTimeout(callback);
                                });
                    });

            // Add a global timeout to prevent hanging
            locationTimeoutRunnable = () -> {
                android.util.Log.d(TAG, "Global location timeout, using backup");
                tryBackupLocationMethod(callback);
            };
            locationTimeoutHandler.postDelayed(locationTimeoutRunnable, 5000);
        } catch (Exception e) {
            android.util.Log.d(TAG, "Location fetch failed: " + e.getMessage());
            callback.onResult("Location: Failed to get");
        }
    }

    private void requestSingleLocationUpdateWithTimeout(LocationResultCallback callback) {
        try {
            // If location is off, prompt user to enable
            try {
                int mode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
                if (mode == Settings.Secure.LOCATION_MODE_OFF) {
                    Toast.makeText(this, "Please enable location (High accuracy)", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            } catch (Exception ignored) {}

            android.util.Log.d(TAG, "Requesting fresh location with aggressive strategy...");

            // Create a more aggressive location request
            LocationRequest request = LocationRequest.create()
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .setInterval(100)  // Much faster interval
                    .setFastestInterval(50)  // Even faster
                    .setNumUpdates(1)
                    .setMaxWaitTime(3000);  // Max 3 seconds wait

            LocationCallback cb = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationTimeoutRunnable != null) {
                        locationTimeoutHandler.removeCallbacks(locationTimeoutRunnable);
                        locationTimeoutRunnable = null;
                    }
                    fusedLocationClient.removeLocationUpdates(this);
                    Location loc = locationResult != null ? locationResult.getLastLocation() : null;
                    if (loc != null) {
                        String text = String.format("Location: %.6f, %.6f", loc.getLatitude(), loc.getLongitude());
                        String copyableCoords = String.format("%.6f, %.6f", loc.getLatitude(), loc.getLongitude());
                        android.util.Log.d(TAG, "Fresh location obtained: " + text);
                        callback.onResult(copyableCoords);
                    } else {
                        android.util.Log.d(TAG, "Location result is null, trying backup method");
                        tryBackupLocationMethod(callback);
                    }
                }
            };

            // Shorter timeout - 3 seconds
            locationTimeoutRunnable = () -> {
                fusedLocationClient.removeLocationUpdates(cb);
                android.util.Log.d(TAG, "Location timeout, trying backup method");
                tryBackupLocationMethod(callback);
            };
            locationTimeoutHandler.postDelayed(locationTimeoutRunnable, 3000);

            fusedLocationClient.requestLocationUpdates(request, cb, Looper.getMainLooper());
        } catch (Exception e) {
            android.util.Log.d(TAG, "Location request failed: " + e.getMessage());
            tryBackupLocationMethod(callback);
        }
    }

    private void tryBackupLocationMethod(LocationResultCallback callback) {
        try {
            android.util.Log.d(TAG, "Trying backup location method...");
            
            // Try multiple location sources
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    String text = String.format("Location: %.6f, %.6f", location.getLatitude(), location.getLongitude());
                    String copyableCoords = String.format("%.6f, %.6f", location.getLatitude(), location.getLongitude());
                    android.util.Log.d(TAG, "Backup location found: " + text);
                    callback.onResult(copyableCoords);
                } else {
                    android.util.Log.d(TAG, "No backup location available, using approximate");
                    // Use a default location or approximate location
                    callback.onResult("Location: Approximate (GPS unavailable)");
                }
            }).addOnFailureListener(e -> {
                android.util.Log.d(TAG, "Backup location failed: " + e.getMessage());
                callback.onResult("Location: GPS unavailable");
            });
        } catch (Exception e) {
            android.util.Log.d(TAG, "Backup method failed: " + e.getMessage());
            callback.onResult("Location: Failed to get");
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_emergency_contacts);
            initViews();
            loadContacts();
            setupRecyclerView();
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing Emergency Contacts: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            
            // Fallback: Create a simple layout programmatically
            createFallbackLayout();
        }
    }
    
    private void createFallbackLayout() {
        try {
            // Create a simple fallback layout
            android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
            layout.setOrientation(android.widget.LinearLayout.VERTICAL);
            layout.setPadding(32, 32, 32, 32);
            layout.setBackgroundColor(getResources().getColor(android.R.color.white));
            
            android.widget.TextView title = new android.widget.TextView(this);
            title.setText("Emergency Contacts");
            title.setTextSize(24);
            title.setTextColor(getResources().getColor(android.R.color.black));
            title.setGravity(android.view.Gravity.CENTER);
            title.setPadding(0, 0, 0, 32);
            
            android.widget.Button addButton = new android.widget.Button(this);
            addButton.setText("Add Emergency Contact");
            addButton.setOnClickListener(v -> {
                Toast.makeText(this, "Please restart the app to add contacts", Toast.LENGTH_LONG).show();
            });
            
            layout.addView(title);
            layout.addView(addButton);
            
            setContentView(layout);
        } catch (Exception e) {
            Toast.makeText(this, "Critical error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    
    private void initViews() {
        try {
            recyclerViewContacts = findViewById(R.id.recycler_view_contacts);
            btnAddContact = findViewById(R.id.btn_add_contact);
            layoutNoContacts = findViewById(R.id.layout_no_contacts);
            
            btnAddContact.setOnClickListener(v -> showAddContactDialog());
            
            // Initialize location client
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            
            // Request SMS permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) 
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error in initViews: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    
    private void setupRecyclerView() {
        try {
            if (contactsList == null) {
                contactsList = new ArrayList<>();
            }
            
            if (recyclerViewContacts != null) {
                adapter = new EmergencyContactsAdapter(contactsList, this::showEditContactDialog, this::deleteContact, this::testEmergencyAlert);
                recyclerViewContacts.setLayoutManager(new LinearLayoutManager(this));
                recyclerViewContacts.setAdapter(adapter);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error setting up RecyclerView: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    
    private void loadContacts() {
        try {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            String contactsJson = prefs.getString(CONTACTS_KEY, "");
            
            if (!contactsJson.isEmpty()) {
                Gson gson = new Gson();
                Type type = new TypeToken<List<EmergencyContact>>(){}.getType();
                contactsList = gson.fromJson(contactsJson, type);
                if (contactsList == null) {
                    contactsList = new ArrayList<>();
                }
            } else {
                contactsList = new ArrayList<>();
            }
            
            updateUI();
        } catch (Exception e) {
            Toast.makeText(this, "Error loading contacts: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            contactsList = new ArrayList<>();
            updateUI();
        }
    }
    
    private void saveContacts() {
        try {
            if (contactsList == null) {
                contactsList = new ArrayList<>();
            }
            
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            
            Gson gson = new Gson();
            String contactsJson = gson.toJson(contactsList);
            
            editor.putString(CONTACTS_KEY, contactsJson);
            editor.commit();
        } catch (Exception e) {
            Toast.makeText(this, "Error saving contacts: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    
    private void updateUI() {
        try {
            if (contactsList == null) {
                contactsList = new ArrayList<>();
            }
            
            if (contactsList.isEmpty()) {
                if (layoutNoContacts != null) {
                    layoutNoContacts.setVisibility(View.VISIBLE);
                }
                if (recyclerViewContacts != null) {
                    recyclerViewContacts.setVisibility(View.GONE);
                }
            } else {
                if (layoutNoContacts != null) {
                    layoutNoContacts.setVisibility(View.GONE);
                }
                if (recyclerViewContacts != null) {
                    recyclerViewContacts.setVisibility(View.VISIBLE);
                }
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                } else {
                    setupRecyclerView();
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error updating UI: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    
    private void showAddContactDialog() {
        try {
            showContactDialog(null);
        } catch (Exception e) {
            Toast.makeText(this, "Error showing add contact dialog: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    
    private void showEditContactDialog(EmergencyContact contact) {
        try {
            showContactDialog(contact);
        } catch (Exception e) {
            Toast.makeText(this, "Error showing edit contact dialog: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    
    private void showContactDialog(EmergencyContact contact) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_emergency_contact, null);
            
            EditText etName = dialogView.findViewById(R.id.et_contact_name);
            EditText etPhone = dialogView.findViewById(R.id.et_contact_phone);
            EditText etRelationship = dialogView.findViewById(R.id.et_contact_relationship);
            
            if (etName == null || etPhone == null || etRelationship == null) {
                Toast.makeText(this, "Error: Dialog fields not found", Toast.LENGTH_SHORT).show();
                return;
            }
        
        if (contact != null) {
            etName.setText(contact.getName());
            etPhone.setText(contact.getPhone());
            etRelationship.setText(contact.getRelationship());
        }
        
        builder.setView(dialogView)
                .setTitle(contact == null ? "Add Emergency Contact" : "Edit Emergency Contact")
                .setPositiveButton("Save", (dialog, which) -> {
                    try {
                        String name = etName.getText().toString().trim();
                        String phone = etPhone.getText().toString().trim();
                        String relationship = etRelationship.getText().toString().trim();
                        
                        if (name.isEmpty() || phone.isEmpty()) {
                            Toast.makeText(this, "Name and phone are required", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        
                        // Ensure contactsList is not null
                        if (contactsList == null) {
                            contactsList = new ArrayList<>();
                        }
                        
                        if (contact == null) {
                            // Add new contact
                            EmergencyContact newContact = new EmergencyContact(name, phone, relationship);
                            contactsList.add(newContact);
                        } else {
                            // Update existing contact
                            contact.setName(name);
                            contact.setPhone(phone);
                            contact.setRelationship(relationship);
                        }
                        
                        saveContacts();
                        updateUI();
                    } catch (Exception e) {
                        Toast.makeText(this, "Error saving contact: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                })
                .setNegativeButton("Cancel", null);
        
        if (contact != null) {
            builder.setNeutralButton("Delete", (dialog, which) -> deleteContact(contact));
        }
        
        builder.show();
        } catch (Exception e) {
            Toast.makeText(this, "Error creating contact dialog: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    
    private void deleteContact(EmergencyContact contact) {
        try {
            if (contact == null) {
                Toast.makeText(this, "Contact is null, cannot delete", Toast.LENGTH_SHORT).show();
                return;
            }
            if (contactsList == null) {
                Toast.makeText(this, "Contacts list is null, cannot delete", Toast.LENGTH_SHORT).show();
                return;
            }
            
            new AlertDialog.Builder(this)
                    .setTitle("Delete Contact")
                    .setMessage("Are you sure you want to delete " + contact.getName() + "?\n\nPhone: " + contact.getPhone())
                    .setPositiveButton("DELETE", (dialog, which) -> {
                        try {
                            contactsList.remove(contact);
                            saveContacts();
                            updateUI();
                        } catch (Exception e) {
                            Toast.makeText(this, "Error deleting contact: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } catch (Exception e) {
            Toast.makeText(this, "Error showing delete dialog: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    
    private void testEmergencyAlert(EmergencyContact contact) {
        new AlertDialog.Builder(this)
                .setTitle("Test Emergency Alert")
                .setMessage("Send a test emergency message to " + contact.getName() + "?")
                .setPositiveButton("Send Test", (dialog, which) -> {
                    emergencyMessageSent = false;
                    
                    // Test location permission first
                    testLocationPermission();
                    
                    // Send basic test message first
                    sendBasicSMSTest(contact);
                    
                    // Send full emergency message after 2 seconds
                    new android.os.Handler().postDelayed(() -> {
                        try {
                            sendEmergencySMS(contact, true);
                        } catch (Exception e) {
                            sendSimpleEmergencyMessage(contact);
                        }
                        
                        // Add a timeout - if no emergency message is sent within 8 seconds, force the simple method
                        new android.os.Handler().postDelayed(() -> {
                            if (!emergencyMessageSent) {
                                sendSimpleEmergencyMessage(contact);
                            }
                        }, 8000);
                    }, 2000);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    // Quick delete method for direct deletion from contact list
    public void quickDeleteContact(EmergencyContact contact) {
        deleteContact(contact);
    }
    
    private void sendSimpleTestSMS(EmergencyContact contact) {
        try {
            String phoneNumber = formatAlgerianPhoneNumber(contact.getPhone());
            String simpleMessage = "Test from SStep App - " + System.currentTimeMillis();
            
            // Check SMS permission first
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) 
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, simpleMessage, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Test location permission and retrieval
    private void testLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                // no UI toast
            }).addOnFailureListener(e -> {
                // no UI toast
            });
        }
    }
    
    // Simple SMS test to verify SMS is working
    private void sendBasicSMSTest(EmergencyContact contact) {
        try {
            String phoneNumber = formatAlgerianPhoneNumber(contact.getPhone());
            String basicMessage = "BASIC TEST - " + System.currentTimeMillis();
            
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) 
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, basicMessage, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Simple emergency message with location
    private void sendSimpleEmergencyMessage(EmergencyContact contact) {
        try {
            String phoneNumber = formatAlgerianPhoneNumber(contact.getPhone());
            
            // Get basic user info
            SharedPreferences userPrefs = getSharedPreferences("UserInfoPrefs", MODE_PRIVATE);
            String userName = userPrefs.getString("name", "User");
            String userAge = userPrefs.getString("age", "");
            String userBloodType = userPrefs.getString("blood_type", "");
            String userWilaya = userPrefs.getString("wilaya", "");
            String userDaira = userPrefs.getString("daira", "");
            
            // Try to get location with timeout
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                    == PackageManager.PERMISSION_GRANTED) {
                
                fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                    String locationText = "Location: Unknown";
                    if (location != null) {
                        locationText = String.format("Location: %.6f, %.6f", 
                            location.getLatitude(), location.getLongitude());
                    } else {
                        locationText = "Location: Not available";
                    }
                    
                    sendEmergencyMessageWithLocation(contact, userName, userAge, userBloodType, userWilaya, userDaira, locationText);
                }).addOnFailureListener(e -> {
                    sendEmergencyMessageWithLocation(contact, userName, userAge, userBloodType, userWilaya, userDaira, "Location: Failed to get");
                });
                
                // Add timeout - if location doesn't come within 3 seconds, send without it
                new android.os.Handler().postDelayed(() -> {
                    sendEmergencyMessageWithLocation(contact, userName, userAge, userBloodType, userWilaya, userDaira, "Location: Timeout");
                }, 3000);
                
            } else {
                sendEmergencyMessageWithLocation(contact, userName, userAge, userBloodType, userWilaya, userDaira, "Location: Permission denied");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void sendEmergencyMessageWithLocation(EmergencyContact contact, String userName, String userAge, 
                                                String userBloodType, String userWilaya, String userDaira, 
                                                String locationText) {
        try {
            // Prevent duplicate messages
            if (emergencyMessageSent) {
                return;
            }
            
            String phoneNumber = formatAlgerianPhoneNumber(contact.getPhone());
            
            String message = "🚨 TEST ALERT - SStep App\n\n" +
                           "This is a test emergency alert from " + userName + ".\n\n" +
                           "Personal Info:\n" +
                           "• Name: " + userName + "\n" +
                           "• Age: " + userAge + "\n" +
                           "• Blood Type: " + userBloodType + "\n" +
                           "• Location: " + userWilaya + ", " + userDaira + "\n" +
                           "• GPS Coordinates: " + locationText + "\n\n" +
                           "📍 COPY COORDINATES:\n" + locationText + "\n\n" +
                           "This is just a test. No actual emergency occurred.";
            
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) 
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            
            emergencyMessageSent = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void sendEmergencySMS(EmergencyContact contact, boolean isTest) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) 
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        
        // Get user info for the message
        SharedPreferences userPrefs = getSharedPreferences("UserInfoPrefs", MODE_PRIVATE);
        String userName = userPrefs.getString("name", "User");
        String userAge = userPrefs.getString("age", "");
        String userBloodType = userPrefs.getString("blood_type", "");
        String userWilaya = userPrefs.getString("wilaya", "");
        String userDaira = userPrefs.getString("daira", "");
        
        // Get current location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED) {

            // Start a timeout to avoid hanging waiting for GPS
            locationTimeoutRunnable = () -> {
                // Fallback to last known location, then send
                fusedLocationClient.getLastLocation().addOnSuccessListener(fallbackLoc -> {
                    String locationText = "Location: Timeout";
                    if (fallbackLoc != null) {
                        locationText = String.format("Location: %.6f, %.6f", 
                                fallbackLoc.getLatitude(), fallbackLoc.getLongitude());
                    }
                    sendSMSMessage(contact, userName, userAge, userBloodType, userWilaya, userDaira, locationText, isTest);
                }).addOnFailureListener(fallbackErr -> {
                    sendSMSMessage(contact, userName, userAge, userBloodType, userWilaya, userDaira, "Location: Timeout", isTest);
                });
            };
            locationTimeoutHandler.postDelayed(locationTimeoutRunnable, 5000);

            // Prefer fresh high-accuracy location if available
            fetchBestLocation(loc -> {
                if (locationTimeoutRunnable != null) {
                    locationTimeoutHandler.removeCallbacks(locationTimeoutRunnable);
                    locationTimeoutRunnable = null;
                }
                sendSMSMessage(contact, userName, userAge, userBloodType, userWilaya, userDaira, loc, isTest);
            });
        } else {
            sendSMSMessage(contact, userName, userAge, userBloodType, userWilaya, userDaira, "Location: Permission denied", isTest);
        }
    }
    
    private void sendSMSMessage(EmergencyContact contact, String userName, String userAge, 
                               String userBloodType, String userWilaya, String userDaira, 
                               String location, boolean isTest) {
        String message;
        if (isTest) {
            message = "🚨 TEST ALERT - SStep App\n\n" +
                     "This is a test emergency alert from " + userName + ".\n\n" +
                     "Personal Info:\n" +
                     "• Name: " + userName + "\n" +
                     "• Age: " + userAge + "\n" +
                     "• Blood Type: " + userBloodType + "\n" +
                     "• Location: " + userWilaya + ", " + userDaira + "\n" +
                     "• GPS Coordinates: " + location + "\n\n" +
                     "📍 COPY COORDINATES:\n" + location + "\n\n" +
                     "This is just a test. No actual emergency occurred.";
        } else {
            message = "🚨 EMERGENCY ALERT - SStep App\n\n" +
                     "URGENT: " + userName + " has fallen and needs immediate help!\n\n" +
                     "Personal Info:\n" +
                     "• Name: " + userName + "\n" +
                     "• Age: " + userAge + "\n" +
                     "• Blood Type: " + userBloodType + "\n" +
                     "• Location: " + userWilaya + ", " + userDaira + "\n" +
                     "• GPS Coordinates: " + location + "\n\n" +
                     "📍 COPY COORDINATES:\n" + location + "\n\n" +
                     "Please respond immediately and contact emergency services if needed.";
        }
        
        // Format phone number for Algeria (+213)
        String phoneNumber = formatAlgerianPhoneNumber(contact.getPhone());
        
        try {
            // Check SMS permission again
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) 
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            
            SmsManager smsManager = SmsManager.getDefault();
            
            // Use multipart for long messages to avoid truncation
            try {
                ArrayList<String> parts = smsManager.divideMessage(message);
                if (parts != null && parts.size() > 1) {
                    smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null);
                } else {
                    smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                }
            } catch (Exception smsException) {
                throw smsException;
            }
        } catch (Exception e) {
            e.printStackTrace();
            
            // Try alternative SMS sending method
            tryAlternativeSMS(phoneNumber, message, contact.getName());
        }
    }
    
    private String formatAlgerianPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return phoneNumber;
        }
        
        // Remove all spaces, dashes, and parentheses
        String cleaned = phoneNumber.replaceAll("[\\s\\-\\(\\)]", "");
        
        // If it starts with 0, replace with +213
        if (cleaned.startsWith("0")) {
            String formatted = "+213" + cleaned.substring(1);
            return formatted;
        }
        
        // If it starts with 213, add +
        if (cleaned.startsWith("213")) {
            String formatted = "+" + cleaned;
            return formatted;
        }
        
        // If it already has +213, return as is
        if (cleaned.startsWith("+213")) {
            return cleaned;
        }
        
        // If it's a 9-digit number, assume it's Algerian and add +213
        if (cleaned.length() == 9 && cleaned.matches("\\d{9}")) {
            String formatted = "+213" + cleaned;
            return formatted;
        }
        
        // Return as is if we can't determine the format
        return phoneNumber;
    }
    
    private void tryAlternativeSMS(String phoneNumber, String message, String contactName) {
        try {
            // Try using Intent to send SMS
            Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
            smsIntent.setData(Uri.parse("smsto:" + phoneNumber));
            smsIntent.putExtra("sms_body", message);
            
            if (smsIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(smsIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reload contacts when returning to this activity
        loadContacts();
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_REQUEST) {
            // no debug toast
        }
    }
    
}
