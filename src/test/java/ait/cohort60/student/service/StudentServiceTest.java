package ait.cohort60.student.service;

import ait.cohort60.configuration.ServiceConfiguration;
import ait.cohort60.student.dao.StudentRepository;
import ait.cohort60.student.dto.ScoreDto;
import ait.cohort60.student.dto.StudentCredentialsDto;
import ait.cohort60.student.dto.StudentDto;
import ait.cohort60.student.dto.StudentUpdateDto;
import ait.cohort60.student.dto.exeptions.NotFoundException;
import ait.cohort60.student.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// AAA - Arrange, Act, Assert

@ContextConfiguration(classes = {ServiceConfiguration.class})
@SpringBootTest
public class StudentServiceTest {
    private final long studentId = 1000;
    private final String name = "John";
    private final String password = "1234";
    private Student student;

    @MockitoBean
    private StudentRepository studentRepository;
    private StudentService studentService;
    @Autowired
    private ModelMapper modelMapper;

    @BeforeEach
    public void setUp() {
        student = new Student(studentId, name, password);
        studentService = new StudentServiceImpl(studentRepository, modelMapper);
    }

    @Test
    void testAddStudentWhenStudentDoesNotExist() {
        // Arrange
        StudentCredentialsDto dto = new StudentCredentialsDto(studentId, name, password);
        when(studentRepository.save(any(Student.class))).thenReturn(student);
        // Act
        boolean result = studentService.addStudent(dto);
        //Assert
        assertTrue(result);
    }

    @Test
    void testAddStudentWhenStudentExists() {
        // Arrange
        StudentCredentialsDto dto = new StudentCredentialsDto(studentId, name, password);
        when(studentRepository.existsById(dto.getId())).thenReturn(true);
        // Act
        boolean result = studentService.addStudent(dto);
        //Assert
        assertFalse(result);
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void testFindStudentWhenStudentExists() {
        // Arrange
        when(studentRepository.findById(studentId)).thenReturn(Optional.ofNullable(student));
        // Act
        StudentDto studentDto = studentService.findStudent(studentId);
        // Assert
        assertNotNull(studentDto);
        assertEquals(studentId, studentDto.getId());
    }

    @Test
    void testFindStudentWhenStudentNotExists() {
        // Arrange
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());
        // Act & Assert
        assertThrows(NotFoundException.class, () -> studentService.findStudent(studentId));
    }

    @Test
    void testRemoveStudent() {
        // Arrange
        when(studentRepository.findById(studentId)).thenReturn(Optional.ofNullable(student));
        //Act
        StudentDto studentDto = studentService.removeStudent(studentId);
        //Assert
        assertNotNull(studentDto);
        assertEquals(studentId, studentDto.getId());
        verify(studentRepository, times(1)).findById(studentId);
    }

    @Test
    void testUpdateStudent() {
        // Arrange
        String newName = "NewName";
        when(studentRepository.findById(studentId)).thenReturn(Optional.ofNullable(student));
        StudentUpdateDto dto = new StudentUpdateDto(newName, null);
        // Act
        StudentCredentialsDto studentCredentialsDto = studentService.updateStudent(studentId, dto);

        // Assert
        assertNotNull(studentCredentialsDto);
        assertEquals(studentId, studentCredentialsDto.getId());
        assertEquals(newName, studentCredentialsDto.getName());
        assertEquals(password, studentCredentialsDto.getPassword());
        verify(studentRepository, times(1)).save(any(Student.class));

    }

    @Test
    void testAddScoreWhenStudentExists() {
        // Arrange
        ScoreDto scoreDto = new ScoreDto("Math", 90);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));

        // Act
        Boolean result = studentService.addScore(studentId, scoreDto);

        // Assert
        assertTrue(result);
        verify(studentRepository).save(student);
    }

    @Test
    void testAddScoreWhenStudentNotExists() {
        ScoreDto scoreDto = new ScoreDto("Math", 90);
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> studentService.addScore(studentId, scoreDto));
    }

    @Test
    void testFindStudentsByName() {
        List<Student> students = List.of(new Student(studentId, name, password));
        when(studentRepository.findByNameIgnoreCase(name)).thenReturn(students.stream());

        List<StudentDto> result = studentService.findStudentsByName(name);

        assertEquals(1, result.size());
        assertEquals(name, result.get(0).getName());
    }

    @Test
    void testCountStudentsByNames() {
        // Arrange
        Set<String> names = Set.of("John", "Jane");
        when(studentRepository.countByNameInIgnoreCase(names)).thenReturn(2L);

        // Act
        Long count = studentService.countStudentsByNames(names);

        // Assert
        assertEquals(2L, count);
    }

    @Test
    void testFindStudentsByExamNameMinScore() {
        // Arrange
        student.addScore("Math", 95);
        List<Student> students = List.of(student);
        when(studentRepository.findByExamAndScoreGreaterThan("Math", 90))
                .thenReturn(students.stream());

        // Act
        List<StudentDto> result = studentService.findStudentsByExamNameMinScore("Math", 90);

        // Assert
        assertEquals(1, result.size());
        assertEquals(studentId, result.get(0).getId());
        assertEquals(95, result.get(0).getScores().get("Math"));
    }
}
