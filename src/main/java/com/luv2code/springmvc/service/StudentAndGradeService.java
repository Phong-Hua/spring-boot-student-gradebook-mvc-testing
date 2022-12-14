package com.luv2code.springmvc.service;

import com.luv2code.springmvc.models.*;
import com.luv2code.springmvc.repository.HistoryGradeDao;
import com.luv2code.springmvc.repository.MathGradeDao;
import com.luv2code.springmvc.repository.ScienceGradeDao;
import com.luv2code.springmvc.repository.StudentDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional  // Allows SpringBoot manage transaction for us in the background
public class StudentAndGradeService {

    @Autowired
    private StudentDao studentDao;

    @Autowired
    private MathGradeDao mathGradeDao;

    @Autowired
    private ScienceGradeDao scienceGradeDao;

    @Autowired
    private HistoryGradeDao historyGradeDao;

    @Autowired
    @Qualifier("mathGrades")
    private MathGrade mathGrade;    // we use the mathGrade bean defined in the MvcTestingExampleApplication

    @Autowired
    @Qualifier("scienceGrades")
    private ScienceGrade scienceGrade;

    @Autowired
    @Qualifier("historyGrades")
    private HistoryGrade historyGrade;

    @Autowired
    private StudentGrades studentGrades;

    public void createStudent(String firstname, String lastname, String email) {
        CollegeStudent student = new CollegeStudent(firstname, lastname, email);
        student.setId(0);
        studentDao.save(student);
    }

    public boolean checkIfStudentIsNull(int id) {
        return !studentDao.existsById(id);
    }

    public void deleteStudent(int id) {
        if (!checkIfStudentIsNull(id)) {
            studentDao.deleteById(id);
            mathGradeDao.deleteByStudentId(id);
            historyGradeDao.deleteByStudentId(id);
            scienceGradeDao.deleteByStudentId(id);
        }

    }

    public Iterable<CollegeStudent> getGradebook() {
        Iterable<CollegeStudent> collegeStudents = studentDao.findAll();
        return collegeStudents;
    }

    public boolean createGrade(double grade, int studentId, String type) {

        if (checkIfStudentIsNull(studentId))
            return false;

        if (grade >= 0 && grade <= 100) {
            if (type.equals("math")) {
                mathGrade.setId(0);
                mathGrade.setGrade(grade);
                mathGrade.setStudentId(studentId);
                mathGradeDao.save(mathGrade);
                return true;
            } else if (type.equals("science")) {
                scienceGrade.setId(0);
                scienceGrade.setGrade(grade);
                scienceGrade.setStudentId(studentId);
                scienceGradeDao.save(scienceGrade);
                return true;
            } else if (type.equals("history")){
                historyGrade.setId(0);
                historyGrade.setGrade(grade);
                historyGrade.setStudentId(studentId);
                historyGradeDao.save(historyGrade);
                return true;
            }
        }
        return false;
    }

    public int deleteGrade(int gradeId, String type) {
        int studentId = 0;

        if (type.equals("math")) {
            Optional<MathGrade> gradeOptional = mathGradeDao.findById(gradeId);
            if (gradeOptional.isPresent()) {
                studentId = gradeOptional.get().getStudentId();
                mathGradeDao.deleteById(gradeId);
            }
        } else if (type.equals("history")){
            Optional<HistoryGrade> gradeOptional = historyGradeDao.findById(gradeId);
            if (gradeOptional.isPresent()) {
                studentId = gradeOptional.get().getStudentId();
                historyGradeDao.deleteById(gradeId);
            }
        } else if (type.equals("science")){
            Optional<ScienceGrade> gradeOptional = scienceGradeDao.findById(gradeId);
            if (gradeOptional.isPresent()) {
                studentId = gradeOptional.get().getStudentId();
                scienceGradeDao.deleteById(gradeId);
            }
        }
        return studentId;
    }

    public GradebookCollegeStudent studentInformation(int studentId) {
        Optional<CollegeStudent> studentOptional = studentDao.findById(studentId);
        if (studentOptional.isEmpty())
            return null;
        CollegeStudent student = studentOptional.get();
        Iterable<MathGrade> mathGradesIter = mathGradeDao.findGradeByStudentId(studentId);
        Iterable<ScienceGrade> scienceGradesIter = scienceGradeDao.findGradeByStudentId(studentId);
        Iterable<HistoryGrade> historyGradesIter = historyGradeDao.findGradeByStudentId(studentId);
        List<Grade> mathGrades = new ArrayList<>();
        List<Grade> historyGrades = new ArrayList<>();
        List<Grade> scienceGrades = new ArrayList<>();
        mathGradesIter.forEach(mathGrades::add);
        scienceGradesIter.forEach(scienceGrades::add);
        historyGradesIter.forEach(historyGrades::add);

        studentGrades.setMathGradeResults(mathGrades);
        studentGrades.setHistoryGradeResults(historyGrades);
        studentGrades.setScienceGradeResults(scienceGrades);

        GradebookCollegeStudent gradebookCollegeStudent = new GradebookCollegeStudent(student.getId(), student.getFirstname(),
                student.getLastname(), student.getEmailAddress(), studentGrades);

        return gradebookCollegeStudent;
    }

    public void configureStudentInformationModel(int id, Model m) {

        GradebookCollegeStudent student = studentInformation(id);
        m.addAttribute("student", student);
        if (!student.getStudentGrades().getMathGradeResults().isEmpty()) {
            m.addAttribute("mathAverage", student.getStudentGrades().findGradePointAverage(
                    student.getStudentGrades().getMathGradeResults()));
        } else {
            m.addAttribute("mathAverage", "N/A");
        }

        if (!student.getStudentGrades().getScienceGradeResults().isEmpty()) {
            m.addAttribute("scienceAverage", student.getStudentGrades().findGradePointAverage(
                    student.getStudentGrades().getScienceGradeResults()));
        } else {
            m.addAttribute("scienceAverage", "N/A");
        }

        if (!student.getStudentGrades().getHistoryGradeResults().isEmpty()) {
            m.addAttribute("historyAverage", student.getStudentGrades().findGradePointAverage(
                    student.getStudentGrades().getHistoryGradeResults()));
        } else {
            m.addAttribute("historyAverage", "N/A");
        }
    }
}
