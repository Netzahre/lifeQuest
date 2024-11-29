package com.example.lifequest

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SQLiteAyudante(contexto: Context, name: String, factory: SQLiteDatabase.CursorFactory, version: Int) : SQLiteOpenHelper(contexto, name, factory, version) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE usuarios (nombre TEXT PRIMARY KEY, correo TEXT, contrasena TEXT, monedas INTEGER, tareas_completadas INTEGER, monedas_gastadas INTEGER)")
        db?.execSQL("CREATE TABLE Upreferencias (usuario TEXT PRIMARY KEY, is_night_mode INTEGER)")
        db?.execSQL("CREATE TABLE misiones (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT, descripcion TEXT, fecha TEXT, usuario TEXT, vecesCompletada INTEGER)")
        db?.execSQL("CREATE TABLE recompensas (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT, descripcion TEXT, costo INTEGER, usuario TEXT)")
        db?.execSQL("CREATE TABLE misiones_recompensas (id INTEGER PRIMARY KEY AUTOINCREMENT, id_mision INTEGER, id_recompensa INTEGER)")


    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }
}