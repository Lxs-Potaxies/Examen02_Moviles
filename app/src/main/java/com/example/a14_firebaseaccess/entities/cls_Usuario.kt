package com.example.a14_firebaseaccess.entities

class cls_Usuario {
    var token: String = ""
    var nombre: String = ""
    var email: String = ""
    var contra: String = ""

    constructor() {}

    constructor(id: String, nombre: String, email: String, contra: String) {
        this.token = id
        this.nombre = nombre
        this.email = email
        this.contra = contra
    }
}