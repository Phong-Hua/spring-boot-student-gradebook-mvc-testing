package com.luv2code.springmvc;

import com.luv2code.springmvc.models.CollegeStudent;
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
    private JdbcTemplate jdbc;  // JdbcTemplate is a helper class provided by Spring to help execute JDBC operations

    @BeforeEach
    public void setupDatabase() {
        jdbc.execute("INSERT INTO student(id, firstname, lastname, email_address)" +
                "VALUES(1, 'Rick', 'Norman', 'rick.norman@luv2code.com')");
    }

    @AfterEach
    public void cleanupDatabase() {
        jdbc.execute("DELETE FROM student;");
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
}
