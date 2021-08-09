# couchbase-wrapper-android

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) 

**Couchbase Lite Wrapper** is a library written in Kotlin that makes it easy for you to implement database CRUD operations with [Couchbase Lite for Android](https://github.com/couchbase/couchbase-lite-android)

## Requirements
- Api 21+

## Installation

Make sure you have the Maven Central repository in root build.gradle:
```groovy
allprojects {
    repositories {
        mavenCentral()
    }
}   

```
Then add the dependency with:
```groovy
dependencies {
    implementation 'io.github.koombea:couchbaselitewrapper:1.0.0'
}

```

## The Basics

### Setup

Create a couchbase database for each of the models you want to store

```kotlin
val couchbaseDatabase = CouchbaseDatabase(context = context, databaseName = "User")

```
### Create / Update document

```kotlin 
val user = User(name = "Brad", lastname = "Depp")
val document = CouchbaseDocument(id = "1", attributes = user)
couchbaseDatabase.save(document)

```

### Fetch documents

```kotlin
val expression = Expression.property("attributes.name").equalTo(Expression.string("Brad"))
val documents = couchbaseDatabase.fetchAll<User>(whereExpression = expression)

```

### Delete Documents

```kotlin
val expression = Expression.property("attributes.name").equalTo(Expression.string("Brad"))
couchbaseDatabase.deleteAll(whereExpression = expression)

```