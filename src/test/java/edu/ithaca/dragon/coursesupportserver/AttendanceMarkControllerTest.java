package edu.ithaca.dragon.coursesupportserver;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import edu.ithaca.dragon.coursesupportserver.reportmodel.AttendanceCourseReport;
import edu.ithaca.dragon.util.JsonUtil;

public class AttendanceMarkControllerTest {
        // @Test
    public void generateAttendanceReportTest() throws Exception {
        List<AttendanceMark> marksFor220 = AttendanceMarkRespositoryExamples.basicTestRepoList().stream().filter(mark-> mark.getCourseId().equals( "COMP220")).collect(Collectors.toList());
        AttendanceCourseReport report = AttendanceMarkController.generateAttendanceReport("COMP220", marksFor220);
        JsonUtil.toJsonFile("src/test/java/edu/ithaca/dragon/coursesupportserver/examples/AttendanceCourseReportExample.json", report);
    }

    @Test
    public void findAllCourseIds(){
        List<AttendanceMark> allMarks = AttendanceMarkRespositoryExamples.basicTestRepoList();
        List<String> allCourseIds = AttendanceMarkController.findAllCourseIds(allMarks);
        assertThat(allCourseIds).hasSize(2).hasSameElementsAs(Arrays.asList("COMP172","COMP220"));
    }

    @Test
    public void findMostRecentAttendanceMarksTest() throws Exception {
        List<AttendanceMark> marksFor220 = AttendanceMarkRespositoryExamples.basicTestRepoList().stream().filter(mark-> mark.getCourseId().equals( "COMP220")).collect(Collectors.toList());
        List<AttendanceMark> marks = AttendanceMarkController.findMostRecentAttendanceMarks(marksFor220);
        assertThat(marks).hasSize(5);
        for (AttendanceMark mark: marks){
            assertEquals(6, mark.getDayNumber());
        }
        JsonUtil.toJsonFile("src/test/java/edu/ithaca/dragon/coursesupportserver/examples/AttendanceMarksExample.json", marks);

        List<AttendanceMark> marksFor172 = AttendanceMarkRespositoryExamples.basicTestRepoList().stream().filter(mark-> mark.getCourseId().equals( "COMP172")).collect(Collectors.toList());
        marks = AttendanceMarkController.findMostRecentAttendanceMarks(marksFor172);
        assertThat(marks).hasSize(4);
        for (AttendanceMark mark: marks){
            assertEquals(6, mark.getDayNumber());
        }

        assertEquals(0,  AttendanceMarkController.findMostRecentAttendanceMarks(new ArrayList<>()).size());
    }

}
