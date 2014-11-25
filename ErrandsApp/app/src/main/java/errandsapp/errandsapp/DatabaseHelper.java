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
    //Global Variables for the database
    public	static	final	String	DATABASE_NAME	=	"Routes.db";
    public 	static	final	int	DATABASE_VERSION	=	1;
    public	static	final	String	RL_TABLE_NAME	=	"RecentLocations" ;
    public	static	final	String	FAV_TABLE_NAME	=	"FavoriteLocations" ;
    private Context context;
    private SQLiteDatabase db;
    private SQLiteStatement rl_InsertStmt;
    private SQLiteStatement fav_InsertStmt;
    private	static	final	String	RL_INSERT	=	"insert	into	"	+
            RL_TABLE_NAME	+	"(name,	longitude, latitude, address)	values	(?,	?, ?, ?)"	;
    private	static	final	String	FAV_INSERT	=	"insert	into	"	+
            FAV_TABLE_NAME	+	"(name,	longitude, latitude, address)	values	(?,	?, ?, ?)"	;

    public	DatabaseHelper(Context	context)	{
        this.context	=	context;
        DatabaseOpenHelper openHelper	=
                new DatabaseOpenHelper(this.context);	//	this	creates	the	DB
        this.db	=	openHelper.getWritableDatabase();
        this.rl_InsertStmt	=	this.db.compileStatement(RL_INSERT);
        this.fav_InsertStmt	=	this.db.compileStatement(FAV_INSERT);
    }

    /*
    This method takes the parameters of a destination and inserts them into the recents db table
     */
    public	long rlInsert(String	name, double longitude, double latitude, String address)	{
        this.rl_InsertStmt.bindString(1,	name);
        this.rl_InsertStmt.bindDouble(2, longitude);
        this.rl_InsertStmt.bindDouble(3, latitude);
        this.rl_InsertStmt.bindString(4,	address);
        return	this.rl_InsertStmt.executeInsert();
    }

    /*
    This method takes the parameters of a destination and inserts them into the favorites db table
     */
    public	long favInsert(String	name, double longitude, double latitude, String address)	{
        this.fav_InsertStmt.bindString(1,	name);
        this.fav_InsertStmt.bindDouble(2, longitude);
        this.fav_InsertStmt.bindDouble(3, latitude);
        this.fav_InsertStmt.bindString(4,	address);
        return	this.fav_InsertStmt.executeInsert();
    }

    /*
    These methods delete the contents of each of their respected tables
     */
    public	void	rlDeleteAll()	{
        this.db.delete(RL_TABLE_NAME,	null,	null);
    }
    public	void	favDeleteAll()	{
        this.db.delete(FAV_TABLE_NAME,	null,	null);
    }

    /*
    This methods returns all the  recent destinations from the database as an arraylist of destinations
     */
    public	ArrayList<Destination>	rlSelectAll()	{
        ArrayList<Destination> list	=	new ArrayList<Destination>();
        Cursor cursor	=
                this.db.query(RL_TABLE_NAME,
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

    /*
    This methods returns all the favorite destinations from the database as an arraylist of destinations
     */
    public	ArrayList<Destination>	favSelectAll()	{
        ArrayList<Destination> list	=	new ArrayList<Destination>();
        Cursor cursor	=
                this.db.query(FAV_TABLE_NAME,
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

    /*
    Helper class that is used to create and upgrade the tables of the database
     */
    private static class	DatabaseOpenHelper	extends SQLiteOpenHelper {
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "Routes.db";

        DatabaseOpenHelper(Context context)	{
            super(context,	DATABASE_NAME,	null,	DATABASE_VERSION);
        }
        @Override
        public	void	onCreate(SQLiteDatabase db)	{
            db.execSQL("CREATE	TABLE	"	+	RL_TABLE_NAME	+
                    "(id	INTEGER	PRIMARY	KEY,	name	TEXT,	longitude REAL, latitude, REAL, address	TEXT)");
            db.execSQL("CREATE	TABLE	"	+	FAV_TABLE_NAME	+
                    "(id	INTEGER	PRIMARY	KEY,	name	TEXT,	longitude REAL, latitude, REAL, address	TEXT)");
        }
        @Override
        public	void	onUpgrade(SQLiteDatabase db,	int oldVersion,	int newVersion)	{
            Log.w("Example", "Upgrading	database;	this	drops	&	recreates	tables.");
            db.execSQL("DROP	TABLE	IF	EXISTS	"	+	RL_TABLE_NAME);
            db.execSQL("DROP	TABLE	IF	EXISTS	"	+	FAV_TABLE_NAME);
            onCreate(db);
        }


    }
}
