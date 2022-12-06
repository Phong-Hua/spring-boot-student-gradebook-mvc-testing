package com.luv2code.springmvc;

import com.luv2code.springmvc.models.CollegeStudent;
import com.luv2code.springmvc.models.HistoryGrade;
import com.luv2code.springmvc.models.MathGrade;
import com.luv2code.springmvc.models.ScienceGrade;
import com.luv2code.springmvc.repository.HistoryGradeDao;
import com.luv2code.springmvc.repository.MathGradeDao;
import com.luv2code.springmvc.repository.ScienceGradeDao;
import com.luv2code.springmvc.repository.StudentDao;
import com.luv2code.springmvc.service.StudentAndGradeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestPropertySource("/application.properties") // load properties during testing as reference
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

    @BeforeEach
    public void setupDatabase() {
        jdbc.execute("INSERT INTO student(id, firstname, lastname, email_address)" +
                "VALUES(1, 'Rick', 'Norman', 'rick.norman@luv2code.com')");

        jdbc.execute("INSERT INTO math_grade(id, student_id, grade) VALUES(1, 1, 100.0)");
        jdbc.execute("INSERT INTO science_grade(id, student_id, grade) VALUES(1, 1, 100.0)");
        jdbc.execute("INSERT INTO history_grade(id, student_id, grade) VALUES(1, 1, 100.0)");

    }

    @AfterEach
    public void cleanupDatabase() {
        jdbc.execute("DELETE FROM student;");
        jdbc.execute("DELETE FROM math_grade;");
        jdbc.execute("DELETE FROM science_grade;");
        jdbc.execute("DELETE FROM history_grade;");
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
        assertTrue(studentOptional.isPresent());

        studentService.deleteStudent(1);
        studentOptional = studentDao.findById(1);
        assertTrue(studentOptional.isEmpty(), "Delete student");
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
}
