package com.example.s23010340.authentication.labour;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LabourDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "labour_auth.db";
    private static final int DATABASE_VERSION = 5;

    private static final String TABLE_USERS = "labour_users";
    private static final String TABLE_PROFILES = "labour_profiles";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_USER_EMAIL = "user_email";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_PHONE = "phone";
    private static final String COLUMN_LOCATION = "location";
    private static final String COLUMN_DISTRICT = "district";
    private static final String COLUMN_CITY = "city";
    private static final String COLUMN_PROFILE_PHOTO_URI = "profile_photo_uri";
    private static final String COLUMN_IS_AVAILABLE = "is_available";
    private static final String COLUMN_IS_HIRED = "is_hired";

    public LabourDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " ("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_EMAIL + " TEXT UNIQUE, "
            + COLUMN_PASSWORD + " TEXT"
            + ")";
        db.execSQL(createUsersTable);

        String createProfilesTable = "CREATE TABLE " + TABLE_PROFILES + " ("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_USER_EMAIL + " TEXT UNIQUE, "
            + COLUMN_NAME + " TEXT NOT NULL, "
            + COLUMN_CATEGORY + " TEXT NOT NULL, "
            + COLUMN_PHONE + " TEXT NOT NULL, "
            + COLUMN_LOCATION + " TEXT NOT NULL, "
            + COLUMN_DISTRICT + " TEXT NOT NULL, "
            + COLUMN_CITY + " TEXT NOT NULL, "
            + COLUMN_PROFILE_PHOTO_URI + " TEXT, "
            + COLUMN_IS_AVAILABLE + " INTEGER NOT NULL DEFAULT 1, "
            + COLUMN_IS_HIRED + " INTEGER NOT NULL DEFAULT 0"
            + ")";
        db.execSQL(createProfilesTable);

        seedDummyLabours(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            String createProfilesTable = "CREATE TABLE IF NOT EXISTS " + TABLE_PROFILES + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USER_EMAIL + " TEXT UNIQUE, "
                + COLUMN_NAME + " TEXT NOT NULL, "
                + COLUMN_CATEGORY + " TEXT NOT NULL, "
                + COLUMN_PHONE + " TEXT NOT NULL, "
                + COLUMN_LOCATION + " TEXT NOT NULL, "
                + COLUMN_DISTRICT + " TEXT NOT NULL, "
                + COLUMN_CITY + " TEXT NOT NULL, "
                + COLUMN_PROFILE_PHOTO_URI + " TEXT, "
                + COLUMN_IS_AVAILABLE + " INTEGER NOT NULL DEFAULT 1, "
                + COLUMN_IS_HIRED + " INTEGER NOT NULL DEFAULT 0"
                + ")";
            db.execSQL(createProfilesTable);
            seedDummyLabours(db);
        }
        if (oldVersion < 3) {
            if (!hasColumn(db, TABLE_PROFILES, COLUMN_IS_HIRED)) {
                db.execSQL(
                    "ALTER TABLE " + TABLE_PROFILES
                        + " ADD COLUMN " + COLUMN_IS_HIRED + " INTEGER NOT NULL DEFAULT 0"
                );
            }
        }
        if (oldVersion < 4) {
            if (!hasColumn(db, TABLE_PROFILES, COLUMN_USER_EMAIL)) {
                db.execSQL(
                    "ALTER TABLE " + TABLE_PROFILES
                        + " ADD COLUMN " + COLUMN_USER_EMAIL + " TEXT"
                );
            }
            if (!hasColumn(db, TABLE_PROFILES, COLUMN_LOCATION)) {
                db.execSQL(
                    "ALTER TABLE " + TABLE_PROFILES
                        + " ADD COLUMN " + COLUMN_LOCATION + " TEXT NOT NULL DEFAULT ''"
                );
            }
        }
        if (oldVersion < 5) {
            if (!hasColumn(db, TABLE_PROFILES, COLUMN_PROFILE_PHOTO_URI)) {
                db.execSQL(
                    "ALTER TABLE " + TABLE_PROFILES
                        + " ADD COLUMN " + COLUMN_PROFILE_PHOTO_URI + " TEXT"
                );
            }
        }
    }

    public boolean insertUser(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public boolean isValidUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_EMAIL + "=? AND " + COLUMN_PASSWORD + "=?";
        String[] selectionArgs = {email, password};
        Cursor cursor = db.query(TABLE_USERS, null, selection, selectionArgs, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public List<LabourProfile> getLaboursByCategory(String category, String searchText) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<LabourProfile> labours = new ArrayList<>();

        String normalizedSearch = searchText == null ? "" : searchText.trim().toLowerCase(Locale.ROOT);
        String selection = COLUMN_CATEGORY + " = ?";
        List<String> args = new ArrayList<>();
        args.add(category);

        if (!normalizedSearch.isEmpty()) {
            selection += " AND (LOWER(" + COLUMN_DISTRICT + ") LIKE ? OR LOWER(" + COLUMN_CITY + ") LIKE ?)";
            String likeTerm = "%" + normalizedSearch + "%";
            args.add(likeTerm);
            args.add(likeTerm);
        }

        Cursor cursor = db.query(
            TABLE_PROFILES,
            null,
            selection,
            args.toArray(new String[0]),
            null,
            null,
            COLUMN_IS_AVAILABLE + " DESC, " + COLUMN_NAME + " ASC"
        );

        while (cursor.moveToNext()) {
            labours.add(new LabourProfile(
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DISTRICT)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CITY)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROFILE_PHOTO_URI)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_AVAILABLE)) == 1,
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_HIRED)) == 1
            ));
        }
        cursor.close();
        return labours;
    }

    public boolean markLabourAsHired(int labourId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_HIRED, 1);
        int rows = db.update(TABLE_PROFILES, values, COLUMN_ID + "=?", new String[] {String.valueOf(labourId)});
        return rows > 0;
    }

    public LabourProfile getLabourProfileByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
            TABLE_PROFILES,
            null,
            COLUMN_USER_EMAIL + "=?",
            new String[] {email},
            null,
            null,
            null
        );

        LabourProfile profile = null;
        if (cursor.moveToFirst()) {
            profile = new LabourProfile(
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DISTRICT)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CITY)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROFILE_PHOTO_URI)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_AVAILABLE)) == 1,
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_HIRED)) == 1
            );
        }
        cursor.close();
        return profile;
    }

    public boolean upsertLabourAvailabilityProfile(
        String email,
        String name,
        String category,
        String phone,
        String location,
        String district,
        String city,
        String photoUri,
        boolean isAvailable
    ) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_EMAIL, email);
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_PHONE, phone);
        values.put(COLUMN_LOCATION, location);
        values.put(COLUMN_DISTRICT, district);
        values.put(COLUMN_CITY, city);
        values.put(COLUMN_PROFILE_PHOTO_URI, photoUri);
        values.put(COLUMN_IS_AVAILABLE, isAvailable ? 1 : 0);
        values.put(COLUMN_IS_HIRED, 0);

        int updatedRows = db.update(TABLE_PROFILES, values, COLUMN_USER_EMAIL + "=?", new String[] {email});
        if (updatedRows > 0) {
            return true;
        }
        return db.insert(TABLE_PROFILES, null, values) != -1;
    }

    private void seedDummyLabours(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PROFILES, null);
        int total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0);
        }
        cursor.close();

        if (total > 0) {
            return;
        }

        insertDummyLabour(db, "Saman Kumara", "Electrician", "0771234567", "Kandy", "Polgolla", true);
        insertDummyLabour(db, "Chamara Ruwan", "Electrician", "0772345678", "Polonnaruwa", "Diyasenpura", false);
        insertDummyLabour(db, "Nuwan Perera", "Electrician", "0773456789", "Polonnaruwa", "New Town", false);

        insertDummyLabour(db, "Saman Kumara", "Plumber", "0781234567", "Kandy", "Polgolla", false);
        insertDummyLabour(db, "Chamara Ruwan", "Plumber", "0782345678", "Polonnaruwa", "Diyasenpura", true);
        insertDummyLabour(db, "Tharindu Silva", "Plumber", "0783456789", "Polonnaruwa", "New Town", true);

        insertDummyLabour(db, "Kasun Madusanka", "Carpenter", "0711234567", "Colombo", "Maharagama", true);
        insertDummyLabour(db, "Ravindu Lakshan", "Welder", "0712345678", "Gampaha", "Kiribathgoda", true);
        insertDummyLabour(db, "Nimal Jayasinghe", "Mason", "0713456789", "Kandy", "Katugastota", true);
        insertDummyLabour(db, "Pradeep Fernando", "Mechanic", "0721234567", "Colombo", "Nugegoda", true);
        insertDummyLabour(db, "Roshan Wijekoon", "Painter", "0722345678", "Kurunegala", "Kuliyapitiya", true);
        insertDummyLabour(db, "Sudath Bandara", "Roofer", "0723456789", "Matale", "Dambulla", true);
        insertDummyLabour(db, "Amila Jayasuriya", "Gardening", "0751234567", "Galle", "Hikkaduwa", true);
    }

    private void insertDummyLabour(
        SQLiteDatabase db,
        String name,
        String category,
        String phone,
        String district,
        String city,
        boolean isAvailable
    ) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_EMAIL, (String) null);
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_PHONE, phone);
        values.put(COLUMN_LOCATION, city + ", " + district);
        values.put(COLUMN_DISTRICT, district);
        values.put(COLUMN_CITY, city);
        values.put(COLUMN_PROFILE_PHOTO_URI, (String) null);
        values.put(COLUMN_IS_AVAILABLE, isAvailable ? 1 : 0);
        values.put(COLUMN_IS_HIRED, 0);
        db.insert(TABLE_PROFILES, null, values);
    }

    private boolean hasColumn(SQLiteDatabase db, String tableName, String columnName) {
        Cursor cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
        boolean found = false;
        while (cursor.moveToNext()) {
            String existingColumn = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            if (columnName.equals(existingColumn)) {
                found = true;
                break;
            }
        }
        cursor.close();
        return found;
    }
}
