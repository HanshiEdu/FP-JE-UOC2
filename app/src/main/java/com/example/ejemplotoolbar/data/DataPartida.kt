package com.example.ejemplotoolbar.data

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import android.provider.BaseColumns
import androidx.annotation.RequiresApi
import com.example.ejemplotoolbar.Jugador
import java.time.LocalDateTime
import java.time.ZoneOffset
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

object PartidaReaderEntry{
    object PartidaEntry{
        const val TABLE_NAME = "partida"
        const val COLUMN_NAME_NOMBRE = "nombre"
        const val COLUMN_NAME_MONEDAS = "monedas"
        const val COLUMN_NAME_RACHAS = "rachas"
        const val COLUMN_NAME_FECHA = "fecha"
        const val COLUMN_NAME_LATITUD = "latitud"
        const val COLUMN_NAME_LONGITUD = "longitud"
    }
}

private const val SQL_CREATE_PARTIDAS =
    "CREATE TABLE partida(" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
            "${PartidaReaderEntry.PartidaEntry.COLUMN_NAME_NOMBRE} TEXT," +
            "${PartidaReaderEntry.PartidaEntry.COLUMN_NAME_MONEDAS} INTEGER," +
            "${PartidaReaderEntry.PartidaEntry.COLUMN_NAME_RACHAS} INTEGER," +
            "${PartidaReaderEntry.PartidaEntry.COLUMN_NAME_LATITUD} DOUBLE," +
            "${PartidaReaderEntry.PartidaEntry.COLUMN_NAME_LONGITUD} DOUBLE," +
            "${PartidaReaderEntry.PartidaEntry.COLUMN_NAME_FECHA} DATETIME DEFAULT CURRENT_TIMESTAMP)"
private const val SQL_DELETE_PARTIDAS = "DROP TABLE IF EXISTS ${PartidaReaderEntry.PartidaEntry.TABLE_NAME}"

private lateinit var dbHelper: DataPartida

class DataPartida (context: Context): SQLiteOpenHelper(context, DATABASE_NAME ,null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_PARTIDAS)
    }
    override fun onUpgrade(db: SQLiteDatabase,oldVersion: Int,newVersion: Int){
        db.execSQL(SQL_DELETE_PARTIDAS)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }
    companion object{
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "lamorra.db"
    }
}

public fun guardarPartida(
    db: SQLiteDatabase,
    nombre: String, monedas: Int, rachas: Int, latitud: Double, longitud: Double
): Single<Long> {
    return Single.fromCallable {
        val values = ContentValues().apply{
            put(PartidaReaderEntry.PartidaEntry.COLUMN_NAME_NOMBRE, nombre)
            put(PartidaReaderEntry.PartidaEntry.COLUMN_NAME_MONEDAS, monedas)
            put(PartidaReaderEntry.PartidaEntry.COLUMN_NAME_RACHAS, rachas)
            put(PartidaReaderEntry.PartidaEntry.COLUMN_NAME_LATITUD, latitud)
            put(PartidaReaderEntry.PartidaEntry.COLUMN_NAME_LONGITUD, longitud)
        }
        db.insert(PartidaReaderEntry.PartidaEntry.TABLE_NAME, null, values)
    }
}

@SuppressLint("Range")
@RequiresApi(Build.VERSION_CODES.O)
public fun UltimaFila (db: SQLiteDatabase): Jugador {
    val cursor = db.rawQuery("SELECT nombre, monedas, rachas, fecha, latitud, longitud FROM partida ORDER BY fecha DESC LIMIT 1", null)
    return if (cursor.moveToFirst()) {
        val title = cursor.getString(cursor.getColumnIndex("nombre"))
        val monedas = cursor.getInt(cursor.getColumnIndex("monedas"))
        val rachas = cursor.getInt(cursor.getColumnIndex("rachas"))
        val latitud = cursor.getDouble(cursor.getColumnIndex("latitud"))
        val longitud = cursor.getDouble(cursor.getColumnIndex("longitud"))
        val fechalong = cursor.getLong(cursor.getColumnIndex("fecha"))
        val fecha = LocalDateTime.ofEpochSecond(fechalong, 0, ZoneOffset.UTC)

        cursor.close()
        Jugador(title, monedas, rachas, fecha, latitud, longitud)
    } else {
        cursor.close()
        Jugador(
            "manolo", 20, 0, null, 0.0, 0.0
        )
    }

}

