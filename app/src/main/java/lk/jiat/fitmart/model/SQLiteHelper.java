package lk.jiat.fitmart.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class SQLiteHelper extends SQLiteOpenHelper {

    public SQLiteHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS gender (" +
                "gender_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "gender TEXT NOT NULL UNIQUE);");

        db.execSQL("INSERT OR IGNORE INTO gender (gender) VALUES ('Male'), ('Female')");


        db.execSQL("CREATE TABLE IF NOT EXISTS user (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "fname TEXT NOT NULL, " +
                "lname TEXT NOT NULL, " +
                "email TEXT NOT NULL, " +
                "mobile TEXT NOT NULL, " +
                "password TEXT NOT NULL, " +
                "registeredDate DATETIME NOT NULL, " +
                "gender_id INTEGER NOT NULL, " +
                "FOREIGN KEY (gender_id) REFERENCES gender(gender_id) ON DELETE CASCADE);");

        db.execSQL("CREATE TABLE IF NOT EXISTS trainer (" +
                "trainer_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "fname TEXT NOT NULL, " +
                "lname TEXT NOT NULL, " +
                "mobile TEXT NOT NULL, " +
                "experience TEXT NOT NULL, " +
                "speciality TEXT NOT NULL, " +
                "gender_id INTEGER NOT NULL, " +
                "FOREIGN KEY (gender_id) REFERENCES gender(gender_id) ON DELETE CASCADE);");

        db.execSQL("INSERT OR IGNORE INTO trainer (speciality) VALUES ('Strength'), ('Cardio'), ('Weight Loss'),('Muscle Gain'), ('Nutrition'),('CrossFit')");


        db.execSQL("CREATE TABLE IF NOT EXISTS trainerImage (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "trainer_image TEXT NOT NULL, " +
                "trainer_id INTEGER NOT NULL, " +
                "FOREIGN KEY (trainer_id) REFERENCES trainer(trainer_id));");

        db.execSQL("CREATE TABLE IF NOT EXISTS user_image (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_image TEXT NOT NULL, " +
                "user_id INTEGER NOT NULL, " +
                "FOREIGN KEY (user_id) REFERENCES user(user_id));");

        db.execSQL("CREATE TABLE IF NOT EXISTS address (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "line1 TEXT NOT NULL, " +
                "line2 TEXT NOT NULL, " +
                "postalcode TEXT NOT NULL, " +
                "city TEXT NOT NULL, " +
                "user_id INTEGER NOT NULL, " +
                "FOREIGN KEY (user_id) REFERENCES user(user_id));");

        db.execSQL("CREATE TABLE IF NOT EXISTS category (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "category TEXT NOT NULL);");

        db.execSQL("INSERT OR IGNORE INTO category (category) VALUES ('Proteins'), ('Pre Workouts'), ('Creatine'),('Protein bar'), ('Fat burners'),('Vitamins')");


        db.execSQL("CREATE TABLE IF NOT EXISTS status (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "status TEXT NOT NULL);");

        db.execSQL("CREATE TABLE IF NOT EXISTS product (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT NOT NULL, " +
                "description TEXT NOT NULL, " +
                "price REAL NOT NULL, " +
                "category_id INTEGER NOT NULL, " +
                "addedDate DATETIME NOT NULL, " +
                "status_id INTEGER NOT NULL, " +
                "deliveryfee REAL NOT NULL, " +
                "qty INTEGER NOT NULL, " +
                "FOREIGN KEY (category_id) REFERENCES category(id), " +
                "FOREIGN KEY (status_id) REFERENCES status(id));");

        db.execSQL("CREATE TABLE IF NOT EXISTS productImages (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "product_image TEXT NOT NULL, " +
                "product_id INTEGER NOT NULL, " +
                "FOREIGN KEY (product_id) REFERENCES product(id));");

        db.execSQL("CREATE TABLE IF NOT EXISTS type (" +
                "id INTEGER PRIMARY KEY, " +
                "type TEXT NOT NULL, " +
                "price TEXT);");

        db.execSQL("CREATE TABLE IF NOT EXISTS appointment (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "trainer_id INTEGER NOT NULL, " +
                "datetime DATETIME NOT NULL, " +
                "appointmentTime DATETIME, " +
                "type_id INTEGER NOT NULL, " +
                "FOREIGN KEY (user_id) REFERENCES user(user_id), " +
                "FOREIGN KEY (trainer_id) REFERENCES trainer(trainer_id), " +
                "FOREIGN KEY (type_id) REFERENCES type(id));");

        db.execSQL("CREATE TABLE IF NOT EXISTS orderStatus (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "orderStatus TEXT NOT NULL);");

        db.execSQL("CREATE TABLE IF NOT EXISTS orders (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "product_id INTEGER NOT NULL, " +
                "user_id INTEGER NOT NULL, " +
                "total_qty INTEGER NOT NULL, " +
                "totalprice REAL NOT NULL, " +
                "orderStatus_id INTEGER NOT NULL, " +
                "datetime DATETIME NOT NULL, " +
                "FOREIGN KEY (product_id) REFERENCES product(id), " +
                "FOREIGN KEY (user_id) REFERENCES user(user_id), " +
                "FOREIGN KEY (orderStatus_id) REFERENCES orderStatus(id));");

        db.execSQL("CREATE TABLE IF NOT EXISTS invoice (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "datetime DATETIME NOT NULL, " +
                "order_id INTEGER NOT NULL, " +
                "FOREIGN KEY (order_id) REFERENCES orders(id));");

        db.execSQL("CREATE TABLE IF NOT EXISTS payment (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "invoice_id INTEGER NOT NULL, " +
                "amount REAL NOT NULL, " +
                "datetime DATETIME NOT NULL, " +
                "FOREIGN KEY (invoice_id) REFERENCES invoice(id));");

        db.execSQL("CREATE TABLE IF NOT EXISTS cart (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_user_id INTEGER NOT NULL, " +
                "product_id INTEGER NOT NULL, " +
                "qty INTEGER NOT NULL, " +
                "price REAL NOT NULL, " +
                "FOREIGN KEY (user_user_id) REFERENCES user(user_id), " +
                "FOREIGN KEY (product_id) REFERENCES product(id));");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
