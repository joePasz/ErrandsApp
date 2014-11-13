package errandsapp.errandsapp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Joe on 11/13/2014.
 */
public class DatabaseHelper {
    public	static	final	String	DATABASE_NAME	=	"Routes.db";
    public 	static	final	int	DATABASE_VERSION	=	1;
    public	static	final	String	TABLE_NAME	=	"RecentLocations" ;
    private Context context;
    private SQLiteDatabase db;
    private SQLiteStatement insertStmt;
    private	static	final	String	INSERT	=	"insert	into	"	+
            TABLE_NAME	+	"(name,	longitude, latitude, address)	values	(?,	?, ?, ?)"	;

    public	DatabaseHelper(Context	context)	{
        this.context	=	context;
        DatabaseOpenHelper openHelper	=
                new DatabaseOpenHelper(this.context);	//	this	creates	the	DB
        this.db	=	openHelper.getWritableDatabase();
        this.insertStmt	=	this.db.compileStatement(INSERT);
    }

    public	long	insert(String	name, double longitude, double latitude, String address)	{
        this.insertStmt.bindString(1,	name);
        this.insertStmt.bindDouble(2, longitude);
        this.insertStmt.bindDouble(3, latitude);
        this.insertStmt.bindString(4,	address);
        return	this.insertStmt.executeInsert();
    }
    public	void	deleteAll()	{
        this.db.delete(TABLE_NAME,	null,	null);
    }

    public	ArrayList<Destination>	selectAll()	{
        ArrayList<Destination> list	=	new ArrayList<Destination>();
        Cursor cursor	=
                this.db.query(TABLE_NAME,
                        new	String[]	{	"name",	"longitude","latitude","address"	},
                        null,
                        null,	null,	null,	null);
        if	(cursor.moveToFirst())	do	{
            String tempName = cursor.getString(0);
            Double tempLong = cursor.getDouble(1);
            Double tempLat = cursor.getDouble(2);
            String tempAddress = cursor.getString(3);
            Destination tempDest = new Destination(tempName, tempLong, tempLat);
            tempDest.address = tempAddress;
            list.add(tempDest);
        }	while	(cursor.moveToNext());
        if	(cursor	!=	null	&&	!cursor.isClosed())	{
            cursor.close();
        }
        return	list;
    }


    private static class	DatabaseOpenHelper	extends SQLiteOpenHelper {
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "Routes.db";

        DatabaseOpenHelper(Context context)	{
            super(context,	DATABASE_NAME,	null,	DATABASE_VERSION);
        }
        @Override
        public	void	onCreate(SQLiteDatabase db)	{
            db.execSQL("CREATE	TABLE	"	+	TABLE_NAME	+
                    "(id	INTEGER	PRIMARY	KEY,	name	TEXT,	longitude REAL, latitude, REAL, address	TEXT)");
        }
        @Override
        public	void	onUpgrade(SQLiteDatabase db,	int oldVersion,	int newVersion)	{
            Log.w("Example", "Upgrading	database;	this	drops	&	recreates	tables.");
            db.execSQL("DROP	TABLE	IF	EXISTS	"	+	TABLE_NAME);
            onCreate(db);
        }


    }
}
