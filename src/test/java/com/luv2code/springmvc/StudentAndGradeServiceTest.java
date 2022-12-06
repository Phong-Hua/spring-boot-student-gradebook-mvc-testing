package com.luv2code.springmvc;

import com.luv2code.springmvc.models.*;
import com.luv2code.springmvc.repository.HistoryGradeDao;
import com.luv2code.springmvc.repository.MathGradeDao;
import com.luv2code.springmvc.repository.ScienceGradeDao;
import com.luv2code.springmvc.repository.StudentDao;
import com.luv2code.springmvc.service.StudentAndGradeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestPropertySource("/application-test.properties") // load properties during testing as reference
@SpringBootTest
public class StudentAndGradeServiceTest {

    @Autowired
    StudentDao studentDao;
    @Autowired
    StudentAndGradeService studentService;
    @Autowired
    MathGradeDao mathGradeDao;
    @Autowired
    ScienceGradeDao scienceGradeDao;
    @Autowired
    HistoryGradeDao historyGradeDao;
    @Autowired
    private JdbcTemplate jdbc;  // JdbcTemplate is a helper class provided by Spring to help execute JDBC operations

    @Value("${sql.script.insert.student}")
    private String sqlInsertStudent;

    @Value("${sql.script.delete.student}")
    private String sqlDeleteStudent;

    @Value("${sql.script.insert.math.grade}")
    private String sqlInsertMathGrade;

    @Value("${sql.script.delete.math.grade}")
    private String sqlDeleteMathGrade;

    @Value("${sql.script.insert.science.grade}")
    private String sqlInsertScienceGrade;

    @Value("${sql.script.delete.science.grade}")
    private String sqlDeleteScienceGrade;

    @Value("${sql.script.insert.history.grade}")
    private String sqlInsertHistoryGrade;

    @Value("${sql.script.delete.history.grade}")
    private String sqlDeleteHistoryGrade;

    @BeforeEach
    public void setupDatabase() {

        jdbc.execute(sqlInsertStudent);
        jdbc.execute(sqlInsertMathGrade);
        jdbc.execute(sqlInsertScienceGrade);
        jdbc.execute(sqlInsertHistoryGrade);

    }

    @AfterEach
    public void cleanupDatabase() {
        jdbc.execute(sqlDeleteStudent);
        jdbc.execute(sqlDeleteMathGrade);
        jdbc.execute(sqlDeleteScienceGrade);
        jdbc.execute(sqlDeleteHistoryGrade);
    }

    @Test
    public void createStudentService() {
        studentService.createStudent("Chad", "Darby", "chad.darby@luv2code.com");

        CollegeStudent student = studentDao.findByEmailAddress("chad.darby@luv2code.com");

        assertEquals("chad.darby@luv2code.com", student.getEmailAddress(), "find by email");
    }

    @Test
    public void isStudentNullCheck() {
        assertFalse(studentService.checkIfStudentIsNull(1));
        assertTrue(studentService.checkIfStudentIsNull(0));
    }

    @Test
    public void deleteStudentService() {

        Optional<CollegeStudent> studentOptional = studentDao.findById(1);
        Iterable<MathGrade> deleteMathGrade = mathGradeDao.findGradeByStudentId(1);
        Iterable<HistoryGrade> deleteHistoryGrade = historyGradeDao.findGradeByStudentId(1);
        Iterable<ScienceGrade> deleteScienceGrade = scienceGradeDao.findGradeByStudentId(1);

        assertTrue(studentOptional.isPresent());
        // assert grades link to student
        assertTrue(deleteMathGrade.iterator().hasNext(), "Student has math grades");
        assertTrue(deleteHistoryGrade.iterator().hasNext(), "Student has history grades");
        assertTrue(deleteScienceGrade.iterator().hasNext(), "Student has science grades");

        // delete student
        studentService.deleteStudent(1);
        studentOptional = studentDao.findById(1);

        deleteMathGrade = mathGradeDao.findGradeByStudentId(1);
        deleteHistoryGrade = historyGradeDao.findGradeByStudentId(1);
        deleteScienceGrade = scienceGradeDao.findGradeByStudentId(1);

        assertTrue(studentOptional.isEmpty(), "Delete student");

        assertFalse(deleteMathGrade.iterator().hasNext(), "Math grades are deleted");
        assertFalse(deleteHistoryGrade.iterator().hasNext(), "History grades are deleted");
        assertFalse(deleteScienceGrade.iterator().hasNext(), "Science grades are deleted");
    }

    @Sql("/sql/insert.sql") // run the sql script at this location for this test. This will run after @BeforeEach and before run the test
    @Test
    public void getGradeBookService() {
        Iterable<CollegeStudent> iterableCollegeStudents = studentService.getGradebook();

        List<CollegeStudent> collegeStudents = new ArrayList<>();
        iterableCollegeStudents.forEach(collegeStudents::add);

        assertEquals(4, collegeStudents.size());
    }

    @Test
    public void createGradeService() {

        // Create a grade
        assertTrue(studentService.createGrade(80.5, 1, "math"));
        assertTrue(studentService.createGrade(90.5, 1, "science"));
        assertTrue(studentService.createGrade(88.0, 1, "history"));

        // Get all grades with studentId
        Iterable<MathGrade> mathGrades = mathGradeDao.findGradeByStudentId(1);
        Iterable<ScienceGrade> scienceGrades = scienceGradeDao.findGradeByStudentId(1);
        Iterable<HistoryGrade> historyGrades = historyGradeDao.findGradeByStudentId(1);

        // verify there is grades
        assertEquals(((Collection<MathGrade>) mathGrades).size(), 2, "Student has math grades");
        assertEquals(((Collection<ScienceGrade>) scienceGrades).size(), 2, "Student has science grades");
        assertEquals(((Collection<HistoryGrade>) historyGrades).size(), 2, "Student has history grades");

    }

    @Test
    public void createGradeServiceReturnFalse() {

        assertFalse(studentService.createGrade(105, 1, "math"), "Grade is bigger than 100");
        assertFalse(studentService.createGrade(-5, 1, "math"), "Grade is negative");
        assertFalse(studentService.createGrade(80.5, 2, "math"), "Student does not exist");
        assertFalse(studentService.createGrade(80.5, 1, "literature"), "Grade type is invalid");
    }

    @Test
    public void deleteGradeService() {
        assertEquals(1, studentService.deleteGrade(1, "math"), "Return student id after delete math grade");
        assertEquals(1, studentService.deleteGrade(1, "history"), "Return student id after delete history grade");
        assertEquals(1, studentService.deleteGrade(1, "science"), "Return student id after delete science grade");
    }

    @Test
    public void deleteGradeServiceReturnStudentIdOfZero() {
        // delete invalid grade id
        assertEquals(0, studentService.deleteGrade(0, "math"), "No student should have math grade id of 0");
        assertEquals(0, studentService.deleteGrade(0, "science"), "No student should have science grade id of 0");
        assertEquals(0, studentService.deleteGrade(0, "history"), "No student should have history grade id of 0");

        // delete invalid grade type
        assertEquals(0, studentService.deleteGrade(1, "literature"), "No student should have literature class");

    }

    @Test
    public void studentInformation(){

        GradebookCollegeStudent gradebookCollegeStudent = studentService.studentInformation(1);
        assertNotNull(gradebookCollegeStudent);
        assertEquals(1, gradebookCollegeStudent.getId());
        assertEquals("Rick", gradebookCollegeStudent.getFirstname());
        assertEquals("Norman", gradebookCollegeStudent.getLastname());
        assertEquals("rick.norman@luv2code.com", gradebookCollegeStudent.getEmailAddress());

        StudentGrades studentGrades = gradebookCollegeStudent.getStudentGrades();
        assertNotNull(studentGrades);
        assertEquals(1, studentGrades.getMathGradeResults().size());
        assertEquals(1, studentGrades.getHistoryGradeResults().size());
        assertEquals(1, studentGrades.getScienceGradeResults().size());
    }

    @Test
    public void studentInformationNotFound(){
        GradebookCollegeStudent gradebookCollegeStudent = studentService.studentInformation(0);
        assertNull(gradebookCollegeStudent, "Student not found");
    }
}
