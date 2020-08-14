# Room SQLite Bindings
Bindings between the Android Room library and the SQLite database from sqlite.org (not the one bundled with Android).

## Usage
1. Download the latest version of SQLite from the sqlite.org website.
https://sqlite.org/download.html

1. Import the sqlite-android-xxxxxxx.aar file into your project

1. Add the following Maven url to your projects top level build.gradle file.


        allprojects {
            repositories {
                maven { url 'https://dl.bintray.com/apkelly/dev-nibbles' }
           }
        }

1. Add the following dependency to the module that uses Room.


        implementation "dev.nibbles.android.room:room_sqlite_bindings:1.0.0"

1. Add the .openHelperFactory(SQLiteOrgOpenHelperFactory) binding to your Room database configuration.


        @Database(entities = [MyEntity::class], version = 1, exportSchema = false)
        abstract class MyRoomDatabase : RoomDatabase() {

            abstract fun myDao(): MyRoomDao

            companion object {
                // Singleton prevents multiple instances of database opening at the same time.
                @Volatile
                private var INSTANCE: MyRoomDatabase? = null

                fun getDatabase(context: Context): MyRoomDatabase {
                    return INSTANCE ?: synchronized(this) {
                        INSTANCE ?: Room.databaseBuilder(
                            context.applicationContext,
                            MyRoomDatabase::class.java,
                            "my_database"
                        )
                        .openHelperFactory(SQLiteOrgOpenHelperFactory)
                        .build().also {
                            INSTANCE = it
                        }
                    }
                }
            }
        }

## Building
Run the following command to build and upload a new version of the library to bintray.
        ./gradlew clean build bintrayUpload

## Contributions
If you find any bugs, or you have any improvements please do reach out I'm more than happy to include fixes where possible.