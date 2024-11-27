package com.example.lifequest

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SQLiteAyudante(contexto: Context, name: String, factory: SQLiteDatabase.CursorFactory, version: Int) : SQLiteOpenHelper(contexto, name, factory, version) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE usuarios (nombre TEXT PRIMARY KEY, correo TEXT, contrasena TEXT)")
        db?.execSQL("CREATE TABLE IF NOT EXISTS preferencias (usuario TEXT PRIMARY KEY, is_night_mode INTEGER)")
        db?.execSQL("CREATE TABLE comidas (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT, calorias INTEGER, proteina INTEGER, carbohidratos INTEGER, grasas INTEGER)")
        db?.execSQL("CREATE TABLE ejercicios (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT, calorias INTEGER, tiempo INTEGER)")
        db?.execSQL("CREATE TABLE dias (id INTEGER PRIMARY KEY AUTOINCREMENT, fecha TEXT, calorias INTEGER, proteina INTEGER, carbohidratos INTEGER, grasas INTEGER)")
        db?.execSQL("CREATE TABLE comidas_dia (id INTEGER PRIMARY KEY AUTOINCREMENT, id_comida INTEGER, id_dia INTEGER)")
        db?.execSQL("CREATE TABLE ejercicios_dia (id INTEGER PRIMARY KEY AUTOINCREMENT, id_ejercicio INTEGER, id_dia INTEGER)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }
}