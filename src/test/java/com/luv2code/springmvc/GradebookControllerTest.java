package com.luv2code.springmvc;

import com.luv2code.springmvc.models.CollegeStudent;
import com.luv2code.springmvc.models.GradebookCollegeStudent;
import com.luv2code.springmvc.repository.MathGradeDao;
import com.luv2code.springmvc.repository.StudentDao;
import com.luv2code.springmvc.service.StudentAndGradeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.servlet.ModelAndView;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource("/application-test.properties")
@AutoConfigureMockMvc
@SpringBootTest
public class GradebookControllerTest {

    private static MockHttpServletRequest request;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private StudentAndGradeService studentCreateServiceMock;

    @Autowired
    private StudentAndGradeService studentService;

    @Autowired
    private StudentDao studentDao;

    @Autowired
    private MathGradeDao mathGradesDao;

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

    @BeforeAll
    public static void setup() {
        request = new MockHttpServletRequest();
        request.setParameter("firstname", "Chad");
        request.setParameter("lastname", "Darby");
        request.setParameter("emailAddress", "chad.darby@luv2code.com");
    }

    @BeforeEach
    public void beforeEach() {
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

    /**
     * This test to make sure we got the setup correct.
     * @throws Exception
     */
    @Test
    public void getStudentHttpRequest() throws Exception {

        // setup
        CollegeStudent studentOne = new GradebookCollegeStudent("Eric", "Roby", "eric.roby@luv2code.com");
        CollegeStudent studentTwo = new GradebookCollegeStudent("Chad", "Darby", "chad.darby@luv2code.com");

        List<CollegeStudent> collegeStudentList = new ArrayList<>(Arrays.asList(studentOne, studentTwo));

        // action
        when(studentCreateServiceMock.getGradebook()).thenReturn(collegeStudentList);

        // verify
        assertIterableEquals(collegeStudentList, studentCreateServiceMock.getGradebook());

        // retrieve result when visit '/'
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/"))
                .andExpect(status().isOk()).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        // assert return view name of index
        ModelAndViewAssert.assertViewName(mav, "index");
    }

    @Test
    public void createStudentHttpRequest() throws Exception {

        CollegeStudent studentOne = new CollegeStudent("Eric", "Roby", "eric_roby@luv2code.com");

        List<CollegeStudent> collegeStudentList = new ArrayList<>(Arrays.asList(studentOne));
        when(studentCreateServiceMock.getGradebook()).thenReturn(collegeStudentList);

        assertIterableEquals(studentCreateServiceMock.getGradebook(), collegeStudentList);

        MvcResult mvcResult = this.mockMvc.perform(post("/")
                .contentType(MediaType.APPLICATION_JSON)
                .param("firstname", request.getParameterValues("firstname"))
                .param("lastname", request.getParameterValues("lastname"))
                .param("emailAddress", request.getParameterValues("emailAddress"))
            ).andExpect(status().isOk()).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(mav, "index");

        // verify in db
        assertNotNull(studentDao.findByEmailAddress("chad.darby@luv2code.com"));
    }

    @Test
    public void deleteStudentHttpRequest() throws Exception {

        // Make sure the studentId exist first
        assertTrue(studentDao.findById(1).isPresent());

        MvcResult mvcResult = this.mockMvc.perform(
                get("/delete/student/{id}", 1))
                .andExpect(status().isOk()).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(mav, "index");

        assertFalse(studentDao.findById(1).isPresent());
    }

    @Test
    public void deleteStudentHttpRequestErrorPage() throws Exception {

        assertTrue(studentDao.findById(2).isEmpty());

        MvcResult mvcResult = this.mockMvc.perform(
                get("/delete/student/{id}", 2))
                .andExpect(status().isOk()).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(mav, "error");

    }

    @Test
    public void studentInformationHttpRequest() throws Exception {

        assertTrue(studentDao.findById(1).isPresent(), "Ensure student exist before test");

        MvcResult mvcResult = this.mockMvc.perform(
                get("/studentInformation/{id}", 1))
                .andExpect(status().isOk()).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(mav, "studentInformation");
        ModelAndViewAssert.assertModelAttributeAvailable(mav, "student");
        ModelAndViewAssert.assertModelAttributeAvailable(mav, "mathAverage");
        ModelAndViewAssert.assertModelAttributeAvailable(mav, "scienceAverage");
        ModelAndViewAssert.assertModelAttributeAvailable(mav, "historyAverage");
    }

    @Test
    public void studentInformationHttpRequestErrorPage() throws Exception {

        assertTrue(studentDao.findById(2).isEmpty(), "Ensure student does not exist before test");

        MvcResult mvcResult = this.mockMvc.perform(
                        get("/studentInformation/{id}", 2))
                .andExpect(status().isOk()).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(mav, "error");
    }

    @Test
    public void createValidGrade() throws Exception {
        assertTrue(studentDao.findById(1).isPresent(), "Ensure student exist before test");

        GradebookCollegeStudent collegeStudent = studentService.studentInformation(1);

        assertEquals(1, collegeStudent.getStudentGrades().getMathGradeResults().size(), "Ensure student has one math grade before action");

        MvcResult mvcResult = this.mockMvc.perform(
                    post("/grades")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("grade", "85.5")
                            .param("gradeType", "math")
                            .param("studentId", "1"))
                .andExpect(status().isOk()).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(mav, "studentInformation");

        collegeStudent = studentService.studentInformation(1);
        assertEquals(2, collegeStudent.getStudentGrades().getMathGradeResults().size(), "Ensure new math grade is added");

    }

    @Test
    public void createValidGradeHttpRequestStudentDoesNotExist() throws  Exception {

        assertFalse(studentDao.findById(2).isPresent(), "Ensure student does not exist before test");

        MvcResult mvcResult = this.mockMvc.perform(
                        post("/grades")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("grade", "85.5")
                                .param("gradeType", "math")
                                .param("studentId", "2"))
                .andExpect(status().isOk()).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(mav, "error");
    }

    @Test
    public void createGradeForInvalidSubject() throws  Exception {

        assertTrue(studentDao.findById(1).isPresent(), "Ensure student exists before test");

        MvcResult mvcResult = this.mockMvc.perform(
                        post("/grades")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("grade", "85.5")
                                .param("gradeType", "literature")
                                .param("studentId", "1"))
                .andExpect(status().isOk()).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(mav, "error");
    }

    @Test
    public void deleteGradeHttpRequest() throws  Exception {

        assertTrue(studentDao.findById(1).isPresent(), "Ensure student exists before test");
        assertEquals(1, ((Collection) mathGradesDao.findGradeByStudentId(1)).size(), "Ensure student must have one grade before test");

        MvcResult mvcResult = this.mockMvc.perform(
                get("/grades/{id}/{gradeType}", 1, "math"))
                .andExpect(status().isOk()).andReturn();


        ModelAndView mav = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(mav, "studentInformation");

        assertTrue(((Collection) mathGradesDao.findGradeByStudentId(1)).isEmpty(), "Ensure student does not have any grade after delete");
    }

    @Test
    public void deleteGradeHttpRequestInvalidGradeId() throws Exception {

        assertTrue(studentDao.findById(1).isPresent(), "Ensure student exists before test");
        assertFalse(mathGradesDao.existsById(2), "Ensure grade does not exist before test");

        MvcResult mvcResult = this.mockMvc.perform(
                        get("/grades/{id}/{gradeType}", 2, "math"))
                .andExpect(status().isOk()).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(mav, "error");
    }

    @Test
    public void deleteGradeHttpRequestInvalidSubject() throws Exception {
        assertTrue(studentDao.findById(1).isPresent(), "Ensure student exists before test");

        MvcResult mvcResult = this.mockMvc.perform(
                        get("/grades/{id}/{gradeType}", 2, "literature"))
                .andExpect(status().isOk()).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();
        ModelAndViewAssert.assertViewName(mav, "error");
    }
}
