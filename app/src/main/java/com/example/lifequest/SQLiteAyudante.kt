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
        db?.execSQL("CREATE TABLE Tareas (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT, monedas INTEGER, repeticiones INTEGER, tipoRepeticion TEXT, fechaInicio TEXT, completada INTEGER DEFAULT 0, ultimaRepeticion TEXT, vecesCompletada INTEGER DEFAULT 0, usuario TEXT, FOREIGN KEY (usuario) REFERENCES usuarios(usuario) ON DELETE CASCADE)")
        db?.execSQL("CREATE TABLE premios (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT, costo INTEGER, usuario TEXT)")
        db?.execSQL("CREATE TABLE logros (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT, tarea_asociada TEXT NOT NULL, repeticiones_necesarias INTEGER DEFAULT 0, progreso INTEGER DEFAULT 0, completado INTEGER DEFAULT 0, premio int, usuario TEXT)")
    }
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }
}