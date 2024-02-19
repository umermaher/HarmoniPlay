package com.harmoniplay.utils

sealed class Resource<T> (val data : T?=null, val message:String?=null){
    class Success<T>(data:T): Resource<T>(data)
    class Error<T>(message: String?, data: T?=null): Resource<T>(data,message)
    class Loading<T>(): Resource<T>()
}

sealed class Resource2<T> (val data : T?=null, val message:String?=null){
    class Success<T>(data:T): Resource2<T>(data)
    class Error<T>(message: String?): Resource2<T>(message = message)
}