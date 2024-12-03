package com.example.lifequest

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SQLiteAyudante(
    contexto: Context,
    name: String,
    factory: SQLiteDatabase.CursorFactory?,
    version: Int
) : SQLiteOpenHelper(contexto, name, factory, version) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE usuarios (usuario TEXT PRIMARY KEY, correo TEXT, contrasena TEXT, monedas INTEGER, tareas_completadas INTEGER, monedas_gastadas INTEGER, modoOscuro INTEGER)")
        db?.execSQL("CREATE TABLE sesionActual (usuario TEXT PRIMARY KEY, modoOscuro INTEGER)")
        db?.execSQL("CREATE TABLE Tareas (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT, descripcion TEXT, fecha TEXT, usuario TEXT, vecesCompletada INTEGER, monedas INTEGER)")
        db?.execSQL("CREATE TABLE premios (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT, descripcion TEXT, costo INTEGER, usuario TEXT)")

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }
}