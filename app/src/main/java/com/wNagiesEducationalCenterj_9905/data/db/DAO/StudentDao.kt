package com.wNagiesEducationalCenterj_9905.data.db.DAO

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wNagiesEducationalCenterj_9905.data.db.Entities.StudentProfileEntity
import com.wNagiesEducationalCenterj_9905.data.db.Entities.StudentTeacherEntity

@Dao
interface StudentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStudentProfile(studentProfileEntity: StudentProfileEntity)

    @Query("SELECT * FROM profile WHERE token = :token")
    fun getStudentProfile(token: String): LiveData<StudentProfileEntity>

    @Query("DELETE FROM profile WHERE token = :token")
    fun deleteProfile(token: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStudentTeacher(studentTeacherEntityList: List<StudentTeacherEntity>)

    @Query("SELECT * FROM student_teacher WHERE token = :token")
    fun getClassTeacher(token: String):LiveData<List<StudentTeacherEntity>>

    @Query("DELETE FROM student_teacher WHERE token = :token")
    fun deleteClassTeacher(token: String)
}