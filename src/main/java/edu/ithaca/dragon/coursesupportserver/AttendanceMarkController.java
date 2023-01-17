package edu.ithaca.dragon.coursesupportserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.ithaca.dragon.coursesupportserver.reportmodel.AttendanceCourseReport;
import edu.ithaca.dragon.coursesupportserver.reportmodel.AttendanceReportMark;
import edu.ithaca.dragon.coursesupportserver.reportmodel.AttendanceStudentReport;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api")
public class AttendanceMarkController {

    private final AttendanceMarkRepository attendanceMarkRepository;


    public AttendanceMarkController(AttendanceMarkRepository attendanceMarkRepository) {
        this.attendanceMarkRepository = attendanceMarkRepository;
    }


    @GetMapping("/attendanceMarks")
    public ResponseEntity<List<AttendanceMark>> getAttendanceMarks(
        @RequestParam(required=false) String courseId, 
        @RequestParam(defaultValue= "-1") int dayNumber
    ){
        List<AttendanceMark> responses=null;
        if (courseId!=null){
            if (dayNumber > 0){
                responses = attendanceMarkRepository.findByCourseIdAndDayNumber(courseId, dayNumber);
            }
            else {
                responses = attendanceMarkRepository.findByCourseId(courseId);
            }
        }
        else{
            if (dayNumber > 0){
                responses = attendanceMarkRepository.findByDayNumber(dayNumber);
            }
            else {
                responses = attendanceMarkRepository.findAll();
            }
        }
        if (responses != null && responses.size() != 0){
            return new ResponseEntity<>(responses, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @GetMapping("/recentAttendanceMarks")
    public ResponseEntity<List<AttendanceMark>> findMostRecentAttendanceMarks(@RequestParam(required=false) String courseId){
        List<AttendanceMark> marks = findMostRecentAttendanceMarks(attendanceMarkRepository.findByCourseId(courseId));
        return new ResponseEntity<>(marks, HttpStatus.OK);
    } 

    public static List<AttendanceMark> findMostRecentAttendanceMarks(List<AttendanceMark> marks){
        if (marks.isEmpty()){
            return new ArrayList<>();
        }
        else {
            AttendanceMark maxDayMark = Collections.max(marks, Comparator.comparing(AttendanceMark::getDayNumber));
            return marks.stream().filter((mark)-> mark.getDayNumber() == maxDayMark.getDayNumber()).collect(Collectors.toList());    
        }
    }

    @PostMapping("/attendanceMarks")
    public ResponseEntity<List<AttendanceMark>> recordAttendance(@RequestBody List<AttendanceMark> attendanceMarks){
        try {
            List<AttendanceMark> newDbResponse = attendanceMarkRepository.saveAll(attendanceMarks);
            return new ResponseEntity<>(newDbResponse, HttpStatus.CREATED);
          } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/attendanceMarks")
    public ResponseEntity<List<AttendanceMark>> updateAttendance(@RequestBody List<AttendanceMark> attendanceMarksIn) {
        List<AttendanceMark> responses = new ArrayList<>();
        for (AttendanceMark attendanceMarkIn : attendanceMarksIn){
            List<AttendanceMark> marksInRepo = attendanceMarkRepository.findByCourseIdAndDayNumberAndStudentId(attendanceMarkIn.getCourseId(), attendanceMarkIn.getDayNumber(), attendanceMarkIn.getStudentId());
            if (marksInRepo.size() == 1){
                AttendanceMark markInRepo = marksInRepo.get(0);
                markInRepo.setStatus(attendanceMarkIn.getStatus());
                responses.add(attendanceMarkRepository.save(markInRepo));
            }
            else {
                System.err.println("Warn: Expected to find 1 single attendanceMark matching: " + attendanceMarkIn + "but instead found " + marksInRepo.size() + ". Skipping this update");
            }
        }
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @GetMapping("/attendanceReport")
    public ResponseEntity<AttendanceCourseReport> generateAttendanceReport( @RequestParam String courseId){
        AttendanceCourseReport report = generateAttendanceReport(courseId, attendanceMarkRepository.findByCourseId(courseId));
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    public static AttendanceCourseReport generateAttendanceReport( String courseId, List<AttendanceMark> marksToUse){
        Map<String, AttendanceStudentReport> student2marks = new TreeMap<>();
        for (AttendanceMark mark : marksToUse){
            AttendanceStudentReport previousMarks = student2marks.get(mark.getStudentId());
            if (previousMarks != null){
                previousMarks.getMarks().add(new AttendanceReportMark(mark.getDayNumber(), mark.getStatus()));
            }
            else {
                AttendanceStudentReport newStudentMarks = new AttendanceStudentReport(mark);
                student2marks.put(mark.getStudentId(), newStudentMarks);
            }
        }
        return  new AttendanceCourseReport(courseId, student2marks.values());
    } 

    @GetMapping("/courseIds")
    public ResponseEntity<List<String>> findAllCourseIds(){
        return new ResponseEntity<>(findAllCourseIds(attendanceMarkRepository.findAll()), HttpStatus.OK);
    } 

    public static List<String> findAllCourseIds(List<AttendanceMark> marks){
        List<String> courseIds = new ArrayList<>();
        for (AttendanceMark mark : marks){
            if (!courseIds.contains(mark.getCourseId())){
                courseIds.add(mark.getCourseId());
            }
        }
        return courseIds;
    }

}
