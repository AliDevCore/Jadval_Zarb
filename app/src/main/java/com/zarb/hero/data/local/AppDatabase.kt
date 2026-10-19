package com.zarb.hero.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        QuizEntity::class,
        QuizQuestionEntity::class,
        QuestionStatEntity::class,
        AppPreferenceEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun quizDao(): QuizDao
    abstract fun questionStatDao(): QuestionStatDao
    abstract fun appPreferenceDao(): AppPreferenceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "zarb_hero.db"
                )
                    // هر تغییر آینده در ساختار جدول‌ها باید با Migration مشخص انجام شود
                    // (از fallbackToDestructiveMigration استفاده نکنید؛ سوابق کودک پاک می‌شود).
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
