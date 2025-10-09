package com.timetable.problem_solver.model;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StaffTimeTableDto {

    @Data
    @NoArgsConstructor
    public static class RequestDto{
        private Long jobId;
    }

    public interface GetSectionListFromProc {
        public String getSectionId();

        public String getSubjectId();

        public String getTeachers();

        public String getTeachersubject();

        public String getPeriods();
    }

    @Data
    @NoArgsConstructor
    public static class ArrangeBasedOnSection {
        private String sectionId;
        private List<String> subjects;
        private List<String> teachers;
        private List<TeacherSubjectHandling> teacherSubjectHandling;
        private List<Periods> periods;
        private Map<String, SubjectDetails> subjectDetailsMap = new HashMap<>();
        private Map<String, Integer> subjectWeeklyCount = new HashMap<>();
        private Map<String, List<String>> subjectDayTracker;

        public ArrangeBasedOnSection(GetSectionListFromProc getSectionListFromProc) {
            this.sectionId = getSectionListFromProc.getSectionId();
            if (Objects.nonNull(getSectionListFromProc.getSubjectId())) {
                this.subjects = List.of(getSectionListFromProc.getSubjectId().split(","));
            }
            if (Objects.nonNull(getSectionListFromProc.getTeachers())) {
                this.teachers = List.of(getSectionListFromProc.getTeachers().split(","));
            }
            if (Objects.nonNull(getSectionListFromProc.getTeachersubject())) {
                this.teacherSubjectHandling = teacherSubjectHandlings(List.of(getSectionListFromProc.getTeachersubject().split(",")));
            }
            if (Objects.nonNull(getSectionListFromProc.getPeriods())) {
                this.periods = periods(List.of(getSectionListFromProc.getPeriods().split(",")));
            }
        }

        public List<TeacherSubjectHandling> teacherSubjectHandlings(List<String> data) {
            return data.stream().map(TeacherSubjectHandling::new).toList();
        }

        public List<Periods> periods(List<String> data) {
            return data.stream().map(Periods::new).toList();
        }

        @Getter
        @Setter
        @NoArgsConstructor
        public static class SubjectDetails {
            private Boolean isTheory;
            private Boolean isPractical;
            private String difficultyLevel;
        }

    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class TeacherSubjectHandling {
        private String teacher;
        private String subject;

        public TeacherSubjectHandling(String data) {
            if (Objects.nonNull(data)) {
                String[] splits = data.split("/");
                this.subject = splits[1];
                this.teacher = splits[0];
            }
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Periods {
        private String id;
        private String order;

        public Periods(String data) {
            if (Objects.nonNull(data)) {
                String[] splits = data.split("/");
                this.id = splits[1];
                this.order = splits[0];
            }
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class FinalList{
        private String day;
        private String section;
        private String period;
        private String subject;
        private String staff;

        public FinalList(String day, String section, String period, String subject, String staff) {
            this.day = day;
            this.section = section;
            this.period = period;
            this.subject = subject;
            this.staff = staff;
        }
    }

    public interface TimetableListDb{
        public Long getSectionId();
        public String getSectionName();
        public Long getPeriodId();
        public Long getPeriodOrder();
        public Long getSubjectId();
        public String getSubjectName();
        public Long getStaffId();
        public String getStaffName();
        public String getDays();
        public Boolean getIsPeriod();
        public String getPeriodTimeString();
        public String getPeriodName();
        public Long getClassId();
        public String getClassName();
    }

    public interface TimetableViewDb{
        public Long getSectionId();
        public String getSectionName();
        public Long getClassId();
        public String getClassName();
    }

    @Data
    @NoArgsConstructor
    public static class TimeTableResponse{
        private SectionTime section;
        private Map<String,List<TimeTableList>> days;
    }
    @Data
    @NoArgsConstructor
    public static class TimeTableList{
        private PeriodTime period;
        private SubjectTime subject;
        private StaffTime staff;

    }

    @Data
    @NoArgsConstructor
    public static class TimeTableView{
        private SectionTime section;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class SectionTime{
        private Long id;
        private String sectionName;
        private ClassMasterTime classMaster;

        public SectionTime(Long sectionId, String sectionName) {
            this.id = sectionId;
            this.sectionName = sectionName;
        }
        public SectionTime(Long sectionId, String sectionName,Long classId,String className) {
            this.id = sectionId;
            this.sectionName = sectionName;
            classMaster=new ClassMasterTime(classId,className);
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class PeriodTime{
        private Long id;
        private Long periodOrder;
        private String periodTimeString;
        private String periodName;
        private Boolean isPeriod;

        public PeriodTime(Long periodId, Long periodOrder,String periodTimeString,Boolean isPeriod,String periodName) {
            this.id = periodId;
            this.periodOrder = periodOrder;
            this.periodTimeString=periodTimeString;
            this.isPeriod=isPeriod;
            this.periodName=periodName;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class SubjectTime{
        private Long id;
        private String subjectName;

        public SubjectTime(Long subjectId, String subjectName) {
            this.id = subjectId;
            this.subjectName = subjectName;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class StaffTime{
        private Long id;
        private String staffName;

        public StaffTime(Long staffId, String staffName) {
            this.id = staffId;
            this.staffName = staffName;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ClassMasterTime{
        private Long id;
        private String className;

        public ClassMasterTime(Long id, String className) {
            this.id = id;
            this.className = className;
        }
    }
}
